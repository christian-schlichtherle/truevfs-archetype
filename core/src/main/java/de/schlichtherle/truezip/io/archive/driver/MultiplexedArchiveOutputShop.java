/*
 * Copyright (C) 2006-2010 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schlichtherle.truezip.io.archive.driver;

import de.schlichtherle.truezip.io.socket.output.FilterOutputSocket;
import de.schlichtherle.truezip.io.rof.SimpleReadOnlyFile;
import de.schlichtherle.truezip.io.rof.ReadOnlyFile;
import de.schlichtherle.truezip.io.socket.input.CommonInputSocket;
import de.schlichtherle.truezip.util.BitField;
import de.schlichtherle.truezip.io.socket.entry.CommonEntry.Access;
import de.schlichtherle.truezip.io.socket.output.CommonOutputShop;
import de.schlichtherle.truezip.io.socket.output.FilterOutputShop;
import de.schlichtherle.truezip.io.socket.output.CommonOutputSocket;
import de.schlichtherle.truezip.io.socket.entry.CommonEntry;
import de.schlichtherle.truezip.io.socket.file.FileEntry;
import de.schlichtherle.truezip.io.socket.IOSocket;
import de.schlichtherle.truezip.io.ChainableIOException;
import de.schlichtherle.truezip.io.ChainableIOExceptionBuilder;
import de.schlichtherle.truezip.io.InputException;
import de.schlichtherle.truezip.io.socket.IOReference;
import de.schlichtherle.truezip.util.JointIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static de.schlichtherle.truezip.io.archive.driver.ArchiveEntry.UNKNOWN;
import static de.schlichtherle.truezip.io.Files.createTempFile;

/**
 * Decorates an {@code CommonOutputShop} in order to support a virtually
 * unlimited number of entries which may be written concurrently while
 * actually at most one entry is written concurrently to the output archive
 * output.
 * If there is more than one entry to be written concurrently, the additional
 * entries are actually written to temp files and copied to the output
 * output archive upon a call to their {@link OutputStream#close()} method.
 * Note that this implies that the {@code close()} method may fail with
 * an {@link IOException}.
 * <p>
 * Implementations do <em>not</em> need to be thread-safe:
 * Multithreading needs to be addressed by client applications.
 *
 * @param   <AE> The type of the archive entries.
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public class MultiplexedArchiveOutputShop<AE extends ArchiveEntry>
extends FilterOutputShop<AE, CommonOutputShop<AE>> {

    /** Prefix for temporary files created by the multiplexer. */
    static final String TEMP_FILE_PREFIX = "tzp-mux";

    /**
     * The map of temporary archive entries which have not yet been written
     * to the output output archive.
     */
    private final Map<String, TempEntryOutputStream> temps
            = new LinkedHashMap<String, TempEntryOutputStream>();

    /** @see #isTargetBusy */
    private boolean targetBusy;

    /**
     * Constructs a new {@code MultiplexedArchiveOutputShop}.
     * 
     * @param output the decorated output archive.
     * @throws NullPointerException iff {@code output} is {@code null}.
     */
    public MultiplexedArchiveOutputShop(final CommonOutputShop<AE> target) {
        super(target);
        if (target == null)
            throw new NullPointerException();
    }

    @Override
    public int size() {
        return target.size() + temps.size();
    }

    @Override
    public Iterator<AE> iterator() {
        return new JointIterator<AE>(target.iterator(), new TempEntriesIterator());
    }

    private class TempEntriesIterator implements Iterator<AE> {
        private final Iterator<TempEntryOutputStream> i
                = temps.values().iterator();

        @Override
        public boolean hasNext() {
            return i.hasNext();
        }

        @Override
        public AE next() {
            return i.next().getTarget();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("entry removal");
        }
    }

    @Override
    public AE getEntry(String name) {
        AE entry = target.getEntry(name);
        if (entry != null)
            return entry;
        final TempEntryOutputStream out = temps.get(name);
        return out != null ? out.getTarget() : null;
    }

    @Override
    public CommonOutputSocket<AE> newOutputSocket(final AE entry)
    throws IOException {
        class OutputSocket extends FilterOutputSocket<AE> {
            OutputSocket() throws IOException {
                super(MultiplexedArchiveOutputShop.super.newOutputSocket(entry));
            }

            @Override
            public OutputStream newOutputStream()
            throws IOException {
                final CommonEntry peer = getPeerTarget();
                if (peer != null)
                    getTarget().setSize(peer.getSize()); // data may be compressed!
                return isTargetBusy()
                        ? new TempEntryOutputStream(
                            createTempFile(
                                TEMP_FILE_PREFIX),
                                output.chain(this),
                                peer)
                        : new EntryOutputStream(super.newOutputStream());
            }
        }
        return new OutputSocket();
    }

    /**
     * Returns whether the output output archive is busy writing an archive
     * entry or not.
     */
    public boolean isTargetBusy() {
        return targetBusy;
    }

    /**
     * This entry output stream writes directly to the output output archive.
     */
    private class EntryOutputStream extends FilterOutputStream {
        private boolean closed;

        EntryOutputStream(final OutputStream out)
        throws IOException {
            super(out);
            targetBusy = true;
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            if (closed)
                return;

            // Order is important here!
            closed = true;
            targetBusy = false;
            super.close();
            storeTemps();
        }
    } // class EntryOutputStream

    /**
     * This entry output stream writes the archive entry to a temporary file.
     * When the stream is closed, the temporary file is then copied to the
     * output output archive and finally deleted unless the output output
     * archive is still busy.
     */
    private class TempEntryOutputStream
    extends FileOutputStream
    implements IOReference<AE> {
        private final File temp;
        private final CommonOutputSocket<? extends AE> output;
        private final CommonInputSocket<CommonEntry> input;
        private boolean closed;

        @SuppressWarnings("LeakingThisInConstructor")
        TempEntryOutputStream(
                final File temp,
                final CommonOutputSocket<? extends AE> output,
                final CommonEntry peer)
        throws IOException {
            super(temp);
            class TempInputSocket extends CommonInputSocket<CommonEntry> {
                private final CommonEntry target
                        = null != peer ? peer : new FileEntry(temp);

                @Override
                public CommonEntry getTarget() {
                    return target;
                }

                @Override
                public InputStream newInputStream() throws IOException {
                    return new FileInputStream(temp);
                }

                @Override
                public ReadOnlyFile newReadOnlyFile() throws IOException {
                    return new SimpleReadOnlyFile(temp);
                }
            }
            this.temp = temp;
            this.output = output;
            this.input = new TempInputSocket();
            temps.put(output.getTarget().getName(), this);
        }

        @Override
        public AE getTarget() {
            return output.getTarget();
        }

        @Override
        public void close() throws IOException {
            if (closed)
                return;

            // Order is important here!
            // Note that this must be guarded by the closed flag:
            // close() gets called from the finalize() method in the
            // subclass, which may cause a ConcurrentModificationException
            // in this method.
            closed = true;
            try {
                super.close();
            } finally {
                final AE dstEntry = output.getTarget();
                final CommonEntry srcEntry = input.getTarget();
                if (dstEntry.getSize() == UNKNOWN)
                    dstEntry.setSize(srcEntry.getSize());
                for (Access access : BitField.allOf(Access.class))
                    if (UNKNOWN == dstEntry.getTime(access))
                        dstEntry.setTime(access, srcEntry.getTime(access));
                storeTemps();
            }
        }

        boolean store() throws IOException {
            if (!closed || isTargetBusy())
                return false;

            try {
                IOSocket.copy(input, output);
            } finally {
                if (!temp.delete()) // may fail on Windoze if in.close() failed!
                    temp.deleteOnExit(); // be bullish never to leavy any temps!
            }
            return true;
        }
    } // class TempEntryOutputStream

    @Override
    public void close() throws IOException {
        assert !isTargetBusy();
        try {
            storeTemps();
            assert temps.isEmpty();
        } finally {
            super.close();
        }
    }

    private void storeTemps() throws IOException {
        if (isTargetBusy())
            return;

        final ChainableIOExceptionBuilder<ChainableIOException> builder
                = new ChainableIOExceptionBuilder<ChainableIOException>();
        final Iterator<TempEntryOutputStream> i = temps.values().iterator();
        while (i.hasNext()) {
            final TempEntryOutputStream out = i.next();
            boolean remove = true;
            try {
                remove = out.store();
            } catch (InputException ex) {
                // Input exception - let's continue!
                builder.warn(new ChainableIOException(ex));
            } catch (IOException ex) {
                // Something's wrong writing this MultiplexedOutputStream!
                throw builder.fail(new ChainableIOException(ex));
            } finally {
                if (remove)
                    i.remove();
            }
        }
        builder.check();
    }
}

/*
 * Copyright (C) 2009-2010 Schlichtherle IT Services
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

package de.schlichtherle.truezip.io.archive.driver.zip;

import de.schlichtherle.truezip.io.socket.common.entry.CommonEntry;
import de.schlichtherle.truezip.io.socket.common.output.CommonOutputSocket;
import de.schlichtherle.truezip.io.archive.driver.MultiplexedArchiveOutput;
import de.schlichtherle.truezip.io.socket.common.output.CommonOutputSocketService;
import de.schlichtherle.truezip.io.socket.common.output.CommonOutputBusyException;
import de.schlichtherle.truezip.io.Streams;
import de.schlichtherle.truezip.io.zip.RawZipOutputStream;
import de.schlichtherle.truezip.util.JointIterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import static de.schlichtherle.truezip.io.archive.driver.zip.ZipDriver.TEMP_FILE_PREFIX;
import static de.schlichtherle.truezip.io.Files.createTempFile;
import static de.schlichtherle.truezip.io.zip.ZipEntry.DEFLATED;
import static de.schlichtherle.truezip.io.zip.ZipEntry.STORED;
import static de.schlichtherle.truezip.io.zip.ZipEntry.UNKNOWN;

/**
 * An implementation of {@link CommonOutputSocketService} to write ZIP archives.
 * <p>
 * This output archive can only write one entry at a time.
 * Archive drivers may wrap this class in a
 * {@link MultiplexedArchiveOutput}
 * to overcome this limitation.
 * 
 * @see ZipDriver
 * @author Christian Schlichtherle
 * @version $Id$
 */
public class ZipOutput
extends RawZipOutputStream<ZipEntry>
implements CommonOutputSocketService<ZipEntry> {

    private final ZipInput source;
    private ZipEntry tempEntry;

    /**
     * Creates a new instance which uses the output stream, character set and
     * compression level.
     *
     * @param level The compression level to use.
     * @throws IllegalArgumentException If {@code level} is not in the
     *         range [{@value java.util.zip.Deflater#BEST_SPEED}..{@value java.util.zip.Deflater#BEST_COMPRESSION}]
     *         and is not {@value java.util.zip.Deflater#DEFAULT_COMPRESSION}.
     */
    public ZipOutput(
            final OutputStream out,
            final String charset,
            final int level,
            final ZipInput source)
    throws  NullPointerException,
            UnsupportedEncodingException,
            IOException {
        super(out, charset);
        super.setLevel(level);

        this.source = source;
        if (source != null) {
            // Retain comment and preamble of input ZIP archive.
            super.setComment(source.getComment());
            if (source.getPreambleLength() > 0) {
                final InputStream in = source.getPreambleInputStream();
                try {
                    de.schlichtherle.truezip.io.file.File.cat(
                            in, source.offsetsConsiderPreamble() ? this : out);
                } finally {
                    in.close();
                }
            }
        }
    }

    @Override
    public int size() {
        return size() + (tempEntry != null ? 1 : 0);
    }

    @Override
    public Iterator<ZipEntry> iterator() {
        if (tempEntry == null)
            return super.iterator();
        return new JointIterator<ZipEntry>(
                super.iterator(),
                Collections.singletonList(tempEntry).iterator());
    }

    @Override
    public ZipEntry getEntry(final String name) {
        ZipEntry entry = super.getEntry(name);
        if (entry != null)
            return entry;
        entry = tempEntry;
        return entry != null && name.equals(entry.getName()) ? entry : null;
    }

    @Override
    public CommonOutputSocket<ZipEntry> getOutputSocket(final ZipEntry entry)
    throws FileNotFoundException {
        class OutputSocket extends CommonOutputSocket<ZipEntry> {
            @Override
            public ZipEntry getTarget() {
                return entry;
            }

            @Override
            public OutputStream newOutputStream()
            throws IOException {
                return ZipOutput.this.newOutputStream(entry, getPeerTarget());
            }
        }
        return new OutputSocket();
    }

    protected OutputStream newOutputStream(
            final ZipEntry target,
            final CommonEntry peer)
    throws IOException {
        if (isBusy())
            throw new CommonOutputBusyException(target);

        if (target.isDirectory()) {
            target.setMethod(STORED);
            target.setCrc(0);
            target.setCompressedSize(0);
            target.setSize(0);
            return new EntryOutputStream(target);
        }

        if (peer != null) {
            target.setSize(peer.getSize());
            if (peer instanceof ZipEntry) {
                // Set up entry attributes for Direct Data Copying (DDC).
                // A preset method in the entry takes priority.
                // The ZIP.RAES drivers use this feature to enforce deflation
                // for enhanced authentication security.
                final ZipEntry srcZipEntry = (ZipEntry) peer;
                if (target.getMethod() == UNKNOWN)
                    target.setMethod(srcZipEntry.getMethod());
                if (target.getMethod() == srcZipEntry.getMethod())
                    target.setCompressedSize(srcZipEntry.getCompressedSize());
                target.setCrc(srcZipEntry.getCrc());
                return new EntryOutputStream(
                        target, srcZipEntry.getMethod() != ZipEntry.DEFLATED);
            }
        }

        switch (target.getMethod()) {
            case UNKNOWN:
                target.setMethod(DEFLATED);
                break;

            case STORED:
                if (target.getCrc() == UNKNOWN
                        || target.getCompressedSize() == UNKNOWN
                        || target.getSize() == UNKNOWN)
                    return new TempEntryOutputStream(
                            createTempFile(TEMP_FILE_PREFIX), target);
                break;

            case DEFLATED:
                break;

            default:
                assert false : "unsupported method";
        }
        return new EntryOutputStream(target);
    }

    /**
     * Returns whether this output archive is busy writing an archive entry
     * or not.
     */
    @Override
    public final boolean isBusy() {
        return super.isBusy() || tempEntry != null;
    }

    /**
     * This entry output stream writes directly to our subclass.
     * It can only be used if this output stream is not currently busy
     * writing another entry and the entry holds enough information to
     * write the entry header.
     * These preconditions are checked by {@link #newOutputStream}.
     */
    private class EntryOutputStream extends FilterOutputStream {
        EntryOutputStream(ZipEntry entry) throws IOException {
            this(entry, true);
        }

        EntryOutputStream(ZipEntry entry, boolean deflate)
        throws IOException {
            super(ZipOutput.this);
            putNextEntry(entry, deflate);
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
            closeEntry();
        }
    } // class EntryOutputStream

    /**
     * This entry output stream writes the entry to a temporary file.
     * When the stream is closed, the temporary file is then copied to this
     * output stream and finally deleted.
     */
    private class TempEntryOutputStream extends CheckedOutputStream {
        private final File temp;
        private boolean closed;

        TempEntryOutputStream(final File temp, final ZipEntry entry)
        throws IOException {
            super(new java.io.FileOutputStream(temp), new CRC32());
            assert entry.getMethod() == STORED;
            this.temp = temp;
            tempEntry = entry;
        }

        @Override
        public void close() throws IOException {
            if (closed)
                return;

            // Order is important here!
            closed = true;
            try {
                try {
                    super.close();
                } finally {
                    final long length = temp.length();
                    if (length > Integer.MAX_VALUE)
                        throw new IOException("file too large");
                    tempEntry.setCrc(getChecksum().getValue());
                    tempEntry.setCompressedSize(length);
                    tempEntry.setSize(length);
                    store();
                }
            } finally {
                tempEntry = null;
            }
        }

        void store()
        throws IOException {
            assert tempEntry.getMethod() == STORED;
            assert tempEntry.getCrc() != UNKNOWN;
            assert tempEntry.getCompressedSize() != UNKNOWN;
            assert tempEntry.getSize() != UNKNOWN;

            try {
                final InputStream in = new FileInputStream(temp);
                try {
                    putNextEntry(tempEntry);
                    try {
                        Streams.cat(in, this);
                    } finally {
                        closeEntry();
                    }
                } finally {
                    in.close();
                }
            } finally {
                if (!temp.delete()) // may fail on Windoze if in.close() failed!
                    temp.deleteOnExit(); // we're bullish never to leavy any temps!
            }
        }
    } // class TempEntryOutputStream

    private static class Crc32OutputStream extends OutputStream {
        private final CRC32 crc = new CRC32();

        public void write(int b) {
            crc.update(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            crc.update(b, off, len);
        }
    } // class Crc32OutputStream

    /**
     * Retain the postamble of the source ZIP archive, if any.
     */
    @Override
    public void finish() throws IOException {
        super.finish();

        if (source == null)
            return;

        final long ipl = source.getPostambleLength();
        if (ipl <= 0)
            return;

        final long il = source.length();
        final long ol = length();

        final InputStream in = source.getPostambleInputStream();
        try {
            // Second, if the output ZIP compatible file differs in length from
            // the input ZIP compatible file pad the output to the next four byte
            // boundary before appending the postamble.
            // This might be required for self extracting files on some platforms
            // (e.g. Wintel).
            if (ol + ipl != il)
                write(new byte[(int) (ol % 4)]);

            // Finally, write the postamble.
            Streams.cat(in, this);
        } finally {
            in.close();
        }
    }
}

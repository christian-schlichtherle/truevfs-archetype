/*
 * Copyright (C) 2010 Schlichtherle IT Services
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
package de.schlichtherle.truezip.io.filesystem.file;

import de.schlichtherle.truezip.util.BitField;
import de.schlichtherle.truezip.io.filesystem.OutputOption;
import de.schlichtherle.truezip.io.socket.IOEntry;
import de.schlichtherle.truezip.io.socket.InputSocket;
import de.schlichtherle.truezip.io.socket.OutputSocket;
import java.io.IOException;
import java.io.OutputStream;
import de.schlichtherle.truezip.io.entry.Entry;
import de.schlichtherle.truezip.io.entry.EntryName;
import de.schlichtherle.truezip.io.filesystem.FileSystemEntry;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static de.schlichtherle.truezip.io.entry.Entry.Type.*;
import static de.schlichtherle.truezip.io.entry.Entry.Access.*;

/**
 * Adapts a {@link File} instance to a {@link FileSystemEntry}.
 *
 * @author Christian Schlichtherle
 * @version $Id$
 */
public class FileEntry extends FileSystemEntry implements IOEntry<FileEntry> {

    private static final BitField<OutputOption> NO_OUTPUT_OPTIONS
            = BitField.noneOf(OutputOption.class);

    private final File file;
    private final EntryName name;

    FileEntry(final File file) {
        this.file = file;
        this.name = EntryName.create(file.getName()); // Path.create(file.toURI()).getEntryName();
    }

    FileEntry(final File file, final EntryName name) {
        this.file = new File(file, name.getPath());
        this.name = name;
    }

    /** Returns the decorated file. */
    public final File getFile() {
        return file;
    }

    @Override
    public final String getName() {
        return name.toString();
    }

    /** Returns the type of this file entry. */
    @Override
    public final Entry.Type getType() {
        return file.isDirectory() ? DIRECTORY
                :   file.isFile() ? FILE
                :   file.exists() ? SPECIAL
                :                   Type.NULL;
    }

    @Override
    public final long getSize(final Size type) {
        switch (type) {
            case DATA:
            case STORAGE:
                return file.exists() ? file.length() : UNKNOWN;
            default:
                return UNKNOWN;
        }
    }

    /** Returns the file's last modification time. */
    @Override
    public final long getTime(Access type) {
        return WRITE == type && file.exists() ? file.lastModified() : UNKNOWN;
    }

    @Override
    @SuppressWarnings("ManualArrayToCollectionCopy")
    public final Set<String> getMembers() {
        final String[] list = file.list();
        if (null == list)
            return null;
        final Set<String> set
                = new HashSet<String>((int) (list.length / .75f) + 1);
        for (String member : list)
            set.add(member);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public final InputSocket<FileEntry> getInputSocket() {
        return new FileInputSocket(this);
    }

    @Override
    public final OutputSocket<FileEntry> getOutputSocket() {
        return new FileOutputSocket(this, NO_OUTPUT_OPTIONS, null);
    }

    public final OutputSocket<FileEntry> getOutputSocket(
            @NonNull BitField<OutputOption> options,
            @Nullable Entry template) {
        return new FileOutputSocket(this, options, template);
    }

    private class Output extends OutputSocket<FileEntry> {
        final BitField<OutputOption> options;
        final Entry template;

        Output( final BitField<OutputOption> options,
                final Entry template) {
            this.options = options;
            this.template = template;
        }

        @Override
        public FileEntry getLocalTarget() {
            return FileEntry.this;
        }

        @Override
        public OutputStream newOutputStream() throws IOException {
            return new FileOutputSocket(FileEntry.this, options, template)
                    .newOutputStream();
        }
    } // class Output
}

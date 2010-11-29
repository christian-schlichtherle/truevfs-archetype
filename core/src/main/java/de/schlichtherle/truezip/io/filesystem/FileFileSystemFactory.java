/*
 * Copyright 2010 Schlichtherle IT Services
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
package de.schlichtherle.truezip.io.filesystem;

import de.schlichtherle.truezip.io.entry.FileEntry;
import java.net.URI;

/**
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public final class FileFileSystemFactory
implements FileSystemFactory<FileSystemModel, FileEntry> {

    /** The default instance. */
    public static final FileFileSystemFactory INSTANCE
            = new FileFileSystemFactory();

    private FileFileSystemFactory() {
    }

    public FileSystemModel newModel(URI mountPoint) {
        return new FileSystemModel(mountPoint, null, this);
    }

    @Override
    public FileSystemModel newModel(URI mountPoint, FileSystemModel parent) {
        return new FileSystemModel(mountPoint, parent, this);
    }

    public ComponentFileSystemController<FileEntry> newController(
            FileSystemModel model) {
        return new FileFileSystemController(model);
    }

    @Override
    public FileSystemController<FileEntry> newController(
            FileSystemModel model,
            ComponentFileSystemController<?> parent) {
        return new FileFileSystemController(model);
    }
}

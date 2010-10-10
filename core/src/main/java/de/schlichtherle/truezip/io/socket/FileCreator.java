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
package de.schlichtherle.truezip.io.socket;

import java.io.File;
import java.io.IOException;

/**
 * @see     BufferingInputSocket
 * @see     BufferingOutputSocket
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public interface FileCreator {

    /**
     * Creates a file in the file system, not just an object.
     * The returned file must be reserved for exclusive access by the client
     * application.
     *
     * @return A new file object which refers to a file in the file system.
     */
    public File createFile() throws IOException;
}

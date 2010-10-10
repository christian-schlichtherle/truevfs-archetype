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
package de.schlichtherle.truezip.io.socket;

import de.schlichtherle.truezip.io.rof.ReadOnlyFile;
import java.io.IOException;
import java.io.InputStream;

/**
 * @param   <CE> The type of the {@link #getTarget() local target} common entry.
 * @author  Christian Schlichtherle
 * @version $Id$
 */
@Deprecated
public abstract class LazyInputSocket<CE extends CommonEntry>
extends InputSocket<CE> {

    private final InputSocketFactory<CE> factory;
    private InputSocket<CE> socket;

    protected LazyInputSocket(final InputSocketFactory<CE> factory) {
        if (null == factory)
            throw new NullPointerException();
        this.factory = factory;
    }

    protected final InputSocket<CE> getInputSocket() throws IOException {
        return (null == socket
                ? socket = factory.newInputSocket(getTarget())
                : socket).share(this);
    }

    @Override
    public final CommonEntry getPeerTarget() {
        try {
            return getInputSocket().getPeerTarget();
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public final InputStream newInputStream() throws IOException {
        return getInputSocket().newInputStream();
    }

    @Override
    public final ReadOnlyFile newReadOnlyFile() throws IOException {
        return getInputSocket().newReadOnlyFile();
    }
}

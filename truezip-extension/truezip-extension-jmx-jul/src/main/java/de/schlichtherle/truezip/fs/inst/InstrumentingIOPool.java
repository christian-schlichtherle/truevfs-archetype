/*
 * Copyright 2004-2012 Schlichtherle IT Services
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.schlichtherle.truezip.fs.inst;

import de.schlichtherle.truezip.entry.DecoratingEntry;
import de.schlichtherle.truezip.socket.IOPool;
import de.schlichtherle.truezip.socket.IOPool.Entry;
import de.schlichtherle.truezip.socket.InputSocket;
import de.schlichtherle.truezip.socket.OutputSocket;
import java.io.IOException;
import net.jcip.annotations.Immutable;

/**
 * @author Christian Schlichtherle
 * @version $Id$
 */
@Immutable
public class InstrumentingIOPool<
        E extends Entry<E>,
        D extends InstrumentingDirector<D>>
implements IOPool<E> {

    protected final D director;
    protected final IOPool<E> delegate;

    public InstrumentingIOPool(final IOPool<E> pool, final D director) {
        if (null == pool || null == director)
            throw new NullPointerException();
        this.director = director;
        this.delegate = pool;
    }

    @Override
    public Entry<E> allocate() throws IOException {
        return new IOBuffer(delegate.allocate());
    }

    @Override
    public void release(Entry<E> resource) throws IOException {
        resource.release();
    }

    public class IOBuffer
    extends DecoratingEntry<Entry<E>>
    implements Entry<E> {

        protected IOBuffer(Entry<E> delegate) {
            super(delegate);
        }

        @Override
        public InputSocket<E> getInputSocket() {
            return director.instrument(delegate.getInputSocket(), this);
        }

        @Override
        public OutputSocket<E> getOutputSocket() {
            return director.instrument(delegate.getOutputSocket(), this);
        }

        @Override
        public void release() throws IOException {
            delegate.release();
        }
    }
}

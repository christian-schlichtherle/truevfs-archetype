/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.kernel.cio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * An output socket which obtains its socket lazily and {@link #reset()}s it
 * upon any {@link Throwable}.
 *
 * @see    ClutchInputSocket
 * @param  <E> the type of the {@link #getLocalTarget() local target}.
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class ClutchOutputSocket<E extends Entry>
extends DecoratingOutputSocket<E> {

    public ClutchOutputSocket() {
        super(null);
    }

    @Override
    protected final OutputSocket<? extends E> getSocket() throws IOException {
        final OutputSocket<? extends E> socket = this.socket;
        return null != socket ? socket : (this.socket = getLazyDelegate());
    };

    /**
     * Returns the socket socket for lazy initialization.
     * 
     * @return the socket socket for lazy initialization.
     * @throws IOException on any I/O failure. 
     */
    protected abstract OutputSocket<? extends E> getLazyDelegate()
    throws IOException;

    @Override
    public E getLocalTarget() throws IOException {
        try {
            return getBoundSocket().getLocalTarget();
        } catch (Throwable ex) {
            throw reset(ex);
        }
    }

    @Override
    public SeekableByteChannel newSeekableByteChannel()
    throws IOException {
        try {
            return getBoundSocket().newSeekableByteChannel();
        } catch (Throwable ex) {
            throw reset(ex);
        }
    }

    @Override
    public OutputStream newOutputStream() throws IOException {
        try {
            return getBoundSocket().newOutputStream();
        } catch (Throwable ex) {
            throw reset(ex);
        }
    }

    private IOException reset(final Throwable ex) {
        reset();
        if (ex instanceof RuntimeException)
            throw (RuntimeException) ex;
        else if (ex instanceof Error)
            throw (Error) ex;
        return (IOException) ex;
    }

    protected final void reset() {
        this.socket = null;
    }
}
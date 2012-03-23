/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.schlichtherle.truezip.fs.inst.comp;

import de.schlichtherle.truezip.fs.inst.jmx.JmxDirector;
import de.schlichtherle.truezip.fs.inst.jul.JulDirector;
import de.schlichtherle.truezip.socket.IOPool;
import de.schlichtherle.truezip.socket.spi.IOPoolService;
import javax.annotation.concurrent.Immutable;

/**
 * @author Christian Schlichtherle
 */
@Immutable
public final class CompositeIOPoolService extends IOPoolService {

    private final IOPoolService service;
    {
        final IOPoolService
                oio = new de.schlichtherle.truezip.fs.file.TempFilePoolService();
        final IOPoolService
                nio = new de.schlichtherle.truezip.fs.nio.file.TempFilePoolService();
        service = oio.getPriority() > nio.getPriority() ? oio : nio;
    }

    @SuppressWarnings("unchecked")
    private final IOPool<?> pool =
            JmxDirector.SINGLETON.instrument(
                JulDirector.SINGLETON.instrument(
                    (IOPool) service.get()));

    @Override
    public IOPool<?> get() {
        return pool;
    }

    /**
     * Returns 1 iff the JVM is running JSE 6 or 151 iff the JVM is running
     * JSE 7.
     * 
     * @return 1 iff the JVM is running JSE 6 or 151 iff the JVM is running
     *         JSE 7.
     */
    @Override
    public int getPriority() {
        return service.getPriority() * 3 / 2 + 1;
    }
}

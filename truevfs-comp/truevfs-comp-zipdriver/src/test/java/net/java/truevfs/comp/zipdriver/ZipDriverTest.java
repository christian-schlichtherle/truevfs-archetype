/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.comp.zipdriver;

import net.java.truevfs.comp.zipdriver.ZipDriver;
import net.java.truevfs.comp.zipdriver.ZipDriverEntry;
import net.java.truevfs.kernel.spec.FsArchiveDriverTestSuite;
import net.java.truevfs.kernel.spec.TestConfig;
import net.java.truevfs.kernel.spec.cio.IoBufferPool;

/**
 * @author Christian Schlichtherle
 */
public final class ZipDriverTest
extends FsArchiveDriverTestSuite<ZipDriverEntry, ZipDriver> {

    @Override
    protected ZipDriver newArchiveDriver() {
        return new ZipDriver() {
            @Override
            public IoBufferPool getPool() {
                return TestConfig.get().getPool();
            }
        };
    }

    @Override
    protected String getUnencodableName() {
        return "\u2297";
    }
}

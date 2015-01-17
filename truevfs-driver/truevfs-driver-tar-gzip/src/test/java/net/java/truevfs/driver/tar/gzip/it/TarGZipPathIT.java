/*
 * Copyright (C) 2005-2013 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.driver.tar.gzip.it;

import net.java.truevfs.comp.tardriver.it.TarPathITSuite;
import net.java.truevfs.driver.tar.gzip.TarGZipDriver;
import net.java.truevfs.driver.tar.gzip.TestTarGZipDriver;

/**
 * @author Christian Schlichtherle
 */
public final class TarGZipPathIT extends TarPathITSuite<TarGZipDriver> {
    @Override
    protected String getExtensionList() {
        return "tar.gz";
    }

    @Override
    protected TarGZipDriver newArchiveDriver() {
        return new TestTarGZipDriver();
    }
}

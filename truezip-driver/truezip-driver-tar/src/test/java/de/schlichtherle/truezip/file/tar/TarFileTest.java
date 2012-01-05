/*
 * Copyright (C) 2005-2011 Schlichtherle IT Services
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.schlichtherle.truezip.file.tar;

import de.schlichtherle.truezip.fs.archive.tar.TarDriver;

/**
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public final class TarFileTest extends TarFileTestSuite<TarDriver> {

    @Override
    protected String getSuffixList() {
        return "tar";
    }

    @Override
    protected TarDriver newArchiveDriver() {
        return new TarDriver(IO_POOL_PROVIDER);
    }
}

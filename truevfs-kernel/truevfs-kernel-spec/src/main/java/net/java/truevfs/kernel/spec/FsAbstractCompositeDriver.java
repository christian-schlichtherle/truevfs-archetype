/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.kernel.spec;

import java.util.Map;
import java.util.ServiceConfigurationError;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import javax.inject.Provider;

/**
 * An abstract composite driver.
 * This class provides an implementation of {@link #newController} which uses
 * the file system driver map returned by {@link #drivers()} to lookup the
 * appropriate driver for the scheme of any given mount point.
 * 
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class FsAbstractCompositeDriver
implements FsCompositeDriver, Provider<Map<FsScheme, FsDriver>> {

    @Override
    public final FsController newController(
            final FsManager manager,
            final FsModel model,
            final @CheckForNull FsController parent)
    throws ServiceConfigurationError {
        assert null == parent
                    ? null == model.getParent()
                    : parent.getModel().equals(model.getParent());
        final FsScheme scheme = model.getMountPoint().getScheme();
        final FsDriver driver = get().get(scheme);
        if (null == driver)
            throw new ServiceConfigurationError(scheme
                    + " (Unknown file system scheme! May be the class path doesn't contain the respective driver module or it isn't set up correctly?)");
        return driver.newController(manager, model, parent);
    }
}

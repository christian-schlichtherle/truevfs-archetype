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
package de.schlichtherle.truezip.io.filesystem;

import de.schlichtherle.truezip.util.BitField;
import de.schlichtherle.truezip.util.ExceptionBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * Provides statistics for the federated file systems managed by the instances
 * of this class.
 * <p>
 * Note that this class is thread-safe.
 *
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public final class StatisticsFederatedFileSystemManager extends FederatedFileSystemManager {

    @Override
    public <M extends FileSystemModel>
    FederatedFileSystemController<?> getController(
            final FileSystemDriver<M> driver,
            final URI mountPoint,
            final FederatedFileSystemController<?> parent) {
        final FederatedFileSystemController<?> controller
                = super.getController(driver, mountPoint, parent);
        return null != controller.getParent()
                ? controller
                : new StatisticsFileSystemController(controller, this);
    }

    private FileSystemStatistics statistics = new FileSystemStatistics(this);

    /**
     * Returns a non-{@code null} object which provides asynchronously updated
     * statistics about the set of federated file systems managed by this
     * instance.
     * Any call to a method of the returned object returns up-to-date data,
     * so there is no need to repeatedly call this method in order to update
     * the statistics.
     * <p>
     * Note that there may be a slight delay until the values returned reflect
     * the actual state of this package.
     * This delay increases if the system is under heavy load.
     */
    public FileSystemStatistics getStatistics() {
        return statistics;
    }

    @Override
    public <E extends IOException>
    void sync(  URI prefix,
                ExceptionBuilder<? super IOException, E> builder,
                BitField<SyncOption> options)
    throws E {
        try {
            super.sync(prefix, builder, options);
        } finally {
            statistics = new FileSystemStatistics(this);
        }
    }

    Set<FederatedFileSystemController<?>> getControllers() {
        return getControllers(null, null);
    }
}

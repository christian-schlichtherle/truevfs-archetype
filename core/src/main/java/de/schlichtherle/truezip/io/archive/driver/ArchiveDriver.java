/*
 * Copyright (C) 2006-2010 Schlichtherle IT Services
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

package de.schlichtherle.truezip.io.archive.driver;

import de.schlichtherle.truezip.io.archive.entry.ArchiveEntry;
import de.schlichtherle.truezip.io.socket.OutputShop;
import de.schlichtherle.truezip.io.socket.InputShop;
import de.schlichtherle.truezip.io.socket.CommonEntryFactory;
import de.schlichtherle.truezip.io.archive.controller.FileSystemModel;
import de.schlichtherle.truezip.io.archive.controller.FileSystemController;
import de.schlichtherle.truezip.io.archive.driver.registry.ArchiveDriverRegistry;
import de.schlichtherle.truezip.io.socket.InputSocket;
import de.schlichtherle.truezip.io.socket.OutputSocket;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.Icon;

/**
 * This "driver" interface is used as an abstract factory which reads and
 * writes archives of a particular type, e.g. ZIP, TZP, JAR, TAR, TAR.GZ,
 * TAR.BZ2 or any other.
 * FileSystemModel drivers may be shared by their client applications.
 * <p>
 * The following requirements must be met by any implementation:
 * <ul>
 * <li>Implementations must be thread-safe.
 * <li>Implementations must be (at least virtually) immutable.
 * <li>Implementations must not assume that they are used as singletons:
 *     Multiple instances of an implementation may be used for the same
 *     archive type.
 * <li>If the driver shall be supported by the {@link ArchiveDriverRegistry},
 *     a no-arguments constructor must be provided.
 * <li>Although not required, it's recommended to implement the
 *     {@link Serializable} interface, so that objects which are referring to
 *     it can be serialized.
 * </ul>
 *
 * @param <AE> The type of the archive entries.
 * @author Christian Schlichtherle
 * @version $Id$
 */
public interface ArchiveDriver<AE extends ArchiveEntry>
extends CommonEntryFactory<AE> {

    /**
     * Creates a new common input shop for reading the archive entries of the
     * the described {@code archive} from the given {@code input} socket's
     * target.
     * 
     * @param  archive the abstract archive representation which TrueZIP's
     *         internal {@link FileSystemController} is processing
     *         - {@code null} is not permitted.
     * @param  input the non-{@code null} common input socket for reading
     *         the contents of the described archive from its target.
     * @throws TransientIOException If calling this method for the same
     *         archive file again could possibly succeed.
     *         This exception is associated with another {@link IOException}
     *         as its cause which is unwrapped and interpreted as below.
     * @throws FileNotFoundException If the input archive is inaccessible
     *         for any reason and the implementation would like the client
     *         application to recognize the archive file as a <i>special</i>
     *         file.
     * @throws IOException On any other I/O or data format related issue
     *         when reading the input archive and the implementation would like
     *         the client application to recognize the archive file as a
     *         <i>regular</i> file.
     * @return A non-{@code null} reference to a new common input shop.
     */
    InputShop<AE> newInputShop(FileSystemModel archive, InputSocket<?> input)
    throws IOException;

    /**
     * Creates a new common output shop for writing archive entries to the
     * the described {@code archive} to the given {@code output} socket's
     * target.
     * 
     * @param  archive the abstract archive representation which TrueZIP's
     *         internal {@link FileSystemController} is processing
     *         - {@code null} is not permitted.
     * @param  output the non-{@code null} common output socket for writing
     *         the contents of the described archive to its target.
     * @param  source the nullable {@link InputShop} if
     *         {@code archive} is going to get updated.
     *         If not {@code null}, this is guaranteed to be a product
     *         of this driver's {@link #newInputShop} factor method, which may
     *         be used to copy some meta data which is specific to the type of
     *         archive this driver supports.
     *         For example, this could be used to copy the comment of a ZIP
     *         file.
     * @return A non-{@code null} reference to a new output archive object.
     * @throws TransientIOException If calling this method for the same
     *         archive file again could possibly succeed.
     *         This exception is associated with another {@code IOException}
     *         as its cause which is unwrapped and interpreted as below.
     * @throws FileNotFoundException If the output archive is inaccessible
     *         for any reason.
     * @throws IOException On any other I/O or data format related issue
     *         when writing the output archive.
     */
    OutputShop<AE> newOutputShop(FileSystemModel archive, OutputSocket<?> output, InputShop<AE> source)
    throws IOException;

    /**
     * Returns the icon that
     * {@link de.schlichtherle.truezip.io.swing.tree.FileTreeCellRenderer}
     * should display for the given archive file.
     *
     * @param  archive the archive file to display - never {@code null}.
     * @return The icon that should be displayed for the given archive file
     *         if it's open/expanded in the view.
     *         If {@code null} is returned, a default icon should be used.
     */
    Icon getOpenIcon(FileSystemModel archive);

    /**
     * Returns the icon that
     * {@link de.schlichtherle.truezip.io.swing.FileSystemView}
     * and
     * {@link de.schlichtherle.truezip.io.swing.tree.FileTreeCellRenderer}
     * should display for the given archive file.
     *
     * @param  archive the archive file to display - never {@code null}.
     * @return The icon that should be displayed for the given archive file
     *         if it's closed/collapsed in the view.
     *         If {@code null} is returned, a default icon should be used.
     */
    Icon getClosedIcon(FileSystemModel archive);
}

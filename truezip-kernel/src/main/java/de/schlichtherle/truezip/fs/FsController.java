/*
 * Copyright 2004-2012 Schlichtherle IT Services
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.schlichtherle.truezip.fs;

import de.schlichtherle.truezip.entry.Entry;
import de.schlichtherle.truezip.entry.Entry.Access;
import de.schlichtherle.truezip.entry.Entry.Type;
import de.schlichtherle.truezip.socket.InputSocket;
import de.schlichtherle.truezip.socket.OutputSocket;
import de.schlichtherle.truezip.util.BitField;
import de.schlichtherle.truezip.util.ExceptionHandler;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.IOException;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JTree;

/**
 * An abstract class which provides read/write access to a file system.
 * Objects of this class are typically organized in a chain of responsibility
 * for file system federation and a decorator chain for implementing different
 * aspects of the management of the file system state, e.g. locking concurrent
 * access.
 * 
 * <h3>General Properties</h3>
 * <p>
 * The {@link FsModel#getMountPoint() mount point} of the
 * {@link #getModel() file system model} addresses the file system at the head
 * of this chain of federated file systems.
 * Where the methods of this abstract class accept a
 * {@link FsEntryName file system entry name} as a parameter, this MUST get
 * resolved against the {@link FsModel#getMountPoint() mount point} URI of this
 * controller's {@link #getModel() file system model}.
 * 
 * <h3>Transactional Support</h3>
 * <p>
 * Even on modern computers, I/O operations are inherently unreliable: They
 * can fail on hardware errors, network timeouts, third party interactions etc.
 * In an ideal world, we would like all file system operations to be truly
 * transactional like some relational database services.
 * However, file system have to cope with really big data, much more than most
 * relational databases will ever see.
 * Its not uncommon these days to store some gigabytes of data in a single
 * file, for example a video file.
 * However, buffering gigabytes of data just for an eventual rollback of a
 * transaction is still not a realistic option and considering the fact that
 * faster computers have always been used to store even bigger data then its
 * getting clear that it never will be.
 * Therefore, the contract of this abstract class strives for only limited
 * transactional support as follows.
 * <ol>
 * <li>
 * Generally all file system operations may fail with either a
 * {@link RuntimeException} or an {@link IOException} to respectively indicate
 * wrong input parameters or a file system operation failure.
 * Where the following terms consider a failure, the term equally applies to
 * BOTH exception types.
 * <li>
 * With the exception of {@link #sync}, all file system operations MUST be
 * <i>atomic</i>, that is they either completely succeed or fail as if they had
 * not been called.
 * <li>
 * All file system operations MUST be <i>consistent</i>, that is they MUST
 * leave their resources in a state so that they can get retried, even after a
 * failure.
 * <li>
 * All file system operations SHOULD be <i>isolated</i> with respect to any
 * threads which share the same definition of the implementing class, that is
 * two such threads SHOULD NOT interfere with each other's file system
 * operations in any other way than the operation's defined side effect on the
 * stored data.
 * In general, this simply means that file system operations SHOULD be
 * thread-safe.
 * Note that some methods make this a MUST requirement, for example
 * {@link FsDriver#newController} and {@link FsCompositeDriver#newController}.
 * <li>
 * All file system operations SHOULD be <i>durable</i>, that is their side
 * effect on the stored data SHOULD be permanent in the parent file system or
 * storage system.
 * <li>
 * Once a call to {@link #sync} has succeeded, all previous file system
 * operations MUST be durable.
 * Furthermore, any changes to the stored data in the parent file system or
 * storage system which have been made by third parties up to this point in
 * time MUST be visible to the users of this class.
 * This enables file system operations to use I/O buffers most of the time and
 * eventually synchronize their contents with the parent file system or storage
 * system upon a call to {@code sync}.
 * </ol>
 * 
 * @param   <M> The type of the file system model.
 * @see     FsManager
 * @see     <a href="http://www.ietf.org/rfc/rfc2119.txt">RFC 2119: Key words for use in RFCs to Indicate Requirement Levels</a>
 * @author  Christian Schlichtherle
 * @version $Id$
 */
@DefaultAnnotation(NonNull.class)
public abstract class FsController<M extends FsModel> {

    /**
     * Returns the file system model.
     * Multiple invocations must return the same object.
     * 
     * @return The file system model.
     */
    public abstract M getModel();

    /**
     * Returns the controller for the parent file system or {@code null} if
     * and only if this file system is not federated, i.e. not a member of
     * another file system.
     * Multiple invocations must return the same object.
     * 
     * @return The nullable controller for the parent file system.
     */
    public abstract @Nullable FsController<?> getParent();

    /**
     * Returns a nullable icon representing the "open" state when displaying
     * the (federated) file system represented by this controller in a GUI,
     * e.g. a {@link JTree}.
     * 
     * @return The nullable icon.
     * @deprecated GUI features will be removed from this class in TrueZIP 8.
     */
    @Deprecated
    public abstract @Nullable Icon getOpenIcon()
    throws IOException;

    /**
     * Returns a nullable icon representing the "closed" state when displaying
     * the (federated) file system represented by this controller in a GUI,
     * e.g. a {@link JTree}.
     * 
     * @return The nullable icon.
     * @deprecated GUI features will be removed from this class in TrueZIP 8.
     */
    @Deprecated
    public abstract @Nullable Icon getClosedIcon()
    throws IOException;

    /**
     * Returns {@code true} if and only if the file system is read-only.
     * 
     * @return {@code true} if and only if the file system is read-only.
     */
    public abstract boolean isReadOnly()
    throws IOException;

    /**
     * Returns a file system entry or {@code null} if no file system entry
     * exists for the given name.
     * Modifying the returned object is either not supported (i.e. throws an
     * {@link UnsupportedOperationException}) or does not show any effect on
     * the file system.
     * 
     * @param  name the name of the file system entry.
     * @return A file system entry or {@code null} if no file system entry
     *         exists for the given name.
     */
    public abstract @Nullable FsEntry getEntry(FsEntryName name)
    throws IOException;

    /**
     * Returns {@code false} if the named file system entry is not readable.
     * 
     * @param  name the name of the file system entry.
     * @return {@code false} if the named file system entry is not readable.
     */
    public abstract boolean isReadable(FsEntryName name) throws IOException;

    /**
     * Returns {@code false} if the named file system entry is not writable.
     * 
     * @param  name the name of the file system entry.
     * @return {@code false} if the named file system entry is not writable.
     */
    public abstract boolean isWritable(FsEntryName name) throws IOException;

    /**
     * Returns {@code false} if the named file system entry is not executable.
     * <p>
     * The implementation in the class {@link FsController} always returns
     * {@code false}.
     * 
     * @param  name the name of the file system entry.
     * @return {@code false} if the named file system entry is not executable.
     * @since  TrueZIP 7.2.
     */
    public boolean isExecutable(FsEntryName name) throws IOException {
        return false;
    }

    /**
     * Sets the named file system entry as read-only.
     * This method will fail for typical federated (archive) file system
     * controller implementations because they do not support it.
     * 
     * @param  name the name of the file system entry.
     * @throws IOException By typical federated (archive) file system
     *         controller implementations because they do not support it.
     */
    public abstract void setReadOnly(FsEntryName name)
    throws IOException;

    /**
     * Makes an attempt to set the last access time of all types in the given
     * map for the file system entry with the given name.
     * If {@code false} is returned or an {@link IOException} is thrown, then
     * still some of the last access times may have been set.
     * Whether or not this is an atomic operation is specific to the
     * implementation.
     * 
     * @param  name the file system entry name.
     * @param  times the access times.
     * @return {@code true} if and only if setting the access time for all
     *         types in {@code times} succeeded.
     * @throws NullPointerException if any key or value in the map is
     *         {@code null}.
     * @since  TrueZIP 7.2
     */
    public boolean setTime(
            final FsEntryName name,
            final Map<Access, Long> times,
            final BitField<FsOutputOption> options)
    throws IOException {
        boolean ok = true;
        for (Map.Entry<Access, Long> time : times.entrySet())
            ok &= setTime(
                    name,
                    BitField.of(time.getKey()),
                    time.getValue(),
                    options);
        return ok;
    }

    /**
     * Makes an attempt to set the last access time of all types in the given
     * bit field for the file system entry with the given name.
     * If {@code false} is returned or an {@link IOException} is thrown, then
     * still some of the last access times may have been set.
     * Whether or not this is an atomic operation is specific to the
     * implementation.
     * 
     * @param  name the file system entry name.
     * @param  types the access types.
     * @param  value the last access time.
     * @return {@code true} if and only if setting the access time for all
     *         types in {@code types} succeeded.
     */
    public abstract boolean setTime(
            FsEntryName name,
            BitField<Access> types,
            long value,
            BitField<FsOutputOption> options)
    throws IOException;

    /**
     * Returns an input socket for reading the contents of the file system
     * entry addressed by the given name from the file system.
     *
     * @param  name the file system entry name.
     * @param  options the input options.
     * @return An {@code InputSocket}.
     */
    public abstract InputSocket<?>
    getInputSocket( FsEntryName name,
                    BitField<FsInputOption> options);

    /**
     * Returns an output socket for writing the contents of the entry addressed
     * by the given name to the file system.
     * If {@code template} is not {@code null}, then the output entry shall
     * have as many of its properties copied as reasonable, e.g. the last
     * modification time.
     *
     * @param  name a file system entry name.
     * @param  options a bit field of output options.
     * @param  template a nullable template for the properties of the output
     *         entry.
     * @return An {@code OutputSocket}.
     */
    public abstract OutputSocket<?>
    getOutputSocket(FsEntryName name,
                    BitField<FsOutputOption> options,
                    @CheckForNull Entry template);

    /**
     * Creates or replaces and finally links a chain of one or more entries
     * for the given entry {@code name} into the file system.
     *
     * @param  name the file system entry name.
     * @param  type the file system entry type.
     * @param  options the file system output options.
     *         If {@link FsOutputOption#CREATE_PARENTS} is set, any missing
     *         parent directories will be created and linked into the file
     *         system with its last modification time set to the system's
     *         current time.
     * @param  template if not {@code null}, then the file system entry
     *         at the end of the chain shall inherit as much properties from
     *         this entry as possible - with the exception of its name and type.
     * @throws IOException on any I/O error, including but not limited to these
     *         reasons:
     *         <ul>
     *         <li>The file system is read only.
     *         <li>{@code name} contains characters which are not
     *             supported by the file system.
     *         <li>The entry already exists and either the option
     *             {@link FsOutputOption#EXCLUSIVE} is set or the entry is a
     *             directory.
     *         <li>The entry exists as a different type.
     *         <li>A parent entry exists but is not a directory.
     *         <li>A parent entry is missing and {@code createParents} is
     *             {@code false}.
     *         </ul>
     */
    public abstract void
    mknod(  FsEntryName name,
            Type type,
            BitField<FsOutputOption> options,
            @CheckForNull Entry template)
    throws IOException;

    /**
     * Removes the named file system entry from the file system.
     * If the named file system entry is a directory, it must be empty.
     * 
     * @param  name the file system entry name.
     * @param  options output options for this operation.
     */
    public abstract void
    unlink(FsEntryName name, BitField<FsOutputOption> options)
    throws IOException;

    /**
     * Commits all unsynchronized changes to the contents of this file system
     * to its parent file system,
     * releases the associated resources (e.g. target archive files) for
     * access by third parties (e.g. other processes), cleans up any temporary
     * allocated resources (e.g. temporary files) and purges any cached data.
     * Note that temporary resources may get allocated even if the federated
     * file systems were accessed read-only.
     * If this is not a federated file system, i.e. if its not a member of a
     * parent file system, then nothing happens.
     * Otherwise, the state of this file system controller is reset.
     * <p>
     * This method calls {@link #sync sync(options, builder)}, where builder is
     * an instance of {@link FsSyncExceptionBuilder}.
     * If the call succeeds, the builder's {@link FsSyncExceptionBuilder#check}
     * method is called to check out any {@link FsSyncWarningException}, too.
     *
     * @param  options the synchronization options.
     * @throws FsSyncException if committing the changes fails for any reason.
     * @throws IllegalArgumentException if the combination of synchronization
     *         options is illegal, e.g. if {@code FORCE_CLOSE_INPUT} is cleared
     *         and {@code FORCE_CLOSE_OUTPUT} is set.
     */
    public final void
    sync(final BitField<FsSyncOption> options)
    throws FsSyncException {
        final FsSyncExceptionBuilder builder = new FsSyncExceptionBuilder();
        sync(options, builder);
        builder.check();
    }

    /**
     * Commits all unsynchronized changes to the contents of this file system
     * to its parent file system,
     * releases the associated resources (e.g. target archive files) for
     * access by third parties (e.g. other processes), cleans up any temporary
     * allocated resources (e.g. temporary files) and purges any cached data.
     * Note that temporary resources may get allocated even if the federated
     * file systems were accessed read-only.
     * If this is not a federated file system, i.e. if its not a member of a
     * parent file system, then nothing happens.
     * Otherwise, the state of this file system controller is reset.
     *
     * @param  options a bit field of synchronization options.
     * @param  handler the exception handling strategy for consuming input
     *         {@code FsSyncException}s and/or assembling output
     *         {@code IOException}s.
     * @param  <X> The type of the {@code IOException} to throw at the
     *         discretion of the exception {@code handler}.
     * @throws IOException at the discretion of the exception {@code handler}
     *         upon the occurence of an {@link FsSyncException}.
     * @throws IllegalArgumentException if the combination of synchronization
     *         options is illegal, e.g. if
     *         {@code FsSyncOption.FORCE_CLOSE_INPUT} is cleared and
     *         {@code FsSyncOption.FORCE_CLOSE_OUTPUT} is set.
     */
    public abstract <X extends IOException> void
    sync(   BitField<FsSyncOption> options,
            ExceptionHandler<? super FsSyncException, X> handler)
    throws X;

    /**
     * Two file system controllers are considered equal if and only if they
     * are identical.
     * This can't get overriden.
     * 
     * @param that the object to compare.
     */
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public final boolean equals(@CheckForNull Object that) {
        return this == that;
    }

    /**
     * Returns a hash code which is consistent with {@link #equals}.
     * This can't get overriden.
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a string representation of this object for debugging and logging
     * purposes.
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("[model=")
                .append(getModel())
                .append(']')
                .toString();
    }
}

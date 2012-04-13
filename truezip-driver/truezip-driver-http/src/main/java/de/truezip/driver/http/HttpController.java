/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.driver.http;

import de.truezip.kernel.*;
import de.truezip.kernel.FsEntryName;
import de.truezip.kernel.FsPath;
import de.truezip.kernel.cio.Entry;
import de.truezip.kernel.cio.Entry.Access;
import de.truezip.kernel.cio.Entry.Type;
import static de.truezip.kernel.cio.Entry.Type.FILE;
import de.truezip.kernel.cio.IOPool;
import de.truezip.kernel.cio.InputSocket;
import de.truezip.kernel.cio.OutputSocket;
import de.truezip.kernel.FsAccessOption;
import de.truezip.kernel.FsSyncOption;
import de.truezip.kernel.util.BitField;
import de.truezip.kernel.util.ExceptionHandler;
import java.io.IOException;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import org.apache.http.HttpResponse;

/**
 * A file system controller for the HTTP(S) schemes.
 * 
 * @author  Christian Schlichtherle
 */
@Immutable
public class HttpController extends FsModelController<FsModel>  {

    private final HttpDriver driver;

    HttpController(final HttpDriver driver, final FsModel model) {
        super(model);
        if (null != model.getParent())
            throw new IllegalArgumentException();
        assert null != driver;
        this.driver = driver;
    }

    final IOPool<?> getPool() {
        return driver.getPool();
    }

    final HttpResponse executeHead(HttpEntry entry) throws IOException {
        return driver.executeHead(entry);
    }

    final HttpResponse executeGet(HttpEntry entry) throws IOException {
        return driver.executeGet(entry);
    }

    protected HttpEntry newEntry(FsEntryName name) {
        return new HttpEntry(this, name);
    }

    final FsPath resolve(FsEntryName name) {
        return getMountPoint().resolve(name);
    }

    @Override
    public FsController<?> getParent() {
        return null;
    }

    @Override
    public boolean isReadOnly() throws IOException {
        return false;
    }

    @Override
    public HttpEntry entry(FsEntryName name) throws IOException {
        HttpEntry entry = newEntry(name);
        return entry.isType(FILE) ? entry : null;
    }

    @Override
    public boolean isReadable(FsEntryName name) throws IOException {
        return null != entry(name);
    }

    @Override
    public boolean isWritable(FsEntryName name) throws IOException {
        return false;
    }

    @Override
    public void setReadOnly(FsEntryName name) throws IOException {
    }

    @Override
    public boolean setTime(
            FsEntryName name,
            BitField<Access> types,
            long value,
            BitField<FsAccessOption> options)
    throws IOException {
        throw new FsReadOnlyFileSystemException();
    }

    @Override
    public InputSocket<?> input(
            FsEntryName name,
            BitField<FsAccessOption> options) {
        return newEntry(name).newInputSocket(options);
    }

    @Override
    public OutputSocket<?> output(
            FsEntryName name,
            BitField<FsAccessOption> options,
            @CheckForNull Entry template) {
        return newEntry(name).newOutputSocket(options, template);
    }

    @Override
    public void mknod(  final FsEntryName name,
                        final Type type,
                        final BitField<FsAccessOption> options,
                        final @CheckForNull Entry template)
    throws IOException {
        throw new FsReadOnlyFileSystemException();
    }

    @Override
    public void unlink(FsEntryName name, BitField<FsAccessOption> options)
    throws IOException {
        throw new FsReadOnlyFileSystemException();
    }

    @Override
    public void
    sync(   BitField<FsSyncOption> options,
            ExceptionHandler<? super FsSyncException, ? extends FsSyncException> handler) {
    }
}

/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.extension.jmx;

import java.util.Date;
import java.util.Hashtable;
import javax.management.*;
import net.java.truecommons.shed.HashMaps;
import static net.java.truevfs.kernel.spec.cio.Entry.Access.*;
import static net.java.truevfs.kernel.spec.cio.Entry.Size.DATA;
import static net.java.truevfs.kernel.spec.cio.Entry.Size.STORAGE;
import static net.java.truevfs.kernel.spec.cio.Entry.UNKNOWN;
import net.java.truevfs.kernel.spec.cio.IoBuffer;

/**
 * The MXBean implementation for an {@link IoBuffer I/O pool entry}.
 *
 * @author Christian Schlichtherle
 */
final class JmxIoBufferView
extends StandardMBean implements JmxIoBufferMXBean {
    private final IoBuffer buffer;

    static void register(final IoBuffer model) {
        final JmxIoBufferMXBean mbean = new JmxIoBufferView(model);
        final ObjectName name = getObjectName(model);
        JmxUtils.registerMBean(mbean, name);
    }

    static void unregister(final IoBuffer model) {
        final ObjectName name = getObjectName(model);
        JmxUtils.unregisterMBean(name);
    }

    private static ObjectName getObjectName(final IoBuffer model) {
        final String path = model.getName();
        @SuppressWarnings("UseOfObsoleteCollectionType")
        final java.util.Hashtable<String, String>
                table = new Hashtable<>(HashMaps.initialCapacity(2));
        table.put("type", IoBuffer.class.getSimpleName());
        table.put("path", ObjectName.quote(path));
        try {
            return new ObjectName(
                    JmxIoBufferView.class.getPackage().getName(),
                    table);
        } catch (MalformedObjectNameException ex) {
            throw new AssertionError(ex);
        }
    }

    private JmxIoBufferView(IoBuffer buffer) {
        super(JmxIoBufferMXBean.class, true);
        this.buffer = buffer;
    }

    @Override
    protected String getDescription(MBeanInfo info) {
        return "An I/O pool entry.";
    }

    @Override
    protected String getDescription(MBeanAttributeInfo info) {
        String description = null;
        switch (info.getName()) {
        case "Name":
            description = "The name of this I/O pool entry.";
            break;
        case "SizeOfData":
            description = "The data size of this I/O pool entry.";
            break;
        case "SizeOfStorage":
            description = "The storage size of this I/O pool entry.";
            break;
        case "TimeWritten":
            description = "The last write time of this I/O pool entry.";
            break;
        case "TimeRead":
            description = "The last read or access time of this I/O pool entry.";
            break;
        case "TimeCreated":
            description = "The creation time of this I/O pool entry.";
            break;
        }
        return description;
    }

    @Override
    public String getName() {
        return buffer.getName();
    }

    @Override
    public long getSizeOfData() {
        return buffer.getSize(DATA);
    }

    @Override
    public long getSizeOfStorage() {
        return buffer.getSize(STORAGE);
    }

    @Override
    public String getTimeWritten() {
        final long time = buffer.getTime(WRITE);
        return UNKNOWN == time ? null : new Date(time).toString();
    }

    @Override
    public String getTimeRead() {
        final long time = buffer.getTime(READ);
        return UNKNOWN == time ? null : new Date(time).toString();
    }

    @Override
    public String getTimeCreated() {
        final long time = buffer.getTime(CREATE);
        return UNKNOWN == time ? null : new Date(time).toString();
    }
}
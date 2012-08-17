/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.ext.pacemanager

import net.java.truevfs.comp.jmx.JmxManagerMXBean

/**
  * An MXBean interface for a [[net.java.truevfs.ext.pacemanager.PaceManager]].
  *
  * @author Christian Schlichtherle
  */
trait PaceManagerMXBean extends JmxManagerMXBean {

  /**
    * Returns the maximum number of file systems which may have been mounted
    * at any time.
    * The mimimum value is `MAXIMUM_FILE_SYSTEMS_MOUNTED_MINIMUM_VALUE`.
    * The default value is `MAXIMUM_FILE_SYSTEMS_MOUNTED_DEFAULT_VALUE`.
    *
    * @return The maximum number of mounted file systems.
    */
  def getMaximumFileSystemsMounted: Int

  /**
    * Sets the maximum number of file systems which may have been mounted
    * at any time.
    * Changing this property will show effect upon the next access to any
    * file system.
    *
    * @param  maxMounted the maximum number of mounted file systems.
    * @throws IllegalArgumentException if `maxMounted` is less than
    *         `MAXIMUM_FILE_SYSTEMS_MOUNTED_MINIMUM_VALUE`.
    */
  def setMaximumFileSystemsMounted(maxMounted: Int)
}

/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.schlichtherle.truezip.kernel;

import java.io.IOException;

/**
 * A callable for I/O operations.
 * 
 * @author Christian Schlichtherle
 */
@SuppressWarnings("MarkerInterface")
interface IOOperation<V> extends Operation<V, IOException> {
}

/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.key.swing;

import net.java.truevfs.key.swing.SwingPromptingKeyManager;
import net.java.truevfs.key.swing.SwingAesPbeParametersView;
import javax.annotation.CheckForNull;
import net.java.truevfs.key.spec.PromptingKeyManager;
import net.java.truevfs.key.spec.PromptingKeyManagerTestSuite;

/**
 * @author Christian Schlichtherle
 */
public class SwingPromptingKeyManagerTest
extends PromptingKeyManagerTestSuite {
    @Override
    protected @CheckForNull PromptingKeyManager<?> newKeyManager() {
        return new SwingPromptingKeyManager<>(new SwingAesPbeParametersView());
    }
}

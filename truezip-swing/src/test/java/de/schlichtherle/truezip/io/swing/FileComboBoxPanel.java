/*
 * Copyright (C) 2006-2011 Schlichtherle IT Services
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.schlichtherle.truezip.io.swing;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.awt.EventQueue;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * @author Christian Schlichtherle
 * @version $Id$
 */
@DefaultAnnotation(NonNull.class)
public class FileComboBoxPanel extends JPanel implements Runnable {
    private static final long serialVersionUID = 1065812374938719922L;

    /** Creates new form FileComboBoxPanel */
    public FileComboBoxPanel() {
        initComponents();
    }

    /** Creates new form FileComboBoxPanel */
    public FileComboBoxPanel(@CheckForNull File directory) {
        initComponents();
        setDirectory0(directory);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        final javax.swing.JComboBox<String> box1 = new javax.swing.JComboBox<String>();
        final javax.swing.JComboBox<String> box2 = new javax.swing.JComboBox<String>();

        browser1.setComboBox(box1);
        browser2.setComboBox(box2);

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
        jLabel1.setText("Please start entering a file name anywhere...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(50, 0, 0, 0);
        add(jLabel1, gridBagConstraints);

        box1.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(box1, gridBagConstraints);

        box2.setEditable(true);
        box2.setModel(box1.getModel());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(50, 0, 0, 0);
        add(box2, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * Getter for property directory.
     * @return Value of property directory.
     */
    public File getDirectory() {
        return browser1.getDirectory();
    }

    /**
     * Setter for property directory.
     * @param directory New value of property directory.
     */
    public void setDirectory(@CheckForNull File directory) {
        setDirectory0(directory);
    }

    private void setDirectory0(@CheckForNull File directory) {
        browser1.setDirectory(directory);
        browser2.setDirectory(directory);
    }

    @Override
    public void run() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JFrame frame = new JFrame("File name auto completion fun");
                frame.add(FileComboBoxPanel.this);
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new FileComboBoxPanel(
                0 < args.length ? new File(args[0]) : null));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final de.schlichtherle.truezip.io.swing.FileComboBoxBrowser browser1 = new de.schlichtherle.truezip.io.swing.FileComboBoxBrowser();
    private final de.schlichtherle.truezip.io.swing.FileComboBoxBrowser browser2 = new de.schlichtherle.truezip.io.swing.FileComboBoxBrowser();
    // End of variables declaration//GEN-END:variables
}

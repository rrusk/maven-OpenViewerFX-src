/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2017 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * ErrorDialog.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.*;

import org.jpedal.utils.Messages;

/**
 * popup an error message to show user what message JPedal generated
 */
public class ErrorDialog {

    public static void showError(final Throwable th, final String message, final Component parent, final String fileName) {

        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel(message), c);

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);

        final String trace = sw.toString();

        final JTextArea ta = new JTextArea(trace);
        ta.setEditable(false);
        ta.setRows(10);
        ta.setCaretPosition(0);

        final JScrollPane scrollPane = new JScrollPane(ta,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVisible(false);


        c.gridy = 1;
        c.ipady = 10;
        final JLabel info = new JLabel(Messages.getMessage("PdfViewerError.CopyStacktrace"));
        info.setVisible(false);
        panel.add(info, c);

        c.ipady = 0;
        c.gridy = 2;
        panel.add(scrollPane, c);

        final JButton okButton = new JButton(Messages.getMessage("PdfViewerButton.ShowDetails"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                final JButton button = (JButton) arg0.getSource();
                if (scrollPane.isVisible()) {
                    scrollPane.setVisible(false);
                    info.setVisible(false);
                    button.setText(Messages.getMessage("PdfViewerButton.ShowDetails"));
                } else {
                    scrollPane.setVisible(true);
                    info.setVisible(true);
                    button.setText(Messages.getMessage("PdfViewerButton.HideDetails"));
                }

                final JDialog parentDialog = (JDialog) button.getTopLevelAncestor();
                parentDialog.pack();
                parentDialog.setLocationRelativeTo(parent);
            }
        });

        final Object[] buttonRowObjects = {okButton, Messages.getMessage("PdfViewerButton.Exit")};

        final JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(panel);
        optionPane.setMessageType(JOptionPane.ERROR_MESSAGE);
        optionPane.setOptionType(JOptionPane.DEFAULT_OPTION);
        optionPane.setOptions(buttonRowObjects);

        final JDialog dialog = optionPane.createDialog(parent, fileName);
        dialog.pack();
        dialog.setVisible(true);
    }
}

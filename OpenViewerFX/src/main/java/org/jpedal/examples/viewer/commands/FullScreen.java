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
 * FullScreen.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jpedal.PdfDecoderInt;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;

/**
 * Toggle fullscreen mode if supported
 */
public class FullScreen {

    /**
     * window for full screen mode
     */
    private static Window win;
    /**
     * location to restore after full screen
     */
    private static Point screenPosition;


    public static void execute(final Object[] args, final GUIFactory currentGUI, final GUIThumbnailPanel thumbnails, final Values commonValues, final PdfDecoderInt decode_pdf, final PropertiesFile properties) {
        if (args == null) {
            // Determine if full-screen mode is supported directly
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice gs = ge.getDefaultScreenDevice();
            if (gs.isFullScreenSupported()) {
                // Full-screen mode is supported
            } else {
                // Full-screen mode will be simulated
            }

            // Create a window for full-screen mode; add a button to leave full-screen mode
            if (win == null) {
                final Frame frame = new Frame(gs.getDefaultConfiguration());
                win = new Window(frame);
            } else {
                //added to allow actions from named actions to interact the full screen as it needs to.
                if (gs.getFullScreenWindow() != null
                        && gs.getFullScreenWindow().equals(win)) {
                    exitFullScreen(currentGUI);
                    return;
                }
            }

            if (currentGUI.getFrame() instanceof JFrame) {
                ((RootPaneContainer) currentGUI.getFrame()).getContentPane().remove((Component) currentGUI.getDisplayPane());
                // Java 1.6 has issues with original pane remaining visible so hide when fullscreen selected
                ((Component) currentGUI.getFrame()).setVisible(false);
            } else {
                ((Container) currentGUI.getFrame()).remove((Component) currentGUI.getDisplayPane());
            }

            win.add((Component) currentGUI.getDisplayPane(), BorderLayout.CENTER);

            // Create a button that leaves full-screen mode
            final JButton btn = new JButton("Return");
            win.add(btn, BorderLayout.NORTH);

            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent evt) {
                    exitFullScreen(currentGUI);
                }
            });

            try {
                screenPosition = ((Component) currentGUI.getFrame()).getLocation();
                // Enter full-screen mode
                gs.setFullScreenWindow(win);
                win.validate();
                currentGUI.scaleAndRotate();
            } catch (final Error e) {
                currentGUI.showMessageDialog("Full screen mode not supported on this machine.\n"
                        + "JPedal will now exit " + e);

                Exit.exit(thumbnails, currentGUI, commonValues, decode_pdf, properties);
                // ...
            } // finally {
            // Exit full-screen mode
            //	gs.setFullScreenWindow(null);
            //}
        }
    }

    private static void exitFullScreen(final GUIFactory currentGUI) {

        final Runnable doPaintComponent = new Runnable() {
            @Override
            public void run() {

                // Return to normal windowed mode
                final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                final GraphicsDevice gs = ge.getDefaultScreenDevice();
                gs.setFullScreenWindow(null);

                win.remove((Component) currentGUI.getDisplayPane());

                if (currentGUI.getFrame() instanceof JFrame) {
                    ((RootPaneContainer) currentGUI.getFrame()).getContentPane().add((Component) currentGUI.getDisplayPane(), BorderLayout.CENTER);
                    // Java 1.6 has issues with original pane remaining visible so show when fullscreen turned off
                    ((Component) currentGUI.getFrame()).setVisible(true);

                    //restore to last position which we saved on entering full screen
                    if (screenPosition != null) {
                        ((Component) currentGUI.getFrame()).setLocation(screenPosition);
                    }
                    screenPosition = null;
                } else {
                    ((Container) currentGUI.getFrame()).add((Component) currentGUI.getDisplayPane(), BorderLayout.CENTER);
                }

                ((Component) currentGUI.getDisplayPane()).invalidate();
                ((JComponent) currentGUI.getDisplayPane()).updateUI();

                if (currentGUI.getFrame() instanceof JFrame) {
                    ((RootPaneContainer) currentGUI.getFrame()).getContentPane().validate();
                } else {
                    ((Component) currentGUI.getFrame()).validate();
                }

                win.dispose();
                win = null;

                currentGUI.scaleAndRotate();
            }
        };

        SwingUtilities.invokeLater(doPaintComponent);

    }
}

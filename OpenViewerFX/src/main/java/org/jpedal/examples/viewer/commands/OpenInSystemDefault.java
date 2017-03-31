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
 * OpenInSystemDefault.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.jpedal.examples.viewer.Values;
import org.jpedal.gui.GUIFactory;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

public class OpenInSystemDefault {

    public static void execute(final GUIFactory currentGUI, final Values commonValues) {
        execute(null, currentGUI, commonValues);
    }

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues) {

        String file = commonValues.getSelectedFile();

        if (args != null && args.length > 0) {
            file = (String) args[0];
        }

        if (file != null) {
            try {

                if (DecoderOptions.isRunningOnWindows) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler",
                            file});
                } else {
                    if (DecoderOptions.isRunningOnMac || DecoderOptions.isRunningOnLinux) {
                        Runtime.getRuntime().exec(new String[]{"/usr/bin/open",
                                file});
                    } else {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(new File(file));
                            } catch (final IOException ex) {
                                currentGUI.showMessageDialog(Messages.getMessage("PdfSystemDefault.error"));
                                LogWriter.writeLog(Messages.getMessage("PdfSystemDefault.exception") + ex.getMessage());
                            }
                        } else {
                            currentGUI.showMessageDialog(Messages.getMessage("PdfSystemDefault.unsupported"));
                        }
                    }
                }
            } catch (final IOException ex) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfSystemDefault.error"));
                LogWriter.writeLog(Messages.getMessage("PdfSystemDefault.exception") + ex.getMessage());
            }
        }
    }
}

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
 * JavaFXExit.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands.javafx;

import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.SharedViewer;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * Clean up and exit program
 */
public class JavaFXExit {

    public static void execute(final Object[] args, final GUIThumbnailPanel thumbnails, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf, final PropertiesFile properties) {
        if (args == null) {
            if (org.jpedal.examples.viewer.utils.Printer.isPrinting()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerStillPrinting.text"));
            } else {
                exit(thumbnails, currentGUI, commonValues, decode_pdf, properties);
            }

        } else {

        }
    }

    public static void exit(final GUIThumbnailPanel thumbnails, final GUIFactory currentGUI, final Values commonValues, final PdfDecoderInt decode_pdf, final PropertiesFile properties) {

        thumbnails.terminateDrawing();

        /*
         * warn user on forms
         */
        //SaveFile.handleUnsaveForms(currentGUI, commonValues);

        if (decode_pdf.getDisplayView() == Display.PAGEFLOW) {
            decode_pdf.getPages().stopGeneratingPage();
        }

        decode_pdf.closePdfFile();

        //needed to save recent files
        try {
            properties.setValue("lastDocumentPage", String.valueOf(commonValues.getCurrentPage()));
            if (properties.getValue("trackViewerSize").equalsIgnoreCase("true")) {
                properties.setValue("startViewerWidth", String.valueOf(((Window) currentGUI.getFrame()).getWidth()));
                properties.setValue("startViewerHeight", String.valueOf(((Window) currentGUI.getFrame()).getHeight()));
            }

            if (properties.getValue("trackScaling").equalsIgnoreCase("true")) {
                properties.setValue("startScaling", String.valueOf(((GUI) currentGUI).getSelectedComboItem(Commands.SCALING)));
            }

            if (properties.getValue("trackView").equalsIgnoreCase("true")) {
                properties.setValue("startView", String.valueOf(decode_pdf.getDisplayView()));
            }

            if (properties.getValue("startSideTabOpen").equalsIgnoreCase("true")) {
                properties.setValue("startSideTabOpen", "true");
            }

            if (properties.getValue("trackSelectedSideTab").equalsIgnoreCase("true")) {
                final TabPane tabs = (TabPane) currentGUI.getSideTabBar();
                if (DecoderOptions.isRunningOnMac) {
                    properties.setValue("startSelectedSideTab", tabs.getTabs().get(tabs.getSelectionModel().getSelectedIndex()).getText());
                } else {
                    properties.setValue("startSelectedSideTab", tabs.getTabs().get(tabs.getSelectionModel().getSelectedIndex()).getText());
                }
            }

            if (properties.getValue("trackSideTabExpandedSize").equalsIgnoreCase("true")) {
                properties.setValue("sideTabBarExpandLength", String.valueOf(currentGUI.getSplitDividerLocation()));
            }

            properties.writeDoc();
        } catch (final Exception e1) {
            LogWriter.writeLog("Exception attempting to Write proterties: " + e1);
        }

        //formClickTest needs this so that it does not exit after first test.
        if (!SharedViewer.exitOnClose) {
            currentGUI.dispose();

        } else {

            //Added this one for now to remove a delay being experienced.
            if (SharedViewer.exitOnClose) {
                decode_pdf.dispose();
                currentGUI.dispose();

                System.exit(0);
            } else {

                final Stage stage = (Stage) currentGUI.getFrame();
                if (stage == null) {
                    decode_pdf.dispose();
                    //currentGUI.dispose();
                } else {

                    ((Stage) currentGUI.getFrame()).close();
                    decode_pdf.dispose();
                    currentGUI.dispose();
                }

            }
        }
    }
}

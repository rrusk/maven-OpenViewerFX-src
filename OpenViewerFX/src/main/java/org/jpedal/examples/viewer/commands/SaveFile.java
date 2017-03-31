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
 * SaveFile.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.utils.FileFilterer;
import org.jpedal.examples.viewer.utils.ItextFunctions;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.gui.GUIFactory;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * Saves the current document file
 */
public class SaveFile {

    public static void execute(final Object[] args, final GUIFactory currentGUI, final Values commonValues) {
        if (args == null) {
            saveFile(currentGUI, commonValues);
        }
    }

    public static void handleUnsaveForms(final GUIFactory currentGUI, final Values commonValues) {

        if (!org.jpedal.DevFlags.GUITESTINGINPROGRESS) {

            //OLD FORM CHANGE CODE
            if (commonValues.isFormsChanged()) {
                final int n = currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerFormsUnsavedOptions.message"), Messages.getMessage("PdfViewerFormsUnsavedWarning.message"), JOptionPane.YES_NO_OPTION);

                if (n == JOptionPane.YES_OPTION) {
                    SaveFile.saveFile(currentGUI, commonValues);
                }
            }
        }

        commonValues.setFormsChanged(false);
        currentGUI.setViewerTitle(null);
    }

    private static final FileFilterer pdf = new FileFilterer(new String[]{"pdf"}, "Pdf (*.pdf)");
    private static final FileFilterer fdf = new FileFilterer(new String[]{"fdf"}, "fdf (*.fdf)");
    private static final FileFilterer png = new FileFilterer(new String[]{"png"}, "Png (*.png)");
    private static final FileFilterer tif = new FileFilterer(new String[]{"tif"}, "Tiff (*.tif)");
    private static final FileFilterer jpg = new FileFilterer(new String[]{"jpg"}, "Jpg (*.jpg)");


    private static void saveFile(final GUIFactory currentGUI, final Values commonValues) {

        //Prevent save is linearised file is still loading
        if (currentGUI.getPdfDecoder().isLoadingLinearizedPDF()) {
            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.LineariseSaveWait"));
        } else {

            final JFileChooser chooser = new JFileChooser(commonValues.getInputDir());
            chooser.setSelectedFile(new File(commonValues.getInputDir() + '/' + commonValues.getSelectedFile()));
            chooser.addChoosableFileFilter(pdf);
            chooser.addChoosableFileFilter(fdf);
            chooser.addChoosableFileFilter(png);
            chooser.addChoosableFileFilter(tif);
            chooser.addChoosableFileFilter(jpg);
            chooser.setFileFilter(pdf);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            //set default name to current file name
            final int approved = chooser.showSaveDialog(null);
            if (approved == JFileChooser.APPROVE_OPTION) {

                final String filter = chooser.getFileFilter().getDescription().toLowerCase();

                if (filter.startsWith("all") || filter.startsWith("pdf") || filter.startsWith("fdf")) {
                    saveAsPdf(chooser.getSelectedFile(), currentGUI, commonValues);
                } else if (filter.startsWith("png") || filter.startsWith("tif") || filter.startsWith("jpg")) {
                    saveAsImage(chooser.getSelectedFile(), currentGUI, filter.substring(0, 3));
                }
            }
        }
    }

    private static void saveAsImage(final File file, final GUIFactory currentGUI, final String ext) {

        if (ext.equals("png") || ext.equals("jpg") || ext.equals("tif")) {

            final PdfDecoderInt decoder = currentGUI.getPdfDecoder();

            final File f = new File(file.getPath().replace("." + ext, "").replace(".pdf", ""));
            f.mkdir();

            for (int i = 1; i <= decoder.getPageCount(); i++) {
                try {

                    final BufferedImage imageToSave = decoder.getPageAsImage(i, 1);

                    final String filename = f.getAbsolutePath() + System.getProperty("file.separator") + file.getName().replace(".pdf", "") + "_" + i + '.' + ext;

                    decoder.getObjectStore().saveStoredImage(filename, imageToSave, true, ext);

                } catch (final Exception ex) {
                    LogWriter.writeLog("Exception attempting to Save as image: " + ex);
                }
            }
        } else {
            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.ImagesSaveUnsupported") + " " + ext);
            LogWriter.writeLog("Saving as " + ext + " is currently unsupported.");
        }
    }

    private static void saveAsPdf(File file, final GUIFactory currentGUI, final Values commonValues) {
        String fileToSave = file.getAbsolutePath();
        File tempFile = null;

        //check if pdf is encrypted/password protected
        //if new annots have been added we prevent saving
        if (currentGUI.getPdfDecoder().isEncrypted()) { // && !decode_pdf.isPasswordSupplied()){
            if (currentGUI.getAnnotationPanel().annotationAdded()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NewAnnotInEncryptedFile"));
                return;
            }
        }

        if (!fileToSave.endsWith(".pdf")) {
            fileToSave += ".pdf";
            file = new File(fileToSave);
        }

        if (file.exists()) {
            final int n = currentGUI.showConfirmDialog(fileToSave + '\n'
                            + Messages.getMessage("PdfViewerMessage.FileAlreadyExists") + '\n'
                            + Messages.getMessage("PdfViewerMessage.ConfirmResave"),
                    Messages.getMessage("PdfViewerMessage.Resave"), JOptionPane.YES_NO_OPTION);
            if (n == 1) {
                return;
            }
        }

        try {
            tempFile = File.createTempFile(file.getName().substring(0, file.getName().lastIndexOf('.')) + "SaveTemp", file.getName().substring(file.getName().lastIndexOf('.')));
            copyFile(commonValues.getSelectedFile(), tempFile.getAbsolutePath(), currentGUI);
        } catch (final IOException ex) {
            LogWriter.writeLog("Exception attempting to create temp file: " + ex);
        }

        if (tempFile != null) {

            if (currentGUI.getValues().isFormsChanged()) {
                final Object[] objArr = currentGUI.getPdfDecoder().getFormRenderer().getFormComponents(null, ReturnValues.FORMOBJECTS_FROM_REF, -1);

                if (objArr != null) {
                    currentGUI.getAnnotationPanel().saveForms(commonValues.getSelectedFile(), tempFile.getAbsolutePath(), objArr);
                }
            }

            if (currentGUI.getAnnotationPanel().annotationAdded()) {
                currentGUI.getAnnotationPanel().saveAnnotations(tempFile.getAbsolutePath(), fileToSave);
            } else {
                copyFile(tempFile.getAbsolutePath(), fileToSave, currentGUI);
            }

            if (ExternalHandlers.isITextPresent()) {
                //final ItextFunctions itextFunctions = new ItextFunctions(currentGUI, commonValues.getSelectedFile(), currentGUI.getPdfDecoder());
                ItextFunctions.saveFormsData(fileToSave);
            }

            //Remove temp File
            tempFile.delete();

            commonValues.setFormsChanged(false);
            currentGUI.setViewerTitle(null);
        }
    }


    private static void copyFile(final String input, final String output, final GUIFactory currentGUI) {
        
        /*
         * reset flag and graphical clue
         */
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(input);
            fos = new FileOutputStream(output);

            final byte[] buffer = new byte[4096];
            int bytes_read;

            while ((bytes_read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytes_read);
            }
        } catch (final Exception e1) {

            //e1.printStackTrace();
            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerException.NotSaveInternetFile") + ' ' + e1);
        }

        try {
            if (fis != null) {
                fis.close();
            }

            if (fos != null) {
                fos.close();
            }
        } catch (final IOException e2) {
            LogWriter.writeLog("Exception attempting close IOStream: " + e2);
        }
    }
}

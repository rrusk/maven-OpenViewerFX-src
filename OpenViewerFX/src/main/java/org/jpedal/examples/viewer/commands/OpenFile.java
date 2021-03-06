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
 * OpenFile.java
 * ---------------
 */
package org.jpedal.examples.viewer.commands;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.jpedal.JDeliHelper;
import org.jpedal.PdfDecoderInt;
import org.jpedal.display.Display;
import org.jpedal.display.GUIThumbnailPanel;
import org.jpedal.examples.viewer.Commands;

import static org.jpedal.examples.viewer.Commands.isPDFLinearized;

import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.generic.GUISearchWindow;
import org.jpedal.examples.viewer.gui.popups.DownloadProgress;
import org.jpedal.examples.viewer.gui.popups.ErrorDialog;
import org.jpedal.examples.viewer.utils.FileFilterer;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.exception.PdfException;
import org.jpedal.gui.GUIFactory;
import org.jpedal.io.TiffHelper;
import org.jpedal.linear.LinearThread;
import org.jpedal.objects.PdfFileInformation;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * This class Opens up a new file on page one whilst still using the users
 * chosen settings, it first handles unsaved forms and resets the viewable area,
 * it decodes the PDF and finally opens the new document.
 */
public class OpenFile {

    public static boolean isPDf;

    private static InputStream inputStream;

    public static void executeOpenURL(final Object[] args, final Values commonValues, final GUISearchWindow searchFrame,
                                      final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final PropertiesFile properties,
                                      final GUIThumbnailPanel thumbnails) {

        //reset value
        inputStream = null;

        if (args == null) {

            //warn user on forms
            SaveFile.handleUnsaveForms(currentGUI, commonValues);

            final String newFile = selectURL(commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);
            if (newFile != null) {
                commonValues.setSelectedFile(newFile);
                commonValues.setFileIsURL(true);
            }
        } else {

            currentGUI.resetNavBar();
            String newFile = (String) args[0];
            if (newFile != null) {
                commonValues.setSelectedFile(newFile);
                commonValues.setFileIsURL(true);

                boolean failed = false;
                try {
                    final URL testExists = new URL(newFile);
                    final URLConnection conn = testExists.openConnection();

                    if (conn.getContent() == null) {
                        failed = true;
                    }
                } catch (final Exception e) {
                    failed = true;
                    LogWriter.writeLog("Unable to open as URL " + newFile + ' ' + e);
                }

                if (failed) {
                    newFile = null;
                }

                //decode pdf
                if (newFile != null) {

                    commonValues.setFileSize(0);
                    currentGUI.setViewerTitle(null);

                    //open the file
                    if (!Values.isProcessing()) {

                        //if running terminate first
                        thumbnails.terminateDrawing();

                        decode_pdf.flushObjectValues(true);

                        // reset the viewableArea before opening a new file
                        decode_pdf.resetViewableArea();

                        currentGUI.stopThumbnails();

                        if (!currentGUI.isSingle()) {
                            currentGUI.openNewMultiplePage(commonValues.getSelectedFile(), commonValues);
                        }

                        try {
                            //Set to true to show our default download window
                            OpenFile.openFile(commonValues.getSelectedFile(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

                            while (Values.isProcessing()) {
                                Thread.sleep(1000);

                            }
                        } catch (final InterruptedException e) {
                            LogWriter.writeLog("Exception attempting to open file: " + e);
                        }
                    }

                } else { // no file selected so redisplay old
                    decode_pdf.repaint();
                }
            }
        }
    }

    public static void executeOpenFile(final Object[] args, final Values commonValues, final GUISearchWindow searchFrame,
                                       final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final PropertiesFile properties,
                                       final GUIThumbnailPanel thumbnails) {

        //reset value to null
        inputStream = null;

        //warn user on forms
        SaveFile.handleUnsaveForms(currentGUI, commonValues);

        if (args == null) {

            if (org.jpedal.examples.viewer.utils.Printer.isPrinting()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPrintWait.message"));
            } else if (Values.isProcessing()) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
            } else {

                selectFile(commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

                commonValues.setFileIsURL(false);
            }
        } else {
            if (args.length == 2 && args[0] instanceof byte[] && args[1] instanceof String) {

                final byte[] data = (byte[]) args[0];
                final String filename = (String) args[1];

                commonValues.setFileSize(data.length);

                commonValues.setSelectedFile(filename);
                currentGUI.setViewerTitle(null);

                if ((commonValues.getSelectedFile() != null) && !Values.isProcessing()) {
                    // reset the viewableArea before opening a new file
                    decode_pdf.resetViewableArea();

                    try {
                        isPDf = true;
                        commonValues.setMultiTiff(false);

                        // reset flag
                        thumbnails.resetToDefault();

                        // [AWI]: Re-set the thumbnail panel so it will recalculate thumbnails for the next document
                        currentGUI.reinitThumbnails();

                        // flush forms list
                        currentGUI.setNoPagesDecoded();

                        // remove search frame if visible
                        if (searchFrame != null) {
                            searchFrame.removeSearchWindow(false);
                            // [AWI]: Reset the search window to clear past result statuses
                            searchFrame.resetSearchWindow();
                        }

                        commonValues.maxViewY = 0; // Ensure reset for any  viewport

                        commonValues.setCurrentPage(1);

                        if (currentGUI.isSingle()) {
                            decode_pdf.closePdfFile();
                        }

                        decode_pdf.openPdfArray(data);

                        currentGUI.updateStatusMessage("opening file");

                        boolean fileCanBeOpened = true;

                        if ((decode_pdf.isEncrypted()) && (!decode_pdf.isFileViewable())) {
                            fileCanBeOpened = false;

                            String password = System.getProperty("org.jpedal.password");

                            if (password == null) {
                                password = currentGUI.showInputDialog(Messages.getMessage("PdfViewerPassword.message")); //$NON-NLS-1$
                            }

                            //try and reopen with new password
                            if (password != null) {
                                decode_pdf.setEncryptionPassword(password);
                                // decode_pdf.verifyAccess();

                                if (decode_pdf.isFileViewable()) {
                                    fileCanBeOpened = true;
                                }
                            }

                            if (!fileCanBeOpened) {
                                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPasswordRequired.message"));
                            }
                        }
                        if (fileCanBeOpened) {

                            if (properties.getValue("Recentdocuments").equals("true")) {
                                properties.addRecentDocument(commonValues.getSelectedFile());
                                currentGUI.getRecentDocument().updateRecentDocuments(properties.getRecentDocuments());
                            }

                            currentGUI.getRecentDocument().addToFileList(commonValues.getSelectedFile());

                            //reset values
                            commonValues.setCurrentPage(1);
                        }

                        processPage(commonValues, decode_pdf, currentGUI, thumbnails);
                    } catch (final PdfException e) {
                        LogWriter.writeLog("Exception attempting to open file: " + e);
                    }
                }

            } else if (args.length >= 1) {

                if (args[0] instanceof InputStream) {

                    inputStream = (InputStream) args[0];

                    final String newFile = "InputStream-" + System.currentTimeMillis() + ".pdf";

                    commonValues.setSelectedFile(newFile);
                    commonValues.setFileIsURL(true);

                    //decode pdf
                    if (inputStream != null) {
                        try {
                            commonValues.setFileSize(0);
                            currentGUI.setViewerTitle(null);
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }

                        //open the file
                        if (!Values.isProcessing()) {

                            //if running terminate first
                            thumbnails.terminateDrawing();

                            decode_pdf.flushObjectValues(true);

                            //reset the viewableArea before opening a new file
                            decode_pdf.resetViewableArea();

                            currentGUI.stopThumbnails();

                            try {
                                //Set to true to show our default download window
                                openFile(commonValues.getSelectedFile(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);
                                while (Values.isProcessing()) {
                                    Thread.sleep(1000);

                                }

                                commonValues.setSelectedFile(decode_pdf.getFileName());
                            } catch (final InterruptedException e) {
                                LogWriter.writeLog("Exception attempting to open file: " + e);
                            }
                        }

                    } else { // no file selected so redisplay old
                        decode_pdf.repaint();
                    }

                } else {
                    File file;
                    if (args[0] instanceof File) {
                        file = (File) args[0];
                    } else if (args[0] instanceof String) {
                        final String filename = (String) args[0];
                        final char[] str = filename.toCharArray();
                        if (str[1] == ':' || str[0] == '\\' || str[0] == '/')//root
                        {
                            file = new File(filename);
                        } else {
                            final String parent = new File(commonValues.getSelectedFile()).getParent();
                            file = new File(parent, filename);
                            try {
                                file = file.getCanonicalFile();
                            } catch (final IOException e) {
                                file = new File(parent, filename);

                                LogWriter.writeLog("Exception in IO " + e);
                            }
                        }
                    } else {
                        file = null;
                    }

                    //decode
                    if (file != null) {
                        //save path so we reopen her for later selections
                        try {
                            commonValues.setInputDir(file.getParentFile().getCanonicalPath());
                            open(file.getAbsolutePath(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

                            //see if second value as Named Dest and store object ref if set
                            String bookmarkPage = null;
                            if (args.length > 1 && args[1] instanceof String) { // it may be a named destination ( ie bookmark=Test1)

                                final String bookmark = (String) args[1];
                                bookmarkPage = decode_pdf.getIO().convertNameToRef(bookmark);

                            }

                            if (bookmarkPage != null) { // and goto named Dest if present

                                // read the object
                                final PdfObject namedDest = new OutlineObject(bookmarkPage);
                                decode_pdf.getIO().readObject(namedDest);

                                // and generic open Dest code
                                decode_pdf.getFormRenderer().getActionHandler().gotoDest(namedDest, ActionHandler.MOUSECLICKED, PdfDictionary.Dest);
                            }

                            while (Values.isProcessing()) {
                                // Do nothing until pdf loaded
                                try {
                                    Thread.sleep(100);
                                } catch (final InterruptedException e) {
                                    LogWriter.writeLog("Exception attempting to open file: " + e);
                                }
                            }
                        } catch (final IOException e1) {
                            LogWriter.writeLog("Exception attempting to open file: " + e1);
                        }
                    } else { // no file selected so redisplay old
                        decode_pdf.repaint();

                        currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoSelection"));
                    }
                }
            }
        }

    }

    public static void open(final String file, final Values commonValues, final GUISearchWindow searchFrame,
                            final GUIFactory currentGUI, PdfDecoderInt decode_pdf, final PropertiesFile properties,
                            final GUIThumbnailPanel thumbnails) {

        currentGUI.removePageListener();

        //Only reset display mode if pdf / fdf
        final String ending = file.toLowerCase().trim();
        if (ending.endsWith(".pdf") || ending.endsWith(".fdf")) {
            currentGUI.setDisplayView(Display.SINGLE_PAGE, decode_pdf.getPageAlignment());
        }

        //Clear previous annotations
        currentGUI.getAnnotationPanel().clearAnnotations();

        final boolean isURL = file.startsWith("http:") || file.startsWith("file:");
        try {

            if (!isURL) {
                commonValues.setFileIsURL(false);
                commonValues.setFileSize(new File(file).length() >> 10);
            } else {
                commonValues.setFileIsURL(true);
            }

            commonValues.setSelectedFile(file);
            currentGUI.setViewerTitle(null);

        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " getting paths");
        }

        //check file exists
        final File testFile = new File(commonValues.getSelectedFile());
        if (!isURL && !testFile.exists()) {
            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerFile.text") + commonValues.getSelectedFile() + Messages.getMessage("PdfViewerNotExist"));

            //open the file
        } else if (commonValues.getSelectedFile() != null && !Values.isProcessing()) {

            if (currentGUI.isSingle()) {
                decode_pdf.flushObjectValues(true);
            } else {
                decode_pdf = currentGUI.openNewMultiplePage(commonValues.getSelectedFile(), commonValues);
            }

            //reset the viewableArea before opening a new file
            decode_pdf.resetViewableArea();

            openFile(commonValues.getSelectedFile(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

        }


        if (isPDf) {
            currentGUI.setDisplayView(decode_pdf.getDecoderOptions().getPageMode(), decode_pdf.getPageAlignment());
        }
    }

    /**
     * checks file can be opened (permission)
     *
     * @throws PdfException
     */
    public static void openFile(final String selectedFile, final Values commonValues, final GUISearchWindow searchFrame,
                                final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final PropertiesFile properties,
                                final GUIThumbnailPanel thumbnails) {

        isPDf = false;
        commonValues.setMultiTiff(false);

        //reset flag
        thumbnails.resetToDefault();

        //flush forms list
        currentGUI.setNoPagesDecoded();

        //remove search frame if visible
        if (searchFrame != null) {
            searchFrame.removeSearchWindow(false);
        }

        commonValues.maxViewY = 0; // Ensure reset for any viewport
        final String ending = selectedFile.toLowerCase().trim();
        commonValues.setPDF(ending.endsWith(".pdf") || ending.endsWith(".fdf"));
        if (ending.endsWith(".pdf") || ending.endsWith(".fdf")) {
            isPDf = true;
        }

        //switch off continous mode for images
        if (!commonValues.isPDF()) {

            if (SwingUtilities.isEventDispatchThread()) {

                currentGUI.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);

            } else {
                final Runnable doPaintComponent = new Runnable() {

                    @Override
                    public void run() {
                        currentGUI.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
                    }
                };
                SwingUtilities.invokeLater(doPaintComponent);
            }

        }

        commonValues.setCurrentPage(1);

        try {
            final boolean fileCanBeOpened = openUpFile(commonValues.getSelectedFile(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

            if (fileCanBeOpened) {
                processPage(commonValues, decode_pdf, currentGUI, thumbnails);
            } else {
                currentGUI.setViewerTitle(Messages.getMessage("PdfViewer.NoFile"));
                decode_pdf.getDynamicRenderer().writeCustom(DynamicVectorRenderer.FLUSH, null);
                decode_pdf.getPages().refreshDisplay();
                currentGUI.scaleAndRotate();
                commonValues.setPageCount(1);
                commonValues.setCurrentPage(1);
            }
        } catch (final Exception e) {

            LogWriter.writeLog(Messages.getMessage("PdfViewerError.Exception") + ' ' + e + ' ' + Messages.getMessage("PdfViewerError.DecodeFile"));
        }

        if (isPDf) {
            final PdfObject collectionObj = decode_pdf
                    .getIO()
                    .getPDFObject(PdfDictionary.Collection);
            if (collectionObj != null) {
                isPDf = false;
                commonValues.setPDF(false);
                //load collection object
                decode_pdf.getIO().checkResolved(collectionObj);
                final String view = collectionObj.getName(PdfDictionary.View);

                if (view.equals("C") || view.equals("D")) {
                    //Create Detail view
                    currentGUI.setDisplayView(Display.PORTFOLIO_DETAIL, Display.DISPLAY_CENTERED);

                } else if (view.equals("T")) {
                    //Create Tile view
                    currentGUI.setDisplayView(Display.PORTFOLIO_TILE, Display.DISPLAY_CENTERED);

                }
            }
        }
    }

    /**
     * initial method called to open a new PDF
     *
     * @throws PdfException
     */
    public static boolean openUpFile(final String selectedFile, final Values commonValues, final GUISearchWindow searchFrame,
                                     final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final PropertiesFile properties,
                                     final GUIThumbnailPanel thumbnails) throws PdfException {

        commonValues.maxViewY = 0; // Ensure reset for any viewport

        searchFrame.resetSearchWindow();

        // Turn MultiPageTiff flag off to ensure no mistakes
        commonValues.setMultiTiff(false);

        boolean fileCanBeOpened = true;

        if (currentGUI.isSingle()) {
            decode_pdf.closePdfFile();
        }

        // ensure all data flushed from PdfDecoder before we decode the file
        try {
            // opens the pdf and reads metadata
            if (commonValues.isPDF()) {
                if (inputStream != null || selectedFile.startsWith("http") || selectedFile.startsWith("file:") || selectedFile.startsWith("jar:")) {
                    try {

                        // <link><a name="linearized" />
                        // code below checks if file linearized and loads rest in background if it is
                        boolean isLinearized = false;

                        // use for all inputStream as we can't easily test
                        if (inputStream != null) {
                            isLinearized = true;
                        } else if (commonValues.getModeOfOperation() != Values.RUNNING_APPLET) {
                            isLinearized = isPDFLinearized(commonValues.getSelectedFile());
                        }

                        if (!isLinearized) {

                            if (commonValues.getSelectedFile().startsWith("jar:")) {
                                final InputStream is = Commands.class.getClass().getResourceAsStream(commonValues.getSelectedFile().substring(4));

                                decode_pdf.openPdfFileFromInputStream(is, false);
                            } else {
                                final DownloadProgress dlp = new DownloadProgress(commonValues.getSelectedFile());
                                final Thread t = new Thread() {
                                    @Override
                                    public void run() {
                                        while (dlp.isDownloading()) {
                                            currentGUI.setDownloadProgress("download", dlp.getProgress());
                                            try {
                                                Thread.sleep(500);
                                            } catch (final InterruptedException e) {
                                                LogWriter.writeLog("Exception attempting to open file: " + e);
                                            }
                                        }
                                    }
                                };
                                t.setDaemon(true);
                                t.start();
                                dlp.startDownload();

                                final File tempFile = dlp.getFile();

                                decode_pdf.openPdfFile(tempFile.getCanonicalPath());
                            }

                        } else {

                            //update viewer to show this
                            currentGUI.setViewerTitle("Loading linearized PDF " + commonValues.getSelectedFile());

                            //now load linearized  part
                            if (inputStream != null) {
                                decode_pdf.openPdfFileFromInputStream(inputStream, true);
                            } else {
                                decode_pdf.openPdfFileFromURL(commonValues.getSelectedFile(), true);
                            }

                            final PdfObject linearObj = (PdfObject) decode_pdf.getJPedalObject(PdfDictionary.Linearized);
                            if (linearObj != null) {
                                int linearfileLength = linearObj.getInt(PdfDictionary.L);

                                final StringBuilder message = new StringBuilder("Downloading ");
                                linearfileLength /= 1024;
                                if (linearfileLength < 1024) {
                                    message.append(linearfileLength).append(" kB");
                                } else {
                                    linearfileLength /= 1024;
                                    message.append(linearfileLength).append(" M");
                                }

                                final String fMessage = message.toString();

                                final Thread fullReaderer = new Thread() {
                                    @Override
                                    public void run() {

                                        final LinearThread linearizedBackgroundReaderer = (LinearThread) decode_pdf.getJPedalObject(PdfDictionary.LinearizedReader);

                                        while (linearizedBackgroundReaderer != null && linearizedBackgroundReaderer.isAlive()) {

                                            try {
                                                Thread.sleep(1000);
                                            } catch (final InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            currentGUI.setDownloadProgress(fMessage, linearizedBackgroundReaderer.getPercentageLoaded());

                                        }

                                        currentGUI.setDownloadProgress(fMessage, 100);

                                        processPage(commonValues, decode_pdf, currentGUI, thumbnails);

                                    }
                                };

                                fullReaderer.setDaemon(true);
                                fullReaderer.start();

                            }
                        }
                    } catch (final Exception e) {
                        currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.UrlError") + " file=" + selectedFile + '\n' + e.getMessage());

                        decode_pdf.closePdfFile();
                        fileCanBeOpened = false;
                    }
                } else {

                    //alternate code to open via array or test byteArray
                    //not recommended for large files
                    final boolean test = false;
                    if (test) {
                        final File fileSize = new File(commonValues.getSelectedFile());
                        final byte[] bytes = new byte[(int) fileSize.length()];
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(commonValues.getSelectedFile());
                            fis.read(bytes);
                            decode_pdf.openPdfArray(bytes);

                        } catch (final Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (final IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        try {
                            decode_pdf.openPdfFile(commonValues.getSelectedFile());
                        } catch (final RuntimeException e) {

                            //customise message for missing bouncycastle error
                            final String message;
                            if (e.getMessage() != null && e.getMessage().contains("bouncycastle")) {
                                message = e.getMessage();
                            } else {
                                message = "Exception in code " + e.getMessage() + " please send to IDRsolutions";
                            }

                            currentGUI.showMessageDialog(message);
                            LogWriter.writeLog("Exception " + e.getMessage());
                        }
                    }

                    if (decode_pdf.getPageCount() > 1) {
                        currentGUI.getButtons().setPageLayoutButtonsEnabled(true);
                    }

                }

                //reset thumbnails
                currentGUI.reinitThumbnails();

            } else {

                //set values for page display
                decode_pdf.resetForNonPDFPage(1);

                PageNavigator.setLastPageDecoded(1);

                final boolean isTiff = selectedFile.toLowerCase().contains(".tif");

                //decode image
                final boolean isURL = selectedFile.startsWith("http:") || selectedFile.startsWith("file:");

                if (isTiff) {
                    try {

                        PageNavigator.setTiffHelper(new TiffHelper(commonValues.getSelectedFile()));
                        final int pageCount = PageNavigator.getTiffHelper().getTiffPageCount();

                        //Default to first page
                        commonValues.setTiffImageToLoad(0);

                        //Multiple pages held within Tiff
                        if (pageCount > 1) {
                            //Set page count
                            decode_pdf.resetForNonPDFPage(pageCount);
                            commonValues.setPageCount(pageCount);
                            PageNavigator.setLastPageDecoded(1);
                            //Flag to show this is a Tiff with multiple pages
                            commonValues.setMultiTiff(true);
                            commonValues.setMultiTiff(true);

                        }

                        PageNavigator.drawMultiPageTiff(commonValues, decode_pdf);

                    } catch (final Exception e) {
                        e.printStackTrace();
                        LogWriter.writeLog("Exception " + e + Messages.getMessage("PdfViewerError.Loading") + commonValues.getSelectedFile());
                    }
                } else {
                    final String fName = selectedFile.toLowerCase();
                    if (fName.endsWith(".jpx") || fName.endsWith(".jp2") || fName.endsWith(".j2k")
                            || fName.endsWith(".psd") || fName.endsWith(".dcm")
                            || fName.endsWith(".rgb") || fName.endsWith(".sgi")
                            || fName.endsWith(".webp") || fName.endsWith(".jpg") || fName.endsWith(".jpeg")) {

                        try {
                            final java.awt.image.BufferedImage img = JDeliHelper.read(new File(selectedFile));
                            commonValues.setBufferedImg(img);
                        } catch (final Exception ex) {
                            ex.printStackTrace();
                            LogWriter.writeLog("Exception " + ex + "loading " + commonValues.getSelectedFile());
                        }

                    } else {
                        try {
                            // Load the source image from a file.
                            if (isURL) {
                                commonValues.setBufferedImg(ImageIO.read(new URL(selectedFile)));
                            } else {
                                commonValues.setBufferedImg(ImageIO.read(new File(selectedFile)));
                            }
                        } catch (final Exception e) {
                            LogWriter.writeLog("Exception " + e + "loading " + commonValues.getSelectedFile());
                        }
                    }

                }
            }

            currentGUI.updateStatusMessage("opening file");

            //popup window if needed
            if ((fileCanBeOpened) && (decode_pdf.isEncrypted()) && (!decode_pdf.isFileViewable())) {
                fileCanBeOpened = false;

                String password = System.getProperty("org.jpedal.password");
                boolean cancel = false; // Has the user clicked cancel
                while (!fileCanBeOpened && !cancel) { // Continuously ask for password until correct or cancelled
                    if (password == null) {
                        password = currentGUI.showInputDialog(Messages.getMessage("PdfViewerPassword.message")); // $NON-NLS-1$
                        cancel = password == null; // Cancel button clicked => password is still null
                    }

                    //try and reopen with new password
                    if (password != null) {
                        decode_pdf.setEncryptionPassword(password);
                        //decode_pdf.verifyAccess();

                        if (decode_pdf.isFileViewable()) {
                            fileCanBeOpened = true;
                        }

                    }

                    if (!fileCanBeOpened) {
                        currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPasswordRequired.message"));
                        password = null;
                    }
                }

            }

            //Ensure bookmarks are loaded on file open
            currentGUI.setBookmarks(true);

            if (fileCanBeOpened) {

                if (properties.getValue("Recentdocuments").equals("true")) {
                    properties.addRecentDocument(commonValues.getSelectedFile());
                    currentGUI.getRecentDocument().updateRecentDocuments(properties.getRecentDocuments());
                }

                currentGUI.getRecentDocument().addToFileList(commonValues.getSelectedFile());

                //reset values
                commonValues.setCurrentPage(1);
            }

        } catch (final PdfException e) {
            LogWriter.writeLog(("Exception " + e + " opening file"));

            if (currentGUI.isSingle()) {

                if (GUI.showMessages) {
                    ErrorDialog.showError(e, Messages.getMessage("PdfViewerOpenerror"), (Component) currentGUI.getFrame(), commonValues.getSelectedFile());
                }

                Exit.exit(thumbnails, currentGUI, commonValues, decode_pdf, properties);
            }

            throw e;
        }

        if (!decode_pdf.isOpen() && commonValues.isPDF() && decode_pdf.getJPedalObject(PdfDictionary.Linearized) == null) {
            return false;
        } else {
            return fileCanBeOpened;
        }
    }

    /**
     * opens a pdf file and calls the display/decode routines
     */
    public static void selectFile(final Values commonValues, final GUISearchWindow searchFrame,
                                  final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final PropertiesFile properties,
                                  final GUIThumbnailPanel thumbnails) {

        //remove search frame if visible
        if (searchFrame != null) {
            searchFrame.removeSearchWindow(false);
        }

        //create the file chooser to select the file
        final JFileChooser chooser = new JFileChooser(commonValues.getInputDir());
        chooser.setName("chooser");
        if (commonValues.getSelectedFile() != null) {
            chooser.setSelectedFile(new File(commonValues.getSelectedFile()));
        }
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);


        final String[][] exts = getSupportedExtensionsFilterValues();
        final String[] descriptions = getSupportedExtensionsFilterDescriptions();
        for (int i = 0; i != exts.length; i++) {
            chooser.addChoosableFileFilter(new FileFilterer(exts[i], descriptions[i]));
        }

        final int state = chooser.showOpenDialog((Component) currentGUI.getFrame());

        final File file = chooser.getSelectedFile();

        //decode
        if (file != null && state == JFileChooser.APPROVE_OPTION) {

            if (isSupportedFileExtension(file.getName())) {
                //save path so we reopen her for later selections
                try {
                    commonValues.setInputDir(chooser.getCurrentDirectory().getCanonicalPath());
                    open(file.getAbsolutePath(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

                } catch (final IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                decode_pdf.repaint();

                currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NotValidPdfWarning"));
            }

        } else { //no file selected so redisplay old
            decode_pdf.repaint();

            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoSelection"));
        }
    }

    /**
     * decode and display selected page
     */
    public static void processPage(final Values commonValues, final PdfDecoderInt decode_pdf, final GUIFactory currentGUI, final GUIThumbnailPanel thumbnails) {

        if (commonValues.isPDF() && ((decode_pdf.isOpen() || !commonValues.isPDF() || decode_pdf.getJPedalObject(PdfDictionary.Linearized) != null))) {

            //get PRODUCER and if OCR disable text printing
            final PdfFileInformation currentFileInformation = decode_pdf.getFileInformationData();

            //switch all on by default
            decode_pdf.setRenderMode(PdfDecoderInt.RENDERIMAGES + PdfDecoderInt.RENDERTEXT);

            final String[] values = currentFileInformation.getFieldValues();
            final String[] fields = PdfFileInformation.getFieldNames();

            //holding all creators that produce OCR pdf's
            final String[] ocr = {"TeleForm", "dgn2pdf", "ABBYY FineReader 8.0 Professional Edition"};

            for (int i = 0; i < fields.length; i++) {

                if ((fields[i].equals("Creator")) || (fields[i].equals("Producer"))) {

                    for (final String anOcr : ocr) {

                        if (values[i].equals(anOcr)) {

                            decode_pdf.setRenderMode(PdfDecoderInt.RENDERIMAGES);

                        }
                    }
                }
            }
        }

        //special customisations for images
        if (commonValues.isPDF()) {
            commonValues.setPageCount(decode_pdf.getPageCount());
        } else if (!commonValues.isMultiTiff()) {
            commonValues.setPageCount(1);
        }

        if (commonValues.getPageCount() < commonValues.getCurrentPage()) {
            commonValues.setCurrentPage(commonValues.getPageCount());
            System.err.println(commonValues.getCurrentPage() + " out of range. Opening on last page");
            LogWriter.writeLog(commonValues.getCurrentPage() + " out of range. Opening on last page");
        }

        //values extraction mode,dpi of images, dpi of page as a factor of 72
        decode_pdf.setExtractionMode(PdfDecoderInt.TEXT, currentGUI.getScaling());

        //update the display, including any rotation
        currentGUI.setPageNumber();

        currentGUI.resetRotationBox();

        if (commonValues.isPDF()) {
            currentGUI.decodePage();
        } else {

            //resize (ensure at least certain size)
            currentGUI.scaleAndRotate();

            //add a border
            decode_pdf.setPDFBorder(BorderFactory.createLineBorder(Color.black, 1));

            Images.decodeImage(decode_pdf, currentGUI, thumbnails, commonValues);
        }
    }

    private static String selectURL(final Values commonValues, final GUISearchWindow searchFrame,
                                    final GUIFactory currentGUI, final PdfDecoderInt decode_pdf, final PropertiesFile properties,
                                    final GUIThumbnailPanel thumbnails) {

        String selectedFile = currentGUI.showInputDialog(Messages.getMessage("PdfViewerMessage.RequestURL"));

        //lose any spaces
        if (selectedFile != null) {
            selectedFile = selectedFile.trim();
        }

        if ((selectedFile != null) && !selectedFile.trim().startsWith("http://") && !selectedFile.trim().startsWith("https://") && !selectedFile.trim().startsWith("file:/")) { //simon
            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.URLMustContain"));
            selectedFile = null;
        }

        if (selectedFile != null) {
            if (!isSupportedFileExtension(selectedFile)) {
                currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.NotValidPdfWarning"));
                selectedFile = null;
            }
        }

        if (selectedFile != null) {

            commonValues.setSelectedFile(selectedFile);

            boolean failed = false;
            try {
                final URL testExists = new URL(selectedFile);
                final URLConnection conn = testExists.openConnection();

                if (conn.getContent() == null) {
                    failed = true;
                }
            } catch (final Exception e) {
                failed = true;

                LogWriter.writeLog("Exception in handling URL " + e);
            }

            if (failed) {
                selectedFile = null;
                currentGUI.showMessageDialog("URL " + selectedFile + ' ' + Messages.getMessage("PdfViewerError.DoesNotExist"));
            }

        }

        //decode
        if (selectedFile != null) {
            try {

                commonValues.setFileSize(0);

                //save path so we reopen her for later selections                
                currentGUI.setViewerTitle(null);

            } catch (final Exception e) {
                System.err.println(Messages.getMessage("PdfViewerError.Exception") + ' ' + e + ' ' + Messages.getMessage("PdfViewerError.GettingPaths"));
            }

            //open the file
            if ((selectedFile != null) && (!Values.isProcessing())) {

                //trash previous display now we are sure it is not needed

                //if running terminate first
                thumbnails.terminateDrawing();

                decode_pdf.flushObjectValues(true);

                //reset the viewableArea before opening a new file
                decode_pdf.resetViewableArea();

                currentGUI.stopThumbnails();

                if (!currentGUI.isSingle()) {
                    currentGUI.openNewMultiplePage(commonValues.getSelectedFile(), commonValues);
                }

                OpenFile.openFile(commonValues.getSelectedFile(), commonValues, searchFrame, currentGUI, decode_pdf, properties, thumbnails);

            }

        } else { //no file selected so redisplay old

            decode_pdf.repaint();

            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NoSelection"));
        }

        return selectedFile;
    }

    /**
     * Utility method to check if the filename ends in an extension that the Viewer can handle
     * @param filename String representing name of the file to open
     * @return True if the file is supported, false otherwise
     */
    public static boolean isSupportedFileExtension(final String filename) {

        if (filename != null && !filename.isEmpty()) {
            final int idx = filename.lastIndexOf('.');

            if (idx != -1) {
                String ext = filename.substring(idx + 1);

                if (!ext.isEmpty()) {
                    ext = ext.toLowerCase();

                    final String[][] exts = getSupportedExtensionsFilterValues();
                    for (final String[] values : exts) {
                        for (final String testExt : values) {
                            if (ext.equals(testExt)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String[][] getSupportedExtensionsFilterValues() {
        return new String[][]{
                {"pdf"},
                {"fdf"},
                {"png", "tif", "tiff", "jpg", "jpeg", "jp2", "psd", "bmp", "sgi", "rgb", "jpx", "j2k", "webp"}};
    }


    public static String[] getSupportedExtensionsFilterDescriptions() {
        return new String[] {
                "Pdf (*.pdf)",
                "fdf (*.fdf)",
                "Images (Tiff, Jpeg, Png, Bmp, Sgi, Rgb, Psd ...)"};
    }
}

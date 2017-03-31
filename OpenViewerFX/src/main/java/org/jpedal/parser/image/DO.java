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
 * DO.java
 * ---------------
 */
package org.jpedal.parser.image;

import java.awt.image.BufferedImage;

import org.jpedal.exception.PdfException;
import org.jpedal.external.ErrorTracker;
import org.jpedal.external.ImageHandler;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.PdfImageData;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

public class DO extends ImageDecoder {
    public DO(final int imageCount, final PdfObjectReader currentPdfFile, final ErrorTracker errorTracker, final ImageHandler customImageHandler, final ObjectStore objectStoreStreamRef, final PdfImageData pdfImages, final PdfPageData pageData, final String imagesInFile) {
        super(imageCount, currentPdfFile, errorTracker, customImageHandler, objectStoreStreamRef, pdfImages, pageData, imagesInFile);
    }


    /**
     * process image in XObject (XForm handled in PdfStreamDecoder)
     */
    @Override
    public int processImage(final String name, final int dataPointer, final PdfObject XObject) throws PdfException {

        //set if we need
        String key = null;
        if (ImageCommands.rejectSuperimposedImages) {
            key = ((int) gs.CTM[2][0]) + "-" + ((int) gs.CTM[2][1]) + '-'
                    + ((int) gs.CTM[0][0]) + '-' + ((int) gs.CTM[1][1]) + '-'
                    + ((int) gs.CTM[0][1]) + '-' + ((int) gs.CTM[1][0]);
        }

        try {

            // Internal tests can disable images to speed up conversion
            if (System.getProperty("testsDisableImages") == null) {
                processXImage(name, name, key, XObject);
            }

        } catch (final Error e) {

            LogWriter.writeLog("Error: " + e.getMessage());

            parserOptions.imagesProcessedFully = false;
            errorTracker.addPageFailureMessage("Error " + e + " in DO");
        } catch (final Exception e) {

            LogWriter.writeLog("Exception " + e);

            parserOptions.imagesProcessedFully = false;
            errorTracker.addPageFailureMessage("Error " + e + " in DO");
        }

        return dataPointer;

    }


    private void processXImage(final String name, String details, final String key, final PdfObject XObject) throws PdfException {

        final int previousUse = -1;

        if (ImageCommands.trackImages) {
            details += " Image";
            if (imagesInFile == null) {
                imagesInFile = "";
            }
        }

        final boolean isForHTML = current.isHTMLorSVG();

        /*don't process unless needed*/
        if (parserOptions.imagesNeeded()) {

            //read stream for image
            final byte[] objectData = currentPdfFile.readStream(XObject, true, true, false, false, false, XObject.getCacheName(currentPdfFile.getObjectReader()));

            //flag issue
            if (objectData == null) {
                parserOptions.imagesProcessedFully = false;
            } else {

                //generate name including filename to make it unique less /
                currentImage = parserOptions.getFileName() + '-' + name;

                //process the image and save raw version

                BufferedImage image = processImageXObject(XObject, name, objectData, details);

                //fix for oddity in Annotation
                if (image != null && image.getWidth() == 1 && image.getHeight() == 1 && parserOptions.isType3Font()) {
                    image.flush();
                    image = null;
                }

                //save transformed image
                if (image != null) {

                    if (isForHTML) {
                        current.drawImage(parserOptions.getPageNumber(), image, gs, false, name, -2);
                    } else {

                        if (parserOptions.renderImages()) {
                            gs.x = gs.CTM[2][0];
                            gs.y = gs.CTM[2][1];

                            final int id = current.drawImage(parserOptions.getPageNumber(), image, gs, false, name, previousUse);

                            /*
                             * store last usage in case it reappears unless it is transparent
                             */
                            if (ImageCommands.rejectSuperimposedImages && key != null) {
                                cache.setImposedKey(key, id);
                            }
                        }

                        if (parserOptions.isPageContent() && ImageCommands.isExtractionAllowed(currentPdfFile)) {
                            if (parserOptions.isClippedImagesExtracted()) {
                                generateClippedImage(image);
                            } else if (parserOptions.isFinalImagesExtracted() || parserOptions.isRawImagesExtracted()) {
                                try {
                                    generateTransformedImageSingle(image);
                                } catch (final Exception e) {
                                    LogWriter.writeLog("Exception " + e + " on transforming image in file");
                                }
                            }
                        }
                    }

                    image.flush();
                }
            }
        }
    }
}

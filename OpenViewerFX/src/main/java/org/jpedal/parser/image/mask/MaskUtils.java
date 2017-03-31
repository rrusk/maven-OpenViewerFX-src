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
 * MaskUtils.java
 * ---------------
 */
package org.jpedal.parser.image.mask;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import org.jpedal.color.PdfPaint;
import org.jpedal.exception.PdfException;
import org.jpedal.function.FunctionFactory;
import org.jpedal.function.PDFFunction;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.FunctionObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.parser.image.PDFObjectToImage;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.SwingDisplay;
import org.jpedal.utils.LogWriter;

public class MaskUtils {


    /**
     * @param XObject
     * @param name
     * @param newSMask
     * @param gs
     * @param current
     * @param currentPdfFile
     * @param parserOptions
     * @param formLevel
     * @param multiplyer
     * @param useTransparancy
     * @throws PdfException
     */
    public static void createMaskForm(final PdfObject XObject, final String name, final PdfObject newSMask, final GraphicsState gs,
                                      final DynamicVectorRenderer current, final PdfObjectReader currentPdfFile,
                                      final ParserOptions parserOptions, final int formLevel, final float multiplyer, final boolean useTransparancy, final int blendMode) {

        final float[] BBox = XObject.getFloatArray(PdfDictionary.BBox);

        //System.out.println("x,y="+gs.CTM[2][0]+" "+gs.CTM[2][1]+" "+BBox[0]+" "+BBox[1]+" "+BBox[2]+" "+BBox[3]+" "+newSMask);
        
        /*get form as an image*/
        int fx = (int) BBox[0];
        int fw = (int) BBox[2];
        int fy = (int) BBox[1];
        int fh = (int) BBox[3];

        if (fw == 32768 || fh == 32768) { //means we should ignore bbox
            fx = 0;
            fy = 0;
            fw = 1;
            fh = 1;
        }

        final int iw, ih;
        float scaling = 4f;
        if (fw * fh >= 25000000) {
            scaling = 1f;
        }

        BufferedImage image = PDFObjectToImage.getImageFromPdfObject(XObject, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer, (newSMask == null && useTransparancy), scaling);

        if (newSMask != null) { //apply SMask to image

            final BufferedImage smaskImage = PDFObjectToImage.getImageFromPdfObject(newSMask, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer, false, scaling);

            int[] tr = null;
            final PdfObject objTR = gs.SMask.getDictionary(PdfDictionary.TR);
            //smask may contain tr values refer to case 25259
            if (objTR != null) {
                final PdfObject funcObj = new FunctionObject(objTR.getObjectRefAsString());
                currentPdfFile.readObject(funcObj);
                final PDFFunction function = FunctionFactory.getFunction(funcObj, currentPdfFile);
                if (function != null) {
                    tr = new int[256];
                    for (int i = 0; i < 256; i++) {
                        tr[i] = (int) (function.compute(new float[]{i / 255f})[0] * 255);
                    }
                }
            }

            if (gs.SMask.getNameAsConstant(PdfDictionary.S) == PdfDictionary.Luminosity) {
                final float[] bcFloats = gs.SMask.getFloatArray(PdfDictionary.BC);
                if (bcFloats != null) {
                    final PdfPaint prev = gs.nonstrokeColorSpace.getColor();
                    gs.nonstrokeColorSpace.setColor(bcFloats, bcFloats.length);
                    final int bc = gs.nonstrokeColorSpace.getColor().getRGB();
                    gs.nonstrokeColorSpace.setColor(prev);
                    image = SMask.applyLuminosityMask(image, smaskImage, tr, true, bc);
                } else {
                    image = SMask.applyLuminosityMask(image, smaskImage, tr, false, 0);
                }
            } else {
                image = SMask.applyAlphaMask(image, smaskImage);
            }
            if (smaskImage != null) {
                smaskImage.flush();
            }
        }

        iw = (int) (image.getWidth() / scaling);
        ih = (int) (image.getHeight() / scaling);

        final GraphicsState gs1; //add in gs

        boolean isChanged = false;
        if (newSMask == null && gs.getAlphaMax(GraphicsState.FILL) < 1f) {

            isChanged = true;

            gs1 = new GraphicsState(); //add in gs
            gs1.setMaxAlpha(GraphicsState.FILL, gs.getAlphaMax(GraphicsState.FILL));
            gs1.setMaxAlpha(GraphicsState.STROKE, gs.getAlphaMax(GraphicsState.STROKE));

            current.setGraphicsState(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE), PdfDictionary.Normal);
            current.setGraphicsState(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL), blendMode); //look at transparency design guide in pdf

        } else {
            if (formLevel == 1) {
                gs1 = new GraphicsState(gs); //add in gs
            } else {
                gs1 = new GraphicsState(); //add in gs
            }
        }

        final int prevBM = gs1.getBMValue();
        if (formLevel == 1) { //dont blend if it is not formlevel 1
            gs1.setBMValue(blendMode);
        }

        gs1.CTM = new float[][]{{iw, 0, 1}, {0, ih, 1}, {0, 0, 0}};

        //different formula needed if flattening forms
        if (parserOptions.isFlattenedForm()) {
            gs1.x = parserOptions.getflattenX();
            gs1.y = parserOptions.getflattenY();
        } else {

            gs1.x = fx;

            if (fy < fh) {
                gs1.y = fy;
            } else {
                gs1.y = fy - ih;
            }
        }

        //see case 20638 for this quick fix we can use affine transform in future
        gs1.CTM[2][0] = (gs1.x * gs.CTM[0][0]) + gs.CTM[2][0];
        gs1.CTM[2][1] = (gs1.y * gs.CTM[1][1]) + gs.CTM[2][1];

        //factor in any scaling and invert
        gs1.CTM[0][0] *= gs.CTM[0][0];
        gs1.CTM[1][1] = -gs1.CTM[1][1] * gs.CTM[1][1];

        gs1.CTM[2][1] -= gs1.CTM[1][1];

        //separate call needed to paint image on thumbnail or background image in HTML/SVG
        if (current.isHTMLorSVG()) {
            
            /*
             * explicitly need clip passed through
             */
            final Area clip = gs.getClippingShape();
            if (clip != null) {
                gs1.updateClip(clip);
            }

            //we always use high res for these in html to give best quality results
            //final boolean currentHiResSetting = current.getHiResImageForDisplayMode();
            //current.setHiResImageForDisplayMode(true);

            current.drawImage(parserOptions.getPageNumber(), image, gs1, false, name, -2);

            //restore default
            //current.setHiResImageForDisplayMode(currentHiResSetting);

        } else {
            gs1.x = gs1.CTM[2][0];
            gs1.y = gs1.CTM[2][1];

            current.drawImage(parserOptions.getPageNumber(), image, gs1, false, name, -1);
        }

        gs1.setBMValue(prevBM);

        if (isChanged) {
            current.setGraphicsState(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE), gs.getBMValue());
            current.setGraphicsState(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL), gs.getBMValue());
        }
    }

    public static BufferedImage createTransparentForm(final PdfObject XObject, final int fy, final int fw, final int fh,
                                                      final PdfObjectReader currentPdfFile, final ParserOptions parserOptions, final int formLevel, final float multiplyer) {

        final BufferedImage image;
        final byte[] objectData1 = currentPdfFile.readStream(XObject, true, true, false, false, false, XObject.getCacheName(currentPdfFile.getObjectReader()));

        final ObjectStore localStore = new ObjectStore();
        final DynamicVectorRenderer glyphDisplay = new SwingDisplay(0, false, 20, localStore);

        final PdfStreamDecoder glyphDecoder = new PdfStreamDecoder(currentPdfFile);
        glyphDecoder.setParameters(true, true, 3, 65, false, parserOptions.useJavaFX());

        glyphDecoder.setObjectValue(ValueTypes.ObjectStore, localStore);
        // glyphDecoder.setObjectValue(Options.ErrorTracker, errorTracker);
        glyphDecoder.setFormLevel(formLevel);
        glyphDecoder.setMultiplyer(multiplyer);
//        glyphDecoder.setFloatValue(SamplingUsed, samplingUsed);

        glyphDecoder.setRenderer(glyphDisplay);

        /*read any resources*/
        try {

            final PdfObject SMaskResources = XObject.getDictionary(PdfDictionary.Resources);
            if (SMaskResources != null) {
                glyphDecoder.readResources(SMaskResources, false);
            }

        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }

        /*decode the stream*/
        if (objectData1 != null) {
            glyphDecoder.decodeStreamIntoObjects(objectData1, false);
        }

        int hh = fh;
        //float diff=fy;
        if (fy > fh) {
            hh = fy - fh;
            //   diff=fh;
        }

        //as we make it image, make it bigger to retain quality
        final int scaling = 4;

        //get bit underneath and merge in
        image = new BufferedImage(scaling * fw, scaling * hh, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D formG2 = image.createGraphics();

        formG2.setColor(Color.WHITE);
        formG2.fillRect(0, 0, scaling * fw, scaling * hh);
        formG2.translate(0, scaling * hh);
        formG2.scale(1, -1);
        formG2.scale(scaling, scaling);

        //current.paint(formG2,null,null,null,false,true);

        glyphDisplay.setG2(formG2);
        glyphDisplay.paint(null, null, null);

        localStore.flush();

        return image;
    }

}

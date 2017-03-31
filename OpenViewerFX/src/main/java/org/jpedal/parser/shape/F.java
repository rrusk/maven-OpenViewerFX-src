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
 * F.java
 * ---------------
 */
package org.jpedal.parser.shape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.PdfPaint;
import org.jpedal.color.ShearedTexturePaint;
import org.jpedal.external.ShapeTracker;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfShape;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.PdfObjectCache;
import org.jpedal.parser.image.PDFObjectToImage;
import org.jpedal.render.DynamicVectorRenderer;

public class F {


    public static void execute(final int tokenNumber, final boolean isStar, final int formLevel, final PdfShape currentDrawShape, final GraphicsState gs,
                               final PdfObjectCache cache, final PdfObjectReader currentPdfFile, final DynamicVectorRenderer current, final ParserOptions parserOptions, final float multiplyer) {

        //ignore transparent white if group set
        if ((formLevel > 1 && cache.groupObj != null && !cache.groupObj.getBoolean(PdfDictionary.K) && gs.getAlphaMax(GraphicsState.FILL) > 0.99f && (gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceCMYK))

                && (gs.nonstrokeColorSpace.getColor().getRGB() == -1)) {
            currentDrawShape.resetPath();
            return;
        }
        
        
        /*
         * if SMask with this color, we need to ignore
         *  (only case of white with BC of 1,1,1 at present for 11jun/12.pdf)
         */
        if (gs.SMask != null && gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceCMYK) {

            final float[] BC = gs.SMask.getFloatArray(PdfDictionary.BC);
            if (gs.nonstrokeColorSpace.getColor().getRGB() == -16777216 && BC != null && BC[0] == 1.0f) {
                currentDrawShape.resetPath();
                return;
            }
        }
        
        /*
         * if SMask with this color, we need to ignore
         *  (only case of white with BC of 1,1,1 at present for 11jun/4.pdf)
         */
        if (gs.SMask != null && gs.nonstrokeColorSpace.getID() == ColorSpaces.ICC) {

            final float[] BC = gs.SMask.getFloatArray(PdfDictionary.BC);
            if (gs.nonstrokeColorSpace.getColor().getRGB() == -16777216 && BC != null && BC[0] == 0.0f) {
                currentDrawShape.resetPath();
                return;
            }
        }

        //replace F with image if soft mask set (see randomHouse/9781609050917_DistX.pdf)
        if (gs.SMask != null && gs.SMask.getDictionary(PdfDictionary.G) != null &&
                (gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceRGB || gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceCMYK)) {

            if (isStar) {
                currentDrawShape.setEVENODDWindingRule();
            } else {
                currentDrawShape.setNONZEROWindingRule();
            }

            currentDrawShape.closeShape();
            final Shape currentShape = currentDrawShape.generateShapeFromPath(gs.CTM, gs.getLineWidth(), Cmd.F);

            if (currentShape == null) {
                createSMaskFill(gs, currentPdfFile, current, parserOptions, formLevel, multiplyer);
            } else {
                final BufferedImage result = getSmaskAppliedImage(gs, currentPdfFile, parserOptions, formLevel, multiplyer);
                final PdfObject maskObj = gs.SMask.getDictionary(PdfDictionary.G);
                currentPdfFile.checkResolved(maskObj);
                final float[] BBox = maskObj.getFloatArray(PdfDictionary.BBox);
                final int fx = (int) (BBox[0] + 0.5f);
                final int fy = (int) (BBox[1] + 0.5f);

                final Rectangle rect = new Rectangle(fx, fy, result.getWidth(), result.getHeight());

                final GraphicsState gs1 = gs.deepCopy();
                gs1.setNonstrokeColor(new ShearedTexturePaint(result, rect, new AffineTransform(gs.CTM[0][0], gs.CTM[0][1], gs.CTM[1][0], gs.CTM[1][1], gs.CTM[2][0], gs.CTM[2][1])));
                gs1.setFillType(GraphicsState.FILL);
                gs1.setStrokeColor(gs.strokeColorSpace.getColor());

                current.drawShape(currentDrawShape, gs1, Cmd.F);

            }

            currentDrawShape.resetPath();

            return;
        }

        // (see randomHouse/9781609050917_DistX.pdf)
//if(gs.SMask!=null && (gs.SMask.getGeneralType(PdfDictionary.SMask)==PdfDictionary.None || gs.SMask.getGeneralType(PdfDictionary.SMask)==PdfDictionary.Multiply) && gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceRGB && gs.getOPM()==1.0f && gs.nonstrokeColorSpace.getColor().getRGB()==-16777216){

        if (gs.SMask != null && gs.SMask.getGeneralType(PdfDictionary.SMask) != PdfDictionary.None && gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceRGB && gs.getOPM() == 1.0f && gs.nonstrokeColorSpace.getColor().getRGB() == -16777216) {
            currentDrawShape.resetPath();
            return;
        }

        if (parserOptions.isLayerVisible()) {

            //set Winding rule
            if (isStar) {
                currentDrawShape.setEVENODDWindingRule();
            } else {
                currentDrawShape.setNONZEROWindingRule();
            }

            currentDrawShape.closeShape();

            Shape currentShape = null;
            Object fxPath = null;
            
            /*
             * fx alternative
             */
            if (parserOptions.useJavaFX()) {
                fxPath = currentDrawShape.getPath();
            } else {
                //generate swing shape and stroke and status. Type required to check if EvenOdd rule emulation required.
                currentShape = currentDrawShape.generateShapeFromPath(gs.CTM, gs.getLineWidth(), Cmd.F);
            }

            boolean hasShape = currentShape != null || fxPath != null;

            //track for user if required
            final ShapeTracker customShapeTracker = parserOptions.getCustomShapeTraker();
            if (customShapeTracker != null) {

                if (isStar) {
                    customShapeTracker.addShape(tokenNumber, Cmd.Fstar, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                } else {
                    customShapeTracker.addShape(tokenNumber, Cmd.F, currentShape, gs.nonstrokeColorSpace.getColor(), gs.strokeColorSpace.getColor());
                }

            }

            //do not paint white CMYK in overpaint mode
            if (hasShape && gs.getAlpha(GraphicsState.FILL) < 1 &&
                    gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceN && gs.getOPM() == 1.0f &&
                    gs.nonstrokeColorSpace.getColor().getRGB() == -16777216) {

                //System.out.println(gs.getNonStrokeAlpha());
                //System.out.println(nonstrokeColorSpace.getAlternateColorSpace()+" "+nonstrokeColorSpace.getColorComponentCount()+" "+nonstrokeColorSpace.pantoneName);
                boolean ignoreTransparent = true; //assume true and disprove
                final float[] raw = gs.nonstrokeColorSpace.getRawValues();

                if (raw != null) {
                    final int count = raw.length;
                    for (int ii = 0; ii < count; ii++) {

                        //System.out.println(ii+"="+raw[ii]+" "+count);

                        if (raw[ii] > 0) {
                            ignoreTransparent = false;
                            ii = count;
                        }
                    }
                }

                if (ignoreTransparent) {
                    hasShape = false;
                }
            }

            //save for later
            if (hasShape && parserOptions.isRenderPage()) {
                gs.setStrokeColor(gs.strokeColorSpace.getColor());
                gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());
                gs.setFillType(GraphicsState.FILL);

                if (parserOptions.useJavaFX()) {
                    current.drawShape(fxPath, gs);
                } else {
                    current.drawShape(currentDrawShape, gs, Cmd.F);

                    if (current.isHTMLorSVG() && cache.groupObj == null) {
                        current.eliminateHiddenText(currentShape, gs, currentDrawShape.getSegmentCount(), false);
                    }
                }
            }
        }
        //always reset flag
        currentDrawShape.setClip(false);
        currentDrawShape.resetPath(); // flush all path ops stored

    }


    private static BufferedImage getSmaskAppliedImage(final GraphicsState gs, final PdfObjectReader currentPdfFile,
                                                      final ParserOptions parserOptions, final int formLevel, final float multiplyer) {
        final PdfObject maskObj = gs.SMask.getDictionary(PdfDictionary.G);
        currentPdfFile.checkResolved(maskObj);
        final float[] BBox = maskObj.getFloatArray(PdfDictionary.BBox);
        final int fx = (int) (BBox[0] + 0.5f);
        final int fy = (int) (BBox[1] + 0.5f);
        final int fw = (int) (BBox[2] + 0.5f);
        final int fh = (int) (BBox[3] + 0.5f);

        final BufferedImage smaskImage = PDFObjectToImage.getImageFromPdfObject(maskObj, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer, false, 1f);

        final PdfPaint prev = gs.nonstrokeColorSpace.getColor();
        final int prevInt = prev.getRGB();

        final float[] BC = gs.SMask.getFloatArray(PdfDictionary.BC);
        int brgb = 0;
        boolean hasBC = false;
        if (BC != null) {
            gs.nonstrokeColorSpace.setColor(BC, BC.length);
            brgb = gs.nonstrokeColorSpace.getColor().getRGB();
            gs.nonstrokeColorSpace.setColor(prev);
            hasBC = true;
        }
        final int pa = (prevInt >>> 24);
//		int ba = ((brgb >> 24) & 0xff);
        final int br = ((brgb >> 16) & 0xff);
        final int bg = ((brgb >> 8) & 0xff);
        final int bb = (brgb & 0xff);

        final BufferedImage result = new BufferedImage(smaskImage.getWidth(), smaskImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final int[] sPixels = ((DataBufferInt) smaskImage.getRaster().getDataBuffer()).getData();
        final int[] dPixels = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < sPixels.length; i++) {
            final int sargb = sPixels[i];
            final int sa = ((sargb >>> 24) & 0xff);
            int sr = ((sargb >> 16) & 0xff);
            int sg = ((sargb >> 8) & 0xff);
            int sb = (sargb & 0xff);
            if (hasBC) {
                if (sa == 0) {
                    sr = br;
                    sg = bg;
                    sb = bb;
                } else if (sa < 255) {
                    final int alpha_ = 255 - sa;
                    sr = (sr * sa + br * alpha_) >> 8;
                    sg = (sg * sa + bg * alpha_) >> 8;
                    sb = (sb * sa + bb * alpha_) >> 8;
                }
            }

            int y = (sr * 77) + (sg * 152) + (sb * 28);
            y = (pa * y) >> 16;
            dPixels[i] = (y << 24) | (prevInt & 0xffffff);
        }

        smaskImage.flush();
        return result;
    }


    /**
     * make image from SMask and colour in with fill colour to simulate effect
     */
    private static void createSMaskFill(final GraphicsState gs, final PdfObjectReader currentPdfFile,
                                        final DynamicVectorRenderer current, final ParserOptions parserOptions, final int formLevel, final float multiplyer) {

        final PdfObject maskObj = gs.SMask.getDictionary(PdfDictionary.G);
        currentPdfFile.checkResolved(maskObj);
        final float[] BBox; //size
        BBox = maskObj.getFloatArray(PdfDictionary.BBox);
        
        /*get dimensions as an image*/
        int fx = (int) (BBox[0] + 0.5f);
        final int fy = (int) (BBox[1] + 0.5f);
        final int fw = (int) (BBox[2] + 0.5f);
        final int fh = (int) (BBox[3] + 0.5f);

        //check x,y offsets and factor in
        if (fx < 0) {
            fx = 0;
        }
        
        /*
         * get the SMAsk
         */
        final BufferedImage smaskImage = PDFObjectToImage.getImageFromPdfObject(maskObj, fx, fw, fy, fh, currentPdfFile, parserOptions, formLevel, multiplyer, false, 1f);
        
        
        /*
         * draw the shape as image
         */
        final GraphicsState gs1 = gs.deepCopy();
        gs1.CTM = new float[][]{{smaskImage.getWidth(), 0, 1}, {0, -smaskImage.getHeight(), 1}, {0, 0, 0}};
        gs1.x = fx;
        gs1.y = fy;

        //add as image
        gs1.CTM[2][0] = gs1.x;
        gs1.CTM[2][1] = gs1.y;

        //old method useful for testing purposes
        if (1 == 2) {
            gs1.setBMValue(PdfDictionary.SMask);
            current.drawImage(parserOptions.getPageNumber(), smaskImage, gs1, false, "F", -1);
            return;
        }

        //now do the smasking in image
        final BufferedImage result;

        final PdfPaint prev = gs.nonstrokeColorSpace.getColor();
        final int prevInt = prev.getRGB();

        final float[] BC = gs.SMask.getFloatArray(PdfDictionary.BC);
        int brgb = 0;
        if (BC != null) {
            gs.nonstrokeColorSpace.setColor(BC, BC.length);
            brgb = gs.nonstrokeColorSpace.getColor().getRGB();
            gs.nonstrokeColorSpace.setColor(prev);
        }

        final int br = ((brgb >> 16) & 0xff);
        final int bg = ((brgb >> 8) & 0xff);
        final int bb = (brgb & 0xff);

        result = new BufferedImage(smaskImage.getWidth(), smaskImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final int[] sPixels = ((DataBufferInt) smaskImage.getRaster().getDataBuffer()).getData();
        final int[] dPixels = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < sPixels.length; i++) {
            final int sargb = sPixels[i];
            int sa = ((sargb >>> 24) & 0xff);
            int sr = ((sargb >> 16) & 0xff);
            int sg = ((sargb >> 8) & 0xff);
            int sb = (sargb & 0xff);
            if (sa == 0) {
                sr = br;
                sg = bg;
                sb = bb;
            } else if (sa < 255) {
                final int alpha_ = 255 - sa;
                sr = (sr * sa + br * alpha_) >> 8;
                sg = (sg * sa + bg * alpha_) >> 8;
                sb = (sb * sa + bb * alpha_) >> 8;
            }
            final int y = (sr * 77) + (sg * 152) + (sb * 28);
            sa = (sa * y) >> 16;
            dPixels[i] = (sa << 24) | (prevInt & 0xffffff);
        }

        current.drawImage(parserOptions.getPageNumber(), result, gs1, false, "F", -1);

        smaskImage.flush();

    }

}

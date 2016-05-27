/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2016 IDRsolutions and Contributors.
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
 * TilingPaint.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.Serializable;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.Matrix;

public class TilingPaint implements PdfPaint, Paint, Serializable {

    private final int[][] imageArr;
    private final AffineTransform affine;
    private final float[][] mm;
    private final float[][] nn;
    private final float xStep;
    private final float yStep;
    private final Rectangle2D rectBBox;

    @Override
    public void setScaling(double cropX, double cropH, float scaling, float textX, float textY) {
    }

    @Override
    public boolean isPattern() {
        return false;
    }

    @Override
    public boolean isTexture() {
        return true;
    }

    @Override
    public int getRGB() {
        return 0;
    }

    @Override
    public int getTransparency() {
        return 255;
    }

    public TilingPaint(final PdfObject patternObj, final byte[] streamData, PatternColorSpace colorSpace) {
        float[] inputs = patternObj.getFloatArray(PdfDictionary.Matrix);
        if (inputs != null) {
            mm = new float[][]{{inputs[0], inputs[1], 0f}, {inputs[2], inputs[3], 0f}, {inputs[4], inputs[5], 1f}};
        } else {
            mm = new float[][]{{1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f}};
        }
        affine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);

        final float[] rawBBox = patternObj.getFloatArray(PdfDictionary.BBox);

        GeneralPath rawPath = new GeneralPath();
        rawPath.moveTo(rawBBox[0], rawBBox[1]);
        rawPath.lineTo(rawBBox[2], rawBBox[1]);
        rawPath.lineTo(rawBBox[2], rawBBox[3]);
        rawPath.lineTo(rawBBox[0], rawBBox[3]);
        rawPath.lineTo(rawBBox[0], rawBBox[1]);
        rawPath.closePath();
        Shape rawShape = rawPath.createTransformedShape(affine);
        rectBBox = rawShape.getBounds2D();

        int iw = (int) rectBBox.getWidth();
        int ih = (int) rectBBox.getHeight();

        iw = iw == 0 ? 1 : iw;
        ih = ih == 0 ? 1 : ih;

        BufferedImage image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        final ObjectStore localStore = new ObjectStore();
        PatternDisplay glyphDisplay;

        nn = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(mm[i], 0, nn[i], 0, 3);
        }

        nn[2][0] = (float) (mm[2][0] - rectBBox.getX());
        nn[2][1] = (float) (mm[2][1] - rectBBox.getY());
        glyphDisplay = colorSpace.decodePatternContent(patternObj, nn, streamData, localStore);
        Graphics2D g2 = image.createGraphics();
        glyphDisplay.setG2(g2);
        glyphDisplay.paint(null, null, null);

        imageArr = new int[ih][iw];
        int[] imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int p = 0;
        for (int i = 0; i < ih; i++) {
            for (int j = 0; j < iw; j++) {
                imageArr[i][j] = imageData[p++];
            }
        }

        float rawXStep = patternObj.getFloatNumber(PdfDictionary.XStep);
        float rawYStep = patternObj.getFloatNumber(PdfDictionary.YStep);

        GeneralPath stepPath = new GeneralPath();
        stepPath.moveTo(0, 0);
        stepPath.lineTo(rawXStep, 0);
        stepPath.lineTo(rawXStep, rawYStep);
        stepPath.lineTo(0, rawYStep);
        stepPath.lineTo(0, 0);
        stepPath.closePath();
        Shape stepShape = stepPath.createTransformedShape(affine);
        Rectangle2D stepRect = stepShape.getBounds2D();

//        System.out.println(rawBBox[0]+" "+rawBBox[1]+" "+rawBBox[2]+" "+rawBBox[3]);
//        System.out.println("rawbboxrect : "+rectBBox);        
//        System.out.println(affine);
//        System.out.println(stepRect);
//        try {
//            ImageIO.write(image, "png", new java.io.File("C:\\Users\\suda\\Desktop\\testimages\\" + System.currentTimeMillis() + ".png"));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        xStep = (float) (stepRect.getWidth());
        yStep = (float) (stepRect.getHeight());

    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
        return new TilingContext(imageArr, xform, rectBBox);
    }

    private class TilingContext implements PaintContext {

        private final int[][] imagePixels;
        private final float[][] toUserSpace;
        private final float offX, offY;

        TilingContext(int[][] imageArr, AffineTransform xform, Rectangle2D rectBBox) {
            this.imagePixels = imageArr;
            float[][] xformMatrix = {
                {(float) xform.getScaleX(), (float) xform.getShearX(), 0},
                {(float) xform.getShearY(), (float) xform.getScaleY(), 0},
                {(float) xform.getTranslateX(), (float) xform.getTranslateY(), 1}
            };
            toUserSpace = Matrix.inverse(xformMatrix);
            offX = (float) rectBBox.getMinX();
            offY = (float) rectBBox.getMinY();
        }

        @Override
        public Raster getRaster(int x, int y, int w, int h) {
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
            int iw = imagePixels[0].length;
            int ih = imagePixels.length;
            int p = 0;
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    float[] src = {x + j, y + i};
                    src = Matrix.transformPoint(toUserSpace, src[0], src[1]);
                    float xx = src[0];
                    float yy = src[1];
                    pixels[p++] = getTilingPixel(imagePixels, iw, ih, xStep, yStep, xx, yy, offX, offY);
                }
            }
            return img.getRaster();
        }

        @Override
        public void dispose() {
        }

        @Override
        public ColorModel getColorModel() {
            return ColorModel.getRGBdefault();
        }
    }

    static int getTilingPixel(int[][] imagePixels, int iw, int ih, float xStep, float yStep, float xx, float yy, float offX, float offY) {
        float x = (xx - offX);
        float y = (yy - offY);
        float x_ = x % xStep;
        float y_ = y % yStep;
        if (y < 0) {
            y_ = yStep + y_;
        }
        if (x < 0) {
            x_ = xStep + x_;
        }

        int bx = (int) x_;
        int by = (int) y_;

        if (bx > -1 && by > -1 && bx < iw && by < ih) {
            return imagePixels[by][bx];
        }
        return 0;
    }

}

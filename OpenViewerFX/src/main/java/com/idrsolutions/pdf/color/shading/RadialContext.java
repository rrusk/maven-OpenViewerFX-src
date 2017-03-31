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
 * RadialContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

public class RadialContext implements PaintContext {

    private final GenericColorSpace shadingColorSpace;
    private final float[] background;
    private final PdfObject shadingObj;
    private final PDFFunction[] function;
    private final float[] coords;
    private boolean[] extended = {false, false};
    private float t0;
    private float t1 = 1.0f;
    private final float x0, y0, r0, x1, y1, r1, deltaX, deltaY, deltaR, deltaC, powerR0;
    private final Color colorT0, colorT1;
    AffineTransform inversed = new AffineTransform();

    RadialContext(final AffineTransform xForm, final GenericColorSpace shadingColorSpace, final float[] background, final PdfObject shading, final float[][] mm, final PDFFunction[] function) {

        this.shadingColorSpace = shadingColorSpace;
        this.background = background;
        this.shadingObj = shading;
        this.function = function;
        final float[] src = shading.getFloatArray(PdfDictionary.Coords);
        final boolean[] extension = shadingObj.getBooleanArray(PdfDictionary.Extend);
        if (extension != null) {
            extended = extension;
        }
        final float[] domain = shadingObj.getFloatArray(PdfDictionary.Domain);
        if (domain != null) {
            t0 = domain[0];
            t1 = domain[1];
        }

        coords = new float[src.length];
        System.arraycopy(src, 0, coords, 0, src.length);

        AffineTransform shadeAffine = new AffineTransform();
        if (mm != null) {
            shadeAffine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);
        }

        try {
            final AffineTransform invXF = xForm.createInverse();
            final AffineTransform invSH = shadeAffine.createInverse();
            invSH.concatenate(invXF);
            inversed = (AffineTransform) invSH.clone();
        } catch (final NoninvertibleTransformException ex) {
            LogWriter.writeLog("Exception " + ex + ' ');
        }

        x0 = coords[0];
        y0 = coords[1];
        r0 = coords[2];

        x1 = coords[3];
        y1 = coords[4];
        r1 = coords[5];

        colorT0 = calculateColor(t0);
        colorT1 = calculateColor(t1);

        //dont use Math.pow functions here;
        deltaX = x1 - x0;
        deltaY = y1 - y0;
        deltaR = r1 - r0;
        deltaC = deltaX * deltaX + deltaY * deltaY - deltaR * deltaR;
        powerR0 = r0 * r0;
//        System.out.println("page height "+pageHeight+" offx"+offX+" offY"+offY+" pagew"+cropX);
    }

    @Override
    public void dispose() {

    }

    @Override
    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    private Color calculateColor(final float val) {
        final float[] colValues = ShadingFactory.applyFunctions(function, new float[]{val});
        shadingColorSpace.setColor(colValues, colValues.length);
        return (Color) shadingColorSpace.getColor();
    }

    @Override
    public Raster getRaster(final int startX, final int startY, final int w, final int h) {

        final int[] data = new int[w * h * 4];
        if (background != null) {
            shadingColorSpace.setColor(background, shadingColorSpace.getColorComponentCount());
            final Color c = (Color) shadingColorSpace.getColor();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    final int base = (y * w + x) * 4;
                    data[base] = c.getRed();
                    data[base + 1] = c.getGreen();
                    data[base + 2] = c.getBlue();
                    data[base + 3] = 255;
                }
            }
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final float[] xy = {startX + x, startY + y};
                inversed.transform(xy, 0, xy, 0, 1);
                Color result = null;

                final float[] qr = quadraticEquate(xy[0], xy[1]);

                if (qr[1] >= 0 && qr[1] <= 1) {
                    result = calculateColor(getTfromS(qr[1]));
                } else if (extended[1] && qr[1] >= 0 && r1 + qr[1] * deltaR >= 0) {
                    result = colorT1;
                } else if (qr[0] >= 0 && qr[0] <= 1) {
                    result = calculateColor(getTfromS(qr[0]));
                } else if (extended[0] && qr[1] <= 0 && r1 + qr[1] * deltaR >= 0) {
                    result = calculateColor(getTfromS(qr[1]));
                } else if (extended[0] && qr[0] <= 1 && r1 + qr[0] * deltaR >= 0) {
                    result = colorT0;
                }

                if (result != null) {
                    final int base = (y * w + x) * 4;
                    data[base] = result.getRed();
                    data[base + 1] = result.getGreen();
                    data[base + 2] = result.getBlue();
                    data[base + 3] = 255;
                }

            }
        }
        final WritableRaster raster = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB).getRaster();
        raster.setPixels(0, 0, w, h, data);
        return raster;
    }

    private float getTfromS(final float s) {
        return (s * (t1 - t0)) + t0;
    }

    private float[] quadraticEquate(final float x, final float y) {
        final float xDiff = x - x0;
        final float yDiff = y - y0;
        final float p = -xDiff * deltaX - yDiff * deltaY - r0 * deltaR;
        final float q = xDiff * xDiff + yDiff * yDiff - powerR0; //dont use Math.pow to xdiff,ydiff;
        final float sqrt = (float) Math.sqrt(p * p - deltaC * q);
        final float sA = (sqrt - p) / deltaC;
        final float sB = (-p - sqrt) / deltaC;
        return ((deltaC < 0) ? new float[]{sA, sB} : new float[]{sB, sA});
    }

}

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
 * ShadedPaint.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.Serializable;
import java.util.ArrayList;

import org.jpedal.color.ColorspaceFactory;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PdfPaint;
import org.jpedal.function.FunctionFactory;
import org.jpedal.function.PDFFunction;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.utils.Matrix;

/**
 * template for all shading operations
 */
public class ShadedPaint implements PdfPaint, Serializable {

    public static final int FUNCTION = 1;
    public static final int AXIAL = 2;
    public static final int RADIAL = 3;
    public static final int FREEFORM = 4;
    public static final int LATTICEFORM = 5;
    public static final int COONS = 6;
    public static final int TENSOR = 7;

    private static final boolean debug = false;

    protected PDFFunction[] function;

    /**
     * colorspace to use for shading
     */
    protected GenericColorSpace shadingColorSpace;

    private PdfObject Shading;

    protected float[] coords;

    /**
     * type used - see values in ShadingFactory
     */
    protected int shadingType;

    protected float[] domain = {0.0f, 1.0f};

    private int type;

    private boolean[] isExtended = new boolean[2];
    private boolean colorsReversed;
    public float scaling;
    public int cropX;

    public int textX, textY;
    public int cropH;
    private float[] background;
    public boolean isPrinting;

    float[][] matrix;

    private ArrayList<Shape67> shapesList = new ArrayList<Shape67>();

    /**
     * read general values
     */
    public ShadedPaint(final PdfObject Shading, final boolean isPrinting, final GenericColorSpace shadingColorSpace,
                       final PdfObjectReader currentPdfFile, final float[][] matrix, final boolean colorsReversed) {

        this.isPrinting = isPrinting;
        this.colorsReversed = colorsReversed;
        this.type = Shading.getInt(PdfDictionary.ShadingType);
        this.matrix = matrix;
        init(Shading, shadingColorSpace, currentPdfFile, matrix);

    }

    public ShadedPaint() {
    }

    private void init(final PdfObject Shading, final GenericColorSpace shadingColorSpace, final PdfObjectReader currentPdfFile, final float[][] matrix) {

        /*
         * read axial specific values not read in generic
         */
        final boolean[] extension = Shading.getBooleanArray(PdfDictionary.Extend);
        if (extension != null) {
            isExtended = extension;
        }

        this.shadingColorSpace = shadingColorSpace;
        this.Shading = Shading;

        shadingType = Shading.getInt(PdfDictionary.ShadingType);

        background = Shading.getFloatArray(PdfDictionary.Background);

        final PdfArrayIterator keys = Shading.getMixedArray(PdfDictionary.Function);

        final int functionCount = keys.getTokenCount();

        if (functionCount > 0) {

            final PdfObject[] subFunction = new PdfObject[functionCount];

            for (int i = 0; i < functionCount; i++) {
                final byte[] nextValue = keys.getNextValueAsByte(true);
                subFunction[i] = ColorspaceFactory.getFunctionObjectFromRefOrDirect(currentPdfFile, nextValue);
            }

            function = new PDFFunction[subFunction.length];

            // get values for sub stream Function
            for (int i1 = 0, imax = subFunction.length; i1 < imax; i1++) {
                function[i1] = FunctionFactory.getFunction(subFunction[i1], currentPdfFile);
            }
        }

        final float[] newDomain = Shading.getFloatArray(PdfDictionary.Domain);
        if (newDomain != null) {
            domain = newDomain;
        }

        final float[] Coords = Shading.getFloatArray(PdfDictionary.Coords);
        if (Coords != null) {

            final int len = Coords.length;
            coords = new float[len];
            System.arraycopy(Coords, 0, coords, 0, len);

            if (matrix != null) {

                if (debug) {
                    Matrix.show(matrix);
                }

                final float a = matrix[0][0];
                final float b = matrix[0][1];
                final float c = matrix[1][0];
                final float d = matrix[1][1];
                final float tx = matrix[2][0];
                final float ty = matrix[2][1];

                final float x;
                final float y;
                final float x1;
                final float y1;

                if (type == AXIAL) { //axial
                    x = coords[0];
                    y = coords[1];
                    x1 = coords[2];
                    y1 = coords[3];
                    coords[0] = (a * x) + (c * y) + tx;
                    coords[1] = (b * x) + (d * y) + ty;
                    coords[2] = (a * x1) + (c * y1) + tx;
                    coords[3] = (b * x1) + (d * y1) + ty;

                    if (debug) {
                        System.out.println(coords[0] + " " + coords[1]);
                        System.out.println(coords[2] + " " + coords[3]);
                        System.out.println("=============================");
                    }

                }
            } else if (type == AXIAL && DecoderOptions.isRunningOnMac && (coords[1] > coords[3])) {
                colorsReversed = true;
            }
        }
    }

    @Override
    public PaintContext createContext(final ColorModel cm, final Rectangle db, final Rectangle2D ub,
                                      final AffineTransform xform, final RenderingHints hints) {

        PaintContext pt = null;

        final int offX;
        final int offY;

        if (!isPrinting) {
            offX = (int) (xform.getTranslateX() + cropX - (textX * scaling));
            offY = (int) (xform.getTranslateY() - cropH + (textY * scaling));

        } else {
            offX = (int) xform.getTranslateX();
            offY = (int) xform.getTranslateY();
            scaling = (float) xform.getScaleY();
        }

        switch (type) {
            case FUNCTION:
                if (textX == 0 && textY == 0) {
                    pt = new FunctionShadeContext(xform, shadingColorSpace, background, Shading, matrix, function);
                } else {
                    pt = new FunctionContext(cropH, (float) (1f / xform.getScaleX()), shadingColorSpace, colorsReversed, function);
                }
                break;

            case AXIAL:
                if (textX == 0 && textY == 0) {
                    pt = new AxialShadeContext(xform, shadingColorSpace, background, Shading, matrix, function);
                } else {
                    pt = new AxialContext(xform, isPrinting, offX, offY, cropX, cropH, 1f / scaling, isExtended, domain, coords, shadingColorSpace, colorsReversed, background, function);
                }
                break;

            case RADIAL:
                pt = new RadialContext(xform, shadingColorSpace, background, Shading, matrix, function);
                break;

            case FREEFORM:
                if (textX == 0 && textY == 0) {
                    pt = new FreeFormShadeContext(xform, shadingColorSpace, background, Shading, matrix, function);
                } else {
                    pt = new FreeFormContext(shadingColorSpace, background, Shading, matrix, cropH, scaling, offX, offY);
                }
                break;

            case LATTICEFORM:
                if (textX == 0 && textY == 0) {
                    pt = new LatticeFormShadeContext(xform, shadingColorSpace, background, Shading, matrix, function);
                } else {
                    pt = new LatticeFormContext(shadingColorSpace, background, Shading, matrix, cropH, scaling, offX, offY);
                }
                break;

            case COONS:
                if (!shapesList.isEmpty()) {
                    pt = new CoonsContext(xform, shadingColorSpace, shapesList, background, matrix, function);
                } else {
                    final CoonsContext ct = new CoonsContext(xform, shadingColorSpace, background, Shading, matrix, function);
                    shapesList = ct.getShapes();
                    pt = ct;
                }

                break;
            case TENSOR:
                //check store setup and try using cached context

                if (!shapesList.isEmpty()) {
                    pt = new TensorContext(xform, shadingColorSpace, shapesList, background, matrix, function);
                } else {
                    final TensorContext tt = new TensorContext(xform, shadingColorSpace, background, Shading, matrix, function);
                    shapesList = tt.getShapes();
                    pt = tt;
                }

                break;

        }

        return pt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Transparency#getTransparency()
     */
    @Override
    public int getTransparency() {
        return 0;
    }

    @Override
    public void setScaling(final double cropX, final double cropH, final float scaling, final float textX, final float textY) {
        this.scaling = scaling;
        this.cropX = (int) cropX;
        this.cropH = (int) cropH;
        this.textX = (int) textX;
        this.textY = (int) textY;
    }

    @Override
    public boolean isPattern() {
        return true;
    }

    @Override
    public int getRGB() {
        return 0;
    }

    @Override
    public boolean isTexture() {
        return false;
    }

}

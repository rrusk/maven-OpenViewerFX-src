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
 * SMaskComposite.java
 * ---------------
 */
package com.idrsolutions.pdf.color.blends;

import java.awt.CompositeContext;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * @author suda
 */
public class SMaskComposite implements CompositeContext {

    private final float fixedAlpha;
    private final ColorModel srcModel;
    private final ColorModel dstModel;

    public SMaskComposite(final ColorModel srcColorModel, final ColorModel dstColorModel, final float alpha) {
        srcModel = srcColorModel;
        dstModel = dstColorModel;
        fixedAlpha = alpha;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void compose(final Raster src, final Raster dstIn, final WritableRaster dstOut) {

        final int snComp = srcModel.getNumComponents();
        final int bnComp = dstModel.getNumComponents();
        final int bnColors = dstModel.getNumColorComponents();

        final int width = Math.min(Math.min(src.getWidth(), dstIn.getWidth()), dstOut.getWidth());
        final int height = Math.min(Math.min(src.getHeight(), dstIn.getHeight()), dstOut.getHeight());

        float[] sColors = new float[snComp]; //src colors
        float[] bColors = new float[bnComp]; //backdrop colors

        final boolean hasAlphaB = dstModel.hasAlpha();

        Object srcPixel = null, dstPixel = null;

        float aB, aR, cB, qS, qM;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                dstPixel = dstIn.getDataElements(x, y, dstPixel);
                bColors = dstModel.getNormalizedComponents(dstPixel, bColors, 0);

                srcPixel = src.getDataElements(x, y, srcPixel);
                sColors = srcModel.getNormalizedComponents(srcPixel, sColors, 0);

                qM = sColors[0];
                qS = qM * fixedAlpha;

                aB = hasAlphaB ? bColors[bnColors] : 1f;
                aR = 0;

                if (aB != 0) {
                    aR = aB + qS - (aB * qS);
                    for (int i = 0; i < bnColors; i++) {
                        cB = bColors[i];
                        bColors[i] = cB + qS - (qS * cB);
                    }
                }

                if (hasAlphaB) {
                    bColors[bnColors] = aR;
                }

                dstPixel = dstModel.getDataElements(bColors, 0, dstPixel);
                dstOut.setDataElements(x, y, dstPixel);

            }
        }
    }

}

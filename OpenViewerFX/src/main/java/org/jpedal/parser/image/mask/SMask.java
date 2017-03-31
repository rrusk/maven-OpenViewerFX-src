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
 * SMask.java
 * ---------------
 */
package org.jpedal.parser.image.mask;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.jpedal.io.ColorSpaceConvertor;

/**
 *
 */
public class SMask {

    public static BufferedImage applyLuminosityMask(BufferedImage image, BufferedImage smask, final int[] tr, final boolean hasBC, final int bc) {

        if (smask == null) {
            return image;
        }

        if (smask.getType() != BufferedImage.TYPE_INT_ARGB) {
            smask = ColorSpaceConvertor.convertToARGB(smask);
        }
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            image = ColorSpaceConvertor.convertToARGB(image);
        }

        final int iw = image.getWidth();
        final int ih = image.getHeight();
        final int imageDim = iw * ih;

        final int sw = smask.getWidth();
        final int sh = smask.getHeight();
        final int smaskDim = sw * sh;

        if (imageDim < smaskDim) {
            image = scaleImage(image, sw, sh, BufferedImage.TYPE_INT_ARGB);
        } else if (smaskDim < imageDim) {
            smask = scaleImage(smask, iw, ih, BufferedImage.TYPE_INT_ARGB);
        }

        final int[] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        final int[] maskPixels = ((DataBufferInt) smask.getRaster().getDataBuffer()).getData();

        int ip, mp, r, g, b, resA, y, a;
        final int r0 = (bc >> 16) & 0xff;
        final int g0 = (bc >> 8) & 0xff;
        final int b0 = bc & 0xff;
        for (int i = 0; i < imagePixels.length; i++) {
            mp = maskPixels[i];
            a = mp >>> 24;
            r = (mp >> 16) & 0xff;
            g = (mp >> 8) & 0xff;
            b = mp & 0xff;
            if (hasBC) {
                if (a == 0) {
                    r = r0;
                    g = g0;
                    b = b0;
                } else if (a < 255) {
                    final int a_ = 255 - a;
                    r = (r * a + r0 * a_) >> 8;
                    g = (g * a + g0 * a_) >> 8;
                    b = (b * a + b0 * a_) >> 8;
                }
            }

            y = (r * 77) + (g * 152) + (b * 28);
            ip = imagePixels[i];
            resA = (ip >> 24) & 0xff;
            resA = tr != null ? (resA * tr[y >> 8]) >> 8 : (resA * y) >> 16;
            imagePixels[i] = (resA << 24) | (ip & 0xffffff);
        }

        return image;
    }


    public static BufferedImage applyAlphaMask(BufferedImage image, BufferedImage smask) {

        if (smask == null) {
            return image;
        }

        if (smask.getType() != BufferedImage.TYPE_INT_ARGB) {
            smask = ColorSpaceConvertor.convertToARGB(smask);
        }
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            image = ColorSpaceConvertor.convertToARGB(image);
        }

        final int iw = image.getWidth();
        final int ih = image.getHeight();
        final int imageDim = iw * ih;

        final int sw = smask.getWidth();
        final int sh = smask.getHeight();
        final int smaskDim = sw * sh;

        if (imageDim < smaskDim) {
            image = scaleImage(image, sw, sh, BufferedImage.TYPE_INT_ARGB);
        } else if (smaskDim < imageDim) {
            smask = scaleImage(smask, iw, ih, BufferedImage.TYPE_INT_ARGB);
        }

        final int[] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        final int[] maskPixels = ((DataBufferInt) smask.getRaster().getDataBuffer()).getData();

        int ip, mp, ia, ma, a;
        final float sc = 1 / 255f;
        for (int i = 0; i < imagePixels.length; i++) {
            mp = maskPixels[i];
            ip = imagePixels[i];
            ia = (ip >> 24) & 0xff;
            ma = (mp >> 24) & 0xff;
            a = (int) (ia * ma * sc);
            imagePixels[i] = (a << 24) | (ip & 0xffffff);
        }
        return image;
    }

    private static BufferedImage scaleImage(final BufferedImage src, final int w, final int h, final int imageType) {
        final BufferedImage dimg = new BufferedImage(w, h, imageType);
        final Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }

}

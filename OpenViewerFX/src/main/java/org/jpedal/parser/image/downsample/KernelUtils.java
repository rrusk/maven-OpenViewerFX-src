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
 * KernelUtils.java
 * ---------------
 */
package org.jpedal.parser.image.downsample;

import org.jpedal.images.SamplingFactory;
import org.jpedal.parser.image.data.ImageData;

/**
 * @author Bethan
 */
class KernelUtils {

    protected static ImageData applyKernel(final ImageData imageData) {

        final double[][] kernel = SamplingFactory.getSharpenKernel();

        final byte[] input = imageData.getObjectData();
        final int w = imageData.getWidth();
        final int h = imageData.getHeight();
        int compCount = imageData.getCompCount();

        if (compCount == 3) { //really it's rgba not rgb
            compCount = 4;
        }

        final byte[] output = new byte[w * h * compCount];

        int imageX, imageY, currentPixel;
        int value = 0;
        final int lineBytes = w * compCount;

        //get matrix size (usually 3)
        final int matrixSize = kernel.length;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int comp = 0; comp < compCount; comp++) {

                    //multiply by nxn matrix to get new pixel value
                    for (int i = 0; i < matrixSize; i++) {
                        for (int j = 0; j < matrixSize; j++) {

                            imageX = (x - matrixSize / 2 + i + w) % w;
                            imageY = (y - matrixSize / 2 + j + h) % h;

                            currentPixel = input[(imageY * lineBytes) + (imageX * compCount) + comp] & 255;
                            value += (currentPixel * kernel[i][j]);
                        }
                    }

                    if (value < 0) { //ensure in range
                        value = 0;
                    } else if (value > 255) {
                        value = 255;
                    }

                    output[(y * lineBytes) + (x * compCount) + comp] = (byte) value;

                    value = 0; //reset for next calculation
                }
            }
        }

        imageData.setObjectData(output);

        return imageData;

    }

}

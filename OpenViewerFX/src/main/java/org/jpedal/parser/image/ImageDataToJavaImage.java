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
 * ImageDataToJavaImage.java
 * ---------------
 */
package org.jpedal.parser.image;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Kernel;
import java.awt.image.Raster;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.parser.image.mask.MaskDecoder;
import org.jpedal.parser.image.utils.ConvertImageToShape;
import org.jpedal.parser.image.utils.ConvertMaskToShape;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;

/**
 * @author markee
 */
public class ImageDataToJavaImage {

    private static byte[] correctDataArraySize(final int d, final int w, final int h, byte[] data) {
        if (d == 1) {
            final int requiredSize = ((w + 7) >> 3) * h;
            final int oldSize = data.length;
            if (oldSize < requiredSize) {
                final byte[] oldData = data;
                data = new byte[requiredSize];
                System.arraycopy(oldData, 0, data, 0, oldSize);

                //and fill rest with 255 for white
                for (int aa = oldSize; aa < requiredSize; aa++) {
                    data[aa] = (byte) 255;
                }
            }

        } else if (d == 8) {
            final int requiredSize = w * h;
            final int oldSize = data.length;
            if (oldSize < requiredSize) {
                final byte[] oldData = data;
                data = new byte[requiredSize];
                System.arraycopy(oldData, 0, data, 0, oldSize);
            }
        }
        return data;
    }

    /**
     * turn raw data into a BufferedImage
     */
    public static BufferedImage makeImage(final GenericColorSpace decodeColorData, final ImageData imageData) {

        final int comp = imageData.getCompCount();

        final int w = imageData.getWidth();
        final int h = imageData.getHeight();
        final int d = imageData.getDepth();
        byte[] data = imageData.getObjectData();
        final int ID = decodeColorData.getID();

        boolean isConverted = imageData.isConvertedToARGB();
        final byte[] index = decodeColorData.getIndexedMap();

        //ensure correct size
        if (ID == ColorSpaces.DeviceGray) {
            data = correctDataArraySize(d, w, h, data);
        }

        BufferedImage image = null;

        if (isConverted) {
            final DataBuffer db = new DataBufferByte(data, data.length);

            final int[] bands = {0, 1, 2, 3};
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            final Raster raster = Raster.createInterleavedRaster(db, w, h, w * 4, 4, bands, null);
            image.setData(raster);

        } else if (index != null) {
            image = IndexedImage.make(w, h, decodeColorData, index, d, data);
        } else if (d == 1) {
            image = BinaryImage.make(w, h, data, decodeColorData, d);

        } else if (ID == ColorSpaces.Separation || ID == ColorSpaces.DeviceN || ID == ColorSpaces.Lab) {
            LogWriter.writeLog("Converting Separation/DeviceN colorspace to sRGB ");

            image = decodeColorData.dataToRGB(data, w, h);

        } else {

            switch (comp) {
                case 4:  //handle CMYK or ICC or ARGB
                    if (decodeColorData.getID() == ColorSpaces.DeviceRGB) {
                        image = ColorSpaceConvertor.createARGBImage(w, h, data);
                    } else {
                        image = ColorSpaceConvertor.convertFromICCCMYK(w, h, data);
                    }
                    break;

                case 3:
                    image = ThreeComponentImage.make(d, data, index, w, h);
                    break;

                case 1:
                    image = OneBitImage.make(d, w, h, data);
                    break;
            }
        }

        return image;
    }

    public static BufferedImage makeMaskImage(final ParserOptions parserOptions, final GraphicsState gs, final DynamicVectorRenderer current, final ImageData imageData, final GenericColorSpace decodeColorData, final byte[] maskCol) {

        final int w = imageData.getWidth();
        final int h = imageData.getHeight();
        final int d = imageData.getDepth();
        final byte[] data = imageData.getObjectData();

        BufferedImage image = null;
        /*
         * allow for 1 x 1 pixels scaled up or fine lines
         */
        final float ratio = h / (float) w;
        if ((parserOptions.isPrinting() && ratio < 0.1f && w > 4000 && h > 1) || (ratio < 0.001f && w > 4000 && h > 1) || (w == 1 && h == 1)) { // && data[0]!=0){

            ConvertMaskToShape.convert(gs, current, parserOptions);
            imageData.setRemoved(true);
        } else if (h == 2 && d == 1 && ImageCommands.isRepeatingLine(data, h)) {
            ConvertImageToShape.convert(data, h, gs, current, parserOptions);

            imageData.setRemoved(true);
        } else {
            image = MaskDecoder.createMaskImage((parserOptions.isPrinting() && !ImageDecoder.allowPrintTransparency), parserOptions.isType3Font(), data, w, h, imageData, d, decodeColorData, maskCol);
        }
        return image;
    }

    //lash up to 
    public static BufferedImage sharpen(BufferedImage image) {
        // A 3x3 kernel that sharpens an image
        Kernel kernel = new Kernel(3, 3,
                new float[]{
                    -1, -1, -1,
                    -1, 9, -1,
                    -1, -1, -1});

        float[] sharpKernel = {
            0.0f, -1.0f, 0.0f,
            -1.0f, 5.0f, -1.0f,
            0.0f, -1.0f, 0.0f
        };

        BufferedImageOp sharpen = new ConvolveOp(new Kernel(3, 3, sharpKernel), ConvolveOp.EDGE_NO_OP, null);

        BufferedImageOp op = new ConvolveOp(kernel);

        // image = op.filter(image, null);
        image = sharpen.filter(image, null);

        return image;
    }
}

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
 * TempStoreImage.java
 * ---------------
 */
package org.jpedal.io.security;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

/**
 *
 * @author suda
 */
public class TempStoreImage {

    public static byte[] getBytes(BufferedImage img) {
        byte[] data = new byte[img.getWidth() * img.getHeight() * 4 + 8];
        byte[] w = numToBytes(img.getWidth());
        System.arraycopy(w, 0, data, 0, 4);
        byte[] h = numToBytes(img.getHeight());
        System.arraycopy(h, 0, data, 4, 4);
        int p = 8;
        int pp = 0;

        int[] pixels;
        byte[] pixBytes;
        int v;
        
        int xx = img.getRaster().getSampleModelTranslateX();
        int yy = img.getRaster().getSampleModelTranslateY();
        if(xx != 0 || yy!= 0){
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    v = img.getRGB(x, y);
                    data[p++] = (byte) ((v >> 24) & 0xff);
                    data[p++] = (byte) ((v >> 16) & 0xff);
                    data[p++] = (byte) ((v >> 8) & 0xff);
                    data[p++] = (byte) (v & 0xff);                    
                }
            }
            return data;
        }

        switch (img.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                for (int i = 0, ii = img.getWidth() * img.getHeight(); i < ii; i++) {
                    v = pixels[i];
                    data[p++] = -1;
                    data[p++] = (byte) ((v >> 16) & 0xff);
                    data[p++] = (byte) ((v >> 8) & 0xff);
                    data[p++] = (byte) (v & 0xff);
                }

                break;
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
                pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                for (int i = 0, ii = img.getWidth() * img.getHeight(); i < ii; i++) {
                    v = pixels[i];
                    data[p++] = (byte) ((v >> 24) & 0xff);
                    data[p++] = (byte) ((v >> 16) & 0xff);
                    data[p++] = (byte) ((v >> 8) & 0xff);
                    data[p++] = (byte) (v & 0xff);
                }
                break;
            case BufferedImage.TYPE_INT_BGR:
                pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                for (int i = 0, ii = img.getWidth() * img.getHeight(); i < ii; i++) {
                    v = pixels[i];
                    data[p++] = -1;
                    data[p++] = (byte) (v & 0xff);
                    data[p++] = (byte) ((v >> 8) & 0xff);
                    data[p++] = (byte) ((v >> 16) & 0xff);
                }
                break;
            case BufferedImage.TYPE_3BYTE_BGR:
                pixBytes = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                for (int i = 0, ii = img.getWidth() * img.getHeight(); i < ii; i++) {
                    data[p++] = -1;
                    data[p++] = pixBytes[pp+2];
                    data[p++] = pixBytes[pp+1];
                    data[p++] = pixBytes[pp];
                    pp += 3;
                }
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                pixBytes = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                for (int i = 0, ii = img.getWidth() * img.getHeight(); i < ii; i++) {
                    data[p++] = pixBytes[pp];
                    data[p++] = pixBytes[pp+3];
                    data[p++] = pixBytes[pp+2];
                    data[p++] = pixBytes[pp+1];
                    pp += 4;
                }
                break;
            default:
                BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                img2.getGraphics().drawImage(img, 0, 0, null);
                pixels = ((DataBufferInt) img2.getRaster().getDataBuffer()).getData();
                for (int i = 0, ii = img.getWidth() * img.getHeight(); i < ii; i++) {
                    v = pixels[i];
                    data[p++] = (byte) ((v >> 24) & 0xff);
                    data[p++] = (byte) ((v >> 16) & 0xff);
                    data[p++] = (byte) ((v >> 8) & 0xff);
                    data[p++] = (byte) (v & 0xff);
                }
        }
        return data;

    }

    public static BufferedImage getImage(byte[] data) {
        int w = ((data[0] & 0xff) << 24) | ((data[1] & 0xff) << 16) | ((data[2] & 0xff) << 8) | (data[3] & 0xff);
        int h = ((data[4] & 0xff) << 24) | ((data[5] & 0xff) << 16) | ((data[6] & 0xff) << 8) | (data[7] & 0xff);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int p = 0;
        int pp = 8;
        for (int i = 0, ii = w * h; i < ii; i++) {
            pixels[p++] = (data[pp++] & 0xff) << 24 | (data[pp++] & 0xff) << 16 | (data[pp++] & 0xff) << 8 | (data[pp++] & 0xff);
        }
        return img;
    }

    private static byte[] numToBytes(int num) {
        return new byte[]{(byte) (num >> 24), (byte) ((num >> 16) & 0xff), (byte) ((num >> 8) & 0xff), (byte) (num & 0xff)};
    }
    
}

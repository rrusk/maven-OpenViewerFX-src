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
 * FastColorSpaceCMYK.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.color.ColorSpace;
import org.jpedal.JDeliHelper;

public class FastColorSpaceCMYK extends ColorSpace {

    public FastColorSpaceCMYK() {
        super(ColorSpace.TYPE_CMYK, 4);
    }

    @Override
    public float[] toRGB(final float[] cv) {
        final int c = (int) (cv[0] * 255);
        final int m = (int) (cv[1] * 255);
        final int y = (int) (cv[2] * 255);
        final int k = (int) (cv[3] * 255);
        final int[] rgb = JDeliHelper.convertCMYKtoRGB(c, m, y, k);
        if(rgb == null){
            final float[] out = new float[4];
            out[0] = (1 - cv[0]) * (1 - cv[3]);
            out[1] = (1 - cv[1]) * (1 - cv[3]);
            out[2] = (1 - cv[2]) * (1 - cv[3]);
            return out;
        }
        return new float[]{rgb[0]/255f,rgb[1]/255f,rgb[2]/255f};
    }

    @Override
    public float[] fromRGB(final float[] rgbvalue) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public float[] toCIEXYZ(final float[] colorvalue) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public float[] fromCIEXYZ(final float[] colorvalue) {
        throw new UnsupportedOperationException("Not supported yet."); 

    }

}

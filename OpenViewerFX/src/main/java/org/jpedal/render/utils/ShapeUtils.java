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
 * ShapeUtils.java
 * ---------------
 */
package org.jpedal.render.utils;


import java.awt.Shape;
import java.awt.geom.PathIterator;

/**
 * static helper methods for Clip code
 */
public class ShapeUtils {

    
    public static boolean isSimpleOutline(final Shape path) {

        int count = 0;
        final PathIterator i = path.getPathIterator(null);
        final float[] values = new float[6];

        while (!i.isDone() && count < 6) { //see if rectangle or complex clip
            //Get value before next called otherwise issues with pathIterator ending breaks everything
            final int value = i.currentSegment(values);

            i.next();

            count++;

            //If there is a curve, class as complex outline
            if(value==PathIterator.SEG_CUBICTO || value==PathIterator.SEG_QUADTO){
                count = 6;
            }


        }
        return count<6;
    }

}

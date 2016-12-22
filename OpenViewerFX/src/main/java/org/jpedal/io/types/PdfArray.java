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
 * PdfArray.java
 * ---------------
 */
package org.jpedal.io.types;

public class PdfArray {
	

	/**
	 * @param value
	 * @return
	 */
	public static float[] convertToFloatArray(final String value) {

        final char[] bts=value.toCharArray();

        final float[] f;
        int fPointer =0,wStart;

        char[] buffer;

        final int len=bts.length;
        int ptr=0;

        //create array which must fit all items
        f=new float[len];

        //find the first [
        while(ptr<len && bts[ptr]!='[') {
            ptr++;
        }

        // in case first '[' is missing start at the begging of bts
        if(ptr==bts.length) {
            ptr = 0;
        } else {
            ptr++;
        }

        while(ptr<len){

            //ignore any spaces
            while(ptr<len && bts[ptr]==' ') {
                ptr++;
            }

            //set start and find next space,] or end
            wStart=ptr;

            while(ptr<len && bts[ptr]!=' ' && bts[ptr]!=']') {
                ptr++;
            }

            final int valLen=ptr-wStart;

            //ensure exit or convert to float
            if(valLen<1) {
                ptr = len;
            } else{
                //log value and repeat above until end
                buffer = new char[valLen];

                for(int t=0;wStart<ptr;wStart++){
                    buffer[t] = bts[wStart];
                    t++;
                }

                f[fPointer] = ConvertToFloat.convert(buffer);
                fPointer++;
            }
        }

        final float[] toBeReturned = new float[fPointer];

        System.arraycopy(f, 0, toBeReturned, 0, fPointer);

        return toBeReturned;
    }
}

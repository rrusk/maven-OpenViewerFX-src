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
 * CS.java
 * ---------------
 */
package org.jpedal.parser.color;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.ColorspaceFactory;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.parser.PdfObjectCache;

/**
 *
 */
public class CS {
   
    public static void execute(final boolean isLowerCase, final String colorspaceObject, final GraphicsState gs, final PdfObjectCache cache, final PdfObjectReader currentPdfFile, final boolean isPrinting) {

        //set flag for stroke
        final boolean isStroke = !isLowerCase;

        Object rawDict=cache.get(PdfObjectCache.Colorspaces,colorspaceObject);

        PdfArrayIterator array=ColorspaceFactory.convertColValueToMixedArray(currentPdfFile,(byte[])rawDict);

        final GenericColorSpace newColorSpace= ColorspaceFactory.getColorSpaceInstance(currentPdfFile, array);

        newColorSpace.setPrinting(isPrinting);

        //pass in pattern arrays containing all values
        if(newColorSpace.getID()==ColorSpaces.Pattern){

            //at this point we only know it is Pattern so need to pass in WHOLE array
            newColorSpace.setPattern(cache.getPatterns());
            newColorSpace.setGS(gs);
        }

        //track colorspace use
        cache.put(PdfObjectCache.ColorspacesUsed, newColorSpace.getID(),"x");

        if(isStroke) {
            gs.strokeColorSpace = newColorSpace;
        } else {
            gs.nonstrokeColorSpace = newColorSpace;
        }
    }
}



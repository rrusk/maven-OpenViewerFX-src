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
 * JPedalBorderFactory.java
 * ---------------
 */
package org.jpedal.objects.acroforms.creation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

@SuppressWarnings("MagicConstant")
public class JPedalBorderFactory {

    private static final boolean printouts = false;
    //private final static boolean debugUnimplemented = false;

    /**
     * setup the border style
     */
    public static Border createBorderStyle(final PdfObject BS, final Color borderColor, final Color borderBackgroundColor, final float scaling) {
        /*Type must be Border
         * W width in points (if 0 no border, default =1)
         * S style - (default =S)
         * 	S=solid, D=dashed (pattern specified by D entry below), B=beveled(embossed appears to above page),
         * 	I=inset(engraved appeared to be below page), U=underline ( single line at bottom of boundingbox)
         * D array phase - e.g. [a b] c means:-  a=on blocks,b=off blocks(if not present default to a),
         * 		c=start of off block preseded index is on block.
         * 	i.e. [4] 6 :- 4blocks on 4blocks off, block[6] if off - 1=off 2=on 3=on 4=on 5=on 6=off 7=off 8=off 9=off etc...
         *
         */

        if (borderBackgroundColor == null && printouts) {
//		    borderBackgroundColor = new Color(0,0,0,0);
            System.out.println("background border color null");
        }

        if (borderColor == null) {
//		    borderColor = new Color(0,0,0,0); //transparent
            if (printouts) {
                System.out.println("border color null");
            }
            return null;
        }

        Border insideBorder = null;

        //set border width or default of 1 if no value
        int w = -1;
        if (BS != null) {
            w = BS.getInt(PdfDictionary.W);
        }
        if (w < 0) {
            w = 1;
        }

        final float ws = w * scaling;
        w = (int) (ws + 0.5);

        int style = PdfDictionary.S;

        if (BS != null) {
            style = BS.getNameAsConstant(PdfDictionary.S);
            if (style == PdfDictionary.Unknown) {
                style = PdfDictionary.S;
            }

            final int xfaBorderStyle = BS.getNameAsConstant(PdfDictionary.X);
            if (xfaBorderStyle != -1) {
                //if we have an extra border, inside and outside,
                //then half the border size so that we keep within our allowed space.
                w /= 2;
            }
        }

        switch (style) {
            case PdfDictionary.U:
                insideBorder = BorderFactory.createMatteBorder(0, 0, w, 0, borderColor); //underline field
                break;

            case PdfDictionary.I:
                insideBorder = BorderFactory.createEtchedBorder(borderColor, borderBackgroundColor); //inset below page
                break;

            case PdfDictionary.B:
                insideBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED, borderColor, borderBackgroundColor); //beveled above page
                break;

            case PdfDictionary.S:
                insideBorder = BorderFactory.createLineBorder(borderColor, w); //solid
                break;

            case PdfDictionary.D:

                final PdfArrayIterator dashPattern = BS.getMixedArray(PdfDictionary.D);

                int current_line_dash_phase = 0;
                float[] current_line_dash_array = new float[1];
                final int count = dashPattern.getTokenCount();

                if (count > 0) {
                    current_line_dash_array = dashPattern.getNextValueAsFloatArray();
                }

                if (count > 1) {
                    current_line_dash_phase = dashPattern.getNextValueAsInteger();
                }

                if (w < 0) {
                    w = 1;
                }

                final Stroke current_stroke = new BasicStroke(w, 0, 0, 1, current_line_dash_array, current_line_dash_phase);

                insideBorder = new DashBorder(current_stroke, borderColor);
                break;

        }
        
        /* if an X in the BS then thats out XFABorder-
         * R - right handed
         * L - left handed
         * E - even
         */
        //preperation for new XFA adding 2 borders
        Border outsideBorder = new EmptyBorder(0, 0, 0, 0); //default

        if (BS != null) {
            final int xfaBorderStyle = BS.getNameAsConstant(PdfDictionary.X);
            if (xfaBorderStyle != -1) {
                final Border xfaBorder = BorderFactory.createLineBorder(borderColor, w); //solid
                switch (xfaBorderStyle) {
                    case PdfDictionary.R:
                        outsideBorder = xfaBorder; //correct on compare
                        break;

//	        	case PdfDictionary.L:
//	        		outsideBorder = insideBorder; //to check
//	        		insideBorder = xfaBorder;
//	        		break;
//	        	case PdfDictionary.E:
//	        		//??even may need rodoing, as it may not appear ontop of.
//	        		insideBorder = xfaBorder;
//	        		break;
                }
            }
        }

        return new CompoundBorder(outsideBorder, insideBorder);
    }

}




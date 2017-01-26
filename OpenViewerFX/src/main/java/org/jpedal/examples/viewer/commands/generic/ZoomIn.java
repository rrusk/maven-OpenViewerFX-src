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
 * ZoomIn.java
 * ---------------
 */

package org.jpedal.examples.viewer.commands.generic;

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.gui.GUIFactory;

/**
 * Takes an Image Snapshot of the Selected Area
 */
public class ZoomIn {
    
    private static final int[] scalingValues = {25, 50, 75, 100, 125, 150, 200, 250, 500, 750, 1000};
    
    public static boolean execute(final Object[] args, final GUIFactory currentGUI, final PdfDecoderInt decode_pdf) {
        
        if (args == null) {
            float scaling = 100 * currentGUI.getScaling();
            scaling = (int)(decode_pdf.getDPIFactory().removeScaling(scaling)+0.5f);
            
            if (scaling < scalingValues[0]) {
                ((GUI)currentGUI).setSelectedComboItem(Commands.SCALING, String.valueOf(scalingValues[0]));
            } else {
                int scalingToUse = -1;
                for (int i = 0; i != scalingValues.length - 1; i++) {
                    if (scaling >= scalingValues[i] && scaling < scalingValues[i + 1]) {
                        scalingToUse = i + 1;
                        break;
                    }
                }
                if (scalingToUse != -1) {
                    ((GUI)currentGUI).setSelectedComboItem(Commands.SCALING, String.valueOf(scalingValues[scalingToUse]));
                }
            }
        }

        return false;
    }
}

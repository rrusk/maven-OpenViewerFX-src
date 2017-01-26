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
 * BlendMode.java
 * ---------------
 */
package com.idrsolutions.pdf.color.blends;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import org.jpedal.objects.raw.PdfDictionary;

/**
 *
 */
public class BlendMode implements Composite {
    
    final int blendMode;
    
    final float alpha;
    
    public BlendMode(final int blendMode, final float alpha) {
        this.blendMode = blendMode;
        this.alpha = alpha;
    }
    
    @Override
    public CompositeContext createContext(final ColorModel srcColorModel, final ColorModel dstColorModel, final RenderingHints hints) {

//        return ContextFactory.getBlendContext(blendMode,alpha);
        switch (blendMode) {
            case PdfDictionary.SMask:
                return new SMaskComposite(srcColorModel, dstColorModel, alpha);
            case PdfDictionary.Hue:
            case PdfDictionary.Saturation:
            case PdfDictionary.Color:
            case PdfDictionary.Luminosity:
                return new BlendContext(blendMode);
            default:
//               return new BlendContext(blendMode); 
                return new BlendComposite(srcColorModel, dstColorModel, blendMode, alpha);
        }        
    }
    
}

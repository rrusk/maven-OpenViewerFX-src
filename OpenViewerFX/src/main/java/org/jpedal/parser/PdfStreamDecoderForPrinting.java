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
 * PdfStreamDecoderForPrinting.java
 * ---------------
 */

package org.jpedal.parser;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import org.jpedal.PdfDecoderInt;
import org.jpedal.external.CustomPrintHintingHandler;
import org.jpedal.external.ErrorTracker;
import org.jpedal.external.Options;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.SwingDisplay;

public class PdfStreamDecoderForPrinting extends PdfStreamDecoder implements PrintStreamDecoder  {

    public PdfStreamDecoderForPrinting(final PdfObjectReader currentPdfFile, final PdfLayerList layers) {
        super(currentPdfFile, layers);

        isPrinting = true;
    }

    @Override
    public void print(final Graphics2D g2, final AffineTransform scaling, final int currentPrintPage,
                      final Rectangle userAnnot, final CustomPrintHintingHandler customPrintHintingHandler, final PdfDecoderInt pdf){

        final SwingDisplay swingDisplay=(SwingDisplay) current;
        
        if(customPrintHintingHandler!=null){
            swingDisplay.stopG2HintSetting(true);
            customPrintHintingHandler.preprint(g2,pdf);
        }

        swingDisplay.setPrintPage(currentPrintPage);

        current.writeCustom(DynamicVectorRenderer.CUSTOM_COLOR_HANDLER, pdf.getExternalHandler(Options.ColorHandler));

        current.setG2(g2);
        current.paint(null,scaling,userAnnot);
    }

    @Override
    public void setObjectValue(final int key, final Object  obj){

        if(key==ValueTypes.ObjectStore){
            objectStoreStreamRef = (ObjectStore)obj;

            current=new SwingDisplay(parserOptions.getPageNumber(),objectStoreStreamRef,true);
            
            if(customImageHandler!=null && current!=null) {
                current.writeCustom(DynamicVectorRenderer.CUSTOM_IMAGE_HANDLER, customImageHandler);
            }
        }else{
            super.setObjectValue(key,obj);
        }

    }

    @Override
    public ErrorTracker getErrorTracker() {
        return errorTracker;
    }
}

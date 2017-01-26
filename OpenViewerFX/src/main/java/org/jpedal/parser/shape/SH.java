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
 * SH.java
 * ---------------
 */
package org.jpedal.parser.shape;

import com.idrsolutions.pdf.color.shading.ShadedPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import org.jpedal.color.ColorspaceFactory;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PdfPaint;
import org.jpedal.io.PdfObjectFactory;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.SwingShape;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.ShadingObject;
import org.jpedal.parser.Cmd;
import org.jpedal.parser.PdfObjectCache;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;

public class SH {

    public static void execute(final String shadingObject, final PdfObjectCache cache, final GraphicsState gs,
                               final boolean isPrinting, final int pageNum,
                               final PdfObjectReader currentPdfFile,
                               final PdfPageData pageData, final DynamicVectorRenderer current) {
        byte[] shadingData= (byte[]) cache.get(PdfObjectCache.LocalShadings, shadingObject);
        if(shadingData==null){
            shadingData= (byte[]) cache.get(PdfObjectCache.GlobalShadings, shadingObject);
        }
        
        PdfObject Shading=PdfObjectFactory.getPDFObjectObjectFromRefOrDirect(new ShadingObject("1 0 R"), currentPdfFile.getObjectReader(),shadingData, PdfDictionary.Shading);
        
        //workout shape
        Shape shadeShape=null;

        if(shadeShape==null) {
            shadeShape = gs.getClippingShape();
        }

        if (shadeShape == null) {
            int mh = pageData.getMediaBoxHeight(pageNum);
            mh = mh == 0 ? 1024 : mh; //this is a hack page height should not be zero
            int mw = pageData.getMediaBoxWidth(pageNum);
            mw = mw == 0 ? 1024 : mw; //this is a hack page width should not be zero            
            shadeShape = new Rectangle(pageData.getMediaBoxX(pageNum), pageData.getMediaBoxY(pageNum), mw, mh);
            //            shadeShape = new Rectangle(pageData.getMediaBoxX(pageNum), pageData.getMediaBoxY(pageNum), pageData.getMediaBoxWidth(pageNum), pageData.getMediaBoxHeight(pageNum));
        }

        
        if (current.isHTMLorSVG() && cache.groupObj==null) {
            current.eliminateHiddenText(shadeShape, gs, 7, true);
        }

        /*
         * generate the appropriate shading and then colour in the current clip with it
         */
        try{
            
            final PdfArrayIterator ColorSpace=Shading.getMixedArray(PdfDictionary.ColorSpace);

            final GenericColorSpace newColorSpace= ColorspaceFactory.getColorSpaceInstance(currentPdfFile, ColorSpace);
            
            newColorSpace.setPrinting(isPrinting);
            
            final PdfPaint shading=new ShadedPaint(Shading, isPrinting,newColorSpace, currentPdfFile,gs.CTM,false);
            
            /*
             * shade the current clip
             */
            gs.setFillType(GraphicsState.FILL);
            gs.setNonstrokeColor(shading);

            //track colorspace use
            cache.put(PdfObjectCache.ColorspacesUsed, newColorSpace.getID(), "x");

            current.drawShape(new SwingShape(shadeShape), gs, Cmd.F);

        }catch(final Exception e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
    }
}

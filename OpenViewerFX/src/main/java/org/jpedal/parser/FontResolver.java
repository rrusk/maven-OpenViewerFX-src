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
 * FontResolver.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.exception.PdfException;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.ObjectDecoder;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.FontObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.output.HTMLFontHandler;
import org.jpedal.utils.LogWriter;

public class FontResolver {
    /**
     * decode or get font
     * @param pdfStreamDecoder
     * @param fontID
     * @param pdfFontFactory
     */
    public static PdfFont resolveFont(final GraphicsState gs, final PdfStreamDecoder pdfStreamDecoder, String fontID, final PdfFontFactory pdfFontFactory, final PdfObjectCache cache) {

        PdfFont restoredFont;

        String fontKey=fontID;
        if(gs!=null && cache.resolvedFonts.get("t-"+fontID)!=null){ //see if cached type3 font and use colour if so
             fontKey = fontID + ':' + gs.nonstrokeColorSpace.getColor().getRGB();
        }
        restoredFont=cache.resolvedFonts.get(fontKey);
        
        //check it was decoded
        if(restoredFont==null){

            PdfObject newFont=null;
            final byte[] newFontData= cache.unresolvedFonts.get(fontID);
            if(newFontData==null){ //once decoded remove from this list of stub font objects 
                cache.directFonts.remove(fontID);
            }else{
                newFont=getFontObjectFromRefOrDirect(pdfStreamDecoder.currentPdfFile, newFontData);
            }
            
            /*
             * in Flatten forms, if font not in our resources, we need to create one based on name
             * Otherwise we will not switch from whatever is last being used on page (and might have custom value)
             */
            if(pdfStreamDecoder.parserOptions.isFlattenedForm() && newFont==null){

                newFont=new FontObject("0 0 R");
                
                fontID = resolveFlattenedFont(fontID, newFont);
            }

            if(newFont!=null){

                pdfStreamDecoder.currentPdfFile.checkResolved(newFont);

                try {
                    
                    final org.jpedal.render.DynamicVectorRenderer current= pdfStreamDecoder.current;
                    
                    boolean fallbackToArial=false;
                    
                    final boolean isHTML=current.isHTMLorSVG();
                    
                    /* if text as shape or image, display Arial if font not embedded*/
                    if(isHTML && !current.getBooleanValue(DynamicVectorRenderer.IsRealText)){
                        fallbackToArial=true;
                    }
                    
                    restoredFont = pdfFontFactory.createFont(fallbackToArial, newFont, fontID, pdfStreamDecoder.objectStoreStreamRef,
                            pdfStreamDecoder.parserOptions.isRenderPage(),
                            pdfStreamDecoder.errorTracker, pdfStreamDecoder.isPrinting, isHTML);

                    if (isHTML) {
                        HTMLFontHandler.processFont(restoredFont, current, newFont, pdfStreamDecoder.currentPdfFile);
                    }
                   
                } catch (final PdfException e) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
            }

            //store (we cache Type3 with colour as colour can change)
            if(restoredFont!=null && !pdfStreamDecoder.parserOptions.isFlattenedForm()) {
                pdfStreamDecoder.cache.resolvedFonts.put(fontKey, restoredFont);
                
                if(restoredFont.getFontType()==StandardFonts.TYPE3){
                    pdfStreamDecoder.cache.resolvedFonts.put("t-"+fontID, new PdfFont());
                }
            }
        }

        return restoredFont;
    }

    private static String resolveFlattenedFont(String fontID, final PdfObject newFont) {
        String name= StandardFonts.expandName(fontID.replace(",", "-"));
        //if font not present then use a replacement
        if(FontMappings.fontSubstitutionAliasTable.get(name)==null && FontMappings.fontSubstitutionTable!=null && FontMappings.fontSubstitutionTable.get(name)==null){
            final String rawName=name.toLowerCase();
            if(rawName.contains("bold")) {
                name = "Arial-Bold";
            } else if(rawName.contains("italic")) {
                name = "Arial-Italic";
            } else {
                name = "Arial";
            }
        }
        newFont.setConstant(PdfDictionary.Subtype, StandardFonts.TRUETYPE);
        fontID=StandardFonts.expandName(name); //turns common shortened versions used in AP (ie Helv to Helvetica)
        return fontID;
    }
    
    private static FontObject getFontObjectFromRefOrDirect(final  PdfObjectReader currentPdfFile, final byte[] data) {
        
        final FontObject obj  = new FontObject(new String(data));

        if(data[0]=='<') {
            obj.setStatus(PdfObject.UNDECODED_DIRECT);
        } else {
            obj.setStatus(PdfObject.UNDECODED_REF);
        }
        obj.setUnresolvedData(data,PdfDictionary.Font);
        
        final ObjectDecoder objectDecoder=new ObjectDecoder(currentPdfFile.getObjectReader());
        objectDecoder.checkResolved(obj);
        
        return obj;
    }
}

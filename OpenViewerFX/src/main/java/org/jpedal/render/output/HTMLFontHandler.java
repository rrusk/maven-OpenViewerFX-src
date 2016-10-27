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
 * HTMLFontHandler.java
 * ---------------
 */
package org.jpedal.render.output;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
public class HTMLFontHandler {

    public HTMLFontHandler() {
    }
    
   public static void processFont(PdfFont restoredFont, DynamicVectorRenderer current, PdfObject newFont, PdfObjectReader currentPdfFile) {

        final String fontName = restoredFont.getFontName();

        final int mode = current.getValue(DynamicVectorRenderer.FontMode);

        // flag to user that this font is not embedded so will not appear in modes like image_shapetext_selectable)
        if (!restoredFont.isFontEmbedded) {
            LogWriter.writeLog("[HTML/SVG] Font " + fontName + " is not embedded and will be replaced with Arial");
        }

        if ((mode == org.jpedal.render.output.GenericFontMapper.EMBED_ALL
                || (mode == org.jpedal.render.output.GenericFontMapper.EMBED_ALL_EXCEPT_BASE_FAMILIES
                && !StandardFonts.isStandardFont(restoredFont.getFontName(), true)
                && !fontName.contains("Arial")))) {
            handleFontInHTML(newFont, current, restoredFont, currentPdfFile);
        }

    }

    private static void handleFontInHTML(PdfObject newFont, org.jpedal.render.DynamicVectorRenderer current, PdfFont restoredFont, PdfObjectReader currentPdfFile) {
        //check for base fonts (explict Arial test for ArialMT)

        PdfObject pdfFontDescriptor = newFont.getDictionary(PdfDictionary.FontDescriptor);

        //if null check to see if it is a CIF font and get data from DescendantFonts obj
        if (pdfFontDescriptor == null) {
            final PdfObject Descendent = newFont.getDictionary(PdfDictionary.DescendantFonts);
            if (Descendent != null) {
                pdfFontDescriptor = Descendent.getDictionary(PdfDictionary.FontDescriptor);
            }
        }

        //write out any embedded font file data
        // Only write out if there's visible/invisible text on the page (IsTextSelectable)
        if (pdfFontDescriptor != null && current.getBooleanValue(DynamicVectorRenderer.IsTextSelectable)) {

            final byte[] stream;
            final PdfObject FontFile2 = pdfFontDescriptor.getDictionary(PdfDictionary.FontFile2);
            if (FontFile2 != null) { //truetype fonts
                stream = currentPdfFile.readStream(FontFile2, true, true, false, false, false, FontFile2.getCacheName(currentPdfFile.getObjectReader()));
                current.writeCustom(DynamicVectorRenderer.SAVE_EMBEDDED_FONT, new Object[]{restoredFont, stream, "ttf"});
            } else {
                final PdfObject FontFile3 = pdfFontDescriptor.getDictionary(PdfDictionary.FontFile3);
                if (FontFile3 != null) { //type1c fonts
                    restoredFont.getGlyphData().setRenderer(current);
                    stream = currentPdfFile.readStream(FontFile3, true, true, false, false, false, FontFile3.getCacheName(currentPdfFile.getObjectReader()));
                    if (stream != null && stream.length > 4 && stream[0] == 'O' && stream[1] == 'T' && stream[2] == 'T' && stream[3] == 'O') {
                        current.writeCustom(DynamicVectorRenderer.SAVE_EMBEDDED_FONT, new Object[]{restoredFont, stream, "otf"});
                    } else {
                        current.writeCustom(DynamicVectorRenderer.SAVE_EMBEDDED_FONT, new Object[]{restoredFont, stream, "cff"});
                    }
                } else {

                    final PdfObject FontFile = pdfFontDescriptor.getDictionary(PdfDictionary.FontFile);

                    if (FontFile != null) { //type1 fonts
                        restoredFont.getGlyphData().setRenderer(current);
                        stream = currentPdfFile.readStream(FontFile, true, true, false, false, false, FontFile.getCacheName(currentPdfFile.getObjectReader()));
                        current.writeCustom(DynamicVectorRenderer.SAVE_EMBEDDED_FONT, new Object[]{restoredFont, stream, "t1"});
                    }
                }
            }
        }
    }

}

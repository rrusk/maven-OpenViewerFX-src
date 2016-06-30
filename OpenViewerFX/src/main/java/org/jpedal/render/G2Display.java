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
 * G2Display.java
 * ---------------
 */
package org.jpedal.render;

import java.awt.*;
import java.awt.geom.Area;
import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.DecoderOptions;
import static org.jpedal.render.BaseDisplay.textColor;

/**
 * Generic code for GUIDisplay and ImageDisplay
 *
 * @author markee
 */
public class G2Display extends BaseDisplay implements DynamicVectorRenderer {

    final void renderText(final float x, final float y, final int type, final Area transformedGlyph2,
            final Rectangle textHighlight, PdfPaint strokePaint,
            PdfPaint textFillCol, final float strokeOpacity, final float fillOpacity) {

        final Paint currentCol = g2.getPaint();

        //type of draw operation to use
        final Composite comp = g2.getComposite();

        if ((type & GraphicsState.FILL) == GraphicsState.FILL) {

            if (textFillCol != null) {
                //If we have an alt text color, its within threshold and not an additional item, use alt color
                if (textColor != null && (itemToRender == -1 || (endItem == -1 || itemToRender <= endItem)) && checkColorThreshold(textFillCol.getRGB())) {
                    textFillCol = new PdfColor(textColor.getRed(), textColor.getGreen(), textColor.getBlue());
                }
                textFillCol.setScaling(cropX, cropH, scaling, x, y);
            }

            if (customColorHandler != null) {
                customColorHandler.setPaint(g2, textFillCol, rawPageNumber, isPrinting);
            } else if (DecoderOptions.Helper != null) {
                DecoderOptions.Helper.setPaint(g2, textFillCol, rawPageNumber, isPrinting);
            } else {
                g2.setPaint(textFillCol);
            }

            renderComposite(fillOpacity);

            if (textHighlight != null) {
                if (invertHighlight) {
                    final Color col = g2.getColor();
                    g2.setColor(new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue()));
                } else if (DecoderOptions.backgroundColor != null) {
                    g2.setColor(DecoderOptions.backgroundColor);
                }
            }

            g2.fill(transformedGlyph2);

            //reset opacity
            g2.setComposite(comp);

        }

        if ((type & GraphicsState.STROKE) == GraphicsState.STROKE) {

            if (strokePaint != null) {
                //If we have an alt text color, its within threshold and not an additional item, use alt color
                if (textColor != null && (itemToRender == -1 || (endItem == -1 || itemToRender <= endItem)) && checkColorThreshold(strokePaint.getRGB())) {
                    strokePaint = new PdfColor(textColor.getRed(), textColor.getGreen(), textColor.getBlue());
                }
                strokePaint.setScaling(cropX + x, cropH + y, scaling, x, y);
            }

            if (customColorHandler != null) {
                customColorHandler.setPaint(g2, strokePaint, rawPageNumber, isPrinting);
            } else if (DecoderOptions.Helper != null) {
                DecoderOptions.Helper.setPaint(g2, strokePaint, rawPageNumber, isPrinting);
            } else {
                g2.setPaint(strokePaint);
            }

            renderComposite(strokeOpacity);

            if (textHighlight != null) {
                if (invertHighlight) {
                    final Color col = g2.getColor();
                    g2.setColor(new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue()));
                } else if (DecoderOptions.backgroundColor != null) {
                    g2.setColor(DecoderOptions.backgroundColor);
                }
            }

            //factor in scaling
            float lineWidth = (float) (1f / g2.getTransform().getScaleX());

            if (lineWidth < 0) {
                lineWidth = -lineWidth;
            }

            g2.setStroke(new BasicStroke(lineWidth));

            if (lineWidth < 0.1f) {
                g2.draw(transformedGlyph2);
            } else {
                g2.fill(transformedGlyph2);
            }

            //reset opacity
            g2.setComposite(comp);
        }

        g2.setPaint(currentCol);

    }

}

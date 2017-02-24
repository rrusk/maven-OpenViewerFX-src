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
 * AnnotationFactory.java
 * ---------------
 */
package org.jpedal.objects.acroforms.creation;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import javax.imageio.ImageIO;
import org.jpedal.color.DeviceCMYKColorSpace;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.objects.GraphicsState;
import static org.jpedal.objects.acroforms.creation.SwingFormFactory.curveInk;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;

public class AnnotationFactory {

    /**
     * Determine the type of annotation from the sub type value and call
     * appropriate method to create an icon for the annotation
     *
     * @param form PdfObject containing the annotation
     * @return BufferedImage of the annotation or null if the supplied FormObject contains errors
     */
    public static BufferedImage getIcon(final PdfObject form){
        return getIcon(form, 1.0f);
    }
    
    /**
     * Determine the type of annotation from the sub type value and call
     * appropriate method to create an icon for the annotation with the given
     * scaling applied
     *
     * @param form PdfObject containing the annotation
     * @param scaling Scaling to display the annotation at
     * @return BufferedImage of the annotation or null if the supplied FormObject contains errors
     */
    public static BufferedImage getIcon(final PdfObject form, final float scaling){
        
        switch(form.getParameterConstant(PdfDictionary.Subtype)){
            case PdfDictionary.Text :
                return getTextIcon(form);
            case PdfDictionary.Highlight :
                return getHightlightIcon(form, scaling);
            case PdfDictionary.Square :
                return getSquareIcon(form, scaling);
            case PdfDictionary.Underline :
                return getUnderLineIcon(form);
            case PdfDictionary.StrickOut :
                return getStrickOutIcon(form);
            case PdfDictionary.Caret :
                return getCaretIcon(form);
            case PdfDictionary.FileAttachment :
                return getFileAttachmentIcon();
            case PdfDictionary.Line :
                return getLineIcon(form, scaling);
            case PdfDictionary.Polygon :
                return getPolyIcon(form, false, scaling);
            case PdfDictionary.PolyLine :
                return getPolyIcon(form, true, scaling);
            case PdfDictionary.Circle :
                return getCircleIcon(form, scaling);
            case PdfDictionary.Squiggly:
                return getSquigglyIcon(form);
            case PdfDictionary.Sound:
                return getSoundIcon(form);
            case PdfDictionary.Ink:
                return getInkIcon(form, scaling);
        }
        
        return null;
    }
    
    private static BufferedImage getInkIcon(final PdfObject form, final float scaling){
        final float[] quad = form.getFloatArray(PdfDictionary.Rect);
        if (quad != null) {

            final Rectangle bounds = getFormBounds((FormObject) form, quad, scaling);
            final Object[] InkListArray = form.getObjectArray(PdfDictionary.InkList);
            if(bounds.width>0 && bounds.height>0){
                final BufferedImage icon1 = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
                final Graphics2D g2 = (Graphics2D)icon1.getGraphics();
                setStroke(form, g2, scaling);
                scanInkListTree(InkListArray, form, g2, scaling);
                return icon1;
            }
        }
        return null;
    }
    
    /**
     * Method to scan through an InkList array, draw the ink to the provided
     * Graphics object and return the bounds of the InkList
     *
     * @param InkListArray Object array representing an InkList
     * @param form FormObject for the Ink annotation this InkList came from
     * @param g Graphics object to draw the Ink to
     * @return float array representing the Inks bounds in the order 
     * lowest x / y, largest x / x
     */
    public static float[] scanInkListTree(final Object[] InkListArray, final PdfObject form, final Graphics g){
        return scanInkListTree(InkListArray, form, g, 1.0f);
    }
    
    /**
     * Method to scan through an InkList array, draw the ink to the provided
     * Graphics object at the specified scaling and return the bounds of the InkList
     *
     * @param InkListArray Object array representing an InkList
     * @param form FormObject representing the Ink annotation this InkList came from
     * @param g Graphics object to draw the Ink to
     * @param scaling float value representing scaling where 1 equals 100%
     * @return float array representing the Inks bounds in the order 
     * lowest x / y, largest x / x
     */
    public static float[] scanInkListTree(final Object[] InkListArray, final PdfObject form, final Graphics g, final float scaling) {

        final float[] quad = form.getFloatArray(PdfDictionary.Rect);
        if (quad == null) {
            return null;
        }

        final Rectangle bounds = getFormBounds((FormObject) form, quad, scaling);

        float minX = bounds.x;
        float minY = bounds.y;
        float maxX = bounds.x+bounds.width;
        float maxY = bounds.y+bounds.height;

        float[] vals = null;
        final Graphics2D g2 = (Graphics2D) g;

        setStroke(form, g2, scaling);
        
        //if specific DecodeParms for each filter, set othereise use global
        if(InkListArray !=null){

            final int count= InkListArray.length;

            float x;
            float y;
            
            //If Graphics not set, don't draw anything.
            if(g!=null){
                final float[] underlineColor = form.getFloatArray(PdfDictionary.C);
                Color c1 = new Color(0);
                if(underlineColor!=null){
                    switch(underlineColor.length){
                        case 0:
                            //Should not happen. Do nothing. Annotation is transparent
                            break;
                        case 1:
                            //DeviceGrey colorspace
                            c1 = new Color(underlineColor[0],underlineColor[0],underlineColor[0],1.0f);
                            break;
                        case 3:
                            //DeviceRGB colorspace
                            c1 = new Color(underlineColor[0],underlineColor[1],underlineColor[2],1.0f);
                            break;
                        case 4:
                            //DeviceCMYK colorspace
                            final DeviceCMYKColorSpace cmyk = new DeviceCMYKColorSpace();
                            cmyk.setColor(underlineColor, 4);
                            c1 = new Color(cmyk.getColor().getRGB());

                            break;
                        default:
                            break;
                    }
                }

                g2.setColor(new Color(0.0f,0.0f,0.0f,0.0f));
                g2.fillRect(0, 0, bounds.width, bounds.height);
                g2.setColor(c1);
                g2.setPaint(c1);
            }

            for(int i=0;i<count;i++){

                if(InkListArray[i] instanceof byte[]){
                    final byte[] decodeByteData= (byte[]) InkListArray[i];
                    
                    if(vals==null){
                        vals = new float[count];
                    }

                    if (decodeByteData != null) {
                        final String val = new String(decodeByteData);
                        final float v = Float.parseFloat(val)*scaling;

                        switch (i % 2) {
                            case 0:
                                if (v < minX) {
                                    minX = v;
                                }
                                if (v > maxX) {
                                    maxX = v;
                                }
                                x = (v - bounds.x);
                                vals[i] = x;
                                break;
                            case 1:
                                if (v < minY) {
                                    minY = v;
                                }
                                if (v > maxY) {
                                    maxY = v;
                                }
                                y = bounds.height - (v - bounds.y);
                                vals[i] = y;

                                break;
                        }
                    }
                } else {
                    final float[] r = scanInkListTree((Object[]) InkListArray[i], form, g, scaling);
                    if (r[0] < minX) {
                        minX = r[0];
                    }
                    if (r[2] > maxX) {
                        maxX = r[2];
                    }
                    if (r[1] < minY) {
                        minY = r[1];
                    }
                    if (r[3] > maxY) {
                        maxY = r[3];
                    }
                }
            }
        }

            if (vals != null) {
                if (vals.length < 6) { //Only use lines on ink
                    if (g2 != null) {

                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for(int i=0; i<vals.length; i+=4){
                            final Line2D.Float line = new Line2D.Float(vals[0], vals[1], vals[2], vals[3]);
                            g2.draw(line);
                        }
                    }
            }else{ //Enough armguments so curve ink
                    final float[] values = curveInk(vals);
                if(g2 != null){

                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    for(int i=0; i<values.length; i+=8){

                            final CubicCurve2D curve = new CubicCurve2D.Double(values[i], values[i + 1], values[i + 2], values[i + 3], values[i + 4], values[i + 5], values[i + 6], values[i + 7]);
                            g2.draw(curve);
                        }
                    }
                }
            }

        return new float[]{minX, minY, maxX, maxY};
    }
    
    private static Color convertFloatArrayToColor(final float[] values){
        Color c = new Color(0,0,0,0);
        if (values != null) {
            switch (values.length) {
                case 0:
                    //Should not happen. Do nothing. Annotation is transparent
                    break;
                case 1:
                    //DeviceGrey colorspace
                    c = new Color(values[0], values[0], values[0]);
                    break;
                case 3:
                    //DeviceRGB colorspace
                    c = new Color(values[0], values[1], values[2]);
                    break;
                case 4:
                    //DeviceCMYK colorspace
                    final DeviceCMYKColorSpace cmyk = new DeviceCMYKColorSpace();
                    cmyk.setColor(values, 4);
                    c = new Color(cmyk.getColor().getRGB());
                    c = new Color(c.getRed(), c.getGreen(), c.getBlue());

                    break;
                default:
                    break;
            }
        }
        return c;
    }
    
    private static Rectangle getFormBounds(final FormObject form, final float[] rect, final float scaling) {
        final Rectangle bounds = (form).getBoundingRectangle();

        //Bounds is 0 so calculate based on rect areas
        if (bounds.getWidth() == 0 && bounds.getHeight() == 0) {
            for (int i = 0; i != rect.length; i++) {
                if (i % 2 == 0) {
                    if (bounds.x > rect[i]) {
                        bounds.x = (int) rect[i];
                    }
                    if (bounds.x + bounds.width < rect[i]) {
                        bounds.width = (int) (rect[i] - bounds.x);
                    }
                } else {
                    if (bounds.y > rect[i]) {
                        bounds.y = (int) rect[i];
                    }
                    if (bounds.y + bounds.height < rect[i]) {
                        bounds.height = (int) (rect[i] - bounds.y);
                    }
                }

            }
        }
        
        bounds.x *= scaling;
        bounds.y *= scaling;
        bounds.width *= scaling;
        bounds.height *= scaling;
        
        return bounds;
    }
    
    private static BufferedImage getStrickOutIcon(final PdfObject form){
        
        final Color color = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.C));

        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad == null) {
            quad = form.getFloatArray(PdfDictionary.Rect);
        }

        final Rectangle bounds = getFormBounds((FormObject)form, quad, 1.0f);
        if(bounds.width>0 && bounds.height>0){
            final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics g = icon.getGraphics();

            if (quad.length >= 8) {
                for (int hi = 0; hi != quad.length; hi += 8) {
                    final int x = (int) quad[hi] - bounds.x;
                    int y = (int) quad[hi + 5] - bounds.y;
                    //Adjust y for display
                    y = (bounds.height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                    final int width = (int) (quad[hi + 2] - quad[hi]);
                    final int height = (int) (quad[hi + 1] - quad[hi + 5]);

                    try {
                        g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
                        g.fillRect(0, 0, width, height);
                        g.setColor(color);
                        g.fillRect(x, y + (height / 2), width, 1);
                    } catch (final Exception e) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                }
            }
            return icon;
        }
        return null;
    }
    
    private static BufferedImage getUnderLineIcon(final PdfObject form){
        
        final Color color = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.C));
        
        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad == null) {
            quad = form.getFloatArray(PdfDictionary.Rect);
        }
        
        final Rectangle bounds = getFormBounds((FormObject)form, quad, 1.0f);
        if(bounds.width>0 && bounds.height>0){
            final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics g = icon.getGraphics();

            if (quad.length >= 8) {
                for (int hi = 0; hi != quad.length; hi += 8) {
                    final int x = (int) quad[hi] - bounds.x;
                    int y = (int) quad[hi + 5] - bounds.y;
                    //Adjust y for display
                    y = (bounds.height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                    final int width = (int) (quad[hi + 2] - quad[hi]);
                    final int height = (int) (quad[hi + 1] - quad[hi + 5]);

                    try {
                        g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
                        g.fillRect(x, y, width, height);
                        g.setColor(color);
                        g.fillRect(x, y + height - 1, width, 1);
                    } catch (final Exception e) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                }
            }

            return icon;
        }
        
        return null;
    }
    
    private static BufferedImage getSquigglyIcon(final PdfObject form){
        
        final Color color = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.C));
        
        float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad == null) {
            quad = form.getFloatArray(PdfDictionary.Rect);
        }
        
        final Rectangle bounds = getFormBounds((FormObject)form, quad, 1.0f);
        if(bounds.width>0 && bounds.height>0){
            final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics g = icon.getGraphics();

            if (quad.length >= 8) {
                for (int hi = 0; hi != quad.length; hi += 8) {
                    final int x = (int) quad[hi] - bounds.x;
                    int y = (int) quad[hi + 5] - bounds.y;
                    //Adjust y for display
                    y = (bounds.height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                    final int width = (int) (quad[hi + 2] - quad[hi]);
                    final int height = (int) (quad[hi + 1] - quad[hi + 5]);
                    final int step = 6;
                    final int bottom = y + height-1;
                    final int top = bottom-(step/2);
                    try {
                        g.setColor(color);

                        for(int i=0; i<width; i+=step){
                            g.drawLine(x+i, bottom, x+i+(step/2), top);
                            g.drawLine(x+i+(step/2), top, x+i+step, bottom);
                        }
                    } catch (final Exception e) {
                        LogWriter.writeLog("Exception: " + e.getMessage());
                    }
                }
            }

            return icon;
        }
        
        return null;
    }
    
    private static BufferedImage getSquareIcon(final PdfObject form, final float scaling){
        final Color c = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.C));
        final Color ic = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.IC));
        
        final float[] quad = form.getFloatArray(PdfDictionary.Rect);
        if (quad != null) {
            
            final Rectangle bounds = getFormBounds((FormObject)form, quad, scaling);
            if(bounds.width>0 && bounds.height>0){
                final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
                final Graphics2D g = (Graphics2D)icon.getGraphics();
                final int width = setStroke(form, g, scaling);
                g.setColor(ic);
                g.fillRect(0, 0, bounds.width, bounds.height);
                g.setColor(c);
                g.drawRect(width/2, width/2, bounds.width-width, bounds.height-width);
    //            FormRenderUtilsG2.renderBorder(g, (FormObject)form, 0,0,icon.getWidth(), icon.getHeight());

                return icon;
            }
        }
        //Return a small empty image as no highlight to make.
        return null;
    }
    
    private static int setStroke(final PdfObject form, final Graphics2D g, final float scaling) {
        int borderWidth = 1;
        final PdfObject BS = form.getDictionary(PdfDictionary.BS);
        if (BS != null && g!=null) {
            final String s = BS.getName(PdfDictionary.S);
            borderWidth = BS.getInt(PdfDictionary.W);
            if (borderWidth == -1) {
                borderWidth = 1;
            }
            final PdfArrayIterator d = BS.getMixedArray(PdfDictionary.D);

            if (s == null || s.equals("S")) {
                borderWidth*=scaling;
                g.setStroke(new BasicStroke(borderWidth));
            } else {
                if (s.equals("D")) {
                    float[] dash = {3};
                    if (d != null && d.hasMoreTokens()) {
                        final int count = d.getTokenCount();
                        if (count > 0) {
                            dash = d.getNextValueAsFloatArray();
                        }
                    }
                    borderWidth*=scaling;
                    g.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, 0));
                }
            }
        }
        return borderWidth;
    }
    
    private static BufferedImage getCircleIcon(final PdfObject form, final float scaling){
        final Color c = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.C));
        final Color ic = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.IC));
        final float[] quad = form.getFloatArray(PdfDictionary.Rect);
        
        if (quad != null) {
            
            final Rectangle bounds = getFormBounds((FormObject)form, quad, scaling);
            if(bounds.width>0 && bounds.height>0){
                final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
                final Graphics2D g = (Graphics2D)icon.getGraphics();

                final int width = setStroke(form, g, scaling);

                g.setColor(ic);
                g.fillOval((width/2), (width/2), bounds.width-width, bounds.height-width);
                g.setColor(c);
                g.drawOval((width/2),(width/2), bounds.width-width, bounds.height-width);

                return icon;
            }
        }
        //Return a small empty image as no highlight to make.
        return null;
    }
    
    private static BufferedImage getLineIcon(final PdfObject form, final float scaling){
        final Color c = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.C));
        
        final float[] quad = form.getFloatArray(PdfDictionary.Rect);
        final float[] line = form.getFloatArray(PdfDictionary.L);
        if (quad != null && line != null) {
            
            final Rectangle bounds = getFormBounds((FormObject)form, quad, scaling);
            if(bounds.width>0 && bounds.height>0){
                final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
                final Graphics2D g = (Graphics2D)icon.getGraphics();
                setStroke(form, g, scaling);
                g.setColor(c);
                g.drawLine((int)(line[0]*scaling)-bounds.x, (int)(bounds.height-((line[1]*scaling)-bounds.y)), (int)(line[2]*scaling)-bounds.x, (int)(bounds.height-((line[3]*scaling)-bounds.y)));

                return icon;
            }
        }
        //Return a small empty image as no highlight to make.
        return null;
    }
    
    private static BufferedImage getPolyIcon(final PdfObject form, final boolean line, final float scaling){
        final Color c = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.C));
        final Color ic = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.IC));
        
        final float[] quad = form.getFloatArray(PdfDictionary.Rect);
        final float[] vertices = form.getFloatArray(PdfDictionary.Vertices);
        
        if (quad != null && vertices!=null) {
            
            final Rectangle bounds = getFormBounds((FormObject)form, quad, scaling);
            if(bounds.width>0 && bounds.height>0){
                final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
                final Graphics2D g = (Graphics2D)icon.getGraphics();
                setStroke(form, g, scaling);

                final GeneralPath gPath = new GeneralPath(Path2D.WIND_NON_ZERO);
                gPath.moveTo((int)(vertices[0]*scaling) - bounds.x, (int)(bounds.height - ((vertices[1]*scaling) - bounds.y)));

                for(int i=2; i!=vertices.length; i+=2){
                    gPath.lineTo((int)(vertices[i]*scaling) - bounds.x, (int)(bounds.height - ((vertices[i+1]*scaling) - bounds.y)));
                }

                if (!line) {
                    gPath.lineTo((int)(vertices[0]*scaling) - bounds.x, (int)(bounds.height - ((vertices[1]*scaling)-bounds.y)));
                    g.setColor(ic);
                    g.fill(gPath);
                }

                g.setColor(c);
                g.draw(gPath);

                return icon;
            }
        }

        //Return a small empty image as no highlight to make.
        return null;
    }
    
    private static BufferedImage getCaretIcon(final PdfObject form){
        final Color c = convertFloatArrayToColor(form.getFloatArray(PdfDictionary.C));
        final float[] rd = form.getFloatArray(PdfDictionary.RD);
        final float[] quad = form.getFloatArray(PdfDictionary.Rect);
        
        if (quad != null) {
            
            final Rectangle bounds = getFormBounds((FormObject)form, quad, 1.0f);
            if(bounds.width>0 && bounds.height>0){
                final BufferedImage icon = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
                final Graphics2D g = (Graphics2D)icon.getGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setStroke(new BasicStroke(rd[1]));
                g.setColor(c);
                g.drawLine(0, bounds.height, bounds.width/2, 0);
                g.drawLine(bounds.width/2, 0, bounds.width, bounds.height);

                return icon;
            }
        }
        //Return a small empty image as no highlight to make.
        return null;
    }
    
    private static BufferedImage getHightlightIcon(final PdfObject form, final float scaling){
        final float[] f = form.getFloatArray(PdfDictionary.C);
        Color c = new Color(0);
        if (f != null) {
            switch (f.length) {
                case 0:
                    //Should not happen. Do nothing. Annotation is transparent
                    break;
                case 1:
                    //DeviceGrey colorspace
                    c = new Color(f[0], f[0], f[0], 0.5f);
                    break;
                case 3:
                    //DeviceRGB colorspace
                    c = new Color(f[0], f[1], f[2], 0.5f);
                    break;
                case 4:
                    //DeviceCMYK colorspace
                    final DeviceCMYKColorSpace cmyk = new DeviceCMYKColorSpace();
                    cmyk.setColor(f, 4);
                    c = new Color(cmyk.getColor().getRGB());
                    c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.5f);

                    break;
                default:
                    break;
            }
        }
        
        final float[] quad = form.getFloatArray(PdfDictionary.QuadPoints);
        if (quad != null) {
            final Rectangle bounds = ((FormObject)form).getBoundingRectangle();
            
            //Bounds is 0 so calculate based on quad areas
            if(bounds.getWidth()==0 && bounds.getHeight()==0){
                for(int i=0; i!=quad.length; i++){
                    if(i%2==0){
                        if(bounds.x>quad[i]){
                            bounds.x = (int)quad[i];
                        }
                        if(bounds.x+bounds.width<quad[i]){
                            bounds.width = (int)(quad[i]-bounds.x);
                        }
                    }else{
                        if(bounds.y>quad[i]){
                            bounds.y = (int)quad[i];
                        }
                        if(bounds.y+bounds.height<quad[i]){
                            bounds.height = (int)(quad[i]-bounds.y);
                        }
                    }
                    
                }
            }
            
            final int scaledWidth = (int)(bounds.width*scaling);
            final int scaledHeight = (int)(bounds.height*scaling);
            if(scaledWidth>0 && scaledHeight*scaling>0){
                final BufferedImage icon = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_4BYTE_ABGR);
                final Graphics g = icon.getGraphics();

                if (quad.length >= 8) {
                    for (int hi = 0; hi != quad.length; hi += 8) {
                        final int x = (int) quad[hi] - bounds.x;
                        int y = (int) quad[hi + 5] - bounds.y;
                        //Adjust y for display
                        y = (bounds.height - y) - (int) (quad[hi + 1] - quad[hi + 5]);
                        final int width = (int) (quad[hi + 2] - quad[hi]);
                        final int height = (int) (quad[hi + 1] - quad[hi + 5]);
                        final Rectangle rh = new Rectangle((int)(x*scaling), (int)(y*scaling), (int)(width*scaling), (int)(height*scaling));
                        g.setColor(c);
                        g.fillRect(rh.x, rh.y, rh.width, rh.height);
                    }
                }
                return icon;
            }
        }
        //Return a small empty image as no highlight to make.
        return null;
    }
    
    /**
     * Get the Icon to be used for Text annotations
     * 
     * @param form PdfObject representing a TextAnnotation
     * @return BufferedImage representing the icon for this annotation
     */
    public static BufferedImage getTextIcon(final PdfObject form){
        
        final String iconFile = getPngImageForAnnotation(form);
        
        BufferedImage commentIcon = null;
        try {
            commentIcon = ImageIO.read(AnnotationFactory.class.getResource(iconFile));
        } catch (final IOException e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
        
        setColorForAnnotation(form, commentIcon);

        return commentIcon;
    }
    
    private static final int STYLE_KEY_FONT = 1718578804;
    private static final int STYLE_KEY_TEXT = 1952807028;
    private static final int STYLE_KEY_COLOR = 1668246639;
    
    /**
     * Load the font style described in a DS string into a Component for use by
     * some annotations (e.g FreeText annotations).
     * 
     * @param DS byte[] representing the DS string
     * @param textInput Component to be used by a given annotation
     * @param scaling float value representing scaling where 1 is equal to 100%
     */
    public static void loadFontValues(final byte[] DS, final Component textInput, final float scaling){
        
        Font font = new Font("Lucida", Font.PLAIN, 12);
        int position = 0;
        
        while(position<DS.length){
            while(DS[position]==' '){
                position++;
            }
            final int key = (DS[position++]<<24) + (DS[position++]<<16) + (DS[position++]<<8) + (DS[position++]); 
            
            switch(key){
                case STYLE_KEY_FONT:
                    switch(DS[position]){
                        case 58 : //font
                            position++;
                            if(DS[position]==' '){
                                position++;
                            }
                            final StringBuilder name = new StringBuilder();
                            while(position<DS.length && DS[position]!=' '){
                                name.append((char)DS[position]);
                                position++;
                            }
                            position++;
                            final StringBuilder size = new StringBuilder();
                            while(position<DS.length && (DS[position]>='0' && DS[position]<='9')){ //ignore decimal as we use integer in java
                                size.append((char)DS[position]);
                                position++;
                            }
                            
                            //Progress to end
                            while(position<DS.length && DS[position]!=';'){
                                position++;
                            }
                            position++;
                            String fontName = StandardFonts.expandName(name.toString());
                            final String altName= FontMappings.fontSubstitutionAliasTable.get(fontName.toLowerCase());
                            if(altName!=null) {
                                fontName = altName;
                            }
                            fontName = StandardFonts.expandName(fontName);
                            final int fontSize = (int)(Integer.parseInt(size.toString())*scaling);
                            font = new Font(fontName, Font.PLAIN, fontSize);
                            break;
                        case 45 : //font-stretch
                            //Do nothing for now
                            while(position<DS.length && DS[position]!=';'){
                                position++;
                            }
                            position++;
                            break;
                        default : 
                            LogWriter.writeLog("Unknown style key for FreeText annotation.");
                    }
                    break;
                case STYLE_KEY_TEXT: //text-align
                    while(position<DS.length && DS[position]!=';'){
                        position++;
                    }
                    position++;
                    break;
                case STYLE_KEY_COLOR: //color as hex values
                    while(position<DS.length && DS[position]!=':'){
                        position++;
                    }
                    position++;
                    if(DS[position]==' '){
                        position++;
                    }
                    
                    if(DS[position]=='#'){
                        final StringBuilder colString = new StringBuilder();
                        while(position<DS.length && DS[position]!=';'){
                            colString.append((char)DS[position]);
                            position++;
                        }
                        final Color c = new Color(
                            Integer.valueOf( colString.substring( 1, 3 ), 16 ),
                            Integer.valueOf( colString.substring( 3, 5 ), 16 ),
                            Integer.valueOf( colString.substring( 5, 7 ), 16 ) );
                        textInput.setForeground(c);
                    }else{
                        LogWriter.writeLog("Unknown color for FreeText annotation.");
                    }
                    break;
            }
        }
        textInput.setFont(font);
    }
    
    private static void setColorForAnnotation(final PdfObject form, final BufferedImage commentIcon) {
        
        //Set color of annotation
        float[] col = form.getFloatArray(PdfDictionary.C);
        
        if(col==null){//If not color set we should use white
            col = new float[]{1.0f,1.0f,1.0f};
        }
        
        final Color c = new Color(col[0], col[1], col[2]);
        final int rgb = c.getRGB();

        //Replace default color with specified color
        for(int x=0; x!=commentIcon.getWidth(); x++){
            for(int y=0; y!=commentIcon.getHeight(); y++){
                
                //Checks for yellow (R255,G255,B000) and replaces with color
                if(commentIcon.getRGB(x, y)==-256){
                    commentIcon.setRGB(x, y, rgb);
                }
            }
        }
    }

    /**
     * Get the image to be used to display an annotation based on the Name 
     * variable it contains.
     * This method is intended for internal use with Text annotations only.
     * 
     * @param form PdfObject representing a form Object
     * @return String value representing the location of the image to use
     */
    public static String getPngImageForAnnotation(final PdfObject form) {
        
        String name = form.getName(PdfDictionary.Name);
        
        if(name==null) {
            name = "Note";
        }
        /* Name of the icon image to use for the icon of this annotation
        * - predefined icons are needed for names:-
        * Comment, Key, Note, Help, NewParagraph, Paragraph, Insert
        */
        if(name.equals("Comment")){
            return "/org/jpedal/objects/acroforms/res/comment.png";
        }else if(name.equals("Check")){
            return  "/org/jpedal/objects/acroforms/res/Check.png";
        }else if(name.equals("Checkmark")){
            return  "/org/jpedal/objects/acroforms/res/Checkmark.png";
        }else if(name.equals("Circle")){
            return "/org/jpedal/objects/acroforms/res/Circle.png";
        }else if(name.equals("Cross")){
            return "/org/jpedal/objects/acroforms/res/Cross.png";
        }else if(name.equals("CrossHairs")){
            return "/org/jpedal/objects/acroforms/res/CrossHairs.png";
        }else if(name.equals("Help")){
            return "/org/jpedal/objects/acroforms/res/Help.png";
        }else if(name.equals("Insert")){
            return "/org/jpedal/objects/acroforms/res/InsertText.png";
        }else if(name.equals("Key")){
            return "/org/jpedal/objects/acroforms/res/Key.png";
        }else if(name.equals("NewParagraph")){
            return "/org/jpedal/objects/acroforms/res/NewParagraph.png";
        }else if(name.equals("Paragraph")){
            return "/org/jpedal/objects/acroforms/res/Paragraph.png";
        }else if(name.equals("RightArrow")){
            return "/org/jpedal/objects/acroforms/res/RightArrow.png";
        }else if(name.equals("RightPointer")){
            return "/org/jpedal/objects/acroforms/res/RightPointer.png";
        }else if(name.equals("Star")){
            return "/org/jpedal/objects/acroforms/res/Star.png";
        }else if(name.equals("UpLeftArrow")){
            return "/org/jpedal/objects/acroforms/res/Up-LeftArrow.png";
        }else if(name.equals("UpArrow")){
            return "/org/jpedal/objects/acroforms/res/UpArrow.png";
        }else{ //Default option. Name = Note
            return "/org/jpedal/objects/acroforms/res/TextNote.png";
        }
    }
    
    private static BufferedImage getFileAttachmentIcon(){
        
        final String iconFile = "/org/jpedal/objects/acroforms/res/FileAttachment.png";
        
        BufferedImage icon = null;
        try {
            icon = ImageIO.read(AnnotationFactory.class.getResource(iconFile));
        } catch (final IOException e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
        
        return icon;
    }
    
    private static BufferedImage getSoundIcon(final PdfObject form){
        
        String iconFile = "/org/jpedal/objects/acroforms/res/Speaker.png";
        
        final String name = form.getName(PdfDictionary.Name);
        if(name!=null && name.equals("Mic")){
            iconFile = "/org/jpedal/objects/acroforms/res/Microphone.png";
        }
        
        BufferedImage icon = null;
        try {
            icon = ImageIO.read(AnnotationFactory.class.getResource(iconFile));
        } catch (final IOException e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
        
        return icon;
    }
    
    /**
     * Method to create an icon to represent the annotation and render it.
     * @param form :: PdfObject to hold the annotation data
     * @param current :: DynamicVectorRender to draw the annotation
     * @param pageNumber :: Int value of the page number
     * @param rotation :: Int value of the page rotation
     */
    public static void renderFlattenedAnnotation(final PdfObject form, final DynamicVectorRenderer current, final int pageNumber, final int rotation) {

        final BufferedImage image=AnnotationFactory.getIcon(form);

        if (image != null) {
            final GraphicsState gs = new GraphicsState();

            /*
             * now draw the finished image of the form
             */
            final int iconHeight = image.getHeight();
            final int iconWidth = image.getWidth();

            final float[] rect = form.getFloatArray(PdfDictionary.Rect);

            //Some Text annotations can have incorrect sizes so correct to icon size
            if (form.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Text) {
                rect[2] = rect[0] + iconWidth;
                rect[1] = rect[3] - iconHeight;
                form.setFloatArray(PdfDictionary.Rect, rect);
            }
            //4 needed as we upsample by a factor of 4
            //Factor out rotation as icon should not be rotated
            switch (rotation % 360) {
                case 0:
                    gs.CTM = new float[][]{{iconWidth, 0, 1}, {0, iconHeight, 1}, {0, 0, 0}};

                    gs.x = rect[0];
                    gs.y = rect[3] - iconHeight;

                    //draw onto image
                    gs.CTM[2][0] = rect[0];
                    gs.CTM[2][1] = rect[3] - iconHeight;
                    break;
                case 90:
                    gs.CTM = new float[][]{{0, iconWidth, 1}, {-iconHeight, 0, 1}, {0, 0, 0}};

                    gs.x = rect[0] + iconHeight;
                    gs.y = rect[3];

                    //draw onto image
                    gs.CTM[2][0] = rect[0] + iconHeight;
                    gs.CTM[2][1] = rect[3];
                    break;
                case 180:
                    gs.CTM = new float[][]{{-iconWidth, 0, 1}, {0, -iconHeight, 1}, {0, 0, 0}};

                    gs.x = rect[0];
                    gs.y = rect[3] + iconHeight;

                    //draw onto image
                    gs.CTM[2][0] = rect[0];
                    gs.CTM[2][1] = rect[3] + iconHeight;
                    break;
                case 270:
                    gs.CTM = new float[][]{{0, -iconWidth, 1}, {iconHeight, 0, 1}, {0, 0, 0}};

                    gs.x = rect[0] - iconHeight;
                    gs.y = rect[3];

                    //draw onto image
                    gs.CTM[2][0] = rect[0] - iconHeight;
                    gs.CTM[2][1] = rect[3];
                    break;
            }

            //Hard code blendMode for highlights to ensure correct output
            if (form.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Highlight) {
                current.setGraphicsState(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE), PdfDictionary.Darken);
                current.setGraphicsState(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL), PdfDictionary.Darken);
            }

            current.drawImage(pageNumber, image, gs, false, form.getObjectRefAsString(), -1);

            if (form.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Highlight) {
                current.setGraphicsState(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE), PdfDictionary.Normal);
                current.setGraphicsState(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL), PdfDictionary.Normal);
            }
        }
    }
    
    /**
     * Create a String to represent the current date.
     * 
     * @return String representing the current date.
     * The String has the format "D:YYYYMMDDHHMMSS"
     * For instance January 1, 2000, 11:58:55 become D:20000101115855
     */
    public static String getCurrentDateAsString(){
        
        final Calendar cal = Calendar.getInstance();
        String date = "D:" + cal.get(Calendar.YEAR) + String.format("%02d", (cal.get(Calendar.MONTH)) + 1) + String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + String.format("%02d", cal.get(Calendar.MINUTE)) + String.format("%02d", cal.get(Calendar.SECOND));
        final int offset = cal.getTimeZone().getOffset(System.currentTimeMillis());
        if(offset>0){
            date += '+';
        }
        if(offset!=0){
            date+=String.format("%02d", ((offset/1000)/3600));
            date+='\'';
            date+=String.format("%02d", ((offset/1000)%3600)/60);
            date+='\'';
        }
        return date;
    }
    
}

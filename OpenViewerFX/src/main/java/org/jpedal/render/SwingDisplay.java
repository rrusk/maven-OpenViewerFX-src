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
 * SwingDisplay.java
 * ---------------
 */
package org.jpedal.render;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.jpedal.color.DeviceRGBColorSpace;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.exception.PdfException;
import org.jpedal.external.JPedalCustomDrawObject;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.*;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfShape;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.image.ImageDataToJavaImage;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.parser.image.downsample.DownSampler;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.utils.repositories.*;
import org.jpedal.utils.repositories.generic.Vector_Rectangle_Int;

 public class SwingDisplay extends GUIDisplay{
    
    
     //debug flag for testing new image rescaling
    public static final boolean testSampling=false;
    
    
    //Flag to prevent drawing highlights too often.
    boolean ignoreHighlight;
    
    float lastStrokeOpacity=-1;
    float lastFillOpacity=-1;
    
    //stop screen being cleared on next repaint
    private boolean noRepaint;
    
    //track items painted to reduce unnecessary calls
    private int lastItemPainted=-1;

    //tell renderer to optimise calls if possible
    private boolean optimsePainting;
    
    private int pageX1=9999, pageX2=-9999, pageY1=-9999, pageY2=9999;
    
    //used to cache single image
    private BufferedImage singleImage;
    
    private int imageCount;
    
    //hint for conversion ops
    private static final RenderingHints hints;
    
    private final Map<String, Integer> cachedWidths=new HashMap<String, Integer>(10);
    
    private final Map<String, Integer> cachedHeights=new HashMap<String, Integer>(10);
    
    private Map<Object, Object> fonts=new HashMap<Object, Object>(50);
    
    private final Set<String> fontsUsed=new HashSet<String>(50);
    
    protected GlyphFactory factory;
    
    private PdfGlyphs glyphs;
    
    private final Map<String, Integer> imageID=new HashMap<String, Integer>(10);
    
    private final Map<String, double[]> storedImageValues=new HashMap<String, double[]>(10);
    
    //text highlights if needed
    private int[] textHighlightsX;
    
    //allow user to diable g2 setting
    boolean stopG2setting;
    
    float[] x_coord,y_coord;

    private Vector_Object text_color;
    private Vector_Object stroke_color;
    private Vector_Object fill_color;
    
    private Vector_Object stroke;
    
    Vector_Int shapeType;
    
    private Vector_Double af1;
    private Vector_Double af2;
    private Vector_Double af3;
    private Vector_Double af4;
    
    //TR for text
    private Vector_Int TRvalues;
    
    //font sizes for text
    private Vector_Int fs;
    
    //line widths if not 0
    private Vector_Int lw;
    
    //holds rectangular outline to test in redraw*/
    private Vector_Shape clips;
    
    //holds object type
    private Vector_Object javaObjects;
    
    //holds fill type
    private Vector_Int textFillType;
    
    //holds object type
    private Vector_Float opacity;
    
    //holds blends
    private Vector_Int BMvalues;
    
    //used to track col changes
    int lastFillTextCol,lastFillCol,lastStrokeCol;
    
    //used to track strokes
    Stroke lastStroke;
    
    //trakc affine transform changes
    private double[] lastAf=new double[4];
    
    //used to minimise TR and font changes by ignoring duplicates
    private int lastTR=2,lastFS=-1,lastLW=-1;
    
    //ensure colors reset if text
    boolean resetTextColors=true;
    
    boolean fillSet,strokeSet;
    
    //If highlgihts are not null and no highlgihts are drawn
    //then it is likely a scanned page. Treat differently.
    private boolean needsHighlights = true;
    
    private int paintThreadCount;
    private int paintThreadID;
    
    //For IDR internal use only
    private boolean[] drawnHighlights;
    
    static {
        hints =new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    }
    
    public SwingDisplay() {
    	currentItem = 0;
        type =DynamicVectorRenderer.DISPLAY_SCREEN;
    }
    
    /**
     * @param defaultSize
     */
    void setupArrays(final int defaultSize) {
        
        x_coord=new float[defaultSize];
        y_coord=new float[defaultSize];
        text_color=new Vector_Object(defaultSize);
        textFillType=new Vector_Int(defaultSize);
        stroke_color=new Vector_Object(defaultSize);
        fill_color=new Vector_Object(defaultSize);
        stroke=new Vector_Object(defaultSize);
        pageObjects=new Vector_Object(defaultSize);
        javaObjects=new Vector_Object(defaultSize);
        shapeType=new Vector_Int(defaultSize);
        areas=new Vector_Rectangle_Int(defaultSize);
        af1=new Vector_Double(defaultSize);
        af2=new Vector_Double(defaultSize);
        af3=new Vector_Double(defaultSize);
        af4=new Vector_Double(defaultSize);
        
        clips=new Vector_Shape(defaultSize);
        objectType=new Vector_Int(defaultSize);
        
        opacity=new Vector_Float(defaultSize);
        
        currentItem = 0;
    }
    
    public SwingDisplay(final int pageNumber, final boolean addBackground, final int defaultSize, final ObjectStore newObjectRef) {
        
        this.rawPageNumber =pageNumber;
        this.objectStoreRef = newObjectRef;
        this.addBackground=addBackground;
        
        setupArrays(defaultSize);
        type =DynamicVectorRenderer.DISPLAY_SCREEN;
    }
    
    
    public SwingDisplay(final int pageNumber, final ObjectStore newObjectRef, final boolean isPrinting) {
        
        this.rawPageNumber =pageNumber;
        this.objectStoreRef = newObjectRef;
        this.isPrinting=isPrinting;
        
        setupArrays(defaultSize);
        type =DynamicVectorRenderer.DISPLAY_SCREEN;
        
    }
    
    private void renderHighlight(final Rectangle highlight, final Graphics2D g2){
        
        if(highlight!=null && !ignoreHighlight){
            //Backup current g2 paint and composite
            final Composite comp = g2.getComposite();
            final Paint p = g2.getPaint();
            
            //Set new values for highlight
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,DecoderOptions.highlightComposite));
            
            if(invertHighlight){
                g2.setColor(Color.WHITE);
                g2.setXORMode(Color.BLACK);
            }else{
                
                g2.setPaint(DecoderOptions.highlightColor);
            }
            //Draw highlight
            g2.fill(highlight);
            
            //Reset to starting values
            g2.setComposite(comp);
            g2.setPaint(p);
            
            needsHighlights = false;
        }
    }
    
   public void stopG2HintSetting(final boolean isSet){
        stopG2setting=isSet;
        
    }
    
    /* remove all page objects and flush queue */
    private void flush() {
        
        singleImage=null;
        
        imageCount=0;
        
        lastFS = -1;
        
        objectAreas = null;
        
        if(shapeType!=null){
            
            shapeType.clear();
            pageObjects.clear();
            objectType.clear();
            javaObjects.clear();
            areas.clear();
            clips.clear();
            x_coord=new float[defaultSize];
            y_coord=new float[defaultSize];
            textFillType.clear();
            text_color.clear();
            fill_color.clear();
            stroke_color.clear();
            stroke.clear();
            
            if(TRvalues!=null) {
                TRvalues = null;
            }
            
            if(fs!=null) {
                fs = null;
            }
            
            if(lw!=null) {
                lw = null;
            }
            
            af1.clear();
            af2.clear();
            af3.clear();
            af4.clear();
            
            if(opacity!=null) {
                opacity.clear();
            }
            
            if(BMvalues!=null) {
                BMvalues.clear();
            }
            
            lastStrokeOpacity=-1;
            lastFillOpacity=-1;

            endItem=-1;
        }
        
        //pointer we use to flag color change
        lastFillTextCol=0;
        lastFillCol=0;
        lastStrokeCol=0;
        
        lastClip=null;
        hasClips=false;
        
        //track strokes
        lastStroke=null;
        
        lastAf=new double[4];
        
        currentItem=0;
        
        fillSet=false;
        strokeSet=false;
        
        fonts.clear();
        fontsUsed.clear();
        
        imageID.clear();
        
        pageX1=9999;
        pageX2=-9999;
        pageY1=-9999;
        pageY2=9999;
        
        lastScaling=0;
        
    }
    
    private boolean renderFailed;
    
    //optional frame for user to pass in - if present, error warning will be displayed
    private Container frame;
    
    //make sure user only gets 1 error message a session
    private static boolean userAlerted;
    
    
    
    /*renders all the objects onto the g2 surface*/
    @Override
    @SuppressWarnings("OverlyLongMethod")
    public void paint(final Rectangle[] highlights, final AffineTransform viewScaling, final Rectangle userAnnot){
        
        //take a lock
        final int currentThreadID=++paintThreadID;
        paintThreadCount++;
        
        //Keep track of drawn highlights so we don't draw multiple times
        if(highlights!=null){
            drawnHighlights = new boolean[highlights.length];
            for(int i=0; i!=drawnHighlights.length; i++) {
                drawnHighlights[i] = false;
            }
        }
        
        //ensure all other threads dead or this one killed  (screen only)
        if(paintThreadCount>1){
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                //tell user and log
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            
            if(currentThreadID!=paintThreadID){
                paintThreadCount--;
                return;
            }
        }
        
        final boolean debug=false;
        
        String fontUsed;
        float a=0,b = 0,c=0,d=0;
        
        Rectangle dirtyRegion=null;
        
        //local copies
        final int[] objectTypes=objectType.get();
        final int[] textFill=textFillType.get();
        
        //currentItem to make the code work - can you let me know what you think
        final int count=currentItem; //DO nOT CHANGE
        final Area[] pageClips=clips.get();
        final double[] afValues1=af1.get();
        int[] fsValues=null;
        if(fs!=null) {
            fsValues = fs.get();
        }

        int[] lwValues=null;
        if(lw!=null) {
            lwValues = lw.get();
        }
        final double[] afValues2=af2.get();
        final double[] afValues3=af3.get();
        final double[] afValues4=af4.get();
        final Object[] text_color=this.text_color.get();
        final Object[] fill_color=this.fill_color.get();
        
        final Object[] stroke_color=this.stroke_color.get();
        final Object[] pageObjects=this.pageObjects.get();
        
        final Object[] javaObjects=this.javaObjects.get();
        final Object[] stroke=this.stroke.get();
        final int[] fillType=this.shapeType.get();
        
        float[] opacity = null;
        if(this.opacity!=null) {
            opacity = this.opacity.get();
        }
        
        int[] BMvalues=null;
        if(this.BMvalues!=null) {
            BMvalues = this.BMvalues.get();
        }
        
        int[] TRvalues = null;
        if(this.TRvalues!=null) {
            TRvalues = this.TRvalues.get();
        }
        
        int[][] areas=null;
        
        if(this.areas!=null) {
            areas = this.areas.get();
        }
        
        boolean isInitialised=false;
        
        Shape defaultClip=null;
        
        if(g2!=null){
            final Shape rawClip=g2.getClip();
            if(rawClip!=null) {
                dirtyRegion = rawClip.getBounds();
            }
            
            defaultClip=g2.getClip();
        }
        
        //used to optimise clipping
        Area clipToUse=null;
        boolean newClip=false;
        
        if(noRepaint) {
            noRepaint = false;
        } else if(lastItemPainted==-1){
            paintBackground(dirtyRegion);
        }
        
        //save raw scaling and apply any viewport
        AffineTransform rawScaling=null;
        
        if(g2!=null){
            rawScaling=g2.getTransform();
            if(viewScaling!=null){
                g2.transform(viewScaling);
                defaultClip=g2.getClip(); //not valid if viewport so disable
            }
        }
        
        Object currentObject;
        int type,textFillType,currentTR=GraphicsState.FILL;
        int lineWidth=0;
        float fillOpacity=1.0f;
        float strokeOpacity=1.0f;
        float x,y ;
        int iCount=0,cCount=0,sCount=0,fsCount=-1,lwCount=0,afCount=-1,tCount=0,stCount=0,
                fillCount=0,strokeCount=0,trCount=0,opCount=0,BMCount=0,
                stringCount=0;//note af is 1 behind!
        PdfPaint textStrokeCol=null,textFillCol=null,fillCol=null,strokeCol = null;
        Stroke currentStroke=null;
        
        //if we reuse image this is pointer to live image
        int imageUsed;
        
        //use preset colours for T3 glyph
        if(colorsLocked){
            strokeCol=this.strokeCol;
            fillCol=this.fillCol;
        }
        
        //now draw all objects
        for(int i=0; i<count;i++){
            
            //if(i>4800)
            //break;
            
        	//Set item we are currently rendering
        	itemToRender = i;
        	
            boolean ignoreItem;
            
            type=objectTypes[i];
            
            Rectangle currentArea=null;
            
            //exit if later paint recall
            if(currentThreadID!=paintThreadID){
                paintThreadCount--;
                
                return;
            }
            
            //generate glyph for text
            if(type<0){
                
                //lazy initialisation on factory
                if(factory==null) {
                    factory = new T1GlyphFactory(false);
                }
                
                final UnrendererGlyph glyph=(UnrendererGlyph)pageObjects[i];
                final Object newGlyph;
                
                //generate glyph now if needed
                final float[][] trm={{a,b},{c,d},{glyph.x,glyph.y}};
                
                final int raw=glyph.rawInt;
                final Integer key= raw;
                final String disp=glyphs.getDisplayValue(key);
                final String charGlyph = glyphs.getCharGlyph(key);
                final String emb = glyphs.getEmbeddedEnc(key);
                final float width=glyph.currentWidth;
                
                if(type==-DynamicVectorRenderer.TEXT){
                    
                    final boolean isSTD=DecoderOptions.isRunningOnMac ||(org.jpedal.fonts.StandardFonts.isStandardFont(glyphs.getBaseFontName(),false));
                    final Area transformedGlyph2= glyphs.getStandardGlyph(trm, raw, disp, width,isSTD);
                    
                    //if its already generated we just need to move it
                    final AffineTransform at2 =AffineTransform.getTranslateInstance(glyph.x,glyph.y);
                    transformedGlyph2.transform(at2);
                    
                    currentArea=RenderUtils.getAreaForGlyph(trm);
                    
                    newGlyph= transformedGlyph2;
                    
                }else{
                    newGlyph= glyphs.getEmbeddedGlyph(factory,charGlyph ,trm, raw, disp, width,emb);
                }
                
                //reset values to generated values
                type=-type;
                objectTypes[i]=type;
                pageObjects[i]=newGlyph;
                
            }
            
            if(type>0){
                
                x=x_coord[i];
                y=y_coord[i];
                
                currentObject=pageObjects[i];
                
                //swap in replacement image
                if(type==DynamicVectorRenderer.REUSED_IMAGE){
                    type=DynamicVectorRenderer.IMAGE;
                    imageUsed= (Integer) currentObject;
                    currentObject=pageObjects[imageUsed];
                }else {
                    imageUsed = -1;
                }
                
                //workout area occupied by glyf
                if(currentArea==null) {
                    currentArea = getObjectArea(afValues1, fsValues, afValues2, afValues3, afValues4, pageObjects, areas, type, x, y, fsCount, afCount, i);
                }
                
                ignoreItem=false;
                
                //see if we need to draw
                if (currentArea != null && userAnnot != null && type < 7) {
                    ignoreItem = testIfAnnotVisible(currentArea, userAnnot, ignoreItem);
                }
                
                if(ignoreItem || (lastItemPainted!=-1 && i<lastItemPainted)){
                    //keep local counts in sync
                    switch (type) {
                        case DynamicVectorRenderer.SHAPE:
                            sCount++;
                            break;
                        case DynamicVectorRenderer.IMAGE:
                            iCount++;
                            break;
                        case DynamicVectorRenderer.REUSED_IMAGE:
                            iCount++;
                            break;
                        case DynamicVectorRenderer.CLIP:
                            cCount++;
                            break;
                        case DynamicVectorRenderer.FONTSIZE:
                            fsCount++;
                            break;
                        case DynamicVectorRenderer.LINEWIDTH:
                            lwCount++;
                            break;
                        case DynamicVectorRenderer.TEXTCOLOR:
                            tCount++;
                            break;
                        case DynamicVectorRenderer.FILLCOLOR:
                            fillCount++;
                            break;
                        case DynamicVectorRenderer.STROKECOLOR:
                            strokeCount++;
                            break;
                        case DynamicVectorRenderer.STROKE:
                            stCount++;
                            break;
                        case DynamicVectorRenderer.TR:
                            trCount++;
                            break;
                    }
                    
                }else{
                    
                    if(!isInitialised && !stopG2setting && g2!=null){
                        
                        if(userHints!=null){
                            g2.setRenderingHints(userHints);
                        }else{
                            //set hints to produce high quality image
                            g2.setRenderingHints(hints);
                        }
                        isInitialised=true;
                    }
                    
                    if(currentTR==GraphicsState.INVISIBLE){
                        needsHighlights = true;
                    }
                    
                    Rectangle highlight = null;
                    
                    switch (type) {
                        
                        case DynamicVectorRenderer.SHAPE:
                            
                            if(debug) {
                                System.out.println("Shape");
                            }
                            
                            if(newClip){
                                RenderUtils.renderClip(clipToUse, dirtyRegion,defaultClip,g2);
                                newClip=false;
                            }

                            paintShape(fillType[sCount], defaultClip, currentObject, fillOpacity, strokeOpacity, fillCol, strokeCol, currentStroke, i);
                            
                            sCount++;
                            
                            break;
                            
                        case DynamicVectorRenderer.TEXT:
                            
                            if(debug) {
                                System.out.println("Text");
                            }
                            
                            if(newClip){
                                RenderUtils.renderClip(clipToUse, dirtyRegion,defaultClip,g2);
                                newClip=false;
                            }

                            paintText(highlights, afValues1, afValues2, afValues3, afValues4, (Area) currentObject, currentTR, fillOpacity, strokeOpacity, x, y, afCount, textStrokeCol, textFillCol, currentArea, highlight);
                            
                            break;
                            
                        case DynamicVectorRenderer.TRUETYPE:
                            
                            if(debug) {
                                System.out.println("Truetype");
                            }
                            
                            if(newClip){
                                RenderUtils.renderClip(clipToUse, dirtyRegion,defaultClip,g2);
                                newClip=false;
                            }
                            
                            paintTrueType(highlights, afValues1, afValues2, afValues3, afValues4, currentObject, currentTR, lineWidth, fillOpacity, strokeOpacity, x, y, afCount, textStrokeCol, textFillCol, currentArea, highlight);

                            break;
                            
                        case DynamicVectorRenderer.TYPE1C:

                        {
                            if (debug) {
                                System.out.println("Type1c");
                            }

                            if (newClip) {
                                RenderUtils.renderClip(clipToUse, dirtyRegion, defaultClip, g2);
                                newClip = false;
                            }

                            final AffineTransform aff = new AffineTransform(afValues1[afCount], afValues2[afCount], afValues3[afCount], afValues4[afCount], x, y);

                            if (!invertHighlight) {
                                highlight = setHighlightForGlyph(currentArea, highlights);
                            }

                            renderEmbeddedText(currentTR, currentObject, DynamicVectorRenderer.TYPE1C, aff, highlight, textStrokeCol, textFillCol, strokeOpacity, fillOpacity, lineWidth);
                        }
                            break;
                            
                        case DynamicVectorRenderer.TYPE3:

                        {
                            if (debug) {
                                System.out.println("Type3");
                            }

                            if (newClip) {
                                RenderUtils.renderClip(clipToUse, dirtyRegion, defaultClip, g2);
                                newClip = false;
                            }

                            final AffineTransform aff = new AffineTransform(afValues1[afCount], afValues2[afCount], afValues3[afCount], afValues4[afCount], x, y);

                            if (!invertHighlight) {
                                highlight = setHighlightForGlyph(currentArea, highlights);
                            }

                            renderEmbeddedText(currentTR, currentObject, DynamicVectorRenderer.TYPE3, aff, highlight, textStrokeCol, textFillCol, strokeOpacity, fillOpacity, lineWidth);
                        }
                            break;
                            
                        case DynamicVectorRenderer.IMAGE:
                            
                            if(newClip){
                                RenderUtils.renderClip(clipToUse, dirtyRegion,defaultClip,g2);
                                newClip=false;
                            }
                            
                            renderImage(afValues1, afValues2, afValues3,afValues4, pageObjects,currentObject, fillOpacity, x, y, iCount, afCount, imageUsed, i);
                            
                            iCount++;
                            
                            break;
                            
                        case DynamicVectorRenderer.CLIP:
                            
                            clipToUse=pageClips[cCount];
                            newClip=true;
                            
                            cCount++;
                            break;
                            
                        case DynamicVectorRenderer.AF:
                            afCount++;
                            break;
                        case DynamicVectorRenderer.FONTSIZE:
                            fsCount++;
                            break;
                        case DynamicVectorRenderer.LINEWIDTH:
                            lineWidth=lwValues[lwCount];
                            lwCount++;
                            break;
                        case DynamicVectorRenderer.TEXTCOLOR:
                            
                            if(debug) {
                                System.out.println("TextCOLOR");
                            }
                            
                            textFillType=textFill[tCount];
                            
                            if(textFillType==GraphicsState.STROKE) {
                                textStrokeCol = (PdfPaint) text_color[tCount];
                            } else {
                                textFillCol = (PdfPaint) text_color[tCount];
                            }
                            
                            tCount++;
                            break;
                        case DynamicVectorRenderer.FILLCOLOR:
                            
                            if(debug) {
                                System.out.println("FillCOLOR");
                            }
                            
                            if(!colorsLocked){
                                fillCol=(PdfPaint) fill_color[fillCount];
                            }
                            fillCount++;
                            
                            break;
                        case DynamicVectorRenderer.STROKECOLOR:
                            
                            if(debug) {
                                System.out.println("StrokeCOL");
                            }
                            
                            if(!colorsLocked){
                                
                                strokeCol=(PdfPaint)stroke_color[strokeCount];
                                
                                if(strokeCol!=null) {
                                    strokeCol.setScaling(cropX, cropH, scaling, 0, 0);
                                }
                            }
                            
                            strokeCount++;
                            break;
                            
                        case DynamicVectorRenderer.STROKE:
                            
                            currentStroke=(Stroke)stroke[stCount];
                            
                            if(debug) {
                                System.out.println("STROKE");
                            }
                            
                            stCount++;
                            break;
                            
                        case DynamicVectorRenderer.TR:
                            
                            if(debug) {
                                System.out.println("TR");
                            }
                            
                            currentTR=TRvalues[trCount];
                            trCount++;
                            break;
                            
                        case DynamicVectorRenderer.STROKEOPACITY:
                            
                            if(debug) {
                                System.out.println("Stroke Opacity " + opacity[opCount] + " opCount=" + opCount);
                            }
                            
                            strokeOpacity=opacity[opCount];
                            opCount++;
                            break;
                        
                         case DynamicVectorRenderer.BLENDMODE:
                            
                            if(debug) {
                                System.out.println("Blend Mode " + BMvalues[BMCount] + " BMCount=" + BMCount);
                            }
                            
                            blendMode=BMvalues[BMCount];
                            BMCount++;
                            break;    
                            
                        case DynamicVectorRenderer.FILLOPACITY:
                            
                            if(debug) {
                                System.out.println("Set Fill Opacity " + opacity[opCount] + " count=" + opCount);
                            }
                            
                            fillOpacity=opacity[opCount];
                            opCount++;
                            break;
                            
                        case DynamicVectorRenderer.STRING:

                            final Shape s1 = g2.getClip();
                            g2.setClip(defaultClip);
                            final AffineTransform defaultAf=g2.getTransform();
                            final String displayValue=(String)currentObject;

                            final double[] af=new double[6];

                            g2.getTransform().getMatrix(af);
                            
                            if(af[2]!=0) {
                                af[2] = -af[2];
                            }
                            if(af[3]!=0) {
                                af[3] = -af[3];
                            }
                            
                            g2.setTransform(new AffineTransform(af));

                            final Font javaFont=(Font) javaObjects[stringCount];

                            g2.setFont(javaFont);

                            if((currentTR & GraphicsState.FILL)==GraphicsState.FILL){

                                if(textFillCol!=null) {
                                    textFillCol.setScaling(cropX, cropH, scaling, 0, 0);
                                }

                                if(customColorHandler !=null){
                                    customColorHandler.setPaint(g2,textFillCol, rawPageNumber, isPrinting);
                                }else if(DecoderOptions.Helper!=null){
                                    DecoderOptions.Helper.setPaint(g2, textFillCol, rawPageNumber, isPrinting);
                                }else {
                                    g2.setPaint(textFillCol);
                                }

                            }

                            if((currentTR & GraphicsState.STROKE)==GraphicsState.STROKE){

                                if(textStrokeCol!=null) {
                                    textStrokeCol.setScaling(cropX, cropH, scaling, 0, 0);
                                }

                                if(customColorHandler !=null){
                                    customColorHandler.setPaint(g2,textFillCol, rawPageNumber, isPrinting);
                                }else if(DecoderOptions.Helper!=null){
                                    DecoderOptions.Helper.setPaint(g2, textFillCol, rawPageNumber, isPrinting);
                                }else {
                                    g2.setPaint(textFillCol);
                                }

                            }
                            
                            //Potential fix for ignoring transform
                            final AffineTransform affine = new AffineTransform(afValues1[afCount],afValues2[afCount],afValues3[afCount],afValues4[afCount],0,0);
                            
                            double scaleFactorX = 1d/affine.getScaleX();
                            double scaleFactorY = 1d/affine.getScaleY();
                            
                            g2.translate(x, y);
                            
                            if(affine.getScaleX()==0){
                                scaleFactorX = 1d/affine.getShearX();
                            }

                            if(affine.getScaleY()==0){
                                scaleFactorY = 1d/affine.getShearY();
                            }
                            
                            affine.scale(Math.abs(scaleFactorX), Math.abs(scaleFactorY));
                            
                            g2.scale(1, -1);
                            g2.transform(affine);                            
                            g2.scale(1, -1);
                            
                            g2.drawString(displayValue, 0, 0);
                            
                            //restore defaults
                            g2.setTransform(defaultAf);
                            g2.setClip(s1);
                            
                            stringCount++;
                            
                            break;
                        
                        case DynamicVectorRenderer.CUSTOM:

                            final Shape s2 = g2.getClip();
                            g2.setClip(defaultClip);
                            final AffineTransform af2 = g2.getTransform();

                            final JPedalCustomDrawObject customObj=(JPedalCustomDrawObject)currentObject;
                            if(isPrinting) {
                                customObj.print(g2, this.rawPageNumber);
                            } else {
                                customObj.paint(g2);
                            }

                            g2.setTransform(af2);
                            g2.setClip(s2);
                            
                            break;
                            
                        case DynamicVectorRenderer.MARKER:
                            
                            final MarkerGlyph marker=(MarkerGlyph) currentObject;
                            fontUsed=marker.fontName;
                            
                            a=marker.a;
                            b=marker.b;
                            c=marker.c;
                            d=marker.d;
                            iCount++;
                            
                            glyphs=(PdfGlyphs) fonts.get(fontUsed);
                            
                            break;                           
                    }
                }
            }
        }
        
        //Reset to minus 1 as rendering loop has ended
        itemToRender = -1;
        
        if(g2!=null){
            //restore clip as not needed when adding highlights
            g2.setClip(defaultClip);
        }
        
        if(highlights!=null) {
            for (int h = 0; h != highlights.length; h++) {
                renderHighlight(highlights[h], g2);
            }
        }
        
        //needs to be before we return defualts to factor
        //in a viewport for abacus
        if(needsHighlights && highlights!=null){
            for(int h=0; h!=highlights.length; h++){
                ignoreHighlight=false;
                renderHighlight(highlights[h], g2);
            }
        }
        
        if(g2!=null){
            //restore transform
            g2.setTransform(rawScaling);
        }
        
        //tell user if problem
        if(frame!=null && renderFailed && !userAlerted){
            
            userAlerted=true;
            
            if(DecoderOptions.showErrorMessages){
                final String status = (Messages.getMessage("PdfViewer.ImageDisplayError")+
                        Messages.getMessage("PdfViewer.ImageDisplayError1")+
                        Messages.getMessage("PdfViewer.ImageDisplayError2")+
                        Messages.getMessage("PdfViewer.ImageDisplayError3")+
                        Messages.getMessage("PdfViewer.ImageDisplayError4")+
                        Messages.getMessage("PdfViewer.ImageDisplayError5")+
                        Messages.getMessage("PdfViewer.ImageDisplayError6")+
                        Messages.getMessage("PdfViewer.ImageDisplayError7"));
                
                JOptionPane.showMessageDialog(frame,status);
                
                frame.invalidate();
                frame.repaint();
            }
        }
        
        //reduce count
        paintThreadCount--;
        
        //track so we do not redo onto raster
        if(optimsePainting){
            lastItemPainted=count;
        }else {
            lastItemPainted = -1;
        }
        
        //track
        lastScaling=scaling;

    }

    static boolean testIfAnnotVisible(final Rectangle currentArea, final Rectangle userAnnot, boolean ignoreItem) {
        
        //Allow for testing components with 0 width or 0 height
        final Rectangle testRect = currentArea.getBounds();
        
        if(testRect.width==0){
            testRect.width++;
        }
        if(testRect.height==0){
            testRect.height++;
        }
        //was glyphArea, changed back to currentArea to fix highlighting issue in Sams files.
        //last width test for odd print issue in phonobingo
        if (!userAnnot.intersects(testRect)) {
            ignoreItem = true;
        }
        return ignoreItem;
    }


    private void paintTrueType(final Rectangle[] highlights, final double[] afValues1, final double[] afValues2, final double[] afValues3, final double[] afValues4, final Object currentObject, final int currentTR, final int lineWidth, final float fillOpacity, final float strokeOpacity, final float x, final float y, final int afCount, final PdfPaint textStrokeCol, final PdfPaint textFillCol, final Rectangle currentArea, Rectangle highlight) {

        //hack to fix exceptions in a PDF using this code to create ReadOnly image
        if(afCount!=-1) {

            final AffineTransform aff = new AffineTransform(afValues1[afCount], afValues2[afCount], afValues3[afCount], afValues4[afCount], x, y);

            if (!invertHighlight) {
                highlight = setHighlightForGlyph(currentArea, highlights);
            }

            renderEmbeddedText(currentTR, currentObject, DynamicVectorRenderer.TRUETYPE, aff, highlight, textStrokeCol, textFillCol, strokeOpacity, fillOpacity, lineWidth);
        }
    }

    private void paintShape(final int fillType, final Shape defaultClip, final Object currentObject, final float fillOpacity, final float strokeOpacity, final PdfPaint fillCol, final PdfPaint strokeCol, final Stroke currentStroke, final int i) {
        Shape s=null;
        if(endItem!=-1 && endItem<i){
            s = g2.getClip();
            g2.setClip(defaultClip);
        }
        
        renderShape(defaultClip, fillType,strokeCol,fillCol, currentStroke, (Shape)currentObject,strokeOpacity,fillOpacity);

        if(endItem!=-1 && endItem<i) {
            g2.setClip(s);
        }
    }

    private void paintText(final Rectangle[] highlights, final double[] afValues1, final double[] afValues2, final double[] afValues3, final double[] afValues4, final Area currentObject, final int currentTR, final float fillOpacity, final float strokeOpacity, final float x, final float y, final int afCount, final PdfPaint textStrokeCol, final PdfPaint textFillCol, final Rectangle currentArea, Rectangle highlight) {
        if(!invertHighlight) {
            highlight = setHighlightForGlyph(currentArea, highlights);
        }

        final AffineTransform def=g2.getTransform();

        if(afCount!=-1){
            g2.transform(new AffineTransform(afValues1[afCount],afValues2[afCount],-afValues3[afCount],-afValues4[afCount],x,y));

            renderText(x,y, currentTR, currentObject, highlight, textStrokeCol,textFillCol,strokeOpacity,fillOpacity);

            g2.setTransform(def);
        }
    }

    //Should not be static
    private Rectangle[] objectAreas;
    
    private Rectangle getObjectArea(final double[] afValues1, final int[] fsValues,
            final double[] afValues2, final double[] afValues3, final double[] afValues4,
            final Object[] pageObjects, final int[][] rectParams, final int type, final float x,
            final float y, final int fsCount, final int afCount, final int i){
        
        Rectangle currentArea=null;
        
        if(objectAreas==null || objectAreas.length!=rectParams.length){
        	objectAreas = new Rectangle[rectParams.length];
        	
        	for(int r = 0; r < objectAreas.length; r++){
        		if(objectAreas[r]!=null){
        			objectAreas[r] = new Rectangle(rectParams[r][0],rectParams[r][1],rectParams[r][2],rectParams[r][3]);
        		}
        	}
        }
        
        if(afValues1!=null && type==DynamicVectorRenderer.IMAGE){
            
            if(objectAreas!=null) {
                currentArea = objectAreas[i];
            }
            
        }else if(afValues1!=null && type==DynamicVectorRenderer.SHAPE){
            
            currentArea=((Shape)pageObjects[i]).getBounds();

        }else if(type==DynamicVectorRenderer.TEXT && afCount>-1){
            
            //Use on page coords to make sure the glyph needs highlighting
            currentArea=RenderUtils.getAreaForGlyph(new float[][]{{(float)afValues1[afCount],(float)afValues2[afCount],0},
                {(float)afValues3[afCount],(float)afValues4[afCount],0},{x,y,1}});
            
        }else if(fsCount!=-1 && afValues1!=null){// && afCount>-1){
            
            final int realSize=fsValues[fsCount];
            if(realSize<0) //ignore sign which is used as flag elsewhere
            {
                currentArea = new Rectangle((int) x + realSize, (int) y, -realSize, -realSize);
            } else {
                currentArea = new Rectangle((int) x, (int) y, realSize, realSize);
            }
        }
        return currentArea;
    }
    
    private void renderImage(final double[] afValues1, final double[] afValues2,
            final double[] afValues3, final double[] afValues4, final Object[] pageObjects,
             Object currentObject, final float fillOpacity,
            final float x, final float y, final int iCount, final int afCount, final int imageUsed, final int i){
        
        
        //generate unique value to every image on given page (no more overighting stuff in the hashmap)
        final String key = Integer.toString(this.rawPageNumber) + Integer.toString(iCount);
        
        if(!isType3Font && objectStoreRef.isRawImageDataSaved(key)){
            currentObject=getResampledImage(key, pageObjects, i, currentObject);
        }
        
        //get image and reload if needed
        BufferedImage img=null;
        if(currentObject!=null) {
            img = (BufferedImage) currentObject;
        }else{
            img = reloadCachedImage(imageUsed, i, img);
        }

        if(img!=null) {
            
            //When we allow transparency in printing there are cases that the JDK can not support, add a work around here to prevent issue.
            //Added for Case 27484
            if(isPrinting && img.getTransparency()==Transparency.TRANSLUCENT && 
                    ((img.getHeight()==1 && afValues4[afCount]<1) 
//                    || (img.getWidth()==1 && afValues1[afCount]<1) //Fix for similar issue with width. Commented out until example is found to confirm
                    )){
                final AffineTransform imageAf=new AffineTransform(afValues1[afCount],afValues2[afCount],afValues3[afCount],1,x,y);
                renderImage(imageAf, img, fillOpacity, null, x, y);
            }else{
                final AffineTransform imageAf=new AffineTransform(afValues1[afCount],afValues2[afCount],afValues3[afCount],afValues4[afCount],x,y);
                renderImage(imageAf, img, fillOpacity, null, x, y);
            }
        }
    }
    
    private Object getResampledImage(final String key, final Object[] pageObjects1, final int i, Object currentObject) {
        
        int sampling=1;
        final int w1;
        final int pY;
        float  scalingToUse=scaling;
        //fix for rescaling on Enkelt-Scanning_-_Bank-10.10.115.166_-_12-12-2007_-_15-27-57jpg50-300.pdf
        if(scaling<1) {
            scalingToUse = 1f;
        }
        final int defaultX= (Integer) objectStoreRef.getRawImageDataParameter(key, ObjectStore.IMAGE_pX);
        final int pX=(int)(defaultX*scalingToUse);
        final int defaultY= (Integer) objectStoreRef.getRawImageDataParameter(key, ObjectStore.IMAGE_pY);
        pY=(int)(defaultY*scalingToUse);
        w1= (Integer) objectStoreRef.getRawImageDataParameter(key, ObjectStore.IMAGE_WIDTH);
        final int h1= (Integer) objectStoreRef.getRawImageDataParameter(key, ObjectStore.IMAGE_HEIGHT);
        final int bpc= (Integer) objectStoreRef.getRawImageDataParameter(key, ObjectStore.IMAGE_DEPTH);
        final byte[] maskCol=(byte[]) objectStoreRef.getRawImageDataParameter(key,ObjectStore.IMAGE_MASKCOL);
        //final int colorspaceID= (Integer) objectStoreRef.getRawImageDataParameter(key, ObjectStore.IMAGE_COLORSPACE);
        
        BufferedImage image=null;
        //down-sample size if displaying
        if(pX>0){
            
            //see what we could reduce to and still be big enough for page
            int newW=w1,newH=h1;
            
            final int smallestH=pY<<2; //double so comparison works
            final int smallestW=pX<<2;
            
            //cannot be smaller than page
            while(newW>smallestW && newH>smallestH){
                sampling <<= 1;
                newW >>= 1;
                newH >>= 1;
            }
            
            //System.out.println("sampling="+sampling+" w,h="+w1+" "+h1+" newW,H"+newW+" "+newH+" pX="+pX+" pY="+pY);
            
            int scaleX=w1/pX;
            if(scaleX<1) {
                scaleX = 1;
            }
            
            int scaleY=h1/pY;
            if(scaleY<1) {
                scaleY = 1;
            }
            
            //choose smaller value so at least size of page
            sampling=scaleX;
            if(sampling>scaleY) {
                sampling = scaleY;
            }
            
            //see what we could reduce to and still be big enough for page
            int defnewW=w1,defnewH=h1;
            
            final int defsmallestH=pY<<2; //double so comparison works
            final int defsmallestW=pX<<2;
            
            //cannot be smaller than page
            while(defnewW>defsmallestW && defnewH>defsmallestH){
                defnewW >>= 1;
                defnewH >>= 1;
            }
            
            //rescan all pixels and down-sample image
            if((scaling>1f || lastScaling>1f)&& sampling>=1 && (lastScaling!=scaling)){
                try{
                    image=resampleImageData(sampling, w1, h1, bpc,maskCol,key);
                }catch(final Exception e){
                    //tell user and log
                    LogWriter.writeLog("Exception rescaling image: " + e.getMessage());
                }
            }
        }
        
        //reset image stored by renderer
        if (image!=null) {
            //reset our track if only graphics
            if(singleImage!=null) {
                singleImage = image;
            }
            pageObjects1[i] = image;
            currentObject=image;
        }
        return currentObject;
    }
    
    private BufferedImage resampleImageData(final int sampling, final int w1, final int h1, final int bpc, final byte[] maskCol, final String key) {
        
        //get data
        final byte[] data= objectStoreRef.getRawImageData(key);
        
        final ImageData imageData=new ImageData(data);
        imageData.setWidth(w1);
        imageData.setHeight(h1);
        imageData.setCompCount(4);
        imageData.setDepth(bpc);
        
        if(testSampling){
            System.out.println("resampleImageData bytes="+data.length+" "+w1+" "+h1+" "+sampling+" bytes="+imageData.getObjectData().length);
        }
        
        GenericColorSpace decodeColorData=new DeviceRGBColorSpace();
        
        BufferedImage image;
        
        if(sampling>1){
            decodeColorData = DownSampler.downSampleImage(decodeColorData, imageData, maskCol, sampling);
        }
        
        if (maskCol!=null) {
            image = ImageDataToJavaImage.makeMaskImage(new ParserOptions(), null, null,imageData, decodeColorData, maskCol);           
        } else { //handle other types

            LogWriter.writeLog( imageData.getWidth() + "W * " + imageData.getHeight() + "H BPC=" + imageData.getDepth() + ' ' + decodeColorData);

            image =ImageDataToJavaImage.makeImage(decodeColorData,imageData);

        }
        
        if(testSampling){
            System.out.println("image now="+image.getWidth()+" "+image.getHeight());
        }
        
       // image=ImageDataToJavaImage.sharpen(image);
        
        return image;
    }
    
    private BufferedImage reloadCachedImage(final int imageUsed, final int i,BufferedImage img) {

        Object currentObject=null;
        try{
            
            //cache single images in memory for speed
            if(singleImage!=null){
                currentObject=singleImage.getSubimage(0,0,singleImage.getWidth(),singleImage.getHeight());
            }
            
            if(currentObject==null){
                
                int keyID=i;
                if(imageUsed!=-1) {
                    keyID = imageUsed;
                }
                
                if(rawKey==null) {
                    currentObject = objectStoreRef.loadStoredImage(rawPageNumber + "_HIRES_" + keyID);
                } else {
                    currentObject = objectStoreRef.loadStoredImage(rawPageNumber + "_HIRES_" + keyID + '_' + rawKey);
                }
                
                //flag if problem
                if(currentObject==null) {
                    renderFailed = true;
                }
            }
            
            img=(BufferedImage)currentObject;
            
        }catch(final Exception e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
        return img;
    }
    
    /**
     * allow user to set component for waring message in renderer to appear -
     * if unset no message will appear
     * @param frame
     */
    public void setMessageFrame(final Container frame){
        this.frame=frame;
    }
    
    /**
     * highlight a glyph by reversing the display. For white text, use black
     */
    private Rectangle setHighlightForGlyph(final Rectangle area, final Rectangle[] highlights) {
        
        
        if (highlights == null || textHighlightsX == null) {
            return null;
        }
        
        ignoreHighlight = false;
        for(int j=0; j!= highlights.length; j++){
            if(highlights[j]!=null && area!=null && (highlights[j].intersects(area))){
                
                //Get intersection of the two areas
                final Rectangle intersection = highlights[j].intersection(area);
                
                //Intersection area between highlight and text area
                final float iArea = intersection.width*intersection.height;
                
                //25% of text area
                final float tArea = (area.width*area.height)/ 4f;
                
                //Only highlight if (x.y) is with highlight and more than 25% intersects
                //or intersect is greater than 60%
                if((highlights[j].contains(area.x, area.y) && iArea>tArea) ||
                        iArea>(area.width*area.height)/ 1.667f){
                    if(!drawnHighlights[j]){
                        ignoreHighlight = false;
                        drawnHighlights[j]=true;
                        return highlights[j];
                    }else{
                        ignoreHighlight = true;
                        return highlights[j];
                    }
                }
            }
        }
        
        //old code not used
        return null;
        
    }
    
    /* saves text object with attributes for rendering*/
    @Override
    public void drawText(final float[][] Trm, final String text, final GraphicsState currentGraphicsState, final float x, final float y, final Font javaFont) {
        
        //set color first
        PdfPaint currentCol;
        
        if(Trm!=null){
            final double[] nextAf= {Trm[0][0],Trm[0][1],Trm[1][0],Trm[1][1],Trm[2][0],Trm[2][1]};
            
            if((lastAf[0]==nextAf[0])&&(lastAf[1]==nextAf[1])&&
                    (lastAf[2]==nextAf[2])&&(lastAf[3]==nextAf[3])){
            }else{
                this.drawAffine(nextAf);
                lastAf[0]=nextAf[0];
                lastAf[1]=nextAf[1];
                lastAf[2]=nextAf[2];
                lastAf[3]=nextAf[3];
            }
        }
        
        final int text_fill_type = currentGraphicsState.getTextRenderType();
        
        //for a fill
        if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL) {
            currentCol=currentGraphicsState.getNonstrokeColor();
            
            if(currentCol.isPattern()){
                drawColor(currentCol,GraphicsState.FILL);
                resetTextColors=true;
            }else{
                
                final int newCol=(currentCol).getRGB();
                if((resetTextColors)||((lastFillTextCol!=newCol))){
                    lastFillTextCol=newCol;
                    drawColor(currentCol,GraphicsState.FILL);
                }
            }
        }
        
        //and/or do a stroke
        if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE){
            currentCol=currentGraphicsState.getStrokeColor();
            
            if(currentCol.isPattern()){
                drawColor(currentCol,GraphicsState.STROKE);
                resetTextColors=true;
            }else{
                
                final int newCol=currentCol.getRGB();
                if((resetTextColors)||(lastStrokeCol!=newCol)){
                    lastStrokeCol=newCol;
                    drawColor(currentCol,GraphicsState.STROKE);
                }
            }
        }
        
        pageObjects.addElement(text);
        javaObjects.addElement(javaFont);
        
        objectType.addElement(DynamicVectorRenderer.STRING);
        
        //add to check for transparency if large
        final int fontSize=javaFont.getSize();
        final int[] rectParams = {(int)x,(int)y,fontSize,fontSize};
        if(fontSize>100) {
            areas.addElement(rectParams);
        } else {
            areas.addElement(null);
        }
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=x;
        y_coord[currentItem]=y;
        
        currentItem++;
        
        resetTextColors=false;
        
    }

    /* save image in array to draw */
    @Override
    public int drawImage(final int pageNumber, final BufferedImage image,
                         final GraphicsState currentGraphicsState,
                         final boolean alreadyCached, final String name, final int previousUse) {
        
        if(previousUse!=-1) {
            return redrawImage(pageNumber, currentGraphicsState, name, previousUse);
        }
        
        this.rawPageNumber =pageNumber;
        final float[][] CTM=currentGraphicsState.CTM;
        
        final float x=currentGraphicsState.x;
        final float y=currentGraphicsState.y;
        
        final double[] nextAf=new double[6];
        
        final boolean cacheInMemory=(image.getWidth()<100 && image.getHeight()<100) || image.getHeight()==1;
        
        String key;
        if(rawKey==null) {
            key = pageNumber + "_" + (currentItem + 1);
        } else {
            key = rawKey + '_' + (currentItem + 1);
        }
        
        final AffineTransform upside_down=new AffineTransform(CTM[0][0],CTM[0][1],CTM[1][0],CTM[1][1],0,0);
            
        upside_down.getMatrix(nextAf);
        
        this.drawAffine(nextAf);
        lastAf[0]=nextAf[0];
        lastAf[1]=nextAf[1];
        lastAf[2]=nextAf[2];
        lastAf[3]=nextAf[3];
            
        int w,h;

        if(!alreadyCached || cachedWidths.get(key)==null){
            w = image.getWidth();
            h = image.getHeight();
        }else{
            w= cachedWidths.get(key);
            h= cachedHeights.get(key);
        }
            
        if(!alreadyCached && !cacheInMemory){

            if(!isPrinting){

                //cache PDF with single image for speed
                if(imageCount==0){
                    singleImage=image.getSubimage(0,0,image.getWidth(),image.getHeight());

                    imageCount++;
                }else {
                    singleImage = null;
                }
            }
            if(rawKey==null){
                objectStoreRef.saveStoredImageAsBytes(pageNumber+"_HIRES_"+currentItem,image,false);
                imageIDtoName.put(currentItem,pageNumber+"_HIRES_"+currentItem);
            }else{
                objectStoreRef.saveStoredImageAsBytes(pageNumber+"_HIRES_"+currentItem+ '_' +rawKey,image,false);
                imageIDtoName.put(currentItem,pageNumber+"_HIRES_"+currentItem+ '_' +rawKey);
            }

            if(rawKey==null) {
                key = pageNumber + "_" + currentItem;
            } else {
                key = rawKey + '_' + currentItem;
            }

            cachedWidths.put(key, w);
            cachedHeights.put(key, h);
        }
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=x;
        y_coord[currentItem]=y;
        
        objectType.addElement(DynamicVectorRenderer.IMAGE);
        final float WidthModifier = 1;
        final float HeightModifier = 1;
        
        //ignore in this case /PDFdata/baseline_screens/customers3/1773_A2.pdf
        if(CTM[0][0]>0 && CTM[0][0]<0.05 && CTM[0][1]!=0 && CTM[1][0]!=0 && CTM[1][1]!=0){
            areas.addElement(null);
        }else{
            w=(int)(CTM[0][0]*WidthModifier);
            if(w==0) {
                w = (int) (CTM[0][1] * WidthModifier);
            }
            h=(int)(CTM[1][1]*HeightModifier);
            if(h==0) {
                h = (int) (CTM[1][0] * HeightModifier);
            }
           
            //fix negative height on Ghostscript image in printing
            final int x1=(int)currentGraphicsState.x;
            int y1=(int)currentGraphicsState.y;
            final int w1=w;
            int h1=h;
            if(h1<0){
                y1 += h1;
                h1=-h1;
            }
            
            if(h1==0) {
                h1 = 1;
            }
            
            final int[] rectParams = {x1,y1,w1,h1};
            
            areas.addElement(rectParams);
            
            checkWidth(rectParams);
        }
        
        if(!cacheInMemory){
            pageObjects.addElement(null);
        }else {
            pageObjects.addElement(image);
        }
        
        //store id so we can get as low res image
        imageID.put(name, currentItem);
        
        //nore minus one as affine not yet done
        storedImageValues.put("imageAff-"+currentItem,nextAf);
        
        currentItem++;
        
        return currentItem-1;
    }
    
    /* save image in array to draw */
    private int redrawImage(final int pageNumber, final GraphicsState currentGraphicsState, final String name, final int previousUse) {
        
        this.rawPageNumber =pageNumber;
        
        final float x=currentGraphicsState.x;
        final float y=currentGraphicsState.y;
        
        final double[] nextAf= storedImageValues.get("imageAff-"+previousUse);
        this.drawAffine(nextAf);

        lastAf[0]=nextAf[0];
        lastAf[1]=nextAf[1];
        lastAf[2]=nextAf[2];
        lastAf[3]=nextAf[3];

        if(rawKey==null && imageIDtoName.containsKey(previousUse)){
            imageIDtoName.put(currentItem,pageNumber+"_HIRES_"+previousUse);
        }else{
            imageIDtoName.put(currentItem,pageNumber+"_HIRES_"+previousUse+ '_' +rawKey);
        }
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=x;
        y_coord[currentItem]=y;
        
        objectType.addElement(DynamicVectorRenderer.REUSED_IMAGE);
        
        final int[] previousRectangle=areas.elementAt(previousUse);
        
        int[] newRect=null;
        
        if(previousRectangle!=null){
            newRect = new int[]{(int)x,(int)y,previousRectangle[2],previousRectangle[3]};
        }
        
        areas.addElement(newRect);
        
        if(previousRectangle!=null) {
            checkWidth(newRect);
        }
        
        pageObjects.addElement(previousUse);
        
        //store id so we can get as low res image
        imageID.put(name, previousUse);
        
        currentItem++;
        
        return currentItem-1;
    }
    
    /**
     * track actual size of shape
     */
    private void checkWidth(final int[] rect) {
        
        final int x1=rect[0];
        final int y2=rect[1];
        final int y1=y2+rect[3];
        final int x2=x1+rect[2];
        
        if(x1<pageX1) {
            pageX1 = x1;
        }
        if(x2>pageX2) {
            pageX2 = x2;
        }
        
        if(y1>pageY1) {
            pageY1 = y1;
        }
        if(y2<pageY2) {
            pageY2 = y2;
        }
    }
    
    /**
     * return which part of page drawn onto
     * @return
     */
    public Rectangle getOccupiedArea(){
        return new Rectangle(pageX1,pageY1,(pageX2-pageX1),(pageY1-pageY2));
    }
    
    /*save shape in array to draw*/
    @Override
    public void drawShape(final PdfShape pdfShape, final GraphicsState currentGraphicsState, final int cmd) {
        
        final Shape currentShape=pdfShape.getShape();
        
        final int fillType=currentGraphicsState.getFillType();
        PdfPaint currentCol;
        
        int newCol;
        
        //Moved to BaseDisplay and only used for screen. Kept here incase of future issue.
        //This way it is only used if image is too small after scaling
//        //check for 1 by 1 complex shape and replace with dot
//        if(currentShape.getBounds().getWidth()==1 &&
//                currentShape.getBounds().getHeight()==1 && currentGraphicsState.getLineWidth()<1) {
//            currentShape = new Rectangle(currentShape.getBounds().x, currentShape.getBounds().y, 1, 1);
//        }
        
        //stroke and fill (do fill first so we don't overwrite Stroke)
        if (fillType == GraphicsState.FILL || fillType == GraphicsState.FILLSTROKE) {
            
            currentCol=currentGraphicsState.getNonstrokeColor();
            
            if(currentCol==null) {
                currentCol = new PdfColor(0, 0, 0);
            }
            
            if(currentCol.isPattern()){
                
                drawFillColor(currentCol);
                fillSet=true;
            }else{
                newCol=currentCol.getRGB();
                if((!fillSet) || (lastFillCol!=newCol)){
                    lastFillCol=newCol;
                    drawFillColor(currentCol);
                    fillSet=true;
                }
            }
        }
        
        if ((fillType == GraphicsState.STROKE) || (fillType == GraphicsState.FILLSTROKE)) {
            
            currentCol=currentGraphicsState.getStrokeColor();
            
            if(currentCol instanceof Color){
                newCol=(currentCol).getRGB();
                
                if((!strokeSet) || (lastStrokeCol!=newCol)){
                    lastStrokeCol=newCol;
                    drawStrokeColor(currentCol);
                    strokeSet=true;
                }
            }else{
                drawStrokeColor(currentCol);
                strokeSet=true;
            }
        }
        
        final Stroke newStroke=currentGraphicsState.getStroke();
        if((lastStroke!=null)&&(lastStroke.equals(newStroke))){
            
        }else{
            lastStroke=newStroke;
            drawStroke((newStroke));
        }
        
        pageObjects.addElement(currentShape);
        objectType.addElement(DynamicVectorRenderer.SHAPE);
        
        final int[] shapeParams = {currentShape.getBounds().x, currentShape.getBounds().y, currentShape.getBounds().width, currentShape.getBounds().height};
        areas.addElement(shapeParams);
        
        checkWidth(shapeParams);
        
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=currentGraphicsState.x;
        y_coord[currentItem]=currentGraphicsState.y;
        
        shapeType.addElement(fillType);
        currentItem++;
        
        resetTextColors=true;
        
    }
    
    
    
    /*save text colour*/
    private void drawColor(final PdfPaint currentCol, final int type) {
        
        areas.addElement(null);
        pageObjects.addElement(null);
        objectType.addElement(DynamicVectorRenderer.TEXTCOLOR);
        textFillType.addElement(type); //used to flag which has changed
        
        text_color.addElement(currentCol);
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=0;
        y_coord[currentItem]=0;
        
        currentItem++;
        
        //ensure any shapes reset color
        strokeSet=false;
        fillSet=false;
        
    }
    
    /**
     * Add output to correct area so we can assemble later.
     * Can also be used for any specific code features (ie setting a value)
     */
    @Override
    public synchronized void writeCustom(final int section, final Object str) {

        switch(section){
            
            case RESET_COLORSPACE:
                fillSet=false;
                strokeSet=false;
                break;
            
            case FLUSH_ADDITIONAL_OBJECTS_ON_PAGE:
                flushAdditionalObjOnPage();
                break;
             
            case FLUSH:
                flush();
                break;
                
            default:
                super.writeCustom(section, str);
        }
    }
    
    /*save shape colour*/
    private void drawFillColor(final PdfPaint currentCol) {
        
        pageObjects.addElement(null);
        objectType.addElement(DynamicVectorRenderer.FILLCOLOR);
        areas.addElement(null);
        
        fill_color.addElement(currentCol);
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=0;
        y_coord[currentItem]=0;
        
        currentItem++;
        
        this.lastFillCol=currentCol.getRGB();
    }
    
    /*save opacity settings*/
    @Override
    public void setGraphicsState(final int fillType, final float value, final int BM) {

        if(BM!=PdfDictionary.Normal || BMvalues!=null){
            
            if(BMvalues==null){
                BMvalues=new Vector_Int(defaultSize);
                BMvalues.setCheckpoint();
            }
            
            pageObjects.addElement(null);
            areas.addElement(null);
            
            objectType.addElement(DynamicVectorRenderer.BLENDMODE);
            
            BMvalues.addElement(BM);
            
            x_coord=RenderUtils.checkSize(x_coord,currentItem);
            y_coord=RenderUtils.checkSize(y_coord,currentItem);
            x_coord[currentItem]=0;
            y_coord[currentItem]=0;
            
            currentItem++;
        }

        if(fillType==GraphicsState.STROKE){
            if(lastFillOpacity==value) {
                return;
            } else {
                lastFillOpacity = value;
            }

        }
        if(fillType==GraphicsState.FILL){
            if(lastStrokeOpacity==value) {
                return;
            } else {
                lastStrokeOpacity = value;
            }

        }

        if(value!=1.0f || opacity!=null){
            
            if(opacity==null){
                opacity=new Vector_Float(defaultSize);
                opacity.setCheckpoint();
            }
            
            pageObjects.addElement(null);
            areas.addElement(null);
            
            if(fillType==GraphicsState.STROKE) {
                objectType.addElement(DynamicVectorRenderer.STROKEOPACITY);
            } else {
                objectType.addElement(DynamicVectorRenderer.FILLOPACITY);
            }
            
            opacity.addElement(value);
            
            x_coord=RenderUtils.checkSize(x_coord,currentItem);
            y_coord=RenderUtils.checkSize(y_coord,currentItem);
            x_coord[currentItem]=0;
            y_coord[currentItem]=0;
            
            currentItem++;
        }
    }
    
    /*Method to add Shape, Text or image to main display on page over PDF - will be flushed on redraw*/
    @Override
    public void drawAdditionalObjectsOverPage(final int[] type, final Color[] colors, final Object[] obj) throws PdfException {
        
        if(obj==null){
            return ;
        }

        //remember end of items from PDF page
        if(endItem==-1){
            
            endItem=currentItem;
            
            objectType.setCheckpoint();
            
            shapeType.setCheckpoint();
            
            pageObjects.setCheckpoint();
            
            areas.setCheckpoint();
            
            clips.setCheckpoint();
            
            textFillType.setCheckpoint();
            
            text_color.setCheckpoint();
            
            fill_color.setCheckpoint();
            
            stroke_color.setCheckpoint();
            
            stroke.setCheckpoint();
            
            if(TRvalues==null) {
                TRvalues = new Vector_Int(defaultSize);
            }
            
            TRvalues.setCheckpoint();
            
            if(fs==null) {
                fs = new Vector_Int(defaultSize);
            }
            
            fs.setCheckpoint();
            
            if(lw==null) {
                lw = new Vector_Int(defaultSize);
            }
            
            lw.setCheckpoint();
            
            af1.setCheckpoint();
            
            af2.setCheckpoint();
            
            af3.setCheckpoint();
            
            af4.setCheckpoint();
            
            if(opacity!=null) {
                opacity.setCheckpoint();
            }
            
            if(BMvalues!=null) {
                BMvalues.setCheckpoint();
            }
            
        }
        
        drawUserContent(type, obj, colors);
    }
  
    /**
     * Do not make private
     */
    public void flushAdditionalObjOnPage(){
        
        //reset pointer
        if(endItem!=-1) {
            currentItem = endItem;
        }
        
        endItem=-1;

        objectType.resetToCheckpoint();
        
        shapeType.resetToCheckpoint();
        
        pageObjects.resetToCheckpoint();
        
        areas.resetToCheckpoint();
        
        clips.resetToCheckpoint();
        
        textFillType.resetToCheckpoint();
        
        text_color.resetToCheckpoint();
        
        fill_color.resetToCheckpoint();
        
        stroke_color.resetToCheckpoint();
        
        stroke.resetToCheckpoint();
        
        if(TRvalues!=null) {
            TRvalues.resetToCheckpoint();
        }
        
        if(fs!=null) {
            fs.resetToCheckpoint();
        }
        
        if(lw!=null) {
            lw.resetToCheckpoint();
        }
        
        af1.resetToCheckpoint();
        
        af2.resetToCheckpoint();
        
        af3.resetToCheckpoint();
        
        af4.resetToCheckpoint();
         
        if(opacity!=null) {
            opacity.resetToCheckpoint();
        }
        
        if(BMvalues!=null) {
            BMvalues.resetToCheckpoint();
        }
        
        //reset pointers we use to flag color change
        lastFillTextCol=0;
        lastFillCol=0;
        lastStrokeCol=0;
        
        lastClip=null;
        hasClips=false;
        
        lastStroke=null;
        
        lastAf=new double[4];
        
        fillSet=false;
        strokeSet=false;

        //reset so we do not cache if reused
        lastStrokeOpacity=-1;
        lastFillOpacity=-1;


    }
    
    /*save shape colour*/
    private void drawStrokeColor(final Paint currentCol) {
        
        pageObjects.addElement(null);
        objectType.addElement(DynamicVectorRenderer.STROKECOLOR);
        areas.addElement(null);
        
        stroke_color.addElement(currentCol);
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=0;
        y_coord[currentItem]=0;
        
        currentItem++;
        
        strokeSet=false;
        fillSet=false;
        resetTextColors=true;
        
    }
   
    /*save custom shape*/
    @Override
    public void drawCustom(final Object value) {
        
        pageObjects.addElement(value);
        objectType.addElement(DynamicVectorRenderer.CUSTOM);
        areas.addElement(null);
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=0;
        y_coord[currentItem]=0;
        
        currentItem++;
        
    }
    
    /*save shape stroke*/
    @Override
    public void drawTR(final int value) {
        
        if(value!=lastTR){ //only cache if needed
            
            if(TRvalues==null){
                TRvalues=new Vector_Int(defaultSize);
                TRvalues.setCheckpoint();
            }
            
            lastTR=value;
            
            pageObjects.addElement(null);
            objectType.addElement(DynamicVectorRenderer.TR);
            areas.addElement(null);
            
            this.TRvalues.addElement(value);
            
            x_coord=RenderUtils.checkSize(x_coord,currentItem);
            y_coord=RenderUtils.checkSize(y_coord,currentItem);
            x_coord[currentItem]=0;
            y_coord[currentItem]=0;
            
            
            currentItem++;
        }
    }
    
    
    /*save shape stroke*/
    public void drawStroke(final Stroke current) {
        
        pageObjects.addElement(null);
        objectType.addElement(DynamicVectorRenderer.STROKE);
        areas.addElement(null);
        
        this.stroke.addElement((current));
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=0;
        y_coord[currentItem]=0;
        
        currentItem++;
        
    }
    
    /*save clip in array to draw*/
    @Override
    public void drawClip(final GraphicsState currentGraphicsState, final Shape defaultClip, final boolean canBeCached) {
        
        boolean resetClip=false;
        
        final Area clip=currentGraphicsState.getClippingShape();
        
        if(canBeCached && hasClips && lastClip==null&& clip==null){
            
        }else if (!canBeCached || lastClip==null || clip==null){
            
            resetClip=true;
        }else{
            
            final Rectangle bounds = clip.getBounds();
            final Rectangle oldBounds=lastClip.getBounds();
            
            //see if different size
            if(bounds.x!=oldBounds.x || bounds.y!=oldBounds.y || bounds.width!=oldBounds.width || bounds.height!=oldBounds.height){
                resetClip=true;
            }
        }
        
        if(resetClip){
            
            pageObjects.addElement(null);
            objectType.addElement(DynamicVectorRenderer.CLIP);
            areas.addElement(null);
            
            lastClip=clip;
            
            if(clip==null){
                clips.addElement(null);
            }else{
                if(clip.isSingular() && clip.isRectangular()){
                    clips.addElement(ClipUtils.convertPDFClipToJavaClip(clip));
                }else{
                    if(clip.getBounds2D().getHeight()>0.5f){
                        final Area a = (Area) clip.clone();
                        Stroke stroke = new BasicStroke(0.5f,BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f);

                        Area strokeShape = new Area(stroke.createStrokedShape(a));

                        a.add(strokeShape);

                        clips.addElement(a);
                    }else{
                        clips.addElement(clip);
                    }
                }
            }
            
            x_coord=RenderUtils.checkSize(x_coord,currentItem);
            y_coord=RenderUtils.checkSize(y_coord,currentItem);
            x_coord[currentItem]=currentGraphicsState.x;
            y_coord[currentItem]=currentGraphicsState.y;
            
            currentItem++;
            
            hasClips=true;
        }
    }
    
    /**
     * store glyph info
     */
    @Override
    public void drawEmbeddedText(final float[][] Trm, final int fontSize, final PdfGlyph embeddedGlyph,
    final Object javaGlyph, int type, final GraphicsState gs, final double[] at, final String glyf, final PdfFont currentFontData, final float glyfWidth) {
        
        //set color first
        PdfPaint currentCol;
        
        final int text_fill_type = gs.getTextRenderType();
        
        //for a fill
        if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL) {
            currentCol= gs.getNonstrokeColor();
            
            if(currentCol.isPattern()){
                drawColor(currentCol,GraphicsState.FILL);
                resetTextColors=true;
            }else{
                
                final int newCol=(currentCol).getRGB();
                if((resetTextColors)||((lastFillTextCol!=newCol))){
                    lastFillTextCol=newCol;
                    drawColor(currentCol,GraphicsState.FILL);
                    resetTextColors=false;
                }
            }
        }
        
        //and/or do a stroke
        if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE){
            currentCol= gs.getStrokeColor();
            
            if(currentCol.isPattern()){
                drawColor(currentCol,GraphicsState.STROKE);
                resetTextColors=true;
            }else{
                final int newCol=currentCol.getRGB();
                if((resetTextColors)||(lastStrokeCol!=newCol)){
                    resetTextColors=false;
                    lastStrokeCol=newCol;
                    drawColor(currentCol,GraphicsState.STROKE);
                }
            }
        }
        
        //allow for lines as shadows
        setLineWidth((int)gs.getLineWidth());
        
        drawFontSize(fontSize);
        
        if(javaGlyph !=null){
            
            
            if(Trm!=null){
                final double[] nextAf= {Trm[0][0],Trm[0][1],Trm[1][0],Trm[1][1],Trm[2][0],Trm[2][1]};
                
                if((lastAf[0]==nextAf[0])&&(lastAf[1]==nextAf[1])&&
                        (lastAf[2]==nextAf[2])&&(lastAf[3]==nextAf[3])){
                }else{
                    
                    this.drawAffine(nextAf);
                    lastAf[0]=nextAf[0];
                    lastAf[1]=nextAf[1];
                    lastAf[2]=nextAf[2];
                    lastAf[3]=nextAf[3];
                }
            }
            
            if(!(javaGlyph instanceof Area)) {
                type = -type;
            }
            
        }else{

            if((lastAf[0]==at[0])&&(lastAf[1]==at[1])&&
                    (lastAf[2]==at[2])&&(lastAf[3]==at[3])){
            }else{
                this.drawAffine(at);
                lastAf[0]=at[0];
                lastAf[1]=at[1];
                lastAf[2]=at[2];
                lastAf[3]=at[3];
            }
        }
        
        if(embeddedGlyph==null) {
            pageObjects.addElement(javaGlyph);
        } else {
            pageObjects.addElement(embeddedGlyph);
        }
        
        objectType.addElement(type);
        
        if(type<0){
            areas.addElement(null);
        }else{
            if(javaGlyph!=null){
                final int[] rectParams = {(int)Trm[2][0],(int)Trm[2][1],fontSize,fontSize};
                areas.addElement(rectParams);
                checkWidth(rectParams);
                
            }else{
                //now text
                int realSize=fontSize;
                if(realSize<0) {
                    realSize = -realSize;
                }
                final int[] area= {(int)Trm[2][0],(int)Trm[2][1],realSize,realSize};
                
                areas.addElement(area);
                checkWidth(area);
            }
        }
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=Trm[2][0];
        y_coord[currentItem]=Trm[2][1];
        
        
        currentItem++;
        
    }
    
    /**
     * store af info
     */
    @Override
    void drawAffine(final double[] afValues) {
        
        pageObjects.addElement(null);
        objectType.addElement(DynamicVectorRenderer.AF);
        areas.addElement(null);
        
        af1.addElement(afValues[0]);
        af2.addElement(afValues[1]);
        af3.addElement(afValues[2]);
        af4.addElement(afValues[3]);
        
        x_coord=RenderUtils.checkSize(x_coord,currentItem);
        y_coord=RenderUtils.checkSize(y_coord,currentItem);
        x_coord[currentItem]=(float)afValues[4];
        y_coord[currentItem]=(float)afValues[5];
        
        currentItem++;
        
    }
    
    /**
     * store af info
     */
    private void drawFontSize(final int fontSize) {
        
        int realSize=fontSize;
        if(realSize<0) {
            realSize = -realSize;
        }
        
        if(realSize!=lastFS){
            pageObjects.addElement(null);
            objectType.addElement(DynamicVectorRenderer.FONTSIZE);
            areas.addElement(null);
            
            if(fs==null){
                fs=new Vector_Int(defaultSize);
                fs.setCheckpoint();
            }
            
            fs.addElement(fontSize);
            
            x_coord=RenderUtils.checkSize(x_coord,currentItem);
            y_coord=RenderUtils.checkSize(y_coord,currentItem);
            x_coord[currentItem]=0;
            y_coord[currentItem]=0;
            
            currentItem++;
            
            lastFS=realSize;
            
        }
    }
    
    /**
     * store line width info
     */
    private void setLineWidth(final int lineWidth) {
        
        if(lineWidth!=lastLW ){
            
            areas.addElement(null);
            pageObjects.addElement(null);
            objectType.addElement(DynamicVectorRenderer.LINEWIDTH);
            
            if(lw==null){
                lw=new Vector_Int(defaultSize);
                lw.setCheckpoint();
            }
            
            lw.addElement(lineWidth);
            
            x_coord=RenderUtils.checkSize(x_coord,currentItem);
            y_coord=RenderUtils.checkSize(y_coord,currentItem);
            x_coord[currentItem]=0;
            y_coord[currentItem]=0;
            
            currentItem++;
            
            lastLW=lineWidth;
            
        }
    }
    
    /**
     * rebuild serialised version
     *
     * NOT PART OF API and subject to change (DO NOT USE)
     * @param fonts
     *
     */
    public SwingDisplay(final byte[] stream, final Map<Object, Object> fonts){
        
        // we use Cannoo to turn our stream back into a DynamicVectorRenderer
        try{
            this.fonts = fonts;
            
            final ByteArrayInputStream bis=new ByteArrayInputStream(stream);
            
            //read version and throw error is not correct version
            final int version=bis.read();
            if(version!=1) {
                throw new PdfException("Unknown version in serialised object " + version);
            }
            
            bis.read(); //0=no,1=yes old hires flag
            
            rawPageNumber =bis.read();
            
            x_coord=(float[]) RenderUtils.restoreFromStream(bis);
            y_coord=(float[]) RenderUtils.restoreFromStream(bis);
            
            //read in arrays - opposite of serializeToByteArray();
            //we may need to throw an exception to allow for errors
            
            text_color = (Vector_Object) RenderUtils.restoreFromStream(bis);
            
            textFillType = (Vector_Int) RenderUtils.restoreFromStream(bis);
            
            stroke_color = new Vector_Object();
            stroke_color.restoreFromStream(bis);
            
            fill_color = new Vector_Object();
            fill_color.restoreFromStream(bis);
            
            stroke = new Vector_Object();
            stroke.restoreFromStream(bis);
            
            pageObjects = new Vector_Object();
            pageObjects.restoreFromStream(bis);
            
            javaObjects=(Vector_Object) RenderUtils.restoreFromStream(bis);
            
            shapeType = (Vector_Int) RenderUtils.restoreFromStream(bis);
            
            af1 = (Vector_Double) RenderUtils.restoreFromStream(bis);
            
            af2 = (Vector_Double) RenderUtils.restoreFromStream(bis);
            
            af3 = (Vector_Double) RenderUtils.restoreFromStream(bis);
            
            af4 = (Vector_Double) RenderUtils.restoreFromStream(bis);
            
            clips = new Vector_Shape();
            clips.restoreFromStream(bis);
            
            objectType = (Vector_Int) RenderUtils.restoreFromStream(bis);

            opacity=(Vector_Float) RenderUtils.restoreFromStream(bis);
            
            BMvalues=(Vector_Int) RenderUtils.restoreFromStream(bis);
            
            TRvalues = (Vector_Int) RenderUtils.restoreFromStream(bis);
            
            fs = (Vector_Int) RenderUtils.restoreFromStream(bis);
            lw = (Vector_Int) RenderUtils.restoreFromStream(bis);
            
            final int fontCount= (Integer) RenderUtils.restoreFromStream(bis);
            for(int ii=0;ii<fontCount;ii++){
                
                final Object key=RenderUtils.restoreFromStream(bis);
                final Object glyphs=RenderUtils.restoreFromStream(bis);
                fonts.put(key,glyphs);
            }
            
            final int alteredFontCount= (Integer) RenderUtils.restoreFromStream(bis);
            for(int ii=0;ii<alteredFontCount;ii++){
                
                final Object key=RenderUtils.restoreFromStream(bis);
                
                final PdfJavaGlyphs updatedFont=(PdfJavaGlyphs) fonts.get(key);
                
                updatedFont.setDisplayValues((Map<Integer, String>) RenderUtils.restoreFromStream(bis));
                updatedFont.setCharGlyphs((Map<Integer, String>) RenderUtils.restoreFromStream(bis));
                updatedFont.setEmbeddedEncs((Map<Integer, String>) RenderUtils.restoreFromStream(bis));
                
            }
            
            bis.close();
            
        }catch(final Exception e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
        
        type =DynamicVectorRenderer.DISPLAY_SCREEN;
        //used in loop to draw so needs to be set
        currentItem=pageObjects.get().length;
        
    }
    
    /**stop screen being cleared on repaint - used by Canoo code
     *
     * NOT PART OF API and subject to change (DO NOT USE)
     **/
    public void stopClearOnNextRepaint(final boolean flag) {
        noRepaint=flag;
    }
    
    /**
     * turn object into byte[] so we can move across
     * this way should be much faster than the stadard Java serialise.
     *
     * NOT PART OF API and subject to change (DO NOT USE)
     *
     * @throws IOException
     */
    @Override
    public byte[] serializeToByteArray(final Set<String> fontsAlreadyOnClient) throws IOException{
        
        final ByteArrayOutputStream bos=new ByteArrayOutputStream();
        
        //add a version so we can flag later changes
        bos.write(1);
        
        //flag hires
        //0=no,1=yes
        bos.write(1);
        
        //save page
        bos.write(rawPageNumber);
        
        text_color.trim();
        stroke_color.trim();
        fill_color.trim();
        stroke.trim();
        pageObjects.trim();
        javaObjects.trim();
        stroke.trim();
        pageObjects.trim();
        javaObjects.trim();
        shapeType.trim();
        af1.trim();
        af2.trim();
        af3.trim();
        af4.trim();
        
        clips.trim();
        objectType.trim();
        if(opacity!=null) {
            opacity.trim();
        }
        if(BMvalues!=null) {
            BMvalues.trim();
        }
        
        if(TRvalues!=null) {
            TRvalues.trim();
        }
        
        if(fs!=null) {
            fs.trim();
        }
        
        if(lw!=null) {
            lw.trim();
        }
        
        RenderUtils.writeToStream(bos,x_coord);
        RenderUtils.writeToStream(bos,y_coord);
        RenderUtils.writeToStream(bos,text_color);
        RenderUtils.writeToStream(bos,textFillType);
        stroke_color.writeToStream(bos);
        fill_color.writeToStream(bos);
        
        stroke.writeToStream(bos);
        
        pageObjects.writeToStream(bos);
        
        RenderUtils.writeToStream(bos,javaObjects);
        RenderUtils.writeToStream(bos,shapeType);
        
        RenderUtils.writeToStream(bos,af1);
        RenderUtils.writeToStream(bos,af2);
        RenderUtils.writeToStream(bos,af3);
        RenderUtils.writeToStream(bos,af4);
        
        clips.writeToStream(bos);
        
        RenderUtils.writeToStream(bos,objectType);
        RenderUtils.writeToStream(bos,opacity);
        RenderUtils.writeToStream(bos,BMvalues);
        RenderUtils.writeToStream(bos,TRvalues);
        
        RenderUtils.writeToStream(bos,fs);
        RenderUtils.writeToStream(bos,lw);
        
        int fontCount=0,updateCount=0;
        final Set<String> fontsAlreadySent=new HashSet<String>(10);
        final Set<String> newFontsToSend=new HashSet<String>(10);
        
        for (final String fontUsed : fontsUsed) {
            if (!fontsAlreadyOnClient.contains(fontUsed)) {
                fontCount++;
                newFontsToSend.add(fontUsed);
            } else {
                updateCount++;
                fontsAlreadySent.add(fontUsed);
            }
        }
        
        //new fonts
        RenderUtils.writeToStream(bos, fontCount);

        for (final String key : newFontsToSend) {
            
            RenderUtils.writeToStream(bos,key);
            RenderUtils.writeToStream(bos,fonts.get(key));
            
            fontsAlreadyOnClient.add(key);
        }
        
        //new data on existing fonts
        RenderUtils.writeToStream(bos, updateCount);

        for (final String key : fontsAlreadySent) {
            
            RenderUtils.writeToStream(bos,key);
            final PdfJavaGlyphs aa = (PdfJavaGlyphs) fonts.get(key);
            RenderUtils.writeToStream(bos,aa.getDisplayValues());
            RenderUtils.writeToStream(bos,aa.getCharGlyphs());
            RenderUtils.writeToStream(bos,aa.getEmbeddedEncs());
            
        }
        
        bos.close();
        
        fontsUsed.clear();
        
        return bos.toByteArray();
    }
    
    /**
     * for font if we are generatign glyph on first render
     */
    public void checkFontSaved(final Object glyph, final String name, final PdfFont currentFontData) {
        
        //save glyph at start
        pageObjects.addElement(glyph);
        objectType.addElement(DynamicVectorRenderer.MARKER);
        areas.addElement(null);
        currentItem++;
        
        if(fontsUsed.contains(name) || currentFontData.isFontSubsetted()){
            fonts.put(name,currentFontData.getGlyphData());
            fontsUsed.add(name);
        }
    }
    
    public void setPrintPage(final int currentPrintPage) {
        rawPageNumber = currentPrintPage;
    }
}


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
 * FormStream.java
 * ---------------
 */
package org.jpedal.objects.raw;



import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;
import org.jpedal.external.ExternalHandlers;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.PdfObjectFactory;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.render.T3Display;
import org.jpedal.render.T3Renderer;
import org.jpedal.utils.LogWriter;


/**
 * can object and creat images, set values in Appearances
 */
public class FormStream {

    public static final boolean debugUnimplemented = false;//to show unimplemented parts*/
    public static final boolean debug = false;//print info to screen

    //only display once
    private static boolean showFontMessage;

    /**
     * exit when an unimplemented feature or error has occured in form/annot code
     */
    public static final boolean exitOnError=false;

    public static Object[] getRolloverKeyValues(PdfObject form, PdfFileReader pdfFileReader) {
        
        final PdfObject APobjR = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.R);

        String key=null;
        PdfObject val=null;
        PdfObject rollOffDic = null;
        
        final PdfKeyPairsIterator APkeys = APobjR.getKeyPairsIterator();
        
        if (APkeys != null && APkeys.getTokenCount() > 0) {
            
            while (APkeys.hasMorePairs()) {
                String glyphKey = APkeys.getNextKeyAsString();
                byte[] data = APkeys.getNextValueAsBytes();
                
                if (data != null) {
                    if (glyphKey.equals("Off")) {
                        rollOffDic = PdfObjectFactory.getPDFObjectObjectFromRefOrDirect(new FormObject(glyphKey), pdfFileReader, data, PdfDictionary.AP);
                    } else {
                        key = glyphKey;
                        val = PdfObjectFactory.getPDFObjectObjectFromRefOrDirect(new FormObject(glyphKey), pdfFileReader, data, PdfDictionary.AP);
                    }
                }
                APkeys.nextPair();
            }
        } else {
            if (APobjR.getDictionary(PdfDictionary.Off) != null) {
                rollOffDic = APobjR.getDictionary(PdfDictionary.Off);
            } else if (APobjR.getDecodedStream() != null) {
                rollOffDic = APobjR;
            }
            
            //if we have a root stream then it is the off value
            if(APobjR.getDictionary(PdfDictionary.On) !=null){
                key = "On";
                val = APobjR.getDictionary(PdfDictionary.On);
            }
        }
        return new Object[] {key, val, rollOffDic};
    }
    
    public static Object[] getNormalKeyValues(PdfObject form, PdfFileReader pdfFileReader) {

        final PdfObject APobjN = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.N);

        String key=null;
        PdfObject val=null;
        PdfObject normalOffDic = null;

        //@zain @bethan - you will need to copy my changes into your version of this routine
        //DO THIS FIRST
        
        final PdfKeyPairsIterator APkeys=APobjN.getKeyPairsIterator();
        
        if(APkeys!=null && APkeys.getTokenCount()>0) {

            while(APkeys.hasMorePairs()){

                String glyphKey=APkeys.getNextKeyAsString();
                byte[] data=APkeys.getNextValueAsBytes();

                if(data!=null){
                    if(glyphKey.equals("Off")){
                        normalOffDic=PdfObjectFactory.getPDFObjectObjectFromRefOrDirect(new FormObject(glyphKey), pdfFileReader, data, PdfDictionary.AP);
                    }else{  
                        key=glyphKey;                   
                        val=PdfObjectFactory.getPDFObjectObjectFromRefOrDirect(new FormObject(glyphKey), pdfFileReader, data, PdfDictionary.AP);
                    }
                }
              
                APkeys.nextPair();
            }            
        }else{
            
            //if we have a root stream then it is the off value
            //check in order of N Off, MK I, then N
            //as N Off overrides others and MK I is in preference to N
            if(APobjN.getDictionary(PdfDictionary.Off) !=null){
                normalOffDic = APobjN.getDictionary(PdfDictionary.Off);
            }else if(form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I) !=null
                    && form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.IF)==null){
                //look here for MK IF
                //if we have an IF inside the MK then use the MK I as some files shown that this value is there
                //only when the MK I value is not as important as the AP N.
                normalOffDic = form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I);
            }else if(APobjN.getDecodedStream()!=null){
                normalOffDic = APobjN;
            }
        
            if(APobjN.getDictionary(PdfDictionary.On) !=null){
                val = APobjN.getDictionary(PdfDictionary.On);
                key = "On";
            }
        }
        
        
        return new Object[]{key,val,normalOffDic};
    }

        public static Object[] getDownKeyValues(PdfObject form, PdfFileReader pdfFileReader){
        
            final PdfObject APobjD = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.D);

            String key=null;
            PdfObject val=null;
            PdfObject downOffDic = null;
            
            final PdfKeyPairsIterator APkeys=APobjD.getKeyPairsIterator();
            
            if(APkeys!=null && APkeys.getTokenCount()>0) {
                
                while(APkeys.hasMorePairs()) {
                    
                    String glyphKey=APkeys.getNextKeyAsString();
                    byte[] data=APkeys.getNextValueAsBytes();
                    
                    if (data != null) {
                        if (glyphKey.equals("Off")) {
                            downOffDic = PdfObjectFactory.getPDFObjectObjectFromRefOrDirect(new FormObject(glyphKey), pdfFileReader, data, PdfDictionary.AP);
                        } else {
                            key=glyphKey;
                            val=PdfObjectFactory.getPDFObjectObjectFromRefOrDirect(new FormObject(glyphKey), pdfFileReader, data, PdfDictionary.AP);
                        }
                    }
                    
                    APkeys.nextPair();
                    
                }
            } 
            
            else {
                
                //down on
                if (APobjD.getDictionary(PdfDictionary.On) != null) {
                    key = "On";
                    val = APobjD.getDictionary(PdfDictionary.On);
                }

                //down off
                //if we have a root stream then it is the off value
                if (APobjD.getDecodedStream() != null) {
                    downOffDic = APobjD;
                } else if (APobjD.getDictionary(PdfDictionary.Off) != null) {
                    downOffDic = APobjD.getDictionary(PdfDictionary.Off);
                }

            }

            return new Object[]{key, val, downOffDic};
        
    }
    
    
    /** handle of file reader for form streams*/
    protected PdfObjectReader currentPdfFile;

    /**
     * flag to show if XFA (will be disabled if XFA from version which is not pure XFA)
     */
    public boolean isXFA;
    
    /**
     * stop anyone creating empty  instance
     */
    public FormStream(){}

    public static final int[] id = {PdfDictionary.A,PdfDictionary.C2,PdfDictionary.Bl,
            PdfDictionary.E, PdfDictionary.X, PdfDictionary.D, PdfDictionary.U, PdfDictionary.Fo,
            PdfDictionary.PO, PdfDictionary.PC, PdfDictionary.PV,
            PdfDictionary.PI, PdfDictionary.O, PdfDictionary.C1, PdfDictionary.K,
            PdfDictionary.F, PdfDictionary.V, PdfDictionary.C2, PdfDictionary.DC,
            PdfDictionary.WS, PdfDictionary.DS, PdfDictionary.WP, PdfDictionary.DP};

    /**
     * takes in a FormObject already populated with values for the child to overwrite
     */
    public void createAppearanceString(final FormObject formObj, final PdfObjectReader inCurrentPdfFile) {

        currentPdfFile = inCurrentPdfFile;
        
        init(formObj);

    }

    private void init(final FormObject formObject) {

        final boolean debug=false;//formObject.getPDFRef().equals("68 0 R");

        if(debug) {
            System.out.println("------------------------------setValues-------------------------------" + formObject + ' ' + formObject.getObjectRefAsString());
        }

        //set Ff flags
        final int Ff=formObject.getInt(PdfDictionary.Ff);
        if(Ff!=PdfDictionary.Unknown) {
            formObject.commandFf(Ff);
        }

        //set Javascript
        resolveAdditionalAction(formObject);

        if(debug) {
            System.out.println("AP=" + formObject.getDictionary(PdfDictionary.AP));
        }


        //at the moment only handles static
        // (and not dynamic which are created at Runtime if
        // formObject.getBoolean(PdfDictionary.NeedAppearances) is true
        setupAPimages(formObject,currentPdfFile.getObjectReader());


        //set H
        final int key = formObject.getNameAsConstant(PdfDictionary.H);
        if(key!=PdfDictionary.Unknown){
            /*
             * highlighting mode
             * done when the mouse is pressed or held down inside the fields active area
             * N nothing
             * I invert the contents
             * O invert the border
             * P display down appearance stream, or if non available offset the normal to look down
             * T same as P
             *
             * this overides the down appearance
             * Default value = I
             */
            if (key==PdfDictionary.T || key==PdfDictionary.P) {
                if (!formObject.hasDownImage()) {
                    formObject.setOffsetDownApp();
                }

            } else if (key==PdfDictionary.N) {
                //do nothing on press
                formObject.setNoDownIcon();

            } else if (key==PdfDictionary.I) {
                //invert the contents colors
                formObject.setInvertForDownIcon();

            }
        }

        //set Fonts

        final String textStream=formObject.getTextStreamValue(PdfDictionary.DA);

        if(textStream!=null){
            decodeFontCommandObj(textStream, formObject);
        }
    }

    /** set correct flags for AP images */
    private static void setupAPimages(final FormObject formObject, PdfFileReader pdfFileReader) {

        final PdfObject APobjN = formObject.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.N);
        //if valid AP, setup flags to show we use images
        if(APobjN!=null){

            final String ASvalue=formObject.getName(PdfDictionary.AS);
            
            formObject.setAppreancesUsed(true);

            final String key=(String) getNormalKeyValues(formObject, pdfFileReader)[0];
            
            if(key!=null){
                formObject.setNormalOnState(key);
            
                if (ASvalue != null && ASvalue.equals(key)) {
                    formObject.setSelected(true);
                }
            }
        }
    }

    /**
     * defines actions to be executed on events 'Trigger Events'
     *
     * @Action This is where the raw data is parsed and put into the FormObject
     */
    private void resolveAdditionalAction(final FormObject formObject) {

        /*
         * entries NP, PP, FP, LP never used
         * A action when pressed in active area ?some others should now be ignored?
         * E action when cursor enters active area
         * X action when cursor exits active area
         * D action when cursor button pressed inside active area
         * U action when cursor button released inside active area
         * Fo action on input focus
         * BI action when input focus lost
         * PO action when page containing is opened,
         * 	actions O of pages AA dic, and OpenAction in document catalog should be done first
         * PC action when page is closed, action C from pages AA dic follows this
         * PV action on viewing containing page
         * PI action when no longer visible in viewer
         * K action on - [javascript]
         * 	keystroke in textfield or combobox
         * 	modifys the list box selection
         * 	(can access the keystroke for validity and reject or modify)
         * F the display formatting of the field (e.g 2 decimal places) [javascript]
         * V action when fields value is changed [javascript]
         * C action when another field changes (recalculate this field) [javascript]
         */
        int idValue;

        for (final int anId : id) {

            //store most actions in lookup table to make code shorter/faster
            idValue = anId;

            currentPdfFile.setJavascriptForObject(formObject, PdfDictionary.AA, idValue);
            currentPdfFile.setJavascriptForObject(formObject, PdfDictionary.A, idValue);
        }
    }
    
    /**
     * decode appearance stream and convert into VectorRenderObject we can redraw
     * if width and height are 0 we define the size hear
     * offsetImage - 0= no change, 1= offset, 2= invert image
     * pScaling used by HTML - set to 1 otherwise
     */
    public static BufferedImage decode(final PdfObject formObj, final PdfObjectReader currentPdfFile, final PdfObject XObject, final int subtype,
    		int width, int height, final int offsetImage, final float pageScaling){
    	
        //handle XFA differently
        if(XObject.getObjectType()==PdfDictionary.XFA_APPEARANCE){
            
            return ExternalHandlers.decode(formObj, currentPdfFile, XObject, subtype, width, height, offsetImage, pageScaling);
        }
        
    	currentPdfFile.checkResolved(XObject);
    	try{
            
    		//create renderer object
    		org.jpedal.fonts.glyph.T3Glyph form = decodeStream(currentPdfFile, XObject);
            
            final float[] matrix=XObject.getFloatArray(PdfDictionary.Matrix);
            final float[] BBox=XObject.getFloatArray(PdfDictionary.BBox);
    		
            final float scaling;
    		
    		float rectX1=0,rectY1=0;
    		if(BBox!=null){

                for(int ii=0;ii<4;ii++) {
                    BBox[ii] *= pageScaling;
                }

    			rectX1 = (BBox[0]);
    			rectY1 = (BBox[1]);
                
                //Some files have fractions of a pixel in their size In some cases 
                //this needs to be ignored, other times it should be expanded to a full pixel
                //At the moment this is not required, this note is left as a reminder.
				int boxWidth=(int) (((BBox[2]+0.5f)-BBox[0]));
				if(boxWidth<0) {
                    boxWidth = -boxWidth;
                }
				int boxHeight=(int) (((BBox[3]+0.5f)-BBox[1]));
				if(boxHeight<0) {
                    boxHeight = -boxHeight;
                }

    			if(boxWidth==0 && boxHeight>0) {
                    boxWidth = 1;
                }
    			if(boxWidth>0 && boxHeight==0) {
                    boxHeight = 1;
                }
    			
    			//if the width and height scaling are miles apart then the width and height 
   				//are probably the wrong way round so swap them. and recalc the scalings.
    			float ws = width/((float)boxWidth);
				float hs = height/((float)boxHeight);
				
				//check if dimensions are correct and alter if not
				final float diff = ws-hs;
				final int diffInt = (int)diff;
				if(diffInt!=0){
					//NOTE width and height sent in need to be rotated
					//as they are not as the image is drawn
					final int tmpI = width;
					width = height;
					height = tmpI;
					
					ws = width/((float)boxWidth);
					hs = height/((float)boxHeight);
				}
				//NOTE now we re set the width and height to scaled 
				//value of Bounding box to keep the orientation
				
				//if scaling less than 1 use 1
				if(ws<1 || hs<1){
					scaling = 1;
					width = boxWidth;
					height = boxHeight;
				}else {
					//use larger scaling as will produce better image
					if(ws>hs){
						scaling = ws;
						height = (int) (boxHeight*scaling);
					}else {
						scaling = hs;
						width = (int) (boxWidth*scaling);
					}
					
					//make sure image position is scaled
					rectX1*=scaling;
					rectY1*=scaling;
				}
				
    		}else {

    			final float defaultSize = 20;
    			if(height<defaultSize) {
                    height = (int) defaultSize;
                }
    			if(width<defaultSize) {
                    width = (int) defaultSize;
                }
    			
				final float ws = width/defaultSize;
				final float hs = height/defaultSize;
				if(ws>hs){
					scaling = ws;
					height=(int)(defaultSize*scaling);
				}else {
					scaling = hs;
					width=(int)(defaultSize*scaling);
				}
				
				//make sure image position is scaled
				rectX1*=scaling;
				rectY1*=scaling;
    		}
    		
    		if(width==0 || height==0) {
                return null;
            }

    		//if offset
    		if(offsetImage==1){
    			width+=2;
    			height+=2;
    		}
    		
            final BufferedImage aa;
        
            if (matrix != null && matrix[2] != 0) {
                aa = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
            } else {
                aa = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);                
            }
        
    		final Graphics2D g2 = createGaphics(aa, formObj, matrix, BBox, pageScaling, scaling, rectX1, rectY1, width, height);

            if(offsetImage==2){//invert
                g2.scale(-1, -1);
            } else if(offsetImage==1){//offset
                g2.translate(1, 1);
            }
            
            //add transparency for highlights
            if(subtype==PdfDictionary.Highlight) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }

            //carry the sclaing through to the render method
            form.render(0,g2, scaling*pageScaling,true);

    		g2.dispose();

            return aa;

    	}catch(final Exception e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        
    		return null;
    	}catch(final Error e){
            LogWriter.writeLog("Error: " + e.getMessage());
            
            if (ExternalHandlers.throwMissingCIDError && e.getMessage()!=null && e.getMessage().contains("kochi")) {
                throw e;
            }

    		return null;
    	}
    }
    
    private static Graphics2D createGaphics(BufferedImage aa, PdfObject formObj, float[] matrix, float[] BBox, float pageScaling, float scaling, float transformOffsetX, float transformOffsetY, int width, int height) {
        
        final Graphics2D g2;
        int offset = height;
        
        if (matrix != null) {

            //Added for odd case 22179
            //pageScaling!=1 added to lock out of html as when not 1 html baseline is affected
            if (pageScaling == 1 && matrix[4] > 0 && matrix[5] > 0) {
                matrix = createMatrixFromBoundBoxes(BBox, formObj.getFloatArray(PdfDictionary.Rect));                
            } else {
                //scale so they offset correctly
                matrix[4] = matrix[4] * scaling * pageScaling;
                matrix[5] = matrix[5] * scaling * pageScaling;
            }
            
            if (matrix[2] != 0) {
                offset = width;
            } else {
                if (matrix[1] >= 0) {
                    //rectX1 and rectY1 already have the scaling applied
                    if (matrix[4] != 0f) {
                        matrix[4] = -transformOffsetX;
                    }
                    if (matrix[5] != 0f) {
                        matrix[5] = -transformOffsetY;
                    }
                }
                
            }
            
            g2 = (Graphics2D) aa.getGraphics();
            final AffineTransform flip = new AffineTransform();
            flip.translate(0, offset);
            flip.scale(1, -1);
            g2.setTransform(flip);
            
            if (debug) {
                System.out.println(" rectX1 = " + transformOffsetX + " rectY1 = " + transformOffsetY + " width = " + width + " height = " + height);
            }
            
            final AffineTransform affineTransform = new AffineTransform(matrix);
            g2.transform(affineTransform);
        } else {
            g2 = (Graphics2D) aa.getGraphics();
            
            final AffineTransform flip = new AffineTransform();
            flip.translate(0, offset);
            flip.scale(1, -1);
            g2.setTransform(flip);
            if (formObj.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Ink) {
                g2.translate(-(BBox[0] * scaling), -(BBox[3] * scaling));
            }
        }
        
        return g2;
    }
    
    private static float[] createMatrixFromBoundBoxes(final float[] BBox, final float[] BBox2) {

        final float[] matrix = new float[6];
        if (BBox2[1] > BBox2[3]) {
            final float t = BBox2[1];
            BBox2[1] = BBox2[3];
            BBox2[3] = t;
        }

        if (BBox2[0] > BBox2[2]) {
            final float t = BBox2[0];
            BBox2[0] = BBox2[2];
            BBox2[2] = t;
        }

        matrix[0] = (BBox2[2] - BBox2[0]) / (BBox[2] - BBox[0]);
        matrix[1] = 0;
        matrix[2] = 0;
        matrix[3] = (BBox2[3] - BBox2[1]) / (BBox[3] - BBox[1]);
        matrix[4] = (BBox2[0] - BBox[0]);
        matrix[5] = (BBox2[1] - BBox[1]);
        
        return matrix;
    }
    
    private static org.jpedal.fonts.glyph.T3Glyph decodeStream(final PdfObjectReader currentPdfFile, final PdfObject XObject) {

        //generate local object to decode the stream
        final ObjectStore localStore = new ObjectStore();

        final T3Renderer glyphDisplay = new T3Display(0, false, 20, localStore);

        final PdfStreamDecoder glyphDecoder = new PdfStreamDecoder(currentPdfFile);
        glyphDecoder.setParameters(false, true, 15, 0, false, false);

        glyphDecoder.setStreamType(ValueTypes.FORM);

        glyphDecoder.setObjectValue(ValueTypes.ObjectStore, localStore);

        glyphDecoder.setRenderer(glyphDisplay);

        /*read any resources*/
        try {
            final PdfObject Resources = XObject.getDictionary(PdfDictionary.Resources);

            if (Resources != null) {
                glyphDecoder.readResources(Resources, false);
            }

        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }

        final float[] BBox = XObject.getFloatArray(PdfDictionary.BBox);

        glyphDecoder.setBBox(BBox);
        /*decode the stream*/
        final byte[] commands = XObject.getDecodedStream();
        if (commands != null) {
            glyphDecoder.decodeStreamIntoObjects(commands, false);
        }

        final boolean ignoreColors = glyphDecoder.ignoreColors;

        localStore.flush();

        return new org.jpedal.fonts.glyph.T3Glyph(glyphDisplay, 0, 0, ignoreColors);

    }
    
    public static String decipherTextFromAP(final PdfObjectReader currentPdfFile, final PdfObject Xobject){
    	try{
	
			final ObjectStore localStore = new ObjectStore();
	
			/*
			 * create renderer object
			 */
			final T3Renderer glyphDisplay=new T3Display(0,false,20,localStore);
	
	   		/*
			 * generate local object to decode the stream
			 */

			final PdfStreamDecoder glyphDecoder=new PdfStreamDecoder(currentPdfFile,null);
            glyphDecoder.setParameters(false,true,15,0,false,false);

            glyphDecoder.setObjectValue(ValueTypes.ObjectStore,localStore);

			glyphDecoder.setRenderer(glyphDisplay);

			/*read any resources*/
			try{
	
				final PdfObject Resources =Xobject.getDictionary(PdfDictionary.Resources);
				if (Resources != null) {
                    glyphDecoder.readResources(Resources, false);
                }
	
			}catch(final Exception e){
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
	
	        /*decode the stream*/
			final byte[] commands=Xobject.getDecodedStream();
	
			String textString="";
	        if(commands!=null) {
                textString = glyphDecoder.decodeStreamIntoObjects(commands, true);
            }
	        if(textString==null || textString.isEmpty()) {
                textString = null;
            }
	
			localStore.flush();
			
			return textString;
    	}catch(final Exception e){
            LogWriter.writeLog("Exception: " + e.getMessage());
            
    		return null;
    	}catch(final Error e){
            LogWriter.writeLog("Error: " + e.getMessage());
            
            if (ExternalHandlers.throwMissingCIDError && e.getMessage()!=null && e.getMessage().contains("kochi")) {
                throw e;
            }
    		return null;
    	}
    }
    
    /**
     * method to rotate an image through a given angle
     * @param src the source image
     * @param rotation the angle to rotate the image through
     * @return the rotated image
     */
    public static BufferedImage rotate(final BufferedImage src, final int rotation) {
        BufferedImage dst;

        if(src == null) {
            return null;
        }

        //if angle is 0 we dont need to do anything
        if(rotation==0) {
            return src;
        }

        final double angle = rotation * Math.PI / 180;

        final int w = src.getWidth();
        final int h = src.getHeight();
        final int newW = (int)(Math.round(h * Math.abs(Math.sin(angle))+w * Math.abs(Math.cos(angle))));
        final int newH = (int)(Math.round(h * Math.abs(Math.cos(angle))+w * Math.abs(Math.sin(angle))));
        final AffineTransform at = AffineTransform.getTranslateInstance((newW-w)/2,(newH-h)/2);
        at.rotate(angle, w/2, h/2);

        dst = new BufferedImage(newW,newH,BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = dst.createGraphics();
        g2.drawRenderedImage(src, at);

        g2.dispose();

        return dst;
    }

    public boolean hasXFADataSet() {
        return false;
    }

    /**
     * takes the PDF commands and creates a font
     */
    public static void decodeFontCommandObj(final String fontStream, final FormObject formObject){

        //now parse the stream into a sequence of tokens
        final StringTokenizer tokens=new StringTokenizer(fontStream,"() []");
        final int tokenCount=tokens.countTokens();
        final String[] tokenValues=new String[tokenCount];
        int i=0;
        while(tokens.hasMoreTokens()){
            tokenValues[i]=tokens.nextToken();
            i++;
        }

        //now work out what it does and build up info
        for(i=tokenCount-1;i>-1;i--){
//			System.out.println(tokenValues[i]+" "+i);

            //look for commands
            if(tokenValues[i].equals("g")){ //set color (takes 1 values
                i--;
                float col=0;
                try{
                    col=Float.parseFloat(handleComma(tokenValues[i]));
                }catch(final Exception e){
                    LogWriter.writeLog("Error in generating g value "+tokenValues[i]+ ' ' +e);
                }

                formObject.setTextColor(new float[]{col});

            }else if(tokenValues[i].equals("Tf")){ //set font (takes 2 values - size and font
                i--;
                int textSize=8;
                try{
                    textSize=(int) Float.parseFloat(handleComma(tokenValues[i]));
//					if(textSize==0)
//						textSize = 0;//TODO check for 0 sizes CHANGE size to best fit on 0
                }catch(final Exception e){
                    LogWriter.writeLog("Error in generating Tf size "+tokenValues[i]+ ' ' +e);
                }

                i--;//decriment for font name
                String font=null;
                try{
                    font=tokenValues[i];
                    if(font.startsWith("/")) {
                        font = font.substring(1);
                    }
                }catch(final Exception e){
                    LogWriter.writeLog("Error in generating Tf font "+tokenValues[i]+"  "+e);
                }

                final PdfFont currentFont=new PdfFont();

                currentFont.setFont(font, textSize);
                
                String fontName=StandardFonts.expandName(font);
                
                final String altName= FontMappings.fontSubstitutionAliasTable.get(fontName.toLowerCase());
                if(altName!=null) {
                    fontName = altName;
                }
                
                formObject.setFontName(fontName);
                formObject.setTextFont(currentFont.getGlyphData().getUnscaledFont());

                formObject.setTextSize(textSize);

            }else if(tokenValues[i].equals("rg") || tokenValues[i].equals("r")){
                i--;
                final float b=Float.parseFloat(handleComma(tokenValues[i]));
                i--;
                final float g=Float.parseFloat(handleComma(tokenValues[i]));
                i--;
                final float r=Float.parseFloat(handleComma(tokenValues[i]));

                formObject.setTextColor(new float[]{r,g,b});

            }else if(tokenValues[i].equals("Sig")){
                LogWriter.writeFormLog("Sig-  UNIMPLEMENTED="+fontStream+"< "+i,debugUnimplemented);
            }else if(tokenValues[i].equals("\\n")){
                //ignore \n
                if(debug) {
                    System.out.println("ignore \\n");
                }
            }else {
                if(!showFontMessage){
                    showFontMessage=true;
                    LogWriter.writeFormLog("{stream} Unknown FONT command "+tokenValues[i]+ ' ' +i+" string="+fontStream,debugUnimplemented);
                }
            }
        }
    }

    private static String handleComma(String tokenValue) {

        //if comma used as full stop remove
        final int comma=tokenValue.indexOf(',');
        if(comma!=-1) {
            tokenValue = tokenValue.substring(0, comma);
        }

        return tokenValue;
    }

    @SuppressWarnings("UnusedDeclaration")
    public byte[] getXFA(final int xfaTemplate) {
        throw new RuntimeException("getXFA Should never be called in base class");
    }

    public boolean isXFA() {
        return isXFA;
    }
}

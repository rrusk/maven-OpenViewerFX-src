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
 * ImageDecoder.java
 * ---------------
 */
package org.jpedal.parser.image;

import com.idrsolutions.pdf.color.shading.BitReader;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.jpedal.color.*;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ErrorTracker;
import org.jpedal.external.ImageDataHandler;
import org.jpedal.external.ImageHandler;
import org.jpedal.images.ImageTransformer;
import org.jpedal.images.ImageTransformerDouble;
import org.jpedal.images.SamplingFactory;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.PdfImageData;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.BaseDecoder;
import org.jpedal.parser.ParserOptions;
import org.jpedal.parser.PdfObjectCache;
import org.jpedal.parser.ValueTypes;
import org.jpedal.parser.image.data.ImageData;
import org.jpedal.parser.image.downsample.DownSampler;
import org.jpedal.parser.image.mask.MaskDataDecoder;
import org.jpedal.parser.image.mask.MaskDecoder;
import org.jpedal.parser.image.mask.SMaskDecoder;
import org.jpedal.parser.image.utils.ConvertMaskToImage;
import org.jpedal.render.SwingDisplay;
import org.jpedal.utils.LogWriter;

public class ImageDecoder extends BaseDecoder{

    private boolean cacheLargeImages;
    
    //Allow print to use transparency in printing instead of removing it
    public static boolean allowPrintTransparency;
    
    protected ParserOptions parserOptions;

    final PdfImageData pdfImages;
    
    private boolean getSamplingOnly;
    
    //flag to show if image transparent*/
    boolean isMask=true;
    
    String imagesInFile;
    
    PdfObjectCache cache;
    
    final ImageHandler customImageHandler;
    
    final PdfPageData pageData;
    
    final ObjectStore objectStoreStreamRef;
    
    
    //name of current image in pdf
    String currentImage = "";
    
    final ErrorTracker errorTracker;
    
    final PdfObjectReader currentPdfFile;
    
    //images on page
    public final int imageCount;
    
    public ImageDecoder(final int imageCount, final PdfObjectReader currentPdfFile, final ErrorTracker errorTracker, final ImageHandler customImageHandler, final ObjectStore objectStoreStreamRef, final PdfImageData pdfImages, final PdfPageData pageData, final String imagesInFile) {
        
        this.imageCount=imageCount;
        
        this.currentPdfFile=currentPdfFile;
        this.errorTracker=errorTracker;
        
        this.customImageHandler=customImageHandler;
        this.objectStoreStreamRef=objectStoreStreamRef;
        
        this.pdfImages=pdfImages;
        this.pageData=pageData;
        
        this.imagesInFile=imagesInFile;
        
    }
    
    private GenericColorSpace setupXObjectColorspace(final PdfObject XObject, final ImageData imageData){
        
        final int width=imageData.getWidth();
        final int height=imageData.getHeight();
        final int depth=imageData.getDepth();
        
        final PdfObject ColorSpace=XObject.getDictionary(PdfDictionary.ColorSpace);
        
        //handle colour information
        GenericColorSpace decodeColorData=new DeviceRGBColorSpace();
        
        if(ColorSpace!=null){
            decodeColorData= ColorspaceFactory.getColorSpaceInstance(currentPdfFile, ColorSpace, cache.XObjectColorspaces);
            
            decodeColorData.setPrinting(parserOptions.isPrinting());
            
            //track colorspace use
            cache.put(PdfObjectCache.ColorspacesUsed, decodeColorData.getID(),"x");
            
            
//            if(depth==1 && decodeColorData.getID()== ColorSpaces.DeviceRGB && XObject.getDictionary(PdfDictionary.Mask)==null){
//                
//                final byte[] data=decodeColorData.getIndexedMap();
//                
//                //no index or first colour is white so use grayscale
//                if(decodeColorData.getIndexedMap()==null || (data.length==6 && data[0]==0 && data[1]==0 && data[2]==0)) {
//                    decodeColorData = new DeviceGrayColorSpace();
//                }
//            }
        }
        
        //fix for odd itext file (/PDFdata/baseline_screens/debug3/Leistung.pdf)
        final byte[] indexData=decodeColorData.getIndexedMap();
        if(depth==8){
            
            final byte[] objectData=imageData.getObjectData();
            
            if(indexData!=null && decodeColorData.getID()==ColorSpaces.DeviceRGB && width*height==objectData.length) {
                
                final PdfObject newMask = XObject.getDictionary(PdfDictionary.Mask);
                if (newMask != null) {
                    
                    final int[] maskArray = newMask.getIntArray(PdfDictionary.Mask);
                    
                    //this specific case has all zeros
                    if (maskArray != null && maskArray.length == 2 && maskArray[0] == 255 && maskArray[0] == maskArray[1] && decodeColorData.getIndexedMap() != null && decodeColorData.getIndexedMap().length == 768) {
                        
                        //see if index looks corrupt (ie all zeros) We exit as soon as we have disproved
                        boolean isCorrupt = true;
                        for (int jj = 0; jj < 768; jj++) {
                            if (indexData[jj] != 0) {
                                isCorrupt = false;
                                jj = 768;
                            }
                        }
                        
                        if (isCorrupt) {
                            decodeColorData = new DeviceGrayColorSpace();
                        }
                    }
                }
            }
        }
        
        //pass through decode params
        final PdfObject parms=XObject.getDictionary(PdfDictionary.DecodeParms);
        if(parms!=null) {
            decodeColorData.setDecodeParms(parms);
        }
        
        return decodeColorData;
    }
    
    public BufferedImage processImageXObject(PdfObject XObject, String image_name, byte[] objectData, final String details) throws PdfException {
        
        BufferedImage image=null;
        
        //add filename to make it unique
        image_name = parserOptions.getFileName()+ '-' + image_name;
        
//          System.out.println("XObject="+XObject+" "+XObject.getObjectRefAsString());
        PdfObject newSMask=XObject.getDictionary(PdfDictionary.SMask);
        final PdfObject newMask=XObject.getDictionary(PdfDictionary.Mask);
        ImageData imageData=new ImageData(XObject, objectData);
        imageData.getFilter(XObject);
        GenericColorSpace decodeColorData = setupXObjectColorspace(XObject, imageData);
        imageData.setCompCount(decodeColorData.getColorSpace().getNumComponents());
              
        /*
         * New code to apply SMask and Mask to data
         * (note old isMask)
         */
        byte[] convertedData=XObject.getConvertedData();
        
        if(convertedData!=null){ //reuse converted mask data
            objectData=convertedData;
            decodeColorData=new DeviceRGBColorSpace();
            //imageData.setObjectData(objectData); 
            imageData=null;
        }else if(newSMask!=null || newMask!=null){
            
            if(newSMask!=null && XObject.getInt(PdfDictionary.Width)==1 && XObject.getInt(PdfDictionary.Height)==1 && XObject.getInt(PdfDictionary.BitsPerComponent)==8){ //swap out the image with inverted SMask if empty
               
                //silly case we handle in code below // /baseline_screens/11dec/grayscale.pdf
               
            }else{
                
                //WE NEED TO CONVERT JPG to raw DATA in IMAGE
                if(imageData.isDCT()){
                    objectData=JPEGDecoder.getBytesFromJPEG(objectData,decodeColorData,XObject);
                    imageData.setObjectData(objectData);
                    XObject.setMixedArray(PdfDictionary.Filter,null);
                    XObject.setDecodedStream(objectData);
                }else if(imageData.isJPX()){
                    objectData=JPeg2000ImageDecoder.getBytesFromJPEG2000(objectData);
                    if(decodeColorData.getID() == ColorSpaces.DeviceN){
                        objectData = ((DeviceNColorSpace)decodeColorData).getRGBBytes(objectData, imageData.getWidth(), imageData.getHeight());
                    }
                    imageData.setObjectData(objectData);
                    XObject.setMixedArray(PdfDictionary.Filter,null);
                    XObject.setDecodedStream(objectData);       
                    decodeColorData=new DeviceRGBColorSpace();
                }
                
                if(newSMask!=null){
                    ///WE NEED TO CONVERT JPG to raw DATA in smask as well
                    ImageData smaskImageData=new ImageData(newSMask, null);
                    smaskImageData.getFilter(newSMask);
                    GenericColorSpace maskColorSpace = setupXObjectColorspace(newSMask, smaskImageData);
                    byte[] maskData =currentPdfFile.readStream(newSMask,true,true,false, false,false, newSMask.getCacheName(currentPdfFile.getObjectReader()));
                    
                              
                    if(1==1){
                        objectData =  SMaskDecoder.applyJPX_JBIG_Smask(imageData, smaskImageData, maskData,XObject, newSMask, decodeColorData, maskColorSpace);                        
                    }else{ // old method
                        maskData = MaskDataDecoder.getSMaskData(maskData,smaskImageData, newSMask,setupXObjectColorspace(newSMask, smaskImageData));
                        objectData=SMaskDecoder.applySMask(maskData,imageData,decodeColorData, newSMask,XObject);
                    }
                    
                    XObject.setConvertedData(objectData);              
                    
                }else{ //mask
                    
                   byte[] index=decodeColorData.getIndexedMap();
                   int[] maskArray=newMask.getIntArray(PdfDictionary.Mask);
       
                    if(index!=null){
                        index=decodeColorData.convertIndexToRGB(index);
                        
                        if(maskArray!=null){
                            return getIndexedMaskImage(index, imageData, maskArray);
                        }
                        
                        objectData=ColorSpaceConvertor.convertIndexToRGBByte(index, imageData.getWidth(), imageData.getHeight(), imageData.getCompCount(), imageData.getDepth(), objectData, false, false);
                        decodeColorData=new DeviceRGBColorSpace();
                        imageData.setObjectData(objectData);
                        decodeColorData.setIndex(null, 0);
                       // imageData.setCompCount(3);
                      //  imageData.setDepth(8);
                    }
                    ///WE NEED TO CONVERT JPG to raw DATA in mask as well
                    ImageData maskImageData=new ImageData(newMask, objectData);
                    if(maskArray!=null){
                        return MaskDataDecoder.applyMaskArray(imageData, maskArray);
                    }                        
                    
                    byte[] maskData= currentPdfFile.readStream(newMask, true, true, false, false, false, newMask.getCacheName(currentPdfFile.getObjectReader()));
                    
                    maskData = MaskDataDecoder.getSMaskData(maskData,maskImageData, newMask,setupXObjectColorspace(newMask, maskImageData));
                                        
                    objectData=MaskDecoder.applyMask(imageData,decodeColorData,newMask,XObject,maskData);
                        
                    XObject.setConvertedData(objectData);
                    decodeColorData=new DeviceRGBColorSpace();
                        
                    XObject.setDictionary(PdfDictionary.Mask, null);
                    
                }
                //  String dest="/Users/markee/Desktop/deviceRGB/"+org.jpedal.DevFlags.currentFile.substring(org.jpedal.DevFlags.currentFile.lastIndexOf("/"));
                //  ObjectStore.copy(org.jpedal.DevFlags.currentFile, dest);
                
                //also set SMask to null (will set image to DeviceRGB below
                //XObject.setDictionary(PdfDictionary.SMask, null);
                imageData=null;
            }    
        }
        
        //reset if changed in Mask/SMask code
        if(imageData==null){
            imageData=new ImageData(XObject, objectData);
            
            decodeColorData = new DeviceRGBColorSpace(true); //sets to 4 comp ARGB
            imageData.setCompCount(4);
            
            newSMask=null;
        }
        
        isMask= XObject.getBoolean(PdfDictionary.ImageMask);
        
        LogWriter.writeLog("Processing XObject: " + image_name + ' ' + XObject.getObjectRefAsString() + " width=" + imageData.getWidth() + " Height=" + imageData.getHeight() +
                    " Depth=" + imageData.getDepth() + " colorspace=" + decodeColorData);
        
        //allow user to process image
        if(customImageHandler != null && !(customImageHandler instanceof ImageDataHandler)){
            image= customImageHandler.processImageData(gs,XObject); //user gets raw JPEG data
        }
        
        //deal with special case of 1x1 pixel backed onto large inverted Smask which would be very slow in Generic code
        //see (11dec/grayscale.pdf)
        if(newSMask!=null && XObject.getInt(PdfDictionary.Width)==1 && XObject.getInt(PdfDictionary.Height)==1 && XObject.getInt(PdfDictionary.BitsPerComponent)==8){ //swap out the image with inverted SMask if empty
            
            image = ConvertMaskToImage.convert(newSMask, currentPdfFile);
            
        }else if(customImageHandler==null ||(image==null && !customImageHandler.alwaysIgnoreGenericHandler())) {
            image = processImage(decodeColorData, imageData, isMask, XObject);
            
            if(image!=null && !current.isHTMLorSVG() && !parserOptions.renderDirectly() && parserOptions.isPageContent() && parserOptions.imagesNeeded()) {
                objectStoreStreamRef.saveStoredImage(image_name,ImageCommands.addBackgroundToMask(image, isMask),false,parserOptions.createScaledVersion(),"tif");          
            }
        
        }
        
        //add details to string so we can pass back
        if(ImageCommands.trackImages && image!=null && details!=null){
            setImageInfo(imageData, details, decodeColorData, image);
        }
        
        return image;
        
    }
    
    private static BufferedImage getIndexedMaskImage(byte[] index, ImageData imageData, int[] maskArray) {
        int d = imageData.getDepth();
        int p = 0;
        int c = 0;

        boolean[] invisible = new boolean[1 << d];       
        
        for (int i = 0; i < maskArray.length; i+=2) {
            int start = maskArray[i];
            int end = maskArray[i+1];
            
            if(start==end){
                invisible[start] = true;
            }else{
                for (int j = start; j < end; j++) {
                    invisible[j] = true;
                }
            }
        }       

        int[] indexColors = new int[index.length / 3];

        for (int i = 0; i < indexColors.length; i++) {
            indexColors[i] = (255 << 24) | ((index[c++] & 0xff) << 16) | ((index[c++] & 0xff) << 8) | (index[c++] & 0xff);
        }

        BitReader reader = new BitReader(imageData.getObjectData(), d < 8);

        BufferedImage img = new BufferedImage(imageData.getWidth(), imageData.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int output[] = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

        int imageDim = imageData.getWidth() * imageData.getHeight();
        int w = imageData.getWidth();
        int wc = 0;
        for (int i = 0; i < imageDim; i++) {
            int v = reader.getPositive(d);
            if(!invisible[v]){
                output[p++] =  indexColors[v];
            }else{
                p++;
            }
            wc++;
            if (wc == w) {
                int balance = 8 - (reader.getPointer() % 8);
                wc = 0;
                if (balance != 8) {
                    reader.getPositive(balance);
                }
            }
        }
        return img;
    } 

    private void setImageInfo(final ImageData imageData, final String details, GenericColorSpace decodeColorData, BufferedImage image) {
        
        final int width=imageData.getWidth();
        final int height=imageData.getHeight();
        
        //work out effective dpi
        float dpi = gs.CTM[0][0];
        if(dpi ==0) {
            dpi = gs.CTM[0][1];
        }
        if(dpi <0) {
            dpi = -dpi;
        }
        
        dpi =(int)(width/dpi*100);
        
        //add details to string
        final StringBuilder imageInfo=new StringBuilder(details);
        imageInfo.append(" w=");
        imageInfo.append(width);
        imageInfo.append(" h=");
        imageInfo.append(height);
        imageInfo.append(' ');
        imageInfo.append((int) dpi);
        imageInfo.append(' ');
        imageInfo.append(ColorSpaces.IDtoString(decodeColorData.getID()));
        
        imageInfo.append(" (");
        imageInfo.append(image.getWidth());
        imageInfo.append(' ');
        imageInfo.append(image.getHeight());
        imageInfo.append(" type=");
        imageInfo.append(image.getType());
        imageInfo.append(')');
        
        if(imagesInFile.isEmpty()) {
            imagesInFile = imageInfo.toString();
        } else {
            imageInfo.append('\n');
            imageInfo.append(imagesInFile);
            imagesInFile=imageInfo.toString();
        }
    }
    
    public void setSamplingOnly(final boolean getSamplingOnly){
        
        this.getSamplingOnly=getSamplingOnly;
        
    }
    
    public String getImagesInFile() {
        return this.imagesInFile;
    }
    
    @Override
    public void setParams(final ParserOptions parserOptions) {
        
        this.parserOptions=parserOptions;
        
        
        //Set flag to allow tunring on/off transparency optimisations in printing
        String value = System.getProperty("org.jpedal.printTransparency");
        if(value!=null){
            ImageDecoder.allowPrintTransparency = parserOptions.isPrinting() && value.equalsIgnoreCase("true");
        }
        
        //Set flag to allow tunring on/off transparency optimisations in printing
        value = System.getProperty("org.jpedal.viewerLargeImageCaching");
        if(value!=null){
            cacheLargeImages = value.equalsIgnoreCase("true");
        }
    }
    
    /**
     * save the current image, clipping and
     *  resizing. This gives us a
     * clipped hires copy. In reparse, we don't
     * need to repeat some actions we know already done.
     */
    public void generateTransformedImage(BufferedImage image, final String image_name) {
        
        float x = 0;
        float y = 0;
        final float w;
        final float h;
        
        //if valid image then process
        //for the moment lock out rotated images as code broken (applies rotated clip to unrotated image)
        if (image != null){
            
            ImageTransformerDouble image_transformation=null;
            
            final boolean isHTML=current.isHTMLorSVG();
            
            if(isHTML) {
                current.drawImage(parserOptions.getPageNumber(), image, gs, false, image_name, -3);
            }
            
            /*
             * scale the raw image to correct page size (at 72dpi)
             */
            
            //this code is generic code being reused for HTML so breaks on several cases which we just lock out (ie rotation)
            final int pageRotation=pageData.getRotation(parserOptions.getPageNumber());
                
            //if down-sampling image, we use larger size in HTML
            //to preserve image quality. ie if image is 1200x1200 for 600x600 hole
            //(which is 900x900 when we scale up again, we keep image as 900x900 not 600x600
            //if image is already 600x600 we DO NOT upscale
            //still needs to be debugged (especially rotated pages)

            //object to scale and clip. Creating instance does the scaling
            if(!isHTML){
                image_transformation = new ImageTransformerDouble(gs, image, parserOptions.createScaledVersion(), 1, pageRotation);
            }

            //extract images either scaled/clipped or scaled then clipped
            if(image_transformation!=null){
                image_transformation.doubleScaleTransformShear();

                //get intermediate image and save
                image = image_transformation.getImage();
            }
            
            
            //save the scaled/clipped version of image if allowed
            if(!isHTML){
                
                String image_type = objectStoreStreamRef.getImageType(currentImage);
                if(image_type==null) {
                    image_type = "tif";
                }
                
                BufferedImage outputImage=image;
                
                //convert mask into proper image if saving clipped images
                if(isMask){
                    
                    final int foreground = gs.nonstrokeColorSpace.getColor().getRGB();
                    
                    final int[] maskCol = new int[4];
                    
                    maskCol[0] = ((foreground >> 16) & 0xFF);
                    maskCol[1] = ((foreground >> 8) & 0xFF);
                    maskCol[2] = ((foreground) & 0xFF);
                    maskCol[3] = 255;
                    
                    final BufferedImage img = new BufferedImage(outputImage.getWidth(), outputImage.getHeight(), outputImage.getType());
                    
                    final Raster src = outputImage.getRaster();
                    final WritableRaster dest = img.getRaster();
                    final int[] values = new int[4];
                    for (int yy = 0; yy < outputImage.getHeight(); yy++) {
                        for (int xx = 0; xx < outputImage.getWidth(); xx++) {
                            
                            //get raw color data
                            src.getPixel(xx, yy, values);
                            
                            //System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]+" ");
                            //if not transparent, fill with color
                            if (values[3] > 2) {
                                dest.setPixel(xx, yy, maskCol);
                            }
                        }
                    }
                    outputImage = img;
                    
                }
                
                if(objectStoreStreamRef.saveStoredImage(
                        "CLIP_"+currentImage,
                        outputImage,
                        false,
                        false,
                        image_type)) {
                    errorTracker.addPageFailureMessage("Problem saving " + image);
                }
            }
            
            /*
             * HTML5/JavaFX code is piggybacking on existing functionality to extract hires clipped image.
             * We do not need other functionality so we ignore that
             */
            if(isHTML){
                
                if (image != null) {
                    
                    gs.x=x;
                    gs.y=y;
                    
                    current.drawImage(parserOptions.getPageNumber(),image,gs,false,image_name, -2);
                    
                }
            }else{
                
                if(parserOptions.isFinalImagesExtracted() || parserOptions.renderImages()) {
                    image_transformation.doubleScaleTransformScale();
                }
                
                //complete the image and workout co-ordinates
                image_transformation.completeImage();
                
                //get initial values
                x = image_transformation.getImageX();
                y = image_transformation.getImageY();
                w = image_transformation.getImageW();
                h = image_transformation.getImageH();
                
                //get final image to allow for way we draw 'upside down'
                image = image_transformation.getImage();
                
                //allow for null image returned (ie if too small)
                if (image != null) {
                    
                    //store  final image on disk & in memory
                    if(parserOptions.imagesNeeded()){
                        pdfImages.setImageInfo(currentImage, parserOptions.getPageNumber(), x, y, w, h);
                    }
                    
                    //add to screen being drawn
                    if ((parserOptions.renderImages() || !parserOptions.isPageContent())) {
                        gs.x=x;
                        gs.y=y;
                        current.drawImage(parserOptions.getPageNumber(),image,gs,false,image_name, -1);
                    }
                    
                    //save if required
                    if((!parserOptions.renderDirectly() && parserOptions.isPageContent() && parserOptions.isFinalImagesExtracted()) &&
                            
                            //save the scaled/clipped version of image if allowed
                            (ImageCommands.isExtractionAllowed(currentPdfFile))){
                        String image_type = objectStoreStreamRef.getImageType(currentImage);
                        if(image_type==null) //Fm can generate null value
                        {
                            image_type = "jpg";
                        }
                        
                        objectStoreStreamRef.saveStoredImage(
                                currentImage,
                                ImageCommands.addBackgroundToMask(image, isMask),
                                false,
                                false,
                                image_type);
                        
                    }
                }
            }
        } else{ 
            //flag no image and reset clip
            LogWriter.writeLog("NO image written");
        }    
    }
    
    /**
     * save the current image, clipping and resizing.Id reparse, we don't
     * need to repeat some actions we know already done.
     * @param image
     */
    public void generateTransformedImageSingle(BufferedImage image, final String image_name) {
        
        float x, y, w, h;
        
        //if valid image then process
        if (image != null) {
            
            // get clipped image and co-ords
            final Area clipping_shape = gs.getClippingShape();
            
            /*
             * scale the raw image to correct page size (at 72dpi)
             */
            
            //object to scale and clip. Creating instance does the scaling
            final ImageTransformer image_transformation;
            
            //object to scale and clip. Creating instance does the scaling
            image_transformation =new ImageTransformer(gs,image);
            
            //get initial values
            x = image_transformation.getImageX();
            y = image_transformation.getImageY();
            w = image_transformation.getImageW();
            h = image_transformation.getImageH();
            
            //get back image, which will become null if TOO small
            image = image_transformation.getImage();
            
            //apply clip as well if exists and not inline image
            if (image != null && customImageHandler!=null && clipping_shape != null && clipping_shape.getBounds().getWidth()>1 &&
                    clipping_shape.getBounds().getHeight()>1 && !customImageHandler.imageHasBeenScaled()) {
                
                //see if clip is wider than image and ignore if so
                final boolean ignore_image = clipping_shape.contains(x, y, w, h);
                
                if (!ignore_image) {
                    //do the clipping
                    image_transformation.clipImage(clipping_shape);
                    
                    //get ALTERED values
                    x = image_transformation.getImageX();
                    y = image_transformation.getImageY();
                    w = image_transformation.getImageW();
                    h = image_transformation.getImageH();
                }
            }
            
            //alter image to allow for way we draw 'upside down'
            image = image_transformation.getImage();
            
            //allow for null image returned (ie if too small)
            if (image != null) {
                
                //store  final image on disk & in memory
                if(parserOptions.isFinalImagesExtracted() || parserOptions.isRawImagesExtracted()){
                    pdfImages.setImageInfo(currentImage, parserOptions.getPageNumber(), x, y, w, h);
                    
                    //					if(includeImagesInData){
                    //
                    //						float xx=x;
                    //						float yy=y;
                    //
                    //						if(clipping_shape!=null){
                    //
                    //							int minX=(int)clipping_shape.getBounds().getMinX();
                    //							int maxX=(int)clipping_shape.getBounds().getMaxX();
                    //
                    //							int minY=(int)clipping_shape.getBounds().getMinY();
                    //							int maxY=(int)clipping_shape.getBounds().getMaxY();
                    //
                    //							if((xx>0 && xx<minX)||(xx<0))
                    //								xx=minX;
                    //
                    //							float currentW=xx+w;
                    //							if(xx<0)
                    //								currentW=w;
                    //							if(maxX<(currentW))
                    //								w=maxX-xx;
                    //
                    //							if(yy>0 && yy<minY)
                    //								yy=minY;
                    //
                    //							if(maxY<(yy+h))
                    //								h=maxY-yy;
                    //
                    //						}
                    //
                    //						pdfData.addImageElement(xx,yy,w,h,currentImage);
                    //					}
                }
               
                //save if required
                if((parserOptions.isPageContent() && parserOptions.isFinalImagesExtracted()) &&
                        
                        //save the scaled/clipped version of image if allowed
                        (ImageCommands.isExtractionAllowed(currentPdfFile))){
                    
                    final String image_type = objectStoreStreamRef.getImageType(currentImage);
                    objectStoreStreamRef.saveStoredImage(
                            currentImage,
                            ImageCommands.addBackgroundToMask(image, isMask),
                            false,
                            false,
                            image_type);
                    
                }
            }
        } else {
            LogWriter.writeLog("NO image written");
        }
    }
    
    /**
     * read in the image and process and save raw image
     */
    BufferedImage processImage(GenericColorSpace decodeColorData,
            final ImageData imageData, final boolean imageMask,
            final PdfObject XObject) throws PdfException {
        
        //track its use
        cache.put(PdfObjectCache.ColorspacesUsed, decodeColorData.getID(), "x");
       
        imageData.getFilter(XObject);
        
        BufferedImage image = null;//
        
        //allow user to process image
        if(customImageHandler instanceof ImageDataHandler){
            image = customImageHandler.processImageData(gs, XObject);
        } else if (imageData.isJPX()) {
            removeJPXEncodingFromImageData(imageData, XObject);
        } else if(imageData.isDCT()) {
                
            int adobeColorTransform = 1;
            PdfObject decodeParms=XObject.getDictionary(PdfDictionary.DecodeParms);
            if(decodeParms!=null){
                adobeColorTransform = decodeParms.getInt(PdfDictionary.ColorTransform);
            }

            byte[] objectData;
            try {
                objectData = JPEGDecoder.getUnconvertedBytesFromJPEG(imageData.getObjectData(), adobeColorTransform);
                if (objectData == null) { //fix for lgpl version
                    objectData = JPEGDecoder.getBytesFromJPEGWithImageIO(imageData.getObjectData(), decodeColorData, XObject);
                    decodeColorData = new DeviceRGBColorSpace();
                    imageData.setCompCount(3);
                    imageData.setDecodeArray(null);
                }
            } catch (Exception e) {

                LogWriter.writeLog("[PDF] Exception " + e + " Processing JPEG data in ImageDecoder");

                objectData = JPEGDecoder.getBytesFromJPEGWithImageIO(imageData.getObjectData(), decodeColorData, XObject);
                decodeColorData = new DeviceRGBColorSpace();
                imageData.setCompCount(3);
                imageData.setDecodeArray(null);
            }
            XObject.setMixedArray(PdfDictionary.Filter, null);
            XObject.setDecodedStream(objectData);
            imageData.setObjectData(objectData);
            imageData.setDCT(false);
            imageData.setDepth(8);
            
            imageData.wasDCT(true);
        }
        
        if(image!=null){
            return image;
        }else{
            return convertDataToImage(imageMask, imageData, XObject, decodeColorData);
        }
    }

    static void removeJPXEncodingFromImageData(final ImageData imageData, final PdfObject XObject) {
        
        final byte[] objectData = JPeg2000ImageDecoder.getUnconvertedBytesFromJPEG2000(imageData.getObjectData());
        imageData.setObjectData(objectData);
        XObject.setMixedArray(PdfDictionary.Filter, null);
        XObject.setDecodedStream(objectData);
        imageData.setDepth(8);
        imageData.setIsJPX(false);
    }

    private BufferedImage convertDataToImage(final boolean imageMask, final ImageData imageData, final PdfObject XObject, GenericColorSpace decodeColorData) {
        
        int sampling=1;
        
        BufferedImage image;
        
        //setup any imageMask
        byte[] maskCol =null;
        if (imageMask) {
            maskCol=ImageCommands.getMaskColor(gs);
        }

        //setup sub-sampling
        if(parserOptions.isRenderPage() && streamType!= ValueTypes.PATTERN){
            setDownsampledImageSize(imageData, XObject,multiplyer,decodeColorData);
        }

        //down-sample size if displaying (some cases excluded at present)
        if(parserOptions.isRenderPage() &&
                decodeColorData.getID()!=ColorSpaces.ICC &&
                imageData.getMode()!=ImageCommands.ID &&
                (imageData.getDepth()==1 || imageData.getDepth()==8)
                && imageData.getpX()>0 && imageData.getpY()>0 && (SamplingFactory.isPrintDownsampleEnabled || !parserOptions.isPrinting())){

            sampling=setSampling(imageData, decodeColorData);

            if(sampling>1 && multiplyer>1){
                sampling = (int) (sampling/ multiplyer);
            }
        }

        //get sampling and exit from this code as we don't need to go further
        if(getSamplingOnly){

            int w=imageData.getWidth();
            int h=imageData.getHeight();

            if(imageData.getpX()>0 && imageData.getpY()>0){
                final float scaleX=(((float)w)/imageData.getpX());
                final float scaleY=(((float)h)/imageData.getpY());

                if(scaleX>100 || scaleY>100){
                    //ignore 
                }else if(scaleX<scaleY){
                    parserOptions.setSamplingUsed(scaleX);
                }else{
                    parserOptions.setSamplingUsed(scaleY);
                }
            }
            return null;
        }
        
        //handle any decode array
        final float[] decodeArray=imageData.getDecodeArray();
        if(decodeArray!=null && decodeArray.length > 0 && decodeColorData.getIndexedMap()==null){ //for the moment ignore if indexed (we may need to recode)
          ImageCommands.applyDecodeArray(imageData.getObjectData(), imageData.getDepth(), decodeArray,decodeColorData.getID());
          
        }
        
        //apply any transfer function directly to data (does not work on DCT data)
        final PdfObject TR=gs.getTR();
        if(TR!=null){ //array of values
            ImageCommands.applyTR(imageData, TR, currentPdfFile);
        }     
        

        //switch to 8 bit and reduce bw image size by averaging
        if(sampling>1 && !imageData.wasDCT()){

            //choose whether we cache raw data so we can redecode images at different resolutions in Viewer
            if(cacheLargeImages && decodeColorData.getIndexedMap()==null){

                decodeColorData.dataToRGBByteArray(imageData.getObjectData(), imageData.getWidth(), imageData.getHeight());
                
                if(SwingDisplay.testSampling){
                    System.out.println("cached image full size= "+imageData.getWidth()+", "+imageData.getHeight()+" Bits="+imageData.getDepth()+
                            " "+decodeColorData+" count="+imageData.getCompCount()+" bytes="+imageData.getObjectData().length);
                }
                
                objectStoreStreamRef.saveRawImageData(parserOptions.getPageNumber() + String.valueOf(imageCount),imageData.getObjectData(),
                        imageData.getWidth(),imageData.getHeight(),imageData.getDepth(), imageData.getpX(), imageData.getpY(),
                        maskCol,ColorSpaces.DeviceRGB);
        
            }

            decodeColorData = DownSampler.downSampleImage(decodeColorData, imageData, maskCol, sampling);
        }
        
        
        if (maskCol!=null) {
            image = ImageDataToJavaImage.makeMaskImage(parserOptions, gs, current,imageData, decodeColorData, maskCol);           
        } else { //handle other types

            LogWriter.writeLog( imageData.getWidth() + "W * " + imageData.getHeight() + "H BPC=" + imageData.getDepth() + ' ' + decodeColorData);

            image =ImageDataToJavaImage.makeImage(decodeColorData,imageData);

        }

        if(image == null && !imageData.isRemoved()){
            parserOptions.imagesProcessedFully=false;
        }

        if (maskCol!=null && gs.nonstrokeColorSpace.getColor().isTexture()) {  //case 19095 vistair

            float mm[][] = gs.CTM;
            int w=imageData.getWidth();
            int h=imageData.getHeight();

            AffineTransform affine = new AffineTransform(mm[0][0], mm[0][1], mm[1][0], mm[1][1], mm[2][0], mm[2][1]);

            BufferedImage temp = ((PatternColorSpace)gs.nonstrokeColorSpace).getRawImage(affine);
            BufferedImage scrap = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            if(temp!=null){
                TexturePaint tp = new TexturePaint(temp, new Rectangle(0,0,temp.getWidth(),temp.getHeight()));
                Graphics2D g2 = scrap.createGraphics();
                g2.setPaint(tp);
                Rectangle rect = new Rectangle(0,0,w,h);
                g2.fill(rect);
            }

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (image.getRGB(x, y) == -16777216) { //255 0 0 0
                        int pRGB = scrap.getRGB(x, y);
                        image.setRGB(x, y, pRGB);
                    }
                }
            }
        }
        
        return image;
    }

    private int setSampling(final ImageData imageData, GenericColorSpace decodeColorData) {
        
        //see what we could reduce to and still be big enough for page
        int sampling=1;
        
        int w=imageData.getWidth();
        int h=imageData.getHeight();
        
        int newW=w;
        int newH=h;
        
        int pX=imageData.getpX();
        int pY=imageData.getpY();
        //limit size (allow bigger grayscale
        if(multiplyer<=1 && !parserOptions.isPrinting()){
            
            int maxAllowed=1000;
            if(decodeColorData.getID()==ColorSpaces.DeviceGray){
                maxAllowed=4000;
            }
            if(pX>maxAllowed) {
                pX = maxAllowed;
            }
            if(pY>maxAllowed) {
                pY = maxAllowed;
            }
        }
        final int smallestH=pY<<2; //double so comparison works
        final int smallestW=pX<<2;
        //cannot be smaller than page
        while(newW>smallestW && newH>smallestH){
            sampling <<= 1;
            newW >>= 1;
            newH >>= 1;
        }
        int scaleX=w/pX;
        if(scaleX<1) {
            scaleX = 1;
        }
        int scaleY=h/pY;
        if(scaleY<1) {
            scaleY = 1;
        }
        //choose smaller value so at least size of page
        sampling=scaleX;
        if(sampling>scaleY) {
            sampling = scaleY;
        }
        imageData.setpX(pX);
        imageData.setpY(pY);
        return sampling;
    }
    
    
    private void setDownsampledImageSize(final ImageData imageData, final PdfObject XObject, final float multiplyer, final GenericColorSpace decodeColorData) {
        
        int w=imageData.getWidth();
        int h=imageData.getHeight();
        
        if(parserOptions.isPrinting() && SamplingFactory.isPrintDownsampleEnabled && w<4000){
            imageData.setpX(pageData.getCropBoxWidth(parserOptions.getPageNumber())*4);
            imageData.setpY(pageData.getCropBoxHeight(parserOptions.getPageNumber())*4);
            
        }else if(SamplingFactory.downsampleLevel== SamplingFactory.high || getSamplingOnly){// && w>500 && h>500){ // ignore small items
            
            //ensure all positive for comparison
            final float[][] CTM=new float[3][3];
            for(int ii=0;ii<3;ii++){
                for(int jj=0;jj<3;jj++){
                    if(gs.CTM[ii][jj]<0) {
                        CTM[ii][jj] = -gs.CTM[ii][jj];
                    } else {
                        CTM[ii][jj] = gs.CTM[ii][jj];
                    }
                }
            }
            
            if(CTM[0][0]==0 || CTM[0][0]<CTM[0][1]) {
                imageData.setpX((int) (CTM[0][1]));
            } else {
                imageData.setpX((int) (CTM[0][0]));
            }
            
            if(CTM[1][1]==0 || CTM[1][1]<CTM[1][0]) {
                imageData.setpY( (int) (CTM[1][0]));
            } else {
                imageData.setpY((int) (CTM[1][1]));
            }
            
            //don't bother on small itemsS
            if(!getSamplingOnly &&(w<500 || (h<600 && w<1000) || imageData.isJPX())){ //change??
                imageData.setpX(0);
                imageData.setpX(0);
                
            }
            
        }else if(SamplingFactory.downsampleLevel==SamplingFactory.medium){
            imageData.setpX(pageData.getCropBoxWidth(parserOptions.getPageNumber()));
            imageData.setpY(pageData.getCropBoxHeight(parserOptions.getPageNumber()));
        }
        
        /*
         * turn off all scaling and allow user to control if switched off or HTML/svg/JavaFX
         * (we still trap very large images in these cases as they blow away the
         * memory footprint)
         */
        final int maxHTMLImageSize=4000;
        if((w<maxHTMLImageSize && h<maxHTMLImageSize &&
                current.isHTMLorSVG() &&
                (imageData.getDepth()!=1 || XObject.getRawObjectType()!=PdfDictionary.Mask))){
            imageData.setpX(-1);
            imageData.setpY(1);
        }
        
        //needs to be factored in or images poor on hires modes
        if((imageData.isDCT() || imageData.isJPX()) && multiplyer>1){
            imageData.setpX((int) (imageData.getpX()* multiplyer));
            imageData.setpY((int) (imageData.getpX()* multiplyer));
        }
        
        //avoid for scanned text
        if(imageData.getDepth()==1 && (XObject.getObjectType()!=PdfDictionary.Mask) &&
                decodeColorData.getID()== ColorSpaces.DeviceGray && imageData.getHeight()<300){
            
            imageData.setpX(0);
            imageData.setpY(0);
        }
    } 
    
    
    
    public void setRes(final PdfObjectCache cache) {
        this.cache=cache;
    }
    
    public int processImage(final String s, final int dataPointer, final PdfObject xObject)throws Exception {
        return 0;
    }
    
    public int processImage(final int dataPointer, final int startInlineStream, final byte[] stream, final int tokenNumber)throws Exception {
        return 0;
    }
    
}

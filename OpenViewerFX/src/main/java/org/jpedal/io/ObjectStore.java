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
 * ObjectStore.java
 * ---------------
 */
package org.jpedal.io;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jpedal.examples.handlers.DefaultImageHelper;
import org.jpedal.io.security.CryptoAES;
import org.jpedal.io.security.TempStoreImage;
import org.jpedal.render.SwingDisplay;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Strip;


/**
 * set of methods to save/load objects to keep memory 
 * usage to a minimum by spooling images to disk 
 * Also includes ancillary method to store a filename - 
 * LogWriter is my logging class - 
 * Several methods are very similar and I should recode 
 * my code to use a common method for the RGB conversion 
 *
 */
public class ObjectStore {

    /** added by MArk to hunt down bug for Adobe - 21832)*/
    private static final boolean debugAdobe=false;
    
    /**list of files to delete*/
    private static final Map<String, String> undeletedFiles=new HashMap<String, String>();

    /**do not set unless you know what you are doing*/
    @SuppressWarnings({"WeakerAccess"})
    public static boolean isMultiThreaded;

    /**debug page cache*/
    private static final boolean debugCache=false;

    /**ensure we check for 'dead' files only once per session*/
    private static boolean checkedThisSession;

    /**correct separator for platform program running on*/
    private static final String separator = System.getProperty("file.separator");

    /**file being decoded at present -used byOXbjects and other classes*/
    private String currentFilename = "",currentFilePath="";

    /**temp storage for the images so they are not held in memory */
    @SuppressWarnings({"WeakerAccess"})
    public static String temp_dir ="";

    @SuppressWarnings({"WeakerAccess"})
    public static final String multiThreaded_root_dir=null;

    /**temp storage for raw CMYK images*/
    private static String cmyk_dir;

    /**key added to each file to make sure unique to pdf being handled*/
    private String key = "jpedal"+Math.random()+ '_';

    /** track whether image saved as tif or jpg*/
    private final Map<String, String> image_type = new HashMap<String, String>();

    /**
     * map to hold file names
     */
    private final Map<String,String> tempFileNames = new HashMap<String,String>();

    /**parameter stored on cached images*/
    public static final Integer IMAGE_WIDTH= 1;

    /**parameter stored on cached images*/
    public static final Integer IMAGE_HEIGHT= 2;

    /**parameter stored on cached images*/
    public static final Integer IMAGE_pX= 3;

    /**parameter stored on cached images*/
    public static final Integer IMAGE_pY= 4;

    /**paramter stored on cached images*/
    public static final Integer IMAGE_MASKCOL= 5;

    /**paramter stored on cached images*/
    public static final Integer IMAGE_COLORSPACE= 6;
    
    /**paramter stored on cached images*/
    public static final Integer IMAGE_DEPTH= 7;

    /**period after which we assumes files must be dead (ie from crashed instance)
     * default is four hour image time*/
    @SuppressWarnings({"WeakerAccess"})
    public static final long time = 14400000 ;

    public String fullFileName;

    //list of cached pages
    private static final Map<String, String> pagesOnDisk=new HashMap<String, String>();
    private static final Map<String, String> pagesOnDiskAsBytes=new HashMap<String, String>();

    //list of images on disk
    private final Map<Integer,String> imagesOnDiskAsBytes=new HashMap<Integer,String>();

    private final Map<Integer,Integer> imagesOnDiskAsBytesW=new HashMap<Integer,Integer>();
    private final Map<Integer,Integer> imagesOnDiskAsBytesH=new HashMap<Integer,Integer>();
    private final Map<Integer,Integer> imagesOnDiskAsBytesD=new HashMap<Integer,Integer>();
    private final Map<Integer,Integer> imagesOnDiskAsBytespX=new HashMap<Integer,Integer>();
    private final Map<Integer,Integer> imagesOnDiskAsBytespY=new HashMap<Integer,Integer>();
    private final Map<Integer, byte[]> imagesOnDiskMask= new HashMap<Integer, byte[]>();
    private final Map<Integer,Integer> imagesOnDiskColSpaceID=new HashMap<Integer,Integer>();
    
    private byte[] encHash;

    /**
     * ObjectStore -
     * Converted for Threading purposes -
     * To fix any errors please try replacing
     * <b>ObjectStore</b> with
     * <b>{your instance of PdfDecoder}.getObjectStore()</b> -
     *
     */
    public ObjectStore(){

        
        setProperties();
        
        init();

    }
    
    static{
        setProperties();
    }
    
    private static void setProperties(){
        
        final String tempDir=System.getProperty("org.jpedal.tempDir");
        
        if(tempDir!=null){
            temp_dir=tempDir;
        }
        
        cmyk_dir = temp_dir + "cmyk" + separator;
        
    }


    private static void init() {
        try{

            //if user has not set static value already, use tempdir
            if(temp_dir.isEmpty()) {
                temp_dir = System.getProperty("java.io.tmpdir");
            }

            if(isMultiThreaded){ //public static variable to ensure unique
                if(multiThreaded_root_dir!=null) {
                    temp_dir = multiThreaded_root_dir + separator + "jpedal-" + System.currentTimeMillis() + separator;
                } else {
                    temp_dir = temp_dir + separator + "jpedal-" + System.currentTimeMillis() + separator;
                }
            }else  if(temp_dir.isEmpty()) {
                temp_dir = temp_dir + separator + "jpedal" + separator;
            } else if(!temp_dir.endsWith(separator)) {
                temp_dir += separator;
            }

            //create temp dir if it does not exist
            final File f = new File(temp_dir);
            if (!f.exists()) {
                f.mkdirs();
            }

        }catch(final Exception e){
            LogWriter.writeLog("Unable to create temp dir at " + temp_dir+ ' ' +e);
        }
    }

    /**
     * Get the file name - we use this as a get in our file repository -
     *
     * <b>Note</b> this method is not part of the API and is not guaranteed to
     * be in future versions of JPedal -
     * 
     * @return String
     */
    public String getCurrentFilename() {
        return currentFilename;
    }

    /**
     * Get the file path for current PDF
     *
     * <b>Note </b> this method is not part of the API and is not guaranteed to
     * be in future versions of JPedal -
     *
     * @return String
     */
    public String getCurrentFilepath() {
        return currentFilePath;
    }
    /**
     * store filename as a key we can use to differentiate images,etc -
     * <b>Note</b> this method is not part of the API and is not guaranteed to
     * be in future versions of JPedal -
     *
     * @param name is of type String
     */
    public final void storeFileName(String name) {

        //System.err.println("7");

        //name = removeIllegalFileNameCharacters(name);
        fullFileName=name;

        //get path
        int ptr=fullFileName.lastIndexOf('/');
        final int ptr2=fullFileName.lastIndexOf('\\');
        if(ptr2>ptr) {
            ptr = ptr2;
        }
        if(ptr>0) {
            currentFilePath = fullFileName.substring(0, ptr + 1);
        } else {
            currentFilePath = "";
        }

        /*
         * work through to get last / or \
         * first we make sure there is one in the name.
         * We could use the properties to get the correct value but the
         * user can still use the Windows format under Unix
         */
        int temp_pointer = name.indexOf('\\');
        if (temp_pointer == -1) //runs on either Unix or Windows!!!
        {
            temp_pointer = name.indexOf('/');
        }
        while (temp_pointer != -1) {
            name = name.substring(temp_pointer + 1);
            temp_pointer = name.indexOf('\\');
            if (temp_pointer == -1) //runs on either Unix or Windows!!!
            {
                temp_pointer = name.indexOf('/');
            }
        }

        /*strip any ending from name*/
        final int pointer = name.lastIndexOf('.');
        if (pointer != -1) {
            name = name.substring(0, pointer);
        }

        /*remove any spaces using my own class and enforce lower case*/
        name = Strip.stripAllSpaces(name);
        currentFilename = name.toLowerCase();

        //System.err.println("8");
    }

    /*
     * save raw CMYK data in CMYK directory - We extract the DCT encoded image
     * stream and save as a file with a .jpeg ending so we have the raw image -
     * This works for DeviceCMYK -
     *
     * @param image_data is of type byte[]
     * @param name is of type String
     * @return boolean
     */
    /*public boolean saveRawCMYKImage(final byte[] image_data, String name) {

        //assume successful
        boolean isSuccessful=true;
        name = removeIllegalFileNameCharacters(name);

        final File cmyk_d = new File(cmyk_dir);
        if (!cmyk_d.exists()) {
            cmyk_d.mkdirs();
        }

        try {
            final FileOutputStream a =new FileOutputStream(cmyk_dir + name + ".jpg");
            tempFileNames.put(cmyk_dir + name + ".jpg","#");

            a.write(image_data);
            a.flush();
            a.close();

        } catch (final Exception e) {
            LogWriter.writeLog("Unable to save CMYK jpeg " + name+ ' ' +e);
            
            isSuccessful=false;
        }

        return isSuccessful;
    }/*/

    public final boolean saveStoredImageAsBytes(String currentImage, BufferedImage image, final boolean file_name_is_path) {
        FileOutputStream fos = null;
        try {
            String current_image = removeIllegalFileNameCharacters(currentImage);
            final File checkDir = new File(temp_dir);
            if (!checkDir.exists()) {
                checkDir.mkdirs();
            }
            String fileName = currentImage+".jpl";
            if (!file_name_is_path) {
                image_type.put(current_image, "jpl");
                fileName = temp_dir + key + current_image+".jpl";
            }
            
            byte[] data = TempStoreImage.getBytes(image);
//        encryption goes here
            if(encHash != null){
                CryptoAES aes = new CryptoAES();
                data = aes.encrypt(encHash, data);
            }           
            fos = new FileOutputStream(fileName);
            fos.write(data);
            fos.close();
            tempFileNames.put(fileName,"#");
            return false;
        } catch (Exception ex) {
            Logger.getLogger(ObjectStore.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(ObjectStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }
    
    /**
     * save buffered image as JPEG or tif
     *
     * @param current_image is of type String
     * @param image is of type BufferedImage
     * @param file_name_is_path is of type boolean
     * @param type is of type String
     * @return boolean
     */
    public final boolean saveStoredImage(
            String current_image,
            BufferedImage image,
            final boolean file_name_is_path,
            final String type) {

        boolean was_error = false;

        if(debugAdobe){
            System.out.println("Save "+current_image);
        }
        current_image = removeIllegalFileNameCharacters(current_image);

        //if(image.getType()==1)
        //image=stripAlpha(image);
        //final int type_id = image.getType();

        //make sure temp directory exists
        final File checkDir = new File(temp_dir);
        if (!checkDir.exists()) {
            checkDir.mkdirs();
        }

        //save image and id so we can reload
        if (type.contains("tif")) {

            if (!file_name_is_path) {
                image_type.put(current_image, "tif");
            }

            was_error =saveStoredImage( "TIF", ".tif", ".tiff", current_image, image, file_name_is_path);
        } else if (type.contains("jpg")) {
            if (!file_name_is_path) {
                image_type.put(current_image, "jpg");
            }

            was_error =saveStoredJPEGImage( current_image, image, file_name_is_path);
        } else if (type.contains("png")) {

            if (!file_name_is_path) {
                image_type.put(current_image, "png");
            }

            was_error = saveStoredImage( "PNG", ".png", ".png", current_image, image, file_name_is_path);

        }
        return was_error;
    }

    /**
     * init method to pass in values for temp directory, unique key,
     * etc so program knows where to store files.
     * 
     * @param current_key is of type String
     */
    public final void init(final String current_key) {
        key = current_key+System.currentTimeMillis();

        //create temp dir if it does not exist
        final File f = new File(temp_dir);
        if (!f.exists()) {
            f.mkdirs();
        }

    }

    /**
     * load a image when required and remove from store
     *
     * @param current_image is of type String
     * @return BufferedImage
     */
    public final BufferedImage loadStoredImage(String current_image) {

        if(current_image==null) {
            return null;
        }

        current_image = removeIllegalFileNameCharacters(current_image);

        //see if jpg
        final String flag = image_type.get(current_image);
        BufferedImage image = null;
        if (flag == null) {
            return null;
        } else if (flag.equals("tif")) {
            image = loadStoredImage(current_image, ".tif");
        } else if (flag.equals("jpg")) {
            image = loadStoredJPEGImage(current_image);
        } else if (flag.equals("png")) {
            image = loadStoredImage(current_image, ".png");
        } else if(flag.equals("jpl")){
            image = loadStoredImage(current_image, ".jpl");
        }

        return image;
    }


    /**
     * routine to remove all objects from temp store
     *
     */
    public final void flush() {

        if(debugAdobe){
            System.out.println("Flush files on close");
        }
        
        /*
         * flush any image data serialized as bytes
         */
        Iterator filesTodelete = imagesOnDiskAsBytes.keySet().iterator();
        while(filesTodelete.hasNext()) {
            final Object file = filesTodelete.next();
            
            if(file!=null){
                final File delete_file = new File(imagesOnDiskAsBytes.get(file));
                if(delete_file.exists()) {
                    delete_file.delete();
                    
                    if(SwingDisplay.testSampling){
                        System.out.println("deleted file as "+delete_file.getAbsolutePath());
                    }
                }
            }
        }

        /**/
        imagesOnDiskAsBytes.clear();
        imagesOnDiskAsBytesW.clear();
        imagesOnDiskAsBytesH.clear();
        imagesOnDiskAsBytesD.clear();
        imagesOnDiskAsBytespX.clear();
        imagesOnDiskAsBytespY.clear();
        imagesOnDiskMask.clear();
        imagesOnDiskColSpaceID.clear();
        /**/

        filesTodelete = tempFileNames.keySet().iterator();
        while(filesTodelete.hasNext()) {
            final String file = ((String)filesTodelete.next());

            if (file.contains(key)) {
                
                //System.out.println("temp_dir="+temp_dir);
                final File delete_file = new File(file);
                
                if(debugAdobe){
                    System.out.println("Delete "+file);
                }
                //delete_file.delete();

                if (delete_file.delete()) {
                    filesTodelete.remove();
                } else //bug in Java stops files being deleted
                {
                    undeletedFiles.put(key, "x");
                    
                    if(debugAdobe){
                        System.out.println("Filed to Delete "+file);
                    }
                }
            }
        }

        try{

            //if setup then flush temp dir
            if (!checkedThisSession && temp_dir.length() > 2) {

                checkedThisSession=true;

                //get contents
                 /**/
                final File temp_files = new File(temp_dir);
                final String[] file_list = temp_files.list();
                final File[] to_be_del = temp_files.listFiles();
                if (file_list != null) {
                    for (int ii = 0; ii < file_list.length; ii++) {
                        if (file_list[ii].contains(key) || file_list[ii].endsWith(".bin")) {
                            final File delete_file = new File(temp_dir + file_list[ii]);
                            delete_file.delete();
                        }
                        //can we also delete any file more than 4 hours old here
                        //its a static variable so user can change

                        if((!file_list[ii].endsWith(".pdf") && (System.currentTimeMillis()-to_be_del[ii].lastModified() >= time))){
                                //System.out.println("File time : " + to_be_del[ii].lastModified() );
                                //System.out.println("Current time: " + System.currentTimeMillis());
                                //System.out.println("Redundant File Removed : " + to_be_del[ii].getName() );
                                to_be_del[ii].delete();
                            }
                        }
                    }

                /*
                
                //suggested by Manuel to ensure flushes correctly
                //System.gc();

                Iterator filesTodelete = tempFileNames.keySet().iterator();
                while(filesTodelete.hasNext()) {
                    String file = ((String)filesTodelete.next());

                    if (file.indexOf(key) != -1) {
                        File delete_file = new File(file);
                        //System.out.println("Delete "+file);
                        //delete_file.delete();

                        //suggested by Manuel to ensure flushes correctly
                        if (delete_file.delete()) filesTodelete.remove();
                    }
                }
                /**/
            }

            /*flush cmyk directory as well*/
            final File cmyk_d = new File(cmyk_dir);
            if (cmyk_d.exists()) {
/*			boolean filesExist = false;
			String[] file_list = cmyk_d.list();

			for (int ii = 0; ii < file_list.length; ii++) {
				File delete_file = new File(cmyk_dir + file_list[ii]);
				delete_file.delete();

				//make sure deleted
				if (delete_file.exists())
					filesExist = true;

			}
*/
                cmyk_d.delete();

/*			if (filesExist)
            LogWriter.writeLog("CMYK files not deleted at end");
*/
            }

        }catch(final Exception e){
            LogWriter.writeLog("Exception " + e + " flushing files");
        }
    }

    /**
     * copies cmyk raw data from cmyk temp dir to target directory.
     * 
     * @param target_dir is of type String
     */
    public static void copyCMYKimages(String target_dir) {

        final File cmyk_d = new File(cmyk_dir);
        if (cmyk_d.exists()) {
            final String[] file_list = cmyk_d.list();

            if (file_list.length > 0) {
                /*check separator on target dir and exists*/
                if (!target_dir.endsWith(separator)) {
                    target_dir += separator;
                }

                final File test_d = new File(target_dir);
                if (!test_d.exists()) {
                    test_d.mkdirs();
                }

            }
            for (final String aFile_list : file_list) {
                final File source = new File(cmyk_dir + aFile_list);
                final File dest = new File(target_dir + aFile_list);

                source.renameTo(dest);

            }
        }
    }

    /**
     * save buffered image as JPEG
     */
    private boolean saveStoredJPEGImage(String file_name, final BufferedImage image, final boolean file_name_is_path) {

        if (!file_name_is_path) {
            file_name = temp_dir + key + file_name;           
        }

        //add ending if needed
        final String s=file_name.toLowerCase();
        if (!s.endsWith(".jpg") && !s.endsWith(".jpeg")) {
            file_name += ".jpg";
        }

        /*
         * fudge to write out high quality then try low if failed
         */
        try { //write out data to create image in temp dir

            DefaultImageHelper.write(image,"jpg",file_name);

            tempFileNames.put(file_name,"#");

        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " writing image " + image + " as " + file_name);
        }

        return false;
    }

    public String getFileForCachedImage(final String current_image) {

        return temp_dir + key + current_image + '.' +image_type.get(current_image);

    }

    /**
     * load a image when required and remove from store
     */
    private BufferedImage loadStoredImage(String current_image,final String ending) {

        current_image = removeIllegalFileNameCharacters(current_image);

        final String file_name = temp_dir + key + current_image + ending;
                
        if(ending.equals(".jpl")){
            File file = new File(file_name);
            if(!file.exists()){
                return null;
            }
            FileInputStream fis = null;
            try {                
                byte[] data= new byte[(int)file.length()];
                fis = new FileInputStream(file_name);
                fis.read(data);                
                if(encHash != null){
                    CryptoAES aes = new CryptoAES();
                    data = aes.decrypt(encHash, data);
                }
                BufferedImage img = TempStoreImage.getImage(data);
                return img;
            } catch (Exception ex) {
                Logger.getLogger(ObjectStore.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(ObjectStore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return null;
        }

        //load the image to process
        return DefaultImageHelper.read(file_name);

    }

     /**
     * Save a Copy.
     * 
     * @param current_image is of type String
     * @param destination is of type String
     */
    public final void saveAsCopy(
            String current_image,
            final String destination) {
        BufferedInputStream from = null;
        BufferedOutputStream to = null;

        current_image = removeIllegalFileNameCharacters(current_image);
        final String source = temp_dir + key + current_image;

        try {
            //create streams
            from = new BufferedInputStream(new FileInputStream(source));
            to = new BufferedOutputStream(new FileOutputStream(destination));

            //write
            final byte[] buffer = new byte[65535];
            int bytes_read;
            while ((bytes_read = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytes_read);
            }
        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " copying file");
        }

        //close streams
        try {
            to.close();
            from.close();
        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " closing files");
        }
    }

    /**
     * Save Copy.
     *
     * @param source is of type String.
     * @param destination is of type String.
     */
    public static void copy(
            final String source,
            final String destination) {

        BufferedInputStream from = null;
        BufferedOutputStream to = null;

        try {
            //create streams
            from = new BufferedInputStream(new FileInputStream(source));
            to = new BufferedOutputStream(new FileOutputStream(destination));
        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " copying file");
        }

        copy(from, to);
    }

    public static void copy(final InputStream from, final OutputStream to) {
        try{
            //write
            final byte[] buffer = new byte[65535];
            int bytes_read;
            while ((bytes_read = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytes_read);
            }
        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " copying file");
        }

        //close streams
        try {
            to.close();
            from.close();
        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " closing files");
        }
    }

    /**
     * load a image when required and remove from store
     */
    private BufferedImage loadStoredJPEGImage(final String current_image) {
        final String file_name = temp_dir + key + current_image + ".jpg";

        //load the image to process
        BufferedImage image = null;
        final File a = new File(file_name);
        if (a.exists()) {
            try {

                image=DefaultImageHelper.read(file_name);

            } catch (final Exception e) {
                LogWriter.writeLog("Exception " + e + " loading " + current_image);
            }
        } else {
            image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        }

        return image;
    }


    /**
     * save buffered image
     */
    private boolean saveStoredImage(
            final String format,
            final String ending1,
            final String ending2,
            String current_image,
            BufferedImage image,
            final boolean file_name_is_path) {
        boolean was_error = false;

        //generate path name or use
        //value supplied by user
        current_image = removeIllegalFileNameCharacters(current_image);
        String file_name = current_image;
        
        if (!file_name_is_path) {
            file_name = temp_dir + key + current_image;
        }

        //add ending if needed
        final String s=file_name.toLowerCase();
        if (!s.endsWith(ending1) && !s.endsWith(ending2)) {
            file_name += ending1;
        }

        try { //write out data to create image in temp dir

            DefaultImageHelper.write(image, format, file_name);
            
            tempFileNames.put(file_name,"#");

        } catch (final Exception e) {
            LogWriter.writeLog(" Exception " + e + " writing image " + image + " with type " + image.getType());
            
            was_error = true;

        }catch (final Error ee) {

            LogWriter.writeLog("Error " + ee + " writing image " + image + " with type " + image.getType());
            
            was_error = true;
        }

        return was_error;
    }

    /**
     * delete all cached pages
     */
    public static void  flushPages(){

        try{

            Iterator<String> filesTodelete = pagesOnDisk.keySet().iterator();
            while(filesTodelete.hasNext()) {
                final String file = filesTodelete.next();

                if(file!=null){
                    final File delete_file = new File(pagesOnDisk.get(file));
                    if(delete_file.exists()) {
                        delete_file.delete();
                    }
                }
            }

            pagesOnDisk.clear();

            /*
             * flush any pages serialized as bytes
             */

            filesTodelete = pagesOnDiskAsBytes.keySet().iterator();
            while(filesTodelete.hasNext()) {
                final String file = filesTodelete.next();

                if(file!=null){
                    final File delete_file = new File(pagesOnDiskAsBytes.get(file));
                    if(delete_file.exists()) {
                        delete_file.delete();
                    }
                }
            }

            pagesOnDiskAsBytes.clear();

            if(debugCache) {
                System.out.println("Flush cache ");
            }

        }catch(final Exception e){
            LogWriter.writeLog("Exception " + e + " flushing files");
        }
    }

    @Override
    protected void finalize(){

        try {
            super.finalize();
        } catch (final Throwable e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }

        flush();

        /*
         * try to redelete files again
         */
        for (final String o : undeletedFiles.keySet()) {
            final String file = (o);

            final File delete_file = new File(file);

            if (delete_file.delete()) {
                undeletedFiles.remove(file);
            }

        }
    }

    public static byte[] getCachedPageAsBytes(final String key) {

        byte[] data=null;

        final String cachedFile= pagesOnDiskAsBytes.get(key);

        if(cachedFile!=null){
            final BufferedInputStream from;
            try {
                final File fis=new File(cachedFile);
                from = new BufferedInputStream(new FileInputStream(fis));

                data=new byte[(int)fis.length()];                
                from.read(data);
                from.close();
                
                CryptoAES aes = new CryptoAES();
                data = aes.decrypt(key.getBytes(), data);
                
                //
            } catch (final Exception e) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
        }
        return data;
    }

    public static void cachePageAsBytes(final String key, byte[] bytes) {

        try {

            //if you already use key, delete it first now
            //as will not be removed otherwise
            if(pagesOnDiskAsBytes.containsKey(key)){
                final File delete_file = new File(pagesOnDiskAsBytes.get(key));
                if(delete_file.exists()){
                    delete_file.delete();

                }
            }

            final File ff=File.createTempFile("bytes",".bin", new File(ObjectStore.temp_dir));

            final BufferedOutputStream to = new BufferedOutputStream(new FileOutputStream(ff));
            
            CryptoAES aes = new CryptoAES();
            bytes = aes.encrypt(key.getBytes(), bytes);
            
            to.write(bytes);
            to.flush();
            to.close();

            //save to delete at end
            pagesOnDiskAsBytes.put(key,ff.getAbsolutePath());

            if(debugCache) {
                System.out.println("save to cache " + key + ' ' + ff.getAbsolutePath());
            }


        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }

    }

    public void saveRawImageData(final String pageImgCount, final byte[] bytes, final int w, final int h, final int bpc, final int pX, final int pY, final byte[] maskCol, final int colorSpaceID) {

        try {
            //System.out.println("ObjectStore.temp_dir="+ObjectStore.temp_dir);
            
            final File ff=File.createTempFile("image",".bin", new File(ObjectStore.temp_dir));

            final BufferedOutputStream to = new BufferedOutputStream(new FileOutputStream(ff));
            if (encHash != null) {
                CryptoAES aes = new CryptoAES();
                to.write(aes.encrypt(encHash, bytes));
            } else {
                to.write(bytes);
            }
            to.flush();
            to.close();

            final Integer key=Integer.valueOf(pageImgCount);
            imagesOnDiskAsBytes.put(key,ff.getAbsolutePath());
            imagesOnDiskAsBytesW.put(key, w);
            imagesOnDiskAsBytesH.put(key, h);
            imagesOnDiskAsBytesD.put(key,bpc);

            imagesOnDiskAsBytespX.put(key, pX);
            imagesOnDiskAsBytespY.put(key, pY);
            imagesOnDiskMask.put(key,maskCol);
            imagesOnDiskColSpaceID.put(key, colorSpaceID);

            if(debugCache) {
                System.out.println("save to image cache " + pageImgCount + ' ' + ff.getAbsolutePath());
            }
            
            if(SwingDisplay.testSampling){
                System.out.println(pageImgCount+" save cached image as "+ff.getAbsolutePath()+" "+this);
            }


        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
    }


    /**
     * see if image data saved.
     * @param number is of type String
     * @return boolean
     */
    public boolean isRawImageDataSaved(final String number) {

        //System.out.println("isSaved="+imagesOnDiskAsBytes.get(new Integer(number))!=null);

        return imagesOnDiskAsBytes.get(Integer.valueOf(number))!=null;
    }

    /**
     * Retrieve byte data on disk.
     * 
     * @param i is of type String
     * @return byte[]
     */
    public byte[] getRawImageData(final String i) {



        byte[] data=null;

        final Object cachedFile= imagesOnDiskAsBytes.get(Integer.valueOf(i));

        if(cachedFile!=null){
            final BufferedInputStream from;
            try {
                final File fis=new File((String)cachedFile);
                from = new BufferedInputStream(new FileInputStream(fis));

                data=new byte[(int)fis.length()];
                from.read(data);
                if(encHash != null){
                    CryptoAES aes = new CryptoAES();
                    data = aes.decrypt(encHash, data);
                }
                from.close();

                //
            } catch (final Exception e) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
        }

        return data;

    }

    /**
     * Return parameter stored for image or null.
     * @param imageID is of type String
     * @param key is of type Integer
     * @return Object
     */
    public Object getRawImageDataParameter(final String imageID, final Integer key) {


        if(key.equals(IMAGE_WIDTH)){
            return imagesOnDiskAsBytesW.get(Integer.valueOf(imageID));
        }else if(key.equals(IMAGE_HEIGHT)){
            return imagesOnDiskAsBytesH.get(Integer.valueOf(imageID));
        }else if(key.equals(IMAGE_DEPTH)){
            return imagesOnDiskAsBytesD.get(Integer.valueOf(imageID));
        
        }else if(key.equals(IMAGE_pX)){
            return imagesOnDiskAsBytespX.get(Integer.valueOf(imageID));
        }else if(key.equals(IMAGE_pY)){
            return imagesOnDiskAsBytespY.get(Integer.valueOf(imageID));
        }else if(key.equals(IMAGE_MASKCOL)){
            return imagesOnDiskMask.get(Integer.valueOf(imageID));
        }else if(key.equals(IMAGE_COLORSPACE)){
            return imagesOnDiskColSpaceID.get(Integer.valueOf(imageID));
        }else {
            return null;
        }

    }

    public static File createTempFile(final String filename) throws IOException {

        final File tempURLFile;
        String suffix;

        StringBuilder prefix=new StringBuilder(filename.substring(0, filename.lastIndexOf('.')));
        while(prefix.length()<3) {
            prefix.append('a');
        }

        suffix=filename.substring(filename.lastIndexOf('.'));
        if(suffix.length()<3) {
            suffix = "pdf";
        }

        tempURLFile = File.createTempFile(prefix.toString(), suffix,new File(ObjectStore.temp_dir));

        return tempURLFile;
    }

    /**
     * Remove troublesome characters from temp file names.
     *
     * @param s is of type String
     * @return String
     */
    public static String removeIllegalFileNameCharacters(final String s)
    {

        //Disabled for the time being.  See case 9311 case 9316.
//		//reduce scope of fix for windows path used as Name of image
//		//as it breaks other files
//		if(s.indexOf(":")!=-1){//use indexOf!=-1 instead of contains for compatability with JAVAME
//	        //s = s.replace('\\', '_');
//	        s = s.replace('/', '_');
//	        s = s.replace(':', '_');
//	        s = s.replace('*', '_');
//	        s = s.replace('?', '_');
//	        s = s.replace('"','_');
//	        s = s.replace('<','_');
//	        s = s.replace('>','_');
//	        s = s.replace('|','_');
//		}
        return s;
    }

    /**
     * Add file to list we delete on flush so we can clear any
     * temp files we create.
     * 
     * @param rawFileName is of type String
     */
    public void setFileToDeleteOnFlush(final String rawFileName) {
        tempFileNames.put(rawFileName,"#");

    }

    public String getKey() {
        return key;
    }

    public void setEncHash(byte[] pass) {
        this.encHash = pass;
    }
}

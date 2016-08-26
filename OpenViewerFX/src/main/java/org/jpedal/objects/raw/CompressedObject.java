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
 * CompressedObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.LogWriter;

public class CompressedObject extends PdfObject {

	//unknown CMAP as String
	//String unknownValue=null;

	private int[] Index, W;
	
	byte[][] ID;

	//private float[] Matrix;

	//boolean ImageMask=false;
	
	int First, Prev=-1,XRefStm=-1;

	private PdfObject Encrypt, Extends, Info, Root;

	int Size;

	//private PdfObject OPI=null, XObject=null;

    public CompressedObject(final String ref) {
        super(ref);
    }

    public CompressedObject(final int ref, final int gen) {
       super(ref,gen);
    }


    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

	        case PdfDictionary.Encrypt:
	        	return Encrypt;

            case PdfDictionary.Extends:
	        	return Extends;

            case PdfDictionary.Info:
	        	return Info;

            case PdfDictionary.Root:
	        	return Root;

//            case PdfDictionary.XObject:
//                return XObject;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value){

        switch(id){

			case PdfDictionary.First:
	        	First=value;
	        break;

            case PdfDictionary.Prev:

                //some PDFs can get multiple values and second one wrong
                if(Prev==-1) {
                    Prev = value;
                }
	        break;
	        
	        case PdfDictionary.Size:
	        	Size=value;
	        break;
//
//	        case PdfDictionary.Height:
//	            Height=value;
//	        break;
//
	        case PdfDictionary.XRefStm:
	            XRefStm=value;
	        break;

            default:
            	super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id){

        switch(id){
        
        	case PdfDictionary.First:
            return First;

            case PdfDictionary.Prev:
            return Prev;

        	case PdfDictionary.Size:
            return Size;
//
//        	case PdfDictionary.Height:
//            return Height;
//
	        case PdfDictionary.XRefStm:
	            return XRefStm;

            default:
            	return super.getInt(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value){

    	value.setID(id);
    	
        switch(id){

            case PdfDictionary.Encrypt:
	        	Encrypt=value;
			break;

	        case PdfDictionary.Extends:
	        	Extends=value;
			break;

            case PdfDictionary.Info:
	        	Info=value;
			break;

            case PdfDictionary.Root:
	        	Root=value;
			break;

//            case PdfDictionary.XObject:
//            	XObject=value;
//    		break;

            default:
            	super.setDictionary(id, value);
        }
    }


    @Override
    public int setConstant(final int pdfKeyType, final int keyStart, final int keyLength, final byte[] raw) {

        int PDFvalue =PdfDictionary.Unknown;

        int id=0,x=0,next;

        try{

            //convert token to unique key which we can lookup

            for(int i2=keyLength-1;i2>-1;i2--){

            	next=raw[keyStart+i2];

            	//System.out.println((char)next);
            	next -= 48;

                id += ((next)<<x);

                x += 8;
            }

            switch(id){

//                case StandardFonts.CIDTYPE0:
//                    PDFvalue =StandardFonts.CIDTYPE0;
//                break;


                default:

//                	if(pdfKeyType==PdfDictionary.Encoding){
//                		PDFvalue=PdfCIDEncodings.getConstant(id);
//
//                		if(PDFvalue==PdfDictionary.Unknown){
//
//                			byte[] bytes=new byte[keyLength];
//
//                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
//
//                			unknownValue=new String(bytes);
//                		}
//
//                		if(debug && PDFvalue==PdfDictionary.Unknown){
//                			System.out.println("Value not in PdfCIDEncodings");
//
//                           	 byte[] bytes=new byte[keyLength];
//
//                               System.arraycopy(raw,keyStart,bytes,0,keyLength);
//                               System.out.println("Add to CIDEncodings and as String");
//                               System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in PdfFont Object");
//
//                               System.out.println("final public static int CMAP_"+new String(bytes)+"="+id+";");
//                		}
//                	}else
                	PDFvalue=super.setConstant(pdfKeyType,id);

                    if(PDFvalue==-1 && debug){

                        	 final byte[] bytes=new byte[keyLength];

                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
                            System.out.println("key="+new String(bytes)+ ' ' +id+" not implemented in setConstant in "+this);

                            System.out.println("final public static int "+new String(bytes)+ '=' +id+ ';');
                           
                        }

                    break;

            }

        }catch(final Exception e){
            LogWriter.writeLog("Exception: " + e.getMessage());
        }

        //System.out.println(pdfKeyType+"="+PDFvalue);
        switch(pdfKeyType){
        
        case PdfDictionary.SMask:
        	generalType=PDFvalue;
            break;
            
        case PdfDictionary.TR:
        	generalType=PDFvalue;
            break;
            
    		default:
    			super.setConstant(pdfKeyType,id);

        }

        return PDFvalue;
    }


//    public void setStream(){
//
//        hasStream=true;
//    }


    @Override
    public int[] getIntArray(final int id) {

        switch(id){

            case PdfDictionary.Index:
                return deepCopy(Index);

            case PdfDictionary.W:
                return deepCopy(W);

            default:
            	return super.getIntArray(id);
        }
    }

    @Override
    public void setIntArray(final int id, final int[] value) {

        switch(id){

	        case PdfDictionary.Index:
	        	Index=value;
	        break;
        
            case PdfDictionary.W:
            	W=value;
            break;

            default:
            	super.setIntArray(id, value);
        }
    }


    @Override
    public byte[][] getStringArray(final int id) {

        switch(id){

            case PdfDictionary.ID:
                            return deepCopy(ID);

            default:
            	return super.getStringArray(id);
        }
    }

    @Override
    public void setStringArray(final int id, final byte[][] value) {

        switch(id){

            case PdfDictionary.ID:
                ID=value;
                break;

            default:
            	super.setStringArray(id, value);
        }

    }

    /**
     * unless you need special fucntions,
     * use getStringValue(int id) which is faster
     */
    @Override
    public String getStringValue(final int id, final int mode) {

        final byte[] data=null;

        //get data
   //     switch(id){

//            case PdfDictionary.BaseFont:
//                data=rawBaseFont;
//                break;

    //    }

        //convert
        switch(mode){
            case PdfDictionary.STANDARD:

                //setup first time
                if(data!=null) {
                    return new String(data);
                } else {
                    return null;
                }


            case PdfDictionary.LOWERCASE:

                //setup first time
                if(data!=null) {
                    return new String(data);
                } else {
                    return null;
                }

            case PdfDictionary.REMOVEPOSTSCRIPTPREFIX:

                //setup first time
                if(data!=null){
                	final int len=data.length;
                	if(len>6 && data[6]=='+'){ //lose ABCDEF+ if present
                		final int length=len-7;
                		final byte[] newData=new byte[length];
                		System.arraycopy(data, 7, newData, 0, length);
                		return new String(newData);
                	}else {
                        return new String(data);
                    }
                }else {
                    return null;
                }

            default:
                throw new RuntimeException("Value not defined in getName(int,mode) in "+this);
        }
    }


    @Override
    public boolean decompressStreamWhenRead() {
		return true;
	}



    @Override
    public int getObjectType(){
    	return PdfDictionary.CompressedObject;
    }
}
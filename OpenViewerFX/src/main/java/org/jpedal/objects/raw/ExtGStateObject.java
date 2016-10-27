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
 * ExtGStateObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.LogWriter;

public class ExtGStateObject extends PdfObject {

	//unknown CMAP as String
	//String unknownValue=null;

	private float[] Matrix;

    float CA=-1,ca=-1, LW=-1,OPM=-1;

    byte[][] TR;
    
    boolean AIS,op, OP;

    PdfObject TRobj;
    private byte[][] BM;

    public ExtGStateObject(final String ref) {
        super(ref);
    }

    public ExtGStateObject(final int ref, final int gen) {
       super(ref,gen);
    }

    @Override
    public float getFloatNumber(final int id){

        switch(id){

	        case PdfDictionary.CA:
	        	return CA;
	        	
	        case PdfDictionary.ca:
	        	return ca;
        	
	        case PdfDictionary.LW:
	        	return LW;

	        case PdfDictionary.OPM:
	        	return OPM;
	        	
            default:
            	return super.getFloatNumber(id);
        }
    }

    @Override
    public void setFloatNumber(final int id, final float value){

        switch(id){

        	case PdfDictionary.CA:
		        	CA=value;
	    	break;
    	
    		case PdfDictionary.ca:
		        	ca=value;
	    	break;
	    	
	        case PdfDictionary.LW:
	        	LW=value;
	        	break;
	        	
	        case PdfDictionary.OPM:
	        	OPM=value;
	        	break;

            default:
            	super.setFloatNumber(id, value);
        }
    }

    @Override
    public boolean getBoolean(final int id){

        switch(id){

        case PdfDictionary.AIS:
        	return AIS;

        case PdfDictionary.op:
        	return op;
        	
        case PdfDictionary.OP:
        	return OP;

            default:
            	return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value){

        switch(id){

        case PdfDictionary.AIS:
        	AIS=value;
        	break;
        	
        case PdfDictionary.OP:
        	OP=value;
    	break;
    	
    	case PdfDictionary.op:
        	op=value;
    	break;
    	
//    	case PdfDictionary.SA:
//        	SA=value;
//        	break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

            case PdfDictionary.TR:
                return TRobj;

            default:
                return super.getDictionary(id);
        }
    }



    @Override
    public void setDictionary(final int id, final PdfObject value){

    	value.setID(id);
    	
        switch(id){

            case PdfDictionary.TR:
            	TRobj=value;
    		break;

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

            	next -= 48;

                id += ((next)<<x);

                x += 8;
            }

            switch(id){

            case PdfDictionary.Image:
                PDFvalue =PdfDictionary.Image;
            break;

            case PdfDictionary.Form:
                PDFvalue =PdfDictionary.Form;
            break;

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
//                               
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
        //switch(pdfKeyType){

//        	case PdfDictionary.Subtype:
//        		subtype=PDFvalue;
//        		break;

        //}

        return PDFvalue;
    }


//    public void setStream(){
//
//        hasStream=true;
//    }


    @Override
    public PdfArrayIterator getMixedArray(final int id) {

    	switch(id){

            case PdfDictionary.BM:
                
                return new PdfArrayIterator(BM);

            default:

            return super.getMixedArray(id);
        }
	}



    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch(id){


            case PdfDictionary.BM:
	
                BM=value;
                break;

            default:
            	super.setMixedArray(id, value);
        }
    }

    @Override
    public float[] getFloatArray(final int id) {

        switch(id){

        	case PdfDictionary.Matrix:
        		return deepCopy(Matrix);

            default:
            	return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch(id){

	        case PdfDictionary.Matrix:
	            Matrix=value;
	        break;

            default:
            	super.setFloatArray(id, value);
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
     //   switch(id){

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
    public byte[][] getKeyArray(final int id) {

        switch(id){

            case PdfDictionary.TR:
       		    return deepCopy(TR);

            default:
            	return super.getKeyArray(id);
        }
    }

    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch(id){

            case PdfDictionary.TR:
                TR=value;
            break;

            default:
            	super.setKeyArray(id, value);
        }

    }
}
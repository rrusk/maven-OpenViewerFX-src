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
 * XObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class XObject extends PdfObject {

	byte[] rawIntent, rawOC;
	
	String Intent,OC;
	
	private int[] Mask;

	boolean ImageMask,K;
	
	int Height=1, Width=1;
	
	private PdfObject Group,MaskObj, OCObject, OPI, XObject;
	
    public XObject(final String ref) {
        super(ref);
    }

    public XObject(final int ref, final int gen) {
       super(ref,gen);
    }


    public XObject(final int type) {
    	super(type);
	}
    
    @Override
    public boolean getBoolean(final int id){

        switch(id){

        case PdfDictionary.ImageMask:
        	return ImageMask;
        	
        case PdfDictionary.K:
        	return K; 	
       
            default:
            	return super.getBoolean(id);
        }

    }
    
    @Override
    public void setBoolean(final int id, final boolean value){

        switch(id){

        case PdfDictionary.ImageMask:
        	ImageMask=value;
        	break;
        
        case PdfDictionary.K:
        	K=value;
        	break;
        
        	
            default:
                super.setBoolean(id, value);
        }
    }
    
    @Override
    public int[] getIntArray(final int id) {

        switch(id){

            case PdfDictionary.Mask:
                return deepCopy(Mask);

            default:
            	return super.getIntArray(id);
        }
    }

    @Override
    public void setIntArray(final int id, final int[] value) {

        switch(id){

            case PdfDictionary.Mask:
            	Mask=value;
            break;

            default:
            	super.setIntArray(id, value);
        }
    }


    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

        	case PdfDictionary.Group:
        		return Group;

	        case PdfDictionary.Mask:
	        	return MaskObj;

            case PdfDictionary.OC:
                return OCObject;

	        case PdfDictionary.OPI:
	        	return OPI;
	
            case PdfDictionary.XObject:
                return XObject;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value){

        switch(id){

	        case PdfDictionary.Height:
	            Height=value;
	        break;
        
	        case PdfDictionary.Width:
	            Width=value;
	        break;
        
            default:
            	super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id){

        switch(id){

        	case PdfDictionary.Height:
            return Height;
         
	        case PdfDictionary.Width:
	            return Width;
	          
            default:
            	return super.getInt(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value){

        if(value!=null){
            value.setID(id);
        }
    	
        switch(id){

	        case PdfDictionary.Group:
	        	Group=value;
	        break;

	        case PdfDictionary.Mask:
	        	MaskObj=value;
	        break;

            case PdfDictionary.OC:
                OCObject=value;
            break;
            
            case PdfDictionary.OPI:
	        	OPI=value;
			break;
			
            case PdfDictionary.XObject:
            	XObject=value;
    		break;
            
            default:
            	super.setDictionary(id, value);
        }
    }

    @Override
    public void setName(final int id, final byte[] value) {

        switch(id){

            
            case PdfDictionary.Intent:
                rawIntent=value;
            break;

            case PdfDictionary.OC:
	        	rawOC=value;
			break;
            
            default:
                super.setName(id,value);

        }

    }


    @Override
    public String getName(final int id) {

        switch(id){

            case PdfDictionary.Intent:

            //setup first time
            if(Intent==null && rawIntent!=null) {
                Intent = new String(rawIntent);
            }

            return Intent;

            case PdfDictionary.OC:

            //setup first time
            if(OC==null && rawOC!=null) {
                OC = new String(rawOC);
            }

            return OC;
               
            default:
                return super.getName(id);

        }
    }

    @Override
    public int getObjectType() {
		return PdfDictionary.XObject;
	}
}

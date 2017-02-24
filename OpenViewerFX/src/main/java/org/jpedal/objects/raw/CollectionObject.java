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
 * CollectionObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.StringUtils;


public class CollectionObject extends PdfObject{
    
    byte[] rawView, rawReorder, rawName, rawDesc, rawCreationDate, rawModDate;
    
    String View, Reorder, Name, Desc, CreationDate, ModDate;
    
    protected PdfObject Schema, Folders, Sort, Navigator, Colors, Child, Next, CI, Thumb;
    
    int O, ID;
    
    byte[] rawN;

    String N;
    
    boolean V = true, E = false;
    
    private byte[][] S;

    private boolean[] A;
    
    float[] Background, CardBackground, CardBorder, PrimaryText, SecondaryText;
    
    int[] Free;
    
    public CollectionObject(final String ref) {
        super(ref);
    }

    public CollectionObject(final int ref, final int gen) {
        super(ref, gen);
    }
        
    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

            case PdfDictionary.Schema:
        		return Schema;
                
            case PdfDictionary.Folders:
        		return Folders;
            
            case PdfDictionary.Sort:
        		return Sort;
            
            case PdfDictionary.Navigator:
        		return Navigator;
            
            case PdfDictionary.Colors:
        		return Colors;
               
            case PdfDictionary.Child:
        		return Child;
               
            case PdfDictionary.Next:
        		return Next;
               
            case PdfDictionary.CI:
        		return CI;
               
            default:
                return super.getDictionary(id);
        }
    }
    
    @Override
    public void setDictionary(final int id, final PdfObject value){

    	value.setID(id);

        switch(id){
            
            case PdfDictionary.Schema:
        		Schema=value;
        	break;

            case PdfDictionary.Folders:
        		Folders=value;
        	break;
            
            case PdfDictionary.Sort:
        		Sort=value;
        	break;
            
            case PdfDictionary.Navigator:
        		Navigator=value;
        	break;
            
            case PdfDictionary.Colors:
        		Colors=value;
        	break;
            
            case PdfDictionary.Child:
        		Child=value;
        	break;
            
            case PdfDictionary.Next:
        		Next=value;
        	break;
            
            case PdfDictionary.CI:
        		CI=value;
        	break;
            
            default:
                super.setDictionary(id, value);
        }
    }
    
    @Override
    public String getName(final int id) {

        switch (id) {

            case PdfDictionary.View:
                if(View==null && rawView!=null) {
                    View = new String(rawView);
                }
                return View;

            case PdfDictionary.Reorder:
                if(Reorder==null && rawReorder!=null) {
                    Reorder = new String(rawReorder);
                }
                return Reorder;

            default:
                return super.getName(id);

        }
    }

    @Override
    public void setName(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.View:
                rawView = value;
                View = null;
                break;

            case PdfDictionary.Reorder:
                rawReorder = value;
                Reorder = null;
                break;

            default:
                super.setName(id, value);

        }
    }

    
    @Override
    public boolean getBoolean(int id){
        
        switch(id){
            case PdfDictionary.E:
                return E;
            case PdfDictionary.V:
                return V;
        }
        
        return super.getBoolean(id);
    }
    
    @Override
    public int getInt(int id){
        
        switch(id){
            case PdfDictionary.O:
                return O;
            case PdfDictionary.ID:
                return ID;
        }
        
        return super.getInt(id);
    }
    
    @Override
    public int[] getIntArray(int id){
        
        switch(id){
            case PdfDictionary.Free:
                return Free;
        }
        
        return super.getIntArray(id);
    }
    
    @Override
    public String getTextStreamValue(int id){
        
        switch(id){
            case PdfDictionary.N:
                if(N==null && rawN!=null){
                    N = StringUtils.getTextString(rawN, false);
                }
                return N;
                
            case PdfDictionary.Name:
                if(Name==null && rawName!=null){
                    Name = StringUtils.getTextString(rawName, false);
                }
                return Name;
                
            case PdfDictionary.Desc:
                if(Desc==null && rawDesc!=null){
                    Desc = StringUtils.getTextString(rawDesc, false);
                }
                return Desc;
                
            case PdfDictionary.CreationDate:
                if(CreationDate==null && rawCreationDate!=null){
                    CreationDate = StringUtils.getTextString(rawCreationDate, false);
                }
                return CreationDate;
                
            case PdfDictionary.ModDate:
                if(ModDate==null && rawModDate!=null){
                    ModDate = StringUtils.getTextString(rawModDate, false);
                }
                return ModDate;
        }
        
        return super.getTextStreamValue(id);
    }
    
    @Override
    public byte[] getTextStreamValueAsByte(int id){
        
        switch(id){
            case PdfDictionary.N:
                return rawN;
                
            case PdfDictionary.Name:
                return rawName;
                
            case PdfDictionary.Desc:
                return rawDesc;
                
            case PdfDictionary.CreationDate:
                return rawCreationDate;
                
            case PdfDictionary.ModDate:
                return rawModDate;
        }
        
        return super.getTextStreamValueAsByte(id);
    }
    
    
    @Override
    public void setBoolean(int id, boolean value){
        
        switch(id){
            case PdfDictionary.E:
                E = value;
                break;
            case PdfDictionary.V:
                V = value;
                break;
            default:
                super.setBoolean(id, value);
                break;
        }
        
        
    }
    
    @Override
    public void setIntArray(int id, int[] value){
        
        
        switch(id){
            case PdfDictionary.Free:
                Free = value;
                break;
            default:
                super.setIntArray(id, value);
                break;
        }
    }
    
    @Override
    public void setIntNumber(int id, int value){
        
        switch(id){
            case PdfDictionary.O:
                O = value;
                break;
            case PdfDictionary.ID:
                ID = value;
                break;
            default:
                super.setIntNumber(id, value);
                break;
        }
    }
    
    @Override
    public void setTextStreamValue(int id, String value){
        
        switch(id){
            case PdfDictionary.N:
                N = value;
                break;
                
            case PdfDictionary.Name:
                Name = value;
                break;
                
            case PdfDictionary.Desc:
                Desc = value;
                break;
                
            case PdfDictionary.CreationDate:
                CreationDate = value;
                break;
                
            case PdfDictionary.ModDate:
                ModDate = value;
                break;
                
            default:
                super.setTextStreamValue(id, value);
                break;
        }
        
        
    }
    
    @Override
    public void setTextStreamValue(int id, byte[] value){
        
        switch(id){
            case PdfDictionary.N:
                rawN = value;
                break;
                
            case PdfDictionary.Name:
                rawName = value;
                break;
                
            case PdfDictionary.Desc:
                rawDesc = value;
                break;
                
            case PdfDictionary.CreationDate:
                rawCreationDate = value;
                break;
                
            case PdfDictionary.ModDate:
                rawModDate = value;
                break;
                
            default:
                super.setTextStreamValue(id, value);
                break;
        }
        
        
    }
    
    
    @Override
    public boolean[] getBooleanArray(final int id) {

        switch (id) {

            case PdfDictionary.A:
                return deepCopy(A);

            default:
                return super.getBooleanArray(id);
        }
    }

    @Override
    public byte[][] getStringArray(final int id) {

        switch (id) {

            case PdfDictionary.S:
                return deepCopy(S);

            default:
                return super.getStringArray(id);
        }
    }

    @Override
    public void setBooleanArray(final int id, final boolean[] value) {

        switch (id) {

            case PdfDictionary.A:
                A = value;
                break;

            default:
                super.setBooleanArray(id, value);

        }
    }

    @Override
    public void setStringArray(final int id, final byte[][] keyValues) {

        switch (id) {

            case PdfDictionary.S:
                S = keyValues;
                break;

            default:
                super.setStringArray(id, keyValues);
        }
    }

    @Override
    public float[] getFloatArray(final int id) {

        switch (id) {

            case PdfDictionary.Background:
                return Background;

            case PdfDictionary.CardBackground:
                return CardBackground;

            case PdfDictionary.CardBorder:
                return CardBorder;

            case PdfDictionary.PrimaryText:
                return PrimaryText;

            case PdfDictionary.SecondaryText:
                return SecondaryText;

            default:
                return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch (id) {
            
            case PdfDictionary.Background:
                Background = value;
                break;
                
            case PdfDictionary.CardBackground:
                CardBackground = value;
                break;
                
            case PdfDictionary.CardBorder:
                CardBorder = value;
                break;
                
            case PdfDictionary.PrimaryText:
                PrimaryText = value;
                break;
                
            case PdfDictionary.SecondaryText:
                SecondaryText = value;
                break;
                
            default:
                super.setFloatArray(id, value);
        }
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Collection;
    }
}

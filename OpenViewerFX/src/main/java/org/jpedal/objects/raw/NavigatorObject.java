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
 * NavigatorObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.StringUtils;

public class NavigatorObject extends PdfObject{
    
    String ID, Desc, Locale, Version, Icon, Name, SWF, Category, APIVersion, LoadType;
    
    byte[] rawID, rawDesc, rawLocale, rawVersion, rawIcon, rawName, rawSWF, rawCategory, rawAPIVersion, rawLoadType;
    
    PdfObject InitialFields, Strings;
        
    public NavigatorObject(final String ref) {
        super(ref);
    }

    public NavigatorObject(final int ref, final int gen) {
        super(ref, gen);
    }
       
    @Override
    public PdfObject getDictionary(final int id){

        switch(id){

            case PdfDictionary.InitialFields:
        		return InitialFields;
                
            case PdfDictionary.Strings:
        		return Strings;
            
            default:
                return super.getDictionary(id);
        }
    }
    
    @Override
    public void setDictionary(final int id, final PdfObject value){

    	value.setID(id);

        switch(id){
            
            case PdfDictionary.InitialFields:
        		InitialFields=value;
        	break;

            case PdfDictionary.Strings:
        		Strings=value;
        	break;
            
            default:
                super.setDictionary(id, value);
        }
    }
        
    @Override
    public String getTextStreamValue(int id){
        
        switch(id){
            case PdfDictionary.ID:
                if(ID==null && rawID!=null){
                    ID = StringUtils.getTextString(rawID, false);
                }
                return ID;
                
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
                
            case PdfDictionary.Locale:
                if(Locale==null && rawLocale!=null){
                    Locale = StringUtils.getTextString(rawLocale, false);
                }
                return Locale;
                
            case PdfDictionary.Version:
                if(Version==null && rawVersion!=null){
                    Version = StringUtils.getTextString(rawVersion, false);
                }
                return Version;
                
            case PdfDictionary.Icon:
                if(Icon==null && rawIcon!=null){
                    Icon = StringUtils.getTextString(rawIcon, false);
                }
                return Icon;
                
            case PdfDictionary.SWF:
                if(SWF==null && rawSWF!=null){
                    SWF = StringUtils.getTextString(rawSWF, false);
                }
                return SWF;
                
            case PdfDictionary.Category:
                if(Category==null && rawCategory!=null){
                    Category = StringUtils.getTextString(rawCategory, false);
                }
                return Version;
                
            case PdfDictionary.APIVersion:
                if(APIVersion==null && rawAPIVersion!=null){
                    APIVersion = StringUtils.getTextString(rawAPIVersion, false);
                }
                return APIVersion;
        }
        
        return super.getTextStreamValue(id);
    }
    
    @Override
    public byte[] getTextStreamValueAsByte(int id){
        //rawIcon, rawSWF, rawCategory, rawAPIVersion;
        switch(id){
            case PdfDictionary.ID:
                return rawID;
                
            case PdfDictionary.Name:
                return rawName;
                
            case PdfDictionary.Desc:
                return rawDesc;
                
            case PdfDictionary.Locale:
                return rawLocale;
                
            case PdfDictionary.Version:
                return rawVersion;
                
            case PdfDictionary.Icon:
                return rawIcon;
                
            case PdfDictionary.SWF:
                return rawSWF;
                
            case PdfDictionary.Category:
                return rawCategory;
                
            case PdfDictionary.APIVersion:
                return rawAPIVersion;
        }
        
        return super.getTextStreamValueAsByte(id);
    }
        
    @Override
    public void setTextStreamValue(int id, String value){
        
        switch(id){
            case PdfDictionary.ID:
                ID = value;
                break;
                
            case PdfDictionary.Name:
                Name = value;
                break;
                
            case PdfDictionary.Desc:
                Desc = value;
                break;
                
            case PdfDictionary.Locale:
                Locale = value;
                break;
                
            case PdfDictionary.Version:
                Version = value;
                break;
                
            case PdfDictionary.Icon:
                Icon = value;
                break;
                
            case PdfDictionary.SWF:
                SWF = value;
                break;
                
            case PdfDictionary.Category:
                Version = value;
                break;
                
            case PdfDictionary.APIVersion:
                APIVersion = value;
                break;
                
            default:
                super.setTextStreamValue(id, value);
        }
    }
    
    @Override
    public void setTextStreamValue(int id, byte[] value){
        //rawIcon, rawSWF, rawCategory, rawAPIVersion;
        switch(id){
            case PdfDictionary.ID:
                rawID = value;
                ID = null;
                break;
                
            case PdfDictionary.Name:
                rawName = value;
                Name = null;
                break;
                
            case PdfDictionary.Desc:
                rawDesc = value;
                Desc = null;
                break;
                
            case PdfDictionary.Locale:
                rawLocale = value;
                Locale = null;
                break;
                
            case PdfDictionary.Version:
                rawVersion = value;
                Version = null;
                break;
                
            case PdfDictionary.Icon:
                rawIcon = value;
                Icon = null;
                break;
                
            case PdfDictionary.SWF:
                rawSWF = value;
                SWF = null;
                break;
                
            case PdfDictionary.Category:
                rawCategory = value;
                Category = null;
                break;
                
            case PdfDictionary.APIVersion:
                rawAPIVersion = value;
                APIVersion = null;
                break;
                
            default:
                super.setTextStreamValue(id, value);
        }
    }
    
    @Override
    public String getName(final int id) {

        switch (id) {

            case PdfDictionary.LoadType:
                if(LoadType==null && rawLoadType!=null) {
                    LoadType = new String(rawLoadType);
                }
                return LoadType;

            default:
                return super.getName(id);

        }
    }

    @Override
    public void setName(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.LoadType:
                rawLoadType = value;
                LoadType = null;
                break;

            default:
                super.setName(id, value);

        }
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Navigator;
    }
}

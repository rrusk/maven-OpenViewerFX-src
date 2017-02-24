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
 * FXClientExternalHandler.java
 * ---------------
 */
package org.jpedal.examples.viewer.objects;

import org.jpedal.examples.viewer.gui.FXAdditionalData;
import org.jpedal.external.Options;

/**
 * Additional handlers used in Client
 * 
 * @author markee
 */
public class FXClientExternalHandler extends ClientExternalHandler {
 
    /**Used in JavaFX to display additional objects if decoding with transition*/
    private FXAdditionalData additionaValuesforPage;
    
    @Override
    public void addExternalHandler(final Object newHandler, final int type) {
        
        switch (type) {
            
            case Options.JavaFX_ADDITIONAL_OBJECTS:
                additionaValuesforPage = (FXAdditionalData)newHandler;
                break;
                
            default:
                super.addExternalHandler(newHandler, type);
                
        }
    } 
    
    @Override
    public Object getExternalHandler(final int type) {
        
        switch (type) {
               
            case Options.JavaFX_ADDITIONAL_OBJECTS:
                return additionaValuesforPage;
              
            default:
                return super.getExternalHandler(type);
                
        }
    }
}

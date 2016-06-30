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
 * GUIAnnotationPanel.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.generic;

import java.awt.Color;
import org.jpedal.PdfDecoderInt;

public interface GUIAnnotationPanel {
    
    public boolean addPanel();
    
    
    public Object getAnnotationType();
    
    public void clearAnnotationType();
    
    
    public void addAnnotationForWriting(Object annot);
    
    public void addUnsavedAnnotationsToPage(PdfDecoderInt decode_pdf);

    public void addAnnotationToDisplay(PdfDecoderInt decode_pdf, int[] types, Color[] cols, Object[] objs);
    
    public boolean annotationAdded();

    public void clearAnnotations();
    
    public void saveAnnotations(String input, String output);
    
    public void saveForms(String input, String output, Object[] objArr);
    
    public void dispose();
    
    public Object getDisplayPanel();
}

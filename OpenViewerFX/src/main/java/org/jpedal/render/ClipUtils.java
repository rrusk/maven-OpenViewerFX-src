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
 * ClipUtils.java
 * ---------------
 */
package org.jpedal.render;


import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

/**
 * static helper methods for Clip code
 */
public class ClipUtils {

    /**
     * Increases clip size without altering input area
     *
     * @param clip The clipping areas that needs increasing
     * @return Area for the modified clip size
     */
    public static Area convertPDFClipToJavaClip(final Area clip) {

        if (clip != null) {
            //Increase clips size by 1 pixel in all direction as pdf clip includes bounds,
            //java only handles inside of bounds
            final double sx = (clip.getBounds2D().getWidth() + 2) / clip.getBounds2D().getWidth();
            final double sy = (clip.getBounds2D().getHeight() + 2) / clip.getBounds2D().getHeight();
            final double posX = clip.getBounds2D().getX();
            final double posY = clip.getBounds2D().getY();

            final Area a = (Area) clip.clone();
            a.transform(AffineTransform.getTranslateInstance(-posX, -posY));
            a.transform(AffineTransform.getScaleInstance(sx, sy));
            a.transform(AffineTransform.getTranslateInstance(posX - 1, posY - 1));

            return a;
        }
        return clip;
    }
}

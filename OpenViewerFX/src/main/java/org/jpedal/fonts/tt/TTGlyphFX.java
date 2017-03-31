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
 * TTGlyphFX.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import java.io.Serializable;

import javafx.collections.ObservableList;
import javafx.scene.shape.*;
import org.jpedal.fonts.tt.hinting.TTVM;


/**
 * Provides addition javafx code if using JavaFX module
 */
public class TTGlyphFX extends BaseTTGlyph implements Serializable {

    /**
     * paths for the letter, marked as transient so it wont be serialized
     */
    transient Path pathsFX;

    /**
     * Unhinted constructor
     */
    public TTGlyphFX(final Glyf currentGlyf, final FontFile2 glyfTable, final Hmtx currentHmtx, final int idx, final float unitsPerEm, final String baseFontName) {

        super(currentGlyf, glyfTable, currentHmtx, idx, unitsPerEm, baseFontName);

    }

    /**
     * Hinted constructor
     */
    public TTGlyphFX(final Glyf currentGlyf, final FontFile2 glyfTable, final Hmtx currentHmtx, final int idx, final float unitsPerEm, final TTVM vm) {

        super(currentGlyf, glyfTable, currentHmtx, idx, unitsPerEm, vm);

    }

    /**
     * create the actual shape
     */
    @Override
    public void createPaths(final int[] pX, final int[] pY, final boolean[] onCurve, final boolean[] endOfContour, final int endIndex) {

        //allow for bum data
        if (endOfContour == null) {
            return;
        }

        /*
         * scan data and adjust glyfs after first if do not end in contour
         */

        final int ptCount = endOfContour.length;

        int start = 0, firstPt = -1;
        for (int ii = 0; ii < ptCount; ii++) {

            if (endOfContour[ii]) {

                if (firstPt != -1 && (!onCurve[start] || !onCurve[ii])) { //last point not on curve and we have a first point

                    final int diff = firstPt - start;
                    int newPos;

                    //make a deep copy of values
                    final int pXlength = pX.length;
                    final int[] old_pX = new int[pXlength];
                    System.arraycopy(pX, 0, old_pX, 0, pXlength);

                    final int[] old_pY = new int[pXlength];
                    System.arraycopy(pY, 0, old_pY, 0, pXlength);

                    final boolean[] old_onCurve = new boolean[pXlength];
                    System.arraycopy(onCurve, 0, old_onCurve, 0, pXlength);

                    //rotate values to ensure point at start
                    for (int oldPos = start; oldPos < ii + 1; oldPos++) {

                        newPos = oldPos + diff;
                        if (newPos > ii) {
                            newPos -= (ii - start + 1);
                        }
                        pX[oldPos] = old_pX[newPos];
                        pY[oldPos] = old_pY[newPos];
                        onCurve[oldPos] = old_onCurve[newPos];

                    }
                }

                //reset values
                start = ii + 1;
                firstPt = -1;

            } else if (onCurve[ii] && firstPt == -1) { //track first point
                firstPt = ii;
            }

        }

        boolean isFirstDraw = true;

        if (pathsFX == null) {
            pathsFX = new Path();
            pathsFX.setFillRule(FillRule.NON_ZERO);
        }

        final int c = pX.length;
        int fc = -1;

        //find first end contour
        for (int jj = 0; jj < c; jj++) {
            if (endOfContour[jj]) {
                fc = jj + 1;
                jj = c;
            }
        }

        int x1, y1, x2 = 0, y2 = 0, x3 = 0, y3 = 0;

        x1 = pX[0];
        y1 = pY[0];

        final ObservableList<PathElement> elements = pathsFX.getElements();

        elements.add(new MoveTo(x1, y1));

        int xs = 0, ys = 0, lc = 0;
        boolean isEnd = false;

        for (int j = 0; j < endIndex; j++) {

            final int p = j % fc;
            int p1 = (j + 1) % fc;
            int p2 = (j + 2) % fc;
            int pm1 = (j - 1) % fc;

            /*special cases
             *
             *round up to last point at end
             *First point
             */
            if (j == 0) {
                pm1 = fc - 1;
            }
            if (p1 < lc) {
                p1 += lc;
            }
            if (p2 < lc) {
                p2 += lc;
            }

            //allow for wrap around on contour
            if (endOfContour[j]) {
                isEnd = true;

                if (onCurve[fc]) {
                    xs = pX[fc];
                    ys = pY[fc];
                } else {
                    xs = pX[j + 1];
                    ys = pY[j + 1];
                }

                //remember start point
                lc = fc;
                //find next contour
                for (int jj = j + 1; jj < c; jj++) {
                    if (endOfContour[jj]) {
                        fc = jj + 1;
                        jj = c;
                    }
                }
            }

            if (lc == fc && onCurve[p]) {
                j = c;
            } else {

                if (onCurve[p] && onCurve[p1]) { //straight line
                    x3 = pX[p1];
                    y3 = pY[p1];
                    elements.add(new LineTo(x3, y3));

                    isFirstDraw = false;
                    //curves
                } else if (j < (c - 3) && ((fc - lc) > 1 || fc == lc)) {
                    boolean checkEnd = false;
                    if (onCurve[p] && !onCurve[p1] && onCurve[p2]) { //2 points + control

                        x1 = pX[p];
                        y1 = pY[p];
                        x2 = pX[p1];
                        y2 = pY[p1];
                        x3 = pX[p2];
                        y3 = pY[p2];
                        j++;
                        checkEnd = true;

                    } else if (onCurve[p] && !onCurve[p1] && !onCurve[p2]) { //1 point + 2 control

                        x1 = pX[p];
                        y1 = pY[p];
                        x2 = pX[p1];
                        y2 = pY[p1];
                        x3 = midPt(pX[p1], pX[p2]);
                        y3 = midPt(pY[p1], pY[p2]);
                        j++;

                        checkEnd = true;

                    } else if (!onCurve[p] && !onCurve[p1] && (!endOfContour[p2] || fc - p2 == 1)) { // 2 control + 1 point (final check allows for last point to complete loop

                        x1 = midPt(pX[pm1], pX[p]);
                        y1 = midPt(pY[pm1], pY[p]);
                        x2 = pX[p];
                        y2 = pY[p];

                        x3 = midPt(pX[p], pX[p1]);
                        y3 = midPt(pY[p], pY[p1]);

                    } else if (!onCurve[p] && onCurve[p1]) { // 1 control + 2 point

                        x1 = midPt(pX[pm1], pX[p]);
                        y1 = midPt(pY[pm1], pY[p]);
                        x2 = pX[p];
                        y2 = pY[p];
                        x3 = pX[p1];
                        y3 = pY[p1];

                    }

                    if (isFirstDraw) {
                        elements.add(new MoveTo(x1, y1));

                        isFirstDraw = false;

                    }

                    if (!(endOfContour[p] && p > 0 && endOfContour[p - 1])) {
                        elements.add(new CubicCurveTo(x1, y1, x2, y2, x3, y3));
                    }


                    /*if end after curve, roll back so we pick up the end*/
                    if (checkEnd && endOfContour[j]) {

                        isEnd = true;

                        xs = pX[fc];
                        ys = pY[fc];
                        //remmeber start point
                        lc = fc;
                        //find next contour
                        for (int jj = j + 1; jj < c; jj++) {
                            if (endOfContour[jj]) {
                                fc = jj + 1;
                                jj = c;
                            }
                        }
                    }
                }

                if (endOfContour[p]) {
                    elements.add(new ClosePath());
                }

                if (isEnd) {
                    elements.add(new MoveTo(xs, ys));
                    isEnd = false;
                }

//                if(debug){
//                    try{
//                        if(img==null)
//                            img=new java.awt.image.BufferedImage(800,800, java.awt.image.BufferedImage.TYPE_INT_ARGB);
//                        
//                        Graphics2D g2= img.createGraphics();
//                        g2.setColor(java.awt.Color.green);
//                        g2.draw(current_path);
//                        
//                        final String key= String.valueOf(p);
//                        
//                        org.jpedal.gui.ShowGUIMessage.showGUIMessage(key,img,key);
//                        
//                    }catch(Exception e){
//                        //tell user and log
//                        if(LogWriter.isOutput())
//                            LogWriter.writeLog("Exception: "+e.getMessage());
//                    }
//                }
            }
        }
    }

    @Override
    public Path getPath() {

        /*
         * we need a new PAth each time so we recreate
         */
        pathsFX = null;

        createGlyph(false);

        return pathsFX;
    }
}

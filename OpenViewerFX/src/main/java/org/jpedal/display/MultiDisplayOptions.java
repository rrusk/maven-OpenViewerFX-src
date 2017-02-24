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
 * MultiDisplayOptions.java
 * ---------------
 */

package org.jpedal.display;

import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.LogWriter;

/**
 *
 * @author markee
 */
public class MultiDisplayOptions {
    
    private boolean turnoverOn =  GUIDisplay.default_turnoverOn;

    private boolean separateCover = GUIDisplay.default_separateCover;
    
    private int[] xReached, yReached, pageW, pageH;
    
    private boolean isGeneratingOtherPages;
    
    private boolean running;
    
    private int startViewPage=1,endViewPage;
    
    private int pageNumber;
    
    private final PdfPageData pageData = new PdfPageData();
 
    /**
     * @return the turnoverOn
     */
    public boolean isTurnoverOn() {
        return turnoverOn;
    }

    /**
     * @param turnoverOn the turnoverOn to set
     */
    public void setTurnoverOn(final boolean turnoverOn) {
        this.turnoverOn = turnoverOn;
    }

    /**
     * @return the separateCover
     */
    public boolean isSeparateCover() {
        return separateCover;
    }

    /**
     * @param separateCover the separateCover to set
     */
    public void setSeparateCover(final boolean separateCover) {
        this.separateCover = separateCover;
    }

    void resetValues(final int pageCount) {
        setxReached(new int[pageCount+1]);
        setyReached(new int[pageCount+1]);
        setPageW(new int[pageCount+1]);
        setPageH(new int[pageCount+1]);
    }

    public void setReachedToNull() {
        setxReached(null);
            
    }
    public void setPageValuesToNull(){
        setPageH(null);
        setPageW(null);
    }

    /**
     * @return the pageH
     */
    public int[] getPageH() {
        return pageH;
    }
    
    public int getPageH(final int i) {
        return pageH[i];
    }
    
    /**
     * @param pageH the pageH to set
     */
    public void setPageH(final int[] pageH) {
        this.pageH = pageH;
    }

    /**
     * @return the pageW
     */
    public int[] getPageW() {
        return pageW;
    }
    
    public int getPageW(final int i) {
        return pageW[i];
    }

    /**
     * @param pageW the pageW to set
     */
    public void setPageW(final int[] pageW) {
        this.pageW = pageW;
    }

    /**
     * @return the yReached
     */
    public int[] getyReached() {
        return yReached;
    }
    
    public int getyReached(final int i) {
        return yReached[i];
    }

    /**
     * @param yReached the yReached to set
     */
    public void setyReached(final int[] yReached) {
        this.yReached = yReached;
    }

    /**
     * @return the xReached
     */
    public int[] getxReached() {
        return xReached;
    }
    
    public int getxReached(final int i) {
        return xReached[i];
    }

    /**
     * @param xReached the xReached to set
     */
    public void setxReached(final int[] xReached) {
        this.xReached = xReached;
    }

    /**
     * @return the isGeneratingOtherPages
     */
    public boolean isIsGeneratingOtherPages() {
        return isGeneratingOtherPages;
    }

    /**
     * @param isGeneratingOtherPages the isGeneratingOtherPages to set
     */
    public void setIsGeneratingOtherPages(final boolean isGeneratingOtherPages) {
        this.isGeneratingOtherPages = isGeneratingOtherPages;
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(final boolean running) {
        this.running = running;
    }

    /**
     * @return the startViewPage
     */
    public int getStartViewPage() {
        return startViewPage;
    }

    /**
     * @param startViewPage the startViewPage to set
     */
    public void setStartViewPage(final int startViewPage) {
        this.startViewPage = startViewPage;
    }

    /**
     * @return the endViewPage
     */
    public int getEndViewPage() {
        return endViewPage;
    }

    /**
     * @param endViewPage the endViewPage to set
     */
    public void setEndViewPage(final int endViewPage) {
        this.endViewPage = endViewPage;
    }
    public void calcDisplayRangeForFacing(){

        final int pageCount=pageData.getPageCount();

        if (separateCover) {
                if(pageCount==2){ //special case
                    startViewPage = 1;
                    endViewPage = 2;         
                }else{
                    startViewPage = pageNumber;

                    if(startViewPage ==1){ //special case
                        endViewPage=1;
                    }else if((startViewPage & 1)!=1){ //right even page selected
                        startViewPage = pageNumber;
                        endViewPage = pageNumber+1;
                    }else{ //left odd page selected
                        startViewPage = pageNumber-1;
                        endViewPage = pageNumber;
                    }
                }
            } else {
                 startViewPage = pageNumber - (1 - (pageNumber & 1));
            endViewPage = startViewPage +1;
            }
    }

    /**
     * @return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @param pageNumber the pageNumber to set
     */
    public void setPageNumber(final int pageNumber) {
        this.pageNumber = pageNumber;
    }
    public void waitToDieThred(){
        //wait to die
        while (running) {
            // System.out.println("Waiting to die");
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
        }
    }
}

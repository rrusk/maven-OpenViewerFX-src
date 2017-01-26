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
 * PageLabels.java
 * ---------------
 */
package org.jpedal.io;

import java.util.HashMap;
import org.jpedal.objects.raw.PageLabelObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

/**
 * convert names to refs
 */
public class PageLabels extends HashMap<Integer, String>{

    private final PdfFileReader objectReader;

    private final int pageCount;

    private static final String symbolLowerCase[]={"m","cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};
    private static final String symbolUpperCase[]={"M","CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    private static final String lettersLowerCase[]={"a","b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m","n",
                                                "o", "p", "q", "r", "s", "t", "u", "v", "x", "w", "y", "z"};
    private static final String lettersUpperCase[]={"A","B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M","N",
                                                "O", "P", "Q", "R", "S", "T", "U", "V", "X", "W", "Y", "Z"};
    private static final int[] power={1000,900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

    PageLabels(PdfFileReader objectReader, int pageCount) {
        this.objectReader=objectReader;
        this.pageCount=pageCount;
    }
    
    void readLabels(PdfObject pageObj) {
      
        PdfArrayIterator numList =pageObj.getMixedArray(PdfDictionary.Nums);
        
        if(numList!=null && objectReader!=null && numList.hasMoreTokens()){
            
            int endPage,numbType,St,pageNum;
            String convertedPage,pageLabel;
            PageLabelObject labelObj;
            boolean isFirstToken = true;

            //read first page values
            int startPage=numList.getNextValueAsInteger(true)+1;

            while (numList.hasMoreTokens()) {

                //read LabelObject
                labelObj = getObject(numList.getNextValueAsByte(true));

                numbType = labelObj.getNameAsConstant(PdfDictionary.S);
                pageLabel = labelObj.getTextStreamValue(PdfDictionary.P);

                St = labelObj.getInt(PdfDictionary.St);
                if (St > 0) {
                    pageNum = St;
                } else {
                    pageNum = 1;
                }

                // Do not store PageLabels if the only token is /S /D (default behavior). E.g. sample_pdfs_html/12jul/1997.pdf
                // Do store in any other case (even if P matches page number). E.g. sample_pdfs_html/12jul/1130MA0711w.pdf
                // This is the same behavior as Adobe Reader.
                if (isFirstToken && numbType == PdfDictionary.D &&
                        pageLabel == null && St <= 0 && !numList.hasMoreTokens()) {
                    break;
                }
                isFirstToken = false;

                if (numList.hasMoreTokens()) {
                    endPage = numList.getNextValueAsInteger(true) + 1;
                } else {
                    endPage = pageCount + 1;
                }

                //now decode type of naming and fill range
                for (int page = startPage; page < endPage; page++) {

                    if (pageLabel != null) {
                        convertedPage = pageLabel + getNumberValue(numbType, pageNum);
                    } else {
                        convertedPage = getNumberValue(numbType, pageNum);
                    }

                    this.put(page, convertedPage);

                    pageNum++;
                }

                startPage = endPage;
            }
        }
    }

    private PageLabelObject getObject(byte[] data) {
        
        final PageLabelObject labelObj  = new PageLabelObject(new String(data));

        if(data[0]=='<') {
            labelObj.setStatus(PdfObject.UNDECODED_DIRECT);
        } else {
            labelObj.setStatus(PdfObject.UNDECODED_REF);
        }
        labelObj.setUnresolvedData(data,PdfDictionary.PageLabels);
        
        final ObjectDecoder objectDecoder=new ObjectDecoder(this.objectReader);
        objectDecoder.checkResolved(labelObj);
        
        return labelObj;
    }

    private static String getNumberValue(int numbType, int page) {
        
        String convertedPage;
    
        switch(numbType){
            case PdfDictionary.a:
                convertedPage=convertLetterToNumber(page, lettersLowerCase);
                break;
                
            case PdfDictionary.A:
                convertedPage=convertLetterToNumber(page, lettersUpperCase);
                break;
                
            case PdfDictionary.D:
                convertedPage=String.valueOf(page);
                break;
                
            case PdfDictionary.R:
                convertedPage=convertToRoman(page,symbolUpperCase);
                break;
                
            case PdfDictionary.r:
                convertedPage=convertToRoman(page,symbolLowerCase);
                break;
                
            default:
                convertedPage="";
        }
        return convertedPage;
    }

    private static String convertToRoman(int arabicNumber, final String[] symbols){
        
        final StringBuilder romanNumeral=new StringBuilder();
        int repeat;
        
        for(int x=0; arabicNumber>0; x++){
            repeat=arabicNumber/power[x];
            
            for(int chars=1; chars<=repeat; chars++){
                romanNumeral.append(symbols[x]);
            }
            arabicNumber %= power[x];
        }
        
        return romanNumeral.toString();
    }
    
    private static String convertLetterToNumber(int page, final String[] letters) {
       
        final StringBuilder finalLetters=new StringBuilder();
        int repeat = page/26;               
        int remainder = page%26;
        
        if (repeat>0) {
            for (int x=0; x<repeat; x++) {
                finalLetters.append(letters[25]);
            }
        }
        if (remainder!=0) {
            finalLetters.append(letters[remainder-1]);
        }
        return finalLetters.toString();
    }
}

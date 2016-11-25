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
 * OutlineData.java
 * ---------------
 */
package org.jpedal.objects.outlines;

import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.acroforms.actions.DestHandler;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * encapsulate the Outline data
 */
public class OutlineData {

	private Document OutlineDataXML;

    private final Map<String, PdfObject> DestObjs=new HashMap<String, PdfObject>();

	/**create list when object initialised*/
	public OutlineData(){

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			OutlineDataXML=factory.newDocumentBuilder().newDocument();
		} catch (final ParserConfigurationException e) {
			System.err.println("Exception "+e+" generating XML document");
		}
	}

	/**return the list*/
	public Document getList(){
		return OutlineDataXML;
	}

	/**
	 * read the outline data
	 */
	@SuppressWarnings("UnusedReturnValue")
    public int readOutlineFileMetadata(final PdfObject OutlinesObj, final PdfObjectReader currentPdfFile) {

		final int count=OutlinesObj.getInt(PdfDictionary.Count);

		final PdfObject FirstObj=OutlinesObj.getDictionary(PdfDictionary.First);
        currentPdfFile.checkResolved(FirstObj);
        if(FirstObj !=null){

			final Element root=OutlineDataXML.createElement("root");

			OutlineDataXML.appendChild(root);

			final int level=0;
			readOutlineLevel(root,currentPdfFile, FirstObj, level, false);

		}

		return count;
	}

	/**
	 * read a level
	 */
	private void readOutlineLevel(final Element root, final PdfObjectReader currentPdfFile, PdfObject outlineObj, final int level, boolean isClosed) {

		String ID;
		int page;
		Element child=OutlineDataXML.createElement("title");
        PdfObject FirstObj, NextObj;

		while(true){

			ID=outlineObj.getObjectRefAsString();
			
			/*
			 * process and move onto next value
			 */
			FirstObj=outlineObj.getDictionary(PdfDictionary.First);
            currentPdfFile.checkResolved(FirstObj);
            NextObj=outlineObj.getDictionary(PdfDictionary.Next);
            currentPdfFile.checkResolved(NextObj);

            final int numberOfItems=outlineObj.getInt(PdfDictionary.Count);

            if(numberOfItems!=0) {
                isClosed = numberOfItems < 0;
            }

            final PdfArrayIterator dest = DestHandler.getDestFromObject(outlineObj, currentPdfFile);
			page = DestHandler.getPageNumberFromLink(dest, currentPdfFile); //set to -1 as default
			final Object[] zoomArray = DestHandler.getZoomFromDest(dest, currentPdfFile);

			//add title to tree
			final byte[] titleData=outlineObj.getTextStreamValueAsByte(PdfDictionary.Title);
			if(titleData !=null){

				final String title= StringUtils.getTextString(titleData, false);

				//add node
				child=OutlineDataXML.createElement("title");
				root.appendChild(child);
				child.setAttribute("title",title);

			}

            child.setAttribute("isClosed", String.valueOf(isClosed));

            //store Object containing Dest so we can access
            if(outlineObj!=null) {
                DestObjs.put(ID, outlineObj);
            }

            if(page==PdfDictionary.Null){
                child.setAttribute("page", "-1");
            }else{
                child.setAttribute("page", String.valueOf(page));
            }

			if (zoomArray != null) {
				child.setAttribute("zoom", DestHandler.convertZoomArrayToString(zoomArray));
			}

            child.setAttribute("level", String.valueOf(level));
            child.setAttribute("objectRef",ID);

			if(FirstObj!=null) {
                readOutlineLevel(child, currentPdfFile, FirstObj, level + 1, isClosed);
            }

			if(NextObj==null) {
                break;
            }

			outlineObj = NextObj;            
		}
	}
    
    /**
     * not recommended for general usage
     * @param ref
     * @return Aobj
     */
    public PdfObject getAobj(final String ref) {
        return DestObjs.get(ref);
    }
}

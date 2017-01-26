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
 * BrokenRefTable.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.exception.PdfSecurityException;
import org.jpedal.io.RandomAccessBuffer;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class BrokenRefTable {

     /**
     * find a valid offsetRef
     */
    public static String findOffsets(final RandomAccessBuffer pdf_datafile, final Offsets offset) throws PdfSecurityException {

    	LogWriter.writeLog("Corrupt xref table - trying to find objects manually");
        
        String root_id = "",line=null;
        int i=0;

        try {
            pdf_datafile.seek(0);
        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " reading line");
        }
        
        while (true) {

            try {
                i = (int) pdf_datafile.getFilePointer();
                line = pdf_datafile.readLine();
            } catch (final Exception e) {
            	LogWriter.writeLog("Exception " + e + " reading line");
            }
            if (line == null) {
                break;
            }

			root_id = readRootObject(pdf_datafile, offset, line, i, root_id);
		}

        return root_id;
    }

	private static String readRootObject(final RandomAccessBuffer pdf_datafile, final Offsets offset, String line, int i, String root_id) throws PdfSecurityException {

		int pointer;

    	if (line.contains(" obj")) {

            pointer = line.indexOf(' ');
            try {
                if (pointer > 0) {
                    offset.storeObjectOffset(Integer.parseInt(line.substring(0, pointer)), i, 1, false, true);
                }
            }catch(Exception e){
                LogWriter.writeLog("[PDF] Exception "+e+" Unable to manually read line "+line);
            }
        } else if (line.contains("/Root")) {
            root_id=readRootID(line,pdf_datafile);
        } else if (line.contains("/Encrypt")) {
            //too much risk on corrupt file
            throw new PdfSecurityException("Corrupted, encrypted file");
        }
		return root_id;
	}

	private static String readRootID(String line, RandomAccessBuffer pdf_datafile) {

    	int start = line.indexOf("/Root") + 5;

		//read actual value which may be on this line or next
		int pointer = line.indexOf('R', start);
		while(pointer==-1){
			start=0;
			try{
				line = pdf_datafile.readLine();
			} catch (final Exception e) {
				LogWriter.writeLog("Exception " + e + " reading line");
			}
			pointer = line.indexOf('R', start);
		}

		return line.substring(start, pointer + 1).trim();
	}

	public static byte[] findFirstRootDict(final RandomAccessBuffer buffer) {		
		long len;
        try {
            buffer.seek(0);
			len = buffer.length();
			while (true) {
				int p = (int) buffer.getFilePointer();
				int x1 = buffer.read();
				int x2 = buffer.read();
				int x3 = buffer.read();
				int x4 = buffer.read();
				int x5 = buffer.read();
				long ps = -1; // start of << 
				long pe = -1;
				if(x1 == 47 && x2 == 82 && x3 == 111 && x4 == 111 && x5 == 116){ //check for /Root
					buffer.seek(p-5);
					while(buffer.getFilePointer() > 0){
						long ss = buffer.getFilePointer();
						int neg = 2;
						if(buffer.read() == 60){
							if(buffer.read() == 60){
								ps = buffer.getFilePointer()-2;
								break;
							}
							neg++;
						}
						buffer.seek(ss - neg);
					}					
					buffer.seek(p+5);
					int braces = 0;
					while(buffer.getFilePointer() < len){
						int pp = (int)buffer.getFilePointer();
						int j1 = buffer.read();
						int j2 = buffer.read();						
						if(j1==60 && j2 == 60){
							braces++;
						}						
						if(j1==62 && j2 == 62){							
							if(braces == 0){
								pe = buffer.getFilePointer();
								break;
							}
							braces--;
						}						
						buffer.seek(pp+1);
					}
					if(pe >-1 && ps >-1){
						byte[] bb = new byte[(int)(pe - ps)];
						buffer.seek(ps);
						buffer.read(bb);
						return bb;
					}
				}
				buffer.seek(p+1);
				if((buffer.getFilePointer()+5) > len){
					break;
				}
			}
        } catch (final Exception e) {
            LogWriter.writeLog("Exception " + e + " corrupted stream");
        }      		
		return null;          
    }
}



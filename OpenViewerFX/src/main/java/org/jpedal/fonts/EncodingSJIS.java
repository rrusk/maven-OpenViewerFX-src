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
 * EncodingSJIS.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import org.jpedal.utils.LogWriter;

public class EncodingSJIS implements CmapEncoding {

	private final int[] uniMap = new int[65536];

	public EncodingSJIS() throws FileNotFoundException {
		ClassLoader loader = this.getClass().getClassLoader();
		String line;
		int rawVal, unicodeVal;
		final BufferedReader input_stream;

		try {
			input_stream = new BufferedReader(
					new InputStreamReader(loader.getResourceAsStream("org/jpedal/res/pdf/jis.cfg")));
			while (true) {
				line = input_stream.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("0") && line.contains("#")) {
					final StringTokenizer values = new StringTokenizer(line);
					final String xx = values.nextToken().substring(2);
					rawVal = Integer.parseInt(xx, 16);
					unicodeVal = Integer.parseInt(values.nextToken().substring(2), 16); //unicode
					uniMap[rawVal] = unicodeVal;
				}
			}
			input_stream.close();
		} catch (final Exception e) {
			LogWriter.writeLog("Exception: " + e.getMessage());
		}
	}

	@Override
	public int getUnicodeValue(int cid) {
		return uniMap[cid];
	}

}

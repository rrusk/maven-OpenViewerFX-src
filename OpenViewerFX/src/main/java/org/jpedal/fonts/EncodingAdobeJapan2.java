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
 * EncodingAdobeJapan2.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author suda
 */
public class EncodingAdobeJapan2 implements CmapEncoding {

	CmapSpec adobeJapanCmapSpec = null;

	public EncodingAdobeJapan2() {
		ClassLoader loader = this.getClass().getClassLoader();
		BufferedReader input_stream = new BufferedReader(
				new InputStreamReader(loader.getResourceAsStream("org/jpedal/res/pdf/japan2unicode.cfg")));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String line;
		try {
			while (true) {
				line = input_stream.readLine();
				if (line == null) {
					break;
				} else {
					bos.write(line.getBytes());
					bos.write(10);
				}
			}
			byte[] data = bos.toByteArray();
			if (data.length > 0) {
				adobeJapanCmapSpec = new CmapSpec(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getUnicodeValue(int v) {
		return adobeJapanCmapSpec.getUnicodeValue(v);
	}

	public static void main(String[] args) {
		EncodingAdobeJapan2 ec = new EncodingAdobeJapan2();
		System.out.println(ec.getUnicodeValue(633));
	}
	
}

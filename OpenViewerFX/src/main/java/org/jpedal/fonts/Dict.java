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
 * Dict.java
 * ---------------
 */
package org.jpedal.fonts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Dict {

	private int pos = 0;
	private final byte[] data;
	public Map<Integer, Number[]> entries = new LinkedHashMap<Integer, Number[]>();

	public Dict privateDict = null;
	public int[] charStringIndex = null;
	public int ascent = 800;
	public int descent = -200;
	public CIDCharset charset = null;
	public CIDEncoding encoding = null;

	Dict(byte[] data) {
		List<Number> operands = new ArrayList<Number>();
		this.data = data;
		int end = data.length;
		while (pos < end) {
			int b = data[pos] & 0xff;
			if (b <= 21) {
				if (b == 12) {
					b = (b << 8) | (data[++pos] & 0xff);
				}
				Number[] operandsArr = new Number[operands.size()];
				operandsArr = operands.toArray(operandsArr);
				entries.put(b, operandsArr);
				operands = new ArrayList<Number>();
				++pos;
			} else {
				operands.add(parseOperand());
			}
		}
	}

	private Number parseOperand() {
		int value = data[pos++] & 0xff;
		if (value == 30) {
			return parseFloatOperand();
		} else if (value == 28) {
			value = data[pos++] & 0xff;
			value = ((value << 24) | ((data[pos++] & 0xff) << 16)) >> 16;
			return value;
		} else if (value == 29) {
			value = data[pos++] & 0xff;
			value = (value << 8) | (data[pos++] & 0xff);
			value = (value << 8) | (data[pos++] & 0xff);
			value = (value << 8) | (data[pos++] & 0xff);
			return value;
		} else if (value >= 32 && value <= 246) {
			return value - 139;
		} else if (value >= 247 && value <= 250) {
			return ((value - 247) * 256) + (data[pos++] & 0xff) + 108;
		} else if (value >= 251 && value <= 254) {
			return -((value - 251) * 256) - (data[pos++] & 0xff) - 108;
		}
		return null;
	}

	private float parseFloatOperand() {
		StringBuilder str = new StringBuilder();
		int eof = 15;
		String[] lookup = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "E", "E-", null, "-"};
		int length = data.length;
		while (pos < length) {
			int b = data[pos++] & 0xff;
			int b1 = b >> 4;
			int b2 = b & 15;

			if (b1 == eof) {
				break;
			}
			str.append(lookup[b1]);

			if (b2 == eof) {
				break;
			}
			str.append(lookup[b2]);
		}
		return Float.parseFloat(str.toString());
	}

}

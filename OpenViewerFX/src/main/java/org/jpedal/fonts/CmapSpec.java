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
 * CmapSpec.java
 * ---------------
 */
package org.jpedal.fonts;

import java.util.TreeMap;

/**
 * reads adobe cmap files for encoding and mapping
 *
 */
public final class CmapSpec {

	private static final byte[] CHAR256 = {
		//      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, A, B, C, D, E, F,
		1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, // 0
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 1
		1, 0, 0, 0, 0, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 2, // 2
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 2, 0, 2, 0, // 3
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 4
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, // 5
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 6
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, // 7
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 8
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 9
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // A
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // B
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // C
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // D
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // E
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 // F    
	};

	private final byte[] cmapData;
	private long[][] codeSpaceRange;
	private final TreeMap<Long, Integer> notDefRange = new TreeMap<Long, Integer>();
	private final TreeMap<Long, Integer> cidRange = new TreeMap<Long, Integer>();

	private final TreeMap<Long, Integer> bfRange = new TreeMap<Long, Integer>();
	private CmapEncoding cmapEncoding = null;

	private int p = 0;
	private final int len;

	public CmapSpec(byte[] cmapData) {
		this.cmapData = cmapData;
		len = cmapData.length;
		codeSpaceRange = new long[1][2];
		codeSpaceRange[0][1] = 65536;

		int count;

		while (p < len) {
			if ((cmapData[p] & 0xff) == 37) { // % comment marker
				skipLine();
				continue;
			}
			String line = getNextLine().trim();
			if (line.isEmpty()) {
				continue;
			}
			if (line.contains("begincodespacerange")) {	// <00> <80>
				String[] nn = line.split(" ");
				count = Integer.parseInt(nn[0]);
				codeSpaceRange = new long[count][2];
				for (int i = 0; i < count; i++) {
					long start = getIntOrHex();
					long end = getIntOrHex();
					codeSpaceRange[i][0] = start;
					codeSpaceRange[i][1] = end;
				}
			} else if (line.contains("begincidrange")) { // <8141> <8142> 7887
				String[] nn = line.split(" ");
				count = Integer.parseInt(nn[0]);
				for (int i = 0; i < count; i++) {
					long start = getIntOrHex();
					long end = getIntOrHex();
					int cid = (int) getIntOrHex();
					long v = (start << 32L) | end;
					cidRange.put(v, cid);
				}
			} else if (line.contains("begincidchar")) {	// <8143> 8286
				String[] nn = line.split(" ");
				count = Integer.parseInt(nn[0]);
				for (int i = 0; i < count; i++) {
					long start = getIntOrHex();
					int cid = (int) getIntOrHex();
					long v = (start << 32L) | start;
					cidRange.put(v, cid);
				}
			} else if (line.contains("beginnotdefrange")) {	// <00> <1f>1
				String[] nn = line.split(" ");
				count = Integer.parseInt(nn[0]);
				for (int i = 0; i < count; i++) {
					long start = getIntOrHex();
					long end = getIntOrHex();
					int cid = (int) getIntOrHex();
					long v = (start << 32L) | end;
					notDefRange.put(v, cid);
				}
			} else if (line.contains("beginnotdefchar")) {	// <8143> 8286
				String[] nn = line.split(" ");
				count = Integer.parseInt(nn[0]);
				for (int i = 0; i < count; i++) {
					long start = getIntOrHex();
					int cid = (int) getIntOrHex();
					long v = (start << 32L) | start;
					notDefRange.put(v, cid);
				}
			} else if (line.contains("beginbfrange")) { // <8141> <8142> 7887
				String[] nn = line.split(" ");
				count = Integer.parseInt(nn[0]);
				for (int i = 0; i < count; i++) {
					long start = getIntOrHex();
					long end = getIntOrHex();
					int cid = (int) getIntOrHex();
					long v = (start << 32L) | end;
					bfRange.put(v, cid);
				}
			} else if (line.contains("beginbfchar")) {	// <8143> 8286
				String[] nn = line.split(" ");
				count = Integer.parseInt(nn[0]);
				for (int i = 0; i < count; i++) {
					long start = getIntOrHex();
					int cid = (int) getIntOrHex();
					long v = (start << 32L) | start;
					bfRange.put(v, cid);
				}
			}
		}
	}

	public boolean isInCodeSpaceRange(int v) {
		for (long[] ls : codeSpaceRange) {
			if (v >= ls[0] && v <= ls[1]) {
				return true;
			}
		}
		return false;
	}

	public int getCIDValue(int unicode) {
		for (Long key : cidRange.keySet()) {
			int start = (int) (key >>> 32);
			int end = (int) (key & 0xffffffffL);
			if (unicode >= start && unicode <= end) {
				int v = cidRange.get(key);
				return v + (unicode - start);
			}
		}
		return 0;
	}
	
	public int getUnicodeValue(int cid) {
		for (Long key : bfRange.keySet()) {
			int start = (int) (key >>> 32);
			int end = (int) (key & 0xffffffffL);
			if (cid >= start && cid <= end) {
				int v = bfRange.get(key);
				return v + (cid - start);
			}
		}
		return 0;
	}

	private long getIntOrHex() {
		int v;
		final StringBuilder sb = new StringBuilder();
		while (p < len) {
			v = cmapData[p] & 0xff;
			if (v == 60) {
				p++;
				while (p < len) {
					v = cmapData[p++] & 0xff;
					if (v == 62) {
						break;
					}
					sb.append((char) v);
				}
				return Long.parseLong(sb.toString(), 16);
			} else if (isDigit(v)) {
				while (p < len) {
					v = cmapData[p++] & 0xff;
					if (!isDigit(v)) {
						break;
					}
					sb.append((char) v);
				}
				return Long.parseLong(sb.toString());
			} else {
				p++;
			}
		}
		return 0;
	}

	private String getNextLine() {
		final StringBuilder bb = new StringBuilder();
		int v = cmapData[p++] & 0xff;
		OUTER:
		while (p < len) {
			switch (v) {
				case 0xd:
					if (p < len && (cmapData[p] & 0xff) == 0xa) {
						p++;
					}
					break OUTER;
				case 0xa:
					break OUTER;
				default:
					bb.append((char) v);
					v = cmapData[p++] & 0xff;
					break;
			}
		}
		return bb.toString();
	}

	private void skipLine() {
		int v = cmapData[p++] & 0xff;
		while (p < len) {
			switch (v) {
				case -1:
				case 0xa:
					return;
				case 0xd:
					if (p < len && (cmapData[p] & 0xff) == 0xa) {
						p++;
					}
					return;
				default:
					v = cmapData[p++] & 0xff;
			}
		}
	}

	private static boolean isDigit(final int ch) {
		return CHAR256[ch] == 4;
	}

	public CmapEncoding getCmapEncoding() {
		return cmapEncoding;
	}

	public void setCmapEncoding(CmapEncoding cmapEncoding) {
		this.cmapEncoding = cmapEncoding;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("codespacerange \n");
		for (long[] codeSpaceRange1 : codeSpaceRange) {
			sb.append(codeSpaceRange1[0]).append(" ... ").append(codeSpaceRange1[1]).append('\n');
		}
		sb.append("cidrange \n");
		for (Long key : cidRange.keySet()) {
			int start = (int) (key >>> 32);
			int end = (int) (key & 0xffffffffL);
			int v = cidRange.get(key);
			sb.append(start).append(" ... ").append(end).append(" ==> ").append(v).append('\n');
		}

		return sb.toString();
	}

//	public static void main(String[] args) throws java.io.IOException {
//		java.io.File f = new java.io.File("C:\\Users\\suda\\Desktop\\cmaps\\cmapresources_japan1-6\\CMap\\UniJIS2004-UTF16-H");
//		FileInput fi = new FileInput(f);
//		byte[] data = new byte[(int) f.length()];
//		fi.read(data);
//
//		CmapSpec spec = new CmapSpec(data);
//		System.out.println(spec);
//	}
}

//	public static boolean isEOL(final int ch) {
//		return ch == 0xa || ch == 0xd;
//	}
//
//	public static boolean isDelimiter(final int ch) {
//		return CHAR256[ch] == 2;
//	}
//
//	public static boolean isComment(final int ch) {
//		return ch == 0x25;
//	}
//	
//	public static boolean isWhiteSpace(final int ch) {
//		return CHAR256[ch] == 1;
//	}

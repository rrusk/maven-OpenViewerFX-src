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
 * RandomAccessFileBuffer.java
 * ---------------
 */
package org.jpedal.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.FastByteArrayOutputStream;

public class RandomAccessFileBuffer implements RandomAccessBuffer {

	private final String fileName;
	private final RandomAccessFile ra;
	private final boolean isBig;

	private int bp;
	private int bl;
	byte[] temp;
	int tSize = 4096;
	int ts;
	int te;

	public RandomAccessFileBuffer(final String fileName, final String mode) throws IOException {
		this.fileName = fileName;
		final File file = new File(fileName);
		ra = new RandomAccessFile(file, mode);

		isBig = file.length() > Integer.MAX_VALUE;

		if (!isBig) {
			bl = (int) file.length();
			tSize = Math.min(tSize, bl);
			temp = new byte[tSize];
			te = tSize;
			ra.read(temp);
		}
	}

	@Override
	public byte[] getPdfBuffer() {

		final URL url;
		byte[] pdfByteArray = null;
		final InputStream is;
		final FastByteArrayOutputStream os;

		try {
			url = new URL("file:///" + fileName);

			is = url.openStream();
			os = new FastByteArrayOutputStream();

			// Download buffer
			final byte[] buffer = new byte[4096];

			// Download the PDF document
			int read;
			while ((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
			}

			// Close streams
			is.close();

			// Copy output stream to byte array
			pdfByteArray = os.toByteArray();

		} catch (final IOException e) {
			LogWriter.writeLog("[PDF] Exception " + e + " getting byte[] for " + fileName);
		}

		return pdfByteArray;
	}

	@Override
	public long getFilePointer() throws IOException {
		if (isBig) {
			return ra.getFilePointer();
		} else {
			return bp;
		}
	}

	@Override
	public void seek(final long pos) throws IOException {
		if (isBig) {
			ra.seek(pos);
		} else {
			bp = (int) pos;
		}
	}

	@Override
	public int read() throws IOException {
		if (isBig) {
			return ra.read();
		} else {
			if (bp < bl) {
				if (bp >= ts && bp < te) {
					final int v = temp[(bp - ts)] & 0xff;
					bp++;
					return v;
				} else {
					ts = bp;
					te = ts + tSize;
					ra.seek(bp);
					final int max = Math.min(bl - bp, tSize);
					ra.read(temp, 0, max);
					bp++;
					return temp[0] & 0xff;
				}
			}
			return -1;
		}
	}

	@Override
	public String readLine() throws IOException {
		final StringBuilder input = new StringBuilder();
		int c = -1;
		boolean eol = false;

		while (!eol) {
			switch (c = read()) {
				case -1:
				case '\n':
					eol = true;
					break;
				case '\r':
					eol = true;
					final long cur = getFilePointer();
					if ((read()) != '\n') {
						seek(cur);
					}
					break;
				default:
					input.append((char) c);
					break;
			}
		}

		if ((c == -1) && (input.length() == 0)) {
			return null;
		}
		return input.toString();
	}

	@Override
	public long length() throws IOException {
		return ra.length();
	}

	@Override
	public void close() throws IOException {
		if (ra != null) {
			ra.close();
		}
	}

	@Override
	public int read(final byte[] b) throws IOException {
		if (isBig) {
			return ra.read(b);
		}else{
			ra.seek(bp);
			final int maxRead = ra.read(b);
			bp += maxRead;
			return maxRead;
		}
	}
}

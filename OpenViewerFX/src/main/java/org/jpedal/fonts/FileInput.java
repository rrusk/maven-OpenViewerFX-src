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
 * FileInput.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileInput {

    private int pos;
    private final RandomAccessFile ra;
    private final int len;
    byte[] temp;
    int tSize = 8192;
    int ts, te;

    public FileInput(final File f) throws IOException {
        ra = new RandomAccessFile(f, "r");
        len = (int) f.length();
        tSize = Math.min(tSize, len);
        temp = new byte[tSize];
        te = tSize;
        ra.read(temp);
    }

    public int getU8() throws IOException {
        if (pos >= ts && pos < te) {
            final int v = temp[pos - ts] & 0xff;
            pos++;
            return v;
        } else {
            ts = pos;
            te = ts + tSize;
            ra.seek(pos);
            final int max = Math.min(len - pos, tSize);
            ra.read(temp, 0, max);
            pos++;
            return temp[0] & 0xff;
        }
    }

    public int getU16() throws IOException {
        return getU8() << 8 | getU8();
    }

    public int getU24() throws IOException {
        return (getU8() << 16) | (getU8() << 8) | getU8();
    }

    public int getU32() throws IOException {
        return (getU8() << 24) | (getU8() << 16) | (getU8() << 8) | getU8();
    }

    public int getOffset(int size) throws IOException {
        switch (size) {
            case 1:
                return getU8();
            case 2:
                return getU16();
            case 3:
                return getU24();
            default:
                return getU32();
        }
    }

    public long getU64() throws IOException {
        final long a = getU8();
        final long b = getU8();
        final long c = getU8();
        final long d = getU8();
        final long e = getU8();
        final long f = getU8();
        final long g = getU8();
        final long h = getU8();
        return a << 56 | b << 48 | c << 40 | d << 32 | e << 24 | f << 16 | g << 8 | h;
    }

    public void read(final byte[] copyTo) throws IOException {
        for (int i = 0, ii = Math.min(copyTo.length, len - pos); i < ii; i++) {
            copyTo[i] = (byte) getU8();
        }
    }

    public int getPosition() {
        return pos;
    }

    public void skip(final int n) {
        pos += n;
    }

    public void moveTo(final int p) {
        pos = p;
    }

    public int getLength() {
        return len;
    }

    public void close() throws IOException {
        ra.close();
    }
}

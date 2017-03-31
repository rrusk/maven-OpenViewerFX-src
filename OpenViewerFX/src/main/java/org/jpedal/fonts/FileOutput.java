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
 * FileOutput.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutput {

    private static final int MAXLEN = 8192;
    private static final int MINP = MAXLEN - 10;
    private int p;
    private int len;
    private final FileOutputStream out;
    private final byte[] buf = new byte[MAXLEN];

    public FileOutput(final File file) throws IOException {
        out = new FileOutputStream(file);
    }

    public void putU8(final int x) throws IOException {
        if (p > MINP) {
            flushNow();
        }
        buf[p++] = (byte) x;
    }

    public void putU16(final int x) throws IOException {
        if (p > MINP) {
            flushNow();
        }
        buf[p++] = (byte) ((x >>> 8) & 0xff);
        buf[p++] = (byte) (x & 0xff);
    }

    public void putU24(final int x) throws IOException {
        if (p > MINP) {
            flushNow();
        }
        buf[p++] = (byte) ((x >>> 16) & 0xff);
        buf[p++] = (byte) ((x >>> 8) & 0xff);
        buf[p++] = (byte) (x & 0xff);
    }

    public void putU32(final int x) throws IOException {
        if (p > MINP) {
            flushNow();
        }
        buf[p++] = (byte) ((x >>> 24) & 0xff);
        buf[p++] = (byte) ((x >>> 16) & 0xff);
        buf[p++] = (byte) ((x >>> 8) & 0xff);
        buf[p++] = (byte) (x & 0xff);
    }

    public void putU64(final long x) throws IOException {
        if (p > MINP) {
            flushNow();
        }
        buf[p++] = (byte) ((x >>> 56) & 0xff);
        buf[p++] = (byte) ((x >>> 48) & 0xff);
        buf[p++] = (byte) ((x >>> 40) & 0xff);
        buf[p++] = (byte) ((x >>> 32) & 0xff);
        buf[p++] = (byte) ((x >>> 24) & 0xff);
        buf[p++] = (byte) ((x >>> 16) & 0xff);
        buf[p++] = (byte) ((x >>> 8) & 0xff);
        buf[p++] = (byte) (x & 0xff);
    }

    public void write(final byte[] copyTo) throws IOException {
        if (copyTo.length + p > MINP) {
            flushNow();
            out.write(copyTo);
            len += copyTo.length;
        } else {
            System.arraycopy(copyTo, 0, buf, p, copyTo.length);
            p += copyTo.length;
        }
    }

    public int getLength() {
        return len + p;
    }

    private void flushNow() throws IOException {
        out.write(buf, 0, p);
        len += p;
        p = 0;
    }

    public void close() throws IOException {
        flushNow();
        out.close();
    }
}

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
 * TiffLZWBuffer.java
 * ---------------
 */
package org.jpedal.io.filter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class TiffLZWBuffer {
    
    private byte[][] codes;
    private BufferedOutputStream output;
    private BufferedInputStream input;
    private int bitsToGet = 9;
    private int tp;
    private int putBuffer, putBits;


    public void decompress(BufferedOutputStream output, BufferedInputStream input) throws IOException {

        init();

        this.input = input;
        this.output = output;

        putBuffer = 0;
        putBits = 0;

        int code, oldCode = 0;
        byte[] chars;

        while (((code = findNext()) != 257)) {

            if (code == 256) {

                init();
                code = findNext();

                if (code == 257) {
                    break;
                }

                addCodes(codes[code]);
                oldCode = code;

            } else {

                if (code < tp) {
                    chars = codes[code];
                    addCodes(chars);
                    addCodeToCodes(codes[oldCode], chars[0]);
                    oldCode = code;

                } else {

                    chars = codes[oldCode];
                    chars = generateCodeArray(chars, chars[0]);
                    addCodes(chars);
                    addCodeArrToCodes(chars);
                    oldCode = code;
                }
            }
        }
    }

    public void init() {
        codes = new byte[4096][];
        for (int i = 0; i < 256; i++) {
            codes[i] = new byte[1];
            codes[i][0] = (byte) i;
        }
        tp = 258;
        bitsToGet = 9;
    }

    private void addCodes(byte[] codes) throws IOException {
        output.write(codes);       
    }

    private void addCodeToCodes(byte[] oldCodes, byte code) {
        int length = oldCodes.length;
        byte string[] = new byte[length + 1];
        System.arraycopy(oldCodes, 0, string, 0, length);
        string[length] = code;
        codes[tp++] = string;
        if (tp == 511) {
            bitsToGet = 10;
        } else if (tp == 1023) {
            bitsToGet = 11;
        } else if (tp == 2047) {
            bitsToGet = 12;
        }
    }

    private void addCodeArrToCodes(byte[] codeArr) {
        codes[tp++] = codeArr;
        if (tp == 511) {
            bitsToGet = 10;
        } else if (tp == 1023) {
            bitsToGet = 11;
        } else if (tp == 2047) {
            bitsToGet = 12;
        }
    }

    private static byte[] generateCodeArray(byte oldString[], byte newString) {
        int length = oldString.length;
        byte string[] = new byte[length + 1];
        System.arraycopy(oldString, 0, string, 0, length);
        string[length] = newString;
        return string;
    }

    private int findNext() {
        int[] combinator = {511, 1023, 2047, 4095};
        try {
            putBuffer = (putBuffer << 8) | input.read();
            putBits += 8;
            if (putBits < bitsToGet) {
                putBuffer = (putBuffer << 8) | input.read();
                putBits += 8;
            }
            int code = (putBuffer >> (putBits - bitsToGet)) & combinator[bitsToGet - 9];
            putBits -= bitsToGet;
            return code;
        } catch (Exception e) {
            System.err.println("Exception in findNext "+e);
            return 257;
        }
    }
}

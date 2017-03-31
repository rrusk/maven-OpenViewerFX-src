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
 * Name.java
 * ---------------
 */
package org.jpedal.io.types;

import org.jpedal.exception.PdfSecurityException;

import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;

import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.io.security.DecryptionFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

/**
 *
 */
public class Name {

    public static int setNameTreeValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {

        boolean isRef = false;

        //move to start
        while (raw[i] != '[') { //can be number as well

            if (raw[i] == '(') { //allow for W (7)
                isRef = false;
                break;
            }

            //allow for number as in refer 9 0 R
            if (raw[i] >= '0' && raw[i] <= '9') {
                isRef = true;
                break;
            }

            i++;
        }

        //allow for direct or indirect
        byte[] data = raw;

        int start = i, j = i;

        int count = 0;

        //read ref data and slot in
        if (isRef) {
            //number
            final int[] values = StreamReaderUtils.readRefFromStream(raw, i);

            i = values[2];
            final int generation = values[1];
            final int number = values[0];

            if (raw[i] != 82) //we are expecting R to end ref
            {
                throw new RuntimeException("3. Unexpected value in file " + raw[i] + " - please send to IDRsolutions for analysis");
            }

            //read the Dictionary data
            data = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(number, generation), number, generation);

            //allow for data in Linear object not yet loaded
            if (data == null) {
                pdfObject.setFullyResolved(false);

                if (debugFastCode) {
                    System.out.println(padding + "Data not yet loaded");
                }

                return raw.length;
            }

            //lose obj at start
            j = 3;
            while (data[j - 1] != 106 && data[j - 2] != 98 && data[j - 3] != 111 && data[j - 3] != '<') {
                j++;
            }

            j = StreamReaderUtils.skipSpaces(data, j);

            //reset pointer
            start = j;

        }


        //move to end
        while (j < data.length) {

            if (data[j] == '[' || data[j] == '(') {
                count++;
            } else if (data[j] == ']' || data[j] == ')') {
                count--;
            }

            if (count == 0) {
                break;
            }

            j++;
        }

        final byte[] newString = getString(pdfObject, objectReader, data, start, j);

        pdfObject.setTextStreamValue(PDFkeyInt, newString);

        if (debugFastCode) {
            System.out.println(padding + "name=" + new String(newString) + " set in " + pdfObject);
        }

        //roll on
        if (!isRef) {
            i = j;
        }
        return i;
    }

    static byte[] getString(final PdfObject pdfObject, final PdfFileReader objectReader, final byte[] data, final int start, final int j) {
        final int stringLength = j - start + 1;
        byte[] newString = new byte[stringLength];

        System.arraycopy(data, start, newString, 0, stringLength);
        if (pdfObject.getObjectType() != PdfDictionary.Encrypt) {
            final DecryptionFactory decryption = objectReader.getDecryptionObject();
            if (decryption != null) {
                try {
                    newString = decryption.decrypt(newString, pdfObject.getObjectRefAsString(), false, null, false, false);
                } catch (final PdfSecurityException e) {
                    LogWriter.writeLog("Exception: " + e.getMessage());
                }
            }
        }
        return newString;
    }

    public static int setNameStringValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {

        final byte[] stringBytes;

        //move cursor to end of last command if needed
        while (raw[i] != 10 && raw[i] != 13 && raw[i] != 32 && raw[i] != 47 && raw[i] != '(' && raw[i] != '<') {
            i++;
        }

        i = StreamReaderUtils.skipSpaces(raw, i);

        //work out if direct (ie /String or read ref 27 0 R
        int j2 = i;
        byte[] arrayData = raw;

        boolean isIndirect = raw[i] != 47 && raw[i] != 40 && raw[i] != 60; //Some /NAME values start (

        final boolean startsWithBrace = raw[i] == 40;

        //delete
        //@speed - lose this code once Filters done properly
        /*
         * just check its not /Filter [/FlateDecode ] or [] or [ /ASCII85Decode /FlateDecode ]
         * by checking next valid char not /
         */
        boolean isInsideArray = false;
        if (isIndirect) {
            int aa = i + 1;
            aa = StreamReaderUtils.skipSpaces(raw, aa);

            if (raw[aa] == 47 || raw[aa] == ']') {
                isIndirect = false;
                i = aa + 1;
                isInsideArray = true;
            }
        }

        if (isIndirect) { //its in another object so we need to fetch

            final int[] values = StreamReaderUtils.readRefFromStream(raw, i);
            final int ref = values[0];
            final int generation = values[1];
            i = values[2];

            if (raw[i] != 82) { //we are expecting R to end ref
                throw new RuntimeException(padding + "2. Unexpected value in file - please send to IDRsolutions for analysis");
            }

            //read the Dictionary data
            arrayData = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);

            //allow for data in Linear object not yet loaded
            if (arrayData == null) {
                pdfObject.setFullyResolved(false);

                if (debugFastCode) {
                    System.out.println(padding + "Data not yet loaded");
                }

                return raw.length;
            }

            //lose obj at start and roll onto /
            if (arrayData[0] == 47) {
                j2 = 0;
            } else {
                j2 = 3;

                while (arrayData[j2] != 47) {
                    j2++;
                }
            }
        }

        //lose /
        j2++;

        //allow for no value with /Intent//Filter
        if (arrayData[j2] == 47) {
            return j2 - 1;
        }

        int end = j2 + 1;


        if (isInsideArray) { //values inside []

            //move cursor to start of text
            j2 = StreamReaderUtils.skipSpacesOrOtherCharacter(arrayData, j2, 47);

            int slashes = 0;

            //count chars
            byte lastChar = 0;
            while (true) {

                if (arrayData[end] == ']') {
                    break;
                }

                if (arrayData[end] == 47 && (lastChar == 32 || lastChar == 10 || lastChar == 13))//count the / if gap before
                {
                    slashes++;
                }

                lastChar = arrayData[end];
                end++;

                if (end == arrayData.length) {
                    break;
                }
            }

            //set value and ensure space gap
            final int charCount = end - slashes;
            int ptr = 0;
            stringBytes = new byte[charCount - j2];

            byte nextChar, previous = 0;
            for (int ii = j2; ii < charCount; ii++) {
                nextChar = arrayData[ii];
                if (nextChar == 47) {
                    if (previous != 32 && previous != 10 && previous != 13) {
                        stringBytes[ptr] = 32;
                        ptr++;
                    }
                } else {
                    stringBytes[ptr] = nextChar;
                    ptr++;
                }

                previous = nextChar;
            }
        } else { //its in data stream directly or (string)

            //count chars
            while (true) {

                if (startsWithBrace) {
                    if (arrayData[end] == ')' && !ObjectUtils.isEscaped(arrayData, end)) {
                        break;
                    }
                } else if (arrayData[end] == 32 || arrayData[end] == 10 || arrayData[end] == 13 || arrayData[end] == 47 || arrayData[end] == 62) {
                    break;
                }

                end++;

                if (end == arrayData.length) {
                    break;
                }
            }

            //set value
            final int charCount = end - j2;
            stringBytes = new byte[charCount];
            System.arraycopy(arrayData, j2, stringBytes, 0, charCount);

        }

        pdfObject.setName(PDFkeyInt, stringBytes);

        if (debugFastCode) {
            System.out.println(padding + "String set as =" + new String(stringBytes) + "< written to " + pdfObject);
        }

        //put cursor in correct place (already there if ref)
        if (!isIndirect) {
            i = end - 1;
        }

        return i;
    }

}



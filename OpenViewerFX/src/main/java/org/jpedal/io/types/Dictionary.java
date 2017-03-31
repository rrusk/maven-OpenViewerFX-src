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
 * Dictionary.java
 * ---------------
 */
package org.jpedal.io.types;

import java.util.ArrayList;

import org.jpedal.io.ObjectDecoder;

import static org.jpedal.io.ObjectDecoder.debugFastCode;
import static org.jpedal.io.ObjectDecoder.padding;
import static org.jpedal.io.ObjectDecoder.resolveFully;

import org.jpedal.io.ObjectUtils;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.ObjectFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.NumberUtils;

/**
 *
 */
public class Dictionary {

    public static int readDictionary(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {

        //if we only need top level do not read whole tree
        final boolean ignoreRecursion = pdfObject.ignoreRecursion();

        //roll on
        if (raw[i] != '<') {
            i++;
        }

        i = StreamReaderUtils.skipSpaces(raw, i);

        //some objects can have a common value (ie /ToUnicode /Identity-H
        if (raw[i] == 47) {

            i = readKey(pdfObject, i + 1, raw, PDFkeyInt);

        } else if (StreamReaderUtils.isEndObj(raw, i)) { //allow for empty object

            if (debugFastCode) {
                System.out.println(padding + "Empty object" + new String(raw) + "<<");
            }

        } else if (!ignoreRecursion) {
            i = Dictionary.readDictionaryFromRefOrDirect(pdfObject, pdfObject.getObjectRefAsString(), i, raw, PDFkeyInt, objectReader);
        } else { //we need to ref from ref elsewhere which may be indirect [ref], hence loop

            i = readRef(pdfObject, i, raw, PDFkeyInt, objectReader);
        }
        return i;
    }

    static int readRef(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {

        if (debugFastCode) {
            System.out.println(padding + "1.About to read ref orDirect i=" + i + " char=" + (char) raw[i]);
        }

        i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 91);

        i = Dictionary.readDictionaryFromRefOrDirect(pdfObject, pdfObject.getObjectRefAsString(), i, raw, PDFkeyInt, objectReader);


        return i;
    }

    static int readKey(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt) {

        final int keyStart = i;

        i = StreamReaderUtils.skipToEndOfRef(raw, i);

        final PdfObject valueObj = ObjectFactory.createObject(PDFkeyInt, pdfObject.getObjectRefAsString(), pdfObject.getObjectType(), pdfObject.getID());
        valueObj.setID(PDFkeyInt);

        //store value
        valueObj.setConstant(PDFkeyInt, keyStart, i - keyStart, raw);

        valueObj.setGeneralStringValue(new String(getByteKeyFromStream(i - keyStart, raw, keyStart)));

        if (debugFastCode) {
            System.out.println(padding + "Set Dictionary as String=" + valueObj.getGeneralStringValue() + "  in " + pdfObject + " to " + valueObj);
        }

        //store value
        pdfObject.setDictionary(PDFkeyInt, valueObj);

        return i;
    }

    public static int setDictionaryValue(final PdfObject pdfObject, int i, final byte[] raw, final PdfFileReader objectReader, final int PDFkeyInt, final boolean dictionaryOnly) {

        //if we only need top level do not read whole tree
        final boolean ignoreRecursion = pdfObject.ignoreRecursion();

        if (debugFastCode) {
            System.out.println(padding + ">>>Reading Dictionary Pairs i=" + i + ' ' + (char) raw[i] + (char) raw[i + 1] + (char) raw[i + 2] + (char) raw[i + 3] + (char) raw[i + 4] + (char) raw[i + 5] + (char) raw[i + 6]);
        }

        i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 47);

        //set data which will be switched below if ref
        byte[] data = raw;
        int j = i;

        //get next key to see if indirect
        final boolean isRef = data[j] != '<';

        if (isRef) {

            //number
            final int[] values = StreamReaderUtils.readRefFromStream(raw, i);
            final int number = values[0];
            final int generation = values[1];
            i = values[2];

            if (!ignoreRecursion) {

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

                if (data[0] == '<' && data[1] == '<') {
                    j = 0;
                } else {
                    //lose obj at start
                    j = 3;

                    while (data[j - 1] != 106 && data[j - 2] != 98 && data[j - 3] != 111) {

                        if (data[j] == '/') {  //trap for odd case
                            j = 0;
                            break;
                        }

                        j++;

                        if (j == data.length) { //some missing obj so catch these
                            j = 0;
                            break;
                        }
                    }

                    j = StreamReaderUtils.skipSpaces(data, j);

                    if (data[j] == '%') {
                        j = StreamReaderUtils.skipComment(data, j);
                    }
                }

            }
        }

        //allow for empty object (ie /Pattern <<>> )
        final int endJ = StreamReaderUtils.skipSpacesOrOtherCharacter(data, j, '<');

        if (data[endJ] == '>') { //empty object
            j = endJ + 1;
        } else {

            final PdfObject valueObj = ObjectFactory.createObject(PDFkeyInt, pdfObject.getObjectRefAsString(), pdfObject.getObjectType(), pdfObject.getID());
            valueObj.setID(PDFkeyInt);

            //read pairs (stream in data starting at j)
            if (dictionaryOnly) {
                j = readKeyPairs(data, j, valueObj);
            } else {
                j = readMixedKeyPairs(data, j, valueObj);
            }
            //store value
            pdfObject.setDictionary(PDFkeyInt, valueObj);

            if (debugFastCode) {
                System.out.println(padding + "Set Dictionary pairs type in " + pdfObject + " to " + valueObj);
            }

        }

        //update pointer if direct so at end (if ref already in right place)
        if (!isRef) {
            i = j;

            if (debugFastCode) {
                System.out.println(i + ">>>>" + data[i - 2] + ' ' + data[i - 1] + " >" + data[i] + "< " + data[i + 1] + ' ' + data[i + 2]);
            }
        }
        return i;
    }

    /**
     * sets pairs and returns point reached in stream
     */
    private static int readMixedKeyPairs(final byte[] data, int start, final PdfObject pdfObject) {

        final ArrayList<byte[]> keys = new ArrayList<byte[]>(100);
        final ArrayList<byte[]> values = new ArrayList<byte[]>(100);

        while (true) {

            //move cursor to start of text
            start = StreamReaderUtils.skipSpacesOrOtherCharacter(data, start, 60);
            start = StreamReaderUtils.skipSpacesOrOtherCharacter(data, start, ')');

            if (data[start] == 37) { //allow for comment
                start = StreamReaderUtils.skipComment(data, start);
            }

            if (data[start] == 62 || StreamReaderUtils.isEndObj(data, start)) { //exit at end
                break;
            }

            //read key (starts with /)
            final int tokenStart = start + 1;
            start = StreamReaderUtils.skipToEndOfKey(data, tokenStart);
            keys.add(getByteKeyFromStream(start - tokenStart, data, tokenStart));

            //read value
            start = StreamReaderUtils.skipSpaces(data, start);

            int refStart = start;

            if (StreamReaderUtils.isNull(data, start)) {
                start += 4;
                values.add(null);
            } else {

                if (data[start] == 60 || data[start] == '[' || data[start] == '/' || data[start] == '(') {

                    refStart = start;

                    if (data[start] == '<') {
                        start = ObjectUtils.skipToEndOfObject(start, data);
                    } else if (data[start] == '[') {
                        start = StreamReaderUtils.skipToEndOfArray(data, start);
                    } else if (data[start] == '/') {
                        start = StreamReaderUtils.skipToEndOfKey(data, start + 1);
                    } else if (data[start] == '(') {
                        refStart++;
                        start = StreamReaderUtils.skipToEndOfStream(data, start);
                    }
                } else { //its 50 0 R
                    while (data[start] != 'R' && (data[start] != '/' || data[start - 1] == '\\' && data[start - 2] == '\\') && (data[start] != '>' && data[start - 1] != '>')) {
                        start++;
                    }
                    if (data[start] == 'R') {
                        start++; //roll past R
                    }
                }

                values.add(getByteKeyFromStream(start - refStart, data, refStart));
            }
        }

        final int size = keys.size();
        final byte[][] returnKeys = new byte[size][];
        final byte[][] returnValues = new byte[size][];

        for (int a = 0; a < size; a++) {
            returnKeys[a] = keys.get(a);
            returnValues[a] = values.get(a);
        }

        pdfObject.setDictionaryPairs(returnKeys, returnValues);

        return start;

    }

    /**
     * sets pairs and returns point reached in stream
     */
    private static int readKeyPairs(final byte[] data, int start, final PdfObject pdfObject) {

        final ArrayList<byte[]> keys = new ArrayList<byte[]>(100);
        final ArrayList<byte[]> values = new ArrayList<byte[]>(100);

        while (true) {

            //move cursor to start of text
            start = StreamReaderUtils.skipSpacesOrOtherCharacter(data, start, 60);

            if (data[start] == 37) { //allow for comment
                start = StreamReaderUtils.skipComment(data, start);
            }

            if (data[start] == 62 || StreamReaderUtils.isEndObj(data, start)) { //exit at end
                break;
            }

            //read key (starts with /)           
            final int tokenStart = start + 1;
            start = StreamReaderUtils.skipToEndOfKey(data, tokenStart);
            keys.add(getByteKeyFromStream(start - tokenStart, data, tokenStart));

            //read value
            start = StreamReaderUtils.skipSpaces(data, start);

            int refStart = start;

            if (StreamReaderUtils.isNull(data, start)) {
                start += 4;
                values.add(null);
            } else {

                if (data[start] == 60 || data[start] == '[' || data[start] == '/') {

                    refStart = start;

                    if (data[start] == '<') {
                        start = ObjectUtils.skipToEndOfObject(start, data);
                    } else if (data[start] == '[') {
                        start = StreamReaderUtils.skipToEndOfArray(data, start);
                    } else if (data[start] == '/') {
                        start = StreamReaderUtils.skipToEndOfKey(data, start + 1);
                    }
                } else { //its 50 0 R
                    while (data[start] != 'R') {
                        start++;
                    }

                    start++; //roll past R
                }

                values.add(getByteKeyFromStream(start - refStart, data, refStart));
            }
        }

        final int size = keys.size();
        final byte[][] returnKeys = new byte[size][];
        final byte[][] returnValues = new byte[size][];

        for (int a = 0; a < size; a++) {
            returnKeys[a] = keys.get(a);
            returnValues[a] = values.get(a);
        }

        pdfObject.setDictionaryPairs(returnKeys, returnValues);

        return start;

    }

    private static byte[] getByteKeyFromStream(final int tokenLength, final byte[] data, final int tokenStart) {
        final byte[] tokenKey = new byte[tokenLength];
        System.arraycopy(data, tokenStart, tokenKey, 0, tokenLength);
        return tokenKey;
    }


    public static int setUnreadDictionaryValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final boolean isInlineImage) {

        if (raw[i] != '<')  //roll on
        {
            i++;
        }

        i = StreamReaderUtils.skipSpaces(raw, i);

        final int start = i;
        final int keyStart;
        int keyLength;

        //create and store stub
        final PdfObject valueObj = ObjectFactory.createObject(PDFkeyInt, pdfObject.getObjectRefAsString(), pdfObject.getObjectType(), pdfObject.getID());
        valueObj.setID(PDFkeyInt);

        if (!StreamReaderUtils.isNull(raw, i)) { //allow for null
            pdfObject.setDictionary(PDFkeyInt, valueObj);
        }

        int status = PdfObject.UNDECODED_DIRECT; //assume not object and reset below if wrong

        //some objects can have a common value (ie /ToUnicode /Identity-H
        if (raw[i] == 47) { //not worth caching

            //move cursor to start of text
            while (raw[i] == 10 || raw[i] == 13 || raw[i] == 32 || raw[i] == 47 || raw[i] == 60) {
                i++;
            }

            keyStart = i;
            keyLength = 0;

            //move cursor to end of text
            while (raw[i] != 10 && raw[i] != 13 && raw[i] != 32 && raw[i] != 47 && raw[i] != 60 && raw[i] != 62) {
                i++;
                keyLength++;
            }

            i--; // move back so loop works

            //store value
            final int constant = valueObj.setConstant(PDFkeyInt, keyStart, keyLength, raw);

            if (constant == PdfDictionary.Unknown || isInlineImage) {

                final byte[] newStr = new byte[keyLength];
                System.arraycopy(raw, keyStart, newStr, 0, keyLength);

                final String s = new String(newStr);
                valueObj.setGeneralStringValue(s);

            }

            status = PdfObject.DECODED;

        } else //allow for empty object
            if (raw[i] == 'e' && raw[i + 1] == 'n' && raw[i + 2] == 'd' && raw[i + 3] == 'o' && raw[i + 4] == 'b') {
            } else { //we need to ref from ref elsewhere which may be indirect [ref], hence loop

                i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 91);

                //roll on and ignore
                if (raw[i] == '<' && raw[i + 1] == '<') {

                    i += 2;
                    int reflevel = 1;

                    while (reflevel > 0) {
                        if (raw[i] == '<' && raw[i + 1] == '<') {
                            i += 2;
                            reflevel++;
                        } else if (raw[i] == '(') { //allow for << (>>) >>

                            i++;
                            while (raw[i] != ')' || ObjectUtils.isEscaped(raw, i)) {
                                i++;
                            }

                        } else if (raw[i] == '>' && i + 1 == raw.length) {
                            reflevel = 0;
                        } else if (raw[i] == '>' && raw[i + 1] == '>') {
                            i += 2;
                            reflevel--;
                        } else {
                            i++;
                        }
                    }
                } else if (raw[i] == '[') {

                    i++;
                    int reflevel = 1;

                    while (reflevel > 0) {

                        if (raw[i] == '(') { //allow for [[ in stream ie [/Indexed /DeviceRGB 255 (abc[[z

                            i++;
                            while (raw[i] != ')' || ObjectUtils.isEscaped(raw, i)) {
                                i++;
                            }

                        } else if (raw[i] == '[') {
                            reflevel++;
                        } else if (raw[i] == ']') {
                            reflevel--;
                        }

                        i++;
                    }
                    i--;
                } else if (StreamReaderUtils.isNull(raw, i)) { //allow for null
                    i += 4;
                } else { //must be a ref

                    //assume not object and reset below if wrong
                    status = PdfObject.UNDECODED_REF;

                    while (raw[i] != 'R' || raw[i - 1] == 'e') { //second condition to stop spurious match on DeviceRGB
                        i++;

                        if (i == raw.length) {
                            break;
                        }
                    }
                    i++;

                    if (i >= raw.length) {
                        i = raw.length - 1;
                    }
                }
            }

        valueObj.setStatus(status);
        if (status != PdfObject.DECODED) {

            final int StrLength = i - start;
            final byte[] unresolvedData = new byte[StrLength];
            System.arraycopy(raw, start, unresolvedData, 0, StrLength);

            //check for returns in data if ends with R and correct to space
            if (unresolvedData[StrLength - 1] == 82) {

                for (int jj = 0; jj < StrLength; jj++) {

                    if (unresolvedData[jj] == 10 || unresolvedData[jj] == 13) {
                        unresolvedData[jj] = 32;
                    }

                }
            }
            valueObj.setUnresolvedData(unresolvedData, PDFkeyInt);

        }

        if (raw[i] == '/' || raw[i] == '>') //move back so loop works
        {
            i--;
        }
        return i;
    }


    /**
     * @param pdfObject
     * @param objectRef
     * @param i
     * @param raw
     * @param PDFkeyInt - -1 will store in pdfObject directly, not as separate object
     * @return
     */
    public static int readDictionaryFromRefOrDirect(final PdfObject pdfObject, final String objectRef, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {

        readDictionaryFromRefOrDirect:
        while (true) {

            i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 91);

            if (raw[i] == '%') {
                i = StreamReaderUtils.skipComment(raw, i);
                i = StreamReaderUtils.skipSpacesOrOtherCharacter(raw, i, 91);
            }

            if (raw[i] == 60) { //[<<data inside brackets>>]

                i = handlePairs(pdfObject, objectRef, i, raw, PDFkeyInt);

                if (i < 0) {
                    i = -i;
                } else {
                    i = DirectDictionaryToObject.convert(pdfObject, objectRef, i, raw, PDFkeyInt, objectReader);
                }

            } else if (raw[i] == 47) { //direct value such as /DeviceGray

                i = ObjectUtils.setDirectValue(pdfObject, i, raw, PDFkeyInt);

            } else { // ref or [ref]

                int j = i, ref, generation;
                byte[] data = raw;

                while (true) {

                    //allow for [ref] at top level (may be followed by gap
                    j = StreamReaderUtils.skipSpacesOrOtherCharacter(data, j, 91);

                    //trap empty arrays ie [ ]
                    //ie 13jun/Factuur 2106010.PDF
                    if (data[j] == ']') {
                        return j;
                    }

                    // trap nulls  as well
                    boolean hasNull = false;
                    int keyStart;
                    int[] values;

                    while (true) {

                        //trap null arrays ie [null null]
                        if (hasNull && data[j] == ']') {
                            return j;
                        }
                        values = StreamReaderUtils.readRefFromStream(data, j);
                        ref = values[0];

                        keyStart = j;
                        j = StreamReaderUtils.skipToEndOfRef(data, j);
                        j = StreamReaderUtils.skipSpaces(data, j);

                        //handle nulls
                        if (ref != 69560 || data[keyStart] != 'n') {
                            break; //not null
                        } else {
                            hasNull = true;
                            if (data[j] == '<') { // /DecodeParms [ null << /K -1 /Columns 1778 >>  ] ignore null and jump down to enclosed Dictionary
                                i = j;
                                continue readDictionaryFromRefOrDirect;

                            }
                        }
                    }

                    generation = values[1];
                    j = values[2];

                    data = objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);

                    //allow for data in Linear object not yet loaded
                    if (data == null) {
                        pdfObject.setFullyResolved(false);

                        return raw.length;
                    }

                    //disregard corrputed data from start of file
                    if (data != null && data.length > 4 && data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
                        data = null;
                    } else if (StreamReaderUtils.isNull(data, 0)) {
                        data = null;
                    }

                    if (data == null) {
                        break;
                    }
                    
                    /*
                     * get not indirect and exit if not
                     */
                    int j2 = 0;

                    //allow for [91 0 r]
                    if (data[j2] != '[' && data[0] != '<' && data[1] != '<') {

                        while (j2 < 3 || (j2 > 2 && data[j2 - 1] != 106 && data[j2 - 2] != 98 && data[j2 - 3] != 111)) {

                            //allow for /None as value
                            if (data[j2] == '/') {
                                break;
                            }
                            j2++;
                        }

                        j2 = StreamReaderUtils.skipSpaces(data, j2);
                    }

                    //if indirect, round we go again
                    if (data[j2] != 91) {
                        j = 0;
                        break;
                    } else if (data[j2] == '[' && data[j2 + 1] == '<') {
                        j2++;
                        j = j2;
                        break;
                    }

                    j = j2;
                }

                //allow for no data found (ie /PDFdata/baseline_screens/debug/hp_broken_file.pdf)
                if (data != null) {
                    i = handlePairs(pdfObject, objectRef, i, raw, PDFkeyInt);

                    if (i < 0) {
                        i = -i;
                    } else {
                        i = readObj(j, data, raw, ref, generation, i, pdfObject, PDFkeyInt, objectReader);
                    }
                }
            }

            return i;
        }
    }

    static int handlePairs(final PdfObject pdfObject, final String objectRef, int i, final byte[] raw, final int PDFkeyInt) {

        boolean isPairs = false;

        //@zain @bethan - you will need to enable here
        //do this third

        //we need to avoid this for AA as D can occur in there as a Dictionary
        final int parentType = pdfObject.getPDFkeyInt();

        if ((parentType != PdfDictionary.AA) &&
                (PDFkeyInt == PdfDictionary.N || PDFkeyInt == PdfDictionary.R || PDFkeyInt == PdfDictionary.D || PDFkeyInt == PdfDictionary.Dests)) {
            isPairs = isDictionaryPairs(i, raw);
        }

        if (isPairs) {
            final FormObject APobj = new FormObject(objectRef);
            pdfObject.setDictionary(PDFkeyInt, APobj);

            i = -readKeyPairs(raw, i, APobj);

        }
        return i;
    }

    private static int readObj(int j, final byte[] data, final byte[] raw, int ref, int generation, int i, final PdfObject pdfObject, final int PDFkeyInt, final PdfFileReader objectReader) {

        /*
        * get id from stream
        */
        j = StreamReaderUtils.skipSpaces(data, j);

        //check not <</Last
        final boolean isMissingValue = j < raw.length && raw[j] == '<' &&
                raw[StreamReaderUtils.skipSpacesOrOtherCharacter(raw, j, '<')] != '/';

        if (isMissingValue) { //missing value at start for some reason

            int keyStart = j;

            j = StreamReaderUtils.skipToEndOfRef(data, j);

            ref = NumberUtils.parseInt(keyStart, j, data);

            j = StreamReaderUtils.skipSpaces(data, j);

            keyStart = j;
            j = StreamReaderUtils.skipToEndOfRef(data, j);

            generation = NumberUtils.parseInt(keyStart, j, data);

            //lose obj at start
            while (data[j - 1] != 106 && data[j - 2] != 98 && data[j - 3] != 111) {

                if (data[j] == '<') {
                    break;
                }

                j++;
            }
        }
        j = StreamReaderUtils.skipSpaces(data, j);
        //move to start of Dict values
        while (data[j] != 60 && data[j + 1] != 60 && data[j] != 47) {
            j++;
        }
        i = handleValue(pdfObject, i, PDFkeyInt, j, ref, generation, data, objectReader);
        return i;
    }


    public static int setDictionaryValue(final PdfObject pdfObject, int i, final byte[] raw, final int PDFkeyInt, final PdfFileReader objectReader) {

        /*
         * workout actual end as not always returned right
         */
        int end = i;
        int nextC = i;
        nextC = StreamReaderUtils.skipSpaces(raw, nextC);

        //allow for null object
        if (StreamReaderUtils.isNull(raw, nextC)) {
            i = nextC + 4;
            return i;
        } else if (raw[nextC] == '[' && raw[nextC + 1] == ']') { //allow for empty object []
            i = nextC;
            return i;
        }

        if (raw[i] != '<' && raw[i + 1] != '<') {
            end += 2;
        }

        boolean inDictionary = true;
        final boolean isKey = raw[end - 1] == '/';

        final int strLen = raw.length;

        while (inDictionary && end < strLen) {

            if (raw[end] == '<' && raw[end + 1] == '<') {
                int level2 = 1;
                end++;
                while (level2 > 0) {

                    if (raw[end] == '<' && raw[end + 1] == '<') {
                        level2++;
                        end += 2;
                    } else if (raw[end - 1] == '>' && raw[end] == '>') {
                        level2--;
                        if (level2 > 0) {
                            end += 2;
                        }
                    } else if (raw[end] == '(') { //scan (strings) as can contain >>

                        end++;
                        while (raw[end] != ')' || ObjectUtils.isEscaped(raw, end)) {
                            end++;
                        }
                    } else {
                        end++;
                    }
                }

                inDictionary = false;

            } else if (raw[end] == 'R') {
                inDictionary = false;
            } else if (isKey && (raw[end] == ' ' || raw[end] == 13 || raw[end] == 10 || raw[end] == 9)) {
                inDictionary = false;
            } else if (raw[end] == '/') {
                inDictionary = false;
                end--;
            } else if (raw[end] == '>' && raw[end + 1] == '>') {
                inDictionary = false;
                end--;
            } else {
                end++;
            }
        }

        //boolean save=debugFastCode;
        Dictionary.readDictionary(pdfObject, i, raw, PDFkeyInt, objectReader);

        //use correct value
        return end;
    }


    static int handleValue(final PdfObject pdfObject, int i, final int PDFkeyInt, int j, final int ref, final int generation, final byte[] data, final PdfFileReader objectReader) {

        final int keyStart;
        int keyLength;
        final int dataLen = data.length;

        if (data[j] == 47) {
            j++; //roll on past /

            keyStart = j;
            keyLength = 0;

            //move cursor to end of text
            while (j < dataLen && data[j] != 10 && data[j] != 13 && data[j] != 32 && data[j] != 47 && data[j] != 60 && data[j] != 62) {
                j++;
                keyLength++;

            }

            i--; // move back so loop works

            if (PDFkeyInt == -1) {
                //store value directly
                pdfObject.setConstant(PDFkeyInt, keyStart, keyLength, data);

                if (debugFastCode) {
                    System.out.println(padding + "Set object Constant directly to " + pdfObject.setConstant(PDFkeyInt, keyStart, keyLength, data));
                }
            } else {
                //convert data to new Dictionary object
                final PdfObject valueObj = ObjectFactory.createObject(PDFkeyInt, null, pdfObject.getObjectType(), pdfObject.getID());
                valueObj.setID(PDFkeyInt);
                //store value
                valueObj.setConstant(PDFkeyInt, keyStart, keyLength, data);
                pdfObject.setDictionary(PDFkeyInt, valueObj);

                if (pdfObject.isDataExternal()) {
                    valueObj.isDataExternal(true);
                    if (!resolveFully(valueObj, objectReader)) {
                        pdfObject.setFullyResolved(false);
                    }
                }
            }
        } else {

            //convert data to new Dictionary object
            final PdfObject valueObj;
            if (PDFkeyInt == -1) {
                valueObj = pdfObject;
            } else {
                valueObj = ObjectFactory.createObject(PDFkeyInt, ref, generation, pdfObject.getObjectType());
                valueObj.setID(PDFkeyInt);
                valueObj.setInCompressedStream(pdfObject.isInCompressedStream());

                if (pdfObject.isDataExternal()) {
                    valueObj.isDataExternal(true);

                    if (!resolveFully(valueObj, objectReader)) {
                        pdfObject.setFullyResolved(false);
                    }
                }

                if (PDFkeyInt != PdfDictionary.Resources) {
                    valueObj.ignoreRecursion(pdfObject.ignoreRecursion());
                }
            }

            final ObjectDecoder objDecoder = new ObjectDecoder(objectReader);
            objDecoder.readDictionaryAsObject(valueObj, j, data);

            //store value
            if (PDFkeyInt != -1) {
                pdfObject.setDictionary(PDFkeyInt, valueObj);
            }
        }

        return i;
    }

    /**
     * work out if single dictionary object or pairs of values by scanning
     * and looking for a /Type which will be missing in pairs
     *
     * @param i
     * @param raw
     * @return
     */
    private static boolean isDictionaryPairs(final int i, final byte[] raw) {

        final int length = raw.length;
        int level = -1;

        //assume true and disprove
        boolean isPair = true;

        for (int j = i; j < length - 1; j++) {

            //skip nested content which might contain spurious match
            if (raw[j] == '<' && raw[j + 1] == '<') {
                level++;
            } else if (raw[j] == '>' && raw[j + 1] == '>') {
                level--;

                if (level < 0) {
                    break;
                }
            } else if (level == 0 && (raw[j] == '[' || (raw[j] == '/' && raw[j + 1] == 'T' && raw[j + 2] == 'y' && raw[j + 3] == 'p')
                    || (raw[j] == '/' && raw[j + 1] == 'R' && raw[j + 2] == 'e' && raw[j + 3] == 's' && raw[j + 4] == 'o' && raw[j + 5] == 'u')
                    || (raw[j] == 's' && raw[j + 1] == 't' && raw[j + 2] == 'r' && raw[j + 3] == 'e' && raw[j + 4] == 'a' && raw[j + 5] == 'm') || (raw[j] == '(' && raw[j + 1] == ')'))) {
                j = length;
                isPair = false;
            }
        }

        return isPair;
    }
}


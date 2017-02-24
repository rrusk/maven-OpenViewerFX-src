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
 * CompressedObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class CompressedObject extends PdfObject {

    private int[] Index, W;

    byte[][] ID;

    int First, Prev = -1, XRefStm = -1;

    private PdfObject Encrypt, Extends, Info, Root;

    int Size;

    public CompressedObject(final String ref) {
        super(ref);
    }

    public CompressedObject(final int ref, final int gen) {
        super(ref, gen);
    }


    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.Encrypt:
                return Encrypt;

            case PdfDictionary.Extends:
                return Extends;

            case PdfDictionary.Info:
                return Info;

            case PdfDictionary.Root:
                return Root;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.First:
                First = value;
                break;

            case PdfDictionary.Prev:

                //some PDFs can get multiple values and second one wrong
                if (Prev == -1) {
                    Prev = value;
                }
                break;

            case PdfDictionary.Size:
                Size = value;
                break;

            case PdfDictionary.XRefStm:
                XRefStm = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.First:
                return First;

            case PdfDictionary.Prev:
                return Prev;

            case PdfDictionary.Size:
                return Size;

            case PdfDictionary.XRefStm:
                return XRefStm;

            default:
                return super.getInt(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.Encrypt:
                Encrypt = value;
                break;

            case PdfDictionary.Extends:
                Extends = value;
                break;

            case PdfDictionary.Info:
                Info = value;
                break;

            case PdfDictionary.Root:
                Root = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }


    @Override
    public int setConstant(final int pdfKeyType, final int keyStart, final int keyLength, final byte[] raw) {

        final int id = PdfObject.getId(keyStart, keyLength, raw);

        final int PDFvalue = super.setConstant(pdfKeyType, id);

        switch (pdfKeyType) {

            case PdfDictionary.SMask:
                generalType = PDFvalue;
                break;

            case PdfDictionary.TR:
                generalType = PDFvalue;
                break;

        }

        return PDFvalue;
    }


    @Override
    public int[] getIntArray(final int id) {

        switch (id) {

            case PdfDictionary.Index:
                return deepCopy(Index);

            case PdfDictionary.W:
                return deepCopy(W);

            default:
                return super.getIntArray(id);
        }
    }

    @Override
    public void setIntArray(final int id, final int[] value) {

        switch (id) {

            case PdfDictionary.Index:
                Index = value;
                break;

            case PdfDictionary.W:
                W = value;
                break;

            default:
                super.setIntArray(id, value);
        }
    }


    @Override
    public byte[][] getStringArray(final int id) {

        switch (id) {

            case PdfDictionary.ID:
                return deepCopy(ID);

            default:
                return super.getStringArray(id);
        }
    }

    @Override
    public void setStringArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.ID:
                ID = value;
                break;

            default:
                super.setStringArray(id, value);
        }

    }

    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }


    @Override
    public int getObjectType() {
        return PdfDictionary.CompressedObject;
    }
}
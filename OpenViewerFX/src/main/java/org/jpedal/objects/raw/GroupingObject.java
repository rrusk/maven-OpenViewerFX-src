/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2016 IDRsolutions and Contributors.
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
 * GroupingObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.color.ColorSpaces;

public class GroupingObject extends PdfObject {

    private String Name;

    private byte[] rawName;

    private boolean K, I;

    private PdfObject colorSpace;

    public GroupingObject(final String ref) {
        super(ref);
    }

    public GroupingObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public boolean getBoolean(final int id) {

        switch (id) {

            case PdfDictionary.I:
                return I;

            case PdfDictionary.K:
                return K;

            default:
                return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value) {

        switch (id) {

            case PdfDictionary.I:
                I = value;
                break;

            case PdfDictionary.K:
                K = value;
                break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.ColorSpace:
                return colorSpace;

            default:
                return super.getDictionary(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.ColorSpace:
                colorSpace = value;

                break;

            default:
                super.setDictionary(id, value);
        }
    }


    @Override
    public int setConstant(final int pdfKeyType, final int keyStart, final int keyLength, final byte[] raw) {

        int PDFvalue;

        final int id = PdfObject.getId(keyStart, keyLength, raw);

        switch (id) {

            case PdfDictionary.G:
                PDFvalue = ColorSpaces.DeviceGray;
                break;


            case PdfDictionary.RGB:
                PDFvalue = ColorSpaces.DeviceRGB;
                break;

            default:
                PDFvalue = super.setConstant(pdfKeyType, id);
                break;
        }

        return PDFvalue;
    }


    @Override
    public void setName(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.Name:
                rawName = value;
                break;

            default:
                super.setName(id, value);

        }
    }

    @Override
    public byte[] getStringValueAsByte(final int id) {

        switch (id) {

            case PdfDictionary.Name:
                return rawName;

            default:
                return super.getStringValueAsByte(id);

        }
    }

    @Override
    public String getName(final int id) {

        switch (id) {

            case PdfDictionary.Name:

                //setup first time
                if (Name == null && rawName != null) {
                    Name = new String(rawName);
                }

                return Name;

            default:
                return super.getName(id);

        }
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Group;
    }


    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }
}
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
 * ColorSpaceObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.color.ColorSpaces;

public class ColorSpaceObject extends PdfObject {

    String Name;

    byte[] rawName;

    PdfObject Process;

    int Alternate = PdfDictionary.Unknown;

    private byte[][] rawComponents;

    private float[] BlackPoint, Gamma, WhitePoint;

    float N = -1;

    public ColorSpaceObject(final String ref) {
        super(ref);
    }

    public ColorSpaceObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public byte[][] getStringArray(final int id) {

        switch (id) {

            case PdfDictionary.Components:
                return deepCopy(rawComponents);

            default:
                return super.getStringArray(id);
        }
    }

    @Override
    public void setStringArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Components:
                rawComponents = value;
                break;

            default:
                super.setStringArray(id, value);
        }

    }


    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.Process:
                return Process;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.Process:
                Process = value;
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

        switch (pdfKeyType) {

            case PdfDictionary.Alternate:
                Alternate = PDFvalue;
                break;
        }

        return PDFvalue;
    }

    @Override
    public int getParameterConstant(final int key) {

        switch (key) {

            case PdfDictionary.Alternate:
                return Alternate;

            default:
                return super.getParameterConstant(key);
        }
    }

    @Override
    public float[] getFloatArray(final int id) {

        switch (id) {

            case PdfDictionary.BlackPoint:
                return deepCopy(BlackPoint);

            case PdfDictionary.Gamma:
                return deepCopy(Gamma);

            case PdfDictionary.WhitePoint:
                return deepCopy(WhitePoint);

            default:
                return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatNumber(final int id, final float value) {

        switch (id) {

            case PdfDictionary.N:
                N = value;
                break;

            default:
                super.setFloatNumber(id, value);
        }
    }

    @Override
    public float getFloatNumber(final int id) {

        switch (id) {

            case PdfDictionary.N:
                return N;

            default:
                return super.getFloatNumber(id);
        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch (id) {

            case PdfDictionary.BlackPoint:
                BlackPoint = value;
                break;

            case PdfDictionary.Gamma:
                Gamma = value;
                break;

            case PdfDictionary.WhitePoint:
                WhitePoint = value;
                break;

            default:
                super.setFloatArray(id, value);
        }
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
    public byte[] getRawName(final int id) {

        switch (id) {

            case PdfDictionary.Name:
                return rawName;

            default:
                return super.getRawName(id);

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
        return PdfDictionary.ColorSpace;
    }


    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }
}
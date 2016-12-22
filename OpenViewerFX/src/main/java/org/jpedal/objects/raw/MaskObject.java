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
 * MaskObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class MaskObject extends XObject {

    private float[] BC;

    private PdfObject G, TRasDict;

    public MaskObject(final String ref) {
        super(ref);

        objType = PdfDictionary.Mask;
    }

    public MaskObject(final int ref, final int gen) {
        super(ref, gen);

        objType = PdfDictionary.Mask;
    }

    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.G:
                return G;

            case PdfDictionary.TR:
                return TRasDict;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value) {

        switch (id) {

            case PdfDictionary.G:
                G = value;
                break;

            case PdfDictionary.TR:
                TRasDict = value;
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
    public float[] getFloatArray(final int id) {

        switch (id) {

            case PdfDictionary.BC:
                return deepCopy(BC);

            default:
                return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch (id) {

            case PdfDictionary.BC:
                BC = value;
                break;

            default:
                super.setFloatArray(id, value);
        }
    }
}
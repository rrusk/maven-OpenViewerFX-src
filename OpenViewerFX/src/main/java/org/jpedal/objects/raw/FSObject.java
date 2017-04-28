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
 * FSObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class FSObject extends FormObject {

    byte[] rawUF, rawDesc, rawF;

    String UF, Desc, FString;

    private byte[][] Names;

    private PdfObject Assets, EF, Thumb, CI;

    public FSObject(final String ref) {
        super(ref);
    }

    public FSObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.Assets:
                return Assets;

            case PdfDictionary.EF:
                return EF;

            case PdfDictionary.Thumb:
                return Thumb;

            case PdfDictionary.CI:
                return CI;

            default:
                return super.getDictionary(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);
        switch (id) {

            case PdfDictionary.Assets:
                Assets = value;
                break;

            case PdfDictionary.EF:
                EF = value;
                break;

            case PdfDictionary.Thumb:
                Thumb = value;
                break;

            case PdfDictionary.CI:
                CI = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }


    @Override
    public PdfArrayIterator getMixedArray(final int id) {

        switch (id) {

            case PdfDictionary.Names:
                return new PdfArrayIterator(Names);

            default:
                return super.getMixedArray(id);
        }
    }


    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Names:
                Names = value;
                break;

            default:
                super.setMixedArray(id, value);
        }
    }


    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.Desc:
                rawDesc = value;
                break;

            case PdfDictionary.UF:
                rawUF = value;
                break;

            case PdfDictionary.F:
                rawF = value;
                break;

            default:
                super.setTextStreamValue(id, value);

        }
    }

    @Override
    public String getTextStreamValue(final int id) {

        switch (id) {

            case PdfDictionary.Desc:

                //setup first time
                if (Desc == null && rawDesc != null) {
                    Desc = new String(rawDesc);
                }

                return Desc;

            case PdfDictionary.UF:

                //setup first time
                if (UF == null && rawUF != null) {
                    UF = new String(rawUF);
                }

                return UF;

            case PdfDictionary.F:

                //setup first time
                if (FString == null && rawF != null) {
                    FString = new String(rawF);
                }

                return FString;

            default:
                return super.getTextStreamValue(id);

        }
    }

    @Override
    public byte[] getTextStreamValueAsByte(final int id) {

        switch (id) {

            case PdfDictionary.Desc:
                return rawDesc;

            case PdfDictionary.UF:
                return rawUF;

            case PdfDictionary.F:
                return rawF;

            default:
                return super.getTextStreamValueAsByte(id);

        }
    }

    @Override
    public boolean decompressStreamWhenRead() {
        return false;
    }


    @Override
    public int getObjectType() {
        return PdfDictionary.FS;
    }
}
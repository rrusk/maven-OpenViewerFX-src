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
 * SoundObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.StringUtils;

public class SoundObject extends PdfObject {

    byte[] rawE;

    String E;

    int B = -1, Cint = -1, R = -1;

    public SoundObject(final String ref) {
        super(ref);
    }

    public SoundObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.B:
                B = value;
                break;

            case PdfDictionary.C:
                Cint = value;
                break;

            case PdfDictionary.R:
                R = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.B:
                return B;

            case PdfDictionary.C:
                return Cint;

            case PdfDictionary.R:
                return R;

            default:
                return super.getInt(id);
        }
    }

    @Override
    public void setName(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.E:
                rawE = value;
                break;

            default:
                super.setName(id, value);

        }

    }

    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.E:
                rawE = value;
                break;
            default:
                super.setTextStreamValue(id, value);

        }

    }

    @Override
    public String getName(final int id) {

        switch (id) {

            case PdfDictionary.E:

                //setup first time
                if (E == null && rawE != null) {
                    E = StringUtils.getTextString(rawE, false);
                }

                return E;

            default:
                return super.getName(id);

        }
    }


    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }

    @Override
    public int getNameAsConstant(final int id) {

        final byte[] raw;

        switch (id) {

            case PdfDictionary.E:
                raw = rawE;
                break;

            default:
                return super.getNameAsConstant(id);

        }

        if (raw == null) {
            return super.getNameAsConstant(id);
        } else {
            return PdfDictionary.generateChecksum(0, raw.length, raw);
        }

    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Sound;
    }
}
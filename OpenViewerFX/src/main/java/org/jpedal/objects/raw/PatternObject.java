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
 * PatternObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class PatternObject extends XObject {

    int PatternType, PaintType = -1, TilingType = -1;

    float XStep = -1, YStep = -1;

    public PatternObject(final String ref) {
        super(ref);
    }

    public PatternObject(final int ref, final int gen) {
        super(ref, gen);
    }


    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }


    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.PaintType:
                PaintType = value;
                break;

            case PdfDictionary.PatternType:
                PatternType = value;
                break;

            case PdfDictionary.TilingType:
                TilingType = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public void setFloatNumber(final int id, final float value) {

        switch (id) {

            case PdfDictionary.XStep:
                XStep = value;
                break;

            case PdfDictionary.YStep:
                YStep = value;
                break;

            default:
                super.setFloatNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.PaintType:
                return PaintType;

            case PdfDictionary.PatternType:
                return PatternType;

            case PdfDictionary.TilingType:
                return TilingType;

            default:
                return super.getInt(id);
        }
    }

    @Override
    public float getFloatNumber(final int id) {

        switch (id) {

            case PdfDictionary.XStep:
                return XStep;

            case PdfDictionary.YStep:
                return YStep;

            default:
                return super.getFloatNumber(id);
        }
    }
}

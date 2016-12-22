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
 * MKObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.StringUtils;

public class MKObject extends FormObject {

    private float[] BC, BG;

    protected String AC, CA, RC;

    protected byte[] rawAC, rawCA, rawRC;

    private int TP = -1;

    int R;

    private PdfObject I;


    @Override
    public String toString() {
        return "BC=" + org.jpedal.objects.acroforms.utils.ConvertToString.convertArrayToString(getFloatArray(PdfDictionary.BC)) +
                " BG=" + org.jpedal.objects.acroforms.utils.ConvertToString.convertArrayToString(getFloatArray(PdfDictionary.BG)) +
                " AC=" + getTextStreamValue(PdfDictionary.AC) + " CA=" + getTextStreamValue(PdfDictionary.CA) +
                " RC=" + getTextStreamValue(PdfDictionary.RC) +
                " TP=" + TP + " R=" + R + " I=" + I;
    }

    /**
     * creates a copy of this MKObject but in a new Object so that changes wont affect this MkObject
     */
    @Override
    public PdfObject duplicate() {

        final MKObject copy = new MKObject();

        final int sourceTP = this.getInt(PdfDictionary.TP);
        if (sourceTP != -1) {
            copy.setIntNumber(PdfDictionary.TP, sourceTP);
        }

        final int sourceR = this.getInt(PdfDictionary.R);
        copy.setIntNumber(PdfDictionary.R, sourceR);

        //make sure also added to getTextStreamValueAsByte
        final int[] textStreams = {PdfDictionary.AC, PdfDictionary.CA, PdfDictionary.RC};

        for (final int textStream : textStreams) {
            final byte[] bytes = this.getTextStreamValueAsByte(textStream);
            if (bytes != null) {
                copy.setTextStreamValue(textStream, bytes);
            }
        }

        //make sure also added to getTextStreamValueAsByte
        final int[] floatStreams = {PdfDictionary.BC, PdfDictionary.BG};

        for (final int floatStream : floatStreams) {
            final float[] floats = this.getFloatArray(floatStream);
            if (floats != null) {
                copy.setFloatArray(floatStream, floats);
            }
        }

        if (this.I != null) {
            copy.I = I.duplicate();
        }

        return copy;
    }

    public MKObject(final String ref) {
        super(ref);
    }

    public MKObject(final int ref, final int gen) {
        super(ref, gen);
    }


    public MKObject() {
    }


    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.I:
                return I;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.R:
                R = value;
                break;

            case PdfDictionary.TP:
                TP = value;
                break;


            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.R:
                return R;

            case PdfDictionary.TP:
                return TP;


            default:
                return super.getInt(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);
        switch (id) {

            case PdfDictionary.I:
                I = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }

    @Override
    public float[] getFloatArray(final int id) {

        switch (id) {

            case PdfDictionary.BC:
                return BC;

            case PdfDictionary.BG:
                return BG;


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

            case PdfDictionary.BG:
                BG = value;
                break;


            default:
                super.setFloatArray(id, value);
        }
    }


    @Override
    public byte[] getTextStreamValueAsByte(final int id) {

        switch (id) {

            case PdfDictionary.AC:
                return rawAC;

            case PdfDictionary.CA:
                return rawCA;

            case PdfDictionary.RC:
                return rawRC;

            default:
                return super.getTextStreamValueAsByte(id);

        }
    }


    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.AC:
                rawAC = value;
                break;

            case PdfDictionary.CA:
                rawCA = value;
                break;

            case PdfDictionary.RC:
                rawRC = value;
                break;

            default:
                super.setTextStreamValue(id, value);

        }

    }

    @Override
    public String getTextStreamValue(final int id) {

        switch (id) {

            case PdfDictionary.AC:

                //setup first time
                if (AC == null && rawAC != null) {
                    AC = StringUtils.getTextString(rawAC, false);
                }

                return AC;

            case PdfDictionary.CA:

                //setup first time
                if (CA == null && rawCA != null) {
                    CA = StringUtils.getTextString(rawCA, false);
                }
                return CA;

            case PdfDictionary.RC:

                //setup first time
                if (RC == null && rawRC != null) {
                    RC = StringUtils.getTextString(rawRC, false);
                }

                return RC;

            default:
                return super.getTextStreamValue(id);

        }
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.MK;
    }
}
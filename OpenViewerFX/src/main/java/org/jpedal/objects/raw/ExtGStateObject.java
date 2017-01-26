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
 * ExtGStateObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.LogWriter;

public class ExtGStateObject extends PdfObject {

    private float[] Matrix;

    float CA = -1, ca = -1, LW = -1, OPM = -1;

    byte[][] TR;

    boolean AIS, op, OP;

    PdfObject TRobj;
    private byte[][] BM;

    public ExtGStateObject(final String ref) {
        super(ref);
    }

    public ExtGStateObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public float getFloatNumber(final int id) {

        switch (id) {

            case PdfDictionary.CA:
                return CA;

            case PdfDictionary.ca:
                return ca;

            case PdfDictionary.LW:
                return LW;

            case PdfDictionary.OPM:
                return OPM;

            default:
                return super.getFloatNumber(id);
        }
    }

    @Override
    public void setFloatNumber(final int id, final float value) {

        switch (id) {

            case PdfDictionary.CA:
                CA = value;
                break;

            case PdfDictionary.ca:
                ca = value;
                break;

            case PdfDictionary.LW:
                LW = value;
                break;

            case PdfDictionary.OPM:
                OPM = value;
                break;

            default:
                super.setFloatNumber(id, value);
        }
    }

    @Override
    public boolean getBoolean(final int id) {

        switch (id) {

            case PdfDictionary.AIS:
                return AIS;

            case PdfDictionary.op:
                return op;

            case PdfDictionary.OP:
                return OP;

            default:
                return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value) {

        switch (id) {

            case PdfDictionary.AIS:
                AIS = value;
                break;

            case PdfDictionary.OP:
                OP = value;
                break;

            case PdfDictionary.op:
                op = value;
                break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.TR:
                return TRobj;

            default:
                return super.getDictionary(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.TR:
                TRobj = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }


    @Override
    public int setConstant(final int pdfKeyType, final int keyStart, final int keyLength, final byte[] raw) {

        int PDFvalue = PdfDictionary.Unknown;

        try {

            final int id = PdfObject.getId(keyStart, keyLength, raw);

            switch (id) {

                case PdfDictionary.Image:
                    PDFvalue = PdfDictionary.Image;
                    break;

                case PdfDictionary.Form:
                    PDFvalue = PdfDictionary.Form;
                    break;

                default:
                    PDFvalue = super.setConstant(pdfKeyType, id);
                    break;

            }

        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }

        return PDFvalue;
    }

    @Override
    public PdfArrayIterator getMixedArray(final int id) {

        switch (id) {

            case PdfDictionary.BM:

                return new PdfArrayIterator(BM);

            default:

                return super.getMixedArray(id);
        }
    }


    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch (id) {


            case PdfDictionary.BM:

                BM = value;
                break;

            default:
                super.setMixedArray(id, value);
        }
    }

    @Override
    public float[] getFloatArray(final int id) {

        switch (id) {

            case PdfDictionary.Matrix:
                return deepCopy(Matrix);

            default:
                return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch (id) {

            case PdfDictionary.Matrix:
                Matrix = value;
                break;

            default:
                super.setFloatArray(id, value);
        }
    }

    @Override
    public byte[][] getKeyArray(final int id) {

        switch (id) {

            case PdfDictionary.TR:
                return deepCopy(TR);

            default:
                return super.getKeyArray(id);
        }
    }

    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.TR:
                TR = value;
                break;

            default:
                super.setKeyArray(id, value);
        }

    }
}
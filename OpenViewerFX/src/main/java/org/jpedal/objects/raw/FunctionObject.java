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
 * FunctionObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class FunctionObject extends PdfObject {

    byte[][] Functions;

    float[] Bounds, C0, C1, Encode;

    int[] Size;

    int BitsPerSample = -1, FunctionType = -1;

    float N = -1;

    public FunctionObject(final String ref) {
        super(ref);
    }

    public FunctionObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public int[] getIntArray(final int id) {

        switch (id) {

            case PdfDictionary.Size:
                return deepCopy(Size);

            default:
                return super.getIntArray(id);
        }
    }

    @Override
    public void setIntArray(final int id, final int[] value) {

        switch (id) {

            case PdfDictionary.Size:
                Size = value;
                break;

            default:
                super.setIntArray(id, value);
        }
    }


    public FunctionObject(final int type) {
        super(type);
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.BitsPerSample:
                BitsPerSample = value;
                break;

            case PdfDictionary.FunctionType:
                FunctionType = value;
                break;

            case PdfDictionary.N:
                N = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.BitsPerSample:
                return BitsPerSample;

            case PdfDictionary.FunctionType:
                return FunctionType;

            default:
                return super.getInt(id);
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
    public PdfArrayIterator getMixedArray(final int id) {

        switch (id) {

            case PdfDictionary.Functions:
                return new PdfArrayIterator(Functions);

            default:
                return super.getMixedArray(id);
        }
    }

    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Functions:
                Functions = value;
                break;

            default:
                super.setMixedArray(id, value);
        }
    }

    @Override
    public float[] getFloatArray(final int id) {

        switch (id) {

            case PdfDictionary.Bounds:
                return deepCopy(Bounds);

            case PdfDictionary.C0:
                return deepCopy(C0);

            case PdfDictionary.C1:
                return deepCopy(C1);

            case PdfDictionary.Encode:
                return deepCopy(Encode);

            default:
                return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch (id) {

            case PdfDictionary.Bounds:
                Bounds = value;
                break;

            case PdfDictionary.C0:
                C0 = value;
                break;

            case PdfDictionary.C1:
                C1 = value;
                break;

            case PdfDictionary.Encode:
                Encode = value;
                break;

            default:
                super.setFloatArray(id, value);
        }
    }

    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Function;
    }
}
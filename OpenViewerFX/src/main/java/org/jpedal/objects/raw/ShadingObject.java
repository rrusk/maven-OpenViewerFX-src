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
 * ShadingObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import java.io.Serializable;

public class ShadingObject extends PdfObject implements Serializable {

    int ShadingType = -1;

    boolean AntiAlias;

    float[] Array, Background, Coords;

    byte[][] Functions;

    float N = -1;

    boolean[] Extend;

    public ShadingObject(final String ref) {
        super(ref);
    }

    public ShadingObject(final int ref, final int gen) {
        super(ref, gen);
    }


    @Override
    public byte[][] getKeyArray(final int id) {

        switch (id) {

            case PdfDictionary.Function:
                return deepCopy(Functions);

            default:
                return super.getKeyArray(id);
        }
    }

    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Function:
                Functions = value;
                break;

            default:
                super.setKeyArray(id, value);
        }

    }


    @Override
    public boolean getBoolean(final int id) {

        switch (id) {

            case PdfDictionary.AntiAlias:
                return AntiAlias;


            default:
                return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value) {

        switch (id) {

            case PdfDictionary.AntiAlias:
                AntiAlias = value;
                break;

            default:
                super.setBoolean(id, value);
        }
    }


    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.ShadingType:
                ShadingType = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {


            case PdfDictionary.ShadingType:
                return ShadingType;

            default:
                return super.getInt(id);
        }
    }

    @Override
    public boolean[] getBooleanArray(final int id) {

        switch (id) {

            case PdfDictionary.Extend:
                return deepCopy(Extend);

            default:
                return super.getBooleanArray(id);

        }
    }

    @Override
    public void setBooleanArray(final int id, final boolean[] value) {

        switch (id) {

            case PdfDictionary.Extend:
                Extend = value;
                break;

            default:
                super.setBooleanArray(id, value);
        }
    }


    @Override
    public float[] getFloatArray(final int id) {

        switch (id) {

            case PdfDictionary.Array:
                return Array;

            case PdfDictionary.Background:
                return Background;

            case PdfDictionary.Coords:
                return Coords;

            default:
                return super.getFloatArray(id);

        }
    }

    @Override
    public void setFloatArray(final int id, final float[] value) {

        switch (id) {

            case PdfDictionary.Array:
                Array = value;
                break;

            case PdfDictionary.Background:
                Background = value;
                break;

            case PdfDictionary.Coords:
                Coords = value;
                break;

            default:
                super.setFloatArray(id, value);
        }
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Shading;
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
    public boolean decompressStreamWhenRead() {
        return true;
    }
}

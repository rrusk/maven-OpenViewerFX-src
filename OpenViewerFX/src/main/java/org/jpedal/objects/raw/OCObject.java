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
 * OCObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class OCObject extends PdfObject {

    float max, min;

    int Event = -1;

    private byte[] rawBaseState, rawListMode, rawViewState;
    String BaseState, ListMode;

    private PdfObject D, Layer, OCGs_dictionary, Usage, View, Zoom;

    private Object[] Order;
    private byte[][] AS, Category, Locked, ON, OFF, OCGs, Configs, RBGroups;

    public OCObject(final String ref) {
        super(ref);
    }

    public OCObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.D:
                return D;

            case PdfDictionary.Layer:
                return Layer;

            case PdfDictionary.OCGs:
                return OCGs_dictionary;

            case PdfDictionary.Usage:
                return Usage;

            case PdfDictionary.View:
                return View;

            case PdfDictionary.Zoom:
                return Zoom;

            default:
                return super.getDictionary(id);
        }
    }


    @Override
    public void setFloatNumber(final int id, final float value) {

        switch (id) {

            case PdfDictionary.max:
                max = value;
                break;

            case PdfDictionary.min:
                min = value;
                break;


            default:

                super.setFloatNumber(id, value);
        }
    }

    @Override
    public float getFloatNumber(final int id) {

        switch (id) {

            case PdfDictionary.max:
                return max;

            case PdfDictionary.min:
                return min;

            default:

                return super.getFloatNumber(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.D:
                D = value;
                break;

            case PdfDictionary.Layer:
                Layer = value;
                break;

            case PdfDictionary.OCGs:
                OCGs_dictionary = value;
                break;

            case PdfDictionary.Usage:
                Usage = value;
                break;

            case PdfDictionary.View:
                View = value;
                break;

            case PdfDictionary.Zoom:
                Zoom = value;
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

            case PdfDictionary.Event:
                Event = PDFvalue;
                break;

        }

        return PDFvalue;
    }


    @Override
    public int getParameterConstant(final int key) {

        //System.out.println("Get constant for "+key +" "+this);
        switch (key) {

            case PdfDictionary.Event:
                return Event;

            default:
                return super.getParameterConstant(key);

        }
    }

    @Override
    public void setName(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.ListMode:
                rawListMode = value;
                break;

            case PdfDictionary.ViewState:
                rawViewState = value;
                break;
            default:
                super.setName(id, value);

        }

    }


    //return as constnt we can check
    @Override
    public int getNameAsConstant(final int id) {

        final byte[] raw;

        switch (id) {

            case PdfDictionary.BaseState:
                raw = rawBaseState;
                break;

            case PdfDictionary.ListMode:
                raw = rawListMode;
                break;

            case PdfDictionary.ViewState:
                raw = rawViewState;
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
    public String getName(final int id) {

        switch (id) {

            case PdfDictionary.BaseState:

                //setup first time
                if (BaseState == null && rawBaseState != null) {
                    BaseState = new String(rawBaseState);
                }

                return BaseState;

            case PdfDictionary.ListMode:

                //setup first time
                if (ListMode == null && rawListMode != null) {
                    ListMode = new String(rawListMode);
                }

                return ListMode;


            default:
                return super.getName(id);

        }
    }

    @Override
    public byte[][] getKeyArray(final int id) {

        switch (id) {

            case PdfDictionary.AS:
                return AS;

            case PdfDictionary.Category:
                return Category;

            case PdfDictionary.Configs:
                return Configs;

            case PdfDictionary.Locked:
                return Locked;

            case PdfDictionary.OCGs:
                return OCGs;

            case PdfDictionary.OFF:
                return OFF;

            case PdfDictionary.ON:
                return ON;

            case PdfDictionary.RBGroups:
                return RBGroups;

            default:
                return super.getKeyArray(id);
        }
    }

    @Override
    public void setObjectArray(final int id, final Object[] objectValues) {

        switch (id) {

            case PdfDictionary.Order:
                Order = objectValues;
                break;

            default:
                super.setObjectArray(id, objectValues);
                break;
        }
    }

    @Override
    public Object[] getObjectArray(final int id) {

        switch (id) {

            case PdfDictionary.Order:
                return deepCopy(Order);

            default:
                return super.getObjectArray(id);
        }
    }

    protected static Object[] deepCopy(final Object[] input) {

        if (input == null) {
            return null;
        }

        final int count = input.length;

        final Object[] deepCopy = new Object[count];

        for (int aa = 0; aa < count; aa++) {

            if (input[aa] instanceof byte[]) {
                final byte[] byteVal = (byte[]) input[aa];
                final int byteCount = byteVal.length;

                final byte[] newValue = new byte[byteCount];
                deepCopy[aa] = newValue;

                System.arraycopy(byteVal, 0, newValue, 0, byteCount);
            } else {
                deepCopy[aa] = deepCopy((Object[]) input[aa]);
            }
        }

        return deepCopy;
    }


    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.AS:
                AS = value;
                break;

            case PdfDictionary.Category:
                Category = value;
                break;

            case PdfDictionary.Configs:
                Configs = value;
                break;

            case PdfDictionary.Locked:
                Locked = value;
                break;

            case PdfDictionary.OCGs:
                OCGs = value;
                break;

            case PdfDictionary.OFF:
                OFF = value;
                break;

            case PdfDictionary.ON:
                ON = value;
                break;

            case PdfDictionary.RBGroups:
                RBGroups = value;
                break;

            default:
                super.setKeyArray(id, value);
        }

    }

    @Override
    public int getObjectType() {
        return PdfDictionary.OCProperties;
    }
}
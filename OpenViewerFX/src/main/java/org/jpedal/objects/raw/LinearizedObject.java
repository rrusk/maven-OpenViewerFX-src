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
 * LinearizedObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class LinearizedObject extends PdfObject {

    int L = -1, O = -1, E = -1, N = -1, P, S = -1, T = -1;
    private int[] H;

    public LinearizedObject(final String ref) {
        super(ref);
    }

    public LinearizedObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.E:
                E = value;
                break;

            case PdfDictionary.L:
                L = value;
                break;

            case PdfDictionary.N:
                N = value;
                break;

            case PdfDictionary.O:
                O = value;
                break;

            case PdfDictionary.P:
                P = value;
                break;

            case PdfDictionary.S:
                S = value;
                break;

            case PdfDictionary.T:
                T = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.E:
                return E;

            case PdfDictionary.L:
                return L;

            case PdfDictionary.N:
                return N;

            case PdfDictionary.O:
                return O;

            case PdfDictionary.P:
                return P;

            case PdfDictionary.S:
                return S;

            case PdfDictionary.T:
                return T;

            default:
                return super.getInt(id);
        }
    }

    @Override
    public int[] getIntArray(final int id) {

        switch (id) {

            case PdfDictionary.H:
                return H;

            default:
                return super.getIntArray(id);
        }
    }

    @Override
    public void setIntArray(final int id, final int[] value) {

        switch (id) {

            case PdfDictionary.H:
                H = value;
                break;

            default:
                super.setIntArray(id, value);
        }
    }

    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }


    @Override
    public int getObjectType() {
        return PdfDictionary.Linearized;
    }
}
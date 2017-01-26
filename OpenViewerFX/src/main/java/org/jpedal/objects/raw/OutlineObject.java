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
 * OutlineObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.StringUtils;

public class OutlineObject extends PdfObject {

    String Fstring, Title;
    byte[] rawFstring, rawTitle;

    private PdfObject A, Fdict, First, Next, Last;
    private byte[][] Dest, D;


    public OutlineObject(final String ref) {
        super(ref);
    }

    public OutlineObject(final int ref, final int gen) {
        super(ref, gen);
    }


    @Override
    public byte[] getTextStreamValueAsByte(final int id) {

        switch (id) {

            case PdfDictionary.Title:

                return rawTitle;

            default:
                return super.getTextStreamValueAsByte(id);

        }
    }


    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.A:
                return A;

            case PdfDictionary.F:
                return Fdict;

            case PdfDictionary.First:
                return First;

            case PdfDictionary.Last:
                return Last;

            case PdfDictionary.Next:
                return Next;

            default:
                return super.getDictionary(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.A:
                A = value;
                break;

            case PdfDictionary.F:
                Fdict = value;
                break;

            case PdfDictionary.First:
                First = value;
                break;

            case PdfDictionary.Last:
                Last = value;
                break;

            case PdfDictionary.Next:
                Next = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }

    @Override
    public PdfArrayIterator getMixedArray(final int id) {

        switch (id) {

            case PdfDictionary.D:
                return new PdfArrayIterator(D);

            case PdfDictionary.Dest:
                return new PdfArrayIterator(Dest);

            default:
                return super.getMixedArray(id);
        }
    }


    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.D:
                D = value;
                break;

            case PdfDictionary.Dest:
                Dest = value;
                break;

            default:
                super.setMixedArray(id, value);
        }
    }


    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.F:
                rawFstring = value;
                break;

            case PdfDictionary.Title:
                rawTitle = value;
                break;


            default:
                super.setTextStreamValue(id, value);

        }

    }


    @Override
    public String getTextStreamValue(final int id) {

        switch (id) {

            case PdfDictionary.F:

                //setup first time
                if (Fstring == null && rawFstring != null) {
                    Fstring = StringUtils.getTextString(rawFstring, false);
                }

                return Fstring;

            case PdfDictionary.Title:

                //setup first time
                if (Title == null && rawTitle != null) {
                    Title = new String(rawTitle);
                }

                return Title;

            default:
                return super.getTextStreamValue(id);

        }
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Outlines;
    }
}
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
 * ResourcesObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class ResourcesObject extends PdfObject {

    private byte[][] ProcSet;

    private PdfObject ExtGState, Font, Pattern, XObject, Properties;

    public ResourcesObject(final String ref) {
        super(ref);
    }

    public ResourcesObject(final int ref, final int gen) {
        super(ref, gen);
    }


    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.ExtGState:
                return ExtGState;

            case PdfDictionary.Font:
                return Font;

            case PdfDictionary.Pattern:
                return Pattern;

            case PdfDictionary.Properties:
                return Properties;

            case PdfDictionary.XObject:
                return XObject;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.ExtGState:
                ExtGState = value;
                break;

            case PdfDictionary.Font:
                Font = value;
                break;

            case PdfDictionary.Pattern:
                Pattern = value;
                break;

            case PdfDictionary.Properties:
                Properties = value;
                break;

            case PdfDictionary.XObject:
                XObject = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }

    @Override
    public PdfArrayIterator getMixedArray(final int id) {

        switch (id) {

            case PdfDictionary.ProcSet:
                return new PdfArrayIterator(ProcSet);

            default:
                return super.getMixedArray(id);
        }
    }


    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.ProcSet:
                ProcSet = value;
                break;

            default:
                super.setMixedArray(id, value);
        }

    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Resources;
    }
}

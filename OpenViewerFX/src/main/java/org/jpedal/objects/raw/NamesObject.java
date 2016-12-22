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
 * NamesObject.java
 * ---------------
 */
package org.jpedal.objects.raw;


public class NamesObject extends PdfObject {

    byte[] rawJS;

    String JSString;

    private PdfObject Dests, EmbeddedFiles, JavaScript, JS, XFAImages;

    private byte[][] Kids, Names;

    private byte[][] Limits;


    public NamesObject(final String ref) {
        super(ref);
    }

    public NamesObject(final int ref, final int gen) {
        super(ref, gen);
    }


    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.Dests:
                return Dests;

            case PdfDictionary.EmbeddedFiles:
                return EmbeddedFiles;

            case PdfDictionary.JavaScript:
                return JavaScript;

            case PdfDictionary.JS:
                return JS;

            case PdfDictionary.XFAImages:
                return XFAImages;

            default:
                return super.getDictionary(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        //if in AP array as other value store here
        if (currentKey != null) {

            setOtherValues(value);
            return;
        }

        switch (id) {

            case PdfDictionary.Dests:
                Dests = value;
                break;

            case PdfDictionary.EmbeddedFiles:
                EmbeddedFiles = value;
                break;

            case PdfDictionary.JavaScript:
                JavaScript = value;
                break;

            case PdfDictionary.JS:
                JS = value;
                break;

            case PdfDictionary.XFAImages:
                XFAImages = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }

    @Override
    public PdfArrayIterator getMixedArray(final int id) {

        switch (id) {

            case PdfDictionary.Names:
                return new PdfArrayIterator(Names);

            default:
                return super.getMixedArray(id);
        }
    }


    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Names:
                Names = value;
                break;

            default:
                super.setMixedArray(id, value);
        }
    }

    @Override
    public void setStringArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Limits:
                Limits = value;
                break;

            default:
                super.setMixedArray(id, value);
        }
    }


    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.JS:
                rawJS = value;
                break;

            default:
                super.setTextStreamValue(id, value);

        }

    }


    @Override
    public String getTextStreamValue(final int id) {

        switch (id) {

            case PdfDictionary.JS:

                //setup first time
                if (JSString == null && rawJS != null) {
                    //JSString= PdfObjectReader.getTextString(rawJS);
                    JSString = new String(rawJS);
                }
                return JSString;

            default:
                return super.getTextStreamValue(id);

        }
    }

    @Override
    public byte[][] getStringArray(final int id) {

        switch (id) {

            case PdfDictionary.Limits:
                return deepCopy(Limits);

            default:
                return super.getKeyArray(id);
        }
    }

    @Override
    public byte[][] getKeyArray(final int id) {

        switch (id) {

            case PdfDictionary.Kids:
                return deepCopy(Kids);

            default:
                return super.getKeyArray(id);
        }
    }

    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Kids:
                Kids = value;
                break;

            default:
                super.setKeyArray(id, value);
        }

    }

    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Names;
    }
}
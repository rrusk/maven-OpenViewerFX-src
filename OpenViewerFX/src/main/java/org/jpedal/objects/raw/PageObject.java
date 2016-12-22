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
 * PageObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class PageObject extends PdfObject {

    private byte[][] Annots, Contents, Kids, OpenAction;

    PdfObject AA, AcroForm, Dests, Group, OCProperties, O, PO, PageLabels, Properties, PV, Metadata, Outlines, Pages, MarkInfo, Names, StructTreeRoot;

    private int StructParents = -1, pageMode = -1;

    public PageObject(final String ref) {
        super(ref);
    }

    public PageObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public int getObjectType() {
        return PdfDictionary.Page;
    }


    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.AA:
                return AA;

            case PdfDictionary.AcroForm:
                return AcroForm;

            case PdfDictionary.Dests:
                return Dests;

            case PdfDictionary.Group:
                return Group;

            case PdfDictionary.MarkInfo:
                return MarkInfo;

            case PdfDictionary.Metadata:
                return Metadata;

            case PdfDictionary.O:
                return O;

            case PdfDictionary.OCProperties:
                return OCProperties;

            case PdfDictionary.Outlines:
                return Outlines;

            case PdfDictionary.Pages:
                return Pages;

            case PdfDictionary.PageLabels:
                return PageLabels;

            case PdfDictionary.PO:
                return PO;

            case PdfDictionary.Properties:
                return Properties;

            case PdfDictionary.PV:
                return PV;

            case PdfDictionary.Names:
                return Names;

            case PdfDictionary.StructTreeRoot:
                return StructTreeRoot;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.StructParents:
                StructParents = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.StructParents:
                return StructParents;

            default:
                return super.getInt(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.AA:
                AA = value;
                break;

            case PdfDictionary.AcroForm:
                AcroForm = value;
                break;

            case PdfDictionary.Dests:
                Dests = value;
                break;

            case PdfDictionary.Group:
                Group = value;
                break;

            case PdfDictionary.MarkInfo:
                MarkInfo = value;
                break;

            case PdfDictionary.Metadata:
                Metadata = value;
                break;

            case PdfDictionary.O:
                O = value;
                break;

            case PdfDictionary.OCProperties:
                OCProperties = value;
                break;

            case PdfDictionary.Outlines:
                Outlines = value;
                break;

            case PdfDictionary.Pages:
                Pages = value;
                break;

            case PdfDictionary.PageLabels:
                PageLabels = value;
                break;

            case PdfDictionary.PO:
                PO = value;
                break;

            case PdfDictionary.Properties:
                Properties = value;
                break;

            case PdfDictionary.PV:
                PV = value;
                break;

            case PdfDictionary.Names:
                Names = value;
                break;

            case PdfDictionary.StructTreeRoot:
                StructTreeRoot = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }


    @Override
    public int setConstant(final int pdfKeyType, final int keyStart, final int keyLength, final byte[] raw) {

        int PDFvalue = PdfDictionary.Unknown;

        final int id = PdfObject.getId(keyStart, keyLength, raw);

        switch (id) {

            case PdfDictionary.Page:
                return super.setConstant(pdfKeyType, PdfDictionary.Page);

            case PdfDictionary.Pages:
                return super.setConstant(pdfKeyType, PdfDictionary.Pages);

            case PdfDictionary.PageMode:
                pageMode = id;
                break;

            default:

                PDFvalue = super.setConstant(pdfKeyType, id);
                break;

        }

        return PDFvalue;
    }

    @Override
    public int getParameterConstant(final int key) {

        switch (key) {

            case PdfDictionary.PageMode:
                return pageMode;

            //check general values
            default:
                return super.getParameterConstant(key);
        }
    }

    @Override
    public PdfArrayIterator getMixedArray(final int id) {

        switch (id) {

            case PdfDictionary.OpenAction:
                return new PdfArrayIterator(OpenAction);

            default:
                return super.getMixedArray(id);
        }

    }


    @Override
    public byte[][] getKeyArray(final int id) {

        switch (id) {

            case PdfDictionary.Annots:
                return deepCopy(Annots);

            case PdfDictionary.Contents:
                return deepCopy(Contents);

            case PdfDictionary.Kids:
                return deepCopy(Kids);

            default:
                return super.getKeyArray(id);
        }
    }

    @Override
    public void setKeyArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Annots:
                Annots = value;
                break;

            case PdfDictionary.Contents:
                Contents = value;
                break;

            case PdfDictionary.Kids:
                Kids = value;
                break;

            default:
                super.setKeyArray(id, value);
        }

    }

    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.OpenAction:
                OpenAction = value;
                break;

            default:
                super.setMixedArray(id, value);
        }

    }

}
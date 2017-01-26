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
 * MCObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

import org.jpedal.utils.StringUtils;

public class MCObject extends PdfObject {

    private String ID, OC, Root, S;

    private byte[] rawID, rawOC, rawRoot, rawS;

    int Kint = -1, MCID = -1;

    private PdfObject A, ClassMap, K, Layer, ParentTree, Pg, RoleMap;

    private byte[] rawActualText, rawLang, rawIDTree, rawT;

    private byte[][] Karray;

    private String ActualText, IDTree, Lang, T;

    public MCObject(final String ref) {
        super(ref);

        objType = PdfDictionary.MCID;
    }

    public MCObject(final int ref, final int gen) {
        super(ref, gen);

        objType = PdfDictionary.MCID;
    }


    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.A:
                return A;

            case PdfDictionary.ClassMap:
                return ClassMap;

            case PdfDictionary.K:
                return K;

            case PdfDictionary.Layer:
                return Layer;

            case PdfDictionary.ParentTree:
                return ParentTree;

            case PdfDictionary.Pg:
                return Pg;

            case PdfDictionary.RoleMap:
                return RoleMap;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.K:
                Kint = value;
                break;

            case PdfDictionary.MCID:
                MCID = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.K:
                return Kint;

            case PdfDictionary.MCID:
                return MCID;

            default:
                return super.getInt(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.A:
                A = value;
                break;

            case PdfDictionary.ClassMap:
                ClassMap = value;
                break;

            case PdfDictionary.K:
                K = value;
                break;

            case PdfDictionary.Layer:
                Layer = value;
                break;

            case PdfDictionary.ParentTree:
                ParentTree = value;
                break;

            case PdfDictionary.Pg:
                Pg = value;
                break;

            case PdfDictionary.RoleMap:
                RoleMap = value;
                break;

            default:
                super.setDictionary(id, value);
        }
    }

    @Override
    public void setName(final int id, final byte[] value) {

        switch (id) {


            case PdfDictionary.OC:
                rawOC = value;
                break;

            case PdfDictionary.Root:
                rawRoot = value;
                break;

            case PdfDictionary.S:
                rawS = value;
                break;

            default:
                super.setName(id, value);

        }

    }

    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.ActualText:
                rawActualText = value;
                break;

            case PdfDictionary.ID:
                rawID = value;
                break;

            case PdfDictionary.IDTree:
                rawIDTree = value;
                break;

            case PdfDictionary.Lang:
                rawLang = value;
                break;

            case PdfDictionary.T:
                rawT = value;
                break;

            default:
                super.setTextStreamValue(id, value);

        }

    }

    @Override
    public String getName(final int id) {

        switch (id) {

            case PdfDictionary.OC:

                //setup first time
                if (OC == null && rawOC != null) {
                    OC = new String(rawOC);
                }

                return OC;

            case PdfDictionary.Root:

                //setup first time
                if (Root == null && rawRoot != null) {
                    Root = new String(rawRoot);
                }

                return Root;

            case PdfDictionary.S:

                //setup first time
                if (S == null && rawS != null) {
                    S = new String(rawS);
                }

                return S;

            default:
                return super.getName(id);

        }
    }

    @Override
    public String getTextStreamValue(final int id) {

        switch (id) {

            case PdfDictionary.ActualText:

                //setup first time
                if (ActualText == null && rawActualText != null) {
                    ActualText = StringUtils.getTextString(rawActualText, false);
                }

                return ActualText;

            case PdfDictionary.ID:

                //setup first time
                if (ID == null && rawID != null) {
                    ID = new String(rawID);
                }

                return ID;

            case PdfDictionary.IDTree:

                //setup first time
                if (IDTree == null && rawIDTree != null) {
                    IDTree = new String(rawIDTree);
                }

                return IDTree;

            case PdfDictionary.Lang:

                //setup first time
                if (Lang == null && rawLang != null) {
                    Lang = new String(rawLang);
                }

                return Lang;

            case PdfDictionary.T:

                //setup first time
                if (T == null && rawT != null) {
                    T = new String(rawT);
                }

                return T;

            default:
                return super.getTextStreamValue(id);

        }
    }

    @Override
    public PdfArrayIterator getMixedArray(final int id) {

        switch (id) {

            case PdfDictionary.K:
                if (Karray == null) {
                    return null;
                } else {
                    return new PdfArrayIterator(Karray);
                }

            default:
                return super.getMixedArray(id);
        }
    }

    @Override
    public void setMixedArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.K:
                Karray = value;
                break;

            default:
                super.setStringArray(id, value);
        }

    }
}
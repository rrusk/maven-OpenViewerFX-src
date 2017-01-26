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
 * EncryptionObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class EncryptionObject extends PdfObject {

    boolean EncryptMetadata = true;

    int V = 1; //default value

    int R = -1, P = -1;

    byte[] rawPerms, rawU, rawUE, rawO, rawOE, rawCFM, rawEFF, rawStrF, rawStmF;
    String U, UE, O, OE, EFF, CFM, StrF, StmF;

    private PdfObject CF;
    private byte[][] Recipients;

    public EncryptionObject(final String ref) {
        super(ref);
    }

    public EncryptionObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public boolean getBoolean(final int id) {

        switch (id) {

            case PdfDictionary.EncryptMetadata:
                return EncryptMetadata;

            default:
                return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value) {

        switch (id) {

            case PdfDictionary.EncryptMetadata:
                EncryptMetadata = value;
                break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.CF:
                return CF;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.P:
                P = value;
                break;

            case PdfDictionary.R:
                R = value;
                break;

            case PdfDictionary.V:
                V = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.P:
                return P;

            case PdfDictionary.R:
                return R;

            case PdfDictionary.V:
                return V;

            default:
                return super.getInt(id);
        }
    }

    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.CF:
                CF = value;
                break;
            default:
                super.setDictionary(id, value);
        }
    }


    @Override
    public void setName(final int id, final byte[] value) {

        switch (id) {


            case PdfDictionary.CFM:
                rawCFM = value;
                break;

            case PdfDictionary.EFF:
                rawEFF = value;
                break;

            case PdfDictionary.StmF:
                rawStmF = value;
                break;

            case PdfDictionary.StrF:
                rawStrF = value;
                break;

            default:
                super.setName(id, value);

        }

    }

    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.O:
                rawO = value;
                break;

            case PdfDictionary.OE:
                rawOE = value;
                break;

            case PdfDictionary.Perms:
                rawPerms = value;
                break;

            case PdfDictionary.U:
                rawU = value;
                break;

            case PdfDictionary.UE:
                rawUE = value;
                break;


            default:
                super.setTextStreamValue(id, value);

        }

    }

    @Override
    public String getName(final int id) {

        switch (id) {

            case PdfDictionary.CFM:

                //setup first time
                if (CFM == null && rawCFM != null) {
                    CFM = new String(rawCFM);
                }

                return CFM;

            case PdfDictionary.EFF:

                //setup first time
                if (EFF == null && rawEFF != null) {
                    EFF = new String(rawEFF);
                }

                return EFF;

            case PdfDictionary.StmF:

                //setup first time
                if (StmF == null && rawStmF != null) {
                    StmF = new String(rawStmF);
                }

                return StmF;


            case PdfDictionary.StrF:

                //setup first time
                if (StrF == null && rawStrF != null) {
                    StrF = new String(rawStrF);
                }

                return StrF;

            default:
                return super.getName(id);

        }
    }

    @Override
    public String getTextStreamValue(final int id) {

        switch (id) {

            case PdfDictionary.O:

                //setup first time
                if (O == null && rawO != null) {
                    O = new String(rawO);
                }

                return O;

            case PdfDictionary.OE:

                //setup first time
                if (OE == null && rawOE != null) {
                    OE = new String(rawOE);
                }

                return OE;


            case PdfDictionary.U:

                //setup first time
                if (U == null && rawU != null) {
                    U = new String(rawU);
                }

                return U;

            case PdfDictionary.UE:

                //setup first time
                if (UE == null && rawUE != null) {
                    UE = new String(rawUE);
                }

                return UE;


            default:
                return super.getTextStreamValue(id);

        }
    }

    @Override
    public byte[] getTextStreamValueAsByte(final int id) {

        switch (id) {

            case PdfDictionary.O:

                return rawO;

            case PdfDictionary.OE:

                return rawOE;

            case PdfDictionary.Perms:

                return rawPerms;

            case PdfDictionary.U:

                //setup first time
                if (U == null && rawU != null) {
                    U = new String(rawU);
                }

                return rawU;

            case PdfDictionary.UE:

                //setup first time
                if (UE == null && rawUE != null) {
                    UE = new String(rawUE);
                }

                return rawUE;

            default:
                return super.getTextStreamValueAsByte(id);

        }
    }

    @Override
    public byte[][] getStringArray(final int id) {

        switch (id) {

            case PdfDictionary.Recipients:
                return deepCopy(Recipients);

            default:
                return super.getStringArray(id);
        }
    }

    @Override
    public void setStringArray(final int id, final byte[][] value) {

        switch (id) {

            case PdfDictionary.Recipients:
                Recipients = value;
                break;

            default:
                super.setStringArray(id, value);
        }

    }


    @Override
    public int getObjectType() {
        return PdfDictionary.Encrypt;
    }
}
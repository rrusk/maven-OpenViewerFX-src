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
 * DecodeParmsObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class DecodeParmsObject extends PdfObject {

    boolean EncodedByteAlign, EndOfBlock = true, EndOfLine, BlackIs1, Uncompressed;

    PdfObject JBIG2Globals;

    int Blend = -1, Colors = -1, ColorTransform = 1, Columns = -1, DamagedRowsBeforeError, EarlyChange = 1, K, Predictor = 1, QFactor = -1, Rows = -1;

    public DecodeParmsObject(final String ref) {
        super(ref);
    }

    public DecodeParmsObject(final int ref, final int gen) {
        super(ref, gen);
    }

    @Override
    public boolean getBoolean(final int id) {

        switch (id) {

            case PdfDictionary.BlackIs1:
                return BlackIs1;

            case PdfDictionary.EncodedByteAlign:
                return EncodedByteAlign;

            case PdfDictionary.EndOfBlock:
                return EndOfBlock;

            case PdfDictionary.EndOfLine:
                return EndOfLine;

            case PdfDictionary.Uncompressed:
                return Uncompressed;

            default:
                return super.getBoolean(id);
        }

    }

    @Override
    public void setBoolean(final int id, final boolean value) {

        switch (id) {

            case PdfDictionary.BlackIs1:
                BlackIs1 = value;
                break;

            case PdfDictionary.EncodedByteAlign:
                EncodedByteAlign = value;
                break;

            case PdfDictionary.EndOfBlock:
                EndOfBlock = value;
                break;

            case PdfDictionary.EndOfLine:
                EndOfLine = value;
                break;

            case PdfDictionary.Uncompressed:
                Uncompressed = value;
                break;

            default:
                super.setBoolean(id, value);
        }
    }

    @Override
    public PdfObject getDictionary(final int id) {

        switch (id) {

            case PdfDictionary.JBIG2Globals:
                return JBIG2Globals;

            default:
                return super.getDictionary(id);
        }
    }

    @Override
    public void setIntNumber(final int id, final int value) {

        switch (id) {

            case PdfDictionary.Blend:
                Blend = value;
                break;

            case PdfDictionary.Colors:
                Colors = value;
                break;

            case PdfDictionary.ColorTransform:
                ColorTransform = value;
                break;

            case PdfDictionary.Columns:
                Columns = value;
                break;

            case PdfDictionary.DamagedRowsBeforeError:
                DamagedRowsBeforeError = value;
                break;

            case PdfDictionary.EarlyChange:
                EarlyChange = value;
                break;

            case PdfDictionary.K:
                K = value;
                break;

            case PdfDictionary.Predictor:
                Predictor = value;
                break;

            case PdfDictionary.QFactor:
                QFactor = value;
                break;

            case PdfDictionary.Rows:
                Rows = value;
                break;

            default:
                super.setIntNumber(id, value);
        }
    }

    @Override
    public int getInt(final int id) {

        switch (id) {

            case PdfDictionary.Blend:
                return Blend;

            case PdfDictionary.Colors:
                return Colors;

            case PdfDictionary.ColorTransform:
                return ColorTransform;

            case PdfDictionary.Columns:
                return Columns;

            case PdfDictionary.DamagedRowsBeforeError:
                return DamagedRowsBeforeError;

            case PdfDictionary.EarlyChange:
                return EarlyChange;

            case PdfDictionary.K:
                return K;

            case PdfDictionary.Predictor:
                return Predictor;

            case PdfDictionary.QFactor:
                return QFactor;

            case PdfDictionary.Rows:
                return Rows;

            default:
                return super.getInt(id);
        }
    }


    @Override
    public void setDictionary(final int id, final PdfObject value) {

        value.setID(id);

        switch (id) {

            case PdfDictionary.JBIG2Globals:
                JBIG2Globals = value;
                break;


            default:
                super.setDictionary(id, value);
        }
    }

    @Override
    public boolean decompressStreamWhenRead() {
        return true;
    }

}
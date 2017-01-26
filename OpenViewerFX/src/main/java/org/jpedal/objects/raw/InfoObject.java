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
 * InfoObject.java
 * ---------------
 */
package org.jpedal.objects.raw;

public class InfoObject extends PdfObject {

    private String Author, CreationDate, Creator, ModDate, Producer, Keywords, Subject, Title, Trapped;

    private byte[] rawAuthor, rawCreationDate, rawCreator, rawModDate, rawProducer, rawKeywords, rawSubject, rawTitle, rawTrapped;

    public InfoObject(final String ref) {
        super(ref);
    }

    public InfoObject(final int ref, final int gen) {
        super(ref, gen);
    }


    @Override
    public void setName(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.Trapped:
                rawTrapped = value;
                break;

            default:
                super.setName(id, value);

        }
    }

    @Override
    public void setTextStreamValue(final int id, final byte[] value) {

        switch (id) {

            case PdfDictionary.Author:
                rawAuthor = value;
                break;

            case PdfDictionary.CreationDate:
                rawCreationDate = value;
                break;

            case PdfDictionary.Creator:
                rawCreator = value;
                break;

            case PdfDictionary.Keywords:
                rawKeywords = value;
                break;

            case PdfDictionary.ModDate:
                rawModDate = value;
                break;

            case PdfDictionary.Producer:
                rawProducer = value;
                break;

            case PdfDictionary.Subject:
                rawSubject = value;
                break;

            case PdfDictionary.Title:
                rawTitle = value;
                break;


            default:
                super.setTextStreamValue(id, value);

        }

    }

    @Override
    public byte[] getTextStreamValueAsByte(final int id) {

        switch (id) {

            case PdfDictionary.Author:
                return rawAuthor;

            case PdfDictionary.CreationDate:
                return rawCreationDate;

            case PdfDictionary.Creator:
                return rawCreator;

            case PdfDictionary.Keywords:
                return rawKeywords;

            case PdfDictionary.ModDate:
                return rawModDate;

            case PdfDictionary.Producer:
                return rawProducer;

            case PdfDictionary.Subject:
                return rawSubject;

            case PdfDictionary.Title:
                return rawTitle;


            default:
                super.getTextStreamValueAsByte(id);

        }

        return null;

    }

    @Override
    public String getName(final int id) {

        switch (id) {

            case PdfDictionary.Trapped:

                //setup first time
                if (Trapped == null && rawTrapped != null) {
                    Trapped = new String(rawTrapped);
                }

                return Trapped;

            default:
                return super.getName(id);

        }
    }

    @Override
    public String getTextStreamValue(final int id) {

        switch (id) {

            case PdfDictionary.Author:

                //setup first time
                if (Author == null && rawAuthor != null) {
                    Author = new String(rawAuthor);
                }

                return Author;

            case PdfDictionary.CreationDate:

                //setup first time
                if (CreationDate == null && rawCreationDate != null) {
                    CreationDate = new String(rawCreationDate);
                }

                return CreationDate;

            case PdfDictionary.Creator:

                //setup first time
                if (Creator == null && rawCreator != null) {
                    Creator = new String(rawCreator);
                }

                return Creator;

            case PdfDictionary.Keywords:

                //setup first time
                if (Keywords == null && rawKeywords != null) {
                    Keywords = new String(rawKeywords);
                }

                return Keywords;

            case PdfDictionary.ModDate:

                //setup first time
                if (ModDate == null && rawModDate != null) {
                    ModDate = new String(rawModDate);
                }

                return ModDate;

            case PdfDictionary.Producer:

                //setup first time
                if (Producer == null && rawProducer != null) {
                    Producer = new String(rawProducer);
                }

                return Producer;

            case PdfDictionary.Subject:

                //setup first time
                if (Subject == null && rawSubject != null) {
                    Subject = new String(rawSubject);
                }

                return Subject;


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
        return PdfDictionary.Metadata;
    }
}
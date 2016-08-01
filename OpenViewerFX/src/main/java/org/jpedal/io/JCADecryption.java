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
 *     This library is free software; you can redistribute it and/or
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
 * JCADecryption.java
 * ---------------
 */
package org.jpedal.io;

import java.lang.reflect.Field;
import java.security.Key;
import java.security.cert.Certificate;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.jpedal.exception.PdfSecurityException;

public class JCADecryption implements BaseDecryption {

    public JCADecryption() {
        allowBiggerKeySize();
    }
    
    @Override
    public byte[] v5Decrypt(final byte[] rawValue, final byte[] encKey) throws PdfSecurityException {

        byte[] returnKey = new byte[rawValue.length];

        try {
            SecretKeySpec key = new SecretKeySpec(encKey, "AES");
            Cipher c = Cipher.getInstance("AES/CBC/NOPADDING");
            c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
            returnKey = c.doFinal(rawValue);

        } catch (final Exception e) {
            throw new PdfSecurityException("Exception " + e.getMessage() + " with v5 encoding");
        }
        return returnKey;
    }

    @Override
    public byte[] decodeAES(final byte[] encKey, final byte[] encData, final byte[] ivData) throws Exception {
        byte[] out = null;
        try {
            SecretKeySpec key = new SecretKeySpec(encKey, "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivData));
            out = c.doFinal(encData);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    private void allowBiggerKeySize() {
        //See case 26048 for notes on this method
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public byte[] readCertificate(final byte[][] recipients, final Certificate certificate, final Key key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

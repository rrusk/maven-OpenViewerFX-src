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
 * CryptoAES.java
 * ---------------
 */
package org.jpedal.io.security;

import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoAES implements Crypto {

    private static final byte[] SALT = new byte[16];
    static{
        new Random().nextBytes(SALT);
    }
    
    @Override
    public byte[] encrypt(final byte[] password, final byte[] data) throws Exception {
        final SecretKeySpec secretKey = new SecretKeySpec(generateKey(password), "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    @Override
    public byte[] decrypt(final byte[] password, final byte[] data) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        final SecretKeySpec secretKey = new SecretKeySpec(generateKey(password), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private static byte[] generateKey(final byte[] pass) {
        final byte[] key = SALT.clone();
        System.arraycopy(pass, 0, key, 0, Math.min(pass.length, SALT.length));
        return key;
    }

//    public static void main(String[] args) throws Exception {
//        String inp = "michael madhan 123456 david %$£$%&*()";
//        String pass = "123456789123456789123456789";
//        CryptoAES aes = new CryptoAES();
//        byte[] enc = aes.encrypt(pass.getBytes(), inp.getBytes());
//        System.out.println(new String(enc));
//        System.out.println(new String(aes.decrypt(pass.getBytes(), enc)));
//    }

}

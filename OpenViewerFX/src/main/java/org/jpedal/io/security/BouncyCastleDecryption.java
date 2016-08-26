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
 * BouncyCastleDecryption.java
 * ---------------
 */
package org.jpedal.io.security;

import java.security.Key;
import java.security.cert.Certificate;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.utils.LogWriter;

public class BouncyCastleDecryption implements BaseDecryption{
    
    @Override
    public byte[] v5Decrypt(final byte[] rawValue, final byte[] key) throws PdfSecurityException {
        final int ELength= rawValue.length;
        final byte[] returnKey = new byte[ELength];
        
        try{

            //setup Cipher
            final BlockCipher cbc = new CBCBlockCipher(new AESFastEngine());
            cbc.init(false, new KeyParameter(key));

            //translate bytes
            int nextBlockSize;
            for(int i=0;i<ELength;i += nextBlockSize){
                cbc.processBlock(rawValue, i, returnKey, i);
                nextBlockSize=cbc.getBlockSize();
            }
            
        }catch(final Exception e){
            throw new PdfSecurityException("Exception "+e.getMessage()+" with v5 encoding");
        }
        return returnKey;
    }
    
    
    @Override
    public byte[] decodeAES(final byte[] encKey, final byte[] encData, final byte[] ivData)
            throws Exception {
        
        final KeyParameter keyParam = new KeyParameter(encKey);
        final CipherParameters params = new ParametersWithIV(keyParam, ivData);

        // setup AES cipher in CBC mode with PKCS7 padding
        final BlockCipherPadding padding = new PKCS7Padding();
        final BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new AESEngine()), padding);
        cipher.reset();
        cipher.init(false, params);

        // create a temporary buffer to decode into (it'll include padding)
        final byte[] buf = new byte[cipher.getOutputSize(encData.length)];
        int len = cipher.processBytes(encData, 0, encData.length, buf, 0);
        len += cipher.doFinal(buf, len);

        // remove padding
        final byte[] out = new byte[len];
        System.arraycopy(buf, 0, out, 0, len);

        // return string representation of decoded bytes
        return out;
    }   
    
    
    @Override
    public byte[] readCertificate(final byte[][] recipients, final Certificate certificate, final Key key) {
        
        byte[] envelopedData=null;

        final String provider="BC";
        
        /*
         * loop through all and get data if match found
         */
        for (final byte[] recipient : recipients) {
            
            try {
                final CMSEnvelopedData recipientEnvelope = new CMSEnvelopedData(recipient);
                
                final Object[] recipientList = recipientEnvelope.getRecipientInfos().getRecipients().toArray();
                final int listCount = recipientList.length;
                
                for (int ii = 0; ii < listCount; ii++) {
                    final RecipientInformation recipientInfo = (RecipientInformation) recipientList[ii];
                    
                    if (recipientInfo.getRID().match(certificate)) {
                        envelopedData = recipientInfo.getContent(key, provider);
                        ii = listCount;
                    }
                }
            } catch (final Exception e) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
        }
        
        return envelopedData;
    }
}

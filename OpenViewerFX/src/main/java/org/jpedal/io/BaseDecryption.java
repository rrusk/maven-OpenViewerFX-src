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
 * BaseDecryption.java
 * ---------------
 */
package org.jpedal.io;

import java.security.Key;
import java.security.cert.Certificate;
import org.jpedal.exception.PdfSecurityException;

public interface BaseDecryption {
 
    byte[] v5Decrypt(final byte[] rawValue, final byte[] key) throws PdfSecurityException; 
    
    byte[] decodeAES(final byte[] encKey, final byte[] encData, final byte[] ivData) throws Exception;
    
    byte[] readCertificate(final byte[][] recipients, final Certificate certificate, final Key key);
}

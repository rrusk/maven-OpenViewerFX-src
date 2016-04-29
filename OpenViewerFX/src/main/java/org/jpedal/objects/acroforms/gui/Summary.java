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
 * Summary.java
 * ---------------
 */

package org.jpedal.objects.acroforms.gui;

import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JDialog;
import org.jpedal.objects.acroforms.gui.certificates.CertificateHolder;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;

public class Summary extends javax.swing.JPanel {
    private final JDialog frame;
    private final PdfObject sigObject;

    public void setValues(final String signName, final String reason, final String location) {
        signedByBox.setText(signName);
        reasonBox.setText(reason);

        final String rawDate=sigObject.getTextStreamValue(PdfDictionary.M);
        //if(rawDate!=null){
            final StringBuilder date = new StringBuilder(rawDate);
            date.delete(0, 2);
            date.insert(4, '/');
            date.insert(7, '/');
            date.insert(10, ' ');
            date.insert(13, ':');
            date.insert(16, ':');
            date.insert(19, ' ');
            dateBox.setText(date.toString());
        //}

        locationBox.setText(location);
    }

    /**
     * Creates new form Signatures
     *
     * @param frame
     * @param sig
     */
    public Summary(final JDialog frame, final PdfObject sig) {
        this.frame = frame;
        this.sigObject = sig;
        initComponents();
        /*
        * Disabling show certificate button if there is no Certificate there
        */
        final byte[] bytes = sigObject.getTextStreamValueAsByte(PdfDictionary.Cert);
        if (bytes == null) {
            showCertificateButton.setEnabled(false);
        } else {
        showCertificateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                showCertificate();
            }
        });
        add(showCertificateButton);
        showCertificateButton.setBounds(380, 10, 150, 23);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        locationBox = new javax.swing.JTextField();
        showCertificateButton = new javax.swing.JButton();
        signedByBox = new javax.swing.JTextField();
        reasonBox = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        dateBox = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setLayout(null);

        jLabel1.setText("Location:");
        add(jLabel1);
        jLabel1.setBounds(310, 70, 70, 20);

        jLabel2.setText("Signed by:");
        add(jLabel2);
        jLabel2.setBounds(10, 10, 70, 20);

        jLabel3.setText("Reason:");
        add(jLabel3);
        jLabel3.setBounds(10, 40, 70, 20);

        locationBox.setEditable(false);
        add(locationBox);
        locationBox.setBounds(360, 70, 170, 20);

        showCertificateButton.setText("Show Certificate...");
        showCertificateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                showCertificate();
            }
        });
        add(showCertificateButton);
        showCertificateButton.setBounds(380, 10, 150, 23);

        signedByBox.setEditable(false);
        add(signedByBox);
        signedByBox.setBounds(70, 10, 300, 20);

        reasonBox.setEditable(false);
        add(reasonBox);
        reasonBox.setBounds(70, 40, 460, 20);

        jLabel4.setText("Date:");
        add(jLabel4);
        jLabel4.setBounds(10, 70, 70, 20);

        dateBox.setEditable(false);
        add(dateBox);
        dateBox.setBounds(70, 70, 230, 20);

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                close();
            }
        });
        add(jButton1);
        jButton1.setBounds(433, 140, 90, 23);
    }// </editor-fold>//GEN-END:initComponents

    private void close() {//GEN-FIRST:event_close
        frame.setVisible(false);
    }//GEN-LAST:event_close

    private void showCertificate() {//GEN-FIRST:event_showCertificate
        final JDialog frame = new JDialog((Frame) null, "Certificate Viewer", true);

        final CertificateHolder ch = new CertificateHolder(frame);
        

        try {

        	//System.out.println("sigObject = "+sigObject+" "+sigObject.getObjectRefAsString());
        	
        	final byte[] bytes = sigObject.getTextStreamValueAsByte(PdfDictionary.Cert);
        	
//        	byte[] contents=sigObject.getTextStreamValueAsByte(PdfDictionary.Contents);
//        	System.out.println(contents+" << "+sigObject);
        	
			//byte[] bytes = null;//(byte[]) dictionary.DecodedStream;
        	//PdfObject dictionary = sigObject.getDictionary(PdfDictionary.M);
        	
        	//System.out.println("dictionary = "+dictionary);
        	
			//byte[] bytes = (byte[]) dictionary.DecodedStream;
        	
        	//String textStreamValue = sigObject.getTextStreamValue(PdfDictionary.Cert);
			//byte[] bytes = StringUtils.toBytes(textStreamValue);
            final InputStream bais = new ByteArrayInputStream(bytes);
            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            final X509Certificate signingCertificate = (X509Certificate) cf.generateCertificate(bais);
            bais.close();

//			// @simon this is the "public key"
//			System.out.println("public key ============");
//			for(int i=0;i<signingCertificate.getPublicKey().getEncoded().length;i++){
//				System.out.println(Integer.toHexString(signingCertificate.getPublicKey().getEncoded()[i]));
//			}
//
////			 @simon this is the "SHA1 Digest of Public key"
////			System.out.println("SHA1 Digest of Public key ===========");
////			MessageDigest sha1PublicKey = MessageDigest.getInstance("SHA-1");
////			sha1PublicKey.update(cert.getPublicKey().getEncoded());
////			byte[] sha1PublicKeyRes = sha1PublicKey.digest();
////			for(int i=0;i<sha1PublicKeyRes.length;i++){
////				System.out.println(Integer.toHexString(sha1PublicKeyRes[i]));
////			}
//			
////			 @simon this is the "SHA1 Digest"
//			System.out.println("SHA1 Digest ===========");
//			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
//			sha1.update(bytes);
//			byte[] sha1Res = sha1.digest();
//			for(int i=0;i<sha1Res.length;i++){
//				System.out.println(Integer.toHexString(sha1Res[i]));
//			}
//			
////			 @simon this is the "MD5 Digest"
//			System.out.println("MD5 Digest ===========");
//			MessageDigest md5 = MessageDigest.getInstance("MD5");
//			md5.update(bytes);
//			byte[] res = md5.digest();
//			for(int i=0;i<res.length;i++){
//				System.out.println(Integer.toHexString(res[i]));
//			}
//			
////			 @simon this is the "x data"
//			System.out.println("x data ============");
//			for(int i=0;i<signingCertificate.getEncoded().length;i++){
//				System.out.println(Integer.toHexString(signingCertificate.getEncoded()[i]));
//			}

//			System.out.println(signingCertificate);
//			System.out.println(signingCertificate.getSigAlgName());
//			System.out.println(signingCertificate.getSigAlgOID());
//			System.out.println(signingCertificate.getType());
//			System.out.println(signingCertificate.getVersion());
//			System.out.println(signingCertificate.getCriticalExtensionOIDs());
//			System.out.println(signingCertificate.getExtendedKeyUsage());
//			System.out.println(signingCertificate.getIssuerAlternativeNames());
//			System.out.println(signingCertificate.getNonCriticalExtensionOIDs());
//			System.out.println(signingCertificate.getSubjectAlternativeNames());
//			System.out.println(signingCertificate.getPublicKey().getAlgorithm());
//			System.out.println(signingCertificate.getPublicKey().getFormat());

            final DateFormat format1 = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

            final Date notBefore = signingCertificate.getNotBefore();
            final Date notAfter = signingCertificate.getNotAfter();

            final String publicKey = byteToHex(signingCertificate.getPublicKey().getEncoded());
            final String x509Data = byteToHex(signingCertificate.getEncoded());
            final String sha1Digest = byteToHex(getDigest(bytes, "SHA1"));
            final String md5Digest = byteToHex(getDigest(bytes, "MD5"));

            String keyDescription = signingCertificate.getPublicKey().toString();
            final int keyDescriptionEnd = keyDescription.indexOf('\n');
            if (keyDescriptionEnd != -1) {
                keyDescription = keyDescription.substring(0, keyDescriptionEnd);
            }

            ch.setValues(sigObject.getTextStreamValue(PdfDictionary.Name), signingCertificate.getVersion(), signingCertificate.getSigAlgName(),
                    signingCertificate.getSubjectX500Principal().toString(),
                    signingCertificate.getIssuerX500Principal().toString(),
                    signingCertificate.getSerialNumber(),
                    format1.format(notBefore),
                    format1.format(notAfter),
                    keyDescription, publicKey, x509Data, sha1Digest, md5Digest);

            frame.getContentPane().add(ch);
            frame.setSize(440, 450);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
    }//GEN-LAST:event_showCertificate

    /**
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static byte[] getDigest(final byte[] bytes, final String algorithm) throws NoSuchAlgorithmException {
        final MessageDigest sha1 = MessageDigest.getInstance(algorithm);
        sha1.update(bytes);
        return sha1.digest();
    }

    /**
     * @return
     */
    private static String byteToHex(final byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (final byte aByte : bytes) {
            String singleByte = Integer.toHexString(aByte);
            if (singleByte.startsWith("ffffff")) {
                singleByte = singleByte.substring(6, singleByte.length());
            } else if (singleByte.length() == 1) {
                singleByte = '0' + singleByte;
            }

            singleByte = singleByte.toUpperCase();
            hex.append(singleByte).append(' ');
        }
        return hex.toString();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField dateBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField locationBox;
    private javax.swing.JTextField reasonBox;
    private javax.swing.JButton showCertificateButton;
    private javax.swing.JTextField signedByBox;
    // End of variables declaration//GEN-END:variables


}

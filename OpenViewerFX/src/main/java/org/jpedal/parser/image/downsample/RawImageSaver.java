/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jpedal.parser.image.downsample;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.io.ObjectStore;
import org.jpedal.parser.image.data.ImageData;

/**
 *
 * @author markee
 */
public class RawImageSaver {
    
    public static void saveRawDataForResampling(final ObjectStore objectStoreStreamRef,
        final ImageData imageData, GenericColorSpace decodeColorData, final byte[] maskCol, final String key) {

        final byte[] data=imageData.getObjectData();

        final int count=data.length;

        final byte[] convertedData=new byte[count];
        
        System.arraycopy(data,0,convertedData,0,count);

        decodeColorData.dataToRGBByteArray(convertedData, imageData.getWidth(), imageData.getHeight(), imageData.isArrayInverted());

        objectStoreStreamRef.saveRawImageData(key,convertedData,imageData.getWidth(),imageData.getHeight(),imageData.getDepth(), imageData.getpX(), imageData.getpY(),maskCol,ColorSpaces.DeviceRGB);
        
    }
    
}

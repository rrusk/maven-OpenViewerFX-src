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
 * DownSampler.java
 * ---------------
 */
package org.jpedal.parser.image.downsample;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.parser.image.data.ImageData;

/**
 *
 * @author markee
 */
public class DownSampler {
    
    
    public static GenericColorSpace downSampleImage(GenericColorSpace decodeColorData,
                                                    final ImageData imageData, final byte[] maskCol, final int sampling) {
        
        if(sampling>1){ //safety check
            
            imageData.setIsDownsampled(true);

            byte[] index=decodeColorData.getIndexedMap();

            if(imageData.getDepth()==1 && (decodeColorData.getID()!=ColorSpaces.DeviceRGB || index==null)){

                //make 1 bit indexed flat

                if(index!=null) {
                    index = decodeColorData.convertIndexToRGB(index);
                    decodeColorData.setIndex(index, index.length/3);
                    decodeColorData=OneBitDownSampler.downSampleIndexed(sampling, imageData, index, decodeColorData);

                }else if(maskCol!=null){
                    decodeColorData=OneBitDownSampler.downSampleMask(sampling, imageData, maskCol, decodeColorData);
                }else{
                    decodeColorData=OneBitDownSampler.downSample(sampling, imageData, decodeColorData);
                }
            }else if(imageData.getDepth()==8){
                decodeColorData=EightBitDownSampler.downSample(imageData, decodeColorData, sampling);
            }

        }
        return decodeColorData;
    }
}

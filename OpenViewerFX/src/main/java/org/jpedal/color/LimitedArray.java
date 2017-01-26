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
 * LimitedArray.java
 * ---------------
 */
package org.jpedal.color;

public class LimitedArray {

    private final long[] list = new long[512];
    private int iter;
    private long lk = Long.MAX_VALUE;
    private long lv = Long.MAX_VALUE;

    public LimitedArray() {
        for (int i = 0; i < 256; i++) {
            list[i << 1] = Long.MAX_VALUE;
        }
    }

    public Long get(long k) {
        if (lk == k) {
            return lv;
        }
        for (int i = 0; i < 256; i++) {
            if (list[i << 1] == k) {
                return list[(i << 1) + 1];
            }
        }
        return null;
    }

    public void put(long k, long v) {
        iter &= 0xff;
        list[iter << 1] = k;
        list[(iter << 1) + 1] = v;
        iter++;
        lk = k;
        lv = v;
    }
}

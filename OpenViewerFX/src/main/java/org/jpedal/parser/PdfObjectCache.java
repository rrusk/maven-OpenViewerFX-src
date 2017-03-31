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
 * PdfObjectCache.java
 * ---------------
 */
package org.jpedal.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jpedal.exception.PdfException;
import org.jpedal.fonts.PdfFont;
import org.jpedal.io.ObjectDecoder;
import org.jpedal.io.PdfFileReader;
import org.jpedal.objects.raw.ObjectFactory;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfKeyPairsIterator;
import org.jpedal.objects.raw.PdfObject;

/**
 * caches for data
 */
public class PdfObjectCache {

    public static final int ColorspacesUsed = 1;
    public static final int Colorspaces = 2;
    public static final int ColorspacesObjects = 3;
    public static final int GlobalShadings = 4;
    public static final int LocalShadings = 5;

    //init size of maps
    private static final int initSize = 50;

    //int values for all colorspaces
    private final Map<java.io.Serializable, Object> colorspacesUsed = new HashMap<java.io.Serializable, Object>(initSize);

    /**
     * colors
     */
    private Map<Object, byte[]> colorspaces = getColorSpaceWithDefaultValues();

    private Map<Object, byte[]> globalXObjects = new HashMap<Object, byte[]>(initSize);
    private Map<Object, byte[]> localXObjects = new HashMap<Object, byte[]>(initSize);

    private final Map<String, byte[]> patterns = new HashMap<String, byte[]>(initSize);
    private final Map<String, byte[]> globalShadings = new HashMap<String, byte[]>(initSize);
    private Map<String, byte[]> localShadings = new HashMap<String, byte[]>(initSize);

    final Map<String, Integer> imposedImages = new HashMap<String, Integer>(initSize);

    public PdfObject groupObj;

    /**
     * fonts
     */
    public Map<Object, byte[]> unresolvedFonts = new HashMap<Object, byte[]>(initSize);
    public Map<String, PdfObject> directFonts = new HashMap<String, PdfObject>(initSize);
    public Map<String, PdfFont> resolvedFonts = new HashMap<String, PdfFont>(initSize);

    /**
     * GS
     */
    Map<Object, byte[]> GraphicsStates = new HashMap<Object, byte[]>(initSize);

    public PdfObjectCache copy() {

        final PdfObjectCache copy = new PdfObjectCache();

        copy.localShadings = localShadings;
        copy.unresolvedFonts = unresolvedFonts;
        copy.GraphicsStates = GraphicsStates;
        copy.directFonts = directFonts;
        copy.resolvedFonts = resolvedFonts;
        copy.colorspaces = colorspaces;

        copy.localXObjects = localXObjects;
        copy.globalXObjects = globalXObjects;

        copy.groupObj = groupObj;


        return copy;

    }

    public PdfObjectCache() {
    }

    public void put(final int type, final int key, final Object value) {
        switch (type) {
            case ColorspacesUsed:
                colorspacesUsed.put(key, value);
                break;
        }
    }

    public void put(final int type, final String key, final Object value) {
        switch (type) {
            case ColorspacesUsed:
                colorspacesUsed.put(key, value);
                break;
        }
    }

    public Iterator<java.io.Serializable> iterator(final int type) {

        Iterator<java.io.Serializable> returnValue = null;

        switch (type) {
            case ColorspacesUsed:
                returnValue = colorspacesUsed.keySet().iterator();
                break;
        }

        return returnValue;
    }

    public Object get(final int key, final Object value) {

        Object returnValue = null;

        switch (key) {
            case ColorspacesUsed:
                returnValue = colorspacesUsed.get(value);
                break;
            case Colorspaces:
                returnValue = colorspaces.get(value);
                break;

            case GlobalShadings:
                returnValue = globalShadings.get(value);
                break;
            case LocalShadings:
                returnValue = localShadings.get(value);
                break;

        }

        return returnValue;
    }

    public void resetFonts() {
        resolvedFonts.clear();
        unresolvedFonts.clear();
        directFonts.clear();
    }

    public byte[] getXObjects(final String localName) {

        byte[] XObject = localXObjects.get(localName);
        if (XObject == null) {
            XObject = globalXObjects.get(localName);
        }

        return XObject;
    }

    public void readResources(final PdfObject Resources, final boolean resetList, final PdfFileReader objectReader) throws PdfException {

        final int[] keys = {PdfDictionary.ColorSpace, PdfDictionary.ExtGState, PdfDictionary.Font,
                PdfDictionary.Pattern, PdfDictionary.Shading, PdfDictionary.XObject};

        PdfObject resObj;

        final int length = keys.length;

        final boolean verifyResourcesAvaiable = Resources.isDataExternal();

        for (int ii = 0; ii < length; ii++) {

            resObj = Resources.getDictionary(keys[ii]);

            if (resObj != null) {
                if (keys[ii] == PdfDictionary.Font || keys[ii] == PdfDictionary.XObject) {
                    readArrayPairs(resObj, resetList, keys[ii], objectReader, verifyResourcesAvaiable);
                } else {
                    readArrayPairs(resObj, false, keys[ii], objectReader, verifyResourcesAvaiable);
                }

                if (verifyResourcesAvaiable && !resObj.isFullyResolved()) {
                    Resources.setFullyResolved(false);
                    ii = length;
                }
            }
        }
    }

    private void readArrayPairs(final PdfObject resObj, final boolean resetFontList, final int type, final PdfFileReader objectReader, final boolean verifyResourcesAvaiable) {

        String id;
        byte[] data;
        final PdfKeyPairsIterator keyPairs = resObj.getKeyPairsIterator();

        while (keyPairs.hasMorePairs()) {

            id = keyPairs.getNextKeyAsString();
            data = keyPairs.getNextValueAsBytes();

            //check we can fully load object if in Linearized mode and return on first failure
            if (verifyResourcesAvaiable &&
                    cannotFullyLoadObjectAndChildren(id, data, type, objectReader, resObj)) {
                return;
            }

            switch (type) {

                case PdfDictionary.ColorSpace:
                    colorspaces.put(id, data);
                    break;

                case PdfDictionary.ExtGState:
                    GraphicsStates.put(id, data);
                    break;

                case PdfDictionary.Font:
                    unresolvedFonts.put(id, data);
                    break;

                case PdfDictionary.Pattern:
                    patterns.put(id, data);
                    break;

                case PdfDictionary.Shading:
                    if (resetFontList) {
                        globalShadings.put(id, data);
                    } else {
                        localShadings.put(id, data);
                    }
                    break;

                case PdfDictionary.XObject:
                    if (resetFontList) {
                        globalXObjects.put(id, data);
                    } else {
                        localXObjects.put(id, data);
                    }
                    break;
            }
            keyPairs.nextPair();
        }
    }

    private boolean cannotFullyLoadObjectAndChildren(final String id, final byte[] data, final int type, final PdfFileReader objectReader, final PdfObject resObj) {

        //System.out.println(id+" "+" "+new String(data));

        final PdfObject pdfObject = ObjectFactory.createObject(type, 0, 0, type);
        pdfObject.setStatus(PdfObject.UNDECODED_DIRECT);
        pdfObject.setUnresolvedData(data, type);
        pdfObject.isDataExternal(true);

        if (!ObjectDecoder.resolveFully(pdfObject, objectReader)) {
            resObj.setFullyResolved(false);

            // System.out.println("failed");

            return true;
        }
        return false;
    }

    public void reset(final PdfObjectCache newCache) {

        //reset copies
        localShadings = new HashMap<String, byte[]>(initSize);
        resolvedFonts = new HashMap<String, PdfFont>(initSize);
        unresolvedFonts = new HashMap<Object, byte[]>(initSize);
        directFonts = new HashMap<String, PdfObject>(initSize);
        colorspaces = getColorSpaceWithDefaultValues();
        GraphicsStates = new HashMap<Object, byte[]>(initSize);
        localXObjects = new HashMap<Object, byte[]>(initSize);

        Iterator<Object> keys = newCache.GraphicsStates.keySet().iterator();
        while (keys.hasNext()) {
            final Object key = keys.next();
            GraphicsStates.put(key, newCache.GraphicsStates.get(key));
        }

        keys = newCache.colorspaces.keySet().iterator();
        while (keys.hasNext()) {
            final Object key = keys.next();
            colorspaces.put(key, newCache.colorspaces.get(key));
        }


        keys = newCache.localXObjects.keySet().iterator();
        while (keys.hasNext()) {
            final Object key = keys.next();
            localXObjects.put(key, newCache.localXObjects.get(key));
        }

        keys = newCache.globalXObjects.keySet().iterator();
        while (keys.hasNext()) {
            final Object key = keys.next();
            globalXObjects.put(key, newCache.globalXObjects.get(key));
        }

        //allow for no fonts in FormObject when we use any global
        if (unresolvedFonts.isEmpty()) {
            //unresolvedFonts=rawFonts;
            keys = newCache.unresolvedFonts.keySet().iterator();
            while (keys.hasNext()) {
                final Object key = keys.next();
                unresolvedFonts.put(key, newCache.unresolvedFonts.get(key));
            }
        }
    }

    public void restore(final PdfObjectCache mainCache) {

        directFonts = mainCache.directFonts;
        unresolvedFonts = mainCache.unresolvedFonts;
        resolvedFonts = mainCache.resolvedFonts;
        GraphicsStates = mainCache.GraphicsStates;
        colorspaces = mainCache.colorspaces;
        localShadings = mainCache.localShadings;
        localXObjects = mainCache.localXObjects;
        globalXObjects = mainCache.globalXObjects;

        groupObj = mainCache.groupObj;

    }

    public void setImposedKey(final String key, final int id) {
        if (imposedImages != null) {
            imposedImages.put(key, id);
        }
    }

    public Map getPatterns() {
        return patterns;
    }

    /**/
    private static Map<Object, byte[]> getColorSpaceWithDefaultValues() {

        final Map colorValues = new HashMap<Object, byte[]>(initSize);

        final String[] keys = {"DeviceRGB", "Pattern", "Separation", "CalRGB", "CalGray", "ICC",
                "DeviceGray", "DeviceN", "Lab", "DeviceCMYK"};
        final String[] values = {"/DeviceRGB", "/Pattern", "/Separation", "/CalRGB", "/CalGray", "/ICC",
                "/DeviceGray", "/DeviceN", "/Lab", "/DeviceCMYK"};

        final int keyCount = keys.length;
        for (int i = 0; i < keyCount; i++) {
            colorValues.put(keys[i], values[i].getBytes());
        }
        return colorValues;
    } /**/
}

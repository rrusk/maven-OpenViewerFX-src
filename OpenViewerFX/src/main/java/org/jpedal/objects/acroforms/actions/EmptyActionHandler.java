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
 * EmptyActionHandler.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.jpedal.PdfDecoderInt;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.Javascript;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.acroforms.ReturnValues;
import org.jpedal.objects.acroforms.actions.privateclasses.FieldsHideObject;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.LogWriter;

public class EmptyActionHandler implements ActionHandler {

    protected static final boolean showMethods = false;

    protected PdfObjectReader currentPdfFile;

    protected Javascript javascript;

    protected AcroRenderer acrorend;

    //handle so we can access
    protected PdfDecoderInt decode_pdf;

    @Override
    public void init(final PdfDecoderInt decode_pdf, final Javascript javascript, final AcroRenderer acrorend) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.init()");
        }

        if (decode_pdf != null) {
            currentPdfFile = decode_pdf.getIO();
        }

        this.javascript = javascript;
        this.acrorend = acrorend;
        this.decode_pdf = decode_pdf;

    }

    @Override
    public Object setHoverCursor() {

        return new MouseListener() {
            @Override
            public void mouseEntered(final MouseEvent e) {

            }

            @Override
            public void mouseExited(final MouseEvent e) {

            }

            @Override
            public void mouseClicked(final MouseEvent e) {
            }

            @Override
            public void mousePressed(final MouseEvent e) {
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
            }
        };
    }

    /**
     * A action when pressed in active area ?some others should now be ignored?
     */
    @Override
    public void A(final Object raw, FormObject formObj, final int eventType) {

        if (showMethods) {
            System.out.println("DefaultActionHandler.A() ");
        }

        switch (eventType) {
            case MOUSEENTERED:
                javascript.execute(formObj, PdfDictionary.E, ActionHandler.TODO, ' ');
                break;
            case MOUSEEXITED:
                javascript.execute(formObj, PdfDictionary.X, ActionHandler.TODO, ' ');
                break;
            case MOUSEPRESSED:
                javascript.execute(formObj, PdfDictionary.D, ActionHandler.TODO, ' ');
                break;
            case MOUSERELEASED:
                javascript.execute(formObj, PdfDictionary.A, ActionHandler.TODO, ' ');
                javascript.execute(formObj, PdfDictionary.U, ActionHandler.TODO, ' ');
                break;
            default:
                break;
        }

        // new version
        PdfObject aData = null;
        if (eventType == MOUSERELEASED) {
            //get the A action if we have activated the form (released)
            aData = formObj.getDictionary(PdfDictionary.A);
        }

        if (aData == null) {
            aData = formObj.getDictionary(PdfDictionary.AA);
            if (aData != null) {
                switch (eventType) {
                    case MOUSEENTERED:
                        aData = aData.getDictionary(PdfDictionary.E);
                        break;
                    case MOUSEEXITED:
                        aData = aData.getDictionary(PdfDictionary.X);
                        break;
                    case MOUSEPRESSED:
                        aData = aData.getDictionary(PdfDictionary.D);
                        break;
                    case MOUSERELEASED:
                        aData = aData.getDictionary(PdfDictionary.U);
                        break;
                    default:
                        break;
                }
            }
        }

        gotoDest(formObj, eventType, PdfDictionary.Dest);

        final int subtype = formObj.getParameterConstant(PdfDictionary.Subtype);
        final int formType = formObj.getNameAsConstant(PdfDictionary.FT);

        final int popupFlag = formObj.getActionFlag();

        if (formType == PdfDictionary.Sig) {

            // additionalAction_Signature(formObj, eventType);

        } else if (eventType == MOUSECLICKED && (popupFlag == FormObject.POPUP || subtype == PdfDictionary.Text)) {
            // If the form object has an IRT entry and is part of a group, the popup that get's shown 
            // is the one associated with the IRT object
            if (formObj.getDictionary(PdfDictionary.IRT) != null && formObj.getNameAsConstant(PdfDictionary.RT) == PdfDictionary.Group) {
                final FormObject IRT = (FormObject) formObj.getDictionary(PdfDictionary.IRT);
                currentPdfFile.checkResolved(IRT);
                formObj = IRT;
            }
            popup(raw, formObj, currentPdfFile);
        } else {
            // can get empty values
            if (aData == null) {
                return;
            }

            final int command = aData.getNameAsConstant(PdfDictionary.S);

            // S is Name of action
            switch (command) {
                case PdfDictionary.Named:
                    additionalAction_Named(aData);
                    break;

                case PdfDictionary.GoTo:
                    gotoDest(aData, eventType, command);
                    break;

                case PdfDictionary.GoToR:
                    gotoDest(aData, eventType, command);
                    break;

                case PdfDictionary.ResetForm:
                    additionalAction_ResetForm(aData);
                    break;

                case PdfDictionary.SubmitForm:
                    additionalAction_SubmitForm(aData);
                    break;

                case PdfDictionary.JavaScript:
                    //javascript called above.
                    break;

                case PdfDictionary.Hide:
                    additionalAction_Hide(aData);
                    break;

                case PdfDictionary.URI:
                    // additionalAction_URI(aData.getTextStreamValue(PdfDictionary.URI));
                    break;

                case PdfDictionary.Launch:

                    LogWriter.writeFormLog("{stream} launch activate action NOT IMPLEMENTED", FormStream.debugUnimplemented);

                    if (FormStream.debugUnimplemented) {
                        System.out.println("{internal only} launch activate action NOT IMPLEMENTED");
                    }
                    break;

                case PdfDictionary.SetOCGState:
                    additionalAction_OCState(eventType, aData);
                    break;

                case PdfDictionary.Sound:

                    if (eventType == MOUSECLICKED || eventType == MOUSERELEASED) {
                        additionalAction_Sound(aData);
                    }
                    break;

                case PdfDictionary.Unknown:
                    LogWriter.writeFormLog("{stream} Activate Action UNKNOWN command " + aData.getName(PdfDictionary.S) + ' ' + formObj.getObjectRefAsString(), FormStream.debugUnimplemented);
                    break;

                default:
                    LogWriter.writeFormLog("{stream} UNKNOWN Command " + aData.getName(PdfDictionary.S) + " Action", FormStream.debugUnimplemented);
            }
        }
    }

    private void additionalAction_Sound(final PdfObject aData) {
        final PdfObject soundObj = aData.getDictionary(PdfDictionary.Sound);

        // read now as lazy initialisation
        currentPdfFile.checkResolved(soundObj);

        try {

            int channels = soundObj.getInt(PdfDictionary.C);
            if (channels == -1) {
                channels = 1;
            }

            int bitsPerSample = soundObj.getInt(PdfDictionary.B);
            if (bitsPerSample == -1) {
                bitsPerSample = 8;
            }

            final float samplingRate = soundObj.getInt(PdfDictionary.R);

            int e = soundObj.getNameAsConstant(PdfDictionary.E);
            if (e == PdfDictionary.Unknown) {
                e = PdfDictionary.Unsigned;
            }

            SoundHandler.setAudioFormat(e, bitsPerSample, samplingRate, channels);
            SoundHandler.PlaySound(soundObj.getDecodedStream());

        } catch (final Exception e) {
            LogWriter.writeLog("Exception: " + e.getMessage());
        }
    }

    private void additionalAction_OCState(final int eventType, final PdfObject aData) {
        if (eventType == MOUSECLICKED) {

            final PdfArrayIterator state = aData.getMixedArray(PdfDictionary.State);

            if (state != null && state.getTokenCount() > 0) {

                final PdfLayerList layers = (PdfLayerList) decode_pdf.getJPedalObject(PdfDictionary.Layer);

                final int count = state.getTokenCount();

                final int action = state.getNextValueAsConstant(true);
                String ref;
                for (int jj = 1; jj < count; jj++) {
                    ref = state.getNextValueAsString(true);

                    final String layerName = layers.getNameFromRef(ref);

                    // toggle layer status when clicked
                    final Runnable updateAComponent = new Runnable() {
                        @Override
                        public void run() {

                            // update settings on display and in PdfDecoder
                            final boolean newState;
                            if (action == PdfDictionary.Toggle) {
                                newState = !layers.isVisible(layerName);
                            } else {
                                newState = action != PdfDictionary.OFF;
                            }

                            layers.setVisiblity(layerName, newState);

                            // decode again with new settings
                            try {
                                decode_pdf.decodePage(-1);
                            } catch (final Exception e) {
                                LogWriter.writeLog("Exception: " + e.getMessage());
                            }
                        }
                    };

                    SwingUtilities.invokeLater(updateAComponent);
                }
            }
        }
    }

    private void additionalAction_Named(final PdfObject aData) {
        final int name = aData.getNameAsConstant(PdfDictionary.N);

        if (name == PdfDictionary.Print) {
            // additionalAction_Print(eventType);
        } else if (name == PdfDictionary.SaveAs) {
            //additionalAction_SaveAs();
        } else if (name == PdfDictionary.NextPage) {
            changeTo(null, decode_pdf.getlastPageDecoded() + 1, null, null, true);
        } else if (name == PdfDictionary.PrevPage) {
            changeTo(null, decode_pdf.getlastPageDecoded() - 1, null, null, true);
        } else if (name == PdfDictionary.FirstPage) {
            changeTo(null, 1, null, null, true);
        } else if (name == PdfDictionary.GoBack) {

        } else if (name == PdfDictionary.LastPage) {
            changeTo(null, decode_pdf.getPageCount(), null, null, true);
        } else if (name == PdfDictionary.ZoomTo) {


        } else if (name == PdfDictionary.FullScreen) {

        } else if (name == PdfDictionary.AcroForm_FormsJSGuide) { //AcroForm:FormsJSGuide

        } else if (FormStream.debugUnimplemented) {
            System.out.println("{internal only} Named Action NOT IMPLEMENTED " + aData.getName(PdfDictionary.N) + ' ' + decode_pdf.getFileName());
        }
    }

    private void additionalAction_Hide(final PdfObject aData) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.additionalAction_Hide()");
        }

        final FieldsHideObject fieldsToHide = new FieldsHideObject();

        getHideMap(aData, fieldsToHide);

        setFieldVisibility(fieldsToHide);
    }

    private void additionalAction_SubmitForm(final PdfObject aData) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.additionalAction_SubmitForm()");
        }

        boolean newExcludeList = false;
        String newSubmitURL = null;
        String[] newListOfFields = null;

        //get the F dictionary
        final PdfObject dict = aData.getDictionary(PdfDictionary.F);
        //then get the submit URL to use
        if (dict != null) {
            newSubmitURL = dict.getTextStreamValue(PdfDictionary.F);
        }

        //get the fields we need to change
        PdfArrayIterator fieldList = aData.getMixedArray(PdfDictionary.Fields);
        if (fieldList != null) {
            if (fieldList.getTokenCount() < 1) {
                fieldList = null;
            }

            if (fieldList != null) {
                // code goes here
                final int fieldIndex = 0;
                newListOfFields = new String[fieldList.getTokenCount()];

                // go through list of fields and store so we can send
                String formObject;
                String tok, preName = null;
                final StringBuilder names = new StringBuilder();
                while (fieldList.hasMoreTokens()) {
                    formObject = fieldList.getNextValueAsString(true);

                    if (formObject.contains(".x")) {
                        preName = formObject.substring(formObject.indexOf('.') + 1,
                                formObject.indexOf(".x") + 1);
                    }
                    if (formObject.contains(" R")) {

                        final FormObject formObj = new FormObject(formObject);
                        currentPdfFile.readObject(formObj);


                        tok = formObj.getTextStreamValue(PdfDictionary.T);
                        if (preName != null) {
                            names.append(preName);
                        }
                        names.append(tok);
                        names.append(',');

                    }
                }

                newListOfFields[fieldIndex] = names.toString();
            } // end of code section
        } // END of Fields defining

        //if there was a list of fields read the corresponding Flags see pdf spec v1.6 p662
        if (newListOfFields != null) {
            // if list is null we ignore this flag anyway
            final int flags = aData.getInt(PdfDictionary.Flags);

            if ((flags & 1) == 1) {
                // fields is an exclude list
                newExcludeList = true;
            }
        } // END of if exclude list ( Flags )

        // send our values to the actioning method
        submitURL(newListOfFields, newExcludeList, newSubmitURL);
    }

    private void additionalAction_ResetForm(final PdfObject aData) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.additionalAction_ResetForm()");
        }
        final int flags = aData.getInt(PdfDictionary.Flags); // potential problem: returns -1 for unknown values also
        boolean shouldExclude = false;
        if ((flags & 1) == 1) {
            // fields is an exclude list
            shouldExclude = true;
        }

        final Object[] allFormObjects = acrorend.getFormComponents(null, ReturnValues.FORMOBJECTS_FROM_REF, -1);
//        System.out.println("allFormObjects = ");
//        for(int i = 0; i < allFormObjects.length; i ++) {
//            FormObject formObject = (FormObject) allFormObjects[i];
//            System.out.println(formObject.getTextStreamValue(PdfDictionary.T));
//        }

        final PdfArrayIterator fieldList = aData.getMixedArray(PdfDictionary.Fields);
        String[] fields = null;
        if (fieldList != null && fieldList.getTokenCount() > 0) {
            fields = new String[fieldList.getTokenCount()];
            int i = 0;
            while (fieldList.hasMoreTokens()) {
                final String fieldname = fieldList.getNextValueAsString(true);
                fields[i] = fieldname;
                i++;
            }
        }
//        System.out.println("should exclude: " + shouldExclude);
//        System.out.println("fields = ");
//        if(fields != null) {
//            for(int i = 0; i < fields.length; i ++) {
//                System.out.println(fields[i]);
//            }
//        }
        if (shouldExclude) {
            // The Fields entry refers to the fields to exclude from being reset
            if (fields != null && fields.length > 0) {
                for (final Object allFormObject : allFormObjects) {
                    final FormObject formObject = (FormObject) allFormObject;
                    boolean skipForm = false;
                    for (final String name : fields) {
                        if (name == null) {
                            continue;
                        }
                        // Check if the names are the same
                        if (formObject.getTextStreamValue(PdfDictionary.T).equals(name)) {
                            skipForm = true;
                            break;
                        }
                    }
                    if (!skipForm) {
                        // Form not present in fields array
                        final String defaultValue = formObject.getTextStreamValue(PdfDictionary.DV);
                        formObject.updateValue(defaultValue, false, true);
//                        if(formObject.hasKeyArray(PdfDictionary.Kids)) {
//                            byte[][] kidList = formObject.getKeyArray(PdfDictionary.Kids);
//                            for (final byte[] aKidList : kidList) { //iterate through all parts
//                                String key = new String(aKidList);
//                                Object[] formComps = acrorend.getFormComponents(key, ReturnValues.FORMOBJECTS_FROM_REF, -1);
//                                for(int y = 0; y < formComps.length; y ++) {
//                                    FormObject childObj = (FormObject) formComps[y];
//                                    String dv = childObj.getTextStreamValue(PdfDictionary.DV);
//                                    childObj.updateValue(dv, false, true);
//                                }
//
//                            }
//                        }
                    }
                }
            } else {
                // Reset all forms
                for (final Object allFormObject : allFormObjects) {
                    final FormObject formObject = (FormObject) allFormObject;
                    final String defaultValue = formObject.getTextStreamValue(PdfDictionary.DV);
                    formObject.updateValue(defaultValue, false, true);
//                    if(formObject.hasKeyArray(PdfDictionary.Kids)) {
//                        byte[][] kidList = formObject.getKeyArray(PdfDictionary.Kids);
//                        for (final byte[] aKidList : kidList) { //iterate through all parts
//                            String key = new String(aKidList);
//                            Object[] formComps = acrorend.getFormComponents(key, ReturnValues.FORMOBJECTS_FROM_REF, -1);
//                            for(int y = 0; y < formComps.length; y ++) {
//                                FormObject childObj = (FormObject) formComps[y];
//                                String dv = childObj.getTextStreamValue(PdfDictionary.DV);
//                                childObj.updateValue(dv, false, true);
//                            }
//
//                        }
//                    }
                }
            }
        } else if (fields != null && fields.length > 0) {
            // The Fields entry refers to the fields we want to reset

            // Iterate over all the forms and add those with the same names as in the fields array
            // Use an array list as fields can potentially use the same name
            final ArrayList<FormObject> formObjects = new ArrayList<FormObject>();
            for (final Object allFormObject : allFormObjects) {
                final FormObject formObject = (FormObject) allFormObject;
                for (final String name : fields) {
                    if (name == null) {
                        continue;
                    }
                    // Check if the names are the same
                    if (formObject.getTextStreamValue(PdfDictionary.T).equals(name)) {
                        formObjects.add(formObject);
                    }
                }
            }
            for (final FormObject formObject : formObjects) {
                final String defaultValue = formObject.getTextStreamValue(PdfDictionary.DV);
                formObject.updateValue(defaultValue, false, true);
//                if(formObject.hasKeyArray(PdfDictionary.Kids)) {
//                    byte[][] kidList = formObject.getKeyArray(PdfDictionary.Kids);
//                    for (final byte[] aKidList : kidList) { //iterate through all parts
//                        String key = new String(aKidList);
//                        Object[] formComps = acrorend.getFormComponents(key, ReturnValues.FORMOBJECTS_FROM_REF, -1);
//                        for(int y = 0; y < formComps.length; y ++) {
//                            FormObject childObj = (FormObject) formComps[y];
//                            String dv = childObj.getTextStreamValue(PdfDictionary.DV);
//                            childObj.updateValue(dv, false, true);
//                        }
//
//                    }
//                }
            }
        } else {
            // Reset all forms
            for (final Object allFormObject : allFormObjects) {
                final FormObject formObject = (FormObject) allFormObject;
                final String defaultValue = formObject.getTextStreamValue(PdfDictionary.DV);
                formObject.updateValue(defaultValue, false, true);
//                if(formObject.hasKeyArray(PdfDictionary.Kids)) {
//                    byte[][] kidList = formObject.getKeyArray(PdfDictionary.Kids);
//                    for (final byte[] aKidList : kidList) { //iterate through all parts
//                        String key = new String(aKidList);
//                        Object[] formComps = acrorend.getFormComponents(key, ReturnValues.FORMOBJECTS_FROM_REF, -1);
//                        for(int y = 0; y < formComps.length; y ++) {
//                            FormObject childObj = (FormObject) formComps[y];
//                            String dv = childObj.getTextStreamValue(PdfDictionary.DV);
//                            childObj.updateValue(dv, false, true);
//                        }
//
//                    }
//                }
            }
        }
    }

    /**
     * public as also called from Viewer to reset
     * <p>
     * new page or -1 returned
     *
     * @param aData
     * @param eventType
     * @param command
     * @return
     */
    @Override
    public int gotoDest(final PdfObject aData, final int eventType, int command) {

        final boolean debugDest = false;

        //new page or -1 returned
        int page = -1;

        if (showMethods) {
            System.out.println("DefaultActionHandler.gotoDest()");
        }

        final PdfArrayIterator Dest = DestHandler.getDestFromObject(aData, currentPdfFile);
        if (Dest != null) {

            if (eventType == MOUSECLICKED) {

                String filename = aData.getTextStreamValue(PdfDictionary.F);

                if (filename == null) {
                    final PdfObject fDic = aData.getDictionary(PdfDictionary.F);

                    if (fDic != null) {
                        filename = fDic.getTextStreamValue(PdfDictionary.F);
                    }
                }

                //add path if none present
                if (filename != null && filename.indexOf('/') == -1 && filename.indexOf('\\') == -1) {
                    filename = decode_pdf.getObjectStore().getCurrentFilepath() + filename;
                }

                //removed \\ checking from iff so slashIndex will work, and
                //stop null pointer exceptions, \\ will also be quicker.
                if (filename != null) {

                    //if we have any \\ then replace with / for Windows
                    int index = filename.indexOf('\\');
                    while (index != -1) {
                        //for some reason String.replaceAll didnt like "\\" so done custom
                        filename = filename.substring(0, index) +
                                '/' + filename.substring(index + ("\\".length()), filename.length());
                        index = filename.indexOf('\\');
                    }

                    //if we dont start with a /,./ or ../ or #:/ then add ./
                    final int slashIndex = filename.indexOf(":/");
                    if ((slashIndex == -1 || slashIndex > 1) && !filename.startsWith("/")) {
                        final File fileStart = new File(decode_pdf.getFileName());
                        filename = fileStart.getParent() + '/' + filename;
                    }

                    //resolve any ../ by removing
                    //(ie /home/test/Downloads/hyperlinks2/Data/../Start.pdf to
                    // /home/test/Downloads/hyperlinks2/Start.pdf)
                    index = filename.indexOf("/../");
                    if (index != -1) {
                        int start = index - 1;
                        while (start > 0) {
                            if ((filename.charAt(start) == '/') || start == 0) {
                                break;
                            }
                            start--;
                        }

                        if (start > 0) {
                            filename = filename.substring(0, start) + filename.substring(index + 3, filename.length());
                        }

                    }
                }

                // new version - read Page Object to jump to
                String pageRef = "";

                if (Dest.getTokenCount() > 0) {

                    //This will catch if the destination page is not set and continue without making changes
                    if (Dest.isNextValueNull()) {
                        return -1;
                    }

                    //get pageRef as number of ref
                    final int possiblePage = Dest.getNextValueAsInteger(false) + 1;
                    pageRef = Dest.getNextValueAsString(true);

                    //convert to target page if ref or ignore

                    if (pageRef.endsWith(" R")) {
                        page = decode_pdf.getPageFromObjectRef(pageRef);
                    } else if (possiblePage > 0) { //can also be a number (cant check range as not yet open)
                        page = possiblePage;
                    }

                    if (debugDest) {
                        System.out.println("pageRef=" + pageRef + " page=" + page + ' ' + aData.getObjectRefAsString());
                    }

                    //allow for named Dest
                    if (page == -1) {
                        final String newRef = currentPdfFile.convertNameToRef(pageRef);

                        //System.out.println(newRef+" "+decode_pdf.getIO().convertNameToRef(pageRef+"XX"));

                        if (newRef != null && newRef.endsWith(" R")) {
                            page = decode_pdf.getPageFromObjectRef(newRef);
                        }

                    }
                    //commented out by mark as named dest should now be handled and -1 shows no page
                    //if(page==-1){
                    //we probably have a named destination
                    //	page = 1;
                    //}
                }

                //added by Mark so we handle these types of links as well in code below with no Dest
                //<</Type/Annot/Subtype/Link/Border[0 0 0]/Rect[56 715.1 137.1 728.9]/A<</Type/Action/S/GoToR/F(test1.pdf)>>
                if (Dest.getTokenCount() == 0 && aData.getNameAsConstant(PdfDictionary.S) == PdfDictionary.GoToR) {
                    command = PdfDictionary.GoToR;
                }

                //				boolean openInNewWindow = aData.getBoolean(PdfDictionary.NewWindow);

                if (debugDest) {
                    System.out.println("Command=" + PdfDictionary.showAsConstant(command));
                }

                switch (command) {
                    case PdfDictionary.Dest:
                        //read all the values
                        if (Dest.getTokenCount() > 1) {

                            //get type of Dest
                            //System.out.println("Next value as String="+Dest.getNextValueAsString(false)); //debug code to show actual value (note false so does not roll on)
                            final int type = Dest.getNextValueAsConstant(true);

                            if (debugDest) {
                                System.out.println("Type=" + PdfDictionary.showAsConstant(type));
                            }

                            Integer scale = null;
                            Rectangle position = null;

                            // - I have added all the keys for you and
                            //changed code below. If you run this on baseline,
                            //with new debug flag testActions on in DefaultAcroRender
                            // it will exit when it hits one
                            //not coded

                            //type of Dest (see page 552 in 1.6Spec (Table 8.2) for full list)
                            switch (type) {
                                case PdfDictionary.XYZ: //get X,y values and convert to rectangle which we store for later

                                    //get x and y, (null will return 0)
                                    final float x = Dest.getNextValueAsFloat();
                                    final float y = Dest.getNextValueAsFloat();

                                    //third value is zoom which is not implemented yet

                                    //create Rectangle to scroll to
                                    position = new Rectangle((int) x, (int) y, 10, 10);

                                    break;
                                case PdfDictionary.Fit: //type sent in so that we scale to Fit.
                                    scale = -3; //0 for width in scaling box and -3 to show its an index
                                    break;

                                case PdfDictionary.FitB:
                                    /*[ page /FitB ] - (PDF 1.1) Display the page designated by page, with its contents
                                     * magnified just enough to fit its bounding box entirely within the window both
                                     * horizontally and vertically. If the required horizontal and vertical magnification
                                     * factors are different, use the smaller of the two, centering the bounding box
                                     * within the window in the other dimension.
                                     */
                                    //scale to same as Fit so use Fit.
                                    scale = -3; //0 for width in scaling box and -3 to show its an index

                                    break;

                                case PdfDictionary.FitH:
                                    /* [ page /FitH top ] - Display the page designated by page, with the vertical coordinate
                                     * top positioned at the top edge of the window and the contents of the page magnified
                                     * just enough to fit the entire width of the page within the window. A null value for
                                     * top specifies that the current value of that parameter is to be retained unchanged.
                                     */
                                    //scale to width
                                    scale = -1; //2 for width in scaling box and -3 to show its an index

                                    if (Dest.hasMoreTokens()) { //value optional
                                        //and then scroll to location
                                        final float top = Dest.getNextValueAsFloat();

                                        //create Rectangle to scroll to
                                        position = new Rectangle(10, (int) top, 10, 10);
                                    }

                                    break;

                                case PdfDictionary.FitR:
                                    /* [ page /FitR left bottom right top ] - Display the page designated by page, with its
                                     * contents magnified just enough to fit the rectangle specified by the coordinates left,
                                     * bottom, right, and topentirely within the window both horizontally and vertically.
                                     * If the required horizontal and vertical magnification factors are different, use
                                     * the smaller of the two, centering the rectangle within the window in the other
                                     * dimension. A null value for any of the parameters may result in unpredictable behavior.
                                     */


                                    //create Rectangle to scroll to
//                                    position=new Rectangle((int)fitR_left,(int)fitR_top,(int)(fitR_right-fitR_left),(int)(fitR_top-fitR_bottom));

                                    break;
                                    
                                    /* [ page /FitV left ] - Display the page designated by page, with the horizontal
                                     * coordinate left positioned at the left edge of the window and the contents of
                                     * the page magnified just enough to fit the entire height of the page within the window.
                                     * A null value for left specifies that the current value of that parameter is to be
                                     * retained unchanged.
                                     */
                                    
                                    /* [ page /FitB ] - (PDF 1.1) Display the page designated by page, with its contents
                                     * magnified just enough to fit its bounding box entirely within the window both
                                     * horizontally and vertically. If the required horizontal and vertical magnification
                                     * factors are different, use the smaller of the two, centering the bounding box within
                                     * the window in the other dimension.
                                     */
                                    
                                    /* [ page /FitBH top ] - (PDF 1.1) Display the page designated by page, with the vertical
                                     * coordinate top positioned at the top edge of the window and the contents of the page
                                     * magnified just enough to fit the entire width of its bounding box within the window.
                                     * A null value for top specifies that the current value of that parameter is to be retained
                                     * unchanged.
                                     */
                                    /* [ page /FitBV left ] - (PDF 1.1) Display the page designated by page, with the horizontal
                                     * coordinate left positioned at the left edge of the window and the contents of the page
                                     * magnified just enough to fit the entire height of its bounding box within the window.
                                     * A null value for left specifies
                                     */

                            }

                            changeTo(filename, page, position, scale, true);
                        }
                        break;

                    case PdfDictionary.GoTo:
                        // S /Goto or /GoToR action is a goto remote file action,
                        // F specifies the file (GoToR only)
                        // D specifies the location or page

                        if (page != -1) {
                            changeTo(null, page, null, null, true);
                        }

                        break;

                    case PdfDictionary.GoToR:
                        //A /GoToR action is a goto remote file action,
                        //F specifies the file
                        //D specifies the location or page
                        //NewWindow a flag specifying whether to open it in a new window.

                        final int index = pageRef.indexOf("P.");
                        if (index != -1) {
                            pageRef = pageRef.substring(index + 2, pageRef.length());
                            page = Integer.parseInt(pageRef);
                        } else if (pageRef.equals("F")) {
                            //use file only
                            page = 1;
                        } else {
                            //if no pageRef defined default to one, confirmed by working example
                            page = 1;
                        }

                        //NOTE: filename full authenticated above dont redo.
                        if (new File(filename).exists()) {

                            //Open this file, on page 'page'
                            if (page != -1) {
                                changeTo(filename, page, null, null, true);
                            }

                            LogWriter.writeFormLog("{DefaultActionHamdler.A} Form has GoToR command, needs methods for opening new file on page specified", FormStream.debugUnimplemented);
                        } else {
                            showMessageDialog("The file specified " + filename + " Does Not Exist!");
                        }
                        break;

                }
            }
        }

        return page;
    }

    /**
     * this calls the PdfDecoder to open a new page and change to the correct page and location on page,
     * is any value is null, it means leave as is.
     *
     * @param type - the type of action
     */
    @Override
    public void changeTo(final String file, final int page, final Object location, final Integer type, final boolean storeView) {

        if (showMethods) {
            System.out.println("DefaultActionHandler.changeTo()" + file);
        }

    }

    @Override
    public PdfDecoderInt getPDFDecoder() {
        return decode_pdf;
    }

    /**
     * E action when cursor enters active area
     */
    @Override
    public void E(final Object e, final FormObject formObj) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.E()");
        }

        javascript.execute(formObj, PdfDictionary.E, ActionHandler.FOCUS_EVENT, ' ');
    }

    /**
     * X action when cursor exits active area
     */
    @Override
    public void X(final Object e, final FormObject formObj) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.X()");
        }

        javascript.execute(formObj, PdfDictionary.X, ActionHandler.FOCUS_EVENT, ' ');
    }

    /**
     * D action when cursor button pressed inside active area
     */
    @Override
    public void D(final Object e, final FormObject formObj) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.D()");
        }

        javascript.execute(formObj, PdfDictionary.D, ActionHandler.FOCUS_EVENT, ' ');

    }

    /**
     * U action when cursor button released inside active area
     */
    @Override
    public void U(final Object e, final FormObject formObj) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.U()");
        }

        javascript.execute(formObj, PdfDictionary.U, ActionHandler.FOCUS_EVENT, ' ');

    }

    /**
     * Fo action on input focus
     */
    @Override
    public void Fo(final Object e, final FormObject formObj) {     //TODO called with focus gained
        if (showMethods) {
            System.out.println("DefaultActionHandler.Fo()");
        }

        javascript.execute(formObj, PdfDictionary.Fo, ActionHandler.FOCUS_EVENT, ' ');

    }

    /**
     * Bl action when input focus lost, blur
     */
    @Override
    public void Bl(final Object e, final FormObject formObj) { // TODO called by focus lost
        if (showMethods) {
            System.out.println("DefaultActionHandler.Bl()");
        }

        javascript.execute(formObj, PdfDictionary.Bl, ActionHandler.FOCUS_EVENT, ' ');

    }

    /**
     * O called when a page is opened
     */
    @Override
    public void O(final PdfObject pdfObject, final int type) {

        if (currentPdfFile == null) {
            return;
        }

        if (showMethods) {
            System.out.println("DefaultActionHandler.O()");
        }

        final FormObject pageDictionary = (FormObject) pdfObject.getDictionary(type);
        currentPdfFile.checkResolved(pageDictionary);

        if (pageDictionary != null) {
            final FormObject Odictionary = (FormObject) pageDictionary.getDictionary(PdfDictionary.O);
            currentPdfFile.checkResolved(Odictionary);

            if (Odictionary != null) {
                String jsCode = Odictionary.getTextStreamValue(PdfDictionary.JS);
                if (jsCode == null) {
                    final PdfObject JS = Odictionary.getDictionary(PdfDictionary.JS);
                    if (JS != null)//in stream
                    {
                        jsCode = new String(JS.getDecodedStream());
                    }
                }
                javascript.executeAction(jsCode);
            }
        }
    }

    /**
     * PO action when page containing is opened,
     * actions O of pages AA dic, and OpenAction in document catalog should be done first
     */
    @Override
    public void PO(final PdfObject pdfObject, final int type) {

        if (currentPdfFile == null) {
            return;
        }

        if (showMethods) {
            System.out.println("DefaultActionHandler.PO()");
        }

        final FormObject pageDictionary = (FormObject) pdfObject.getDictionary(type);
        currentPdfFile.checkResolved(pageDictionary);

        if (pageDictionary != null) {
            final FormObject POdictionary = (FormObject) pageDictionary.getDictionary(PdfDictionary.PO);
            currentPdfFile.checkResolved(POdictionary);

            if (POdictionary != null) {
                final String jsCode = POdictionary.getTextStreamValue(PdfDictionary.JS);
                javascript.executeAction(jsCode);
            }
        }
    }

    /**
     * PC action when page is closed, action C from pages AA dic follows this
     */
    @Override
    public void PC(final PdfObject pdfObject, final int type) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.PC()");
        }

        final FormObject pageDictionary = (FormObject) pdfObject.getDictionary(type);
        currentPdfFile.checkResolved(pageDictionary);

        if (pageDictionary != null) {
            final FormObject PCdictionary = (FormObject) pageDictionary.getDictionary(PdfDictionary.PC);
            currentPdfFile.checkResolved(PCdictionary);

            if (PCdictionary != null) {
                final String jsCode = PCdictionary.getTextStreamValue(PdfDictionary.JS);
                javascript.executeAction(jsCode);

            }
        }
    }

    /**
     * PV action on viewing containing page
     */
    @Override
    public void PV(final PdfObject pdfObject, final int type) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.PV()");
        }

        final FormObject pageDictionary = (FormObject) pdfObject.getDictionary(type);
        currentPdfFile.checkResolved(pageDictionary);

        if (pageDictionary != null) {
            final FormObject PVdictionary = (FormObject) pageDictionary.getDictionary(PdfDictionary.PV);
            currentPdfFile.checkResolved(PVdictionary);

            if (PVdictionary != null) {
                final String jsCode = PVdictionary.getTextStreamValue(PdfDictionary.JS);
                javascript.executeAction(jsCode);
            }
        }
    }

    /**
     * PI action when page no longer visible in viewer
     */
    @Override
    public void PI(final PdfObject pdfObject, final int type) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.PI()");
        }

        final FormObject pageDictionary = (FormObject) pdfObject.getDictionary(type);
        currentPdfFile.checkResolved(pageDictionary);

        if (pageDictionary != null) {
            final FormObject PIdictionary = (FormObject) pageDictionary.getDictionary(PdfDictionary.PI);
            currentPdfFile.checkResolved(PIdictionary);

            if (PIdictionary != null) {
                final String jsCode = PIdictionary.getTextStreamValue(PdfDictionary.JS);
                javascript.executeAction(jsCode);

                // Scan through the fields and change any that have changed
                //acrorend.updateChangedForms();
            }
        }
    }

    /**
     * when user types a keystroke
     * K action on - [javascript]
     * keystroke in textfield or combobox
     * modifys the list box selection
     * (can access the keystroke for validity and reject or modify)
     */
    @Override
    public int K(final Object ex, final FormObject formObj, final int actionID) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.K()");
        }

        final int result = javascript.execute(formObj, PdfDictionary.K, actionID, getKeyPressed(ex));

        final int fontSize = formObj.getTextSize();
        if (acrorend.getCompData() != null && (fontSize == 0 || fontSize == -1)) {
            acrorend.getCompData().setAutoFontSize(formObj);
        }

        return result;
    }

    /**
     * F the display formatting of the field (e.g 2 decimal places) [javascript]
     */
    @Override
    public void F(final FormObject formObj) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.F()");
        }

        javascript.execute(formObj, PdfDictionary.F, ActionHandler.FOCUS_EVENT, ' ');

    }

    /**
     * V action when fields value is changed [javascript], validate
     */
    @Override
    public void V(final Object ex, final FormObject formObj, final int actionID) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.V()");
        }

        javascript.execute(formObj, PdfDictionary.V, actionID, getKeyPressed(ex));

    }

    final Map<String, String> Ccalled = new HashMap<String, String>();

    /**
     * C action when another field changes (recalculate this field) [javascript]
     * <br>
     * NOT actually called as called from other other objects but here for completeness
     */
    @Override
    public void C(final FormObject formObj) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.C() called from=" + formObj.getObjectRefAsString());
        }

        if (Ccalled.get(formObj.getObjectRefAsString()) != null) {
            return;
        }
        Ccalled.put(formObj.getObjectRefAsString(), "1");

        javascript.execute(formObj, PdfDictionary.C2, ActionHandler.FOCUS_EVENT, ' ');

        Ccalled.remove(formObj.getObjectRefAsString());
    }

    /**
     * goes through the map and adds the required data to the hideMap and returns it
     */
    private static void getHideMap(final PdfObject aData, final FieldsHideObject fieldToHide) {
        if (showMethods) {
            System.out.println("DefaultActionHandler.getHideMap()");
        }

        String[] fieldstoHide = fieldToHide.getFieldArray();
        boolean[] whethertoHide = fieldToHide.getHideArray();

        if (aData.getTextStreamValue(PdfDictionary.T) != null) {
            final String fieldList = aData.getTextStreamValue(PdfDictionary.T);
            if (fieldList != null) {
                final String[] fields;
                if (fieldstoHide.length > 0) {
                    fields = new String[fieldstoHide.length + 1];
                    System.arraycopy(fieldstoHide, 0, fields, 0, fieldstoHide.length);
                    fields[fields.length - 1] = fieldList;
                } else {
                    fields = new String[]{fieldList};
                }
                fieldstoHide = fields;
            }
        }

        final boolean hideFlag = aData.getBoolean(PdfDictionary.H);

        final boolean[] hideFlags;
        if (whethertoHide.length > 0) {
            hideFlags = new boolean[whethertoHide.length + 1];
            System.arraycopy(whethertoHide, 0, hideFlags, 0, whethertoHide.length);
            hideFlags[hideFlags.length - 1] = hideFlag;
        } else {
            hideFlags = new boolean[]{hideFlag};
        }
        whethertoHide = hideFlags;

        //put values back into fields to hide object
        fieldToHide.setFieldArray(fieldstoHide);
        fieldToHide.setHideArray(whethertoHide);

        if (aData.getDictionary(PdfDictionary.Next) != null) {
            final PdfObject nextDic = aData.getDictionary(PdfDictionary.Next);
            getHideMap(nextDic, fieldToHide);
        }
    }

    public static void showMessageDialog(final String s) {
        JOptionPane.showMessageDialog(null, s);

    }

    /**
     * pick up key press or return ' '
     */
    public static char getKeyPressed(final Object raw) {

        try {
            final ComponentEvent ex = (ComponentEvent) raw;

            if (ex instanceof KeyEvent) {
                return ((KeyEvent) ex).getKeyChar();
            } else {
                return ' ';
            }

        } catch (final Exception ee) {
            System.out.println("Exception " + ee);
        }

        return ' ';

    }

    /**
     * shows and hides the appropriate fields as defined within the map defined
     *
     * @param fieldToHide - the field names to which we want to hide
     *                    both arrays must be the same length.
     */
    public void setFieldVisibility(final FieldsHideObject fieldToHide) {

        final String[] fieldsToHide = fieldToHide.getFieldArray();
        final boolean[] whetherToHide = fieldToHide.getHideArray();

        if (fieldsToHide.length != whetherToHide.length) {
            //this will exit internally only and the production version will carry on regardless.
            LogWriter.writeFormLog("{custommouselistener} number of fields and nuber of hides or not the same", FormStream.debugUnimplemented);

            return;
        }

        for (int i = 0; i < fieldsToHide.length; i++) {
            hideComp(fieldsToHide[i], !whetherToHide[i]);
        }
    }

    private void hideComp(final String compName, final boolean visible) {

        final Object[] checkObj = acrorend.getFormComponents(compName, ReturnValues.FORMOBJECTS_FROM_NAME, -1);
        final Object[] allObj = acrorend.getFormComponents(compName, ReturnValues.FORMOBJECTS_FROM_NAME, -1);

        if (checkObj != null) {
            for (final Object obj : allObj) {


                final FormObject formObject = (FormObject) obj;
                final Rectangle rect = formObject.getBoundingRectangle();

                if (rect == null) {
                    continue;
                }

                //we need the index for the object so we can check the bounding boxes
                final float rx = rect.x;
                final float ry = rect.y;
                final float rwidth = rect.width;
                final float rheight = rect.height;
                final Rectangle rootRect = new Rectangle((int) rx, (int) ry, (int) rwidth, (int) rheight);

                //find components hidden within this components bounds and hide
                for (final Object possiblyHiddenObj : allObj) {

                    final FormObject formObject2 = (FormObject) possiblyHiddenObj;

                    if (formObject2 != null && formObject2.getBoundingRectangle() != null && rootRect.contains(formObject2.getBoundingRectangle())) {
                        formObject2.setVisible(!visible);
                    }
                }

                //checkGUIObjectResolved(formObject);

                formObject.setVisible(visible);

            }
        }
    }

    /**
     * @param listOfFields - defines a list of fields to either include or exclude from the submit option,
     *                     Dependent on the <B>flag</b>, if is null all fields are submitted.
     * @param excludeList  - if true then the listOfFields defines an exclude list,
     *                     if false the list is an include list, if listOfFields is null then this field is ignored.
     * @param submitURL    - the URL to submit to.
     */
    private void submitURL(final String[] listOfFields, final boolean excludeList, final String submitURL) {

        if (submitURL != null) {
            Component[] compsToSubmit = new Component[0];
            String[] includeNameList = new String[0];
            if (listOfFields != null) {
                if (excludeList) {
                    //listOfFields defines an exclude list

                    //Object[] forms = acrorend.getFormComponents(null, ReturnValues.FORM_NAMES, -1);


                } else {
                    //fields is an include list
                    includeNameList = listOfFields;
                }

                Component[] compsToAdd, tmp;
                for (int i = 0; i < includeNameList.length; i++) {
                    compsToAdd = (Component[]) acrorend.getFormComponents(includeNameList[i], ReturnValues.GUI_FORMS_FROM_NAME, -1);

                    if (compsToAdd != null) {
                        tmp = new Component[compsToSubmit.length + compsToAdd.length];
                        if (compsToAdd.length > 1) {
                            LogWriter.writeFormLog("(internal only) SubmitForm multipul components with same name", FormStream.debugUnimplemented);

                            if (FormStream.debugUnimplemented) {
                                org.jpedal.objects.acroforms.utils.ConvertToString.printStackTrace(1);
                            }
                        }
                        for (int k = 0; i < tmp.length; k++) {
                            if (k < compsToSubmit.length) {
                                tmp[k] = compsToSubmit[k];
                            } else if (k - compsToSubmit.length < compsToAdd.length) {
                                tmp[k] = compsToAdd[k - compsToSubmit.length];
                            }
                        }
                        compsToSubmit = tmp;
                    }
                }
            } else {
                compsToSubmit = (Component[]) acrorend.getFormComponents(null, ReturnValues.GUI_FORMS_FROM_NAME, -1);
            }


            final StringBuilder text = new StringBuilder();
            if (compsToSubmit != null && compsToSubmit.length > 0) {
                for (final Component aCompsToSubmit : compsToSubmit) {
                    if (aCompsToSubmit instanceof JTextComponent) {
                        text.append(((JTextComponent) aCompsToSubmit).getText());
                    } else if (aCompsToSubmit instanceof AbstractButton) {
                        text.append(((AbstractButton) aCompsToSubmit).getText());
                    } else if (aCompsToSubmit != null) {
                        LogWriter.writeFormLog("(internal only) SubmitForm field form type not accounted for", FormStream.debugUnimplemented);
                    }
                }
            }
        }
    }

    protected void popup(final Object raw, final FormObject formObj, final PdfObjectReader currentPdfFile) {

        if (((MouseEvent) raw).getClickCount() == 2) {

            //find the popup dictionary so we can get the ref (we do this to get the ref so we can lookup)
            final FormObject popupObj = (FormObject) formObj.getDictionary(PdfDictionary.Popup);
            currentPdfFile.checkResolved(popupObj);

            //use the ref to lookup the actual instance where the gui comp will be stored in
            final FormObject decodedObj = this.acrorend.getFormObject(popupObj.getObjectRefAsString());

            //if it exists toggle on/off
            final Object comp = decodedObj.getGUIComponent();
            if (comp != null) {

                //and we need a seperate popup for each field.
                final JComponent popup = (JComponent) comp;

                if (popup.isVisible()) {
                    popup.setVisible(false);
                } else {
                    popup.setVisible(true);
                }
            }

            //move focus so that the button does not flash
            ((Component) ((EventObject) raw).getSource()).setFocusable(false);
        }
    }

    @Override
    public PdfLayerList getLayerHandler() {

        if (decode_pdf == null) {
            return null;
        }

        final Object layer = decode_pdf.getJPedalObject(PdfDictionary.Layer);

        if (layer == null) {
            return null;
        } else {
            return (PdfLayerList) layer;
        }

    }
}

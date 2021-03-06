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
 * ClientExternalHandler.java
 * ---------------
 */
package org.jpedal.examples.viewer.objects;

import org.jpedal.external.AdditonalHandler;
import org.jpedal.external.AnnotationHandler;
import org.jpedal.external.JPedalActionHandler;
import org.jpedal.external.Options;
import org.jpedal.io.Speech;

/**
 * Additional handlers used in Client
 *
 * @author markee
 */
public abstract class ClientExternalHandler implements AdditonalHandler {


    AnnotationHandler annotationHandler; // =new ExampleAnnotationHandler();

    // [AWI] Used when the UI is ready for Keyboard input (used for touchscreens with virtual keyboards).

    private JPedalActionHandler keyboardHandler;

    private Speech speech;

    @Override
    public Object getExternalHandler(final int type) {

        switch (type) {
            
            /* [AWI] Used when the UI is ready for Keyboard input (used for touchscreens with virtual keyboards). */
            case Options.KeyboardReadyHandler:
                return keyboardHandler;

            case Options.SpeechEngine:
                return speech;

            case Options.UniqueAnnotationHandler:
                return annotationHandler;

            default:
                throw new IllegalArgumentException("Unknown type=" + type);

        }
    }

    @Override
    public void addExternalHandler(final Object newHandler, final int type) {

        switch (type) {

            case Options.UniqueAnnotationHandler:
                annotationHandler = (AnnotationHandler) newHandler;
                break;
            
             /* [AWI] Used when the UI is ready for Keyboard input (used for touchscreens with virtual keyboards). */
            case Options.KeyboardReadyHandler:
                if (newHandler instanceof JPedalActionHandler) {
                    keyboardHandler = (JPedalActionHandler) newHandler;
                }
                break;

            case Options.SpeechEngine:
                if (newHandler instanceof Speech) {
                    speech = (Speech) newHandler;
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown type=" + type);

        }
    }
}

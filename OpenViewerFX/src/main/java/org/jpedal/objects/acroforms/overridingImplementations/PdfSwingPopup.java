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
 * PdfSwingPopup.java
 * ---------------
 */
package org.jpedal.objects.acroforms.overridingImplementations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JTextArea;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MouseInputAdapter;

import org.jpedal.objects.acroforms.actions.SwingListener;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.StringUtils;

/**
 * provide PDF popup for Annotations
 */
public class PdfSwingPopup extends JInternalFrame {
    /**
     *
     */
    private static final long serialVersionUID = 796302916236391896L;

    PopupTitleBar titleBar;
    PopupContentArea contentArea;

    final FormObject formObject;

    @SuppressWarnings("UnusedParameters")
    public PdfSwingPopup(final FormObject popupObj, final int cropBoxWidth, final SwingListener listener) {

        formObject = popupObj;
        //float[] rect = formObject.getFloatArray(PdfDictionary.Rect);

        float[] col;
        final String titleString;
        String contentString;
        /*
         * all the popup data is in the Parent not the popup object
         */
        final PdfObject parentObj = popupObj.getParentPdfObj();

        if (parentObj == null) {
            col = new float[]{255, 255, 0};
            titleString = "";
            contentString = "";
        } else {

            //Set color from the popup object
            col = popupObj.getFloatArray(PdfDictionary.C);

            //If no C value present, check the parent object
            if (col == null) {
                col = parentObj.getFloatArray(PdfDictionary.C);
            }

            //If no color specified then use our default
            if (col == null) {
                col = new float[]{255, 255, 0};
            }


            final StringBuilder titleBuilder = new StringBuilder();

            final String subject = parentObj.getTextStreamValue(PdfDictionary.Subj);
            if (subject != null) {
                titleBuilder.append(subject);
                titleBuilder.append('\t');
            }

            //read in date for title bar
            final String modifiedDate = parentObj.getTextStreamValue(PdfDictionary.M);
            if (modifiedDate != null) {
                final StringBuilder date = new StringBuilder(modifiedDate);
                date.delete(0, 2); //delete D:
                date.insert(10, ':');
                date.insert(13, ':');
                date.insert(16, ' ');

                final String year = date.substring(0, 4);
                final String day = date.substring(6, 8);
                date.delete(6, 8);
                date.delete(0, 4);
                date.insert(0, day);
                date.insert(4, year);
                date.insert(2, '/');
                date.insert(5, '/');
                date.insert(10, ' ');

                titleBuilder.append(date);
            }

            //setup title text for popup

            final String autherName = popupObj.getTextStreamValue(PdfDictionary.T);
            if (autherName != null) {
                titleBuilder.append('\n');
                titleBuilder.append(autherName);
            }

            titleString = titleBuilder.toString();

            //main body text on contents is always a text readable form of the form or the content of the popup window.
            contentString = parentObj.getTextStreamValue(PdfDictionary.Contents);
            if (contentString == null) {
                contentString = "";
            }
            if (contentString.indexOf('\r') != -1) {
                contentString = contentString.replaceAll("\r", "\n");
            }

        }

        //remove title bar from internalframe so its looks as we want
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null);

        setLayout(new BorderLayout());

        //add title bar
        titleBar = new PopupTitleBar(titleString);
        titleBar.setHighlighter(null);
        titleBar.setEditable(false);
        add(titleBar, BorderLayout.NORTH);

        setColor(col);
        //add content area
        contentArea = new PopupContentArea(contentString);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        add(contentArea, BorderLayout.CENTER);

        contentArea.addKeyListener(new PopupContentsUpdater());

        contentArea.addMouseListener(listener);

        //Set to false by listener but should be true
        contentArea.setOpaque(true);

        //set the font sizes so that they look more like adobes popups
        final Font titFont = titleBar.getFont();

        //Set base font size
        final int baseFontSize = (int) formObject.getFontSize();

        titleBar.setFont(new Font(titFont.getName(), titFont.getStyle(), baseFontSize - 1));
        final Font curFont = contentArea.getFont();
        contentArea.setFont(new Font(curFont.getName(), curFont.getStyle(), baseFontSize - 2));

        //add focus listener to bring selected popup to front
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                toFront();
                super.focusGained(e);
            }
        });

        // Fix for popups showing up behind other objects on page        
        addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameActivated(final InternalFrameEvent e) {
                final JInternalFrame frame = e.getInternalFrame();
                // Brings the component to the front
                frame.getParent().setComponentZOrder(frame, 0);
            }

            @Override
            public void internalFrameDeactivated(final InternalFrameEvent e) {
                final JInternalFrame frame = e.getInternalFrame();

                //Changing page will set parent to null, so if null we ignore
                if (frame.getParent() != null) {
                    // Sends the component back by one when focus is lost
                    frame.getParent().setComponentZOrder(frame, 1);
                }
            }
        });
    }

    @Override
    public void updateUI() {
        super.updateUI();
        //remove title bar from internalframe so its looks as we want
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null);
    }

    public void setColor(final float[] col) {
        //setup background color
        Color bgColor = null;
        if (col != null) {
            if (col[0] > 1 || col[1] > 1 || col[2] > 1) {
                bgColor = new Color((int) col[0], (int) col[1], (int) col[2]);
            } else {
                bgColor = new Color(col[0], col[1], col[2]);
            }

            //and set border to that if valid
            setBorder(BorderFactory.createLineBorder(bgColor));
        }
        if (bgColor != null) {
            titleBar.setBackground(bgColor);
        }
    }

    //Done this way so our listener to immediately tell if popup and drag accordingly
    private class PopupTitleBar extends JTextArea {

        public PopupTitleBar(final String title) {
            super(title);

            final PopupDragListener listener = new PopupDragListener();
            //add our drag listener so it acts like an internal frame
            addMouseMotionListener(listener);
            addMouseListener(listener);
        }
    }

    //Done this way so our listener to immediately tell if popup and drag accordingly
    private class PopupContentArea extends JTextArea {

        public PopupContentArea(final String contents) {
            super(contents);

            final PopupContentsUpdater listener = new PopupContentsUpdater();
            //add our drag listener so it acts like an internal frame
            addKeyListener(listener);
        }
    }

    private class PopupDragListener extends MouseInputAdapter {
        Point clickStart;

        @Override
        public void mouseDragged(final MouseEvent e) {
            if (clickStart == null) {
                clickStart = e.getPoint();
            }
            //move the popup as the user drags the mouse
            final Point pt = e.getPoint();
            final Point curLoc = getLocation();
            final int x = pt.x - clickStart.x;
            final int y = pt.y - clickStart.y;
            curLoc.translate(x, y);
            setLocation(curLoc);

            final float[] rect = formObject.getFloatArray(PdfDictionary.Rect);
            rect[0] += (x / formObject.getCurrentScaling());
            rect[2] += (x / formObject.getCurrentScaling());
            rect[1] -= (y / formObject.getCurrentScaling());
            rect[3] -= (y / formObject.getCurrentScaling());
            formObject.setFloatArray(PdfDictionary.Rect, rect);
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            clickStart = e.getPoint();
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            clickStart = null;
        }
    }

    private class PopupContentsUpdater extends KeyAdapter {

        @Override
        public void keyReleased(final KeyEvent e) {
            PdfObject parentObj = formObject.getParentPdfObj();

            if (parentObj == null) {
                parentObj = formObject;
            }

            parentObj.setTextStreamValue(PdfDictionary.Contents, StringUtils.toBytes(contentArea.getText()));
        }
    }

    @Override
    /*
     * Set the font for the popup window.
     * The font is modified in size for the title and the content.
     */
    public void setFont(final Font f) {
        super.setFont(f);

        final int fontSize = f.getSize();

        if (titleBar != null) {
            titleBar.setFont(titleBar.getFont().deriveFont((float) fontSize - 1));
        }

        if (contentArea != null && titleBar != null) {
            contentArea.setFont(titleBar.getFont().deriveFont((float) fontSize - 2));
        }

    }
}

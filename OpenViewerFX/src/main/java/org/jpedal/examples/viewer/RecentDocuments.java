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
 * RecentDocuments.java
 * ---------------
 */
package org.jpedal.examples.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.swing.JMenuItem;

import org.jpedal.examples.viewer.commands.SaveFile;
import org.jpedal.examples.viewer.utils.Printer;
import org.jpedal.examples.viewer.utils.PropertiesFile;
import org.jpedal.gui.GUIFactory;
import org.jpedal.utils.Messages;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RecentDocuments implements RecentDocumentsFactory {

    //int noOfRecentDocs;
    //PropertiesFile properties;

    public final int noOfRecentDocs;

    private final Stack<String> previousFiles = new Stack<String>();
    private final Stack<String> nextFiles = new Stack<String>();

    public final JMenuItem[] recentDocuments;


    public RecentDocuments(final int noOfRecentDocs) {

        this.noOfRecentDocs = noOfRecentDocs;
        recentDocuments = new JMenuItem[noOfRecentDocs];
        //this.noOfRecentDocs=noOfRecentDocs;
        //this.properties=properties;

    }

    /**
     * Convert the provided file name into a shortened file name
     *
     * @param fileNameToAdd String value of the filename
     * @return String value representing the shortened file name
     */
    static String getShortenedFileName(final String fileNameToAdd) {
        final int maxChars = 30;

        if (fileNameToAdd.length() <= maxChars) {
            return fileNameToAdd;
        }

        final StringTokenizer st = new StringTokenizer(fileNameToAdd, "\\/");

        final int noOfTokens = st.countTokens();

        //allow for /filename.pdf
        if (noOfTokens == 1) {
            return fileNameToAdd.substring(0, maxChars);
        }

        final String[] arrayedFile = new String[noOfTokens];
        for (int i = 0; i < noOfTokens; i++) {
            arrayedFile[i] = st.nextToken();
        }

        final String filePathBody = fileNameToAdd.substring(arrayedFile[0].length(),
                fileNameToAdd.length() - arrayedFile[noOfTokens - 1].length());

        final StringBuilder sb = new StringBuilder(filePathBody);

        int start, end;
        for (int i = noOfTokens - 2; i > 0; i--) {

            start = sb.lastIndexOf(arrayedFile[i]);
            end = start + arrayedFile[i].length();
            sb.replace(start, end, "...");

            if (sb.length() <= maxChars) {
                break;
            }
        }

        return arrayedFile[0] + sb + arrayedFile[noOfTokens - 1];
    }

    /**
     * Get the previously opened documents file name
     *
     * @return String value representing the documents file name
     */
    @Override
    public String getPreviousDocument() {

        String fileToOpen = null;

        if (previousFiles.size() > 1) {
            nextFiles.push(previousFiles.pop());
            fileToOpen = previousFiles.pop();
        }

        return fileToOpen;
    }

    /**
     * Get the file name of the next document in the list.
     * This can be used to move through the list of recent files     *
     *
     * @return String value representing the documents file name
     */
    @Override
    public String getNextDocument() {

        String fileToOpen = null;

        if (!nextFiles.isEmpty()) {
            fileToOpen = nextFiles.pop();
        }

        return fileToOpen;
    }

    /**
     * Adds a file to the list of recent file
     *
     * @param selectedFile String representing a file name
     */
    @Override
    public void addToFileList(final String selectedFile) {
        previousFiles.push(selectedFile);


    }

    /**
     * Control if recent documents items should be enabled and visible
     *
     * @param enable True to show and enable the recent documents items, false otherwise
     */
    @Override
    public void enableRecentDocuments(final boolean enable) {
        if (recentDocuments == null) {
            return;
        }

        for (int i = 0; i < recentDocuments.length; i++) {
            if (recentDocuments[i] != null && !recentDocuments[i].getText().equals(i + 1 + ": ")) {
                recentDocuments[i].setVisible(enable);
                recentDocuments[i].setEnabled(enable);
            }
        }
    }

    /**
     * Set the recent documents items from the set of values provided
     *
     * @param recentDocs String array of file names to load into the recent documents
     */
    @Override
    public void updateRecentDocuments(final String[] recentDocs) {
        if (recentDocs == null) {
            return;
        }

        for (int i = 0; i < recentDocs.length; i++) {
            if (recentDocs[i] != null) {

                final String shortenedFileName = getShortenedFileName(recentDocs[i]);

                if (recentDocuments[i] == null) {
                    recentDocuments[i] = new JMenuItem();
                }

                recentDocuments[i].setText(i + 1 + ": " + shortenedFileName);
                if (recentDocuments[i].getText().equals(i + 1 + ": ")) {
                    recentDocuments[i].setVisible(false);
                } else {
                    recentDocuments[i].setVisible(true);
                }
                recentDocuments[i].setName(recentDocs[i]);
            }
        }
    }

    /**
     * Remove all items from the recent documents list and stored in properties list
     *
     * @param properties PropertiesFile object holding the recent documents to clear
     */
    @Override
    public void clearRecentDocuments(final PropertiesFile properties) {
        final NodeList nl = properties.getDoc().getElementsByTagName("recentfiles");

        if (nl != null && nl.getLength() > 0) {
            final NodeList allRecentDocs = ((Element) nl.item(0)).getElementsByTagName("*");

            for (int i = 0; i < allRecentDocs.getLength(); i++) {
                final Node item = allRecentDocs.item(i);
                nl.item(0).removeChild(item);
            }
        }

        for (int i = 0; i < noOfRecentDocs; i++) {
            recentDocuments[i].setText(i + 1 + ": ");
            recentDocuments[i].setVisible(false);
        }
    }

    /**
     * Create the menu items used to represent the recent documents in the Viewer menu
     *
     * @param fileNameToAdd String filename for the menu item
     * @param position      int value representing the position in the recent documents
     * @param currentGUI    GUIFactory object to add the menu item to
     * @param commonValues  Values object used by the view
     */
    @Override
    public void createMenuItems(final String fileNameToAdd, final int position, final GUIFactory currentGUI,
                                final Values commonValues) {

        final String shortenedFileName = RecentDocuments.getShortenedFileName(fileNameToAdd);
        recentDocuments[position] = new JMenuItem(position + 1 + ": " + shortenedFileName);

        if (recentDocuments[position].getText().equals(position + 1 + ": ")) {
            recentDocuments[position].setVisible(false);
        }

        recentDocuments[position].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {

                if (Printer.isPrinting()) {
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerPrintWait.message"));
                } else if (Values.isProcessing()) {
                    currentGUI.showMessageDialog(Messages.getMessage("PdfViewerDecodeWait.message"));
                } else {
                    /*
                     * warn user on forms
                     */
                    SaveFile.handleUnsaveForms(currentGUI, commonValues);
                    final JMenuItem item = (JMenuItem) e.getSource();
                    final String fileName = item.getName();

                    if (!fileName.isEmpty()) {
                        currentGUI.open(fileName);
                    }
                }
            }
        });

        recentDocuments[position].setName(fileNameToAdd);

        currentGUI.getMenuItems().addToMenu(recentDocuments[position], Commands.FILEMENU);

    }

}

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
 * ExtractPDFPagesNup.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.print.attribute.standard.PageRanges;
import javax.swing.*;

import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.utils.ItextFunctions;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

/**
 * This class creates an interface to allow the user to specify various options
 * to be used when N-upping a document. The values chosen are held in this class
 * so that can be passed along to the required methods.
 */
public class ExtractPDFPagesNup extends Save {

    final JLabel OutputLabel = new JLabel();
    final ButtonGroup buttonGroup1 = new ButtonGroup();
    final ButtonGroup buttonGroup2 = new ButtonGroup();

    final JToggleButton jToggleButton3 = new JToggleButton();

    final JToggleButton jToggleButton2 = new JToggleButton();

    final JRadioButton printAll = new JRadioButton();
    final JRadioButton printCurrent = new JRadioButton();
    final JRadioButton printPages = new JRadioButton();

    final JTextField pagesBox = new JTextField();

    ArrayList<String> papers;
    ArrayList<Dimension> paperDimensions;

    private final javax.swing.JSpinner horizontalSpacing;
    private final javax.swing.JLabel jLabel1;
    private final javax.swing.JLabel jLabel11;
    private final javax.swing.JLabel jLabel12;
    private final javax.swing.JLabel jLabel13;
    private final javax.swing.JLabel jLabel14;
    private final javax.swing.JLabel jLabel15;
    private final javax.swing.JLabel jLabel16;
    private final javax.swing.JLabel jLabel17;
    private final javax.swing.JLabel jLabel2;
    private final javax.swing.JLabel jLabel3;
    private final javax.swing.JLabel jLabel4;
    private final javax.swing.JSpinner layoutColumns;
    private final javax.swing.JSpinner layoutRows;
    private final JComboBox<String> layouts;
    private final javax.swing.JSpinner leftRightMargins;
    private final javax.swing.JSpinner scaleHeight;
    private final javax.swing.JCheckBox pageProportionally;
    private final JComboBox<String> pageScalings;
    private final javax.swing.JSpinner scaleWidth;
    private final javax.swing.JSpinner paperHeight;
    private final JComboBox<String> paperOrientation;
    private final JComboBox<String> paperSizes;
    private final javax.swing.JSpinner paperWidth;
    private final javax.swing.JSpinner topBottomMargins;
    private final javax.swing.JSpinner verticalSpacing;

    private final JComboBox<String> repeat = new JComboBox<String>();
    private final JSpinner copies = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JComboBox<String> ordering = new JComboBox<String>();
    private final JComboBox<String> doubleSided = new JComboBox<String>();

    /**
     * Create an interface to set the required values for N-upping the current
     * document
     *
     * @param root_dir    String representing the root directory to be displayed
     *                    when saving
     * @param end_page    The last page in the document (page count)
     * @param currentPage The current displayed page
     */
    public ExtractPDFPagesNup(final String root_dir, final int end_page, final int currentPage) {
        super(root_dir, end_page, currentPage);

        genertatePaperSizes();

        layouts = new javax.swing.JComboBox<String>();
        paperOrientation = new javax.swing.JComboBox<String>();
        pageScalings = new javax.swing.JComboBox<String>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        topBottomMargins = new javax.swing.JSpinner(new SpinnerNumberModel(18.00, -720.00, 720.00, 1.00));
        leftRightMargins = new javax.swing.JSpinner(new SpinnerNumberModel(18.00, -720.00, 720.00, 1.00));
        pageProportionally = new javax.swing.JCheckBox();
        paperSizes = new javax.swing.JComboBox<String>();
        jLabel11 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        paperWidth = new javax.swing.JSpinner();
        paperHeight = new javax.swing.JSpinner();
        scaleWidth = new javax.swing.JSpinner(new SpinnerNumberModel(396.00, 72.00, 5184.00, 1.00));
        scaleHeight = new javax.swing.JSpinner(new SpinnerNumberModel(612.00, 72.00, 5184.00, 1.00));
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        layoutRows = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        layoutColumns = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        jLabel14 = new javax.swing.JLabel();
        verticalSpacing = new javax.swing.JSpinner(new SpinnerNumberModel(7.20, 0.00, 720.00, 1.00));
        horizontalSpacing = new javax.swing.JSpinner(new SpinnerNumberModel(7.20, 0.00, 720.00, 1.00));
        jLabel16 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();

        try {
            jbInit();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public final int[] getPages() {

        int[] pagesToExport = null;

        if (printAll.isSelected()) {
            pagesToExport = new int[end_page];
            for (int i = 0; i < end_page; i++) {
                pagesToExport[i] = i + 1;
            }

        } else if (printCurrent.isSelected()) {
            pagesToExport = new int[1];
            pagesToExport[0] = currentPage;

        } else if (printPages.isSelected()) {

            try {
                final PageRanges pages = new PageRanges(pagesBox.getText());

                int count = 0;
                int i = -1;
                while ((i = pages.next(i)) != -1) {
                    count++;
                }

                pagesToExport = new int[count];
                count = 0;
                i = -1;
                while ((i = pages.next(i)) != -1) {
                    if (i > end_page) {
                        if (GUI.showMessages) {
                            JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerText.Page") + ' '
                                    + i + ' ' + Messages.getMessage("PdfViewerError.OutOfBounds") + ' '
                                    + Messages.getMessage("PdfViewerText.PageCount") + ' ' + end_page);
                        }
                        return null;
                    }
                    pagesToExport[count] = i;
                    count++;
                }
            } catch (final IllegalArgumentException e) {
                LogWriter.writeLog("Exception " + e + " in exporting pdfs");
                if (GUI.showMessages) {
                    JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerError.InvalidSyntax"));
                }
            }
        }

        return pagesToExport;

    }

    public float getHorizontalSpacing() {
        return Float.parseFloat(horizontalSpacing.getValue().toString());
    }

    public float getVerticalSpacing() {
        return Float.parseFloat(verticalSpacing.getValue().toString());
    }

    public float getLeftRightMargin() {
        return Float.parseFloat(leftRightMargins.getValue().toString());
    }

    public float getTopBottomMargin() {
        return Float.parseFloat(topBottomMargins.getValue().toString());
    }

    public int getPaperWidth() {
        return Integer.parseInt(paperWidth.getValue().toString());
    }

    public int getPaperHeight() {
        return Integer.parseInt(paperHeight.getValue().toString());
    }

    public String getPaperOrientation() {
        return (String) paperOrientation.getSelectedItem();
    }

    public String getScale() {
        return (String) pageScalings.getSelectedItem();
    }

    public boolean isScaleProportional() {
        return pageProportionally.isSelected();
    }

    public float getScaleWidth() {
        return Float.parseFloat(scaleWidth.getValue().toString());
    }

    public float getScaleHeight() {
        return Float.parseFloat(scaleHeight.getValue().toString());
    }

    public String getSelectedLayout() {
        return (String) layouts.getSelectedItem();
    }

    public int getLayoutRows() {
        return Integer.parseInt(layoutRows.getValue().toString());
    }

    public int getLayoutColumns() {
        return Integer.parseInt(layoutColumns.getValue().toString());
    }

    public int getRepeat() {
        if (repeat.getSelectedIndex() == 0) {
            return ItextFunctions.REPEAT_NONE;
        }

        if (repeat.getSelectedIndex() == 1) {
            return ItextFunctions.REPEAT_AUTO;
        }

        return ItextFunctions.REPEAT_SPECIFIED;
    }

    public int getCopies() {
        return Integer.parseInt(copies.getValue().toString());
    }

    public int getPageOrdering() {
        if (ordering.getSelectedIndex() == 0) {
            return ItextFunctions.ORDER_ACROSS;
        }

        if (ordering.getSelectedIndex() == 1) {
            return ItextFunctions.ORDER_DOWN;
        }

        return ItextFunctions.ORDER_STACK;
    }

    public String getDoubleSided() {
        return (String) doubleSided.getSelectedItem();
    }

    private void jbInit() throws Exception {
        setChildBounds();

        initComboBoxes();

        pageProportionally.setText(Messages.getMessage("PdfViewerNUPText.Proportionally"));
        pageProportionally.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pageProportionally.setMargin(new java.awt.Insets(0, 0, 0, 0));

        copies.setEnabled(false);
        paperWidth.setEnabled(false);
        paperHeight.setEnabled(false);
        scaleWidth.setEnabled(false);
        scaleHeight.setEnabled(false);
        layoutRows.setEnabled(false);
        layoutColumns.setEnabled(false);

        jLabel1.setText(Messages.getMessage("PdfViewerNUPLabel.Width"));
        jLabel2.setText(Messages.getMessage("PdfViewerNUPLabel.Height"));
        jLabel11.setText(Messages.getMessage("PdfViewerNUPLabel.Orientation"));
        jLabel3.setText(Messages.getMessage("PdfViewerNUPLabel.Width"));
        jLabel4.setText(Messages.getMessage("PdfViewerNUPLabel.Height"));
        jLabel12.setText(Messages.getMessage("PdfViewerNUPLabel.Rows"));
        jLabel13.setText(Messages.getMessage("PdfViewerNUPLabel.Columns"));
        jLabel14.setText(Messages.getMessage("PdfViewerNUPLabel.Left&RightMargins"));
        jLabel16.setText(Messages.getMessage("PdfViewerNUPLabel.HorizontalSpacing"));
        jLabel15.setText(Messages.getMessage("PdfViewerNUPLabel.Top&BottomMargins"));
        jLabel17.setText(Messages.getMessage("PdfViewerNUPLabel.VerticalSpacing"));
        pageRangeLabel.setText(Messages.getMessage("PdfViewerNUPLabel.PageRange"));
        printAll.setText(Messages.getMessage("PdfViewerNUPOption.All"));
        printCurrent.setText(Messages.getMessage("PdfViewerNUPOption.CurrentPage"));
        printPages.setText(Messages.getMessage("PdfViewerNUPOption.Pages"));

        printAll.setSelected(true);
        pageProportionally.setSelected(true);

        pagesBox.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent arg0) {
            }

            @Override
            public void keyReleased(final KeyEvent arg0) {
                if (pagesBox.getText().isEmpty()) {
                    printCurrent.setSelected(true);
                } else {
                    printPages.setSelected(true);
                }

            }

            @Override
            public void keyTyped(final KeyEvent arg0) {
            }
        });

        pageSelectionChanged();

        buildUserInterface();
    }

    private void initComboBoxes() {

        layouts.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{"2 Up", "4 Up", "8 Up", Messages.getMessage("PdfViewerNUPOption.Custom")}));
        repeat.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{Messages.getMessage("PdfViewerNUPOption.None"), Messages.getMessage("PdfViewerNUPOption.Auto"), Messages.getMessage("PdfViewerNUPOption.Specified")}));
        ordering.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{Messages.getMessage("PdfViewerNUPOption.Across"), Messages.getMessage("PdfViewerNUPOption.Down")}));
        doubleSided.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{Messages.getMessage("PdfViewerNUPOption.None"), Messages.getMessage("PdfViewerNUPOption.Front&Back"), Messages.getMessage("PdfViewerNUPOption.Gutter")}));
        paperOrientation.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{Messages.getMessage("PdfViewerNUPOption.Auto"), Messages.getMessage("PdfViewerNUPOption.Portrait"), Messages.getMessage("PdfViewerNUPOption.Landscape")}));
        pageScalings.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{Messages.getMessage("PdfViewerNUPOption.OriginalSize"), Messages.getMessage("PdfViewerNUPOption.Auto"), Messages.getMessage("PdfViewerNUPOption.Specified")}));
        paperSizes.setModel(new javax.swing.DefaultComboBoxModel<String>(getPaperSizes()));

        layouts.setSelectedIndex(0);
        pageScalings.setSelectedIndex(1);

        layouts.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                layoutsSelectionChanged();
            }
        });

        repeat.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                if (repeat.getSelectedItem().equals("None")) {
                    copies.getModel().setValue(1);
                    copies.setEnabled(false);
                } else if (repeat.getSelectedItem().equals("Auto")) {
                    final int rows = Integer.parseInt(layoutRows.getValue().toString());
                    final int coloumns = Integer.parseInt(layoutColumns.getValue().toString());

                    copies.getModel().setValue(rows * coloumns);
                    copies.setEnabled(false);
                } else if (repeat.getSelectedItem().equals("Specified")) {
                    copies.setEnabled(true);
                }
            }
        });

        pageScalings.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                scalingSelectionChanged();
            }
        });

        paperSizes.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                pageSelectionChanged();
            }
        });
    }

    private void buildUserInterface() {

        this.add(rootDir, null);
        this.add(rootFilesLabel, null);
        this.add(changeButton, null);

        this.add(printAll, null);
        this.add(printCurrent, null);

        this.add(createScaleLabel());
        this.add(createLayoutLabel());
        this.add(createMarginsLabel());

        this.add(layoutColumns);
        this.add(layoutRows);
        this.add(layouts);
        this.add(leftRightMargins);
        this.add(scaleHeight);
        this.add(pageProportionally);
        this.add(pageScalings);
        this.add(scaleWidth);
        this.add(paperHeight);
        this.add(paperOrientation);
        this.add(paperSizes);
        this.add(paperWidth);
        this.add(topBottomMargins);
        this.add(verticalSpacing);

        this.add(horizontalSpacing);
        this.add(jLabel1);
        this.add(jLabel2);
        this.add(jLabel3);
        this.add(jLabel4);
        this.add(jLabel11);
        this.add(jLabel12);
        this.add(jLabel13);
        this.add(jLabel14);
        this.add(jLabel15);
        this.add(jLabel16);
        this.add(jLabel17);

        this.add(createPageSettingsLabel());
        this.add(createRepeatLabel());
        this.add(repeat);
        this.add(createPageOrderingLabel());
        this.add(copies);
        this.add(createCopiesLabel());
        this.add(ordering);
        //this.add(jLabel21);
        //this.add(doubleSided);

        this.add(printPages, null);
        this.add(pagesBox, null);
        this.add(createPageInfoInput(), null);

        this.add(createPaperSizeLabel(), null);
        this.add(changeButton, null);
        this.add(pageRangeLabel, null);

        this.add(jToggleButton2, null);
        this.add(jToggleButton3, null);

        buttonGroup1.add(printAll);
        buttonGroup1.add(printCurrent);
        buttonGroup1.add(printPages);
    }

    private static JLabel createMarginsLabel() {
        final JLabel margins = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Margins"));
        margins.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        margins.setDisplayedMnemonic('0');
        margins.setBounds(new Rectangle(13, 280, 220, 26));

        return margins;
    }

    private static JLabel createLayoutLabel() {
        final JLabel layout = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Layout"));
        layout.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        layout.setDisplayedMnemonic('0');
        layout.setBounds(new Rectangle(13, 210, 220, 26));

        return layout;
    }

    private static JLabel createScaleLabel() {
        final JLabel scale = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Scale"));
        scale.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        scale.setDisplayedMnemonic('0');
        scale.setBounds(new Rectangle(13, 140, 220, 26));

        return scale;
    }

    private static JLabel createPageSettingsLabel() {
        final JLabel pageSettings = new JLabel(Messages.getMessage("PdfViewerNUPLabel.PageSettings"));
        pageSettings.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        pageSettings.setDisplayedMnemonic('0');
        pageSettings.setBounds(new Rectangle(13, 400, 220, 26));

        return pageSettings;
    }

    private static JLabel createPageOrderingLabel() {
        final JLabel jLabel19 = new JLabel(Messages.getMessage("PdfViewerNUPLabel.PageOrdering"));
        jLabel19.setBounds(22, 474, 130, 15);

        return jLabel19;
    }

    private static JLabel createRepeatLabel() {

        final JLabel jLabel18 = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Repeat"));
        jLabel18.setBounds(22, 446, 130, 15);
        return jLabel18;
    }

    private static JLabel createCopiesLabel() {
        final JLabel jLabel20 = new JLabel(Messages.getMessage("PdfViewerNUPLabel.Copies"));
        jLabel20.setBounds(300, 446, 130, 15);

        return jLabel20;
    }

    private static JTextArea createPageInfoInput() {
        final JTextArea pagesInfo = new JTextArea(Messages.getMessage("PdfViewerMessage.PageNumberOrRangeLong"));
        pagesInfo.setBounds(new Rectangle(23, 640, 600, 40));
        pagesInfo.setOpaque(false);

        return pagesInfo;
    }

    private static JLabel createPaperSizeLabel() {
        final JLabel textAndFont = new JLabel(Messages.getMessage("PdfViewerNUPLabel.PaperSize"));
        textAndFont.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        textAndFont.setDisplayedMnemonic('0');
        textAndFont.setBounds(new Rectangle(13, 70, 220, 26));

        return textAndFont;
    }

    private void setChildBounds() {
        rootFilesLabel.setBounds(13, 13, 400, 26);
        rootDir.setBounds(20, 40, 232, 23);
        changeButton.setBounds(272, 40, 101, 23);
        layouts.setBounds(20, 240, 110, 23);
        paperOrientation.setBounds(510, 100, 90, 23);
        pageScalings.setBounds(20, 170, 200, 23);
        jLabel1.setBounds(148, 100, 50, 15);
        jLabel2.setBounds(278, 100, 50, 15);
        pageProportionally.setBounds(240, 170, 120, 15);
        paperSizes.setBounds(20, 100, 110, 23);
        jLabel11.setBounds(408, 100, 130, 15);
        jLabel3.setBounds(370, 170, 50, 15);
        jLabel4.setBounds(500, 170, 50, 15);
        paperWidth.setBounds(195, 100, 70, 23);
        paperHeight.setBounds(318, 100, 70, 23);
        scaleWidth.setBounds(420, 170, 70, 23);
        scaleHeight.setBounds(540, 170, 70, 23);
        jLabel12.setBounds(148, 240, 50, 15);
        jLabel13.setBounds(278, 240, 50, 15);
        layoutRows.setBounds(195, 240, 70, 23);
        layoutColumns.setBounds(328, 240, 70, 23);
        jLabel14.setBounds(22, 326, 200, 15);
        leftRightMargins.setBounds(210, 322, 70, 23);
        jLabel16.setBounds(22, 356, 180, 15);
        horizontalSpacing.setBounds(210, 354, 70, 23);
        jLabel15.setBounds(300, 326, 180, 15);
        topBottomMargins.setBounds(480, 320, 70, 23);
        jLabel17.setBounds(300, 356, 180, 15);
        verticalSpacing.setBounds(480, 354, 70, 23);
        repeat.setBounds(140, 442, 100, 23);
        ordering.setBounds(140, 474, 130, 23);
        copies.setBounds(420, 440, 70, 23);
        doubleSided.setBounds(420, 474, 100, 23);
        pageRangeLabel.setBounds(13, 530, 199, 26);
        printAll.setBounds(23, 560, 75, 22);
        printCurrent.setBounds(23, 580, 100, 22);
        printPages.setBounds(23, 600, 70, 22);
        pagesBox.setBounds(95, 602, 230, 22);
    }

    private void layoutsSelectionChanged() {
        final String layout = (String) layouts.getSelectedItem();

        if (layout.equals("2 Up")) {
            layoutRows.getModel().setValue(1);
            layoutColumns.getModel().setValue(2);

            layoutRows.setEnabled(false);
            layoutColumns.setEnabled(false);
        } else if (layout.equals("4 Up")) {
            layoutRows.getModel().setValue(2);
            layoutColumns.getModel().setValue(2);

            layoutRows.setEnabled(false);
            layoutColumns.setEnabled(false);

        } else if (layout.equals("8 Up")) {
            layoutRows.getModel().setValue(2);
            layoutColumns.getModel().setValue(4);

            layoutRows.setEnabled(false);
            layoutColumns.setEnabled(false);

        } else if (layout.equals("Custom")) {
            layoutRows.setEnabled(true);
            layoutColumns.setEnabled(true);
        }
    }

    private void scalingSelectionChanged() {
        final String scaling = (String) pageScalings.getSelectedItem();

        if (scaling.equals("Use Original Size")) {
            pageProportionally.setEnabled(false);
            scaleWidth.setEnabled(false);
            scaleHeight.setEnabled(false);
        } else if (scaling.equals("Auto")) {
            pageProportionally.setEnabled(true);
            scaleWidth.setEnabled(false);
            scaleHeight.setEnabled(false);
        } else if (scaling.equals("Specified")) {
            pageProportionally.setEnabled(true);
            scaleWidth.setEnabled(true);
            scaleHeight.setEnabled(true);
        }
    }

    private void pageSelectionChanged() {

        final Dimension d = getPaperDimension((String) paperSizes.getSelectedItem());

        if (d == null) {
            paperWidth.setEnabled(true);
            paperHeight.setEnabled(true);
        } else {
            paperWidth.setEnabled(false);
            paperHeight.setEnabled(false);

            paperWidth.setValue(d.width);
            paperHeight.setValue(d.height);
        }
    }

    private void genertatePaperSizes() {
        papers = new ArrayList<String>();
        paperDimensions = new ArrayList<Dimension>();

        papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Letter"));
        papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Legal"));
        papers.add("11x17");
        papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Ledger"));
        papers.add("A2");
        papers.add("A3");
        papers.add("A4");
        papers.add("A5");
        papers.add("B3");
        papers.add("B4");
        papers.add("B5");
        papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Folio"));
        papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Status"));
        papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Note"));
        papers.add(Messages.getMessage("PdfViewerNUPComboBoxOption.Custom"));

        paperDimensions.add(new Dimension(612, 792));
        paperDimensions.add(new Dimension(612, 1008));
        paperDimensions.add(new Dimension(792, 1224));
        paperDimensions.add(new Dimension(1224, 792));
        paperDimensions.add(new Dimension(1190, 1684));
        paperDimensions.add(new Dimension(842, 1190));
        paperDimensions.add(new Dimension(595, 842));
        paperDimensions.add(new Dimension(421, 595));
        paperDimensions.add(new Dimension(1002, 1418));
        paperDimensions.add(new Dimension(709, 1002));
        paperDimensions.add(new Dimension(501, 709));
        paperDimensions.add(new Dimension(612, 936));
        paperDimensions.add(new Dimension(396, 612));
        paperDimensions.add(new Dimension(540, 720));

        //paperSizesMap.put("Custom",null);
    }

    private String[] getPaperSizes() {
        return papers.toArray(new String[papers.size()]);
    }

    private Dimension getPaperDimension(final String paper) {
        if (paper.equals("Custom")) {
            return null;
        }

        return paperDimensions.get(papers.indexOf(paper));
    }

    @Override
    public final Dimension getPreferredSize() {
        return new Dimension(620, 680);
    }

}

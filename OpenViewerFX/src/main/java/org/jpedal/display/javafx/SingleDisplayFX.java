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
 * SingleDisplayFX.java
 * ---------------
 */
package org.jpedal.display.javafx;


import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Transform;
import org.jpedal.PdfDecoderFX;
import org.jpedal.display.Display;
import org.jpedal.display.GUIDisplay;
import org.jpedal.examples.viewer.commands.javafx.JavaFXPreferences;
import org.jpedal.examples.viewer.gui.JavaFxGUI;
import org.jpedal.external.Options;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.FXDisplay;

public class SingleDisplayFX extends GUIDisplay {

    final PdfDecoderFX pdf;

    // Rectangle drawn on screen by user
    private int[] cursorBoxOnScreen;

    final Pane cursorBoxPane = new Pane();

    public SingleDisplayFX(int pageNumber, final DynamicVectorRenderer currentDisplay, final PdfDecoderFX pdf, final DecoderOptions options) {

        if (pageNumber < 1) {
            pageNumber = 1;
        }

        this.pageNumber = pageNumber;
        this.currentDisplay = currentDisplay;
        this.pdf = pdf;

        this.options = options;

        displayOffsets = pdf.getDisplayOffsets();

        pageData = pdf.getPdfPageData();

    }

    public SingleDisplayFX(final PdfDecoderFX pdf, final DecoderOptions options) {

        this.pdf = pdf;

        this.options = options;

        displayOffsets = pdf.getDisplayOffsets();

        pageData = pdf.getPdfPageData();
    }

    @Override
    public void refreshDisplay() {

        if (Platform.isFxApplicationThread()) {
            if (displayScalingDbl != null) {
                pdf.getTransforms().setAll(Transform.affine(displayScalingDbl[0], displayScalingDbl[1], displayScalingDbl[2], displayScalingDbl[3], displayScalingDbl[4], displayScalingDbl[5]));
            }

            if (currentDisplay != null) {
                paintPage(pdf.highlightsPane, pdf.getFormRenderer());
            }
        } else {
            // Ensure dialog is handled on FX thread
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    if (displayScalingDbl != null) {
                        pdf.getTransforms().setAll(Transform.affine(displayScalingDbl[0], displayScalingDbl[1], displayScalingDbl[2], displayScalingDbl[3], displayScalingDbl[4], displayScalingDbl[5]));
                    }

                    if (currentDisplay != null) {
                        paintPage(pdf.highlightsPane, pdf.getFormRenderer());
                    }
                }
            });
        }
    }

    /**
     * initialise panel and set size to display during updates and update the AffineTransform to new values<br>
     *
     * @param newRotation int value to specify the rotation for the page
     */
    @Override
    public void setPageRotation(final int newRotation) {

        super.setPageRotation(newRotation);

        // force redraw if screen being cached
        refreshDisplay();
    }

    static Path getBorder(final int crw, final int crh) {

        final Path border = new Path();

        border.getElements().add(new MoveTo(-1, -1));
        border.getElements().add(new LineTo(crw + 2, -1));
        border.getElements().add(new LineTo(crw + 2, crh + 2));
        border.getElements().add(new LineTo(-1, crh + 2));
        border.getElements().add(new ClosePath());
        border.setStroke(Color.rgb(0, 0, 0));

        return border;

    }

    /**
     * Resets the FXPane when we open a new PDF
     */
    @Override
    public void disableScreen() {
        if (currentDisplay != null) {

            final Group FXpane = ((FXDisplay) currentDisplay).getFXPane();

            if (pdf.getChildren().contains(FXpane)) {

                final int count = pdf.getChildren().size();

                for (int i = 0; i < count; i++) {
                    pdf.getChildren().remove(0);
                }
            }
        }
    }

    private void paintPage(final Object rawBox, final AcroRenderer formRenderer) {
        final boolean debugPane = false;

        final Pane box = (Pane) rawBox;

        final Group fxPane = ((FXDisplay) currentDisplay).getFXPane();
        final String pageNumberStr = String.valueOf(pageNumber);

        final Rectangle clip = new Rectangle(crx, cry, crw, crh);
        clip.setFill(Color.WHITE);

        // Remove box from the current node it belongs to - avoids duplication errors
        if (box != null && box.getParent() != null) {
            ((Group) box.getParent()).getChildren().remove(box);
        }
        fxPane.getChildren().addAll(box);

        pdf.setPrefSize(crw, crh);

        if (displayView == SINGLE_PAGE) {
            pdf.getChildren().clear();

            if (formRenderer.isXFA()) {
                // Draw wihte background border on xfa contents
                final Path border = getBorder(crw, crh);
                border.setFill(Color.WHITE);
                pdf.getChildren().addAll(border);
                border.setLayoutX(crx);
                border.setLayoutY(cry);
            }

            if (!pdf.getChildren().contains(fxPane)) {
                pdf.getChildren().addAll(fxPane);
            }

            fxPane.setLayoutX(-crx);
            fxPane.setLayoutY(-cry);
        } else {

            Node pagePath = null;

            for (final Node child : pdf.getChildren()) {
                if (child.getId() != null && child.getId().equals(pageNumberStr)) {
                    if (child instanceof Path) {
                        pagePath = child;
                    }
                }
            }

            if (pagePath != null) {
                pdf.getChildren().remove(pagePath);
            }

            fxPane.setId(pageNumberStr);
            if (!pdf.getChildren().contains(fxPane)) {
                pdf.getChildren().addAll(fxPane);
            }

            final int[] xReached = multiDisplayOptions.getxReached();
            final int[] yReached = multiDisplayOptions.getyReached();

            int cx;
            final int cy;
            final int j = pageNumber;

            cx = (int) (xReached[j] / scaling);
            cy = (int) (yReached[j] / scaling);

            // Code works differently in Swing and FX so needs reversing
            if (displayView == CONTINUOUS_FACING) {
                cx = currentOffset.getWidestPageR() - cx;
            }

            fxPane.setLayoutX(-cx);
            fxPane.setLayoutY(pdf.getHeight() - cy);

        }

        if (!debugPane) {
            clip.setFill(Color.WHITE);
            fxPane.setClip(clip);
        } else {
            // Debug Different GUI Display Panes
            clip.setFill(Color.BLUE);
            clip.setOpacity(0.5);
            fxPane.getChildren().add(clip);

            pdf.setStyle("-fx-background-color: red;");
            pdf.getParent().setStyle("-fx-background-color: yellow;");
            fxPane.setStyle("-fx-background-color: green;");
        }

        addForms(formRenderer);


    }

    private void addForms(final AcroRenderer formRenderer) {
        int start = pageNumber, end = pageNumber;
        // Control if we display forms on multiple pages
        if (displayView != Display.SINGLE_PAGE) {
            start = getStartPage();
            end = getEndPage();
            if (start == 0 || end == 0 || lastEnd != end || lastStart != start) {
                lastFormPage = -1;
            }

            lastEnd = end;
            lastStart = start;

        }
        if ((lastFormPage != pageNumber) && (formRenderer != null)) {

            formRenderer.displayComponentsOnscreen(start, end);

            // Switch off if forms for this page found
            if (formRenderer.getCompData().hasformsOnPageDecoded(pageNumber)) {
                lastFormPage = pageNumber; //ensure not called too early
            }
        }
        // Add the forms to the Pane
        if (formRenderer != null && currentOffset != null) { // If all forms flattened, we can get a null value for currentOffset so avoid this case
            formRenderer.getCompData().setPageValues(scaling, displayRotation, (int) indent, displayOffsets.getUserOffsetX(), displayOffsets.getUserOffsetY(), displayView, currentOffset.getWidestPageNR(), currentOffset.getWidestPageR());
            formRenderer.getCompData().resetScaledLocation(scaling, displayRotation, (int) indent); // Indent here does nothing.
        }
    }

    @Override
    public void init(final float scaling, final int displayRotation, final int pageNumber, final DynamicVectorRenderer currentDisplay, final boolean isInit) {

        this.pageData = pdf.getPdfPageData();

        super.init(scaling, displayRotation, pageNumber, currentDisplay, isInit);

        setPageSize(pageNumber, scaling);

        lastFormPage = -1;

    }

    /**
     * Set the page size for the given page and scaling.
     * The scaling is not actually applied here. It is instead passed along to
     * used by FX transformations.
     *
     * @param pageNumber int value representing the page number
     * @param scaling    float value representing the scaling for the page
     */
    @Override
    public void setPageSize(final int pageNumber, final float scaling) {

        // Handle clip - crop box values
        pageData.setScalingValue(scaling); //ensure aligned
        topW = pageData.getCropBoxWidth(pageNumber);
        topH = pageData.getCropBoxHeight(pageNumber);
        final double mediaH = pageData.getMediaBoxHeight(pageNumber);

        cropX = pageData.getCropBoxX(pageNumber);
        cropY = pageData.getCropBoxY(pageNumber);
        cropW = topW;
        cropH = topH;

        // Actual clip values - for flipped page
        if (displayView == Display.SINGLE_PAGE) {
            crx = (int) (insetW + cropX);
            cry = (int) (insetH - cropY);
        } else {
            crx = insetW;
            cry = insetH;
        }

        // Amount needed to move cropped page into correct position
        int offsetY = (int) (mediaH - cropH);
        // Adjust the offset more in cases like costena
        if (!pageData.getMediaValue(pageNumber).isEmpty()) {
            offsetY -= pageData.getMediaBoxHeight(pageNumber) - (int) (cropY + cropH) - pageData.getCropBoxY(pageNumber);
        }

        crw = (int) (cropW);
        crh = (int) (cropH);

        cry += offsetY;
    }

    @Override
    public int[] getCursorBoxOnScreenAsArray() {
        return cursorBoxOnScreen;
    }

    @Override
    public void updateCursorBoxOnScreen(final int[] newOutlineRectangle, final int outlineColor, final int pageNumber, final int x_size, final int y_size) {

        if (displayView != Display.SINGLE_PAGE && getPageSize(displayView)[0] == 0 && getPageSize(displayView)[1] == 0) {
            return;
        }

        if (newOutlineRectangle != null) {

            final int x = newOutlineRectangle[0] - pageData.getCropBoxX(pageNumber);
            final int y = newOutlineRectangle[1] - pageData.getCropBoxY(pageNumber);
            final int w = newOutlineRectangle[2];
            final int h = newOutlineRectangle[3];

            cursorBoxOnScreen = new int[]{x, y, w, h};

            if (DecoderOptions.showMouseBox) {
                //Setup Cursor box.
                final Rectangle cursorRect = new Rectangle(x, y, w, h);
                cursorRect.setStroke(JavaFXPreferences.shiftColorSpaceToFX(outlineColor));
                cursorRect.setFill(Color.TRANSPARENT);

                //Draw Cursor box.
                if (pdf.getChildren().contains(cursorBoxPane)) {
                    cursorBoxPane.getChildren().clear();
                    cursorBoxPane.getChildren().add(cursorRect);
                    pdf.getChildren().remove(cursorBoxPane);
                }
                pdf.getChildren().add(cursorBoxPane);
            }
        } else {
            cursorBoxOnScreen = null;
            if (pdf.getChildren().contains(cursorBoxPane)) {
                cursorBoxPane.getChildren().clear();
                pdf.getChildren().remove(cursorBoxPane);
            }
        }
    }

    @Override
    public java.awt.Rectangle getDisplayedRectangle() {

        final ScrollPane customFXHandle = ((JavaFxGUI) pdf.getExternalHandler(Options.MultiPageUpdate)).getPageContainer();

        if (customFXHandle == null) {
            return new java.awt.Rectangle(0, 0, 0, 0);
        }

        final Bounds bounds = customFXHandle.getViewportBounds();

        return getDisplayedRectangle(true, new java.awt.Rectangle((int) bounds.getMinX(), (int) -bounds.getMinY(), (int) bounds.getWidth(), (int) bounds.getHeight()));
    }
}

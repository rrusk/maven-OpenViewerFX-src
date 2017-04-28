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
 * PattenPaint.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.Graphics2D;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.Serializable;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.Matrix;

public class PatternPaint implements PdfPaint, Serializable {

	private final int[][] imageArr;
	private final float[][] mm;
//	private float[][] sn = null;
//	private float[][] nn = null;
	private final float xStep;
	private final float yStep;
//	private final boolean isSingleImage;

	@Override
	public void setScaling(final double cropX, final double cropH, final float scaling, final float textX, final float textY) {
	}

	@Override
	public boolean isPattern() {
		return false;
	}

	@Override
	public boolean isTexture() {
		return true;
	}

	@Override
	public int getRGB() {
		return 0;
	}

	@Override
	public int getTransparency() {
		return 255;
	}

	public PatternPaint(final PdfObject patternObj, final byte[] streamData, final PatternColorSpace colorSpace) {
		final float[] inputs = patternObj.getFloatArray(PdfDictionary.Matrix);
		if (inputs != null) {
			mm = new float[][]{{inputs[0], inputs[1], 0f}, {inputs[2], inputs[3], 0f}, {inputs[4], inputs[5], 1f}};
		} else {
			mm = new float[][]{{1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f}};
		}

		final float[] rawBBox = patternObj.getFloatArray(PdfDictionary.BBox);
		final GeneralPath rawPath = new GeneralPath();
		rawPath.moveTo(rawBBox[0], rawBBox[1]);
		rawPath.lineTo(rawBBox[2], rawBBox[1]);
		rawPath.lineTo(rawBBox[2], rawBBox[3]);
		rawPath.lineTo(rawBBox[0], rawBBox[3]);
		rawPath.lineTo(rawBBox[0], rawBBox[1]);
		rawPath.closePath();

		Rectangle2D rectBBox = rawPath.getBounds2D();
		int iw = (int) Math.round(rectBBox.getWidth());
		int ih = (int) Math.round(rectBBox.getHeight());

		if (rectBBox.getWidth() > 3000 || rectBBox.getHeight() > 3000) {
			iw = 3000;
			ih = 3000;
		}

		float[][] sn = {{1, 0f, 0f}, {0f, 1f, 0f}, {(float) -rectBBox.getX(), (float) -rectBBox.getY(), 1f}};
		final BufferedImage image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
		final ObjectStore localStore = new ObjectStore();
		PatternDisplay glyphDisplay = colorSpace.decodePatternContent(patternObj, sn, streamData, localStore);
		final Graphics2D g2 = image.createGraphics();
		glyphDisplay.setG2(g2);
		glyphDisplay.paint(null, null, null);
		float rawXStep = patternObj.getFloatNumber(PdfDictionary.XStep);
		float rawYStep = patternObj.getFloatNumber(PdfDictionary.YStep);
		xStep = rawXStep;
		yStep = rawYStep;

		imageArr = new int[ih][iw];
		final int[] imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		int p = 0;
		for (int i = 0; i < ih; i++) {
			for (int j = 0; j < iw; j++) {
				imageArr[i][j] = imageData[p++];
			}
		}
	}

	@Override
	public PaintContext createContext(final ColorModel cm, final Rectangle deviceBounds, final Rectangle2D userBounds, final AffineTransform xform, final RenderingHints hints) {
		return new PatternTileContext(imageArr, xform, userBounds, mm);
	}

	private class PatternTileContext implements PaintContext {

		private final int[][] imagePixels;
		private final float[][] toUserSpace;
		private final float[][] fromPatternToUserSpace;

		PatternTileContext(final int[][] imageArr, final AffineTransform xform, final Rectangle2D rectBBox, float[][] mmm) {
			this.imagePixels = imageArr;
			final float[][] xformMatrix = {
				{(float) xform.getScaleX(), (float) xform.getShearX(), 0},
				{(float) xform.getShearY(), (float) xform.getScaleY(), 0},
				{(float) xform.getTranslateX(), (float) xform.getTranslateY(), 1}
			};
			toUserSpace = Matrix.inverse(xformMatrix);
			fromPatternToUserSpace = Matrix.inverse(mmm);
		}

		@Override
		public Raster getRaster(final int x, final int y, final int w, final int h) {
			final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			final int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
			final int iw = imagePixels[0].length;
			final int ih = imagePixels.length;
			int p = 0;
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					float[] src = {x + j, y + i};
					src = Matrix.transformPoint(toUserSpace, src[0], src[1]);
					src = Matrix.transformPoint(fromPatternToUserSpace, src[0], src[1]);
					final float xx = src[0];
					final float yy = src[1];
					pixels[p++] = calculatePixels(imagePixels, iw, ih, xStep, yStep, xx, yy);
				}
			}
			return img.getRaster();
		}

		@Override
		public void dispose() {
		}

		@Override
		public ColorModel getColorModel() {
			return ColorModel.getRGBdefault();
		}
	}

	static int calculatePixels(final int[][] imagePixels, final int iw, final int ih, final float xStep, final float yStep, final float xx, final float yy) {
		final float x = (xx);
		final float y = (yy);
		float x_ = x % xStep;
		float y_ = y % yStep;
		if (y < 0) {
			y_ = yStep + y_;
		}
		if (x < 0) {
			x_ = xStep + x_;
		}

		final int bx = (int) x_;
		final int by = (int) y_;

		if (bx > -1 && by > -1 && bx < iw && by < ih) {
			return imagePixels[by][bx];
		}
		return 0;
	}

}

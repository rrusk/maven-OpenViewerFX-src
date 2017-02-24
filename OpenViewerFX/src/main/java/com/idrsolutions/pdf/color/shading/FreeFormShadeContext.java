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
 * FreeFormShadeContext.java
 * ---------------
 */
package com.idrsolutions.pdf.color.shading;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.function.PDFFunction;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.Matrix;

public class FreeFormShadeContext implements PaintContext {

	private final GenericColorSpace shadingColorSpace;

	private final float[] background;
	//final double textX;
	//final double textY;

	private final float[][] toUserSpace;
	private final float[][] toShadeSpace;
	private final int bitsPerComponent;
	private final int bitsPerFlag;
	private final int bitsPerCoordinate;
	private final float[] decodeArr;
	private final PDFFunction[] function;
	private final int nComp;

	private final List<float[]> triCoords;
	private final List<Color> triColors;
	private final int triCount;

	public FreeFormShadeContext(final AffineTransform xform, final GenericColorSpace shadingColorSpace, final float[] background, final PdfObject shadingObject, final float[][] mm, final PDFFunction[] function) {

		this.shadingColorSpace = shadingColorSpace;

		this.bitsPerComponent = shadingObject.getInt(PdfDictionary.BitsPerComponent);
		this.bitsPerFlag = shadingObject.getInt(PdfDictionary.BitsPerFlag);
		this.bitsPerCoordinate = shadingObject.getInt(PdfDictionary.BitsPerCoordinate);
		this.decodeArr = shadingObject.getFloatArray(PdfDictionary.Decode);
		this.function = function;
		this.background = background;
		this.nComp = (this.decodeArr.length - 4) / 2;

		float[][] shadeMatrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
		if (mm != null) {
			shadeMatrix = mm;
		}

		final float[][] xformMatrix = {
			{(float) xform.getScaleX(), (float) xform.getShearX(), 0},
			{(float) xform.getShearY(), (float) xform.getScaleY(), 0},
			{(float) xform.getTranslateX(), (float) xform.getTranslateY(), 1}
		};
		toUserSpace = Matrix.inverse(xformMatrix);
		toShadeSpace = Matrix.inverse(shadeMatrix);

		final boolean hasSmallBits = bitsPerFlag < 8 || bitsPerComponent < 8 || bitsPerCoordinate < 8;
		final BitReader reader = new BitReader(shadingObject.getDecodedStream(), hasSmallBits);

		final double bitCoordScaling = 1.0 / ((1L << bitsPerCoordinate) - 1);
		final double bitCompScaling = 1.0 / ((1L << bitsPerComponent) - 1);

		final List<Point2D> trianglesPoints = new ArrayList<Point2D>();
		triColors = new ArrayList<Color>();

		while (reader.getPointer() < reader.getTotalBitLen()) {
			final int flag = reader.getPositive(bitsPerFlag);
			final Point2D p = getPointCoords(reader, bitCoordScaling, bitsPerCoordinate, decodeArr);
			final Color c = getPointColor(reader, bitCompScaling, bitsPerComponent);
			switch (flag) {
				case 0:
					break;
				case 1:
					final Point2D[] temp = new Point2D[2];
					temp[0] = trianglesPoints.get(trianglesPoints.size() - 2);
					temp[1] = trianglesPoints.get(trianglesPoints.size() - 1);
					trianglesPoints.addAll(Arrays.asList(temp));

					final Color[] tc = new Color[2];
					tc[0] = triColors.get(triColors.size() - 2);
					tc[1] = triColors.get(triColors.size() - 1);
					triColors.addAll(Arrays.asList(tc));
					break;
				case 2:
					final Point2D[] ff = new Point2D[2];
					ff[0] = trianglesPoints.get(trianglesPoints.size() - 3);
					ff[1] = trianglesPoints.get(trianglesPoints.size() - 1);
					trianglesPoints.addAll(Arrays.asList(ff));

					final Color[] fc = new Color[2];
					fc[0] = triColors.get(triColors.size() - 3);
					fc[1] = triColors.get(triColors.size() - 1);
					triColors.addAll(Arrays.asList(fc));
					break;
			}
			trianglesPoints.add(p);
			triColors.add(c);
		}

		triCoords = new ArrayList<float[]>();
		for (final Point2D trianglesPoint : trianglesPoints) {
			final float[] xy = new float[2];
			xy[0] = (float) trianglesPoint.getX();
			xy[1] = (float) trianglesPoint.getY();
			triCoords.add(xy);
		}
		triCount = triCoords.size() / 3;

	}

	@Override
	public void dispose() {

	}

	@Override
	public ColorModel getColorModel() {
		return ColorModel.getRGBdefault();
	}

	private static Point2D getPointCoords(final BitReader reader, final double bitScaling, final int bps, final float[] decode) {
		final long x_ = reader.readBitsAsLong(bps);
		final long y_ = reader.readBitsAsLong(bps);
		final double x = x_ * bitScaling * (decode[1] - decode[0]) + decode[0];
		final double y = y_ * bitScaling * (decode[3] - decode[2]) + decode[2];
		return new Point2D.Double(x, y);
	}

	private Color getPointColor(final BitReader reader, final double bitScaling, final int bps) {
		final float[] components = new float[nComp];
		for (int i = 0, j = 4; i < nComp; i++, j += 2) {
			final long ci = reader.readBitsAsLong(bps);
			components[i] = (float) (ci * bitScaling * (decodeArr[j + 1] - decodeArr[j]) + decodeArr[j]);
		}
		return calculateColor(components);
	}

	@Override
	public Raster getRaster(final int startX, final int startY, final int w, final int h) {

		final int rastSize = (w * h * 4);
		final int[] data = new int[rastSize];

		if (background != null) {
			shadingColorSpace.setColor(background, 4);
			final Color c = (Color) shadingColorSpace.getColor();
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					final int base = (i * w + j) * 4;
					data[base] = c.getRed();
					data[base + 1] = c.getGreen();
					data[base + 2] = c.getBlue();
					data[base + 3] = 255;
				}
			}
		}

		float x, y, x1, y1, x2, y2, x3, y3;
		float[] temp;
		int r, g, b;

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				float[] src = {startX + j, startY + i};
				src = Matrix.transformPoint(toUserSpace, src[0], src[1]);
				src = Matrix.transformPoint(toShadeSpace, src[0], src[1]);

				x = src[0];
				y = src[1];

				for (int t = 0; t < triCount; t++) {
					final int p = t * 3;

					temp = triCoords.get(p);
					x1 = temp[0];
					y1 = temp[1];

					temp = triCoords.get(p + 1);
					x2 = temp[0];
					y2 = temp[1];

					temp = triCoords.get(p + 2);
					x3 = temp[0];
					y3 = temp[1];

					if (isInTriangle(x, y, x1, y1, x2, y2, x3, y3)) { //shapes.get(t).contains(x, y)) {//

						final Color c1 = triColors.get(p);
						final Color c2 = triColors.get(p + 1);
						final Color c3 = triColors.get(p + 2);

						final float a = areaTriangle(x1, y1, x2, y2, x3, y3);
						final float a1 = areaTriangle(x, y, x1, y1, x2, y2);
						final float a2 = areaTriangle(x, y, x1, y1, x3, y3);
						final float a3 = areaTriangle(x, y, x2, y2, x3, y3);

						r = (int) ((a1 / a) * c3.getRed() + (a2 / a) * c2.getRed() + (a3 / a) * c1.getRed());
						g = (int) ((a1 / a) * c3.getGreen() + (a2 / a) * c2.getGreen() + (a3 / a) * c1.getGreen());
						b = (int) ((a1 / a) * c3.getBlue() + (a2 / a) * c2.getBlue() + (a3 / a) * c1.getBlue());

						final int base = (i * w + j) * 4;
						data[base] = r;
						data[base + 1] = g;
						data[base + 2] = b;
						data[base + 3] = 255;
						break;
					}
				}
			}
		}

		final WritableRaster raster = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB).getRaster();
		raster.setPixels(0, 0, w, h, data);
		return raster;

	}

	private Color calculateColor(final float[] val) {
		final Color col;
		if (function != null) {
			final float[] colValues = ShadingFactory.applyFunctions(function, val);
			shadingColorSpace.setColor(colValues, colValues.length);
			col = (Color) shadingColorSpace.getColor();
		} else {
			shadingColorSpace.setColor(val, val.length);
			col = (Color) shadingColorSpace.getColor();
		}
		return col;
	}

	public float areaTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) {
		return Math.abs((x1 - x3) * (y2 - y1) - (x1 - x2) * (y3 - y1));
	}

	private static boolean isInTriangle(final float x, final float y, final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) {
		final float dX = x - x3;
		final float dY = y - y3;
		final float dX21 = x3 - x2;
		final float dY12 = y2 - y3;
		final float D = dY12 * (x1 - x3) + dX21 * (y1 - y3);
		final float s = dY12 * dX + dX21 * dY;
		final float t = (y3 - y1) * dX + (x1 - x3) * dY;
		if (D < 0) {
			return s <= 0 && t <= 0 && s + t >= D;
		}
		return s >= 0 && t >= 0 && s + t <= D;
	}

}

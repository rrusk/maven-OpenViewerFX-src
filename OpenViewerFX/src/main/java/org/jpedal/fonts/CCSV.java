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
 * ccsv.java
 * ---------------
 */
package org.jpedal.fonts;

/**
 * CID CHAR STRING VALIDATION
 */
public class CCSV {

	String id;
	int min;
	boolean stackClearing;
	boolean resetStack;
	boolean undefStack;
	boolean stem;

	Integer stackDelta;
	boolean hasFunction;

	public CCSV(String id, int min, boolean stackClearing, boolean resetStack, boolean undefStack, boolean stem, Integer stackDelta, boolean hasFunction) {
		this.id = id;
		this.min = min;
		this.stackClearing = stackClearing;
		this.resetStack = resetStack;
		this.undefStack = undefStack;
		this.stem = stem;
		this.stackDelta = stackDelta;
		this.hasFunction = hasFunction;
	}

	@Override
	public String toString() {
		return "CCSV{" + "id=" + id + ", min=" + min + ", stackClearing="
				+ stackClearing + ", resetStack=" + resetStack + ", undefStack="
				+ undefStack + ", stem=" + stem + ", stackDelta=" + stackDelta
				+ ", hasFunction=" + hasFunction + '}';
	}

	public void performFunction(String id, double[] stack, int si) {
		if (id.equals("add")) {
			stack[si - 2] += stack[si - 1];
		} else if (id.equals("sub")) {
			stack[si - 2] -= stack[si - 1];
		} else if (id.equals("div")) {
			stack[si - 2] /= stack[si - 1];
		} else if (id.equals("neg")) {
			stack[si - 1] = -stack[si - 1];
		} else if (id.equals("mul")) {
			stack[si - 2] *= stack[si - 1];
		}
	}

}

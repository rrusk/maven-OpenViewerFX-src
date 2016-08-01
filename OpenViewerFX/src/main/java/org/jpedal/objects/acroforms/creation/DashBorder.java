/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 * <p>
 * Project Info:  http://www.jpedal.org
 * <p>
 * (C) Copyright 2007, IDRsolutions and Contributors.
 * <p>
 * This file is part of JPedal
 *
 *     This library is free software; you can redistribute it and/or
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

 ---------------
 * <p>
 * DashBorder.java
 * ---------------
 * (C) Copyright 2007, by IDRsolutions and Contributors.
 * <p>
 * <p>
 * --------------------------
 */
package org.jpedal.objects.acroforms.creation;

import java.awt.*;
import javax.swing.border.LineBorder;

class DashBorder extends LineBorder {

    //make getters and setters for stroke as exercise
    Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{5, 5}, 10);

    DashBorder(final Stroke stroke, final Color borderColor) {

        super(borderColor);
        this.stroke=stroke;
    }

    @Override
    public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(stroke);

        super.paintBorder(c, g2d, x, y, width, height);
        g2d.dispose();
    }
}

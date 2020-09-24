package org.mitre.treemap;

import java.awt.*;
import javax.swing.SizeSequence;

/**
 * A TreeMapRectangle wraps a TreeMapNode and displays it.
 * It also maps x, y coordinates to rectangles so a mouse
 * listener can identify the rectangle to which it points.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
class TreeMapRectangle
{
    // dimensions of the rectangle and the height of text used for the label.
    private int x, y, width, height, textHeight;
    private boolean highlight;
    private boolean selected;
    private SizeSequence seq;	// sizes of the children of this rectangle

    private final TreeMapNode node; // node wrapped by this rectangle
    private final boolean horizontal;
    private final int hgap;
    private final int vgap;
    private final int childCount;
    private final TreeMapRectangle[] children;

    TreeMapRectangle(TreeMapNode node, boolean horizontal,
		     int hgap, int vgap) {
	this.node = node;
	this.horizontal = horizontal;
	this.hgap = hgap;
	this.vgap = vgap;
	childCount = node.getChildCount();
	children = new TreeMapRectangle[childCount];
	for (int i = 0; i < childCount; i++)
	    children[i] = new TreeMapRectangle(node.getChildAt(i),
					       !horizontal, hgap, vgap);
    }

    /**
     * Paint everything in this rectangle.
     */
    void paint(Graphics g, Color background) {
	if (isSmallBounds())
	    return;
	Color color = node.getColor();
	g.setColor(color == null ? background : color);
	g.setClip(x, y, width, height);
	g.fillRect(x, y, width, height);
	for (int i = 0; i < childCount; i++)
	    children[i].paint(g, background);
	update(g);
    }

    /**
     * Paint this rectangle only.  Does not paint the background.
     */
    void update(Graphics g) {
	if (isSmallBounds())
	    return;
	g.setClip(x, y, width, height);
	g.setColor(highlight || selected ? Color.red: Color.black);
	g.drawRect(x, y, width - 1, height -1);
	String label = node.getLabel();
	if (label != null)
	    g.drawString(label, x + hgap, y + vgap + textHeight);
    }

    /**
     * Returns the inner most rectangle at the coordinate x0, y0.
     */
    TreeMapRectangle getRectangleAt(int x0, int y0) {
	if (seq == null)
	    return this;
	else if (horizontal) {
	    if (y0 < y + 2 * vgap + textHeight)	// check vertically first
		return this;
	    else if (y0 >= y + height - vgap)
		return this;
	    else {		// check horizontally using the size sequence
		int index = seq.getIndex(x0);
		if ((index & 1) == 1) // a child is present
		    return children[index >> 1].getRectangleAt(x0, y0);
		else
		    return this;
	    }
	}
	else {			// as above but rotated
	    if (x0 < x + hgap)
		return this;
	    else if (x0 >= x + width - hgap)
		return this;
	    else {
		int index = seq.getIndex(y0);
		if ((index & 1) == 1)
		    return children[index >> 1].getRectangleAt(x0, y0);
		else
		    return this;
	    }
	}
    }
	
    TreeMapNode getNode() {
	return node;
    }

    void setHighlight(boolean highlight) {
	this.highlight = highlight;
    }

    void setSelected(boolean selected) {
	this.selected = selected;
    }

    /**
     * Sets this rectangle's bounds and its children.
     */
    void setBounds(int x, int y, int width, int height, int textHeight) {
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
	this.textHeight = textHeight;
	if (width <= 0 || height <= 0 || node.getWeight() <= 0.0f) {
	    setSmallBounds();
	    return;
	}

	if (childCount <= 0)
	    return;

	if (seq == null)
	    seq = new SizeSequence();

	// Set the bounds assuming all children are big enough.
	setBoundsFiltered(childCount, false);

	int n = 0;
	for (int i = 0; i < childCount; i++) {
	    if (!children[i].isSmallBounds())
		n++;
	}

	// Set the bounds ignoring children that are too small.
	if (n != childCount && n > 0)
	    setBoundsFiltered(n, true);

	computeSequence();	// Get ready for the mouse.
    }

    /**
     * Set the bounds of the children.
     */
    private void setBoundsFiltered(int n, boolean filter) {
	double invWeight = 1.0 / node.getWeight();
	int x0 = x + hgap;	// x0, y0 forms the origin of children
	int y0 = y + 2 * vgap + textHeight;

	if (horizontal) {
	    // Length is the size that can be allocated to the children.
	    int length = width - hgap * (n + 1);
	    int h0 = height - 3 * vgap - textHeight;
	    for (int i = 0; i < childCount; i++)
		if (!filter || !children[i].isSmallBounds()) {
		    // Compute the ratio of the length for this child.
		    double ratio
			= children[i].getNode().getWeight() * invWeight;
		    int w0 = (int)Math.floor(length * ratio);
		    children[i].setBounds(x0, y0, w0, h0, textHeight);
		    x0 += w0 + hgap;
		}
	}
	else {			// as above but rotated
	    int length = height - vgap * (n + 2) - textHeight;
	    int w0 = width - 2 * hgap;
	    for (int i = 0; i < childCount; i++)
		if (!filter || !children[i].isSmallBounds()) {
		    double ratio
			= children[i].getNode().getWeight() * invWeight;
		    int h0 = (int)Math.floor(length * ratio);
		    children[i].setBounds(x0, y0, w0, h0, textHeight);
		    y0 += h0 + vgap;
		}
	}
    }

    /**
     * When child is too small, zero it out.
     */
    private void setSmallBounds() {
	this.width = 0;
	this.height = 0;
	seq = null;
	for (int i = 0; i < childCount; i++) {
	    children[i].setBounds(0, 0, 0, 0, textHeight);
	}
    }

    private boolean isSmallBounds() {
	return width <= 1 || height <= 1;
    }

    /**
     * Computes the size sequence for getRectangleAt(int, int).
     */
    private void computeSequence() {
	int[] sizes = new int[2 * childCount + 1];
	if (horizontal) {
	    sizes[0] = x + hgap;
	    for (int i = 0; i < childCount; i++) {
		TreeMapRectangle child = children[i];
		int j = 2 * i + 1;
		if (!child.isSmallBounds()) {
		    sizes[j] = child.width; // odd j is child width
		    sizes[j + 1] = hgap; // even j is gap
		} // else size[j] = size[j + 1] = 0
	    }
	    sizes[2 * childCount] = width; // okay to overestimate width
	}
	else {			// as above but rotated
	    sizes[0] = y + 2 * vgap + textHeight;
	    for (int i = 0; i < childCount; i++) {
		TreeMapRectangle child = children[i];
		int j = 2 * i + 1;
		if (!child.isSmallBounds()) {
		    sizes[j] = child.height;
		    sizes[j + 1] = vgap;
		}
	    }
	    sizes[2 * childCount] = height;
	}
	seq.setSizes(sizes);
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append(TreeMapRectangle.class.getName());
	sb.append(":");
	if (node.getLabel() != null) {
	    sb.append(" node=");
	    sb.append(node.getLabel());
	    sb.append(",");
	}
	sb.append(" childCount=");
	sb.append(childCount);
	sb.append(", x=");
	sb.append(x);
	sb.append(", y=");
	sb.append(y);
	sb.append(", width=");
	sb.append(width);
	sb.append(", height=");
	sb.append(height);
	if (horizontal)
	    sb.append(", horizontal orientation");
	else
	    sb.append(", vertical orientation");
	return sb.toString();
    }
}

/******************************************************************
A Tree-Map Viewer in Swing.
Copyright (C) 2001 The MITRE Corporation

This library is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2 of the
License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA
*******************************************************************/

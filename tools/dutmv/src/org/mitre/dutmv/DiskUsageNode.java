package org.mitre.dutmv;

import java.awt.Color;
import org.mitre.treemap.TreeMapNode;

/**
 * An obvious implementation of a tree map node for the disk usage
 * application.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
class DiskUsageNode
    implements TreeMapNode
{
    private final String s;
    private float w = 0.0f;
    private Color color;
    private DiskUsageNode[] children;
    private DiskUsageNode parent;

    DiskUsageNode(String s) {
	this.s = s;
    }

    public String getLabel() {
	return s;
    }

    public float getWeight() {
	return w;
    }

    void setWeight(long weight) {
	w = (float)weight;
    }

    void setColor(Color color) {
	this.color = color;
    }

    public Color getColor() {
	return color;
    }

    public int getChildCount() {
	if (children == null)
	    return 0;
	else
	    return children.length;
    }

    public TreeMapNode getChildAt(int i) {
	if (children == null)
	    return null;
	try {
	    return children[i];
	}
	catch (IndexOutOfBoundsException ioobe) {
	    return null;
	}
    }

    public TreeMapNode getParent() {
	return parent;
    }

    void setParent(DiskUsageNode parent) {
	this.parent = parent;
    }

    void setChildren(DiskUsageNode[] children) {
	this.children = children;
    }

    /**
     * Do nothing runner.
     */
    public void run() {
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

package org.mitre.treemap;

/**
 * A TreeMapNode is the interface for nodes displayed by the tree-map
 * algorithm.  The run method is invoked when the tree node is double
 * clicked.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
public interface TreeMapNode
    extends Runnable
{
    /**
     * Get text associated with a node.
     * @return the text
     */
    String getLabel();

    /**
     * Get the weight associated with a node.  The weight must be a
     * positive number.  It also must be no less than the sum of the
     * weights of the children of this node.
     * @return the weight
     */
    float getWeight();

    /**
     * Get the color associated with a node.  The color is used as the
     * background for the panel associated with the node.
     * @return the color or null to use the default background
     */
    java.awt.Color getColor();

    /**
     * Get the child at the given index.
     * @param childIndex index of child
     * @return a node or null on bad index
     */
    TreeMapNode getChildAt(int childIndex);

    /**
     * Get the number of children.
     * @return child count
     */
    int getChildCount();

    /**
     * Get the parent of this node.
     * @return a node or null
     */
    TreeMapNode getParent();
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

package org.mitre.treemap;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The TreeMapComponent class implements a visual representation of a
 * weighted tree using a 2-D space-filling approach and area to show a
 * node's weight.  See Ben Shneiderman, "Tree Visualization with
 * Tree-Maps: A 2-D Space-filling Approach", ACM Transactions on
 * Graphics (TOG), Vol. 11, No. 1, Pages 92-99, Jan., 1992.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
class TreeMapComponent
    extends JComponent
    implements ItemSelectable
{
    private int width;
    private int height;

    private final JLabel status; // place for messages
    private TreeMapNode node;	// root node for display
    private TreeMapRectangle rect; // wrapper for the root node
    private boolean horizontal;
    private int hgap;
    private int vgap;

    TreeMapComponent(JLabel status, TreeMapNode node,
		     boolean horizontal, int hgap, int vgap) {
	this.status = status;
	this.horizontal = horizontal;
	this.hgap = hgap;
	this.vgap = vgap;
	setNode(node);
	addMouseListener(new TreeMapMouseListener(this));
    }

    /**
     * Paint component by painting the rectangle for the root node.
     */
    public void paintComponent(Graphics g) {
	Color background = getBackground();
	g.setColor(background);
	Dimension d = getSize();
	if (d.width != width || d.height != height) {
	    width = d.width;
	    height = d.height;
	    // recompute bounds on size change
	    rect.setBounds(hgap, 0, width - 2 * hgap, height,
			   g.getFontMetrics().getHeight());
	}
	g.setClip(0, 0, width, height);
	g.fillRect(0, 0, width, height);
	rect.paint(g, background);
    }

    /**
     * Get the root rectangle.
     */
    TreeMapRectangle getRootRectangle() {
	return rect;
    }

    /**
     * Get the root node.
     */
    TreeMapNode getRootNode() {
	return node;
    }

    /**
     * Set the tree root for display.
     * This must be called in the AWT event thread.
     */
    void setRoot(TreeMapNode node) {
	setNode(node);
	repaint();
    }

    private void setNode(TreeMapNode node) {
	setSelected(null);
	if (node == null)
	    node = new EmptyTree("Nil");
	this.node = node;
	rect = new TreeMapRectangle(node, horizontal, hgap, vgap);
	width = height = 0;
    }	

    /**
     * Show a message in the status line.  If there is no message,
     * show the selected node.  The message must not be the empty
     * string or the size of the status line will shrink.  Use " " for
     * empty messages.
     */
    void showStatus(String msg) {
	if (msg != null && msg.length() > 0)
	    status.setText(msg);
	else if (selected == null) {
	    status.setText(" ");
	}
	else {
	    String label = selected.getNode().getLabel();
	    if (label != null && label.length() > 0)
		status.setText(label);
	    else
		status.setText(" ");
	}
    }

    private TreeMapRectangle selected;

    /**
     * Set the selected rectangle.  The mouse listener calls this.
     */
    void setSelected(TreeMapRectangle selected) {
	if (this.selected == selected)
	    return;
	if (this.selected != null) {
	    this.selected.setSelected(false);
	    fireItemEvent(this.selected, false);
	}
	this.selected = selected;
	if (selected != null) {
	    selected.setSelected(true);
	    fireItemEvent(selected, true);
	}
	showStatus(null);
	repaint();
    }

    /**
     * Get the selected node.
     */
    TreeMapNode getSelected() {
	if (selected == null)
	    return null;
	else
	    return selected.getNode();
    }

    /**
     * Returns the selected tree map node in the form required by
     * ItemSelectable.
     */
    public Object[] getSelectedObjects() {
	if (selected == null)
	    return null;
	else
	    return new TreeMapNode[] { selected.getNode() };
    }

    private Vector<ItemListener> itemListeners =
	new Vector<ItemListener>();

    /**
     * Adds an item listener
     */
    public synchronized void addItemListener(ItemListener il) {
	itemListeners.addElement(il);
    }

    /**
     * Removes an item listener
     */
    public synchronized void removeItemListener(ItemListener il) {
	itemListeners.removeElement(il);
    }

    private void fireItemEvent(TreeMapRectangle rect, boolean selected) {
	int stateChange = selected ? ItemEvent.SELECTED : ItemEvent.DESELECTED;
	ItemEvent e = new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
				    rect.getNode(), stateChange);
	Vector l;
	synchronized (this) {
	    l = (Vector)itemListeners.clone();
	}
	for (int i = 0; i < l.size(); i++) {
	    ((ItemListener)l.elementAt(i)).itemStateChanged(e);
	}
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

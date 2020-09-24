package org.mitre.treemap;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A mouse listener for tree maps.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
class TreeMapMouseListener
    extends MouseInputAdapter
{
    // When true, highlights node under the pointer.
    // When false, shows label of node under the pointer in the status line.
    private final static boolean HIGHLIGHT = false;
    private TreeMapRectangle highlight;

    private final TreeMapComponent comp;

    TreeMapMouseListener(TreeMapComponent comp) {
	this.comp = comp;
    }

    private void setHighlight(TreeMapRectangle highlight) {
	if (this.highlight == highlight)
	    return;
	if (this.highlight != null)
	    this.highlight.setHighlight(false);
	this.highlight = highlight;
	if (highlight != null) {
	    highlight.setHighlight(true);
	    comp.showStatus(highlight.getNode().getLabel());
	}
	else
	    comp.showStatus(null);
	comp.repaint();
    }

    public void mouseEntered(MouseEvent e) {
	comp.addMouseMotionListener(this);
    }

    public void mouseExited(MouseEvent e) {
	comp.removeMouseMotionListener(this);
	if (HIGHLIGHT)
	    setHighlight(null);
	else
	    comp.showStatus(null);
    }

    public void mouseMoved(MouseEvent e) {
	TreeMapRectangle rect = comp.getRootRectangle();
	if (HIGHLIGHT)
	    setHighlight(rect.getRectangleAt(e.getX(), e.getY()));
	else {
	    rect = rect.getRectangleAt(e.getX(), e.getY());
	    if (rect != null)
		comp.showStatus(rect.getNode().getLabel());
	    else
		comp.showStatus(null);
	}
    }

    public void mousePressed(MouseEvent e) {
	TreeMapRectangle rect = comp.getRootRectangle();
	rect = rect.getRectangleAt(e.getX(), e.getY());
	if (e.getClickCount() > 1) {
	    if (rect != null)
		rect.getNode().run();
	}
	else
	    comp.setSelected(rect);
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

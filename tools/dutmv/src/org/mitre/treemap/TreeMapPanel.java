package org.mitre.treemap;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A panel for tree maps.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
public class TreeMapPanel
    extends JPanel
    implements ItemSelectable
{
    /**
     * The {@link javax.swing.ActionMap ActionMap} key for the tree
     * map go action.
     */
    public final static String GO_ACTION
	= "treemap-go";
    /**
     * The {@link javax.swing.ActionMap ActionMap} key for the tree
     * map stop action.
     */
    public final static String STOP_ACTION
	= "treemap-stop";
    /**
     * The {@link javax.swing.ActionMap ActionMap} key for the tree
     * map show children action.
     */
    public final static String SHOW_CHILDREN_ACTION
	= "treemap-show-children";
    /**
     * The {@link javax.swing.ActionMap ActionMap} key for the tree
     * map set root action.
     */
    public final static String SET_ROOT_ACTION
	= "treemap-set-root";
    /**
     * The {@link javax.swing.ActionMap ActionMap} key for the tree
     * map make parent root action.
     */
    public final static String MAKE_PARENT_ROOT_ACTION
	= "treemap-make-parent-root";
    /**
     * The {@link javax.swing.ActionMap ActionMap} key for the tree
     * map pop root stack action.
     */
    public final static String POP_ROOT_STACK_ACTION
	= "treemap-pop-root-stack";
    /**
     * The {@link javax.swing.ActionMap ActionMap} key for the tree
     * map restore root action.
     */
    public final static String RESTORE_ROOT_ACTION
	= "treemap-restore-root";
    /**
     * The {@link javax.swing.ActionMap ActionMap} key for the tree
     * map help action.
     */
    public final static String HELP_ACTION
	= "treemap-help";

    private final TreeMapComponent comp;
    private final TreeMapActions actions;

    /**
     * Create a tree map panel with a status line.
     * @param factory creator of root nodes (can be null)
     * @param node the root node to be displayed (can be null)
     * @param horizontal true if top-level layed out horizontally
     * @param hgap horizontal length in pixels between rectangles
     * @param vgap vertical length in pixels between rectangles
     */
    public TreeMapPanel(TreeMapFactory factory, TreeMapNode node,
			boolean horizontal, int hgap, int vgap) {
	setLayout(new BorderLayout());
	JLabel status = new JLabel(" ");
	status.setBorder(new EmptyBorder(vgap, hgap, 0, hgap));
	add(status, BorderLayout.SOUTH);
	comp = new TreeMapComponent(status, node, horizontal, hgap, vgap);
	add(comp);
	actions = new TreeMapActions(getActionMap(), factory, comp);
	addItemListener(actions);
    }

    /**
     * Gets the root node being displayed.
     * @return root node
     */
    public TreeMapNode getRoot() {
	return comp.getRootNode();
    }

    /**
     * Changes the root node for display.
     * @param node new root node
     */
    public void setRoot(final TreeMapNode node) {
	if (SwingUtilities.isEventDispatchThread()) {
	    actions.setRoot(node);
	    comp.setRoot(node);
	}
	else
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			setRoot(node);
		    }
		});
    }

    /**
     * Get the selected tree map node.
     * @return an array of one tree map node or null
     */
    public Object[] getSelectedObjects() {
	return comp.getSelectedObjects();
    }

    /**
     * Add a listener for tree map node selections.
     * @param il item listener
     */
    public synchronized void addItemListener(ItemListener il) {
	comp.addItemListener(il);
    }

    /**
     * Remove a listener for tree map node selections.
     * @param il item listener
     */
    public synchronized void removeItemListener(ItemListener il) {
	comp.removeItemListener(il);
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

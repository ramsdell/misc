package org.mitre.treemap;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

/**
 * A tool bar for tree maps.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
public class TreeMapToolBar
    extends JToolBar
{
    private final static String EXIT_ACTION = "treemap-exit";

    private static class ExitAction
	extends TreeMapActions.TreeMapAction
    {
	ExitAction() {
	    super(EXIT_ACTION);
	}

	public void actionPerformed(ActionEvent e) {
	    System.exit(0);
	}
    }

    /**
     * Create a tool bar with tree map actions.
     * @param actions action map containing the tree map actions
     * @param hgap the horizontal gap
     * @param vgap the vertical gap
     */
    public TreeMapToolBar(ActionMap actions, int hgap, int vgap) {
	setBorder(new EmptyBorder(vgap, hgap, vgap, hgap));
	add(actions.get(TreeMapPanel.GO_ACTION));
	add(Box.createHorizontalStrut(hgap));
	add(actions.get(TreeMapPanel.STOP_ACTION));
	add(Box.createHorizontalStrut(hgap));
	add(actions.get(TreeMapPanel.SHOW_CHILDREN_ACTION));
	add(Box.createHorizontalStrut(hgap));
	add(actions.get(TreeMapPanel.SET_ROOT_ACTION));
	add(Box.createHorizontalStrut(hgap));
	add(actions.get(TreeMapPanel.MAKE_PARENT_ROOT_ACTION));
	add(Box.createHorizontalStrut(hgap));
	add(actions.get(TreeMapPanel.POP_ROOT_STACK_ACTION));
	add(Box.createHorizontalStrut(hgap));
	add(actions.get(TreeMapPanel.RESTORE_ROOT_ACTION));
	add(Box.createHorizontalStrut(hgap));
	add(new ExitAction());
	add(Box.createHorizontalGlue());
	add(actions.get(TreeMapPanel.HELP_ACTION));
    }

    /**
     * Add an action to this tool bar and set the mnemonic.
     */
    public JButton add(Action action) {
	JButton button = super.add(action);
	Integer mnemonic = (Integer)action.getValue(Action.MNEMONIC_KEY);
	if (mnemonic != null)
	    button.setMnemonic(mnemonic.intValue());
	return button;
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

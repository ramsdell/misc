package org.mitre.treemap;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Stack;
import java.util.EmptyStackException;

/**
 * Actions associated with a tree map panel.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
class TreeMapActions
    implements ItemListener
{
    private final JPopupMenu childrenPopup = new JPopupMenu("Children");
    private final TreeMapFactory factory;
    private final TreeMapComponent comp;
    private final Stack<TreeMapNode> roots =
	new Stack<TreeMapNode>(); // Stack of roots
    private TreeMapNode root;	// Root of the tree being displayed
    private TreeMapNode base;	// Original tree given by setRoot
    private Thread t;

    TreeMapActions(ActionMap am, TreeMapFactory factory,
		   TreeMapComponent comp) {
	this.factory = factory;
	this.comp = comp;
	comp.add(childrenPopup);
	comp.addItemListener(this);
	base = root = comp.getRootNode();
	initMessages();
	initActions(am);
    }

    // Load messages

    private final static String SHOW_MORE
	= "treemap-show-more";
    private final static String ACTIVITY_RUNNING_MESSAGE
	= "treemap-activity-running";
    private final static String ACTIVITY_STARTED_MESSAGE
	= "treemap-activity-started";
    private final static String ACTIVITY_FINISHED_MESSAGE
	= "treemap-activity-finished";
    private final static String ACTIVITY_ABORTED_MESSAGE
	= "treemap-activity-aborted";
    private final static String ACTIVITY_CRASHED_MESSAGE
	= "treemap-activity-crashed";
    private final static String ACTIVITY_NOT_THERE_MESSAGE
	= "treemap-activity-not-there";
    private final static String ACTIVITY_INTERRUPTED_MESSAGE
	= "treemap-activity-interrupted";
    private final static String HELP_MESSAGE
	= "treemap-help-message";

    private final static String MISSING_MESSAGE
	= "internal error: message missing from resources";
    private String showMore;
    private String activityRunningMessage;
    private String activityStartedMessage;
    private String activityFinishedMessage;
    private String activityAbortedMessage;
    private String activityCrashedMessage;
    private String activityNotThereMessage;
    private String activityInterruptedMessage;
    private String helpMessage;

    private String initMessage(String key) {
	return TreeMapResources.getResourceString(key, MISSING_MESSAGE);
    }

    private void initMessages() {
	showMore = initMessage(SHOW_MORE);
	activityRunningMessage = initMessage(ACTIVITY_RUNNING_MESSAGE);
	activityStartedMessage = initMessage(ACTIVITY_STARTED_MESSAGE);
	activityFinishedMessage = initMessage(ACTIVITY_FINISHED_MESSAGE);
	activityAbortedMessage = initMessage(ACTIVITY_ABORTED_MESSAGE);
	activityCrashedMessage = initMessage(ACTIVITY_CRASHED_MESSAGE);
	activityNotThereMessage = initMessage(ACTIVITY_NOT_THERE_MESSAGE);
	activityInterruptedMessage = initMessage(ACTIVITY_INTERRUPTED_MESSAGE);
	activityRunningMessage = initMessage(ACTIVITY_RUNNING_MESSAGE);
	helpMessage = initMessage(HELP_MESSAGE);
    }

    // Define the actions performed.

    abstract static class TreeMapAction
	extends AbstractAction
    {
	TreeMapAction(String command) {
	    putValue(Action.ACTION_COMMAND_KEY, command);
	    String key = command + NAME_SUFFIX;
	    String value = TreeMapResources.getResourceString(key, null);
	    if (value == null || value.length() == 0)
		value = "?";
	    putValue(Action.NAME, value);
	    key = command + MNEMONIC_SUFFIX;
	    value = TreeMapResources.getResourceString(key, null);
	    if (value == null || value.length() == 0)
		return;
	    putValue(Action.MNEMONIC_KEY, Integer.valueOf(value.charAt(0)));
	}

	void putInMap(ActionMap am) {
	    String command = (String)getValue(Action.ACTION_COMMAND_KEY);
	    if (command == null)
		return;
	    am.put(command, this);
	}
    }

    private final TreeMapAction goAction
	= new TreeMapAction(TreeMapPanel.GO_ACTION) {
		public void actionPerformed(ActionEvent e) {
		    go();
		}
	    };

    private final TreeMapAction stopAction
	= new TreeMapAction(TreeMapPanel.STOP_ACTION) {
		public void actionPerformed(ActionEvent e) {
		    stop();
		}
	    };

    private final TreeMapAction showChildrenAction
	= new TreeMapAction(TreeMapPanel.SHOW_CHILDREN_ACTION) {
		public void actionPerformed(ActionEvent e) {
		    showChildren();
		}
	    };

    private TreeMapAction setRootAction
	= new TreeMapAction(TreeMapPanel.SET_ROOT_ACTION) {
		public void actionPerformed(ActionEvent e) {
		    makeSelectionRoot();
		}
	    };

    private final TreeMapAction makeParentRootAction
	= new TreeMapAction(TreeMapPanel.MAKE_PARENT_ROOT_ACTION) {
		public void actionPerformed(ActionEvent e) {
		    makeParentRoot();
		}
	    };

    private final TreeMapAction popRootStackAction
	= new TreeMapAction(TreeMapPanel.POP_ROOT_STACK_ACTION) {
		public void actionPerformed(ActionEvent e) {
		    popRootStack();
		}
	    };

    private final TreeMapAction restoreRootAction
	= new TreeMapAction(TreeMapPanel.RESTORE_ROOT_ACTION) {
		public void actionPerformed(ActionEvent e) {
		    restoreRoot();
		}
	    };

    private final TreeMapAction helpAction
	= new TreeMapAction(TreeMapPanel.HELP_ACTION) {
		public void actionPerformed(ActionEvent e) {
		    help();
		}
	    };

    // Load action attributes

    private final static String NAME_SUFFIX = "-name";
    private final static String MNEMONIC_SUFFIX = "-mnemonic";
    private final static String ROWS_SUFFIX = "-rows";
    private final static String X_SUFFIX = "-x";
    private final static String Y_SUFFIX = "-y";

    private final static int DEFAULT_MAX_ROWS = 20;
    private final static int DEFAULT_SHOW_X = 40;
    private final static int DEFAULT_SHOW_Y = 25;
    private int maxRows;
    private int showX;
    private int showY;

    private void initActions(ActionMap am) {
	goAction.putInMap(am);
	stopAction.putInMap(am);
	showChildrenAction.putInMap(am);
	setRootAction.putInMap(am);
	makeParentRootAction.putInMap(am);
	popRootStackAction.putInMap(am);
	restoreRootAction.putInMap(am);
	helpAction.putInMap(am);
	if (factory == null) {
	    goAction.setEnabled(false);
	}
	stopAction.setEnabled(false);
	showChildrenAction.setEnabled(false);
	setRootAction.setEnabled(false);
	makeParentRootAction.setEnabled(root.getParent() != null);
	popRootStackAction.setEnabled(false);
	restoreRootAction.setEnabled(false);
	// Special setup for the show children action
	String key = TreeMapPanel.SHOW_CHILDREN_ACTION + ROWS_SUFFIX;
	maxRows = TreeMapResources.getResourceInt(key, DEFAULT_MAX_ROWS);
	key = TreeMapPanel.SHOW_CHILDREN_ACTION + X_SUFFIX;
	showX = TreeMapResources.getResourceInt(key, DEFAULT_SHOW_X);
	key = TreeMapPanel.SHOW_CHILDREN_ACTION + Y_SUFFIX;
	showY = TreeMapResources.getResourceInt(key, DEFAULT_SHOW_Y);
    }

    // methods called by actions

    void setRoot(TreeMapNode node) {
	base = root = node;
	roots.clear();
	boolean hasParent = node != null && node.getParent() != null;
	makeParentRootAction.setEnabled(hasParent);
	popRootStackAction.setEnabled(false);
	restoreRootAction.setEnabled(false);
    }

    private void showChildren() {
	childrenPopup.removeAll();	
	TreeMapNode selected = comp.getSelected();
	if (selected == null)
	    return;
	int childCount = selected.getChildCount();
	if (childCount <= 0)
	    return;
	JComponent menu = childrenPopup;
	int i = 0;
	for (;;) {
	    int n = Math.min(childCount, i + maxRows);
	    while (i < n)
		menu.add(makeShowChildrenItem(selected.getChildAt(i++)));
	    if (i == childCount)
		break;
	    JMenu more = new JMenu(showMore);
	    menu.add(more);
	    menu = more;
	}
	childrenPopup.show(comp, showX, showY);
    }
	
    private JMenuItem makeShowChildrenItem(final TreeMapNode node) {
	String label = node.getLabel();
	JMenuItem mi = new JMenuItem(label == null ? "" : label);
	mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    changeRoot(node);
		}
	    });
	return mi;
    }

    private void go() {
	if (factory == null)
	    return;
	if (t != null && t.isAlive()) {
	    comp.showStatus(activityRunningMessage);
	    return;
	}
	goAction.setEnabled(false);
	stopAction.setEnabled(true);
	t = new Thread(new Runnable() {
		public void run() {
		    TreeMapNode tree = null;
		    String msg;
		    try {
			tree = factory.createTree();
			if (tree != null)
			    msg = activityFinishedMessage;
			else
			    msg = activityAbortedMessage;
		    }
		    catch (Throwable thr) {
			msg = activityCrashedMessage + ": " + thr.getMessage();
		    }
		    invokeDone(tree, msg);
		}
	    });
	comp.showStatus(activityStartedMessage);
	t.start();
    }

    private void invokeDone(final TreeMapNode node, final String msg) {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    done(node, msg);
		}
	    });
    }

    private void done(TreeMapNode node, String msg) {
	comp.showStatus(msg);
	if (node != null) {
	    setRoot(node);
	    comp.setRoot(node);
	}
	goAction.setEnabled(true);
	stopAction.setEnabled(false);
    }

    private void stop() {
	if (t == null || !t.isAlive()) {
	    t = null;
	    comp.showStatus(activityNotThereMessage);
	    goAction.setEnabled(true);
	    stopAction.setEnabled(false);
	}
	else {
	    comp.showStatus(activityInterruptedMessage);
	    t.interrupt();
	}
    }

    private void changeRoot(TreeMapNode node) {
	if (node == null || root == node)
	    return;
	if (root != null) {
	    roots.push(root);
	    popRootStackAction.setEnabled(true);
	}
	root = node;
	makeParentRootAction.setEnabled(node.getParent() != null);
	restoreRootAction.setEnabled(node != base);
	comp.setRoot(node);
    }

    private void popRootStack() {
	try {
	    TreeMapNode node = (TreeMapNode)roots.pop();
	    if (roots.empty())
		popRootStackAction.setEnabled(false);
	    root = node;
	    makeParentRootAction.setEnabled(node.getParent() != null);
	    restoreRootAction.setEnabled(node != base);
	    comp.setRoot(node);
	} catch (EmptyStackException e) {
	}
    }

    private void makeSelectionRoot() {
	changeRoot(comp.getSelected());
    }

    private void makeParentRoot() {
	changeRoot(root.getParent());
    }

    private void restoreRoot() {
	changeRoot(base);
    }

    private void help() {
	String tag = TreeMapVersion.getVersion();
	if (tag != null && tag.length() > 0) {
	    StringBuffer sb = new StringBuffer(helpMessage);
	    sb.append(" [");
	    sb.append(tag);
	    sb.append("]");
	    comp.showStatus(sb.toString());
	}
	else
	    comp.showStatus(helpMessage);
    }

    // receive selection events from the tree map component.

    public void itemStateChanged(ItemEvent e) {
	boolean selected = e.getStateChange() == ItemEvent.SELECTED;
	boolean notRoot = false;
	boolean hasChildren = false;
	if (selected) {
	    TreeMapNode node = comp.getSelected();
	    if (node != null) {
		notRoot = node != root;
		hasChildren = node.getChildCount() > 0;
	    }
	}
	setRootAction.setEnabled(notRoot);
	showChildrenAction.setEnabled(hasChildren);
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

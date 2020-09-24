package org.mitre.dutmv;

import java.util.Arrays;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.mitre.treemap.*;

/**
 * The Disk Usage Tree Map Viewer application.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
public class DiskUsage
    implements TreeMapFactory
{
    private final static int WIDTH = 800;
    private final static int HEIGHT = 500;
    private final static int HGAP = 5;
    private final static int VGAP = 5;
    private final static Color FILE_COLOR = Color.lightGray;
    private final static Color DIRECTORY_COLOR = Color.white;

    private final JFileChooser chooser;
    private final JProgressBar progress;
    private final JFrame frame;

    DiskUsage(File directory, JProgressBar progress, JFrame frame) {
	chooser = new JFileChooser();
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	chooser.setSelectedFile(directory);
	this.progress = progress;
	this.frame = frame;
    }

    /**
     * Create a tree that shows disk usage.  The method uses a dialog
     * to select a directory.  It then creates a tree with a node for
     * each directory and file in the directory.  The weight of each
     * file is the size of the file.  The weight of each directory is
     * the sum of the size of each file in the directory plus the
     * weights of each directory in the directory.
     */
    public TreeMapNode createTree() {
	int result = chooser.showOpenDialog(frame);
	if (result != JFileChooser.APPROVE_OPTION)
	    return null;
	File directory = chooser.getSelectedFile();
	try {
	    progress.setIndeterminate(true);
	    return createDiskUsageTree(directory);
	}
	catch (InterruptedException ie) {
	    return null;
	}
	finally {
	    progress.setIndeterminate(false);
	}
    }

    private static DiskUsageNode createDiskUsageTree(File file)
	throws InterruptedException
    {
	DiskUsageNode parent = new DiskUsageNode(file.getName());
	long weight = file.length(); // create base weight
	if (file.isDirectory() && isCanonical(file)) {
	    parent.setColor(DIRECTORY_COLOR);
	    if (Thread.interrupted())
		throw new InterruptedException("Interrupt detected by polling");
	    File[] kids = file.listFiles(); // get children
	    if (kids != null	// This should always be the case, right?
		&& kids.length > 0) {
		Arrays.sort(kids); // sort by file name
		DiskUsageNode[] children = new DiskUsageNode[kids.length];
		for (int i = 0; i < kids.length; i++) {
		    DiskUsageNode kid = createDiskUsageTree(kids[i]);
		    kid.setParent(parent);
		    weight += kid.getWeight(); // add in child's weight
		    children[i] = kid;
		}
		parent.setChildren(children);
	    }
	}
	else
	    parent.setColor(FILE_COLOR);
	parent.setWeight(weight);
	return parent;
    }

    /**
     * This method is used to prevent loops.
     * The analysis is done only on canononical files.
     */
    private static boolean isCanonical(File file) {
	try {
	    return file.equals(file.getCanonicalFile());
	}
	catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * This is the main entry point for the Disk Usage Tree Map Viewer.
     */
    public static void main(String[] args) {
	String pathname = ".";
	switch (args.length) {
	case 0:
	    break;
	case 1:
	    pathname = args[0];
	    break;
	default:
	    System.out.println("Usage: java " + DiskUsage.class.getName()
			       + " [pathname]");
	    System.exit(1);
	}

	try {
	    File file = new File(pathname).getCanonicalFile();
	    final JFrame frame = new JFrame("Disk Usage");
	    JProgressBar progress = new JProgressBar();
	    DiskUsage du = new DiskUsage(file, progress, frame);
	    JPanel panel = new JPanel();
	    panel.setLayout(new BorderLayout());
	    panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
	    TreeMapPanel treeMap
		= new TreeMapPanel(du, null, true, HGAP, VGAP);
	    panel.add(treeMap, BorderLayout.CENTER);
	    TreeMapToolBar toolBar
		= new TreeMapToolBar(treeMap.getActionMap(), HGAP, VGAP);
	    panel.add(toolBar, BorderLayout.NORTH);
	    panel.add(progress, BorderLayout.SOUTH);
	    frame.getContentPane().add(panel);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    EventQueue.invokeLater(new Runnable() {
		    public void run() {
			frame.pack();
			frame.setVisible(true);
		    }
		});
	}
	catch (Throwable t) {
	    t.printStackTrace();
	    System.exit(1);
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

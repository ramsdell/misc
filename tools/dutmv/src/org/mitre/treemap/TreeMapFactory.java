package org.mitre.treemap;

/**
 * A TreeMapFactory is the interface for a tree creator.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */
public interface TreeMapFactory
{
    /**
     * Create a tree for viewing.  A return value of null signals
     * failure.  The view will ignore a null return value and continue
     * displaying the previous tree.  An implementation of this method
     * should poll the current thread for interrupts, and return null
     * when an interrupt is detected.
     * @return the new tree or null on failure
     */
    TreeMapNode createTree();
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

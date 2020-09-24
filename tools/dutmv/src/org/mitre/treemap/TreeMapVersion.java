package org.mitre.treemap;

import java.util.*;

/**
 * Version numbers for tree maps.
 *
 * @version November 2003
 * @author John D. Ramsdell
 */

public final class TreeMapVersion
{
    private TreeMapVersion() { } // Everything of interest is static

    private static ResourceBundle resources;

    private final static String VERSION_RESOURCES
	= "org/mitre/treemap/version";

    /**
     * Get program name and a version number from a resource.
     */
    public static String getVersion() {
	if (resources == null)
	    resources = ResourceBundle.getBundle(VERSION_RESOURCES);
	String program = resources.getString("program");
	String version = resources.getString("version");
	return program + " " + version;
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

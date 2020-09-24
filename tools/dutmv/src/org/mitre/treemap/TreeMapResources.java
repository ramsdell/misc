package org.mitre.treemap;

import java.util.*;

/**
 * Resources for tree maps.
 *
 * @version October 2001
 * @author John D. Ramsdell
 */

public final class TreeMapResources
{
    private TreeMapResources() { } // Everything of interest is static

    private static ResourceBundle resources;

    private final static String TREE_MAP_RESOURCES
	= "org/mitre/treemap/treemap";

    /**
     * Get a string from a ResourceBundle.
     * @param nm name of key
     * @return resource as a string
     * @throws MissingResourceException when unable to find resource
     */
    static String getResourceString(String nm)
	throws MissingResourceException
    {
	if (resources == null)
	    resources = ResourceBundle.getBundle(TREE_MAP_RESOURCES);
	return resources.getString(nm);
    }

    /**
     * Get a string from a ResourceBundle.
     * @param nm name of key
     * @param defaultValue value on error
     * @return resource as a string or default value on error
     */
    static String getResourceString(String nm, String defaultValue) {
	try {
	    return getResourceString(nm);
	}
	catch (MissingResourceException mre) {
	    return defaultValue;
	}
    }

    /**
     * Get an integer from a ResourceBundle.
     * @param nm name of key
     * @param defaultValue value on error
     * @return resource as an integer or default value on error
     */
    static int getResourceInt(String nm, int defaultValue) {
	String str = getResourceString(nm, null);
	if (str == null)
	    return defaultValue;
	try {
	    return Integer.parseInt(str);
	}
	catch (NumberFormatException ex) {
	    return defaultValue;
	}
    }

    /**
     * Get a boolean from a ResourceBundle.
     * @param nm name of key
     * @return resource as a boolean or the false value on error
     */
    static boolean getResourceBoolean(String nm) {
	String str = getResourceString(nm, null);
	return str != null && str.toLowerCase().equals("true");
    }

    /**
     * Take the given string and chop it up into a series
     * of strings on whitespace boundries.  This is useful
     * for trying to get an array of strings out of the
     * resource file.
     */
    static String[] tokenize(String input) {
	StringTokenizer t = new StringTokenizer(input);
	Vector<String> v = new Vector<String>();
	while (t.hasMoreTokens())
	    v.addElement(t.nextToken());
	String[] tokenized = new String[v.size()];
	return (String[])v.toArray(tokenized);
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

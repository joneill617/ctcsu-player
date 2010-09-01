/*
This source code file is part of the CASAA Treatment Coding System Utility
    Copyright (C) 2009  UNM CASAA

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.unm.casaa.main;

import java.util.HashMap;

import javax.swing.Action;

// Wrapper for a String->Action map.  Asserts input and output is non-null.
public class ActionTable {
	private HashMap< String, Action > map = new HashMap< String, Action >();

	public void	put( String key, Action action ) {
		assert( action != null );
		assert( key != null );
		map.put( key, action );
	}

	// Get action mapped to given name.
	public Action get( String key ) {
		assert( key != null );
		Action result = map.get( key );

		assert( result != null ) : key;
		return result;
	}
};

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

package edu.unm.casaa.globals;

public enum GlobalCode { 
	ACCEPTANCE( "Acceptance", 0 ),
	EMPATHY( "Empathy", 1 ),
	DIRECTION( "Direction", 2 ),
	AUTONOMY( "Autonomy Support", 3 ),
	COLLABORATION( "Collaboration", 4 ),
	EVOCATION( "Evocation", 5 ),
	SELF_EXPLORATION( "Self Exploration", 6 );

	public final int 		value;
	public final String		label; // Human-readable name.

	GlobalCode( String label, int value ) {
		this.label = label;
		this.value = value;
	}

	/**
	 * Get initial (default) value for this code.
	 */
	public int defaultValue() {
		if( this == SELF_EXPLORATION ) {
			return 1;
		} else {
			return 3;
		}
	}
};

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

package edu.unm.casaa.misc;

import java.util.Vector;

import edu.unm.casaa.main.MainController;

// MiscCode associates a label, such as "CR+/-" or "ADP", with a numeric value.
public class MiscCode { 
	private static final long serialVersionUID 	= 1L;

	public static final int 			INVALID			= 0;
	public static final MiscCode		INVALID_CODE	= new MiscCode();

	// List of available codes.  Built when we parse XML file.
	private static Vector< MiscCode >	list	= new Vector< MiscCode >();

	public int 			value	= INVALID;
	public String		label	= "";

	// Class:

	// Add new code.  Returns true on success, shows warning dialog on failure.
	public static boolean	addCode( int value, String label ) {
		MiscCode newCode = new MiscCode( value, label );

		// Check that we're not duplicating an existing value or label.
		for( int i = 0; i < list.size(); i++ ) {
			MiscCode code = list.get( i );

			if( code.value == newCode.value || code.label.equals( newCode.label ) ) {
				MainController.instance.showWarning(
						"User Code Error",
						"New code " + 
						newCode.toDisplayString() + " conflicts with existing code " + code.toDisplayString() );
				return false;
			}
		}
		list.add( newCode );
		return true;
	}

	public static int	numCodes() {
		return list.size();
	}

	// PRE: index < numCodes().
	public static MiscCode codeAtIndex( int index ) {
		return list.get( index );
	}

	// PRE: code exists with given value.
	public static MiscCode codeWithValue( int value ) {
		// Check known codes.
		if( value == INVALID_CODE.value ) {
			return INVALID_CODE;
		}
		// Check user codes.
		for( int i = 0; i < list.size(); i++ ) {
			MiscCode code = list.get( i );

			if( code.value == value ) {
				return code;
			}
		}
		assert false : "Code with given value not found: " + value;
		return null;
	}

	// PRE: code exists with given label.
	public static MiscCode codeWithLabel( String label ) {
		// Check known codes.
		if( label.equals( INVALID_CODE.value ) ) {
			return INVALID_CODE;
		}
		// Check user codes.
		for( int i = 0; i < list.size(); i++ ) {
			MiscCode code = list.get( i );

			if( code.label.equals( label ) ) {
				return code;
			}
		}
		assert false : "Code with given label not found: " + label;
		return null;
	}

	// Instance:

	public MiscCode( int value, String label ) {
		this.value = value;
		this.label = label;
	}

	public MiscCode() {
	}

	public boolean isValid() {
		return value != INVALID;
	}

	// Get string representation for use in user dialogs.
	public String toDisplayString() {
		return "(label: " + label + ", value: " + value + ")";
	}
};

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import edu.unm.casaa.globals.GlobalCode;

/**
 * Stores globals data.
 * @author amanuel
 */
public class GlobalDataModel {

	// Map of GlobalCode to value.
	private HashMap< Integer, Integer > valueMap = new HashMap< Integer, Integer >();

	public GlobalDataModel() {
		// Initialize to zero.
		for( GlobalCode g : GlobalCode.values() ) {
			valueMap.put( g.value, 0 );
		}
	}

	public int getValue( GlobalCode code ) {
		Integer result = valueMap.get( code.value );

		assert( result != null );
		return result.intValue();
	}

	public void	setValue( GlobalCode code, int value ) {
		valueMap.put( new Integer( code.value ), value );
	}

	public String toString() {
		String result = new String();
		
		for( GlobalCode g : GlobalCode.values() ) {
			result += g.toString() + ":\t" + getValue( g ) + "\n";
		}
		return result;
	}

	public void writeToFile( File file, String filenameAudio, String notes ) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter( new FileWriter( file, false ) );
			writer.println( "Global Ratings\n" );
			writer.println( "Audio File:\t" + filenameAudio );
			writer.println( toString() );
			if( !"".equals( notes ) ) {
				writer.println( "Notes:\n" + notes );
			}
		} catch( IOException e ) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
}

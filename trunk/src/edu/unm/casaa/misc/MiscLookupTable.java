/*
This source code file is part of the CASAA Treatment Coding System Utility
    Copyright (C) 2009  Alex Manuel amanuel@unm.edu

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

/**
 * An abstract class to provide MISC utterances a lookup table for their
 * code's name abbreviations.  This is to assist in providing human
 * readable output.
 * @author Alex Manuel
 *
 */
public abstract class MiscLookupTable {

	/**
	 * Returns the string containing the MISC code's name abbreviation.
	 * @param code the MISC statistical code number
	 * @return the code's string abbreviation
	 */
	public String getCodeAbbrv(int code){
		//therapist codes
		if( code == 1 )  {return "ADP";}
		if( code == 2 )  {return "ADW";}
		if( code == 3 )  {return "AF";}
		if( code == 4 )  {return "CO";}
		if( code == 5 )  {return "DI";}
		if( code == 6 )  {return "EC";}
		if( code == 7 )  {return "FA";}
		if( code == 8 )  {return "FI";}
		if( code == 9 )  {return "GI";}
		if( code == 10 ) {return "CQ-";}
		if( code == 11 ) {return "CQ0";}
		if( code == 12 ) {return "CQ+";}
		if( code == 13 ) {return "OQ-";}
		if( code == 14 ) {return "OQ0";}
		if( code == 15 ) {return "OQ+";}
		if( code == 16 ) {return "RCP";}
		if( code == 17 ) {return "RCW";}
		if( code == 18 ) {return "SR-";}
		if( code == 19 ) {return "SR0";}
		if( code == 20 ) {return "SR+";}
		if( code == 21 ) {return "CR-";}
		if( code == 22 ) {return "CR0";}
		if( code == 23 ) {return "CR+";}
		if( code == 24 ) {return "RF";}
		if( code == 25 ) {return "SU";}
		if( code == 26 ) {return "ST";}
		if( code == 27 ) {return "WA";}
			
		//client codes
		if( code == 30 ) {return "C+";}
		if( code == 31 ) {return "C-";}
		if( code == 32 ) {return "R+";}
		if( code == 33 ) {return "R-";}
		if( code == 34 ) {return "D+";}
		if( code == 35 ) {return "D-";}
		if( code == 36 ) {return "A+";}
		if( code == 37 ) {return "A-";}
		if( code == 38 ) {return "N+";}
		if( code == 39 ) {return "N-";}
		if( code == 40 ) {return "TS+";}
		if( code == 41 ) {return "TS-";}
		if( code == 42 ) {return "O+";}
		if( code == 43 ) {return "O-";}
		if( code == 44 ) {return "FN";}
		if( code == 50 ) {return "NC";}
		
		return "Error Retrieving Code";
	}

	public int getCodeVal(String code) {
		//therapist codes
		if( code == "ADP" )		{return 1;}
		if( code == "ADW" )		{return 2;}
		if( code == "AF" ) 		{return 3;}
		if( code == "CO" )  	{return 4;}
		if( code == "DI" )  	{return 5;}
		if( code == "EC" )  	{return 6;}
		if( code == "FA" )  	{return 7;}
		if( code == "FI" )  	{return 8;}
		if( code == "GI" )  	{return 9;}
		if( code == "CQ-" )		{return 10;}
		if( code == "CQ0" ) 	{return 11;}
		if( code == "CQ+" ) 	{return 12;}
		if( code == "OQ-" ) 	{return 13;}
		if( code == "OQ0" ) 	{return 14;}
		if( code == "OQ+" ) 	{return 15;}
		if( code == "RCP" ) 	{return 16;}
		if( code == "RCW" ) 	{return 17;}
		if( code == "SR-" ) 	{return 18;}
		if( code == "SR0" ) 	{return 19;}
		if( code == "SR+" ) 	{return 20;}
		if( code == "CR-" ) 	{return 21;}
		if( code == "CR0" ) 	{return 22;}
		if( code == "CR+" ) 	{return 23;}
		if( code == "RF" ) 		{return 24;}
		if( code == "SU" ) 		{return 25;}
		if( code == "ST" ) 		{return 26;}
		if( code == "WA" ) 		{return 27;}
			
		//client codes
		if( code == "C+" ) 		{return 30;}
		if( code == "C-" ) 		{return 31;}
		if( code == "R+" ) 		{return 32;}
		if( code == "R-" ) 		{return 33;}
		if( code == "D+" ) 		{return 34;}
		if( code == "D-" ) 		{return 35;}
		if( code == "A+" ) 		{return 36;}
		if( code == "A-" ) 		{return 37;}
		if( code == "N+" ) 		{return 38;}
		if( code == "N-" ) 		{return 39;}
		if( code == "TS+" ) 	{return 40;}
		if( code == "TS-" ) 	{return 41;}
		if( code == "O+" ) 		{return 42;}
		if( code == "O-" ) 		{return 43;}
		if( code == "FN" ) 		{return 44;}
		if( code == "NC" ) 		{return 50;}

		// on error
		return 0;
	}
	
}

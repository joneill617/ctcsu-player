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

public enum MiscCode{ 
	// Uninitialized code.
	INVALID( "INVALID", 0 ),

	// Therapist codes.
	ADP( "ADP", 1 ),
	ADW( "ADW", 2 ),
	AF( "AF", 3 ),
	CO( "CO", 4 ),
	DI( "DI", 5 ),
	EC( "EC", 6 ),
	FA( "FA", 7 ),
	FI( "FI", 8 ),
	GI( "GI", 9 ),
	CQ_MINUS( "CQ-", 10 ),
	CQ_NEUTRAL( "CQ0", 11 ),
	CQ_PLUS( "CQ+", 12 ),
	OQ_MINUS( "OQ-", 13 ),
	OQ_NEUTRAL( "OQ0", 14 ),
	OQ_PLUS( "OQ+", 15 ),
	RCP( "RCP", 16 ),
	RCW( "RCW", 17 ),
	SR_MINUS( "SR-", 18 ),
	SR_NEUTRAL( "SR0", 19 ),
	SR_PLUS( "SR+", 20 ),
	CR_MINUS( "CR-", 21 ),
	CR_NEUTRAL( "CR0", 22 ),
	CR_PLUS( "CR+", 23 ),
	RF( "RF", 24 ),
	SU( "SU", 25 ),
	ST( "ST", 26 ),
	WA( "WA", 27 ),

	// Client codes.
	C_PLUS( "C+", 30 ),
	C_MINUS( "C-", 31 ),
	R_PLUS( "R+", 32 ),
	R_MINUS( "R-", 33 ),
	D_PLUS( "D+", 34 ),
	D_MINUS( "D-", 35 ),
	A_PLUS( "A+", 36 ),
	A_MINUS( "A-", 37 ),
	N_PLUS( "N+", 38 ),
	N_MINUS( "N-", 39 ),
	TS_PLUS( "TS+", 40 ),
	TS_MINUS( "TS-", 41 ),
	O_PLUS( "O+", 42 ),
	O_MINUS( "O-", 43 ),
	FN( "FN", 44 ),
	NC( "NC", 50 );

	private final int 		value;
	private final String	label;

	MiscCode( String label, int value ) {
		this.label = label;
		this.value = value;
	}

	public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
};

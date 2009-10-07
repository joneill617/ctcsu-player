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

package edu.unm.casaa.utterance;

public interface Utterance {

	public int getEnum();
	public String getStartTime();	
	public String getEndTime();
	public int getStartBytes();
	public int getEndBytes();
	public int getCodeVal();
	public String getCodeAbbrv(int codeVal);
	
	//these should be set on initialization
	//public int setEnum();	
	//public int setStartTime();	
	public void setEndTime(String end);	
	public void setEndBytes(int bytes);
	public void setCodeVal(int code);
	public void setCodeVal(String code);
	
	//output order should be tab-delimited:
	//order startCode endCode [codeCode codeString] "\r\n"
	//the section in [] is only if utterance has been coded
	public String writeParsed();
	public String writeCoded();
	
}

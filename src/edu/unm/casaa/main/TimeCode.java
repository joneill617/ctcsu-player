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

import java.util.StringTokenizer;

public class TimeCode {
	
	private int seconds			= 0;
	private int minutes			= 0;
	private int hours			= 0;
	private String timestamp	= null;
	
	public TimeCode(int secs){
		seconds = secs;
	}
	
	public TimeCode(String time){
		timestamp = time;
	}

	public String convertToTimeString(){
		hours = seconds / 3600;
		minutes = (seconds / 60) - (hours * 60);
		seconds = seconds - (hours * 3600) - (minutes * 60);
		
		String hour = new Integer(hours).toString();
		String mins = new Integer(minutes).toString();
		String secs = new Integer(seconds).toString();
		
		if( minutes < 10 )	{mins = "0" + mins;}
		if( seconds < 10 )	{secs = "0" + secs;}	
		
		return (hour + ":" + mins + ":" + secs);
	}
	
	public int convertTimeCodeStringToSecs(){
		StringTokenizer st = new StringTokenizer(timestamp, ":");
		hours 	= new Integer(st.nextToken()).intValue() * 3600;
		minutes = new Integer(st.nextToken()).intValue() * 60;
		return (hours + minutes) + new Integer(st.nextToken()).intValue();
	}
}

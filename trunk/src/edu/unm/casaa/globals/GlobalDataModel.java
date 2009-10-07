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

/**
 * Stores globals data.
 * @author amanuel
 *
 */
public class GlobalDataModel {

	private int acceptance;
	private int empathy;
	private int direction;
	private int autonomy;
	private int collaboration;
	private int evocation;
	private int selfExploration;

	public GlobalDataModel(){
		init();
	}

	private void init(){
		//init to GUI settings to start
	}

	//===============================================================
	// getters and setters
	//===============================================================

	public int getAcceptance(){
		return acceptance;
	}

	public void setAcceptance(int data){
		acceptance = data;
	}

	public int getEmpathy(){
		return empathy;
	}

	public void setEmpathy(int data){
		empathy = data;
	}

	public int getDirection(){
		return direction;
	}

	public void setDirection(int data){
		direction = data;
	}

	public int getAutonomy(){
		return autonomy;
	}

	public void setAutonomy(int data){
		autonomy = data;
	}

	public int getCollaboration(){
		return collaboration;
	}

	public void setCollaboration(int data){
		collaboration = data;
	}

	public int getEvocation(){
		return evocation;
	}

	public void setEvocation(int data){
		evocation = data;
	}

	public int getSelfExploration(){
		return selfExploration;
	}

	public void setSelfExploration(int data){
		selfExploration = data;
	}

	public String toString(){
		return ("ACCEPTANCE:\t" + acceptance + "\n" +
				"EMPATHY:\t" + empathy + "\n" +
				"DIRECTION:\t" + direction + "\n" +
				"AUTONOMY:\t" + autonomy + "\n" +
				"COLLABORATION:\t" + collaboration + "\n" +
				"EVOCATION:\t" + evocation + "\n" +
				"SELF_EXPLORATION:\t" + selfExploration + "\n");
	}

	public void writeToFile(File file){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter( new FileWriter(file, false));
			writer.println("Global Ratings\n\n");
			writer.println(this.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			writer.close();
		}
	}
}

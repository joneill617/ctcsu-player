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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

public class AboutWindowView extends JFrame {

	private static final long serialVersionUID = 1L;
	
	// GUI Components.
	private JEditorPane textPane 			= null;
	
	public AboutWindowView(){
		init();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void init(){
		setTitle("About this Application");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(300, 400));
		setResizable(true);
		setIconImage(new ImageIcon("images/UNM_Color.jpg").getImage());

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getTextPane(), BorderLayout.CENTER);
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JEditorPane getTextPane(){
		if( textPane == null ){
			textPane = new JEditorPane();
			textPane.setText(
					"CACTI | The CASAA Application for Coding Treatment Interactions\n\n" +
					"Version " + Version.versionString() + "\n\n" +
					"Copyright (C) 2009\n\n" +
					"Original developer:\n" +
					"Alex Manuel - amanuel@unm.edu\n" +
					"http://www.cs.unm.edu/~amanuel/casaa/\n\n\n" +
					"This application is being developed to assist\n" +
					"the research of\n\nDr. Theresa B. Moyers\n" +
					"of the\nCenter on Alcoholism, Substance Abuse and Addictions\n" +
					"at the\n" +
					"University of New Mexico\n" +
					"Albuquerque, NM\n\n\n" +
					"Unless otherwise stated,\n" +
					"this software is free and open source,\n" +
					"per the terms of the GNU Public License:\n" +
					"http://www.gnu.org/licenses/gpl-3.0.txt");
			textPane.setEditable(false);
		}
		return textPane;
	}
}

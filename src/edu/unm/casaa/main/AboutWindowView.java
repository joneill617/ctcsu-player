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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class AboutWindowView extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//GUI Components
	private JFrame frameParentWindow		= null;
	private ImageIcon iconParentWindow		= null;
	private JEditorPane textPane 			= null;
	
	public AboutWindowView(){
		init();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void init(){
		setLookAndFeel();
		getFrameParentWindow().getContentPane().setLayout(new BorderLayout());
		getFrameParentWindow().getContentPane().add(getTextPane(), BorderLayout.CENTER);
		getFrameParentWindow().setLocationRelativeTo(getParent());
		getFrameParentWindow().setVisible(true);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JFrame getFrameParentWindow(){
		if( frameParentWindow == null ){
			frameParentWindow = new JFrame("About this Application");
			frameParentWindow.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			frameParentWindow.setMinimumSize(new Dimension(300, 400));
			frameParentWindow.setResizable(true);
			if( iconParentWindow == null ){
				iconParentWindow = new ImageIcon("images/UNM_Color.jpg");
			}
			frameParentWindow.setIconImage(iconParentWindow.getImage());
		}
		return frameParentWindow;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JEditorPane getTextPane(){
		if( textPane == null ){
			textPane = new JEditorPane();
			textPane.setText(
					"CTCSU | The CASAA Treatment Coding System Utility\n\n" +
					"Version 0.9.5\n\n" +
					"Copyright (C) 2009\n\n" +
					"Actively under development by:\n" +
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
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void setLookAndFeel(){
		//set the look and feel to the native platform
		String strNativeLF = UIManager.getSystemLookAndFeelClassName();
		if( ! strNativeLF.equalsIgnoreCase("com.sun.java.swing.plaf.gtk.GTKLookAndFeel") ){
			//this check avoids a bug where gtk can't display properly
			try {
				UIManager.setLookAndFeel(strNativeLF);
			} catch (InstantiationException e) {
			} catch (ClassNotFoundException e) {
			} catch (UnsupportedLookAndFeelException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}
	
}

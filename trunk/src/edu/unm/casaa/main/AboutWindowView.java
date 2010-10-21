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
        setTitle( "About this Application" );
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setMinimumSize( new Dimension( 550, 450 ) );
        setResizable( true );
        setIconImage( new ImageIcon( "images/UNM_Color.jpg" ).getImage() );

        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( getTextPane(), BorderLayout.CENTER );
        setLocationRelativeTo( getParent() );
        setVisible( true );
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JEditorPane getTextPane(){
		if( textPane == null ){
			textPane = new JEditorPane();
			textPane.setContentType( "text/html" );
			String   sourceURL  = "http://casaa.unm.edu";
            String   licenseURL = "http://www.gnu.org/licenses/gpl-3.0.txt";

            textPane.setText(
					"CACTI | The CASAA Application for Coding Treatment Interactions<br/>" +
					"Version " + Version.versionString() + "<br/>" +
					"Copyright (C) 2009<br/>" +
                    "<br/>" +
					"Original developers:<br/>" +
                    "Alex Manuel<br/>" +
					"Carl Staaf<br/>" +
					"<br/>" +
					"The latest version, source code, and help manual can be downloaded from " + sourceURL + ".<br/>" +
                    "<br/>" +
					"This application is being developed to assist the research of Theresa B. Moyers of the " +
					"Center on Alcoholism, Substance Abuse and Addictions at the University of New Mexico<br/>" +
					"Albuquerque, NM<br/>" +
                    "<br/>" +
					"Unless otherwise stated, this software is free and open source, per the terms of the GNU Public License:<br/>" +
				    licenseURL );
			textPane.setEditable( false );
		}
		return textPane;
	}
}

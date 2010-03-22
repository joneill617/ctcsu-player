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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

class MainControllerAction extends AbstractAction {

	private static final long 	serialVersionUID 	= 1L;
	private MainController 		mc 					= null;
	private String				actionCommand		= null;

	public MainControllerAction( MainController mc, String text, String actionCommand ) {
		super( text );
		this.mc 			= mc;
		this.actionCommand	= actionCommand;
	}

	public void actionPerformed( ActionEvent e ) {
		mc.handleAction( actionCommand ); // Pass to MainController.
	}
}

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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import edu.unm.casaa.main.MainController;
import edu.unm.casaa.main.TemplateUiService;

enum ParserCode{
	START,
	END;
}

public class ParserTemplateUiService extends TemplateUiService{

	private MainController mp_control = null;
	private ParserTemplateView mp_view = null;
	
	public ParserTemplateUiService(MainController control){
		mp_control = control;
		init();
	}
	
	private void init(){
		mp_view = new ParserTemplateView();
		mp_view.addKeyListener(getParserKeyListener());
		mp_view.getButtonStartParse().addMouseListener(getParserButtonListener(ParserCode.START));
		mp_view.getButtonEndParse().addMouseListener(getParserButtonListener(ParserCode.END));
	}
	
	public JPanel getTemplateView(){
		return mp_view;
	}
	
	private ParserTemplateMouseListener getParserButtonListener(ParserCode button){
		return new ParserTemplateMouseListener(button);
	}
	
	private ParserTemplateKeyListener getParserKeyListener(){
		System.out.println("DEBUG: KeyListener init called");
		return new ParserTemplateKeyListener();
	}
	
	//===============================================================
	// Mouse Adapter inner class
	//===============================================================
	private class ParserTemplateMouseListener extends MouseAdapter {

		ParserCode code;
		
		ParserTemplateMouseListener(ParserCode button){
			code = button;
		}
		
		public void mousePressed(MouseEvent e){
			switch(code){
			case START:
				mp_control.handleButtonStartParse();
				break;
			case END:
				mp_control.handleButtonEndParse();
				break;
			default:
				System.err.println("ERROR: ParserTemplateListener failed on code: " + code);
			}
		}
		
	}
	
	//===============================================================
	// Key Adapter inner class
	//===============================================================
	private class ParserTemplateKeyListener extends KeyAdapter{
		
		public ParserTemplateKeyListener(){};
		
		public void keyTyped(KeyEvent event){
			System.out.println("DEBUG: KeyEvent captured");
			if( event.getKeyCode() == KeyEvent.VK_SPACE ){
				System.out.println("DEBUG: KeyEvent VK_SPACE");
				mp_control.handleButtonStartParse();
			}
		}
		
	}
	
}

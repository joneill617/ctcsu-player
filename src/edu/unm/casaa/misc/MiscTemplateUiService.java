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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import edu.unm.casaa.main.MainController;
import edu.unm.casaa.main.TemplateUiService;

public class MiscTemplateUiService extends TemplateUiService{
	
	private MainController mp_control = null;
	private MiscTemplateView mp_view = null;
	
	public MiscTemplateUiService(MainController control){
		mp_control = control;
		init();
	}
	
	private void init(){
		mp_view = new MiscTemplateView();
		
		for( MiscCode m : MiscCode.values() ){
			if( m == MiscCode.INVALID ){
				continue;
			}
			
			JButton button = mp_view.getButtonMiscCode(m);

			button.addMouseListener(getMiscButtonListener(m));
			button.setToolTipText(Integer.toString(m.getValue()));
		}
		
		mp_view.getCheckBoxPauseUncoded().addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				mp_control.setPauseOnUncoded(mp_view.getCheckBoxPauseUncoded().isSelected());
			}
		});
	}
	
	private MiscTemplateListener getMiscButtonListener(MiscCode button){
		return new MiscTemplateListener(button);
	}
	
	public JPanel getTemplateView(){
		return mp_view;
	}
	
	//===============================================================
	// Mouse Adapter inner class
	//===============================================================
	private class MiscTemplateListener extends MouseAdapter {

		private MiscCode code;
		
		MiscTemplateListener(MiscCode code){
			this.code = code;
		}
		
		public void mousePressed(MouseEvent e){
			mp_control.handleButtonMiscCode( code );
		}
				
	}

}

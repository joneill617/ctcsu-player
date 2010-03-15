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
		
		mp_view.getButtonADP().addMouseListener(getMiscButtonListener(MiscCode.ADP));
		mp_view.getButtonADP().setToolTipText( Integer.toString( MiscCode.ADP.getValue() ) );

		// TODO - CARL - Other tooltips...

		mp_view.getButtonADW().addMouseListener(getMiscButtonListener(MiscCode.ADW));
		mp_view.getButtonAF().addMouseListener(getMiscButtonListener(MiscCode.AF));
		
		mp_view.getButtonCO().addMouseListener(getMiscButtonListener(MiscCode.CO));
		mp_view.getButtonDI().addMouseListener(getMiscButtonListener(MiscCode.DI));
		mp_view.getButtonEC().addMouseListener(getMiscButtonListener(MiscCode.EC));
		
		mp_view.getButtonFA().addMouseListener(getMiscButtonListener(MiscCode.FA));
		mp_view.getButtonFI().addMouseListener(getMiscButtonListener(MiscCode.FI));
		mp_view.getButtonGI().addMouseListener(getMiscButtonListener(MiscCode.GI));
		
		mp_view.getButtonCQminus().addMouseListener(getMiscButtonListener(MiscCode.CQ_MINUS));
		mp_view.getButtonCQ().addMouseListener(getMiscButtonListener(MiscCode.CQ_NEUTRAL));
		mp_view.getButtonCQplus().addMouseListener(getMiscButtonListener(MiscCode.CQ_PLUS));
		
		mp_view.getButtonOQminus().addMouseListener(getMiscButtonListener(MiscCode.OQ_MINUS));
		mp_view.getButtonOQ().addMouseListener(getMiscButtonListener(MiscCode.OQ_NEUTRAL));
		mp_view.getButtonOQplus().addMouseListener(getMiscButtonListener(MiscCode.OQ_PLUS));
		
		mp_view.getButtonRCP().addMouseListener(getMiscButtonListener(MiscCode.RCP));
		mp_view.getButtonRCW().addMouseListener(getMiscButtonListener(MiscCode.RCW));
		
		mp_view.getButtonSRminus().addMouseListener(getMiscButtonListener(MiscCode.SR_MINUS));
		mp_view.getButtonSR().addMouseListener(getMiscButtonListener(MiscCode.SR_NEUTRAL));
		mp_view.getButtonSRplus().addMouseListener(getMiscButtonListener(MiscCode.SR_PLUS));
		
		mp_view.getButtonCRminus().addMouseListener(getMiscButtonListener(MiscCode.CR_MINUS));
		mp_view.getButtonCR().addMouseListener(getMiscButtonListener(MiscCode.CR_NEUTRAL));
		mp_view.getButtonCRplus().addMouseListener(getMiscButtonListener(MiscCode.CR_PLUS));
		
		mp_view.getButtonRF().addMouseListener(getMiscButtonListener(MiscCode.RF));
		mp_view.getButtonSU().addMouseListener(getMiscButtonListener(MiscCode.SU));
		mp_view.getButtonST().addMouseListener(getMiscButtonListener(MiscCode.ST));
		mp_view.getButtonWA().addMouseListener(getMiscButtonListener(MiscCode.WA));
		
		mp_view.getButtonCplus().addMouseListener(getMiscButtonListener(MiscCode.C_PLUS));
		mp_view.getButtonCminus().addMouseListener(getMiscButtonListener(MiscCode.C_MINUS));
		
		mp_view.getButtonRplus().addMouseListener(getMiscButtonListener(MiscCode.R_PLUS));
		mp_view.getButtonRminus().addMouseListener(getMiscButtonListener(MiscCode.R_MINUS));
		
		mp_view.getButtonDplus().addMouseListener(getMiscButtonListener(MiscCode.D_PLUS));
		mp_view.getButtonDminus().addMouseListener(getMiscButtonListener(MiscCode.D_MINUS));
		
		mp_view.getButtonAplus().addMouseListener(getMiscButtonListener(MiscCode.A_PLUS));
		mp_view.getButtonAminus().addMouseListener(getMiscButtonListener(MiscCode.A_MINUS));
		
		mp_view.getButtonNplus().addMouseListener(getMiscButtonListener(MiscCode.N_PLUS));
		mp_view.getButtonNminus().addMouseListener(getMiscButtonListener(MiscCode.N_MINUS));
		
		mp_view.getButtonTSplus().addMouseListener(getMiscButtonListener(MiscCode.TS_PLUS));
		mp_view.getButtonTSminus().addMouseListener(getMiscButtonListener(MiscCode.TS_MINUS));
		
		mp_view.getButtonOplus().addMouseListener(getMiscButtonListener(MiscCode.O_PLUS));
		mp_view.getButtonOminus().addMouseListener(getMiscButtonListener(MiscCode.O_MINUS));
		
		mp_view.getButtonFN().addMouseListener(getMiscButtonListener(MiscCode.FN));
		mp_view.getButtonNC().addMouseListener(getMiscButtonListener(MiscCode.NC));
		
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

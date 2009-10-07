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


package edu.unm.casaa.globals;

import java.io.File;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.unm.casaa.main.TemplateUiService;

enum GlobalCode{
	ACCEPTANCE,
	EMPATHY,
	DIRECTION,
	AUTONOMY,
	COLLABORATION,
	EVOCATION,
	SELF_EXPLORATION;
}

public class GlobalTemplateUiService extends TemplateUiService{

	private GlobalTemplateView 		mp_view 		= null;
	private GlobalDataModel 		mp_data 		= null;
	
	public GlobalTemplateUiService(){
		init();
	}

	private void init(){
		mp_view = new GlobalTemplateView();

		mp_view.getSliderAcceptance().addChangeListener(getGlobalSliderListener(GlobalCode.ACCEPTANCE));
		mp_view.getSliderAcceptance().addChangeListener(getGlobalSliderListener(GlobalCode.EMPATHY));
		mp_view.getSliderAcceptance().addChangeListener(getGlobalSliderListener(GlobalCode.DIRECTION));
		mp_view.getSliderAcceptance().addChangeListener(getGlobalSliderListener(GlobalCode.AUTONOMY));
		mp_view.getSliderAcceptance().addChangeListener(getGlobalSliderListener(GlobalCode.COLLABORATION));
		mp_view.getSliderAcceptance().addChangeListener(getGlobalSliderListener(GlobalCode.EVOCATION));
		mp_view.getSliderAcceptance().addChangeListener(getGlobalSliderListener(GlobalCode.SELF_EXPLORATION));
	

		mp_data = new GlobalDataModel();
		mp_data.setAcceptance(mp_view.getSliderAcceptance().getValue());
		mp_data.setAutonomy(mp_view.getSliderAutonomy().getValue());
		mp_data.setCollaboration(mp_view.getSliderCollaboration().getValue());
		mp_data.setDirection(mp_view.getSliderDirection().getValue());
		mp_data.setEmpathy(mp_view.getSliderEmpathy().getValue());
		mp_data.setEvocation(mp_view.getSliderEvocation().getValue());
		mp_data.setSelfExploration(mp_view.getSliderSelfExploration().getValue());
	}

	public JPanel getTemplateView(){
		return mp_view;
	}

	private GlobalTemplateSliderListener getGlobalSliderListener(GlobalCode slider){
		return new GlobalTemplateSliderListener(slider);
	}

	public void writeGlobalsToFile(File file){
		System.out.println("DEBUG: Call to globals ui to write to file");
		mp_data.writeToFile(file);
	}

	//code to manipulate data?

	//===============================================================
	// Slider Adapter Listener Class
	//===============================================================
	private class GlobalTemplateSliderListener implements ChangeListener{

		private GlobalCode code;

		public GlobalTemplateSliderListener(GlobalCode slider){
			code = slider;
		}

		public void stateChanged(ChangeEvent ce) {
			switch(code){
			case ACCEPTANCE:
				if( ! mp_view.getSliderAcceptance().getValueIsAdjusting()){
					mp_data.setAcceptance(mp_view.getSliderAcceptance().getValue());
				}
				break;

			case EMPATHY:
				if( ! mp_view.getSliderEmpathy().getValueIsAdjusting()){
					mp_data.setAcceptance(mp_view.getSliderEmpathy().getValue());
				}
				break;

			case DIRECTION:
				if( ! mp_view.getSliderDirection().getValueIsAdjusting()){
					mp_data.setAcceptance(mp_view.getSliderDirection().getValue());
				}
				break;

			case AUTONOMY:
				if( ! mp_view.getSliderAutonomy().getValueIsAdjusting()){
					mp_data.setAcceptance(mp_view.getSliderAutonomy().getValue());
				}
				break;

			case COLLABORATION:
				if( ! mp_view.getSliderCollaboration().getValueIsAdjusting()){
					mp_data.setAcceptance(mp_view.getSliderCollaboration().getValue());
				}
				break;

			case EVOCATION:
				if( ! mp_view.getSliderEvocation().getValueIsAdjusting()){
					mp_data.setAcceptance(mp_view.getSliderEvocation().getValue());
				}
				break;

			case SELF_EXPLORATION:
				if( ! mp_view.getSliderSelfExploration().getValueIsAdjusting()){
					mp_data.setAcceptance(mp_view.getSliderSelfExploration().getValue());
				}
				break;

			default:
				System.err.println("ERROR: GlobalTemplateListener failed on code: " + code);
			}

		}

	}
}

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

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.unm.casaa.main.TemplateUiService;
import edu.unm.casaa.globals.GlobalCode;

public class GlobalTemplateUiService extends TemplateUiService {

	private GlobalTemplateView	view 	= new GlobalTemplateView();
	private GlobalDataModel 	data	= new GlobalDataModel();

	public GlobalTemplateUiService() {
		init();
	}

	private void init() {
		for( GlobalCode g : GlobalCode.values() ) {
			JSlider slider = view.getSlider( g );

			slider.addChangeListener( getGlobalSliderListener( g ) );
			data.setValue( g, slider.getValue() ); // Initialize data to slider value.
		}
	}

	public JPanel getTemplateView() {
		return view;
	}

	private GlobalTemplateSliderListener getGlobalSliderListener( GlobalCode code ) {
		return new GlobalTemplateSliderListener( code );
	}

	public void writeGlobalsToFile( File file, String filenameAudio ) {
		data.writeToFile( file, filenameAudio, view.getNotes() );
	}

	//===============================================================
	// Slider Adapter Listener Class
	//===============================================================
	private class GlobalTemplateSliderListener implements ChangeListener {

		private GlobalCode code;

		public GlobalTemplateSliderListener( GlobalCode code ) {
			this.code = code;
		}

		public void stateChanged( ChangeEvent ce ) {
			data.setValue( code, view.getSlider( code ).getValue() );
		}

	}
}

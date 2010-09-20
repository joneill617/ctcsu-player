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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.unm.casaa.main.MainController;
import edu.unm.casaa.main.TemplateUiService;
import edu.unm.casaa.globals.GlobalCode;

public class GlobalTemplateUiService extends TemplateUiService {

	private MainController 		control	= null;
	private GlobalTemplateView	view 	= new GlobalTemplateView();
	private GlobalDataModel 	data	= new GlobalDataModel();

	public GlobalTemplateUiService( MainController control ) {
		this.control = control;

        for( int i = 0; i < GlobalCode.numCodes(); i++ ) {
            GlobalCode  code    = GlobalCode.codeAtIndex( i );
            JSlider     slider  = view.getSlider( code );

			slider.addChangeListener( new GlobalTemplateSliderListener( code ) );
			slider.setValue( data.getRating( code ) ); // Initialize slider to data value.
		}

		// Add document listener to text area, so we can save file when text data changes (as we do with sliders).
		view.getTextArea().getDocument().addDocumentListener( new GlobalTemplateDocumentListener() );
	}

	public JPanel getTemplateView() {
		return view;
	}

	public void writeGlobalsToFile( File file, String filenameAudio ) {
		data.writeToFile( file, filenameAudio, view.getNotes() );
	}

	//===============================================================
	// GlobalTemplateDocumentListener
	//===============================================================

	private class GlobalTemplateDocumentListener implements DocumentListener {

		public void removeUpdate( DocumentEvent e ) {
			control.globalDataChanged();
		}
		
		public void insertUpdate( DocumentEvent e ) {
			control.globalDataChanged();
		}
		
		public void changedUpdate( DocumentEvent e ) {
			control.globalDataChanged();
		}
		
	}

	//===============================================================
	// GlobalTemplateSliderListener
	//===============================================================

	private class GlobalTemplateSliderListener implements ChangeListener {

		private GlobalCode code;

		public GlobalTemplateSliderListener( GlobalCode code ) {
			this.code = code;
		}

		public void stateChanged( ChangeEvent ce ) {
			// We save every time data changes, so (to be nice), make sure value is actually changing.
			JSlider slider = view.getSlider( code );

			if( slider.getValueIsAdjusting() ) {
				return; // Wait until user releases slider.
			}

			int rating = slider.getValue();

			if( data.getRating( code ) == rating ) {
				return; // No change.
			}

			data.setRating( code, rating );
			control.globalDataChanged();
		}

	}
}

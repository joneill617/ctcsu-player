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

import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * This is the template panel for the Global Ratings sliders.
 * @author UNM CASAA
 *
 */
public class GlobalTemplateView extends JPanel {

	private static final long serialVersionUID = 1L;

	// Window constants and variables.
	private static final int PANEL_WIDTH	= 600;
	private static final int PANEL_HEIGHT	= 450;

	// GUI components and constants.
	private JPanel panelSliders				= null;
	private JPanel panelLeftSliders			= null;
	private JPanel panelRightSliders		= null;
	private static final int TEXT_ROWS		= 6;
	private static final int TEXT_COLUMNS	= 60;
	private JScrollPane textScrollPane		= null;
	private JTextArea textArea				= null;
	private Dimension dimMainPanel			= new Dimension( PANEL_WIDTH, PANEL_HEIGHT );
	
	// Sliders.
	private HashMap< Integer, JSlider > sliderGlobalCode = new HashMap< Integer, JSlider >();
	
	// Slider constants.
	private static final int SLIDER_MIN		= 1;
	private static final int SLIDER_MAX		= 5;
	private static final int SLIDER_INIT	= 3;

	public GlobalTemplateView() {
		init();
	}
	
	private void init() {
		setBorder( createBorderWindow() );
		setMaximumSize( dimMainPanel );
		setMinimumSize( dimMainPanel );
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
		add( getPanelSliders() );
		add( Box.createRigidArea( new Dimension( 0, 10 ) ) );
		add( getTextScrollPane() );
		setVisible( true );
	}

	public JSlider getSlider( GlobalCode code ) {
		JSlider slider = sliderGlobalCode.get( code.value );

		// Create slider (and border) if it does not yet exist.
		if( slider == null ) {
			slider = new JSlider( SLIDER_MIN, SLIDER_MAX, SLIDER_INIT );
			slider.setSnapToTicks( true );
			slider.setMajorTickSpacing( 1 );
			slider.setPaintTicks( true );
			slider.setPaintTrack( true );
			slider.setPaintLabels( true );

			TitledBorder border = BorderFactory.createTitledBorder( code.label );

			border.setTitleJustification( TitledBorder.LEADING );
			slider.setBorder( border );
			sliderGlobalCode.put( code.value, slider );
		}
		return slider;
	}

	// Get notes written in text area.
	public String getNotes() {
		return getTextArea().getText();
	}

	private JPanel getPanelSliders() {
		if( panelSliders == null ) {
			panelSliders = new JPanel();
			panelSliders.setMaximumSize( dimMainPanel );
			panelSliders.setMinimumSize( dimMainPanel );
			panelSliders.setLayout( new BoxLayout( panelSliders, BoxLayout.X_AXIS ) );
			panelSliders.add( getLeftPanelSliders() );
			panelSliders.add( getRightPanelSliders() );
		}
		return panelSliders;
	}

	private TitledBorder createBorderWindow() {
		TitledBorder border = BorderFactory.createTitledBorder( "Global Ratings" );

		border.setTitleJustification( TitledBorder.CENTER );		
		return border;
	}
	
	private JPanel getLeftPanelSliders() {
		if( panelLeftSliders == null ) {
			panelLeftSliders = new JPanel();
			panelLeftSliders.setLayout( new BoxLayout( panelLeftSliders, BoxLayout.Y_AXIS ) );
			panelLeftSliders.add( getSlider( GlobalCode.ACCEPTANCE ) );
			panelLeftSliders.add( getSlider( GlobalCode.EMPATHY ) );
			panelLeftSliders.add( getSlider( GlobalCode.DIRECTION ) );
			panelLeftSliders.add( getSlider( GlobalCode.AUTONOMY ) );
		}
		return panelLeftSliders;
	}
	
	private JPanel getRightPanelSliders() {
		if( panelRightSliders == null ) {
			panelRightSliders = new JPanel();
			panelRightSliders.setLayout( new BoxLayout( panelRightSliders, BoxLayout.Y_AXIS ) );
			panelRightSliders.add( getSlider( GlobalCode.COLLABORATION ) );
			panelRightSliders.add( getSlider( GlobalCode.EVOCATION ) );
			panelRightSliders.add( getSlider( GlobalCode.SELF_EXPLORATION ) );
		}
		return panelRightSliders;
	}

	private JScrollPane getTextScrollPane() {
		if( textScrollPane == null ) {
			textScrollPane = new JScrollPane( getTextArea() );
		}
		return textScrollPane;
	}

	private JTextArea getTextArea() {
		if( textArea == null ) {
			textArea = new JTextArea( TEXT_ROWS, TEXT_COLUMNS );
			textArea.setLineWrap( true );
		}
		return textArea;
	}
}

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

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import edu.unm.casaa.main.MainController;

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
			slider = new JSlider( code.minRating, code.maxRating, code.defaultRating );
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

    private TitledBorder createBorderWindow() {
        TitledBorder border = BorderFactory.createTitledBorder( MainController.instance.getGlobalsLabel() );

        border.setTitleJustification( TitledBorder.CENTER );        
        return border;
    }

    private JPanel getPanelSliders() {
		if( panelSliders == null ) {
			panelSliders = new JPanel();
			panelSliders.setMaximumSize( dimMainPanel );
			panelSliders.setMinimumSize( dimMainPanel );
			panelSliders.setLayout( new BoxLayout( panelSliders, BoxLayout.X_AXIS ) );
			parseUserControls();
			panelSliders.add( getLeftPanelSliders() );
			panelSliders.add( getRightPanelSliders() );
		}
		return panelSliders;
	}

    private void parseUserControls() {
        File    file    = new File( "userConfiguration.xml" );

        if( file.exists() ) {
            try {
                DocumentBuilderFactory  fact    = DocumentBuilderFactory.newInstance();
                DocumentBuilder         builder = fact.newDocumentBuilder();
                Document                doc     = builder.parse( file.getCanonicalFile());
                Node                    root    = doc.getDocumentElement();

                /* Expected format:
                 * <userConfiguration>
                 *   <globals>
                 *    ...
                 *   </globals>
                 *   <globalControls panel="left">
                 *     ...
                 *   </globalControls>
                 *   <globalControls panel="right">
                 *     ...
                 *   </globalControls>
                 * </userConfiguration>
                 */
                for( Node node = root.getFirstChild(); node != null; node = node.getNextSibling() ) {
                    if( node.getNodeName().equalsIgnoreCase( "globalControls" ) ) {
                        // Get panel attribute.  Must be "left" or "right".
                        NamedNodeMap    map         = node.getAttributes();
                        String          panelName   = map.getNamedItem( "panel" ).getTextContent();

                        if( panelName.equalsIgnoreCase( "left" ) ) {
                            parseControlColumn( node, getLeftPanelSliders() );
                        } else if( panelName.equalsIgnoreCase( "right" ) ) {
                            parseControlColumn( node, getRightPanelSliders() );
                        }
                    }
                }
            } catch( SAXParseException e ) {
                MainController.instance.handleUserCodesParseException( file, e );
            } catch( Exception e ) {
                MainController.instance.handleUserCodesGenericException( file, e );
            }
        } else {
            MainController.instance.handleUserCodesMissing( file );
        }
    }
	
    // Parse a column of controls from given XML node.  Add buttons to given panel, and set panel layout.
    // Each child of given node is expected to be one row of controls.
    private void parseControlColumn( Node node, JPanel panel ) {
        // Traverse children, creating sliders.
        for( Node n = node.getFirstChild(); n != null; n = n.getNextSibling() ) {
            if( n.getNodeName().equalsIgnoreCase( "slider" ) ) {
                NamedNodeMap    map         = n.getAttributes();
                String          globalName  = map.getNamedItem( "global" ).getTextContent();
                GlobalCode      code        = GlobalCode.codeWithName( globalName );
    
                panel.add( getSlider( code ) );
            } else if( n.getNodeName().equalsIgnoreCase( "spacer" ) ) {

                // Create a dummy slider so we can query preferred size.
                JSlider slider = new JSlider( 1, 1 );

                slider.setMajorTickSpacing( 1 );
                slider.setPaintTicks( true );
                slider.setPaintTrack( true );
                slider.setPaintLabels( true );
                slider.setBorder( BorderFactory.createTitledBorder( "Dummy" ) );
                panel.add( Box.createRigidArea( slider.getPreferredSize() ) ); // Spacing.
            }
        }
    }

    private JPanel getLeftPanelSliders() {
		if( panelLeftSliders == null ) {
			panelLeftSliders = new JPanel();
			panelLeftSliders.setLayout( new BoxLayout( panelLeftSliders, BoxLayout.Y_AXIS ) );
			panelLeftSliders.setAlignmentY( Component.TOP_ALIGNMENT );
		}
		return panelLeftSliders;
	}
	
	private JPanel getRightPanelSliders() {
		if( panelRightSliders == null ) {
			panelRightSliders = new JPanel();
			panelRightSliders.setLayout( new BoxLayout( panelRightSliders, BoxLayout.Y_AXIS ) );
            panelRightSliders.setAlignmentY( Component.TOP_ALIGNMENT );
		}
		return panelRightSliders;
	}

	private JScrollPane getTextScrollPane() {
		if( textScrollPane == null ) {
			textScrollPane = new JScrollPane( getTextArea() );
		}
		return textScrollPane;
	}

	public JTextArea getTextArea() {
		if( textArea == null ) {
			textArea = new JTextArea( TEXT_ROWS, TEXT_COLUMNS );
			textArea.setLineWrap( true );
		}
		return textArea;
	}
}

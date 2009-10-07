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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;

/**
 * This is the template panel for the Global Ratings sliders.
 * @author UNM CASAA
 *
 */
public class GlobalTemplateView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	//====================================================================
	// Fields
	//====================================================================
	//Window Constants and Variables
	private static final int PANEL_WIDTH	= 600;
	private static final int PANEL_HEIGHT	= 450;

	//GUI Components and Constants
	private TitledBorder borderWindow		= null;
	private JPanel panelSliders				= null;
	private JPanel panelLeftSliders			= null;
	private JPanel panelRightSliders		= null;
	private Dimension dimMainPanel			= null;
	
	//Sliders
	private JSlider sliderAcceptance		= null;
	private TitledBorder borderSlAccept		= null;
	private JSlider sliderEmpathy			= null;
	private TitledBorder borderSlEmpath		= null;
	private JSlider sliderDirection			= null;
	private TitledBorder borderSlDirect		= null;
	private JSlider sliderAutonomy			= null;
	private TitledBorder borderSlAutono		= null;
	private JSlider sliderCollaboration		= null;
	private TitledBorder borderSlCollab		= null;
	private JSlider sliderEvocation			= null;
	private TitledBorder borderSlEvocat		= null;
	private JSlider sliderSelfExploration	= null;
	private TitledBorder borderSlSelfExp	= null;
	
	//Slider Set-up Constants
	private static final int SLIDER_MIN		= 1;
	private static final int SLIDER_MAX		= 5;
	private static final int SLIDER_INIT	= 3;
	
	
	//====================================================================
	// Constructor and Initialization Methods
	//====================================================================
	
	public GlobalTemplateView(){
		init();
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void init(){
		this.setBorder(getBorderWindow());
		this.setMaximumSize(getDimMainPanel());
		this.setMinimumSize(getDimMainPanel());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(getPanelSliders());
		this.setVisible(true);
	}

	//====================================================================
	// Getter and Setter Methods
	//====================================================================
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//Sliders
	public JSlider getSliderAcceptance(){
		if( sliderAcceptance == null ){
			sliderAcceptance = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
			sliderAcceptance.setSnapToTicks(true);
			sliderAcceptance.setMajorTickSpacing(1);
			sliderAcceptance.setPaintTicks(true);
			sliderAcceptance.setPaintTrack(true);
			sliderAcceptance.setPaintLabels(true);
			sliderAcceptance.setBorder(getBorderSlAccept());
		}
		return sliderAcceptance;
	}
	
	private TitledBorder getBorderSlAccept(){
		if( borderSlAccept == null ){
			borderSlAccept = BorderFactory.createTitledBorder("Acceptance");
			borderSlAccept.setTitleJustification(TitledBorder.LEADING);
		}
		return borderSlAccept;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JSlider getSliderEmpathy(){
		if( sliderEmpathy == null ){
			sliderEmpathy = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
			sliderEmpathy.setSnapToTicks(true);
			sliderEmpathy.setMajorTickSpacing(1);
			sliderEmpathy.setPaintTicks(true);
			sliderEmpathy.setPaintTrack(true);
			sliderEmpathy.setPaintLabels(true);
			sliderEmpathy.setBorder(getBorderSlEmpath());
		}
		return sliderEmpathy;
	}
	
	private TitledBorder getBorderSlEmpath(){
		if( borderSlEmpath == null ){
			borderSlEmpath = BorderFactory.createTitledBorder("Empathy");
			borderSlEmpath.setTitleJustification(TitledBorder.LEADING);
		}
		return borderSlEmpath;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JSlider getSliderDirection(){
		if( sliderDirection == null ){
			sliderDirection = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
			sliderDirection.setSnapToTicks(true);
			sliderDirection.setMajorTickSpacing(1);
			sliderDirection.setPaintTicks(true);
			sliderDirection.setPaintTrack(true);
			sliderDirection.setPaintLabels(true);
			sliderDirection.setBorder(getBorderSlDirect());
		}
		return sliderDirection;
	}
	
	private TitledBorder getBorderSlDirect(){
		if( borderSlDirect == null ){
			borderSlDirect = BorderFactory.createTitledBorder("Direction");
			borderSlDirect.setTitleJustification(TitledBorder.LEADING);
		}
		return borderSlDirect;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JSlider getSliderAutonomy(){
		if( sliderAutonomy == null ){
			sliderAutonomy = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
			sliderAutonomy.setSnapToTicks(true);
			sliderAutonomy.setMajorTickSpacing(1);
			sliderAutonomy.setPaintTicks(true);
			sliderAutonomy.setPaintTrack(true);
			sliderAutonomy.setPaintLabels(true);
			sliderAutonomy.setBorder(getBorderSlAutono());
		}
		return sliderAutonomy;
	}
	
	private TitledBorder getBorderSlAutono(){
		if( borderSlAutono == null ){
			borderSlAutono = BorderFactory.createTitledBorder("Autonomy");
			borderSlAutono.setTitleJustification(TitledBorder.LEADING);
		}
		return borderSlAutono;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JSlider getSliderCollaboration(){
		if( sliderCollaboration == null ){
			sliderCollaboration = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
			sliderCollaboration.setSnapToTicks(true);
			sliderCollaboration.setMajorTickSpacing(1);
			sliderCollaboration.setPaintTicks(true);
			sliderCollaboration.setPaintTrack(true);
			sliderCollaboration.setPaintLabels(true);
			sliderCollaboration.setBorder(getBorderSlCollab());
		}
		return sliderCollaboration;
	}
	
	private TitledBorder getBorderSlCollab(){
		if( borderSlCollab == null ){
			borderSlCollab = BorderFactory.createTitledBorder("Collaboration");
			borderSlCollab.setTitleJustification(TitledBorder.LEADING);
		}
		return borderSlCollab;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JSlider getSliderEvocation(){
		if( sliderEvocation == null ){
			sliderEvocation = new JSlider(SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
			sliderEvocation.setSnapToTicks(true);
			sliderEvocation.setMajorTickSpacing(1);
			sliderEvocation.setPaintTicks(true);
			sliderEvocation.setPaintTrack(true);
			sliderEvocation.setPaintLabels(true);
			sliderEvocation.setBorder(getBorderSlEvocat());
		}
		return sliderEvocation;
	}
	
	private TitledBorder getBorderSlEvocat(){
		if( borderSlEvocat == null ){
			borderSlEvocat = BorderFactory.createTitledBorder("Evocation");
			borderSlEvocat.setTitleJustification(TitledBorder.LEADING);
		}
		return borderSlEvocat;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JSlider getSliderSelfExploration(){
		if( sliderSelfExploration == null ){
			sliderSelfExploration = new JSlider(SLIDER_MIN, SLIDER_MAX, 1);
			sliderSelfExploration.setSnapToTicks(true);
			sliderSelfExploration.setMajorTickSpacing(1);
			sliderSelfExploration.setPaintTicks(true);
			sliderSelfExploration.setPaintTrack(true);
			sliderSelfExploration.setPaintLabels(true);
			sliderSelfExploration.setBorder(getBorderSlSelfExp());
		}
		return sliderSelfExploration;
	}
	
	private TitledBorder getBorderSlSelfExp(){
		if( borderSlSelfExp == null ){
			borderSlSelfExp = BorderFactory.createTitledBorder("Self Exploration");
			borderSlSelfExp.setTitleJustification(TitledBorder.LEADING);
		}
		return borderSlSelfExp;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//Panels and Borders
	private JPanel getPanelSliders(){
		if( panelSliders == null ){
			panelSliders = new JPanel();
			panelSliders.setMaximumSize(getDimMainPanel());
			panelSliders.setMinimumSize(getDimMainPanel());
			panelSliders.setLayout(new BoxLayout(panelSliders, BoxLayout.X_AXIS));
			//panelSliders.setLayout(new FlowLayout());
			panelSliders.add(getLeftPanelSliders());
			panelSliders.add(getRightPanelSliders());
		}
		return panelSliders;
	}
	
	private TitledBorder getBorderWindow(){
		if( borderWindow == null ){
			borderWindow = BorderFactory.createTitledBorder("Global Ratings");
			borderWindow.setTitleJustification(TitledBorder.CENTER);		
		}
		return borderWindow;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getLeftPanelSliders(){
		if( panelLeftSliders == null ){
			panelLeftSliders = new JPanel();
			panelLeftSliders.setLayout(new BoxLayout(panelLeftSliders, BoxLayout.Y_AXIS));
			panelLeftSliders.add(getSliderAcceptance());
			panelLeftSliders.add(getSliderEmpathy());
			panelLeftSliders.add(getSliderDirection());
			panelLeftSliders.add(getSliderAutonomy());
		}
		return panelLeftSliders;
	}
	
	private JPanel getRightPanelSliders(){
		if( panelRightSliders == null ){
			panelRightSliders = new JPanel();
			panelRightSliders.setLayout(new BoxLayout(panelRightSliders, BoxLayout.Y_AXIS));
			panelRightSliders.add(getSliderCollaboration());
			panelRightSliders.add(getSliderEvocation());
			panelRightSliders.add(getSliderSelfExploration());
		}
		return panelRightSliders;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private Dimension getDimMainPanel(){
		if( dimMainPanel == null ){
			dimMainPanel = new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
		}
		return dimMainPanel;
	}
	

	
}

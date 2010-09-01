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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * This class creates the empty shell GUI for the Audio Player interface.
 * 
 * @author UNM CASAA
 *
 */
public class PlayerView extends JFrame {

	private static final long serialVersionUID = 1L;

	//====================================================================
	// Fields
	//====================================================================

	// Window Constants and Variables
	private String strWindowTitle = "CACTI | The CASAA Application for Coding Treatment Interactions | v" + Version.versionString();
	private String strNativeLF					= null;
	private static final int X_LOCATION			= 100;
	private static final int Y_LOCATION			= 5;
	private static final int WINDOW_WIDTH		= 700;
	private static final int WINDOW_HEIGHT		= 850;
	private Dimension dimMAX					= null;
	private Dimension dimMIN					= null;
	private ImageIcon iconParentWindow			= null;

	// GUI Components
	private JFrame frameParentWindow				= null;
	private JPanel topLayoutPanel					= null;
	private static final int TOP_PANEL_WIDTH		= 600;
	private static final int TOP_PANEL_HEIGHT		= 270;
	private Border borderTop						= null;
	private JPanel bottomLayoutPanel				= null;
	private static final int BOTTOM_PANEL_WIDTH		= 600;
	private static final int BOTTOM_PANEL_HEIGHT	= 450;
	private Border borderBottom						= null;
	private JPanel panelTemplate					= null;
	private JPanel panelPlayerControls				= null;
	private JPanel panelFileInfo					= null;
	private JPanel panelGainPan						= null;
	private Timeline timeline						= null;

	// Player Controls
	private Dimension dimPlayerButtonSize		= null;
	private static final int BUTTON_WIDTH		= 100;
	private static final int BUTTON_HEIGHT		= 25;
	private JButton buttonPlay					= null;
	private JButton buttonReplay				= null;
	private JButton buttonRewind5s				= null;
	private JSlider sliderSeek					= null;
	private JSlider sliderGain					= null;
	private JSlider sliderPan					= null;
	private JLabel labelPlayerStatus			= null;
	private JLabel labelTime					= null;

	private JMenuBar menuBarPlayer				= null;
	private JMenu menuFile						= null;
	private JMenuItem menuItemLoadAudio			= null;	
	private JMenuItem menuItemExit				= null;
	private JMenu menuParse						= null;
	private JMenuItem menuItemNewParse			= null;
	private JMenuItem menuItemLoadParse			= null;
	private JMenu menuCode						= null;
	private JMenuItem menuItemNewCode			= null;
	private JMenuItem menuItemLoadCode			= null;
	private JMenu menuGlobals					= null;
	private JMenuItem menuItemCodeGlobals		= null;
	private JMenu menuAbout						= null;
	private JMenuItem menuItemHelp				= null;
	private JMenuItem menuItemAbout				= null;


	// Slider Control Constants
	private static final int SEEK_MIN_VAL		= 0;
	public static final int SEEK_MAX_VAL		= 1000;
	private static final int SEEK_INIT_VAL		= 0;
	private TitledBorder borderSeek				= null;

	private static final int GAIN_MIN_VAL		= 0;
	private static final int GAIN_MAX_VAL		= 100;
	private static final int GAIN_TICK_VAL		= 10;
	private static final int GAIN_INIT_VAL		= 50;
	private TitledBorder borderGain				= null;

	private static final int PAN_MIN_VAL		= -10;
	private static final int PAN_MAX_VAL		= 10;
	private static final int PAN_TICK_VAL		= 2;
	private static final int PAN_INIT_VAL		= 0;
	private TitledBorder borderPan				= null;

	private MainController	control				= null;

	//====================================================================
	// Constructor and Initialization Methods
	//====================================================================

	public PlayerView( MainController control ) {
		assert( control != null );
		this.control = control;
		setLookAndFeel();
		getFrameParentWindow().getContentPane().setLayout(new BorderLayout());
		getFrameParentWindow().getContentPane().add(getTopLayoutPanel(), BorderLayout.NORTH);
		getFrameParentWindow().getContentPane().add(getBottomLayoutPanel(), BorderLayout.CENTER);

		// TODO - CARL - pack?  Maybe in a number of places?  Is this the most top-level widget?

		getFrameParentWindow().setLocation(X_LOCATION, Y_LOCATION);
		getFrameParentWindow().setVisible(true);
	}

	//====================================================================
	// Public Getter and Setter Methods
	//====================================================================

	/**
	 * Returns a new player button using given action and optional key binding.
	 * @param actionCommand
	 * @param keyBinding - if non-null, assign given key stroke as key binding.
	 * @return a JButton used to start playing the audio file
	 */
	private JButton newPlayerButton( String actionCommand, KeyStroke keyBinding ) {
		JButton button = new JButton( control.getActionTable().get( actionCommand ) );

		button.setPreferredSize( getDimPlayerButtonSize() );
		if( keyBinding != null ) {
			button.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( keyBinding, "pressed" );
			button.getActionMap().put( "pressed", button.getAction() );
		}
		return button;
	}

	private JButton newPlayerButton( String actionCommand ) {
		return newPlayerButton( actionCommand, null );
	}

	/**
	 * Returns the Player's Play Button
	 * @return a JButton used to start playing the audio file
	 */
	public JButton getButtonPlay() {
		if( buttonPlay == null ) {
			buttonPlay = newPlayerButton( "play" );
		}
		return buttonPlay;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Returns the Player's Replay Button
	 * @return a JButton used to undo last parse or code.
	 */
	public JButton getButtonReplay(){
		if( buttonReplay == null ){
			buttonReplay = newPlayerButton( "replay", KeyStroke.getKeyStroke( KeyEvent.VK_R, 0 ) );
		}
		return buttonReplay;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Returns the Player's Rewind5s Button
	 * @return a JButton used to rewind the audio file five seconds
	 */
	public JButton getButtonRewind5s(){
		if( buttonRewind5s == null ){
			buttonRewind5s = newPlayerButton( "rewind5s", KeyStroke.getKeyStroke( KeyEvent.VK_5, 0 ) );
		}
		return buttonRewind5s;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Returns the Player's Pan Slider
	 * @return a JSlider used to control and show the current play position
	 */
	public JSlider getSliderPan(){
		if( sliderPan == null ){
			//actual control uses -1.0 to 1.0 with 0.0 at center
			sliderPan = new JSlider(SwingConstants.HORIZONTAL, 
					PAN_MIN_VAL, PAN_MAX_VAL, PAN_INIT_VAL);
			//sliderPan.setToolTipText("Pan the Audio");
			sliderPan.setMajorTickSpacing(PAN_TICK_VAL);
			sliderPan.setSnapToTicks(true);
			sliderPan.setPaintTicks(true);
			Hashtable<Integer, JLabel> tableLabel = 
				new Hashtable<Integer, JLabel>();
			tableLabel.put(new Integer(0), new JLabel("Center"));
			tableLabel.put(new Integer(-10), new JLabel("Left"));
			tableLabel.put(new Integer(10), new JLabel("Right"));
			sliderPan.setLabelTable(tableLabel);
			sliderPan.setPaintLabels(true);
			sliderPan.setBorder(getBorderPan());
		}
		return sliderPan;
	}

	private Border getBorderPan(){
		if( borderPan == null ){
			borderPan = BorderFactory.createTitledBorder(
			"Balance");
			borderPan.setTitleJustification(TitledBorder.LEADING);
		}
		return borderPan;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Returns the Player's Seek Slider
	 * @return a JSlider used to control and show the current play position
	 */
	public JSlider getSliderSeek(){
		if( sliderSeek == null ){
			sliderSeek = new JSlider(SwingConstants.HORIZONTAL, 
					SEEK_MIN_VAL, SEEK_MAX_VAL, 
					SEEK_INIT_VAL);
			sliderSeek.setBorder(getBorderSeek());
			//sliderSeek.setToolTipText("Seek the Audio File");
			sliderSeek.setDoubleBuffered(true);
		}
		return sliderSeek;
	}
	
	private Border getBorderSeek(){
		if( borderSeek == null ){
			borderSeek = BorderFactory.createTitledBorder("Seek");
			borderSeek.setTitleJustification(TitledBorder.LEADING);
		}
		return borderSeek;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Sets the Player's Seek Slider to a value between 0 and 1000.
	 * @param int value
	 */
	public void setSliderSeek( int value ){
		getSliderSeek().setValue( value );
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Returns the Player's Gain Slider
	 * @return a JSlider used to control and show the current play position
	 */
	public JSlider getSliderGain(){
		if( sliderGain == null ){
			//actual control uses 0.0 to 1.0 in increments of 0.05
			sliderGain = new JSlider(SwingConstants.HORIZONTAL, 
					GAIN_MIN_VAL, GAIN_MAX_VAL, 
					GAIN_INIT_VAL);
			//sliderGain.setToolTipText("Set the Player's Gain Value");
			sliderGain.setMajorTickSpacing(GAIN_TICK_VAL);
			Hashtable<Integer, JLabel> tableLabel = 
				new Hashtable<Integer, JLabel>();
			tableLabel.put(new Integer(0), new JLabel("0"));
			tableLabel.put(new Integer(10), new JLabel("1"));
			tableLabel.put(new Integer(20), new JLabel("2"));
			tableLabel.put(new Integer(30), new JLabel("3"));
			tableLabel.put(new Integer(40), new JLabel("4"));
			tableLabel.put(new Integer(50), new JLabel("5"));
			tableLabel.put(new Integer(60), new JLabel("6"));
			tableLabel.put(new Integer(70), new JLabel("7"));
			tableLabel.put(new Integer(80), new JLabel("8"));
			tableLabel.put(new Integer(90), new JLabel("9"));
			tableLabel.put(new Integer(100), new JLabel("10"));
			sliderGain.setLabelTable(tableLabel);
			sliderGain.setPaintTicks(true);
			sliderGain.setPaintLabels(true);
			sliderGain.setBorder(getBorderGain());
		}
		return sliderGain;
	}	

	private Border getBorderGain(){
		if( borderGain == null ){
			borderGain = BorderFactory.createTitledBorder(
			"Volume");
			borderGain.setTitleJustification(TitledBorder.LEADING);
		}
		return borderGain;
	}

	//====================================================================
	// Private Helper Methods
	//====================================================================

	public JFrame getFrameParentWindow(){
		if( frameParentWindow == null ){
			frameParentWindow = new JFrame(strWindowTitle);
			
			frameParentWindow.setMaximumSize(getDimMAX());
			frameParentWindow.setMinimumSize(getDimMIN());
			if( iconParentWindow == null ){
				iconParentWindow = new ImageIcon("images/UNM_Color.jpg");
			}
			frameParentWindow.setIconImage(iconParentWindow.getImage());
			frameParentWindow.setJMenuBar(getMenuBarPlayer());
			frameParentWindow.setResizable(true);
		}
		return frameParentWindow;
	}
	
	private Dimension getDimMAX(){
		if( dimMAX == null ){
			dimMAX = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
		}
		return dimMAX;
	}
	
	private Dimension getDimMIN(){
		if( dimMIN == null ){
			dimMIN = new Dimension(WINDOW_WIDTH, TOP_PANEL_HEIGHT);
		}
		return dimMAX;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getTopLayoutPanel(){
		if( topLayoutPanel == null ){
			topLayoutPanel = new JPanel();
			topLayoutPanel.setMaximumSize(new Dimension(TOP_PANEL_WIDTH, TOP_PANEL_HEIGHT));
			topLayoutPanel.setMinimumSize(new Dimension(TOP_PANEL_WIDTH, TOP_PANEL_HEIGHT));
			topLayoutPanel.setLayout(new BorderLayout());
			topLayoutPanel.setBorder(getBorderTop());
			topLayoutPanel.add(getPanelPlayerControls(), BorderLayout.NORTH);
			topLayoutPanel.add(getPanelFileInfo(), BorderLayout.CENTER);
			topLayoutPanel.add(getPanelGainPan(), BorderLayout.SOUTH);
		}
		return topLayoutPanel;
	}

	private Border getBorderTop(){
		if( borderTop == null ){
			borderTop = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		}
		return borderTop;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getBottomLayoutPanel(){
		if( bottomLayoutPanel == null ){
			bottomLayoutPanel = new JPanel();
			bottomLayoutPanel.setMaximumSize(new Dimension(BOTTOM_PANEL_WIDTH,
					BOTTOM_PANEL_HEIGHT));
			bottomLayoutPanel.setMinimumSize(new Dimension(BOTTOM_PANEL_WIDTH,
					BOTTOM_PANEL_HEIGHT));
			//TODO: Set a default view (Instructions, logo, etc.)
			bottomLayoutPanel.setBorder(getBorderBottom());
			bottomLayoutPanel.setVisible(true);
		}
		return bottomLayoutPanel;
	}

	private Border getBorderBottom(){
		if( borderBottom == null ){
			borderBottom = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		}
		return borderBottom;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void setPanelTemplate(JPanel template){
		if( panelTemplate != null ){
			getBottomLayoutPanel().remove(panelTemplate);
			getBottomLayoutPanel().setVisible(false);
		}	
		if( template != null ){
			panelTemplate = template;
			getBottomLayoutPanel().add(panelTemplate);
			getBottomLayoutPanel().setVisible(true);
		}		
		getBottomLayoutPanel().validate();
	}
	
	public JPanel getPanelTemplate(){
		if( panelTemplate == null ){
			return getBottomLayoutPanel();
		}
		else{
			return panelTemplate;
		}
	}
	
	public String getTemplateType(){
		if( panelTemplate != null ){
			return panelTemplate.toString();
		}
		else{
			return null;
		}		
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getPanelPlayerControls(){
		if( panelPlayerControls == null ){
			panelPlayerControls = new JPanel();
			panelPlayerControls.setLayout(new FlowLayout());
			panelPlayerControls.add(getButtonPlay());
			panelPlayerControls.add(getButtonReplay());
			panelPlayerControls.add(getButtonRewind5s());
		}
		return panelPlayerControls;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getPanelFileInfo(){
		if( panelFileInfo == null ){
			panelFileInfo = new JPanel();
			panelFileInfo.setLayout(new BoxLayout(panelFileInfo, BoxLayout.Y_AXIS));
			panelFileInfo.add(getLabelTime());
			panelFileInfo.add(getLabelPlayerStatus());
			panelFileInfo.add(getSliderSeek());

			// TMP - Carl.  TODO - We want this as part of parse and code interface, but not play or globals.
			panelFileInfo.add(getTimeline());
			// TMP
		}
		return panelFileInfo;
	}

	public Timeline getTimeline() {
		if( timeline == null ){
			timeline = new Timeline( control );
		}
		return timeline;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getPanelGainPan(){
		if( panelGainPan == null ){
			panelGainPan = new JPanel();
			panelGainPan.setLayout(new BoxLayout(panelGainPan, BoxLayout.X_AXIS));
			panelGainPan.add(getSliderGain());
			panelGainPan.add(getSliderPan());
		}
		return panelGainPan;
	}

	private JLabel getLabelPlayerStatus(){
		if( labelPlayerStatus == null ){
			labelPlayerStatus = new JLabel("Player Status");
		}
		return labelPlayerStatus;
	}

	public void setLabelPlayerStatus(String strStatus){
		getLabelPlayerStatus().setText(strStatus);
	}

	private JLabel getLabelTime(){
		if( labelTime == null ){
			labelTime = new JLabel("Time");
			labelTime.setDoubleBuffered(true);
		}
		return labelTime;
	}

	public void setLabelTime(String strTime){
		getLabelTime().setText(strTime);
		getLabelTime().validate();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JMenuBar getMenuBarPlayer(){
		if( menuBarPlayer == null ){
			menuBarPlayer = new JMenuBar();
			menuBarPlayer.add(getMenuFile());
			menuBarPlayer.add(getMenuParse());
			menuBarPlayer.add(getMenuCode());
			menuBarPlayer.add(getMenuGlobals());
			menuBarPlayer.add(getMenuAbout());
		}
		return menuBarPlayer;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JMenu getMenuFile(){
		if( menuFile == null ){
			menuFile = new JMenu(" File ");
			menuFile.add(getMenuItemLoadAudio());
			menuFile.add(getMenuItemExit());
		}
		return menuFile;
	}

	public JMenuItem getMenuItemLoadAudio(){
		if( menuItemLoadAudio == null ){
			menuItemLoadAudio = new JMenuItem("Load Audio File");
		}
		return menuItemLoadAudio;
	}
	
	public JMenuItem getMenuItemExit(){
		if( menuItemExit == null ){
			menuItemExit = new JMenuItem("Exit");
		}
		return menuItemExit;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JMenu getMenuParse(){
		if( menuParse == null ){
			menuParse = new JMenu(" Parse Utterances ");
			menuParse.add(getMenuItemNewParse());
			menuParse.add(getMenuItemLoadParse());
		}
		return menuParse;
	}
	
	public JMenuItem getMenuItemNewParse(){
		if( menuItemNewParse == null ){
			menuItemNewParse = new JMenuItem("Start a New Parse File");
		}
		return menuItemNewParse;
	}

	public JMenuItem getMenuItemLoadParse(){
		if( menuItemLoadParse == null ){
			menuItemLoadParse = new JMenuItem("Resume Parsing a Parse File");
		}
		return menuItemLoadParse;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JMenu getMenuGlobals(){
		if( menuGlobals == null ){
			menuGlobals = new JMenu(" Global Ratings ");
			menuGlobals.add(getMenuItemCodeGlobals());
		}
		return menuGlobals;
	}
	
	public JMenuItem getMenuItemCodeGlobals(){
		if( menuItemCodeGlobals == null ){
			menuItemCodeGlobals = new JMenuItem("Score Global Ratings");
		}
		return menuItemCodeGlobals;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JMenu getMenuCode(){
		if( menuCode == null ){
			menuCode = new JMenu(" Code Utterances ");
			menuCode.add(getMenuItemNewCode());
			menuCode.add(getMenuItemLoadCode());
		}
		return menuCode;
	}
	
	public JMenuItem getMenuItemNewCode(){
		if( menuItemNewCode == null ){
			menuItemNewCode = new JMenuItem("Start Coding a Parse File");
		}
		return menuItemNewCode;
	}

	public JMenuItem getMenuItemLoadCode(){
		if( menuItemLoadCode == null ){
			menuItemLoadCode = new JMenuItem("Resume Coding a Casaa File");
		}
		return menuItemLoadCode;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JMenu getMenuAbout(){
		if( menuAbout == null ){
			menuAbout = new JMenu(" About ");
			menuAbout.add(getMenuItemHelp());
			menuAbout.add(getMenuItemAbout());
		}
		return menuAbout;
	}

	public JMenuItem getMenuItemHelp(){
		if( menuItemHelp == null ){
			menuItemHelp = new JMenuItem("Help");
		}
		return menuItemHelp;
	}

	public JMenuItem getMenuItemAbout(){
		if( menuItemAbout == null ){
			menuItemAbout = new JMenuItem("About this Application");
		}
		return menuItemAbout;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void setLookAndFeel(){
		if( strNativeLF == null ){
			//set the look and feel to the native platform
			strNativeLF = UIManager.getSystemLookAndFeelClassName();
			if( strNativeLF.equalsIgnoreCase("com.sun.java.swing.plaf.windows.WindowsLookAndFeel") ){
				//this check avoids a bug where gtk can't display properly
				try {
					UIManager.setLookAndFeel(strNativeLF);
				} catch (InstantiationException e) {
				} catch (ClassNotFoundException e) {
				} catch (UnsupportedLookAndFeelException e) {
				} catch (IllegalAccessException e) {
				}
			}
			else{
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				} catch (ClassNotFoundException e) {					
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				} catch (UnsupportedLookAndFeelException e) {
				}
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private Dimension getDimPlayerButtonSize(){
		if( dimPlayerButtonSize == null ){
			dimPlayerButtonSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
		}
		return dimPlayerButtonSize;
	}

}

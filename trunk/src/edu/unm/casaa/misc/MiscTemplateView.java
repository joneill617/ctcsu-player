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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * This class creates the empty shell GUI for the MISC Coding interface.
 * 
 * @author UNM CASAA
 *
 */
public class MiscTemplateView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//====================================================================
	// Fields
	//====================================================================
	//Window Constants and Variables
	private static final int PANEL_WIDTH	= 600;
	private static final int PANEL_HEIGHT	= 450;

	//GUI Components and Constants
	private TitledBorder borderWindow		= null;
	private Dimension dimMainPanel			= null;

	private JPanel panelButtons				= null;
	private JPanel panelCurrentText			= null;
	private JPanel panelLastText			= null;
	private JPanel panelTherapistControls	= null;
	private static final int ROWS_THERAPIST	= 9;
	private static final int COLS_THERAPIST	= 3;
	private TitledBorder borderTherapist	= null;
	private JPanel panelClientControls		= null;
	private static final int ROWS_CLIENT	= 9;
	private static final int COLS_CLIENT	= 2;
	private TitledBorder borderClient		= null;
	private static final int BUTTON_HOR_GAP	= 5;
	private static final int BUTTON_VER_GAP	= 4;

	private Dimension dimButtonSize			= null;
	private static final int BUTTON_WIDTH	= 90;
	private static final int BUTTON_HEIGHT	= 24;
	private TitledBorder borderButtons		= null;

	//User Feedback Components
	private JTextField textFieldOrder		= null;
	private static final int ORDER_COLS		= 9;
	private JLabel labelOrder				= null;
	private JTextField textFieldCode		= null;
	private static final int CODE_COLS		= 9;
	private JLabel labelCode				= null;
	private JTextField textFieldStartTime	= null;
	private JTextField textFieldEndTime		= null;
	private static final int TIME_COLS		= 20;
	private JLabel labelStart				= null;
	private JLabel labelEnd					= null;
	private TitledBorder borderTextFields	= null;

	private JTextField textFieldNext		= null;
	private static final int NEXT_COLS		= 60;
	private TitledBorder borderNext			= null;

	private JCheckBox checkBoxPauseUncoded	= null;
	//private JButton buttonDUMMY 			= null;

	//Therapist Coding Controls
	private JButton buttonADP				= null;
	private JButton buttonADW				= null;
	private JButton buttonAF				= null;
	private JButton buttonCO				= null;
	private JButton buttonDI				= null;
	private JButton buttonEC				= null;
	private JButton buttonFA				= null;
	private JButton buttonFI				= null;
	private JButton buttonGI				= null;

	private JButton buttonCQminus			= null;
	private JButton buttonCQ				= null;
	private JButton buttonCQplus			= null;

	private JButton buttonOQminus			= null;
	private JButton buttonOQ				= null;
	private JButton buttonOQplus			= null;

	private JButton buttonRCP				= null;
	private JButton buttonRCW				= null;

	private JButton buttonSRminus			= null;
	private JButton buttonSR				= null;
	private JButton buttonSRplus			= null;

	private JButton buttonCRminus			= null;
	private JButton buttonCR				= null;
	private JButton buttonCRplus			= null;

	private JButton buttonRF				= null;
	private JButton buttonSU				= null;
	private JButton buttonST				= null;
	private JButton buttonWA				= null;

	//Client Coding Controls
	private JButton buttonCplus				= null;
	private JButton buttonCminus			= null;
	private JButton buttonRplus				= null;
	private JButton buttonRminus			= null;
	private JButton buttonDplus				= null;
	private JButton buttonDminus			= null;
	private JButton buttonAplus				= null;
	private JButton buttonAminus			= null;
	private JButton buttonNplus				= null;
	private JButton buttonNminus			= null;
	private JButton buttonTSplus			= null;
	private JButton buttonTSminus			= null;
	private JButton buttonOplus				= null;
	private JButton buttonOminus			= null;
	private JButton buttonFN				= null;
	private JButton buttonNC				= null;


	//====================================================================
	// Constructor and Initialization Methods
	//====================================================================

	public MiscTemplateView(){
		init();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void init(){
		this.setBorder(getBorderWindow());
		this.setMaximumSize(getDimMainPanel());
		this.setMinimumSize(getDimMainPanel());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(getPanelNextText());
		this.add(getPanelCurrentText());
		this.add(getPanelButtons());
		this.setVisible(true);
	}

	//====================================================================
	// Public Getter and Setter Methods
	//====================================================================

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//Therapist Coding Buttons
	public JButton getButtonADP(){
		if( buttonADP == null ){
			buttonADP = new JButton("ADP");
			buttonADP.setPreferredSize(getDimButtonSize());
			//buttonADP.setToolTipText("Advise With Permission");
		}
		return buttonADP;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonADW(){
		if( buttonADW == null ){
			buttonADW = new JButton("ADW");
			buttonADW.setPreferredSize(getDimButtonSize());
			//buttonADW.setToolTipText("Advise Without Permission");
		}
		return buttonADW;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonAF(){
		if( buttonAF == null ){
			buttonAF = new JButton("AF");
			buttonAF.setPreferredSize(getDimButtonSize());
			//buttonAF.setToolTipText("Affirm");
		}
		return buttonAF;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonCO(){
		if( buttonCO == null ){
			buttonCO = new JButton("CO");
			buttonCO.setPreferredSize(getDimButtonSize());
			//buttonCO.setToolTipText("Confront");
		}
		return buttonCO;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonDI(){
		if( buttonDI == null ){
			buttonDI = new JButton("DI");
			buttonDI.setPreferredSize(getDimButtonSize());
			//buttonDI.setToolTipText("Direct");
		}
		return buttonDI;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonEC(){
		if( buttonEC == null ){
			buttonEC = new JButton("EC");
			buttonEC.setPreferredSize(getDimButtonSize());
			//buttonEC.setToolTipText("Emphasize Control");
		}
		return buttonEC;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonFA(){
		if( buttonFA == null ){
			buttonFA = new JButton("FA");
			buttonFA.setPreferredSize(getDimButtonSize());
			//buttonFA.setToolTipText("Facilitate");
		}
		return buttonFA;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonFI(){
		if( buttonFI == null ){
			buttonFI = new JButton("FI");
			buttonFI.setPreferredSize(getDimButtonSize());
			//buttonFI.setToolTipText("Filler");
		}
		return buttonFI;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonGI(){
		if( buttonGI == null ){
			buttonGI = new JButton("GI");
			buttonGI.setPreferredSize(getDimButtonSize());
			//buttonGI.setToolTipText("Giving Information");
		}
		return buttonGI;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonCQminus(){
		if( buttonCQminus == null ){
			buttonCQminus = new JButton("CQ-");
			buttonCQminus.setPreferredSize(getDimButtonSize());
			//buttonCQminus.setToolTipText("Closed Question Minus");
		}
		return buttonCQminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonCQ(){
		if( buttonCQ == null ){
			buttonCQ = new JButton("CQ0");
			buttonCQ.setPreferredSize(getDimButtonSize());
			//buttonCQ.setToolTipText("Closed Question Neutral");
		}
		return buttonCQ;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonCQplus(){
		if( buttonCQplus == null ){
			buttonCQplus = new JButton("CQ+");
			buttonCQplus.setPreferredSize(getDimButtonSize());
			//buttonCQplus.setToolTipText("Closed Question Plus");
		}
		return buttonCQplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonOQminus(){
		if( buttonOQminus == null ){
			buttonOQminus = new JButton("OQ-");
			buttonOQminus.setPreferredSize(getDimButtonSize());
			//buttonOQminus.setToolTipText("Open Question Minus");
		}
		return buttonOQminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonOQ(){
		if( buttonOQ == null ){
			buttonOQ = new JButton("OQ0");
			buttonOQ.setPreferredSize(getDimButtonSize());
			//buttonOQ.setToolTipText("Open Question Neutral");
		}
		return buttonOQ;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonOQplus(){
		if( buttonOQplus == null ){
			buttonOQplus = new JButton("OQ+");
			buttonOQplus.setPreferredSize(getDimButtonSize());
			//buttonOQplus.setToolTipText("Open Question Plus");
		}
		return buttonOQplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonSRminus(){
		if( buttonSRminus == null ){
			buttonSRminus = new JButton("SR-");
			buttonSRminus.setPreferredSize(getDimButtonSize());
			//buttonSRminus.setToolTipText("Simple Reflection Minus");
		}
		return buttonSRminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonSR(){
		if( buttonSR == null ){
			buttonSR = new JButton("SR0");
			buttonSR.setPreferredSize(getDimButtonSize());
			//buttonSR.setToolTipText("Simple Reflection Neutral");
		}
		return buttonSR;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonSRplus(){
		if( buttonSRplus == null ){
			buttonSRplus = new JButton("SR+");
			buttonSRplus.setPreferredSize(getDimButtonSize());
			//buttonSRplus.setToolTipText("Simple Reflection Plus");
		}
		return buttonSRplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonCRminus(){
		if( buttonCRminus == null ){
			buttonCRminus = new JButton("CR-");
			buttonCRminus.setPreferredSize(getDimButtonSize());
			//buttonCRminus.setToolTipText("Complex Reflection Minus");
		}
		return buttonCRminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonCR(){
		if( buttonCR == null ){
			buttonCR = new JButton("CR0");
			buttonCR.setPreferredSize(getDimButtonSize());
			//buttonCR.setToolTipText("Complex Reflection");
		}
		return buttonCR;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonCRplus(){
		if( buttonCRplus == null ){
			buttonCRplus = new JButton("CR+");
			buttonCRplus.setPreferredSize(getDimButtonSize());
			//buttonCRplus.setToolTipText("Complex Reflection Plus");
		}
		return buttonCRplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonRCP(){
		if( buttonRCP == null ){
			buttonRCP = new JButton("RCP");
			buttonRCP.setPreferredSize(getDimButtonSize());
			//buttonRCP.setToolTipText("Raise Concern With Permission");
		}
		return buttonRCP;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonRCW(){
		if( buttonRCW == null ){
			buttonRCW = new JButton("RCW");
			buttonRCW.setPreferredSize(getDimButtonSize());
			//buttonRCW.setToolTipText("Raise Concern Without Permission");
		}
		return buttonRCW;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonRF(){
		if( buttonRF == null ){
			buttonRF = new JButton("RF");
			buttonRF.setPreferredSize(getDimButtonSize());
			//buttonRF.setToolTipText("Reframe");
		}
		return buttonRF;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonSU(){
		if( buttonSU == null ){
			buttonSU = new JButton("SU");
			buttonSU.setPreferredSize(getDimButtonSize());
			//buttonSU.setToolTipText("Support");
		}
		return buttonSU;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonST(){
		if( buttonST == null ){
			buttonST = new JButton("ST");
			buttonST.setPreferredSize(getDimButtonSize());
			//buttonST.setToolTipText("Structure");
		}
		return buttonST;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonWA(){
		if( buttonWA == null ){
			buttonWA = new JButton("WA");
			buttonWA.setPreferredSize(getDimButtonSize());
			//buttonWA.setToolTipText("Warn");
		}
		return buttonWA;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//Client Coding Buttons
	public JButton getButtonCplus(){
		if( buttonCplus == null ){
			buttonCplus = new JButton("C+");
			buttonCplus.setPreferredSize(getDimButtonSize());
			//buttonCplus.setToolTipText("Commit+");
		}
		return buttonCplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonCminus(){
		if( buttonCminus == null ){
			buttonCminus = new JButton("C-");
			buttonCminus.setPreferredSize(getDimButtonSize());
			//buttonCminus.setToolTipText("Commit-");
		}
		return buttonCminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonRplus(){
		if( buttonRplus == null ){
			buttonRplus = new JButton("R+");
			buttonRplus.setPreferredSize(getDimButtonSize());
			//buttonRplus.setToolTipText("Reason+");
		}
		return buttonRplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonRminus(){
		if( buttonRminus == null ){
			buttonRminus = new JButton("R-");
			buttonRminus.setPreferredSize(getDimButtonSize());
			//buttonRminus.setToolTipText("Reason-");
		}
		return buttonRminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonDplus(){
		if( buttonDplus == null ){
			buttonDplus = new JButton("D+");
			buttonDplus.setPreferredSize(getDimButtonSize());
			//buttonDplus.setToolTipText("Desire+");
		}
		return buttonDplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonDminus(){
		if( buttonDminus == null ){
			buttonDminus = new JButton("D-");
			buttonDminus.setPreferredSize(getDimButtonSize());
			//buttonDminus.setToolTipText("Desire-");
		}
		return buttonDminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonAplus(){
		if( buttonAplus == null ){
			buttonAplus = new JButton("A+");
			buttonAplus.setPreferredSize(getDimButtonSize());
			//buttonAplus.setToolTipText("Ability+");
		}
		return buttonAplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonAminus(){
		if( buttonAminus == null ){
			buttonAminus = new JButton("A-");
			buttonAminus.setPreferredSize(getDimButtonSize());
			//buttonAminus.setToolTipText("Ability-");
		}
		return buttonAminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonNplus(){
		if( buttonNplus == null ){
			buttonNplus = new JButton("N+");
			buttonNplus.setPreferredSize(getDimButtonSize());
			//buttonNplus.setToolTipText("Need+");
		}
		return buttonNplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonNminus(){
		if( buttonNminus == null ){
			buttonNminus = new JButton("N-");
			buttonNminus.setPreferredSize(getDimButtonSize());
			//buttonNminus.setToolTipText("Need-");
		}
		return buttonNminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonTSplus(){
		if( buttonTSplus == null ){
			buttonTSplus = new JButton("TS+");
			buttonTSplus.setPreferredSize(getDimButtonSize());
			//buttonTSplus.setToolTipText("Taking Steps+");
		}
		return buttonTSplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonTSminus(){
		if( buttonTSminus == null ){
			buttonTSminus = new JButton("TS-");
			buttonTSminus.setPreferredSize(getDimButtonSize());
			//buttonTSminus.setToolTipText("Taking Steps-");
		}
		return buttonTSminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonOplus(){
		if( buttonOplus == null ){
			buttonOplus = new JButton("O+");
			buttonOplus.setPreferredSize(getDimButtonSize());
			//buttonOplus.setToolTipText("Other+");
		}
		return buttonOplus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonOminus(){
		if( buttonOminus == null ){
			buttonOminus = new JButton("O-");
			buttonOminus.setPreferredSize(getDimButtonSize());
			//buttonOminus.setToolTipText("Other-");
		}
		return buttonOminus;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonFN(){
		if( buttonFN == null ){
			buttonFN = new JButton("FN");
			buttonFN.setPreferredSize(getDimButtonSize());
			//buttonFN.setToolTipText("Follow/Neutral/Ask");
		}
		return buttonFN;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JButton getButtonNC(){
		if( buttonNC == null ){
			buttonNC = new JButton("NC");
			buttonNC.setPreferredSize(getDimButtonSize());
			//buttonNC.setToolTipText("No Code");
		}
		return buttonNC;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/*public JButton getButtonDUMMY(){
		if( buttonDUMMY == null ){
			buttonDUMMY = new JButton();
			buttonDUMMY.setPreferredSize(getDimButtonSize());
			buttonDUMMY.setVisible(false);
		}
		return buttonDUMMY;
	}*/

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public JCheckBox getCheckBoxPauseUncoded(){
		if( checkBoxPauseUncoded == null ){
			checkBoxPauseUncoded = new JCheckBox("Pause if Uncoded", true);
			/*checkBoxPauseUncoded.setToolTipText("Pauses playback if current utterance " +
			"has not been assigned a MISC code value.");*/
		}
		return checkBoxPauseUncoded;
	}

	public String toString(){
		return ("MISC");
	}

	//====================================================================
	// Private Helper Methods
	//====================================================================

	private JPanel getPanelTherapistControls(){
		if( panelTherapistControls == null ){
			panelTherapistControls = new JPanel();
			panelTherapistControls.setLayout(new GridLayout(ROWS_THERAPIST,
					COLS_THERAPIST,
					BUTTON_HOR_GAP,
					BUTTON_VER_GAP));
			panelTherapistControls.setBorder(getBorderTherapist());			
			panelTherapistControls.add(getButtonCQminus());
			panelTherapistControls.add(getButtonCQ());
			panelTherapistControls.add(getButtonCQplus());
			
			panelTherapistControls.add(getButtonOQminus());
			panelTherapistControls.add(getButtonOQ());
			panelTherapistControls.add(getButtonOQplus());
			
			panelTherapistControls.add(getButtonSRminus());
			panelTherapistControls.add(getButtonSR());
			panelTherapistControls.add(getButtonSRplus());
			
			panelTherapistControls.add(getButtonCRminus());
			panelTherapistControls.add(getButtonCR());
			panelTherapistControls.add(getButtonCRplus());
			
			panelTherapistControls.add(getButtonADP());
			panelTherapistControls.add(getButtonADW());
			panelTherapistControls.add(getButtonAF());
			
			panelTherapistControls.add(getButtonCO());
			panelTherapistControls.add(getButtonDI());
			panelTherapistControls.add(getButtonEC());
			
			panelTherapistControls.add(getButtonFA());
			panelTherapistControls.add(getButtonFI());
			panelTherapistControls.add(getButtonGI());	
			
			panelTherapistControls.add(getButtonRCP());
			panelTherapistControls.add(getButtonRCW());
			panelTherapistControls.add(getButtonRF());
			
			panelTherapistControls.add(getButtonST());
			panelTherapistControls.add(getButtonSU());
			panelTherapistControls.add(getButtonWA());
		}
		return panelTherapistControls;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getPanelClientControls(){
		if( panelClientControls == null ){
			panelClientControls = new JPanel();
			panelClientControls.setLayout(new GridLayout(ROWS_CLIENT,
					COLS_CLIENT,
					BUTTON_HOR_GAP,
					BUTTON_VER_GAP));
			panelClientControls.setBorder(getBorderClient());

			panelClientControls.add(getButtonDplus());
			panelClientControls.add(getButtonDminus());
			
			panelClientControls.add(getButtonAplus());
			panelClientControls.add(getButtonAminus());
			
			panelClientControls.add(getButtonRplus());
			panelClientControls.add(getButtonRminus());
			
			panelClientControls.add(getButtonNplus());
			panelClientControls.add(getButtonNminus());
			
			panelClientControls.add(getButtonCplus());
			panelClientControls.add(getButtonCminus());
			
			panelClientControls.add(getButtonTSplus());
			panelClientControls.add(getButtonTSminus());
			
			panelClientControls.add(getButtonOplus());
			panelClientControls.add(getButtonOminus());
			
			panelClientControls.add(getButtonFN());
			//panelClientControls.add(this.getButtonDUMMY());
			panelClientControls.add(this.getButtonNC());
			panelClientControls.add(this.getCheckBoxPauseUncoded());
		}
		return panelClientControls;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private TitledBorder getBorderWindow(){
		if( borderWindow == null ){
			borderWindow = BorderFactory.createTitledBorder("MISC Coding Template");
			borderWindow.setTitleJustification(TitledBorder.CENTER);
			borderWindow.setTitleColor(Color.BLACK);
		}
		return borderWindow;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getPanelButtons(){
		if( panelButtons == null ){
			panelButtons = new JPanel();
			panelButtons.setBorder(getBorderButtons());
			panelButtons.setLayout(new FlowLayout());
			panelButtons.add(getPanelTherapistControls());
			panelButtons.add(getPanelClientControls());
		}
		return panelButtons;
	}

	private TitledBorder getBorderButtons(){
		if( borderButtons == null ){
			borderButtons = BorderFactory.createTitledBorder("MISC Coding Controls");
			borderButtons.setTitleJustification(TitledBorder.LEADING);
		}
		return borderButtons;
	}

	private Dimension getDimButtonSize(){
		if( dimButtonSize == null ){
			dimButtonSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
		}
		return dimButtonSize;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JTextField getTextFieldOrder(){
		if( textFieldOrder == null ){
			textFieldOrder = new JTextField(ORDER_COLS);
			textFieldOrder.setEditable(false);
		}
		return textFieldOrder;
	}

	public void setTextFieldOrder(int order){
		getTextFieldOrder().setText("" + order );
	}

	private JLabel getLabelOrder(){
		if( labelOrder == null ){
			labelOrder = new JLabel("Enumeration");
			labelOrder.setLabelFor(getTextFieldOrder());
		}
		return labelOrder;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JTextField getTextFieldCode(){
		if( textFieldCode == null ){
			textFieldCode = new JTextField(CODE_COLS);
			textFieldCode.setEditable(false);
		}
		return textFieldCode;
	}

	public void setTextFieldCode(String utteranceString){
		getTextFieldCode().setText(utteranceString);
	}

	private JLabel getLabelCode(){
		if( labelCode == null ){
			labelCode = new JLabel("MISC Code");
			labelCode.setLabelFor(getTextFieldOrder());
		}
		return labelCode;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JTextField getTextFieldStartTime(){
		if( textFieldStartTime == null ){
			textFieldStartTime = new JTextField(TIME_COLS);
			textFieldStartTime.setEditable(false);
		}
		return textFieldStartTime;
	}

	public void setTextFieldStartTime(String utteranceString){
		getTextFieldStartTime().setText(utteranceString);
	}

	private JLabel getLabelStart(){
		if( labelStart == null ){
			labelStart = new JLabel("Starting TimeCode");
			labelStart.setLabelFor(getTextFieldStartTime());
		}
		return labelStart;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JTextField getTextFieldEndTime(){
		if( textFieldEndTime == null ){
			textFieldEndTime = new JTextField(TIME_COLS);
			textFieldEndTime.setEditable(false);
		}
		return textFieldEndTime;
	}

	public void setTextFieldEndTime(String utteranceString){
		getTextFieldEndTime().setText(utteranceString);
	}

	private JLabel getLabelEnd(){
		if( labelEnd == null ){
			labelEnd = new JLabel("Ending TimeCode");
			labelEnd.setLabelFor(getTextFieldEndTime());
		}
		return labelEnd;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JTextField getTextFieldNext(){
		if( textFieldNext == null ){
			textFieldNext = new JTextField(NEXT_COLS);
			textFieldNext.setEditable(false);
		}
		return textFieldNext;
	}

	public void setTextFieldNext(String utteranceString){
		getTextFieldNext().setText(utteranceString);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getPanelCurrentText(){
		if( panelCurrentText == null ){
			panelCurrentText = new JPanel();
			panelCurrentText.setBorder(getBorderTextFields());
			panelCurrentText.setLayout(new GridBagLayout());

			GridBagConstraints orderC = new GridBagConstraints();
			orderC.gridx = 0;
			orderC.gridy = 0;
			orderC.weightx = 1.0;
			orderC.anchor = GridBagConstraints.LINE_START;
			panelCurrentText.add(getLabelOrder(), orderC);

			GridBagConstraints codeC = new GridBagConstraints();
			codeC.gridx = 1;
			codeC.gridy = 0;
			codeC.weightx = 1.0;
			codeC.anchor = GridBagConstraints.LINE_START;
			panelCurrentText.add(getLabelCode(), codeC);

			GridBagConstraints startC = new GridBagConstraints();
			startC.gridx = 2;
			startC.gridy = 0;
			startC.weightx = 1.0;
			startC.anchor = GridBagConstraints.LINE_START;
			panelCurrentText.add(getLabelStart(), startC);

			GridBagConstraints endC = new GridBagConstraints();
			endC.gridx = 3;
			endC.gridy = 0;
			endC.weightx = 1.0;
			endC.anchor = GridBagConstraints.LINE_START;
			panelCurrentText.add(getLabelEnd(), endC);

			GridBagConstraints orderTC = new GridBagConstraints();
			orderTC.gridx = 0;
			orderTC.gridy = 1;
			orderTC.weightx = 1.0;
			orderTC.anchor = GridBagConstraints.LINE_START;
			panelCurrentText.add(getTextFieldOrder(), orderTC);

			GridBagConstraints codeTC = new GridBagConstraints();
			codeTC.gridx = 1;
			codeTC.gridy = 1;
			codeTC.weightx = 1.0;
			codeTC.anchor = GridBagConstraints.LINE_START;
			panelCurrentText.add(getTextFieldCode(), codeTC);

			GridBagConstraints startTC = new GridBagConstraints();
			startTC.gridx = 2;
			startTC.gridy = 1;
			startTC.weightx = 1.0;
			startTC.anchor = GridBagConstraints.LINE_START;
			panelCurrentText.add(getTextFieldStartTime(), startTC);

			GridBagConstraints endTC = new GridBagConstraints();
			endTC.gridx = 3;
			endTC.gridy = 1;
			endTC.weightx = 1.0;
			endTC.anchor = GridBagConstraints.LINE_START;
			panelCurrentText.add(getTextFieldEndTime(), endTC);
		}
		return panelCurrentText;
	}

	private TitledBorder getBorderTextFields(){
		if( borderTextFields == null ){
			borderTextFields = BorderFactory.createTitledBorder("Current Utterance");
			borderTextFields.setTitleJustification(TitledBorder.LEADING);
		}
		return borderTextFields;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private JPanel getPanelNextText(){
		if( panelLastText == null ){
			panelLastText = new JPanel();
			panelLastText.setBorder(getBorderNext());
			panelLastText.setLayout(new BorderLayout());
			panelLastText.add(getTextFieldNext(), BorderLayout.CENTER);
		}
		return panelLastText;
	}

	private TitledBorder getBorderNext(){
		if( borderNext == null ){
			borderNext = BorderFactory.createTitledBorder("Next Utterance");
			borderNext.setTitleJustification(TitledBorder.LEADING);
		}
		return borderNext;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private TitledBorder getBorderTherapist(){
		if( borderTherapist == null ){
			borderTherapist = BorderFactory.createTitledBorder(
			"Therapist Codes");
			borderTherapist.setTitleJustification(TitledBorder.LEADING);
		}
		return borderTherapist;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private TitledBorder getBorderClient(){
		if( borderClient == null ){
			borderClient = BorderFactory.createTitledBorder("Client Codes");
			borderClient.setTitleJustification(TitledBorder.LEADING);
		}
		return borderClient;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private Dimension getDimMainPanel(){
		if( dimMainPanel == null ){
			dimMainPanel = new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
		}
		return dimMainPanel;
	}

}

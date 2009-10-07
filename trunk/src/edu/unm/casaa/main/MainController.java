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

package edu.unm.casaa.main;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.unm.casaa.globals.GlobalTemplateUiService;
import edu.unm.casaa.globals.GlobalTemplateView;
import edu.unm.casaa.misc.MiscDataItem;
import edu.unm.casaa.misc.MiscTemplateUiService;
import edu.unm.casaa.misc.MiscTemplateView;
import edu.unm.casaa.utterance.ParserTemplateUiService;
import edu.unm.casaa.utterance.ParserTemplateView;
import edu.unm.casaa.utterance.Utterance;
import edu.unm.casaa.utterance.UtteranceQueue;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

public class MainController implements BasicPlayerListener {

	//====================================================================
	// Fields
	//====================================================================

	//GUI
	private PlayerView 			playerView 		= null;
	private JPanel 				templateView	= null;
	private TemplateUiService 	templateUI		= null;
	JFileChooser 				chooser			= null;

	private boolean inUtterance				= false;
	private File 	fileParse				= null;
	private String 	filenameParse			= null;
	private File 	fileMisc				= null;
	private String 	filenameMisc			= null;
	private File 	fileGlobals				= null;
	private String 	filenameGlobals			= null;
	private String 	filenameBackup			= null;

	//Audio Player back-end
	private BasicPlayer 	basicPlayer		= null;
	private BasicController control			= null;

	private File 	fileAudio				= null;
	private String 	filenameAudio			= null;

	private Timer timer						= null;
	private static final int TIMER_DELAY 	= 250;
	private static final int INIT_DELAY 	= 1000;
	//private static final int REPLAY_SEC 	= 11;

	private String strStatus				= null;

	//DataQueue back-end
	private UtteranceQueue queue			= null;
	private Utterance currentUtterance		= null;
	private Utterance nextUncodedUtterance	= null;
	private boolean allowUpdate				= false;
	private boolean pauseOnUncoded			= true;
	private boolean waitingForCode		 	= false;


	//====================================================================
	// Main, Constructor and Initialization Methods
	//====================================================================

	public MainController(){
		init();		
	}

	private void init(){
		basicPlayer = new BasicPlayer();
		playerView = new PlayerView();
		control = (BasicController) basicPlayer;

		basicPlayer.addBasicPlayerListener(this);
		registerPlayerViewListeners();
		// Handle window closing events.
		playerView.getFrameParentWindow().addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				actionExit();
			}
		});
	}

	private void actionExit(){
		if( inUtterance ){
			handleButtonEndParse();
		}
		this.saveCurrentTextFile();
		System.exit(0);
	}

	private void pokeTimer(){
		//This does not provide the file time to the player
		if( timer != null ){
			timer.cancel();
		}
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				setTimeDisplay();
				setSeekSliderDisplay();
			}
		}, INIT_DELAY, TIMER_DELAY);
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MainController();
	}


	//====================================================================
	// Public Getter and Setter Methods
	//====================================================================

	public void setPauseOnUncoded( boolean value ){
		pauseOnUncoded = value;
	}


	//====================================================================
	// Private Helper Methods
	//====================================================================

	private void registerPlayerViewListeners() {
		// Player GUI listeners	

		//Play Button
		playerView.getButtonPlay().addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				handleButtonPlay();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Stop Button
		playerView.getButtonStop().addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				handleButtonStop();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Backup Button
		playerView.getButtonBackup().addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				handleButtonBackup();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Pause Button
		playerView.getButtonPause().addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				handleButtonPause();
			}
		});


		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Replay button
		playerView.getButtonReplay().addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				handleButtonReplay();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Pan Slider
		playerView.getSliderPan().addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent ce){
				if( ! playerView.getSliderPan().getValueIsAdjusting()){
					handleSliderPan();
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Gain Slider
		playerView.getSliderGain().addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent ce){
				if( ! playerView.getSliderGain().getValueIsAdjusting()){
					handleSliderGain();
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Seek Slider
		playerView.getSliderSeek().addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent ce){
				if( playerView.getSliderSeek().getValueIsAdjusting()){
					handleSliderSeek();
				}
			}
		});

		//================================================================
		// Menu Listeners

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// File Menu: Load Audio File
		playerView.getMenuItemLoadAudio().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				handleLoadAudioFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// File Menu: Exit
		playerView.getMenuItemExit().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//Need to check if queue has been saved first
				System.exit(0);
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Parse Utterances Menu: Start New Parse File
		playerView.getMenuItemNewParse().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				handleNewParseFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Parse Utterances Menu: Load Parse File
		playerView.getMenuItemLoadParse().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				handleLoadParseFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Code Utterances Menu: Start New Code File
		playerView.getMenuItemNewCode().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				handleNewCodeFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Code Utterances Menu: Load Code File
		playerView.getMenuItemLoadCode().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				handleLoadCodeFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Global Ratings Menu: Score Global Ratings
		playerView.getMenuItemCodeGlobals().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				handleNewGlobalRatings();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// About Menu: Help
		playerView.getMenuItemHelp().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showNotImplementedDialog();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// About Menu: About this Application
		playerView.getMenuItemAbout().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				handleAboutWindow();
			}
		});

	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Player Handlers
	private void handleButtonPlay(){		
		if( basicPlayer.getStatus() == BasicPlayer.PAUSED ){
			//resume
			try {
				control.resume();
			} catch (BasicPlayerException e) {
				e.printStackTrace();
			}
		}
		else{
			try{
				control.play();
				//set volume and pan according to GUI
				this.handleSliderGain();
				this.handleSliderPan();
			}catch (BasicPlayerException e){
				e.printStackTrace();
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleButtonStop(){
		if( inUtterance ){
			handleButtonEndParse();
		}
		try{
			control.stop();
		}catch (BasicPlayerException e){
			e.printStackTrace();
		}
		//TODO: give user save option, to prevent overwrite.
		//		offer a backup option instead of save.
		saveCurrentTextFile();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleButtonBackup(){
		//System.out.println("DEBUG: 0) call to backup: status = " + basicPlayer.getStatus() );
		if( ! (basicPlayer.getStatus() == BasicPlayer.PLAYING ||
				basicPlayer.getStatus() == BasicPlayer.PAUSED) ){
			//System.out.println("DEBUG: 0a) basicPlayer.getStatus() = " + basicPlayer.getStatus());
			this.showBackupErrorDialog();
			return;
		}
		if( basicPlayer.getStatus() == BasicPlayer.PLAYING ){
			try {
				control.pause();
			} catch (BasicPlayerException e) {
				e.printStackTrace();
			}
		}

		if( inUtterance ){
			handleButtonEndParse();
		}

		saveCurrentTextFile();
		//System.out.println("DEBUG: 1) call to copy");
		copyTextFileToBackupFile();
		reloadQueueFromFile();
		this.showBackupCompleteDialog();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleButtonPause(){
		if( basicPlayer.getStatus() == BasicPlayer.PLAYING ){
			try{
				control.pause();
			}catch (BasicPlayerException e){
				e.printStackTrace();
			}
		}else if( basicPlayer.getStatus() == BasicPlayer.PAUSED ){		
			//set volume and pan according to GUI
			this.handleSliderGain();
			this.handleSliderPan();

			//resume
			try {
				control.resume();
			} catch (BasicPlayerException e) {
				e.printStackTrace();
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleButtonReplay(){
		if( templateView instanceof ParserTemplateView ){
			killLastUtterance();
		}
		else if( templateView instanceof MiscTemplateView ){
			//skip back to last utterance
			//queue.stripCodeFromCurrentUtterance();
			//this.setToCurrentUtterance();

			//go back two
			//queue.getPreviousUtterance();
			currentUtterance = queue.getPreviousUtterance();
			//System.out.println("1).currentUtterance = " + currentUtterance.toString());
			queue.stripCodeFromCurrentUtterance();
			//System.out.println("2).currentUtterance = " + currentUtterance.toString());

			if( currentUtterance == null ){
				currentUtterance = queue.getFirstUtterance();
			}
			/*if( currentUtterance == null ){
				currentUtterance = getDataQueue().getLastCodedUtterance();
			}*/
			//nextUncodedUtterance = getDataQueue().getUtteranceAt(currentUtterance.getEnum()-1);
			//System.out.println("DEBUG: 1st currentUtterance enum = " + currentUtterance.getEnum());
			currentUtterance = queue.getCurrentUtterance();
			//System.out.println("DEBUG: 2nd currentUtterance enum = " + currentUtterance.getEnum());
			nextUncodedUtterance = getDataQueue().getNextUtteranceNoAdvance();
			//System.out.println("DEBUG: nextUncodedUtterance enum = " + nextUncodedUtterance.getEnum());
			((MiscTemplateView) templateView).setTextFieldNext(getNextUncodedUtterance().toString());
			((MiscTemplateView) templateView).setTextFieldOrder(currentUtterance.getEnum());
			//if( currentUtterance.getCodeVal() == -1 ){
			((MiscTemplateView) templateView).setTextFieldCode("");
			/*}
			else{
				((MiscTemplateView) templateView).setTextFieldCode(
						currentUtterance.getCodeAbbrv(currentUtterance.getCodeVal()));
			}*/
			currentUtterance.setCodeVal(-1);
			((MiscTemplateView) templateView).setTextFieldStartTime(currentUtterance.getStartTime());
			((MiscTemplateView) templateView).setTextFieldEndTime(currentUtterance.getEndTime());

			// Setting this to reset audio at start time rather than end time per request
			int secs = new TimeCode(currentUtterance.getStartTime()).convertTimeCodeStringToSecs();
			try {
				control.seek((long) secs * basicPlayer.getBytesPerSecond());
			} catch (BasicPlayerException e) {
				showAudioFileNotSeekableDialog();
				e.printStackTrace();
			}
		}
		else{
			this.showParsingErrorDialog();
		}
		this.pokeTimer();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleSliderSeek(){
		if( basicPlayer.getStatus() == BasicPlayer.UNKNOWN ){
			return;
		}
		else{
			double sliderPct = new Integer(playerView.getSliderSeek().getValue())
			.doubleValue() / new Integer(PlayerView.SEEK_MAX_VAL).doubleValue();
			int skipAmt = (int) (sliderPct * basicPlayer.getEncodedLength());
			skipAmt = skipAmt - (skipAmt % basicPlayer.getBytesPerSecond());
			try {
				skipAmt = (int) (control.seek((long) skipAmt));
				if( (skipAmt % basicPlayer.getBytesPerSecond()) != 0 ){
					// NO OP - Skip error
				}
				pokeTimer();
			} catch (BasicPlayerException e) {
				e.printStackTrace();
			}
		}
	}

	private void setSeekSliderDisplay(){
		Integer position = new Integer(basicPlayer.getEncodedStreamPosition());
		Integer length = new Integer(basicPlayer.getEncodedLength());
		if( (position.doubleValue() / length.doubleValue()) == 1.0 ){
			playerView.setSliderSeek(PlayerView.SEEK_MAX_VAL);
		}
		else if( (position.doubleValue() / length.doubleValue()) == 0 ){
			playerView.setSliderSeek(0);
		}
		else{
			playerView.setSliderSeek(new Double((position.doubleValue() / 
					length.doubleValue()) *
					PlayerView.SEEK_MAX_VAL).intValue());
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleSliderPan(){
		Long valSliderPan = new Long((long)playerView.
				getSliderPan().getValue());
		double dPanVal = valSliderPan.doubleValue() / 10;
		try{
			control.setPan(dPanVal);
		}catch (BasicPlayerException e){
			e.printStackTrace();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleSliderGain(){
		Long valSliderGain = new Long((long)playerView.
				getSliderGain().getValue());
		double dGainVal = valSliderGain.doubleValue() / 100;
		try{
			control.setGain(dGainVal);
		}catch (BasicPlayerException e){
			e.printStackTrace();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Menu Handlers
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleMenuParseUtterances(){
		playerView.setPanelTemplate(getTemplateView("PARSER"));
		//registerParserTemplateListeners();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleMenuCodeUtterances(){
		playerView.setPanelTemplate(getTemplateView("MISC"));
		//registerMiscTemplateListeners();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleMenuGlobalRatings(){
		playerView.setPanelTemplate(getTemplateView("GLOBALS"));
		//registerGlobalTemplateListeners();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewParseFile(){
		queue = null;
		chooser = new JFileChooser();
		chooser.setDialogTitle("Select Save Directory and Enter a Filename for the PARSE File");
		//filter supported files
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"PARSE files", "parse");
		chooser.setFileFilter(filter);
		chooser.setToolTipText("Use \".parse\" as the file extension.");
		int returnVal = chooser.showSaveDialog(playerView);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			filenameParse = chooser.getSelectedFile().getAbsolutePath();
			filenameParse = correctTextFileType(".parse", filenameParse);
			fileParse = new File(filenameParse);
		}
		else{
			return;
		}

		handleLoadAudioFile();
		handleMenuParseUtterances();		
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewCodeFile(){
		queue = null;
		chooser = new JFileChooser();
		chooser.setDialogTitle("Select Save Directory and Enter a Filename for the CASAA File");
		//filter supported files
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"PARSE files", "parse");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(playerView);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			filenameParse = chooser.getSelectedFile().getAbsolutePath();
			fileParse = new File(filenameParse);
			filenameMisc = correctTextFileType(".casaa", filenameParse);
			fileMisc = new File(filenameMisc);
			try {
				copyParseFileToCodeFile();
			} catch (IOException e) {
				e.printStackTrace();
				this.showFileNotCreatedDialog();
			}
		}
		else{
			return;
		}
		//Load the parse file
		handleMenuCodeUtterances();	
		filenameAudio = getDataQueue().loadFromFile(fileMisc);
		getNextUncodedUtterance();
		//load the audio file
		fileAudio = new File(filenameAudio);
		try{
			control.open(fileAudio);
			pokeTimer();
		}catch (BasicPlayerException e){
			showAudioFileNotFoundDialog();
			e.printStackTrace();
		}
		// set the audio and parse file at the first position
		((MiscTemplateView) templateView).setTextFieldNext(
				getDataQueue().getFirstUtterance().toString());
		//TODO: Set to add codes to queue data items from beginning of files

	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewGlobalRatings(){
		chooser = new JFileChooser();
		chooser.setDialogTitle("Select Save Directory and Enter a Filename");
		//filter supported files
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"GLOBALS files", "global");
		chooser.setFileFilter(filter);
		chooser.setToolTipText("Use \".global\" as the file extension.");
		int returnVal = chooser.showSaveDialog(playerView);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			filenameGlobals = chooser.getSelectedFile().getAbsolutePath();
			filenameGlobals = correctTextFileType(".global", filenameGlobals);
			fileGlobals = new File(filenameGlobals);
		}
		else{
			return;
		}
		System.out.println("DEBUG: filenameGlobals = " + filenameGlobals + " fileGlobals = " + fileGlobals.getAbsolutePath());
		handleLoadAudioFile();
		handleMenuGlobalRatings();		
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private Utterance getNextUncodedUtterance(){
		if( getDataQueue() == null ){
			System.out.println("Queue not initialized in next call");
		}
		if( nextUncodedUtterance == null ){
			nextUncodedUtterance = getDataQueue().getFirstUtterance();
		}
		else{
			Utterance testUtterance = getDataQueue().getNextUtterance();
			if( testUtterance != null ){
				nextUncodedUtterance = testUtterance;
			}
			else{
				return null;
			}
		}
		return nextUncodedUtterance;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleLoadParseFile(){
		queue = null;
		chooser = new JFileChooser();
		chooser.setDialogTitle("Select a PARSE File to Load");
		//filter supported files
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"PARSE files", "parse");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(playerView);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			filenameParse = chooser.getSelectedFile().getAbsolutePath();
			fileParse = new File(filenameParse);
		}
		else{
			return;
		}
		//Load the parse file
		handleMenuParseUtterances();
		filenameAudio = getDataQueue().loadFromFile(fileParse);
		if( filenameAudio.equalsIgnoreCase("ERROR: No Audio File Listed") ){
			showAudioFileNotFoundDialog();
			return;
		}
		//load the audio file
		fileAudio = new File(filenameAudio);
		try{
			control.open(fileAudio);
			pokeTimer();
		}catch (BasicPlayerException e){
			showAudioFileNotFoundDialog();
			e.printStackTrace();
		}
		// set the audio and parse file at the last position
		setToCurrentUtterance();	
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleLoadCodeFile(){
		queue = null;
		chooser = new JFileChooser();
		chooser.setDialogTitle("Select a CASAA Code File to Load");
		//filter supported files
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"CASAA code files", "casaa");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(playerView);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			filenameMisc = chooser.getSelectedFile().getAbsolutePath();
			fileMisc = new File(filenameMisc);
		}
		else{
			return;
		}
		//Load the code file
		//queue = new UtteranceQueue();
		handleMenuCodeUtterances();
		filenameAudio = getDataQueue().loadFromFile(fileMisc);
		//load the audio file
		fileAudio = new File(filenameAudio);
		try{
			control.open(fileAudio);
			pokeTimer();
		}catch (BasicPlayerException e){
			showAudioFileNotFoundDialog();
			e.printStackTrace();
		}
		// set the audio and parse file at the last position
		//  needs to set to last *coded* utterance, not last utterance in file
		setToCurrentUtterance();
		// this method will have to rely on an Update method.
		//	see notes in handleNewCodeFile()
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private String correctTextFileType(String fileType, String filename){
		if( filename.endsWith(fileType) ){
			//no changes needed
			return filename;
		}
		else if( fileType.equalsIgnoreCase(".parse") ){
			//add .parse to parse files that need it
			return filename.concat(fileType);
		}
		else if( filename.endsWith(".parse") && fileType.equalsIgnoreCase(".casaa") ){
			//rename file type from .parse to .casaa
			return filename.substring(0, 
					(filename.length() - ".parse".length())).concat(fileType);
		}
		else{
			return filename.concat(fileType);
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleLoadAudioFile(){
		chooser = new JFileChooser();
		chooser.setDialogTitle("Select an Audio File to Load");
		//filter supported files
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"WAV Audio only for coding", "wav");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(playerView);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			filenameAudio = chooser.getSelectedFile().getAbsolutePath();
			fileAudio = new File(filenameAudio);
		}
		else{
			return;
		}

		try{
			control.open(fileAudio);
			pokeTimer();
		}catch (BasicPlayerException e){
			e.printStackTrace();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void saveCurrentTextFile(){
		if( (queue != null) || (templateView instanceof GlobalTemplateView) ){
			if( templateView instanceof ParserTemplateView &&
					fileParse != null ){
				getDataQueue().writeToFile(fileParse, filenameAudio);
			}
			else if( templateView instanceof MiscTemplateView &&
					fileMisc != null ){
				getDataQueue().writeToFile(fileMisc, filenameAudio);
			}
			else if( templateView instanceof GlobalTemplateView ){
				((GlobalTemplateUiService)templateUI).writeGlobalsToFile(fileGlobals);
			}
			else if( templateView == null ){
				//no op
				//reached if only playing audio file, and stop pressed.
			}
			else{
				JOptionPane.showMessageDialog(playerView,
						"There is No Reference to a PARSE or CASAA File to Save.\n" +
						"Please Report This Error to the Developer for Repair.",
						"Error Notification", JOptionPane.ERROR_MESSAGE);
			}
			queue = null;
			currentUtterance = null;
			nextUncodedUtterance = null;
		}
	}

	private String getFilenameBackup(){
		if( templateView instanceof ParserTemplateView ){
			try {
				filenameBackup = fileParse.getCanonicalPath().concat( ".BACKUP_" + System.currentTimeMillis() );;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if( templateView instanceof MiscTemplateView ){
			try {
				filenameBackup = fileMisc.getCanonicalPath().concat( ".BACKUP_" + System.currentTimeMillis() );;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if( templateView instanceof GlobalTemplateView ){
			try {
				filenameBackup = fileGlobals.getCanonicalPath().concat( ".BACKUP_" + System.currentTimeMillis() );;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			showBackupErrorDialog();
			return "ERROR_CREATING_BACKUP_FILE";
		}
		return filenameBackup;
	}

	private void copyTextFileToBackupFile(){
		//File fileBackup;
		String srcFilename;
		if( templateView instanceof ParserTemplateView ){
			srcFilename = filenameParse;
		}
		else if( templateView instanceof MiscTemplateView ){
			srcFilename = filenameMisc;
		}
		else if( templateView instanceof GlobalTemplateView ){
			srcFilename = filenameGlobals;
		}
		else{
			showBackupErrorDialog();
			return;
		}
		//System.out.println("DEBUG: 2) in copy: srcFilename = " + srcFilename);
		if( getFilenameBackup().equalsIgnoreCase("ERROR_CREATING_BACKUP_FILE") ){
			//fileBackup = null;
			return;
		}
		else{
			//System.out.println("DEBUG: 3) attempt to copy: filenameBackup = " + filenameBackup);
			File fileBackup = new File(filenameBackup);
			try {
				FileChannel srcChannel = new FileInputStream(srcFilename).getChannel();
				FileChannel dstChannel = new FileOutputStream(fileBackup).getChannel();
				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				srcChannel.close();
				dstChannel.close();
			} catch (IOException e) {
			}
		}
		//fileBackup = null;
	}

	private void reloadQueueFromFile(){
		String strAudioFile;
		if( templateView instanceof ParserTemplateView ){
			strAudioFile = getDataQueue().loadFromFile(fileParse);
			assert(strAudioFile == fileParse.toString());
		}
		else if( templateView instanceof MiscTemplateView ){
			strAudioFile = getDataQueue().loadFromFile(fileMisc);
			assert(strAudioFile == fileMisc.toString());
		}
		else{
			this.showQueueNotLoadedDialog();
		}
		//System.out.println("DEBUG: queue = " + queue );
		this.setToCurrentUtterance();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleAboutWindow(){
		AboutWindowView aboutWindow = new AboutWindowView();
		aboutWindow.setFocusable(true);
	}

	//====================================================================
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Other Stuff
	private void showNotImplementedDialog(){
		JOptionPane.showMessageDialog(playerView,
				"This Feature Has Not Been Implemented Yet.\n" +
				"Please Check for it in a Future Release.",
				"Beta Release Notification", JOptionPane.ERROR_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void showAudioFileNotFoundDialog(){
		JOptionPane.showMessageDialog(playerView,
				"The audio file:\n" + filenameAudio + 
				"\nassociated with this project cannot be located.\n" +
				"Please verify that this file exists, and that it is named correctly.",
				"Audio File Not Found Error", JOptionPane.ERROR_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void showAudioFileNotSeekableDialog(){
		JOptionPane.showMessageDialog(playerView,
				"The audio file:\n" + filenameAudio + 
				"\nfailed when setting the play position in the file.\n" +
				"Please try to reload the file.",
				"Audio File Seek Error", JOptionPane.ERROR_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void showFileNotCreatedDialog(){
		JOptionPane.showMessageDialog(playerView,
				"The file:\n" + filenameMisc + 
				"\nfailed to be modified or created.\n" +
				"Please try to rename or reload the file manually.",
				"File Error", JOptionPane.ERROR_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void showQueueNotLoadedDialog(){
		JOptionPane.showMessageDialog(playerView,
				"The Data Queue failed to load.\n" + 
				"Please verify the text file is properly formatted.",
				"Data Queue Loading Error", JOptionPane.ERROR_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void showBackupErrorDialog(){
		JOptionPane.showMessageDialog(playerView,
				"The Data Queue failed to backup to file.\n" + 
				"Backup can only be called while player is playing or paused.\n" +
				"while using the parsign or coding features.",
				"Backup Error", JOptionPane.ERROR_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void showBackupCompleteDialog(){
		JOptionPane.showMessageDialog(playerView,
				"The backup to file is complete.\n" + 
				"Please press Pause or Play to resume.",
				"Backup Complete", JOptionPane.INFORMATION_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void showTemplateNotFoundDialog(){
		JOptionPane.showMessageDialog(playerView,
				"The Coding Template Failed to Load.\n",
				"Coding Template Loading Error", JOptionPane.ERROR_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void showParsingErrorDialog(){
		JOptionPane.showMessageDialog(playerView,
				"An error occurred while parsing this utterance.\n",
				"Utterance Parsing Error", JOptionPane.ERROR_MESSAGE);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void setTimeDisplay(){
		if( basicPlayer.getBytesPerSecond() != 0 ){
			//Handles constant bit-rates only
			//set the player display
			int startBytes = basicPlayer.getEncodedStreamPosition();
			String strTimeCode = new TimeCode( (startBytes /
					basicPlayer.getBytesPerSecond())).
					convertToTimeString();
			playerView.setLabelTime( "Time:  " + strTimeCode );
			//update the misc queue if needed
			if( templateView instanceof MiscTemplateView && allowUpdate ){
				//could send int bytes here instead		
				updateMiscDisplay(startBytes);
			}
		}
		else{
			//get time based on frames rather than bytes
			//need a way to determine current position based on frames
			//something like getEncodedStreamPosition(),
			//but that returns frames.  This for VBR type compressions.
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void copyParseFileToCodeFile() throws IOException {
		InputStream in = new FileInputStream(fileParse);
		OutputStream out = new FileOutputStream(fileMisc);
		byte[] buffer = new byte[1024];
		int length;
		while ( (length = in.read(buffer)) > 0 ){
			out.write(buffer, 0, length);
		}
		in.close();
		out.close();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void nullifyLastTemplateView(){
		templateView = null;
		templateUI = null;
		System.gc();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//TODO: Is this a getter or a setter - smelly code
	private JPanel getTemplateView(String strTemplate){
		if( strTemplate == "PARSER" ){
			nullifyLastTemplateView();
			templateUI = new ParserTemplateUiService(this);
			templateView = templateUI.getTemplateView();
		}
		else if(strTemplate == "MISC" ){
			nullifyLastTemplateView();
			allowUpdate = true;
			templateUI = new MiscTemplateUiService(this);
			templateView = templateUI.getTemplateView();
		}
		else if(strTemplate == "GLOBALS" ){
			nullifyLastTemplateView();
			templateUI = new GlobalTemplateUiService();
			templateView = templateUI.getTemplateView();
		}
		else{
			nullifyLastTemplateView();
		}
		return templateView;
	}

	//====================================================================
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Parser Template Handlers

	//TODO: Disable buttons or alert if no PARSE file loaded

	public void handleButtonStartParse(){
		//get the data
		if( inUtterance ){
			handleButtonEndParse();
		}
		int startByteCode = basicPlayer.getEncodedStreamPosition();
		String start = new TimeCode( (startByteCode / 
				basicPlayer.getBytesPerSecond()) ).convertToTimeString();
		if( getDataQueue().getCurrentUtterance() != null ){
			((ParserTemplateView) playerView.getPanelTemplate()).
			setTextFieldLast(getDataQueue().getCurrentUtterance().toString());
		}
		((ParserTemplateView) playerView.getPanelTemplate()).setTextFieldEndTime("");
		inUtterance = true;
		//save the data
		int order = getDataQueue().getNextAvailableIndex();
		//System.out.println("DEBUG: next index = " + order);
		Utterance data = new MiscDataItem(order, start, startByteCode);
		getDataQueue().addUtterance(data);
		//report the data
		((ParserTemplateView) playerView.getPanelTemplate()).setTextFieldOrder(order);
		((ParserTemplateView) playerView.getPanelTemplate()).setTextFieldStartTime(start);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonEndParse(){
		//get the data
		int endBytes = basicPlayer.getEncodedStreamPosition();
		String end = new TimeCode( ( endBytes /
				basicPlayer.getBytesPerSecond()) ).convertToTimeString();
		inUtterance = false;
		//save the data
		if( getDataQueue().getCurrentUtterance() != null ){
			getDataQueue().getCurrentUtterance().setEndTime(end);
			getDataQueue().getCurrentUtterance().setEndBytes(endBytes);
			//report the data
			((ParserTemplateView) playerView.getPanelTemplate()).setTextFieldEndTime(end);
		}
		else{
			showParsingErrorDialog();
		}
	}

	//====================================================================
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Globals Template Listeners
	/*private void registerGlobalTemplateListeners(){
		//TODO
	}*/

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Globals Template Handlers
	//TODO

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// MISC Template Handlers

	//Therapist Coding Buttons
	public void handleButtonADP(){
		currentUtterance.setCodeVal("ADP");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("ADP");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonADW(){
		currentUtterance.setCodeVal("ADW");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("ADW");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonAF(){
		currentUtterance.setCodeVal("AF");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("AF");		
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonCO(){
		currentUtterance.setCodeVal("CO");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("CO");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonDI(){
		currentUtterance.setCodeVal("DI");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("DI");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonEC(){
		currentUtterance.setCodeVal("EC");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("EC");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonFA(){
		currentUtterance.setCodeVal("FA");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("FA");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonFI(){
		currentUtterance.setCodeVal("FI");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("FI");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonGI(){
		currentUtterance.setCodeVal("GI");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("GI");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonCQminus(){
		currentUtterance.setCodeVal("CQ-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("CQ-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonCQ(){
		currentUtterance.setCodeVal("CQ0");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("CQ0");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonCQplus(){
		currentUtterance.setCodeVal("CQ+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("CQ+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonOQminus(){
		currentUtterance.setCodeVal("OQ-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("OQ-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonOQ(){
		currentUtterance.setCodeVal("OQ0");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("OQ0");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonOQplus(){
		currentUtterance.setCodeVal("OQ+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("OQ+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonRCP(){
		currentUtterance.setCodeVal("RCP");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("RCP");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonRCW(){
		currentUtterance.setCodeVal("RCW");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("RCW");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonSRminus(){
		currentUtterance.setCodeVal("SR-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("SR-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonSR(){
		currentUtterance.setCodeVal("SR0");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("SR0");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonSRplus(){
		currentUtterance.setCodeVal("SR+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("SR+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonCRminus(){
		currentUtterance.setCodeVal("CR-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("CR-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonCR(){
		currentUtterance.setCodeVal("CR0");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("CR0");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonCRplus(){
		currentUtterance.setCodeVal("CR+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("CR+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonRF(){
		currentUtterance.setCodeVal("RF");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("RF");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonSU(){
		currentUtterance.setCodeVal("SU");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("SU");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonST(){
		currentUtterance.setCodeVal("ST");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("ST");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonWA(){
		currentUtterance.setCodeVal("WA");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("WA");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//Client Coding Buttons
	public void handleButtonCplus(){
		currentUtterance.setCodeVal("C+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("C+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonCminus(){
		currentUtterance.setCodeVal("C-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("C-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonRplus(){
		currentUtterance.setCodeVal("R+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("R+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonRminus(){
		currentUtterance.setCodeVal("R-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("R-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonDplus(){
		currentUtterance.setCodeVal("D+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("D+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonDminus(){
		currentUtterance.setCodeVal("D-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("D-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonAplus(){
		currentUtterance.setCodeVal("A+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("A+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonAminus(){
		currentUtterance.setCodeVal("A-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("A-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonNplus(){
		currentUtterance.setCodeVal("N+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("N+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonNminus(){
		currentUtterance.setCodeVal("N-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("N-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonTSplus(){
		currentUtterance.setCodeVal("TS+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("TS+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonTSminus(){
		currentUtterance.setCodeVal("TS-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("TS-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonOplus(){
		currentUtterance.setCodeVal("O+");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("O+");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonOminus(){
		currentUtterance.setCodeVal("O-");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("O-");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonFN(){
		currentUtterance.setCodeVal("FN");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("FN");
		waitingForCode = false;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void handleButtonNC(){
		currentUtterance.setCodeVal("NC");
		((MiscTemplateView) playerView.getPanelTemplate()).setTextFieldCode("NC");
		waitingForCode = false;
	}

	//====================================================================
	// TODO: DataQueue Implementation
	//====================================================================

	private void killLastUtterance(){
		this.getDataQueue().removeLastAddedUtterance();
		//currentUtterance = queue.getPreviousUtterance();
		currentUtterance = queue.getCurrentUtterance();
		((ParserTemplateView) templateView).setTextFieldLast(currentUtterance.toString());

		//Start time or End Time
		//int secs = new TimeCode(currentUtterance.getEndTime()).convertTimeCodeStringToSecs();
		int secs = new TimeCode(currentUtterance.getStartTime()).convertTimeCodeStringToSecs();
		try {
			control.seek((long) secs * basicPlayer.getBytesPerSecond());
		} catch (BasicPlayerException e) {
			showAudioFileNotSeekableDialog();
			e.printStackTrace();
		}
	}


	//TODO: 
	//	This could include a counter that reports the number of 
	//		utterances that failed to be coded, or a readout of
	//		utterance order numbers for those that didn't get coded.

	// Set currentUtterance text fields to empty strings when current utterance passes, and
	// nextUncodedUtterance hasn't yet begun.
	// The safest way may be to nullify currentUtterance in this time gap, to prevent
	// the user from accidentally changing the code on an utterance that has ended.
	private void updateMiscDisplay(int currentBytes){
		if( (currentUtterance != null && currentBytes > currentUtterance.getEndBytes()) ||
				(nextUncodedUtterance != null && 
						currentBytes >= nextUncodedUtterance.getStartBytes()) ){

			// pause on uncoded condition
			if( pauseOnUncoded && waitingForCode && 
					(basicPlayer.getStatus() == BasicPlayer.PLAYING) ){
				try {
					control.pause();
				} catch (BasicPlayerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			//update to next utterance to code
			if( ! waitingForCode || ! pauseOnUncoded ){
				waitingForCode = true;
				if( basicPlayer.getStatus() == BasicPlayer.PAUSED ){
					int skipToBytes = currentUtterance.getEndBytes();
					int skipAmt = skipToBytes - (skipToBytes % basicPlayer.getBytesPerSecond());
					try {
						skipAmt = (int) control.seek((long) skipAmt);
						control.resume();
					} catch (BasicPlayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				currentUtterance = nextUncodedUtterance;
				if( nextUncodedUtterance != null ){
					nextUncodedUtterance = getNextUncodedUtterance();
				}
				if( currentUtterance == null ){
					//end condition
					try {
						control.pause();
						allowUpdate = false;
						((MiscTemplateView) templateView).setTextFieldNext("");
						saveCurrentTextFile();				
					} catch (BasicPlayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if( nextUncodedUtterance != null ){
					((MiscTemplateView) templateView).setTextFieldNext(nextUncodedUtterance.toString());
				}
				if( currentUtterance != null ){
					((MiscTemplateView) templateView).setTextFieldOrder(currentUtterance.getEnum());
					((MiscTemplateView) templateView).setTextFieldCode("");
					((MiscTemplateView) templateView).setTextFieldStartTime(currentUtterance.getStartTime());
					((MiscTemplateView) templateView).setTextFieldEndTime(currentUtterance.getEndTime());
				}
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//TODO: re-factor out the set to next utterance functionality
	//		perhaps have a global currentUtterance variable that 
	//		is rotated in the controller.  Is that easier than calling
	//		it out of the queue directly?  Could we just keep the
	//		index value instead?

	private void setToCurrentUtterance(){
		System.out.println("templateView = " + templateView);
		
		//set the utterance display
		if( templateView != null ){
			if( templateView instanceof ParserTemplateView ){
				Utterance current = getDataQueue().getCurrentUtterance();
				//System.out.println("DEBUG: curr enum = " + current.getEnum());
				((ParserTemplateView) templateView).setTextFieldLast(current.toString());
				int secs = new TimeCode(current.getEndTime()).convertTimeCodeStringToSecs();
				try {
					control.seek((long) secs * basicPlayer.getBytesPerSecond());
				} catch (BasicPlayerException e) {
					showAudioFileNotSeekableDialog();
					e.printStackTrace();
				}
			}
			else if( templateView instanceof MiscTemplateView ){
				if( currentUtterance == null ){
					currentUtterance = getDataQueue().getLastCodedUtterance();
				}
				//nextUncodedUtterance = getDataQueue().getUtteranceAt(currentUtterance.getEnum()-1);
				//System.out.println("DEBUG: 1st currentUtterance enum = " + currentUtterance.getEnum());
				currentUtterance = queue.getCurrentUtterance();
				//System.out.println("DEBUG: 2nd currentUtterance enum = " + currentUtterance.getEnum());
				nextUncodedUtterance = getDataQueue().getNextUtteranceNoAdvance();
				//System.out.println("DEBUG: nextUncodedUtterance enum = " + nextUncodedUtterance.getEnum());
				((MiscTemplateView) templateView).setTextFieldNext(getNextUncodedUtterance().toString());
				((MiscTemplateView) templateView).setTextFieldOrder(currentUtterance.getEnum());
				if( currentUtterance.getCodeVal() == -1 ){
					((MiscTemplateView) templateView).setTextFieldCode("");
				}
				else{
					((MiscTemplateView) templateView).setTextFieldCode(
							currentUtterance.getCodeAbbrv(currentUtterance.getCodeVal()));
				}
				((MiscTemplateView) templateView).setTextFieldStartTime(currentUtterance.getStartTime());
				((MiscTemplateView) templateView).setTextFieldEndTime(currentUtterance.getEndTime());

				// Setting this to reset audio at start time rather than end time per request
				int secs = new TimeCode(currentUtterance.getStartTime()).convertTimeCodeStringToSecs();
				try {
					control.seek((long) secs * basicPlayer.getBytesPerSecond());
				} catch (BasicPlayerException e) {
					showAudioFileNotSeekableDialog();
					e.printStackTrace();
				}
			}
			else{
				showQueueNotLoadedDialog();
			}
		}
		else{
			showTemplateNotFoundDialog();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private UtteranceQueue getDataQueue(){
		if( queue == null ){
			//new queue
			queue = new UtteranceQueue();
		}
		return queue;
	}

	//====================================================================
	// TODO: Required to implement BasicPlayerListener
	//====================================================================

	public void opened(Object stream, Map<Object, Object> properties)
	{
		// Pay attention to properties. It's useful to get duration, 
		// bitrate, channels, even tag such as ID3v2.
		display("opened : "+properties.toString());
		//audioInfo = properties;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void setController(BasicController controller)
	{
		display("setController : "+controller);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void progress(int bytesread, long microseconds, byte[] pcmdata, 
			Map<Object, Object> properties)
	{
		// Pay attention to properties. It depends on underlying JavaSound SPI
		// MP3SPI provides mp3.equalizer.
		//display("progress : "+properties.toString());
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void stateUpdated(BasicPlayerEvent event)
	{
		// Notification of BasicPlayer states 
		// (opened, playing, end of media, ...)
		//display("stateUpdated : "+event.toString());
		switch( event.getCode() ){
		case 0:
			strStatus = "OPENING";
			break;
		case 1:
			strStatus = "OPENED";
			break;
		case 2:
			strStatus = "PLAYING";
			break;
		case 3:
			strStatus = "STOPPED";
			break;
		case 4:
			strStatus = "PAUSED";
			break;
		case 5:
			//strStatus = "RESUMED";
			strStatus = "PLAYING";
			break;
		case 6:
			//strStatus = "SEEKING";
			break;
		case 7:
			//strStatus = "SEEKED";
			break;
		case 8:
			//strStatus = "EOM";
			saveCurrentTextFile();
			break;
		case 9:
			//strStatus = "PAN";
			break;
		case 10:
			//strStatus = "GAIN";
			break;
		default:
			strStatus = "UNKNOWN";
		}
		String playerStatus = strStatus.concat(":  " + fileAudio.getName() + 
				"  |  Total Time = " +
				(new TimeCode(basicPlayer.getSecondsPerFile()).
						convertToTimeString()));
		playerView.setLabelPlayerStatus(playerStatus);
	}

	public void display(String msg)
	{
		System.out.println(msg);
	}
}

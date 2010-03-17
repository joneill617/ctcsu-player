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
import edu.unm.casaa.misc.MiscCode;
import edu.unm.casaa.misc.MiscDataItem;
import edu.unm.casaa.misc.MiscTemplateUiService;
import edu.unm.casaa.misc.MiscTemplateView;
import edu.unm.casaa.utterance.ParserTemplateUiService;
import edu.unm.casaa.utterance.ParserTemplateView;
import edu.unm.casaa.utterance.Utterance;
import edu.unm.casaa.utterance.UtteranceList;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

enum Mode {
	MODE_PLAYBACK, 	// Play audio file.
	MODE_PARSE,		// Parse audio file.
	MODE_CODE,		// Code previously parsed audio file.
	MODE_GLOBALS	// TODO - Carl - What is this mode?
};

public class MainController implements BasicPlayerListener, ActionListener {

	//====================================================================
	// Fields
	//====================================================================

	// GUI
	private PlayerView 			playerView 		= null;
	private JPanel 				templateView	= null;
	private TemplateUiService 	templateUI		= null;

	private String 	filenameParse			= null;
	private String 	filenameMisc			= null;
	private String 	filenameGlobals			= null;

	// Audio Player back-end
	private BasicPlayer 	basicPlayer		= null;

	private String 	filenameAudio			= null;

	private Timer timer						= null;
	private static final int TIMER_DELAY 	= 250;
	private static final int INIT_DELAY 	= 1000;

	private String strStatus				= null;

	private UtteranceList utteranceList		= null;
	private	int currentUtterance			= 0; // Index of utterance selected for parse or code.  May be equal to list size, in which case current utterance is null.

	private boolean pauseOnUncoded			= true;


	//====================================================================
	// Main, Constructor and Initialization Methods
	//====================================================================

	public MainController(){
		init();		
	}

	private void init(){
		basicPlayer = new BasicPlayer();
		playerView 	= new PlayerView();
		playerView.getSliderSeek().setEnabled( false ); // Start disabled, since we'll have no audio file loaded yet.

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
		if( isParsingUtterance() ){
			parseEnd();
		}
		saveCurrentTextFile( false );
		System.exit(0);
	}

	private void pokeTimer(){
		// This does not provide the file time to the player.
		if( timer != null ){
			timer.cancel();
		}
		timer = new Timer();
		timer.scheduleAtFixedRate( new TimerTask() {
			public void run() {
				setTimeDisplay();
				setSeekSliderDisplay();
			}
		}, INIT_DELAY, TIMER_DELAY );
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

	public void setPauseOnUncoded( boolean value ) {
		pauseOnUncoded = value;
	}

	//====================================================================
	// ActionListener interface
	//====================================================================

	public void actionPerformed( ActionEvent e ) {
		String command = e.getActionCommand();

		if( "parseStart".equals( command ) ) {
			parseStart();
		} else if( "parseEnd".equals( command ) ) {
			parseEnd();
			saveCurrentTextFile( false );
		}
	}

	//====================================================================
	// Private Helper Methods
	//====================================================================

	private Utterance getCurrentUtterance() {
		assert( currentUtterance >= 0 );
		if( currentUtterance < getUtteranceList().size() ) {
			return getUtteranceList().get( currentUtterance );
		} else {
			return null;
		}
	}

	// Get next utterance, or null if no next utterance exists.
	private Utterance getNextUtterance() {
		if( currentUtterance + 1 < getUtteranceList().size() ) {
			return getUtteranceList().get( currentUtterance + 1 );
		} else {
			return null;
		}
	}

	// Get previous utterance, or null if no previous utterance exists.
	private Utterance getPreviousUtterance() {
		if( currentUtterance > 0 ) {
			return getUtteranceList().get( currentUtterance - 1 );
		} else {
			return null;
		}
	}

	private boolean hasPreviousUtterance() {
		return currentUtterance > 0;
	}

	// PRE: hasPreviousUtterance.
	private void gotoPreviousUtterance() {
		assert( hasPreviousUtterance() );
		currentUtterance--;
	}

	private boolean isParsingUtterance() {
		Utterance current = getCurrentUtterance();
		
		if( current == null )
			return false;
		return !current.isParsed();
	}

	// Seek player to time represented by given timeCode string, or to start if timeCode is empty string.
	private void playerSeek( String timeCode ) {
		int secs = 0;
		
		if( !timeCode.equals( "" ) ) {
			secs = new TimeCode(timeCode).convertTimeCodeStringToSecs();
		}

		try {
			basicPlayer.seek( (long) secs * basicPlayer.getBytesPerSecond() );
		} catch (BasicPlayerException e) {
			showAudioFileNotSeekableDialog();
			e.printStackTrace();
		}
	}

	// Pause/resume/stop/play player.  These wrappers are here to clean up exception handling.
	private void playerPause() {
		try {
			basicPlayer.pause();
		} catch( BasicPlayerException e ) {
			e.printStackTrace();
		}
	}
	private void playerStop() {
		try {
			basicPlayer.stop();
		} catch( BasicPlayerException e ) {
			e.printStackTrace();
		}
	}
	private void playerResume() {
		try {
			basicPlayer.resume();
		} catch( BasicPlayerException e ) {
			e.printStackTrace();
		}
	}
	private void playerPlay() {
		try {
			basicPlayer.play();
		} catch( BasicPlayerException e ) {
			e.printStackTrace();
		}
	}

	// Switch modes.  Hides/shows relevant UI.
	private void setMode( Mode mode ) {
		setTemplateView( mode );
		playerView.getSliderSeek().setEnabled( mode == Mode.MODE_PLAYBACK ); // Enable seek slider during play-back only.
	}

	// Open file chooser to select audio file.  On approve, load audio file.
	// Returns true if audio file was successfully opened.
	private boolean selectAndLoadAudioFile() {
		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select an Audio File to Load" );
		chooser.setFileFilter( new FileNameExtensionFilter( "WAV Audio only for coding", "wav") );
		if( chooser.showOpenDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			return loadAudioFile( chooser.getSelectedFile().getAbsolutePath() );
		} else {
			return false;
		}
	}

	// Load audio file from given filename.  Records filenameAudio.
	// Returns true on success.
	private boolean loadAudioFile( String filename ) {
		filenameAudio	= filename;
		try {
			basicPlayer.open( new File( filenameAudio ) );
			pokeTimer();
			return true;
		} catch( BasicPlayerException e ) {
			showAudioFileNotFoundDialog();
			e.printStackTrace();
			return false;
		}
	}

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
				if( !playerView.getSliderPan().getValueIsAdjusting()){
					handleSliderPan();
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Gain Slider
		playerView.getSliderGain().addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent ce){
				if( !playerView.getSliderGain().getValueIsAdjusting()){
					handleSliderGain();
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Seek Slider
		playerView.getSliderSeek().addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent ce){
				// TODO - Carl - Shouldn't this be !, like the above similar checks?  If it should be different, why?
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
				if( selectAndLoadAudioFile() ) {
					setMode( Mode.MODE_PLAYBACK );
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// File Menu: Exit
		playerView.getMenuItemExit().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// Need to check if utterance list has been saved first.
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
	private void handleButtonPlay() {
		if( basicPlayer.getStatus() == BasicPlayer.PAUSED ) {
			// TODO - Carl - Look here for volume/pan control bug.
			playerResume();
		} else {
			// Set volume and pan according to GUI.
			handleSliderGain();
			handleSliderPan();
			playerPlay();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleButtonStop() {
		if( isParsingUtterance() ) {
			parseEnd();
		}
		playerStop();
		saveCurrentTextFile( false );
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleButtonBackup() {
		if( basicPlayer.getStatus() == BasicPlayer.PLAYING ) {
			playerPause();
		} else if( basicPlayer.getStatus() != BasicPlayer.PAUSED &&
				   basicPlayer.getStatus() != BasicPlayer.OPENED &&
				   basicPlayer.getStatus() != BasicPlayer.STOPPED ) {
			showBackupErrorDialog();
			return;
		}

		if( isParsingUtterance() ) {
			parseEnd();
		}

		saveCurrentTextFile( true );
		showBackupCompleteDialog();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleButtonPause() {
		if( basicPlayer.getStatus() == BasicPlayer.PLAYING ){
			playerPause();
		} else if( basicPlayer.getStatus() == BasicPlayer.PAUSED ) {
			// Set volume and pan according to GUI.
			handleSliderGain();
			handleSliderPan();
			playerResume();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleButtonReplay(){
		if( templateView instanceof ParserTemplateView ){
			removeLastUtterance();
		}
		else if( templateView instanceof MiscTemplateView ){
			// Strip code from current utterance.
			Utterance utterance = getCurrentUtterance();

			if( utterance != null ){
				utterance.setMiscCode(MiscCode.INVALID);
			}

			// Move to previous utterance, if one exists.
			if( hasPreviousUtterance() ) {
				gotoPreviousUtterance();
				utterance = getCurrentUtterance();
			}

			// Set audio to utterance start time.
			if( utterance == null ){
				playerSeek("");
			}
			else {
				playerSeek(utterance.getStartTime());
			}

			updateUtteranceDisplays();
		}
		else{
			showParsingErrorDialog();
		}
		pokeTimer();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleSliderSeek(){
		if( basicPlayer.getStatus() == BasicPlayer.UNKNOWN ) {
			return;
		}
		// TODO - Carl - Check out this code.  Looks error prone.
		double sliderPct = new Integer(playerView.getSliderSeek().getValue()).doubleValue() /
			new Integer(PlayerView.SEEK_MAX_VAL).doubleValue();
		int skipAmt = (int) (sliderPct * basicPlayer.getEncodedLength());
		skipAmt = skipAmt - (skipAmt % basicPlayer.getBytesPerSecond());
		try {
			skipAmt = (int) (basicPlayer.seek((long) skipAmt));
			if( (skipAmt % basicPlayer.getBytesPerSecond()) != 0 ){
				// NO OP - Skip error
			}
			pokeTimer();
		} catch (BasicPlayerException e) {
			e.printStackTrace();
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

		try {
			basicPlayer.setPan( dPanVal );
		} catch( BasicPlayerException e ) {
			e.printStackTrace();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleSliderGain(){
		Long valSliderGain = new Long((long)playerView.
				getSliderGain().getValue());
		double dGainVal = valSliderGain.doubleValue() / 100;

		try {
			basicPlayer.setGain( dGainVal );
		} catch( BasicPlayerException e ) {
			e.printStackTrace();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Menu Handlers
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewParseFile(){
		saveCurrentTextFile( false );

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle("Select Save Directory and Enter a Filename for the PARSE File");
		chooser.setFileFilter(new FileNameExtensionFilter("PARSE files", "parse"));
		chooser.setToolTipText("Use \".parse\" as the file extension.");
		if( chooser.showSaveDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			if( selectAndLoadAudioFile() ) {
				utteranceList 		= null;
				currentUtterance	= 0;
				filenameParse 		= chooser.getSelectedFile().getAbsolutePath();
				filenameParse 		= correctTextFileType(".parse", filenameParse);
				setMode( Mode.MODE_PARSE );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewCodeFile(){
		saveCurrentTextFile( false );

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle("Select Save Directory and Enter a Filename for the CASAA File");
		chooser.setFileFilter(new FileNameExtensionFilter("PARSE files", "parse"));
		if( chooser.showSaveDialog(playerView) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			filenameParse 		= chooser.getSelectedFile().getAbsolutePath();
			filenameMisc 		= correctTextFileType(".casaa", filenameParse);
			try {
				copyParseFileToCodeFile();
			} catch (IOException e) {
				e.printStackTrace();
				showFileNotCreatedDialog();
			}

			// Load the parse and audio files.
			setMode( Mode.MODE_CODE );
			filenameAudio = getUtteranceList().loadFromFile( new File( filenameMisc ) );
			loadAudioFile( filenameAudio );
			postLoad();
		}

		//TODO: Set to add codes to queue data items from beginning of files
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewGlobalRatings(){
		saveCurrentTextFile( false );

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select Save Directory and Enter a Filename" );
		chooser.setFileFilter( new FileNameExtensionFilter( "GLOBALS files", "global" ) );
		chooser.setToolTipText( "Use \".global\" as the file extension." );
		if( chooser.showSaveDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			if( selectAndLoadAudioFile() ) {
				utteranceList 		= null; // TODO - Carl should we clear list here?  What is globals supposed to do?
				currentUtterance	= 0;
				filenameGlobals 	= chooser.getSelectedFile().getAbsolutePath();
				filenameGlobals 	= correctTextFileType( ".global", filenameGlobals );
				setMode( Mode.MODE_GLOBALS );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleLoadParseFile(){
		saveCurrentTextFile( false );

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select a PARSE File to Load" );
		chooser.setFileFilter( new FileNameExtensionFilter( "PARSE files", "parse" ) );
		if( chooser.showOpenDialog(playerView) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			filenameParse 		= chooser.getSelectedFile().getAbsolutePath();
			setMode( Mode.MODE_PARSE );
			// Load the parse file.
			filenameAudio = getUtteranceList().loadFromFile(new File( filenameParse ));
			if( filenameAudio.equalsIgnoreCase("ERROR: No Audio File Listed") ){
				showAudioFileNotFoundDialog();
				return;
			}
			// Load the audio file.
			loadAudioFile( filenameAudio );
			postLoad();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleLoadCodeFile(){
		saveCurrentTextFile( false );

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select a CASAA Code File to Load" );
		chooser.setFileFilter( new FileNameExtensionFilter( "CASAA code files", "casaa" ) );
		if( chooser.showOpenDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			filenameMisc 		= chooser.getSelectedFile().getAbsolutePath();
			setMode( Mode.MODE_CODE );
			filenameAudio = getUtteranceList().loadFromFile(new File(filenameMisc)); // Load the code file.
			loadAudioFile( filenameAudio ); // Load the audio file.
			postLoad();
		}
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
	private void saveCurrentTextFile( boolean asBackup ) {
		if( (utteranceList != null) || (templateView instanceof GlobalTemplateView) ){
			if( templateView instanceof ParserTemplateView && filenameParse != null ) {
				String filename = filenameParse;
				
				if( asBackup ) {
					filename += ".backup";
				}
				getUtteranceList().writeToFile( new File( filename ), filenameAudio );
			}
			else if( templateView instanceof MiscTemplateView && filenameMisc != null ) {
				String filename = filenameMisc;
				
				if( asBackup ) {
					filename += ".backup";
				}
				getUtteranceList().writeToFile( new File( filename ), filenameAudio );
			}
			else if( templateView instanceof GlobalTemplateView ) {
				String filename = filenameGlobals;

				if( asBackup ) {
					filename += ".backup";
				}
				((GlobalTemplateUiService)templateUI).writeGlobalsToFile( new File( filename ) );
			}
			else if( templateView == null ) {
				// No-op.  Reached if only playing audio file, and stop pressed.
			}
			else {
				JOptionPane.showMessageDialog(playerView,
						"There is No Reference to a PARSE or CASAA File to Save.\n" +
						"Please Report This Error to the Developer for Repair.",
						"Error Notification", JOptionPane.ERROR_MESSAGE);
			}
		}
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
				"Backup can only be called while player is playing or paused\n" +
				"while using the parsing or coding features.",
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
		if( basicPlayer.getBytesPerSecond() != 0 ) {
			// Handles constant bit-rates only.
			// Set the player time display.
			int 	startBytes 	= basicPlayer.getEncodedStreamPosition();
			String 	strTimeCode = new TimeCode( (startBytes /
					basicPlayer.getBytesPerSecond())).
					convertToTimeString();

			playerView.setLabelTime( "Time:  " + strTimeCode );
			if( templateView instanceof MiscTemplateView ) {
				updateMiscDisplay( startBytes );
			}
		} else {
			// EXTEND: Get time based on frames rather than bytes.
			// Need a way to determine current position based on frames.
			// Something like getEncodedStreamPosition(),
			// but that returns frames.  This for VBR type compressions.
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void copyParseFileToCodeFile() throws IOException {
		InputStream 	in 		= new FileInputStream( new File( filenameParse ) );
		OutputStream 	out 	= new FileOutputStream( new File( filenameMisc ) );
		byte[] 			buffer 	= new byte[1024];
		int 			length;

		while( (length = in.read( buffer )) > 0 ) {
			out.write( buffer, 0, length );
		}
		in.close();
		out.close();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void nullifyLastTemplateView(){
		templateView 	= null;
		templateUI 		= null;
		System.gc();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void setTemplateView( Mode mode ) {
		nullifyLastTemplateView();
		switch( mode ) {
		case MODE_PARSE:
			templateUI 		= new ParserTemplateUiService(this);
			templateView 	= templateUI.getTemplateView();
			break;
		case MODE_CODE:
			templateUI 		= new MiscTemplateUiService(this);
			templateView 	= templateUI.getTemplateView();
			break;
		case MODE_GLOBALS:
			templateUI 		= new GlobalTemplateUiService();
			templateView 	= templateUI.getTemplateView();
			break;
		default:
			assert false : "Mode unrecognized: " + mode.toString();
			break;
		}
		playerView.setPanelTemplate(templateView);
	}

	//====================================================================
	// Parser Template Handlers

	public void parseStart() {
		if( isParsingUtterance() ) {
			parseEnd();
			saveCurrentTextFile( false );
		}
		// Record start data.
		int 	startPosition	= basicPlayer.getEncodedStreamPosition();
		String 	start 			= new TimeCode( (startPosition / 
				basicPlayer.getBytesPerSecond()) ).convertToTimeString();

		// Create a new utterance.
		int 		order 	= getUtteranceList().size();
		Utterance 	data 	= new MiscDataItem(order, start, startPosition);

		getUtteranceList().add( data );
		currentUtterance	= order; // Select this as current utterance.

		updateUtteranceDisplays();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void parseEnd() {
		// Record end data.
		int 	endPosition = basicPlayer.getEncodedStreamPosition();
		String 	end 		= new TimeCode( (endPosition /
				basicPlayer.getBytesPerSecond()) ).convertToTimeString();

		// Save to utterance.
		Utterance current = getCurrentUtterance();

		if( current == null ) {
			showParsingErrorDialog();
		}
		else {
			current.setEndTime(end);
			current.setEndBytes(endPosition);
		}
		updateUtteranceDisplays();
	}

	//====================================================================
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Globals Template Handlers
	//TODO

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// MISC Template Handlers

	public void handleButtonMiscCode(MiscCode miscCode){
		if( miscCode == MiscCode.INVALID ){
			System.err.println("ERROR: handleButtonMisc received MiscCode.INVALID");
			return;
		}
		Utterance utterance = getCurrentUtterance();

		assert(utterance != null);
		utterance.setMiscCode(miscCode);
		updateUtteranceDisplays();
	}

	private void removeLastUtterance(){
		getUtteranceList().removeLast();

		if( getUtteranceList().size() > 0 &&
				currentUtterance >= getUtteranceList().size() ) {
			currentUtterance = getUtteranceList().size() - 1;
		}

		Utterance utterance = getCurrentUtterance();

		if( utterance == null ) {
			playerSeek( "" );
		}
		else {
			playerSeek( utterance.getStartTime() );
		}

		updateUtteranceDisplays();
	}


	//TODO: 
	//	This could include a counter that reports the number of 
	//		utterances that failed to be coded, or a readout of
	//		utterance order numbers for those that didn't get coded.

	// Set currentUtterance text fields to empty strings when current utterance passes, and
	// nextUncodedUtterance hasn't yet begun.
	// The safest way may be to nullify currentUtterance in this time gap, to prevent
	// the user from accidentally changing the code on an utterance that has ended.
	private void updateMiscDisplay( int currentBytes ) {
		Utterance	current	= getCurrentUtterance();
		Utterance	next	= getNextUtterance();

		// TMP - Carl
		// System.out.println( "updateMiscDisplay, currentBytes: " + currentBytes );
		// TMP

		if( (current != null && currentBytes > current.getEndBytes()) ||
			(next != null && currentBytes >= next.getStartBytes()) ) {
			// Pause on uncoded condition.
			assert( current != null );
			if( pauseOnUncoded && !current.isCoded() && 
					(basicPlayer.getStatus() == BasicPlayer.PLAYING) ) {
				playerPause();
			}

			// Update to next utterance to code.
			if( current.isCoded() || !pauseOnUncoded ) {
				if( basicPlayer.getStatus() == BasicPlayer.PAUSED ) {
					int skipToBytes = current.getEndBytes();
					// TODO - Carl - Check this for potential resume/skip bug.
					// TODO - Carl - Also, why are we snapping to integral second alignment here (and in several other places)?
					// TMP int skipAmt = skipToBytes - (skipToBytes % basicPlayer.getBytesPerSecond());
					try {
						// TMP skipAmt = (int) basicPlayer.seek((long) skipAmt);
						basicPlayer.seek((long) skipToBytes);
						basicPlayer.resume();
					} catch (BasicPlayerException e) {
						e.printStackTrace();
					}
				}
				currentUtterance++;
				// TODO - Carl - Look here for end of file bug.
				if( getCurrentUtterance() == null ) {
					// End condition.
					playerPause();
					saveCurrentTextFile( false );
				}
				updateUtteranceDisplays();
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Update utterance displays (e.g. current, last, etc) in active template view.
	private void updateUtteranceDisplays() {
		if( templateView instanceof ParserTemplateView ) {
			ParserTemplateView 	view 	= (ParserTemplateView) templateView;
			Utterance			current	= getCurrentUtterance();
			Utterance			prev	= getPreviousUtterance();

			if( current == null ) {
				view.setTextFieldOrder(0);
				view.setTextFieldStartTime("");
				view.setTextFieldEndTime("");
			}
			else {
				view.setTextFieldOrder(current.getEnum());
				view.setTextFieldStartTime(current.getStartTime());
				view.setTextFieldEndTime(current.getEndTime());
			}

			if( prev == null ) {
				view.setTextFieldLast("");
			}
			else {
				view.setTextFieldLast(prev.toString());
			}

		} else if( templateView instanceof MiscTemplateView ) {
			MiscTemplateView 	view 	= (MiscTemplateView) templateView;
			Utterance			current	= getCurrentUtterance();
			Utterance 			next 	= getNextUtterance();
			Utterance 			prev	= getPreviousUtterance();

			if( next == null ) {
				view.setTextFieldNext("");
			}
			else {
				view.setTextFieldNext(next.toString());
			}
			if( prev == null ) {
				view.setTextFieldLast("");
			}
			else {
				view.setTextFieldLast(prev.toString());
			}

			if( current == null ) {
				view.setTextFieldOrder(0);
				view.setTextFieldCode("");
				view.setTextFieldStartTime("");
				view.setTextFieldEndTime("");
			}
			else{
				view.setTextFieldOrder(current.getEnum());
				if( current.getMiscCode() == MiscCode.INVALID ) {
					view.setTextFieldCode("");
				}
				else {
					view.setTextFieldCode(current.getMiscCode().getLabel());
				}
				view.setTextFieldStartTime(current.getStartTime());
				view.setTextFieldEndTime(current.getEndTime());
			}
		}
	}

	// Select current utterance index, seek player, and update UI after loading
	// data from file.
	private void postLoad() {
		currentUtterance = 0; // Default to first utterance.
		if( templateView instanceof ParserTemplateView ) {
			// Seek to last existing utterance.
			if( getUtteranceList().size() > 0 ) {
				currentUtterance = getUtteranceList().size() - 1;
			}

			Utterance	current = getCurrentUtterance();

			if( current == null ) {
				playerSeek( "" );
			} else {
				playerSeek( current.getEndTime() ); // Seek to end of last existing utterance, so we're ready to start parsing the next.
			}
		}
		else if( templateView instanceof MiscTemplateView ){
			// Seek to first uncoded utterance.
			currentUtterance = getUtteranceList().getLastCodedUtterance();
			currentUtterance++;

			if( getUtteranceList().size() > 0 &&
					currentUtterance >= getUtteranceList().size() ) {
				currentUtterance = getUtteranceList().size() - 1;
			}

			Utterance	current = getCurrentUtterance();

			if( current == null ) {
				playerSeek( "" );
			} else {
				playerSeek( current.getStartTime() ); // Seek to start time.
			}
		}
		else if( templateView != null ) {
			showQueueNotLoadedDialog(); // TODO - Carl - These cases seem backwards (see below).
		}
		else {
			showTemplateNotFoundDialog(); // TODO - Carl - These cases seem backwards (see above).
		}

		updateUtteranceDisplays();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private UtteranceList getUtteranceList(){
		if( utteranceList == null ){
			utteranceList = new UtteranceList();
		}
		return utteranceList;
	}

	//====================================================================
	// Required to implement BasicPlayerListener
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
			saveCurrentTextFile( false );
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
		File	file			= new File( filenameAudio );
		String 	playerStatus 	= strStatus.concat(":  " + file.getName() + 
				"  |  Total Time = " +
				(new TimeCode(basicPlayer.getSecondsPerFile()).
						convertToTimeString()));
		playerView.setLabelPlayerStatus( playerStatus );
	}

	public void display(String msg)
	{
		System.out.println(msg);
	}
}

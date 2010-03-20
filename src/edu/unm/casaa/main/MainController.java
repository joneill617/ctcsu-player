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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

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

/*
 Design notes:
 - Save to file whenever we modify data (i.e. utterances), unless that modification leaves
   data in an incomplete state (e.g. parse started, but not yet ended).
 */

/*
TODO - Carl
- Determine desired behavior for replay button.
- Do we want the same auto-save behavior for global ratings?  If not, how do we trigger save?
  - Overload backup button.
  - On exit.
  - Add new menu item (Global Ratings->Save) and/or button (Save, visible only in GLOBALS mode).
 */

public class MainController implements BasicPlayerListener, ActionListener {

	//====================================================================
	// Fields
	//====================================================================

	enum Mode {
		PLAYBACK, 	// Play audio file.
		PARSE,		// Parse audio file.
		CODE,		// Code previously parsed audio file.
		GLOBALS		// Assign overall ratings to an audio file.
	};

	// GUI
	private PlayerView 			playerView 			= new PlayerView();
	private JPanel 				templateView		= null;
	private TemplateUiService 	templateUI			= null;

	private String 				filenameParse		= null;
	private String 				filenameMisc		= null;
	private String 				filenameGlobals		= null;

	// Audio Player back-end
	private BasicPlayer 		basicPlayer			= new BasicPlayer();
	private String 				playerStatus 		= null;

	private String 				filenameAudio		= null;

	private UtteranceList 		utteranceList		= null;
	private	int 				currentUtterance	= 0; // Index of utterance selected for parse or code.  May be equal to list size, in which case current utterance is null.

	private boolean 			pauseOnUncoded		= true; // If true, pause playback in CODE mode when we reach the end of an uncoded utterance.
	private int					numSaves			= 0; // Number of times we've saved since loading current session data.

	//====================================================================
	// Main, Constructor and Initialization Methods
	//====================================================================

	public MainController() {
		basicPlayer.addBasicPlayerListener( this );
		registerPlayerViewListeners();
		// Handle window closing events.
		playerView.getFrameParentWindow().addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e ) {
				actionExit();
			}
		});

		// Start in playback mode, but with no audio file loaded.
		setMode( Mode.PLAYBACK );
	}

	private void actionExit() {
		cleanupSession();
		System.exit( 0 );
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * @param args
	 */
	public static void main( String[] args ) {
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
		} else if( "play".equals( command ) ) {
			handleActionPlay();
		} else if( "pause".equals( command ) ) {
			handleActionPause();
		} else if( "stop".equals( command ) ) {
			handleActionStop();
		} else if( "replay".equals( command ) ) {
			handleActionReplay();
		} else if( "backup".equals( command ) ) {
			handleActionBackup();
		}
	}

	//====================================================================
	// Private Helper Methods
	//====================================================================

	public void display( String msg ) {
		System.out.println( msg );
	}

	private void displayPlayerException( BasicPlayerException e ) {
		display( "BasicPlayerException: " + e.getMessage() );
		e.printStackTrace();
	}

	private Utterance getCurrentUtterance() {
		assert( currentUtterance >= 0 );
		if( currentUtterance < getUtteranceList().size() ) {
			return getUtteranceList().get( currentUtterance );
		} else {
			return null;
		}
	}

	// Get final utterance in list, or null if list is empty.
	private Utterance getLastUtterance() {
		if( getUtteranceList().size() > 0 ) {
			return getUtteranceList().get( getUtteranceList().size() - 1 );
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

	// Seek player as close as possible to requested bytes.  Updates slider and time display.
	private void playerSeek( int bytes ) {
		try {
			basicPlayer.seek( bytes );
		} catch( BasicPlayerException e ) {
			showAudioFileNotSeekableDialog();
			displayPlayerException( e );
		}

		// Set player volume and pan according to sliders, after player line is initialized.
		handleSliderGain();
		handleSliderPan();

		// Update time and seek slider displays.
		updateTimeDisplay();
		updateSeekSliderDisplay();
	}

	// Seek player to position defined by slider.  Updates time display, but not slider
	// (as that would create a feedback cycle).
	private void playerSeekToSlider() {
		if( basicPlayer.getStatus() == BasicPlayer.UNKNOWN ) {
			return;
		}
		double 	t 		= playerView.getSliderSeek().getValue() / (double) PlayerView.SEEK_MAX_VAL;
		long	bytes	= (long) (t * basicPlayer.getEncodedLength());

		try {
			basicPlayer.seek( bytes );
		} catch( BasicPlayerException e ) {
			displayPlayerException( e );
		}

		// Set player volume and pan according to sliders, after player line is initialized.
		handleSliderGain();
		handleSliderPan();

		// Update time display.
		updateTimeDisplay();
	}

	// Pause/resume/stop/play player.  These wrappers are here to clean up exception handling.
	private void playerPause() {
		try {
			basicPlayer.pause();
		} catch( BasicPlayerException e ) {
			displayPlayerException( e );
		}
	}
	private void playerStop() {
		try {
			basicPlayer.stop();
		} catch( BasicPlayerException e ) {
			displayPlayerException( e );
		}
		updateTimeDisplay();
		updateSeekSliderDisplay();
	}
	private void playerResume() {
		try {
			basicPlayer.resume();
		} catch( BasicPlayerException e ) {
			displayPlayerException( e );
		}
		// Set player volume and pan according to sliders, after player line is initialized.
		handleSliderGain();
		handleSliderPan();
	}
	private void playerPlay() {
		try {
			basicPlayer.play();
		} catch( BasicPlayerException e ) {
			displayPlayerException( e );
		}
		// Set player volume and pan according to sliders, after player line is initialized.
		handleSliderGain();
		handleSliderPan();
	}

	// Switch modes.  Hides/shows relevant UI.
	// PRE: filenameAudio is set.
	private void setMode( Mode mode ) {
		setTemplateView( mode );

		// Disable seek slider and stop during parsing/coding, since manipulating the
		// audio position would allow play-back to get out of sync with parsing/coding.
		boolean allowSeek = (mode == Mode.PLAYBACK || mode == Mode.GLOBALS);

		playerView.getSliderSeek().setEnabled( allowSeek && filenameAudio != null );
		playerView.getButtonStop().setEnabled( allowSeek && filenameAudio != null );

		// NOTE - Carl - Enable/disable feature for these buttons (play, pause, replay, stop, backup)
		// has not been requested yet.
		playerView.getButtonPlay().setEnabled( filenameAudio != null );
		playerView.getButtonPause().setEnabled( filenameAudio != null );
		playerView.getButtonReplay().setEnabled( mode == Mode.PARSE || mode == Mode.CODE );
		playerView.getButtonBackup().setEnabled( mode == Mode.PARSE || mode == Mode.CODE || mode == Mode.GLOBALS );
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
			updateTimeDisplay();
			updateSeekSliderDisplay();
			return true;
		} catch( BasicPlayerException e ) {
			showAudioFileNotFoundDialog();
			displayPlayerException( e );
			return false;
		}
	}

	private void registerPlayerViewListeners() {
		// Player GUI listeners	

		playerView.getButtonPlay().addActionListener( this );
		playerView.getButtonStop().addActionListener( this );
		playerView.getButtonPause().addActionListener( this );
		playerView.getButtonReplay().addActionListener( this );
		playerView.getButtonBackup().addActionListener( this );

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Pan Slider
		playerView.getSliderPan().addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent ce ) {
				if( playerView.getSliderPan().getValueIsAdjusting() ) {
					handleSliderPan();
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Gain Slider
		playerView.getSliderGain().addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent ce ) {
				if( playerView.getSliderGain().getValueIsAdjusting() ) {
					handleSliderGain();
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//Seek Slider
		playerView.getSliderSeek().addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent ce ){
				// If value is being adjusted by user, apply to player.
				// Else slider has changed due to call-back from player.
				if( playerView.getSliderSeek().getValueIsAdjusting() ) {
					playerSeekToSlider();
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
					setMode( Mode.PLAYBACK );
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// File Menu: Exit
		playerView.getMenuItemExit().addActionListener(new ActionListener(){
			public void actionPerformed( ActionEvent e ) {
				actionExit();
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
	private void handleActionPlay() {
		if( basicPlayer.getStatus() == BasicPlayer.PAUSED ) {
			playerResume();
		} else {
			playerPlay();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleActionStop() {
		playerStop();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleActionBackup() {
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
			saveCurrentTextFile( false );
		}

		saveCurrentTextFile( true );
		showBackupCompleteDialog();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleActionPause() {
		if( basicPlayer.getStatus() == BasicPlayer.PLAYING ) {
			playerPause();
		} else if( basicPlayer.getStatus() == BasicPlayer.PAUSED ) {
			playerResume();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleActionReplay(){
		if( templateView instanceof ParserTemplateView ) {
			removeLastUtterance();
		} else if( templateView instanceof MiscTemplateView ) {
			Utterance utterance = getCurrentUtterance();

			// While coding, we should never be on an utterance with a code.
			if( utterance != null ) {
				assert( !utterance.isCoded() );
			}

			// Move to previous utterance, if one exists.
			if( hasPreviousUtterance() ) {
				gotoPreviousUtterance();
				utterance = getCurrentUtterance();
			}

			if( utterance == null ) {
				playerSeek( 0 );
			} else {
				utterance.setMiscCode( MiscCode.INVALID );
				playerSeek( utterance.getStartBytes() );
			}

			updateUtteranceDisplays();
		} else {
			showParsingErrorDialog();
		}
		saveSession();
		updateTimeDisplay();
		updateSeekSliderDisplay();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void updateSeekSliderDisplay() {
		// Don't set slider position if user is dragging it.
		if( playerView.getSliderSeek().getValueIsAdjusting() ) {
			return;
		}

		Integer position 	= new Integer( basicPlayer.getEncodedStreamPosition() );
		Integer length 		= new Integer( basicPlayer.getEncodedLength() );
		double 	t			= position.doubleValue() / length.doubleValue();

		if( t >= 1.0 ){
			playerView.setSliderSeek( PlayerView.SEEK_MAX_VAL );
		} else if( t == 0 ) {
			playerView.setSliderSeek( 0 );
		} else {
			playerView.setSliderSeek( new Double( t * PlayerView.SEEK_MAX_VAL ).intValue() );
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleSliderPan() {
		if( basicPlayer.hasPanControl() ) {
			try {
				basicPlayer.setPan( playerView.getSliderPan().getValue() / 10.0 );
			} catch( BasicPlayerException e ) {
				displayPlayerException( e );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleSliderGain() {
		if( basicPlayer.hasGainControl() ) {
			try {
				basicPlayer.setGain( playerView.getSliderGain().getValue() / 100.0 );
			} catch( BasicPlayerException e ) {
				displayPlayerException( e );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Menu Handlers
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewParseFile() {
		cleanupSession();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select Save Directory and Enter a Filename for the PARSE File" );
		chooser.setFileFilter( new FileNameExtensionFilter( "PARSE files", "parse" ) );
		chooser.setToolTipText( "Use \".parse\" as the file extension." );
		if( chooser.showSaveDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			if( selectAndLoadAudioFile() ) {
				utteranceList 		= null;
				currentUtterance	= 0;
				filenameParse 		= chooser.getSelectedFile().getAbsolutePath();
				filenameParse 		= correctTextFileType( ".parse", filenameParse );
				setMode( Mode.PARSE );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewCodeFile() {
		cleanupSession();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select a PARSE File to Start Coding" );
		chooser.setFileFilter( new FileNameExtensionFilter( "PARSE files", "parse" ) );
		if( chooser.showSaveDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			filenameParse 		= chooser.getSelectedFile().getAbsolutePath();
			filenameMisc 		= correctTextFileType( ".casaa", filenameParse );
			try {
				copyParseFileToCodeFile();
			} catch( IOException e ) {
				e.printStackTrace();
				showFileNotCreatedDialog();
			}

			// Load the parse and audio files.
			filenameAudio = getUtteranceList().loadFromFile( new File( filenameMisc ) );
			loadAudioFile( filenameAudio );
			setMode( Mode.CODE );
			postLoad();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleNewGlobalRatings(){
		cleanupSession();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select Save Directory and Enter a Filename" );
		chooser.setFileFilter( new FileNameExtensionFilter( "GLOBALS files", "global" ) );
		chooser.setToolTipText( "Use \".global\" as the file extension." );
		if( chooser.showSaveDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			if( selectAndLoadAudioFile() ) {
				utteranceList 		= null;
				currentUtterance	= 0;
				filenameGlobals 	= chooser.getSelectedFile().getAbsolutePath();
				filenameGlobals 	= correctTextFileType( ".global", filenameGlobals );
				setMode( Mode.GLOBALS );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleLoadParseFile(){
		cleanupSession();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select a PARSE File to Load" );
		chooser.setFileFilter( new FileNameExtensionFilter( "PARSE files", "parse" ) );
		if( chooser.showOpenDialog(playerView) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			filenameParse 		= chooser.getSelectedFile().getAbsolutePath();
			// Load the parse file.
			filenameAudio = getUtteranceList().loadFromFile(new File( filenameParse ));
			if( filenameAudio.equalsIgnoreCase("ERROR: No Audio File Listed") ){
				showAudioFileNotFoundDialog();
				return;
			}
			// Load the audio file.
			loadAudioFile( filenameAudio );
			setMode( Mode.PARSE );
			postLoad();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleLoadCodeFile(){
		cleanupSession();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select a CASAA Code File to Load" );
		chooser.setFileFilter( new FileNameExtensionFilter( "CASAA code files", "casaa" ) );
		if( chooser.showOpenDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			filenameMisc 		= chooser.getSelectedFile().getAbsolutePath();
			filenameAudio = getUtteranceList().loadFromFile(new File(filenameMisc)); // Load the code file.
			loadAudioFile( filenameAudio ); // Load the audio file.
			setMode( Mode.CODE );
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

	// Cleanup current session (i.e. parsing, coding, etc).  Saves if necessary.
	private void cleanupSession() {
		if( isParsingUtterance() ) {
			parseEnd();
		} else if( templateView instanceof GlobalTemplateView ) {
			saveSession(); // Assume we need to save.  TODO - Carl - Save when data changes.
		}
	}

	// Save current session.  Periodically also save backup copy.
	private void saveSession() {
		// Save normal file.
		saveCurrentTextFile( false );

		// Save backup every n'th normal save.
		if( numSaves % 10 == 0 ) {
			saveCurrentTextFile( true );
		}
		numSaves++;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void saveCurrentTextFile( boolean asBackup ) {
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
			((GlobalTemplateUiService)templateUI).writeGlobalsToFile( new File( filename ), filenameAudio );
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void handleAboutWindow() {
		AboutWindowView aboutWindow = new AboutWindowView();

		aboutWindow.setFocusable( true );
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
	private void updateTimeDisplay() {
		if( basicPlayer.getBytesPerSecond() != 0 ) {
			// Handles constant bit-rates only.
			// Set the player time display.
			int 	startBytes 	= basicPlayer.getEncodedStreamPosition();
			int		seconds		= startBytes / basicPlayer.getBytesPerSecond();

			playerView.setLabelTime( "Time:  " + TimeCode.toString( seconds ) );
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
	private void setTemplateView( Mode mode ) {
		templateView 	= null;
		templateUI 		= null;
		System.gc();

		switch( mode ) {
		case PARSE:
			templateUI 		= new ParserTemplateUiService( this );
			templateView 	= templateUI.getTemplateView();
			break;
		case CODE:
			templateUI 		= new MiscTemplateUiService( this );
			templateView 	= templateUI.getTemplateView();
			break;
		case GLOBALS:
			templateUI 		= new GlobalTemplateUiService();
			templateView 	= templateUI.getTemplateView();
			break;
		default:
			assert false : "Mode unrecognized: " + mode.toString();
			break;
		}
		playerView.setPanelTemplate( templateView );
	}

	//====================================================================
	// Parser Template Handlers

	public void parseStart() {
		if( isParsingUtterance() ) {
			parseEnd();
		}
		// Record start data.
		int 	startPosition	= basicPlayer.getEncodedStreamPosition();
		String 	startString		= TimeCode.toString( startPosition / basicPlayer.getBytesPerSecond() );

		// Create a new utterance.
		int 		order 	= getUtteranceList().size();
		Utterance 	data 	= new MiscDataItem( order, startString, startPosition );

		getUtteranceList().add( data );
		currentUtterance	= order; // Select this as current utterance.

		updateUtteranceDisplays();
	}

	// End parse, reading bytes position from basicPlayer.
	public void parseEnd() {
		parseEnd( basicPlayer.getEncodedStreamPosition() );
	}

	// End parse at given byte position.
	public void parseEnd( int endBytes ) {
		assert( endBytes >= 0 );

		// Record end data to current utterance.
		Utterance 	current 	= getCurrentUtterance();

		if( current == null ) {
			showParsingErrorDialog();
		} else {
			String	endString	= TimeCode.toString( endBytes / basicPlayer.getBytesPerSecond() );

			current.setEndTime( endString );
			current.setEndBytes( endBytes );
			saveSession();
		}
		updateUtteranceDisplays();
	}

	//====================================================================
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Globals Template Handlers
	//TODO

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// MISC Template Handlers

	public void handleButtonMiscCode( MiscCode miscCode ) {
		if( miscCode == MiscCode.INVALID ) {
			display( "ERROR: handleButtonMisc received MiscCode.INVALID" );
			return;
		}

		// Assign code to current utterance.
		Utterance utterance = getCurrentUtterance();

		if( utterance == null ) {
			return; // No current utterance.
		}

		utterance.setMiscCode( miscCode );
		saveSession();

		// Go to next utterance (which may be null).
		currentUtterance++;
		utterance = getCurrentUtterance();
		if( utterance == null ) {
			playerSeek( getLastUtterance().getEndBytes() ); // We'll always have a last utterance at this point.
		} else {
			playerSeek( utterance.getStartBytes() );
		}
		updateUtteranceDisplays();
	}

	private void removeLastUtterance(){
		getUtteranceList().removeLast();

		if( getUtteranceList().size() > 0 ) {
			currentUtterance = getUtteranceList().size() - 1;
		} else {
			currentUtterance = 0;
		}

		if( getCurrentUtterance() == null ) {
			playerSeek( 0 );
		} else {
			playerSeek( getCurrentUtterance().getEndBytes() ); // Seek to end of current parsed utterance, so we're ready to start next (i.e. to replace the one we just removed).
		}

		updateUtteranceDisplays();
	}


	// TODO: Visual indication when in between utterances (i.e. past end of current utterance,
	// but not yet at start of next utterance).  We want to prevent the user from accidentally
	// changing the code on an utterance that has ended.
	private void updateMiscDisplay( int currentBytes ) {
		Utterance	current	= getCurrentUtterance();
		Utterance	next	= getNextUtterance();

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
					playerSeek( current.getEndBytes() );
					playerResume();
				}
				currentUtterance++;
				if( getCurrentUtterance() == null ) {
					playerPause(); // End of parsed utterances.
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
				view.setTextFieldOrder("");
				view.setTextFieldStartTime("");
				view.setTextFieldEndTime("");
			} else {
				view.setTextFieldOrder("" + current.getEnum());
				view.setTextFieldStartTime(current.getStartTime());
				view.setTextFieldEndTime(current.getEndTime());
			}

			if( prev == null ) {
				view.setTextFieldLast("");
			} else {
				view.setTextFieldLast(prev.toString());
			}

		} else if( templateView instanceof MiscTemplateView ) {
			MiscTemplateView 	view 	= (MiscTemplateView) templateView;
			Utterance			current	= getCurrentUtterance();
			Utterance 			next 	= getNextUtterance();
			Utterance 			prev	= getPreviousUtterance();

			if( next == null ) {
				view.setTextFieldNext("");
			} else {
				view.setTextFieldNext(next.toString());
			}
			if( prev == null ) {
				view.setTextFieldLast("");
			} else {
				view.setTextFieldLast(prev.toString());
			}

			if( current == null ) {
				view.setTextFieldOrder("");
				view.setTextFieldCode("");
				view.setTextFieldStartTime("");
				view.setTextFieldEndTime("");
			} else{
				view.setTextFieldOrder("" + current.getEnum());
				if( current.getMiscCode() == MiscCode.INVALID ) {
					view.setTextFieldCode("");
				} else {
					view.setTextFieldCode(current.getMiscCode().label);
				}
				view.setTextFieldStartTime(current.getStartTime());
				view.setTextFieldEndTime(current.getEndTime());
			}
		}
	}

	// Select current utterance index, seek player, and update UI after loading
	// data from file.
	// PRE: Mode is set, so appropriate templateView is active.
	private void postLoad() {
		currentUtterance = 0; // Default to first utterance.
		if( templateView instanceof ParserTemplateView ) {
			// Seek to last existing utterance.
			if( getUtteranceList().size() > 0 ) {
				currentUtterance = getUtteranceList().size() - 1;
			}

			if( getCurrentUtterance() == null ) {
				playerSeek( 0 );
			} else {
				playerSeek( getCurrentUtterance().getEndBytes() ); // Seek to end of last existing utterance, so we're ready to start parsing the next.
			}
		}
		else if( templateView instanceof MiscTemplateView ){
			// Seek to first uncoded utterance.  May be one past last utterance in list.
			currentUtterance = getUtteranceList().getLastCodedUtterance();
			currentUtterance++;

			if( getCurrentUtterance() == null ) {
				if( getUtteranceList().isEmpty() ) {
					playerSeek( 0 );
				} else {
					playerSeek( getLastUtterance().getEndBytes() );
				}
			} else {
				playerSeek( getCurrentUtterance().getStartBytes() );
			}
		}
		else if( templateView == null ) {
			showTemplateNotFoundDialog();
		} else {
			showQueueNotLoadedDialog();
		}

		updateUtteranceDisplays();
		numSaves = 0; // Reset save counter, so we backup on next save (i.e. as soon as player saves changes to newly loaded data).  Just to be nice.
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private UtteranceList getUtteranceList(){
		if( utteranceList == null ){
			utteranceList = new UtteranceList();
		}
		return utteranceList;
	}

	//====================================================================
	// BasicPlayerListener interface
	//====================================================================

	public void opened( Object stream, Map< Object, Object > properties ) {}

	public void setController(BasicController controller) {}

	public void progress( int bytesread, long microseconds, byte[] pcmdata, 
			Map< Object, Object > properties)
	{
		updateTimeDisplay();
		updateSeekSliderDisplay();
	}

	public void stateUpdated( BasicPlayerEvent event ) {
		// Notification of BasicPlayer states (opened, playing, end of media, ...).
		// Modify stored playerStatus string only on "significant" changes (e.g. "Opened", but not "Seeked").
		switch( event.getCode() ) {
		case 0:
			playerStatus = "OPENING";
			break;
		case 1:
			playerStatus = "OPENED";
			break;
		case 2:
			playerStatus = "PLAYING";
			break;
		case 3:
			playerStatus = "STOPPED";
			break;
		case 4:
			playerStatus = "PAUSED";
			break;
		case 5:
			// RESUMED
			playerStatus = "PLAYING";
			break;
		case 6:
			// SEEKING
			break;
		case 7:
			// SEEKED
			break;
		case 8:
			// EOM: End of media (i.e. player reached end of audio file).
			if( isParsingUtterance() ) {
				// Specify end bytes manually from event, as basicPlayer will now report -1 for
				// encoded stream position.
				parseEnd( event.getPosition() );
			}
			break;
		case 9:
			// PAN
			break;
		case 10:
			// GAIN
			break;
		default:
			playerStatus = "UNKNOWN";
		}

		File	file	= new File( filenameAudio );
		String	str		= playerStatus.concat( ":  " + file.getName() + "  |  Total Time = " +
				TimeCode.toString( basicPlayer.getSecondsPerFile() ) );

		playerView.setLabelPlayerStatus( str );
	}
}

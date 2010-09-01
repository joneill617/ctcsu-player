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

import java.awt.Color;
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
 * TODO:
 * Optimize:
 *  - Avoid double-repainting timeline, which currently happens when both
 *   updateUtteranceDisplays and updateTimeDisplay are called.
 * Interface:
 *  - We should label the current mode (parse, code, globals).
 *  - We should label the file (e.g. foo.parse, foo.casaa) we're editing.
 */

/*
 Design notes:
 - Save to file whenever we modify data (e.g. utterances), unless that modification leaves
   data in an incomplete state (e.g. parse started, but not yet ended).
 */

public class MainController implements BasicPlayerListener {

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

	private ActionTable			actionTable				= null; // Communication between GUI and MainController.

	private PlayerView 			playerView 				= null;
	private JPanel 				templateView			= null;
	private TemplateUiService 	templateUI				= null;

	private String 				filenameParse			= null;
	private String 				filenameMisc			= null;
	private String 				filenameGlobals			= null;

	// Audio Player back-end
	private BasicPlayer 		player					= new BasicPlayer();
	private String 				playerStatus 			= "";
	private int					bytesPerSecond			= 0; // Cached when we load audio file.

	private String 				filenameAudio			= null;

	private UtteranceList 		utteranceList			= null;
	private	int 				currentUtterance		= 0; // Index of utterance selected for parse or code.  May be equal to list size, in which case current utterance is null.

	private boolean 			pauseOnUncoded			= true; // If true, pause playback in CODE mode when we reach the end of an uncoded utterance.
	private boolean				waitingForCode			= false; // If true, we've paused playback in CODE mode, waiting for user to enter code.
	private int					numSaves				= 0; // Number of times we've saved since loading current session data.

	// The following variables are used for thread-safe handling of player callbacks.
	private boolean				progressReported		= false; // If true, player thread has called progress(), and we will apply change in run().
	private boolean				endOfMediaReported		= false; // If true, player thread has reported EOM, and we will apply change in run().
	private int					endOfMediaPosition		= 0; // Position reported in EOM notification, when endOfMediaReported is true.

	private int 				statusChangeEventIndex 	= 0; // Track highest player event index that changed our displayed status, since events can be reported out of order.

	//====================================================================
	// Main
	//====================================================================

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		MainController mc = new MainController();

		mc.run();
	}

	//====================================================================
	// Constructor and Initialization Methods
	//====================================================================

	public MainController() {
		playerView = new PlayerView( this );
		player.addBasicPlayerListener( this );
		registerPlayerViewListeners();
		// Handle window closing events.
		playerView.getFrameParentWindow().addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e ) {
				actionExit();
			}
		});

		// Start in playback mode, with no audio file loaded.
		setMode( Mode.PLAYBACK );
	}

	//====================================================================
	// MainController interface
	//====================================================================

	// Run update loop.
	public void run() {
		while( true ) {
			try {
				// Thread-safety: Check for and handle player progress and EOM notifications.
				// Because these callbacks come from a separate thread (i.e. the player thread),
				// and handling them may call player methods and/or modify our state, we need to
				// apply them here from our main thread.  We also need to make sure any of our methods
				// which are called from the GUI thread, and which can manipulate our internal state
				// (i.e. most of our methods) are synchronized to ensure thread-safety with the methods
				// we call here.
				if( progressReported ) {
					applyPlayerProgress();
				}
				if( endOfMediaReported ) {
					applyEOM();
				}
				Thread.sleep( 100 );
			} catch( InterruptedException e ) {
				e.printStackTrace();
			}
		}
	}

	// Callbacks for GUI actions.
	public void handleAction( String action ) {
		if( "parseStart".equals( action ) ) {
			parseStart();
		} else if( "parseEnd".equals( action ) ) {
			parseEnd();
		} else if( "play".equals( action ) ) {
			handleActionPlay();
		} else if( "replay".equals( action ) ) {
			handleActionReplay();
		} else if( "rewind5s".equals( action ) ) {
			handleActionRewind5s();
		}
	}

	// Callback when global data changes.
	public void globalDataChanged() {
		assert( templateView instanceof GlobalTemplateView );
		saveSession();
	}

	public synchronized void setPauseOnUncoded( boolean value ) {
		pauseOnUncoded 	= value;
		waitingForCode	= false;
	}

	public ActionTable getActionTable() {
		if( actionTable == null ) {
			actionTable = new ActionTable();
			mapAction( "Start", "parseStart" );
			mapAction( "End", "parseEnd" );
			mapAction( "Play/Pause", "play" );
			mapAction( "Replay", "replay" );
			mapAction( "Rewind 5s", "rewind5s" );
		}
		return actionTable;
	}

	// Access utterances.
	public int numUtterances() {
		return getUtteranceList().size();
	}
	public Utterance utterance( int index ) {
		return getUtteranceList().get( index );
	}
	private void utteranceListChanged() {
		playerView.getTimeline().repaint();
	}

	// Get audio bytes per second.
	public int getBytesPerSecond() {
		return bytesPerSecond;
	}

	// Get audio file length, in bytes.
	public int getAudioLength() {
		return player.getEncodedLength();
	}

	// Get current utterance.  May be null.
	public synchronized Utterance getCurrentUtterance() {
		assert( currentUtterance >= 0 );
		if( currentUtterance < getUtteranceList().size() ) {
			return getUtteranceList().get( currentUtterance );
		} else {
			return null;
		}
	}

	//====================================================================
	// Private Helper Methods
	//====================================================================

	private void actionExit() {
		saveIfNeeded();
		System.exit( 0 );
	}

	private void mapAction( String text, String command ) {		
		actionTable.put( command, new MainControllerAction( this, text, command ) );
	}

	private void display( String msg ) {
		System.out.println( msg );
	}

	private void displayPlayerException( BasicPlayerException e ) {
		display( "BasicPlayerException: " + e.getMessage() );
		e.printStackTrace();
	}

	// Get final utterance in list, or null if list is empty.
	private synchronized Utterance getLastUtterance() {
		if( getUtteranceList().size() > 0 ) {
			return getUtteranceList().get( getUtteranceList().size() - 1 );
		} else {
			return null;
		}
	}

	// Get next utterance, or null if no next utterance exists.
	private synchronized Utterance getNextUtterance() {
		if( currentUtterance + 1 < getUtteranceList().size() ) {
			return getUtteranceList().get( currentUtterance + 1 );
		} else {
			return null;
		}
	}

	// Get previous utterance, or null if no previous utterance exists.
	private synchronized Utterance getPreviousUtterance() {
		if( currentUtterance > 0 ) {
			return getUtteranceList().get( currentUtterance - 1 );
		} else {
			return null;
		}
	}

	private synchronized boolean hasPreviousUtterance() {
		return currentUtterance > 0;
	}

	// PRE: hasPreviousUtterance.
	private synchronized void gotoPreviousUtterance() {
		assert( hasPreviousUtterance() );
		currentUtterance--;
	}

	private synchronized boolean isParsingUtterance() {
		Utterance current = getCurrentUtterance();

		if( current == null )
			return false;
		return !current.isParsed();
	}

	// Seek player as close as possible to requested bytes.  Updates slider and time display.
	private synchronized void playerSeek( int bytes ) {
		try {
			player.seek( bytes );
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
	private synchronized void playerSeekToSlider() {
		if( player.getStatus() == BasicPlayer.UNKNOWN ) {
			return;
		}
		double 	t 		= playerView.getSliderSeek().getValue() / (double) PlayerView.SEEK_MAX_VAL;
		long	bytes	= (long) (t * player.getEncodedLength());

		try {
			// Stop before seeking, to minimize lag.
			player.stop();
			player.seek( bytes );
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
	private synchronized void playerPause() {
		try {
			player.pause();
		} catch( BasicPlayerException e ) {
			displayPlayerException( e );
		}
	}

	private synchronized void playerResume() {
		try {
			player.resume();
		} catch( BasicPlayerException e ) {
			displayPlayerException( e );
		}
		handleSliderGain();
		handleSliderPan();
	}

	private synchronized void playerPlay() {
		try {
			player.play();
		} catch( BasicPlayerException e ) {
			displayPlayerException( e );
		}
		handleSliderGain();
		handleSliderPan();
	}

	// Switch modes.  Hides/shows relevant UI.
	// PRE: filenameAudio is set.
	private void setMode( Mode mode ) {
		setTemplateView( mode );

		playerView.getSliderSeek().setEnabled( filenameAudio != null );
		playerView.getButtonPlay().setEnabled( filenameAudio != null );
		playerView.getButtonReplay().setEnabled( mode == Mode.PARSE || mode == Mode.CODE );
		playerView.getButtonRewind5s().setEnabled( mode == Mode.PARSE || mode == Mode.CODE );
		playerView.getTimeline().setVisible( mode == Mode.PARSE || mode == Mode.CODE );

		// If entering GLOBALS mode, ping callback so we'll save the file.
		if( mode == Mode.GLOBALS ) {
			globalDataChanged();
		}
	}

	// Synchronize both the GUI (slider, time display) and current utterance index with the
	// most recently reported audio playback position, if any.
	private synchronized void applyPlayerProgress() {
		updateTimeDisplay();
		updateSeekSliderDisplay();

		// Handle MISC utterance and player state.
		if( templateView instanceof MiscTemplateView ) {
			Utterance	current	= getCurrentUtterance();
			Utterance	next	= getNextUtterance();
			int 		bytes 	= player.getEncodedStreamPosition();
	
			if( (current != null && bytes > current.getEndBytes()) ||
				(next != null && bytes >= next.getStartBytes()) ) {
				// Pause on uncoded condition.
				assert( current != null );
				if( pauseOnUncoded && !current.isCoded() && 
					(player.getStatus() == BasicPlayer.PLAYING) ) {
					playerPause();
					waitingForCode = true;
				}
	
				// Move to next utterance.  IMPROVE: If pauseOnUncoded is disabled, it is possible for
				// user to leave utterances uncoded and move on to later utterances.  It would be better
				// to always use pauseOnUncoded behavior.
				if( current.isCoded() || !pauseOnUncoded ) {
					currentUtterance++;
				}
			}
		}
		updateUtteranceDisplays();
		progressReported = false; // Clear flag once (current) progress report is applied.
	}

	// Handle end of media (i.e. audio playback reached end).
	private synchronized void applyEOM() {
		if( isParsingUtterance() ) {
			// Specify end bytes manually from record, as player will now report -1 for
			// encoded stream position.
			parseEnd( endOfMediaPosition );
		}
		endOfMediaReported = false;
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
		bytesPerSecond	= 0;
		try {
			player.open( new File( filenameAudio ) );
			bytesPerSecond = player.getBytesPerSecond();
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
		playerView.getMenuItemLoadAudio().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if( selectAndLoadAudioFile() ) {
					setMode( Mode.PLAYBACK );
				}
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// File Menu: Exit
		playerView.getMenuItemExit().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				actionExit();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Parse Utterances Menu: Start New Parse File
		playerView.getMenuItemNewParse().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				handleNewParseFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Parse Utterances Menu: Load Parse File
		playerView.getMenuItemLoadParse().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				handleLoadParseFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Code Utterances Menu: Start New Code File
		playerView.getMenuItemNewCode().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				handleNewCodeFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Code Utterances Menu: Load Code File
		playerView.getMenuItemLoadCode().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				handleLoadCodeFile();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Global Ratings Menu: Score Global Ratings
		playerView.getMenuItemCodeGlobals().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				handleNewGlobalRatings();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// About Menu: Help
		playerView.getMenuItemHelp().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				showNotImplementedDialog();
			}
		});

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// About Menu: About this Application
		playerView.getMenuItemAbout().addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				handleAboutWindow();
			}
		});

	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Player Handlers
	private synchronized void handleActionPlay() {
		if( waitingForCode ) {
			return; // Ignore play button when waiting for code.
		}
		if( player.getStatus() == BasicPlayer.PLAYING ) {
			playerPause();
		} else if( player.getStatus() == BasicPlayer.PAUSED ) {
			playerResume();
		} else if( player.getStatus() == BasicPlayer.STOPPED ||
				   player.getStatus() == BasicPlayer.OPENED ) {
			playerPlay();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void handleActionReplay(){
		if( templateView instanceof ParserTemplateView ) {
			removeLastUtterance();
		} else if( templateView instanceof MiscTemplateView ) {
			Utterance utterance = getCurrentUtterance();

			// Strip code from current (may or may not be coded at this point).
			if( utterance != null ) {
				utterance.setMiscCode( MiscCode.INVALID );
			}

			// Move to previous utterance, if one exists.
			if( hasPreviousUtterance() ) {
				gotoPreviousUtterance();
				utterance = getCurrentUtterance();

				// Strip code, now that we've stepped back to previous utterance.
				assert utterance != null;
				utterance.setMiscCode( MiscCode.INVALID );
				playerSeek( utterance.getStartBytes() );
			} else {
				playerSeek( 0 );
			}

			waitingForCode = false;
			updateUtteranceDisplays();
		} else {
			showParsingErrorDialog();
		}
		saveSession();
		updateTimeDisplay();
		updateSeekSliderDisplay();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void handleActionRewind5s(){
		// Rewind playback position 5 seconds, without affecting utterances.
		assert( bytesPerSecond > 0 );

		int pos = streamPosition();
		
		pos -= 5 * bytesPerSecond;
		pos = Math.max( pos, 0 ); // Clamp to beginning of file.

		playerSeek( pos );
		updateUtteranceDisplays();
		updateTimeDisplay();
		updateSeekSliderDisplay();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void updateSeekSliderDisplay() {
		// Don't set slider position if user is dragging it.
		if( playerView.getSliderSeek().getValueIsAdjusting() ) {
			return;
		}

		int		position	= player.getEncodedStreamPosition();
		int		length		= player.getEncodedLength();
		double 	t			= 0;
		
		if( length > 0 ) {
			t = position / (double) length;
		}

		if( t >= 1.0 ){
			playerView.setSliderSeek( PlayerView.SEEK_MAX_VAL );
		} else if( t == 0 ) {
			playerView.setSliderSeek( 0 );
		} else {
			playerView.setSliderSeek( (int) (t * PlayerView.SEEK_MAX_VAL) );
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void handleSliderPan() {
		if( player.hasPanControl() ) {
			try {
				player.setPan( playerView.getSliderPan().getValue() / 10.0 );
			} catch( BasicPlayerException e ) {
				displayPlayerException( e );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void handleSliderGain() {
		if( player.hasGainControl() ) {
			try {
				player.setGain( playerView.getSliderGain().getValue() / 100.0 );
			} catch( BasicPlayerException e ) {
				displayPlayerException( e );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Menu Handlers
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void handleNewParseFile() {
		if( player.getStatus() == BasicPlayer.PLAYING ) {
			playerPause();
		}
		saveIfNeeded();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select Save Directory and Enter a Filename for the PARSE File" );
		chooser.setFileFilter( new FileNameExtensionFilter( "PARSE files", "parse" ) );
		chooser.setToolTipText( "Use \".parse\" as the file extension." );
		if( chooser.showSaveDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			if( selectAndLoadAudioFile() ) {
				utteranceList 		= null;
				currentUtterance	= 0;
				waitingForCode		= false;
				filenameParse 		= chooser.getSelectedFile().getAbsolutePath();
				filenameParse 		= correctTextFileType( ".parse", filenameParse );
				setMode( Mode.PARSE );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void handleNewCodeFile() {
		if( player.getStatus() == BasicPlayer.PLAYING ) {
			playerPause();
		}
		saveIfNeeded();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select a PARSE File to Start Coding" );
		chooser.setFileFilter( new FileNameExtensionFilter( "PARSE files", "parse" ) );
		if( chooser.showSaveDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			waitingForCode		= false;
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
			utteranceListChanged();
			loadAudioFile( filenameAudio );
			setMode( Mode.CODE );
			postLoad();
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void handleNewGlobalRatings(){
		if( player.getStatus() == BasicPlayer.PLAYING ) {
			playerPause();
		}
		saveIfNeeded();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select Save Directory and Enter a Filename" );
		chooser.setFileFilter( new FileNameExtensionFilter( "GLOBALS files", "global" ) );
		chooser.setToolTipText( "Use \".global\" as the file extension." );
		if( chooser.showSaveDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			if( selectAndLoadAudioFile() ) {
				utteranceList 		= null;
				currentUtterance	= 0;
				waitingForCode		= false;
				filenameGlobals 	= chooser.getSelectedFile().getAbsolutePath();
				filenameGlobals 	= correctTextFileType( ".global", filenameGlobals );
				setMode( Mode.GLOBALS );
			}
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void handleLoadParseFile(){
		if( player.getStatus() == BasicPlayer.PLAYING ) {
			playerPause();
		}
		saveIfNeeded();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select a PARSE File to Load" );
		chooser.setFileFilter( new FileNameExtensionFilter( "PARSE files", "parse" ) );
		if( chooser.showOpenDialog(playerView) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			waitingForCode		= false;
			filenameParse 		= chooser.getSelectedFile().getAbsolutePath();
			// Load the parse file.
			filenameAudio = getUtteranceList().loadFromFile(new File( filenameParse ));
			utteranceListChanged();
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
	private synchronized void handleLoadCodeFile(){
		if( player.getStatus() == BasicPlayer.PLAYING ) {
			playerPause();
		}
		saveIfNeeded();

		JFileChooser chooser = new JFileChooser();

		chooser.setDialogTitle( "Select a CASAA Code File to Load" );
		chooser.setFileFilter( new FileNameExtensionFilter( "CASAA code files", "casaa" ) );
		if( chooser.showOpenDialog( playerView ) == JFileChooser.APPROVE_OPTION ) {
			utteranceList 		= null;
			currentUtterance	= 0;
			waitingForCode		= false;
			filenameMisc 		= chooser.getSelectedFile().getAbsolutePath();
			filenameAudio 		= getUtteranceList().loadFromFile(new File(filenameMisc)); // Load the code file.
			utteranceListChanged();
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

	// Save current session (i.e. parsing, coding, etc) if necessary.
	private synchronized void saveIfNeeded() {
		if( isParsingUtterance() ) {
			parseEnd();
		}
	}

	// Save current session.  Periodically also save backup copy.
	private synchronized void saveSession() {
		// Save normal file.
		saveCurrentTextFile( false );

		// Save backup every n'th normal save.
		if( numSaves % 10 == 0 ) {
			saveCurrentTextFile( true );
		}
		numSaves++;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private synchronized void saveCurrentTextFile( boolean asBackup ) {
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
		playerView.getTimeline().repaint();
		if( bytesPerSecond != 0 ) {
			// Handles constant bit-rates only.
			int 	bytes 		= player.getEncodedStreamPosition();
			int		seconds		= bytes / bytesPerSecond;

			playerView.setLabelTime( "Time:  " + TimeCode.toString( seconds ) );
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
		case PLAYBACK:
			// No template view.
			break;
		case PARSE:
			templateUI 		= new ParserTemplateUiService( actionTable );
			templateView 	= templateUI.getTemplateView();
			break;
		case CODE:
			templateUI 		= new MiscTemplateUiService( this );
			templateView 	= templateUI.getTemplateView();
			break;
		case GLOBALS:
			templateUI 		= new GlobalTemplateUiService( this );
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

	// Get current playback position, in bytes.
	public int streamPosition() {
		int position = player.getEncodedStreamPosition();

		// If playback has reached end of file, position will be -1.
		// In that case, use length - 1.
		if( position < 0 ) {
			int length = player.getEncodedLength() - 1;

			position = (length > 0) ? (length - 1) : 0;
		}
		return position;
	}

	// Start parse.
	public synchronized void parseStart() {
		// Ignore parseStart if we've rewound earlier than last parse start.
		Utterance last = getLastUtterance();

		if( last != null && streamPosition() < last.getStartBytes() ) {
			return;
		}

		if( isParsingUtterance() ) {
			parseEnd();
		}
		// Record start data.
		assert( bytesPerSecond > 0 );
		int 	startPosition	= streamPosition();
		String 	startString		= TimeCode.toString( startPosition / bytesPerSecond );

		// Create a new utterance.
		int 		order 	= getUtteranceList().size();
		Utterance 	data 	= new MiscDataItem( order, startString, startPosition );

		getUtteranceList().add( data );
		currentUtterance	= order; // Select this as current utterance.

		updateUtteranceDisplays();
	}

	// End parse, reading byte position from player.
	public synchronized void parseEnd() {
		parseEnd( streamPosition() );
	}

	// End parse at given byte position.
	public synchronized void parseEnd( int endBytes ) {
		assert( endBytes >= 0 );

		// Ignore parseEnd if we've rewound earlier than last parse start.
		Utterance last = getLastUtterance();

		if( last != null && endBytes < last.getStartBytes() ) {
			return;
		}

		// Record end data to current utterance (if we have one).
		Utterance 	current 	= getCurrentUtterance();

		if( current != null ) {
			assert( bytesPerSecond > 0 );
			String	endString	= TimeCode.toString( endBytes / bytesPerSecond );

			current.setEndTime( endString );
			current.setEndBytes( endBytes );
			saveSession();
		}
		updateUtteranceDisplays();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// MISC Template Handlers

	public synchronized void handleButtonMiscCode( MiscCode miscCode ) {

		assert( miscCode != MiscCode.INVALID );

		// Assign code to current utterance, if one exists.
		Utterance utterance = getCurrentUtterance();

		if( utterance == null ) {
			return; // No current utterance.
		}

		// If playback position is not within current utterance (i.e. it is between utterances),
		// ignore code.  NOTE: We don't test end bytes because a) when waitingForCode, we will be
		// past end of current utterance and b) when not waitingForCode, we advance utterance index
		// as soon as we pass end of one utterance.
		int playbackPosition = streamPosition();

		if( playbackPosition < utterance.getStartBytes() ) {
			return;
		}

		utterance.setMiscCode( miscCode );
		saveSession();

		// If paused, waiting for a code, advance utterance index and resume playback.
		if( waitingForCode ) {
			currentUtterance++;
			waitingForCode = false;
			playerResume();
		}
		updateUtteranceDisplays();
	}

	private synchronized void removeLastUtterance(){
		getUtteranceList().removeLast();

		if( getUtteranceList().size() > 0 ) {
			currentUtterance = getUtteranceList().size() - 1;
		} else {
			currentUtterance = 0;
		}

		Utterance utterance = getCurrentUtterance();

		if( utterance == null ) {
			playerSeek( 0 );
		} else {
			playerSeek( getCurrentUtterance().getStartBytes() ); // Seek to start of current parsed utterance.

			// Strip end data from current utterance, so it will register as not parsed.
			utterance.stripEndData();
		}

		updateUtteranceDisplays();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Update utterance displays (e.g. current, last, etc) in active template view.
	private synchronized void updateUtteranceDisplays() {
		playerView.getTimeline().repaint();

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
				view.setTextFieldPrev("");
			} else {
				view.setTextFieldPrev(prev.toString());
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
				view.setTextFieldPrev("");
			} else {
				view.setTextFieldPrev(prev.toString());
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

				// Visual indication when in between utterances.
				if( streamPosition() < current.getStartBytes() ) {
					view.setTextFieldStartTimeColor( Color.RED );
				} else {
					view.setTextFieldStartTimeColor( Color.BLACK );
				}
			}
		}
	}

	// Select current utterance index, seek player, and update UI after loading
	// data from file.
	// PRE: Mode is set, so appropriate templateView is active.
	private synchronized void postLoad() {
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
				// Special case - if we haven't coded any utterances yet, always seek to zero.
				// Else seek to start of first uncoded utterance.
				if( currentUtterance == 0 ) {
					playerSeek( 0 );
				} else {
					playerSeek( getCurrentUtterance().getStartBytes() );
				}
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
	private synchronized UtteranceList getUtteranceList() {
		if( utteranceList == null ) {
			utteranceList = new UtteranceList();
		}
		return utteranceList;
	}

	//====================================================================
	// BasicPlayerListener interface
	//====================================================================

	public void opened( Object stream, Map< Object, Object > properties ) {}

	public void setController( BasicController controller ) {}

	public void progress( int bytesread, long microseconds, byte[] pcmdata,
						  Map< Object, Object > properties ) {
		progressReported = true; // Will be handled in main thread's run().
	}

	public void stateUpdated( BasicPlayerEvent event ) {
		// Notification of BasicPlayer states (opened, playing, end of media, ...).
		// Modify stored playerStatus string only on "significant" changes (e.g. "Opened", but not "Seeked").
		// Synchronize, so we apply changes before another status update comes in.
		synchronized( this ) {
			String oldStatus = new String( playerStatus );

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
				// Record position before setting flag, to ensure flag is not handled
				// in main thread until position is valid.
				endOfMediaPosition = event.getPosition();
				endOfMediaReported = true;
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

			// If status has changed, and no later-ordered event has already changed the status,
			// apply this event's changes.
			if( !playerStatus.equals( oldStatus ) ) {
				if( event.getIndex() >= statusChangeEventIndex ) {
					statusChangeEventIndex = event.getIndex();

					File	file	= new File( filenameAudio );
					String	str		= playerStatus.concat( ":  " + file.getName() + "  |  Total Time = " +
							TimeCode.toString( player.getSecondsPerFile() ) );
			
					playerView.setLabelPlayerStatus( str );
				}
			}
		}
	}
}

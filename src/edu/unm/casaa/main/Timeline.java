package edu.unm.casaa.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.unm.casaa.utterance.Utterance;

// Timeline is a custom renderer for utterance data.
public class Timeline extends JPanel {
	private static final long serialVersionUID = 1L;

	private Dimension 		dimension = new Dimension( 800, 80 );
	private MainController 	control;
	private Insets			insets;

	public Timeline( MainController control ) {
		assert( control != null );
		this.control = control;
		setBorder( BorderFactory.createTitledBorder( "Timeline" ) );
		insets = getBorder().getBorderInsets( this );
	}

	public Dimension getPreferredSize() {
		return dimension;
	}

	public void paintComponent( Graphics g ) {
		super.paintComponent( g );

		// Early-out if no audio file is loaded.
		int audioLength	= control.getAudioLength();

		if( audioLength <= 0 ) {
			return;
		}

		// Adjust clip by border insets, so rendering does not overlap border.
		Rectangle 	clip 		= g.getClipBounds();
		int 		innerRight	= getWidth() - insets.right;
		int 		innerBottom	= getHeight() - insets.bottom;
		int 		clipW		= clip.width;
		int 		clipH		= clip.height;
		int 		clipX		= clip.x;
		int 		clipY		= clip.y;

		if( clipX < insets.left )
		{
			clipW -= (insets.left - clipX);
			clipX = insets.left;
		}
		if( clipY < insets.top )
		{
			clipH -= (insets.top - clipY);
			clipY = insets.top;
		}
		clipW = Math.min( clipW, innerRight - clipX );
		clipH = Math.min( clipH, innerBottom - clipY );

		g.setClip( clipX, clipY, clipW, clipH );

		// Timeline.
		int fontAscent	= g.getFontMetrics().getAscent();
		int fontHeight	= g.getFontMetrics().getHeight();
		int centerLineY = insets.top + ((innerBottom - insets.top) / 2);

		centerLineY += fontHeight / 2; // Shift down, since we have a line of text (time stamps) above boxes.

		g.setColor( Color.GRAY );
		g.drawLine( insets.left, centerLineY, getWidth() - insets.right, centerLineY );

		// Utterances.
		int	pixelsPerSecond	= 100; // Determines zoom level.
		int bytesPerSecond 	= control.getBytesPerSecond();
		int bytesPerPixel	= (int) (bytesPerSecond / (float) pixelsPerSecond);

		assert( bytesPerPixel > 0 );

		// TODO:
		// - Derive position and size based on component dimensions, and inner area (within border).
		// - Scroll to appropriate region.
		//   - Maybe keep current time marker centered on screen (except when we're at beginning/end).
		// - Handle zoom (this may not actually be configurable).
		// - Highlight current utterance.
		int	boxH		= fontHeight + 10; // Include some space around font.
		int boxY		= centerLineY - (boxH / 2);

		for( int i = 0; i < control.numUtterances(); i++ ) {
			Utterance 	u 			= control.utterance( i );
			int			startBytes	= u.getStartBytes();
			int			endBytes	= u.isParsed() ? u.getEndBytes() : audioLength;
			int 		boxW 		= (endBytes - startBytes) / bytesPerPixel;
			int  		boxX 		= insets.left + (startBytes / bytesPerPixel);
			Color		borderColor	= new Color( 0.0f, 0.0f, 1.0f ); // Dark blue.
			Color		boxColor	= new Color( 0.75f, 0.75f, 1.0f ); // Light blue.

			if( u == control.getCurrentUtterance() ) {
				borderColor = new Color( 0.0f, 1.0f, 0.0f ); // Dark green.
				boxColor 	= new Color( 0.75f, 1.0f, 0.75f ); // Light green.
			}
			g.setColor( boxColor );
			g.fillRect( boxX, boxY, boxW, boxH );
			g.setColor( borderColor );
			g.drawRect( boxX, boxY, boxW, boxH );

			// Time stamp.
			g.setColor( Color.BLACK );
			g.drawString( u.getStartTime(), boxX + 5, boxY - 5 );

			// Label.
			// TODO - What should we include in label?  Ask UNM.
			// - Enumeration?
			// - MISC code numeric value?
			// - MISC code label?
			String	label = "" + u.getEnum() + ")";

			if( u.isCoded() ) {
				label += " " + u.getMiscCode().label;
			}
			g.drawString( label, boxX + 5, boxY + fontAscent + 5 );
		}

		// Current playback time indicator.
		// TODO - Can we avoid repainting everything if this is the only thing changing?
		//   - This might not be a significant optimization, if we need to scroll entire display around as time changes.
		int position	= control.streamPosition();
		int timeX		= insets.left + (position / bytesPerPixel);

		g.setColor( Color.GRAY );
		g.drawLine( timeX, insets.top, timeX, getHeight() - insets.bottom );

		// Restore original clip shape, so border is not clipped.
		g.setClip( clip );
	}
}

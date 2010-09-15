package edu.unm.casaa.main;

import java.awt.BorderLayout;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OptionsWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel            panelGainPan     = null;
    private JSlider           sliderGain       = null;
    private JSlider           sliderPan        = null;

    private static final int  GAIN_MIN_VAL     = 0;
    private static final int  GAIN_MAX_VAL     = 100;
    private static final int  GAIN_TICK_VAL    = 10;
    private static final int  GAIN_INIT_VAL    = 50;
    private TitledBorder      borderGain       = null;

    private static final int  PAN_MIN_VAL      = -10;
    private static final int  PAN_MAX_VAL      = 10;
    private static final int  PAN_TICK_VAL     = 2;
    private static final int  PAN_INIT_VAL     = 0;
    private TitledBorder      borderPan        = null;

    public OptionsWindow() {
        JPanel content = (JPanel) getContentPane();

        setTitle( "Options" );
        content.add( getPanelGainPan(), BorderLayout.CENTER );
        pack();
        setLocationRelativeTo( null ); // Center on screen.

        // Register listeners.

        getSliderPan().addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent ce ) {
                if( getSliderPan().getValueIsAdjusting() ) {
                    MainController.instance.handleSliderPan( getSliderPan() );
                }
            }
        } );

        getSliderGain().addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent ce ) {
                if( getSliderGain().getValueIsAdjusting() ) {
                    MainController.instance.handleSliderGain( getSliderGain() );
                }
            }
        } );
    }

    public void applyAudioOptions() {
        MainController.instance.handleSliderGain( getSliderGain() );
        MainController.instance.handleSliderPan( getSliderPan() );
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /**
     * Returns the Player's Pan Slider
     * 
     * @return a JSlider used to control pan.
     */
    public JSlider getSliderPan() {
        if( sliderPan == null ) {
            // Actual control uses -1.0 to 1.0 with 0.0 at center.
            sliderPan = new JSlider( SwingConstants.HORIZONTAL, PAN_MIN_VAL, PAN_MAX_VAL, PAN_INIT_VAL );
            sliderPan.setMajorTickSpacing( PAN_TICK_VAL );
            sliderPan.setSnapToTicks( true );
            sliderPan.setPaintTicks( true );

            Hashtable< Integer, JLabel > tableLabel = new Hashtable< Integer, JLabel >();

            tableLabel.put( new Integer( 0 ), new JLabel( "Center" ) );
            tableLabel.put( new Integer( -10 ), new JLabel( "Left" ) );
            tableLabel.put( new Integer( 10 ), new JLabel( "Right" ) );
            sliderPan.setLabelTable( tableLabel );
            sliderPan.setPaintLabels( true );
            sliderPan.setBorder( getBorderPan() );
        }
        return sliderPan;
    }

    private Border getBorderPan() {
        if( borderPan == null ) {
            borderPan = BorderFactory.createTitledBorder( "Balance" );
            borderPan.setTitleJustification( TitledBorder.LEADING );
        }
        return borderPan;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private JPanel getPanelGainPan() {
        if( panelGainPan == null ) {
            panelGainPan = new JPanel();
            panelGainPan.setLayout( new BoxLayout( panelGainPan, BoxLayout.X_AXIS ) );
            panelGainPan.add( getSliderGain() );
            panelGainPan.add( getSliderPan() );
        }
        return panelGainPan;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /**
     * Returns the Player's Gain Slider
     * 
     * @return a JSlider used to control gain (volume)
     */
    public JSlider getSliderGain() {
        if( sliderGain == null ) {
            // Actual control uses 0.0 to 1.0 in increments of 0.05.
            sliderGain = new JSlider( SwingConstants.HORIZONTAL, GAIN_MIN_VAL, GAIN_MAX_VAL, GAIN_INIT_VAL );
            sliderGain.setMajorTickSpacing( GAIN_TICK_VAL );

            Hashtable< Integer, JLabel > tableLabel = new Hashtable< Integer, JLabel >();

            tableLabel.put( new Integer( 0 ), new JLabel( "0" ) );
            tableLabel.put( new Integer( 10 ), new JLabel( "1" ) );
            tableLabel.put( new Integer( 20 ), new JLabel( "2" ) );
            tableLabel.put( new Integer( 30 ), new JLabel( "3" ) );
            tableLabel.put( new Integer( 40 ), new JLabel( "4" ) );
            tableLabel.put( new Integer( 50 ), new JLabel( "5" ) );
            tableLabel.put( new Integer( 60 ), new JLabel( "6" ) );
            tableLabel.put( new Integer( 70 ), new JLabel( "7" ) );
            tableLabel.put( new Integer( 80 ), new JLabel( "8" ) );
            tableLabel.put( new Integer( 90 ), new JLabel( "9" ) );
            tableLabel.put( new Integer( 100 ), new JLabel( "10" ) );
            sliderGain.setLabelTable( tableLabel );
            sliderGain.setPaintTicks( true );
            sliderGain.setPaintLabels( true );
            sliderGain.setBorder( getBorderGain() );
        }
        return sliderGain;
    }

    private Border getBorderGain() {
        if( borderGain == null ) {
            borderGain = BorderFactory.createTitledBorder( "Volume" );
            borderGain.setTitleJustification( TitledBorder.LEADING );
        }
        return borderGain;
    }

}

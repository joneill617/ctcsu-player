package edu.unm.casaa.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class Splash extends JWindow {

    private static final long serialVersionUID = 1L;

    public Splash() {
        JPanel content = (JPanel) getContentPane();

        content.setBackground( Color.white );

        // Set the window's bounds, centering the window.
        Dimension   screen  = Toolkit.getDefaultToolkit().getScreenSize();
        int         width   = Math.min( screen.width / 2, 600 );
        int         height  = Math.min( screen.height / 2, 250 );
        int         x       = (screen.width - width) / 2;
        int         y       = (screen.height - height) / 2;

        setBounds( x, y, width, height );

        // Build the splash screen.
        // TODO - Get contents from UNM.
        JLabel image    = new JLabel( new ImageIcon( "images/UNM_Color.jpg" ) );
        JLabel title    = new JLabel( "CACTI | The CASAA Application for Coding Treatment Interactions", JLabel.CENTER );
        JLabel version  = new JLabel( "v" + Version.versionString(), JLabel.CENTER );

        title.setFont( new Font( "Sans-Serif", Font.PLAIN, 16 ) );
        version.setFont( new Font( "Sans-Serif", Font.PLAIN, 14 ) );
        content.add( title, BorderLayout.NORTH );
        content.add( image, BorderLayout.CENTER );
        content.add( version, BorderLayout.SOUTH );

        Color oraRed = new Color( 156, 20, 20, 255 );

        content.setBorder( BorderFactory.createLineBorder( oraRed, 5 ) );
    }
}

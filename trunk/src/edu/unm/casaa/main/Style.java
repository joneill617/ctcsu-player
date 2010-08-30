package edu.unm.casaa.main;

import java.awt.Color;

import javax.swing.JTextField;

// Style is central definition for UI style.
public class Style {

	public static void	configureLightText( JTextField field ) {
		field.setForeground( Color.GRAY );
	}

	public static void	configureStrongText( JTextField field ) {
		// NOTE: This is currently the same as the default JTextField configuration, so
		// this method is really here for forward-compatibility.
		field.setForeground( Color.BLACK );
	}
}

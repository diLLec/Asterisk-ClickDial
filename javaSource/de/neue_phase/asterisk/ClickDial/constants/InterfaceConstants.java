package de.neue_phase.asterisk.ClickDial.constants;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 * @since 03. Juni 2007
 * constants defined to be used by Interface package
 *  
 */

public class InterfaceConstants {

	/**
	 * the official used name for this program
	 * InterfaceConstants.myName
	 */
	public static final String  myName	= "Asterisk Click-Dial";
	
	
	/** 
	 * @see de.neue_phase.asterisk.ClickDial.widgets.DialWindow
	 */
	
	public static final String  DialWindowBackgroundImage = "lib\\icons\\HG.png";
	public static final boolean DialWindowUseAlpha 		  = true;
	public static final Color	DialWindowBackground	  = new Color(Display.getDefault (),190,190,190);
	
	/**
	 * @see de.neue_phase.asterisk.ClickDial.controller.DialWindowController
	 */
	
	public static final int DialWindowController_timerValueForPopUpList = 2000; // in milis

	/**
	 * DialList Autocomplete
	 */

	public static final String DialWindowAutocompleteHotkey = "Ctrl+Space";

	/**
	 *  @see de.neue_phase.asterisk.ClickDial.widgets.UserActionBox
	 */
	public static enum SettingsImages {
		error, info, warning
	}
	
	/**
	 * @see de.neue_phase.asterisk.ClickDial.widgets.SplashScreen
	 */

	public static final String SplashScreen_splashImage = "lib\\icons\\splash.png";

	/**
	 * @see de.neue_phase.asterisk.ClickDial.controller.DialWindowController
	 */
	
	public static final String DialWindow_red_lamp_icon 	= "red_lamp";
	public static final String DialWindow_green_lamp_icon 	= "green_lamp";

	/**
	 * @see de.neue_phase.asterisk.ClickDial.widgets.CallWindow
	 * size of the call window
	 * InterfaceConstants.CallWindow_size
	 */
	
	public static final Point CallWindow_size = new Point(150,120); // (width, height)
	public static enum CallWindowAppearEdges {
		lower, upper, right, left
	}
	
	public static CallWindowAppearEdges configStringToEnum (String edge) {
		if (edge.equals("lower edge"))
			return CallWindowAppearEdges.lower;

		if (edge.equals("upper edge"))
			return CallWindowAppearEdges.upper;
		
		if (edge.equals("right edge"))
			return CallWindowAppearEdges.right;
		
		if (edge.equals("left edge"))
			return CallWindowAppearEdges.left;
		
		return CallWindowAppearEdges.right;
	}
	
	public static final Integer	MaxCallWindowInstances		= 10;
	
	
	/**
	 * the path where SettingsTypeIcons are searched
	 * @see de.neue_phase.asterisk.ClickDial.widgets.SettingsWindow
	 */
	
	public static final String SettingsTypeIcons_Path 	= "lib\\icons\\";
	public static final String SettingsTypeIcons_Suffix 	= ".png";
	public static final String SettingsTreeIconPath 	= SettingsTypeIcons_Path + "16\\";
	public static final String SettingsTreeIconExpander = SettingsTreeIconPath   + "arrow.png";

	
	/**
	 * Tray Icon
	 */
	public static final String TrayIcon_Path 	= SettingsTypeIcons_Path + "tray\\";
	public static final String TrayIcon_Icon 	= TrayIcon_Path + "asterisk-tray.png";
	public static final String TrayIcon_ToolTip = InterfaceConstants.myName;

    public static enum WorkstateTypes {
        Arbeit,
        AusserHaus,
        Feierabend,
        Pause
    }

}

package de.neue_phase.asterisk.ClickDial.constants;

/**
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 * @since 03. Juni 2007
 * constants defined to be used by Settings Package
 * 
 */

public class SettingsConstants {

	/**
	 * @see de.neue_phase.asterisk.ClickDial.settings.SettingsHolder
	 */
	
	public static enum SettingsTypes {
		none, asterisk, global, datasource
	}
	
	public static enum SettingsExpander {
		Base, Asterisk, Plugins
	}
	
	public static enum SettingsElementType {
		text, checkbox, radio, dropdown, spinner
	}
	

	/**
	 * the default location where files are searched
	 */
	public static final String configSearchLocation 	= "conf/";
	
	/** 
	 * the default suffix, which is appended on file search
	 */
	public static final String configSearchSuffix 		= ".cfg";
	
	/**
	 * size of the settings dialog
	 * @see de.neue_phase.asterisk.ClickDial.widgets.SettingsWindow
	 */
	
	public static final int SettingsWindow_heigth = 500;
	public static final int SettingsWindow_width  = 1000;
	
	/**
	 * @see de.neue_phase.asterisk.ClickDial.settings.SettingsAbstractMaster
	 */
	
	public static final int SettingsAbstractMaster_version = 9;
	
}


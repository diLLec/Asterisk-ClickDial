package de.neue_phase.asterisk.ClickDial.settings;

import org.eclipse.swt.widgets.Display;

import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;

/**
 * The Global settings class
 *  
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsGlobal extends SettingsAbstractMaster {

	private static final long serialVersionUID 				= -8636697812094090038L;
	protected SettingsConstants.SettingsTypes    type 		= SettingsConstants.SettingsTypes.global;
	protected SettingsConstants.SettingsExpander expander 	= SettingsConstants.SettingsExpander.Base;


	public SettingsGlobal() {
		super();
	}

	@Override
	protected void initializeSettings() {
		super.initializeSettings();

		SettingsElement tmp;
		tmp = new SettingsElement("start_minimized", "start window minimized"	, "", SettingsConstants.SettingsElementType.checkbox);
		settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("naming_forename", "Forename"	, null);
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("naming_lastname", "Lastname"	, null);
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("naming_personal_number", "Telephone number", null);
		settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("call_window_monitor", "Which Monitor should be\nused for the Call Window ?", "1", SettingsConstants.SettingsElementType.dropdown);
		
		int monitor_count 			= Display.getCurrent().getMonitors().length;
		String[] dropdown_values 	= new String[monitor_count];
		for (Integer i = 1; i <= monitor_count; i++)
			dropdown_values[i-1] = i.toString();

		tmp.registerSpecificSetting("dropdown_values", dropdown_values );
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("call_window_from", "From which screen edge should \n the \"new Call Window\" appear ?", "right edge", SettingsConstants.SettingsElementType.dropdown);
		tmp.registerSpecificSetting("dropdown_values", new String[] {"lower edge", "right edge", "upper edge", "left edge", } );
		settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("call_window_trans", "Maximum CallWindow transparency", "80", SettingsConstants.SettingsElementType.spinner);
		tmp.registerSpecificSetting("spinner_min_max", new String[] {"10", "255"} );
		settings.put(tmp.getName(), tmp);
	}
	
	/** 
	 * return the defined type
	 * @return the type
	 */
	@Override
	public SettingsConstants.SettingsTypes getType() {
		return type;
	}

	/**
	 * return the designated expander
	 * @return the expander
	 */
	@Override
	public SettingsConstants.SettingsExpander getExpander () {
		return expander;
	}
}

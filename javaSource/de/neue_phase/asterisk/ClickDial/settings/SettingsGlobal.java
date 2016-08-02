package de.neue_phase.asterisk.ClickDial.settings;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import org.eclipse.swt.widgets.Display;

import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;

import java.util.Arrays;

/**
 * The Global settings class
 *  
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsGlobal extends SettingsAbstractMaster {

	private static final long serialVersionUID 				= -8636697812094090038L;
	protected SettingsConstants.SettingsTypes    type 		= SettingsConstants.SettingsTypes.global;
	private SettingsConstants.SettingsExpander expander 	= SettingsConstants.SettingsExpander.Base;
	private Integer monitorCount = 0;

	public SettingsGlobal() {
		super();
	}

	@Override
	protected void initializeSettings() {
		super.initializeSettings();

		SettingsElement tmp;
		tmp = new SettingsElement("start_minimized", "Start ClickDial minimized", "", SettingsConstants.SettingsElementType.checkbox);
		settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("naming_forename", "Forename"	, null);
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("naming_lastname", "Lastname"	, null);
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("naming_personal_number", "Telephone number", null);
		settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("call_window_monitor", "Monitor for call notification", "1", SettingsConstants.SettingsElementType.dropdown);

		Display.getDefault ().syncExec (() -> SettingsGlobal.this.monitorCount = Display.getDefault ().getMonitors().length);
		log.debug (String.format ("monitor count according to swt: %d", SettingsGlobal.this.monitorCount));

		String[] dropdown_values 	= new String[SettingsGlobal.this.monitorCount];
		for (Integer i = 1; i <= SettingsGlobal.this.monitorCount; i++)
			dropdown_values[i-1] = i.toString();

		tmp.registerSpecificSetting("dropdown_values", dropdown_values );
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("call_window_from", "Place call notification at", "right edge", SettingsConstants.SettingsElementType.dropdown);
		tmp.registerSpecificSetting("dropdown_values", new String[] {"lower edge", "right edge", "upper edge", "left edge", } );
		settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("call_window_trans", "Call notification window transparency", "80", SettingsConstants.SettingsElementType.spinner);
		tmp.registerSpecificSetting("spinner_min_max", new String[] {"10", "255"} );
		settings.put(tmp.getName(), tmp);

        tmp = new SettingsElement("text_selection_call_hk", "Hotkey for dial currently selected text", "STRG+A", SettingsConstants.SettingsElementType.dropdown);
        tmp.registerSpecificSetting("dropdown_values", new String[] {"STRG+A", "STRG+D" } );
        settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("change_workstate_on_screenlock", "Change workstate on screenlock", "1", SettingsConstants.SettingsElementType.checkbox);
		settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("change_workstate_on_screenlock_target_workstate", "Target workstate on screenlock",
								  InterfaceConstants.WorkstateTypes.Arbeit.name (), SettingsConstants.SettingsElementType.dropdown);
		tmp.registerSpecificSetting("dropdown_values", InterfaceConstants.enumToStringArray (InterfaceConstants.WorkstateTypes.class) );
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

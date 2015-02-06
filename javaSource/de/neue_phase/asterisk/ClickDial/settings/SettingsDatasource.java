package de.neue_phase.asterisk.ClickDial.settings;

import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;

/**
 * The Datasource settings class
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsDatasource extends SettingsAbstractMaster {

	private static final long serialVersionUID 				= -9162690728854994782L;
	protected SettingsConstants.SettingsTypes    type 		= SettingsConstants.SettingsTypes.datasource;
	protected SettingsConstants.SettingsExpander expander 	= SettingsConstants.SettingsExpander.Base;

	public SettingsDatasource() {
		super();
	}

	@Override
	protected void initializeSettings() {
		super.initializeSettings();
		SettingsElement tmp;

		tmp = new SettingsElement("xml_enabled", "XML Datasource", "", SettingsConstants.SettingsElementType.checkbox);
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("outlook_enabled", "Local Outlook Datasource", "", SettingsConstants.SettingsElementType.checkbox);
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("ldap_enabled", "LDAP Datasource" , "", SettingsConstants.SettingsElementType.checkbox);
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

package de.neue_phase.asterisk.ClickDial.settings;

import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractAsteriskManagerInterfaceAuthData;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractAsteriskManagerInterfaceConnectionData;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ISettingsExtractModel;

/**
 * The Asterisk Settings class
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsAsterisk extends SettingsAbstractMaster {

	protected SettingsConstants.SettingsTypes 	 type 		= SettingsConstants.SettingsTypes.asterisk;
	protected SettingsConstants.SettingsExpander expander 	= SettingsConstants.SettingsExpander.Asterisk;
	
	private static final long serialVersionUID 	= -180232310382368293L;

	public SettingsAsterisk() {
		super();
	}

	@Override
	protected void initializeSettings() {
		super.initializeSettings();


		SettingsElement tmp;
		tmp = new SettingsElement("asterisk_hostname", "Hostname", null);
		tmp.enableAutoConfig ();
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("asterisk_port", "Port", "5038");
		tmp.enableAutoConfig ();
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("asterisk_user", "Username", null);
		tmp.enableAutoConfig ();
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("asterisk_pass", "Password", null);
		tmp.enableAutoConfig ();
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("asterisk_channel", "Phone Channel Name"	, null);
		tmp.enableAutoConfig ();
		settings.put(tmp.getName(), tmp);
		
		tmp = new SettingsElement("asterisk_timeout", "Connect Timeout", "30");
		tmp.enableAutoConfig ();
		settings.put(tmp.getName(), tmp);

		tmp = new SettingsElement("asterisk_callerid", "Caller Id", "Firstname Lastname <123456>");
		tmp.enableAutoConfig ();
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

	/**
	 * @return settings extract (connect data)
	 */
	public String getAsteriskConnectString () {
		return settings.get("asterisk_hostname").getValue () + ":" + settings.get("asterisk_port").getValue ();
	}

	/**
	 * @return settings extract (auth data)
	 */
	public ISettingsExtractModel getAsteriskAuthData () {
		ExtractAsteriskManagerInterfaceConnectionData connectData = new ExtractAsteriskManagerInterfaceConnectionData (settings.get("asterisk_hostname").getValue (),
																													   settings.get("asterisk_port").getValue ());
		return new ExtractAsteriskManagerInterfaceAuthData (connectData,
															settings.get("asterisk_user").getValue (),
															settings.get("asterisk_pass").getValue (),
															new Integer(settings.get("asterisk_timeout").getValue ()),
															settings.get("asterisk_channel").getValue ()
															);
	}

}

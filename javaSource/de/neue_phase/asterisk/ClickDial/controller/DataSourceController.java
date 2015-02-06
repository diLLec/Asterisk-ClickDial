/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.controller;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.util.Dispatcher;
import org.apache.log4j.Logger;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsTypes;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.datasource.DataSourceHolder;
import de.neue_phase.asterisk.ClickDial.datasource.OutlookDataSource;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;

/**
 * This will encapsulate every dial window helper datasource inside 
 * the application
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */
public class DataSourceController extends ControllerBaseClass implements ControllerInterface {

	protected DataSourceHolder holder			= null;
	private final Logger    log 				= Logger.getLogger(this.getClass());
	
	/**
	 * 
	 */
	public DataSourceController(SettingsHolder settingsRef, BaseController b, Dispatcher dispatcherRef) {
		super(settingsRef, b);
		type = ControllerTypes.DataSource;
		holder = new DataSourceHolder (dispatcherRef);
	}

	/* (non-Javadoc)
	 * @see de.neue_phase.asterisk.ClickDial.controller.ControllerInterface#startUp()
	 */
	public void startUp() throws InitException {
		/* check down the settings, which datasources may be added */
		checkSettings();
	}

	private boolean checkSettings () {

		if (! settingsRef.get(SettingsTypes.datasource).getValue("xml_enabled").isEmpty()) {
			// create a new XMLDataSource object and register it 
		}
		
		if (! settingsRef.get(SettingsTypes.datasource).getValue("outlook_enabled").isEmpty()) {
			log.debug("Outlook datasource is enabled!");
			holder.registerDatasource(new OutlookDataSource());
		}
			
		if (! settingsRef.get(SettingsTypes.datasource).getValue("ldap_enabled").isEmpty()) {
			// create an LDAP DS ?
		}
		
		return true;
	}

	/**
	 * close every ressource
	 */
	public void closeDown () {
		holder.closeDown ();
	}
	
	/**
	 * 
	 * @return true if this controller controls a widget
	 */
	public boolean isWidgetController() {
		return false;
	}
	
}


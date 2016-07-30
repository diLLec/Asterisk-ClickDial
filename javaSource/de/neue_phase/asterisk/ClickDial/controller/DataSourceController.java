/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.controller;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.datasource.WebservicePhonebookDataSource;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.SettingsUpdatedEvent;
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
	public DataSourceController(SettingsHolder settingsRef, BaseController b) {
		super(settingsRef, b);

		type = ControllerTypes.DataSource;
		holder = new DataSourceHolder ();

        EventBusFactory.getThreadPerTaskEventBus ().register (this); // for SettingsUpdatedEvent
	}

    /**
     * @param event event that indicates what has been updated
     */
    @Subscribe public void handleSettingsUpdatedEvent (SettingsUpdatedEvent event) {
        if (event.getUpdatedTypes ().contains (SettingsTypes.datasource))
            this.checkSettings ();
    }

	/* (non-Javadoc)
	 * @see de.neue_phase.asterisk.ClickDial.controller.ControllerInterface#startUp()
	 */
	public void startUp() throws InitException {
		checkSettings();
	}


    /**
     * check the settings and add the enabled datasources
     */
	private void checkSettings () {

		if (settingsRef.get(SettingsTypes.datasource).getValue("webservice_enabled").equals ("1") &&
            ! holder.isRegistered(WebservicePhonebookDataSource.class)) {
			// create a new XMLDataSource object and register it
            log.debug("Webservice datasource is enabled!");
            holder.registerDatasource (new WebservicePhonebookDataSource ());
		}
		
		if (settingsRef.get(SettingsTypes.datasource).getValue("outlook_enabled").equals ("1") &&
            ! holder.isRegistered(OutlookDataSource.class)) {
			log.debug("Outlook datasource is enabled! ("+settingsRef.get(SettingsTypes.datasource).getValue("outlook_enabled")+")");
			holder.registerDatasource(new OutlookDataSource());
		}
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


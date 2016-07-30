package de.neue_phase.asterisk.ClickDial.controller;


import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import de.neue_phase.asterisk.ClickDial.widgets.SettingsWindow;
import org.eclipse.swt.widgets.Display;

public class SettingsController extends ControllerBaseClass {

    private SettingsWindow widget = null;

    public SettingsController(SettingsHolder settingsRef, BaseController b) {
        super (settingsRef, b);
        widget      = new SettingsWindow (settingsRef);
        type		= ControllerConstants.ControllerTypes.Settings;
    }

    @Override
    public void startUp () throws InitException {
        // nothing to startup
    }

    @Override
    public void closeDown () {
        // nothing to close
    }

    /**
     * open a settings window without predestination
     */
    public void openSettingsWindow () {
        widget.open ();
    }

    /**
     * open a settings window with predestination asterisk settings
     */
    public void openAsteriskSettingsWindow () {
        widget.open (SettingsConstants.SettingsTypes.asterisk);
    }

    /**
     * open a settings window with predestination global settings
     */
    public void openGlobalSettingsWindow () {
        widget.open (SettingsConstants.SettingsTypes.global);
    }

    /**
     * open a settings window with predestination datasource settings
     */
    public void openDataSourceSettingsWindow () {
        widget.open (SettingsConstants.SettingsTypes.datasource);
    }

    /**
     * returns if the controller controls a SWT widget or not
     * @return yes/no
     */
    @Override
    public boolean isWidgetController () {
        return true;
    }
}

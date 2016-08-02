/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.boot;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.ServiceConstants;
import de.neue_phase.asterisk.ClickDial.controller.*;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.jobs.AutoConfigJob;
import de.neue_phase.asterisk.ClickDial.jobs.IJob;
import de.neue_phase.asterisk.ClickDial.jobs.JobFactory;
import de.neue_phase.asterisk.ClickDial.jobs.exceptions.JobCreationException;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerWebservice;
import de.neue_phase.asterisk.ClickDial.settings.*;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerInterface;
import de.neue_phase.asterisk.ClickDial.widgets.SplashScreen;

import java.util.logging.Level;

/**
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 * @since 03. Juni 2007
 *
 * Bootstrap class to bring elemental parts up and
 * start controllers.
 *
 */
public class Bootstrap {

	/**
	 * @param args
	 */
	
	/* the logging facility */
	private static final Logger    log 		= Logger.getLogger(Bootstrap.class);

    /* the global settings reader */
    private static SettingsHolder  settings;

	/* the base controller */
	private static BaseController bC;

	/* the splash screen */
	private static SplashScreen splash;

	public static Rectangle priMonSize;
	
	public static void main(String[] args) {
		log.info(InterfaceConstants.myName + " starting up. SWT Version = " + SWT.getVersion() + " OS = " + System.getProperty("os.name"));

		/* start the controllers */
		log.debug("Init Display and other SWT stuff");
        Display display 		= new Display ();
        Shell primaryShell	    = new Shell();
        priMonSize              = display.getPrimaryMonitor().getBounds();
        settings 	            = SettingsHolder.getInstance();
        bC		                = new BaseController (display);


        splash = new SplashScreen (8);
        splash.open();

        log.debug("Starting the Boostrap ...");
        new Bootstrap().run (display);

		log.debug("Splash screen iteration started");
		splash.goIteration();
		
		/* we've been dropped out of the splash iteration - this means, that
		 * the splash is finished and we can go into base controller widget iteration now
		 */

		bC.iterate();
	}

	/**
	 *
	 * @param display The root display
     */
	private void run(Display display) {

		EventBusFactory.instantiateDisplayEventBus(display);
		log.debug("Starting the instantiation thread.");

		new Thread (() -> {
            /*
                 instantiate the display event bus
             */

            /*
             *  first step:   parse the configuration
             */
            splash.setDescribingText("Loading settings ...");
            settings.newTile(SettingsAsterisk.class);
            settings.newTile(SettingsGlobal.class);
            settings.newTile(SettingsDatasource.class);
            bC.bringUp (new SettingsController (settings, bC), BaseController.BRINGUP_SHUTDOWN_IF_FAIL);

            log.debug("starting TrayIconController");
            bC.bringUp(new TrayIconController(settings, bC), BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
            log.debug("... done starting TrayIconController");

            /*
             * register eventbus
             */
            EventBusFactory.getDisplayThreadEventBus ().register (bC);

            splash.setDescribingText("Connecting Service Interfaces ...");
            bC.initKeystore ();

            AsteriskManagerWebservice asWebservice = new AsteriskManagerWebservice (ServiceConstants.WebserviceURL);
            bC.bringUp (asWebservice, BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
            EventBusFactory.getSyncEventBus ().register (asWebservice);
            EventBusFactory.getThreadPerTaskEventBus ().register (asWebservice);

            splash.setDescribingText("Starting AutoConfiguration via Asterisk Manager Webservice ...");
            try {
                IJob job = JobFactory.createJob (ControllerConstants.JobTypes.AutoConfig);
                bC.bringUp (job, BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
            } catch (JobCreationException e) {
                log.error ("Unable to spawn AutoConfig job", e);
            }


            splash.setDescribingText("Starting WorkstateGetterJob ...");
            try {
                IJob job = JobFactory.createJob (ControllerConstants.JobTypes.WorkstateGetter);
                EventBusFactory.getDisplayThreadEventBus ().register (job);
                bC.bringUp (job, BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
            } catch (JobCreationException e) {
                log.error ("Unable to spawn WorkstateGetterJob", e);
            }

            splash.setDescribingText("Starting ScreenLockWatcherJob ...");
            try {
                IJob job = JobFactory.createJob (ControllerConstants.JobTypes.ScreenLockWatcherJob);
                EventBusFactory.getDisplayThreadEventBus ().register (job);
                bC.bringUp (job, BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
            } catch (JobCreationException e) {
                log.error ("Unable to spawn ScreenLockWatcherJob", e);
            }

            bC.scheduleJobWatchdog ();

            splash.setDescribingText("linking with asterisk ...");
            log.debug("starting AsteriskConnectionController ...");
            bC.bringUp(new AsteriskManagerInterface (), BaseController.BRINGUP_CONTINUE_IF_FAIL);

            log.debug("... done starting AsteriskConnectionController");
            /*
             * second step:   startup the controller's
             */

            /* the DataSources Controller */
            splash.setDescribingText ("Connecting datasources ...");
            log.debug("starting DataSourceController ...");
            bC.bringUp(new DataSourceController(settings, bC), BaseController.BRINGUP_CONTINUE_IF_FAIL);
            log.debug("... done starting DataSourceController");
            /* the AsteriskConnection Controller */

            /* !!!!
             * it is imperative, that this Controller is started before AsteriskConnectionController is
             * started - AsteriskConnectionController uses this Controller to pop-up the CallWindows
             * !!!!
             */
            log.debug("starting CallWindowController");
            bC.bringUp(new CallWindowController(settings, bC), BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
            log.debug("... done starting CallWindowController");

            log.debug("starting HotkeyController");
            bC.bringUp(new HotkeyController (settings, bC), BaseController.BRINGUP_CONTINUE_IF_FAIL);
            log.debug("... done starting HotkeyController");

            /* raising counter to 4  - splash screen dies now */
            splash.setDescribingText("Done! Now we bring up the dialwindow ...");

            log.debug("starting DialWindowController");
            bC.bringUp(new DialWindowController(settings, bC), BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
            log.debug("... done starting DialWindowController");
        }).start ();
	}
}

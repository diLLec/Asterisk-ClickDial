/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.boot;

import de.neue_phase.asterisk.ClickDial.constants.ServiceConstants;
import de.neue_phase.asterisk.ClickDial.controller.*;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.jobs.AutoConfigJob;
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
public class Bootstrap  implements Runnable {

	/**
	 * @param args
	 */
	
	/* the logging facility */
	private static final Logger    log 				= Logger.getLogger("Bootstrap");
	/* the global settings reader */
	private static final SettingsHolder  settings 	= SettingsHolder.getCurrent();
	/* the main SWT display */
	private static final Display	display 		= new Display ();

	/* the non-open primary shell, which will prevent the taskbar to show our windows */
	public static final Shell		primaryShell	= new Shell();

	/* the base controller */
	private static final BaseController bC			= new BaseController(display, settings);

	/* the splash screen */
	private static SplashScreen splash 				= null;

	public static final Rectangle priMonSize 		= display.getPrimaryMonitor().getBounds();
	
	public static void main(String[] args) {
		log.info(InterfaceConstants.myName + " starting up. SWT Version = " + SWT.getVersion() + " OS = " + System.getProperty("os.name"));
        java.util.logging.Logger.getGlobal ().setLevel (Level.ALL);

		splash = new SplashScreen (4);
		splash.open();
		
		/* start the controllers */
		log.debug("Scheduling the Boostrap ...");
		display.asyncExec(new Bootstrap());
		log.debug("Splash screen iteration started");
		splash.goIteration();
		
		/* we've been dropped out of the splash iteration - this means, that
		 * the splash is finished and we can go into base controller widget iteration now
		 */

		bC.iterate();
	}

	public void run() {

        /*
         instanciate the display event bus
         */
        EventBusFactory.instanciateDisplayEventBus(display);


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

		/**
		 * register eventbus
		 */
		EventBusFactory.getDisplayThreadEventBus ().register (bC);

		splash.setDescribingText("Connecting Service Interfaces ...");
		bC.initKeystore ();
		AsteriskManagerWebservice asWebservice = new AsteriskManagerWebservice (ServiceConstants.WebserviceURL);
		bC.bringUp (asWebservice, BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
        EventBusFactory.getSyncEventBus ().register (asWebservice);
        EventBusFactory.getThradPerTaskEventBus ().register (asWebservice);

		splash.setDescribingText("Starting AutoConfiguration via Asterisk Manager Webservice ...");
		bC.bringUp (new AutoConfigJob (new AutoConfig (settings, asWebservice)), BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
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


		
		/* raising counter to 4  - splash screen dies now */
		splash.setDescribingText("Done! Now we bring up the dialwindow ...");
		
		log.debug("starting DialWindowController");
		bC.bringUp(new DialWindowController(settings, bC), BaseController.BRINGUP_SHUTDOWN_IF_FAIL);
		log.debug("... done starting DialWindowController");
		
		return;
	}	
}

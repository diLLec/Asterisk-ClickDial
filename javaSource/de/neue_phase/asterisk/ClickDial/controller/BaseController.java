package de.neue_phase.asterisk.ClickDial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientServiceAuthenticationDataListener;
import de.neue_phase.asterisk.ClickDial.controller.listener.ServiceInterfaceProblemListener;
import de.neue_phase.asterisk.ClickDial.jobs.AutoConfigJob;
import de.neue_phase.asterisk.ClickDial.jobs.IJob;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerWebservice;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.IServiceInterface;
import de.neue_phase.asterisk.ClickDial.settings.*;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ISettingsExtractModel;
import de.neue_phase.asterisk.ClickDial.widgets.UserActionBox;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ServiceInterfaceTypes;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.JobTypes;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ServiceInterfaceProblems;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsTypes;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientSettingsListener;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.util.Dispatcher;

/**
 * The BaseController will startup every Controller and bailsOut if
 * the startup failed. Further more the SWT system event loop is done
 * in here.
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class BaseController implements InsufficientSettingsListener, InsufficientServiceAuthenticationDataListener, ServiceInterfaceProblemListener {

	public final static boolean BRINGUP_SHUTDOWN_IF_FAIL = true;
	public final static boolean BRINGUP_CONTINUE_IF_FAIL = false;


	private Display     	displayRef 				= null;
	private SettingsHolder 	settingsRef				= null;
	
	private final ControllerTypes type				= ControllerTypes.Base;
	protected final Logger log 						= Logger.getLogger(this.getClass());
	
	private final HashMap<ControllerTypes,ControllerInterface> controllerHash 		= new HashMap<ControllerTypes,ControllerInterface>();
	private final HashMap<ServiceInterfaceTypes,IServiceInterface> servicesHash 	= new HashMap<ServiceInterfaceTypes,IServiceInterface>();
	private final HashMap<JobTypes,IJob> jobsHash 									= new HashMap<JobTypes,IJob>();
	private final HashMap<JobTypes,Integer> jobCrashHash 							= new HashMap<JobTypes,Integer>();
	private final ArrayList<ControllerInterface> widgetController 					= new ArrayList<ControllerInterface>();

	private boolean jobWatchdogScheduled			= false;

	private Dispatcher dispatcherRef 				= null;
	private SettingsWebserviceKeystore keystore		= null;

	private IServiceInterface lastExtract			= null;

	public BaseController(Display displayRef, SettingsHolder settingsRef, Dispatcher dispatcherRef) {
		this.displayRef 	= displayRef;
		this.settingsRef 	= settingsRef;
		this.dispatcherRef	= dispatcherRef;
	}

	abstract class MyRunnable<T> implements Runnable {
		public T result;
	}

	public ISettingsExtractModel startSettingsProducer(ControllerTypes type) {

		MyRunnable <ISettingsExtractModel> runblock = new MyRunnable<ISettingsExtractModel> () {
			@Override
			public void run () {
				result = _startSettingsProducer (type);
			}
		};

		// make sure this one is executed by the display main thread
		displayRef.syncExec (runblock);
		return runblock.result;
	}

	public ISettingsExtractModel _startSettingsProducer(ControllerTypes type) {
		/* check from where the setting request is sent */
		log.debug("startSettingsProducer from " + type.toString());
		switch (type) {

		}
		lastExtract = null;
		return null;
	}


	public ISettingsExtractModel startSettingsProducer (ServiceInterfaceTypes type, Integer tryCount) {

		MyRunnable <ISettingsExtractModel> runblock = new MyRunnable<ISettingsExtractModel> () {
			@Override
			public void run () {
				result = _startSettingsProducer (type, tryCount);
			}
		};

		// make sure this one is executed by the display main thread
		displayRef.syncExec (runblock);
		return runblock.result;
	}


	public ISettingsExtractModel _startSettingsProducer(ServiceInterfaceTypes type, Integer tryCount) {
		/* check from where the setting request is sent */
		log.debug("startSettingsProducer from " + type.toString());
		switch (type) {
			case Webservice: {
				if (tryCount == 0 && keystore.hasWebserviceAuthData ())
					return keystore.getWebserviceAuthData();
				else {
					/* start the settings dialog */
					log.debug ("Starting the settings dialog, since the webservice connections lacks auth data");

					String message;
					if (tryCount == 0) {
						message = "Please enter Username/Password.";
					}
					else if (tryCount == 1 && keystore.hasWebserviceAuthData () && keystore.areCredentialsAcknowledged ())
						message = "Password Cache expired - Please renew Username/Password.";
					else
						message = "Username/Password mismatch. Please enter valid Credentials.";

					String[] authData = WebserviceAuthDialog.getAuthentication (message);

					if (authData != null && keystore.isWriteable ()) {
						keystore.setWebserviceAuthData (authData[0], authData[1]);
						return keystore.getWebserviceAuthData();
					} else
						return null;
				}
			}
			case AsteriskManagerInterface: {

				String re;
				if (tryCount == 0)
					return ((SettingsAsterisk) settingsRef.get(SettingsTypes.asterisk)).getAsteriskAuthData ();
				else {
					UserActionBox ua = new UserActionBox ("Authentication Data for Asterisk Manager Interface was incorrect.\n" +
														  "Connection will probably be restored in next AutoConfig run.",
														  InterfaceConstants.SettingsImages.error,
														  true);
					ua.addButton ("OK", 1);
					re = ua.open ();
					return null;
				}
			}
		}

		return null;
	}

	public Boolean handleServiceInterfaceContinueOrNot (ServiceInterfaceTypes type, ServiceInterfaceProblems problem, Integer tryCount) {
		MyRunnable <Boolean> runblock = new MyRunnable<Boolean> () {
			@Override
			public void run () {
				result = _handleServiceInterfaceContinueOrNot (type, problem, tryCount);
			}
		};

		// make sure this one is executed by the display main thread
		displayRef.syncExec (runblock);
		return runblock.result;
	}

	public Boolean _handleServiceInterfaceContinueOrNot (ServiceInterfaceTypes type, ServiceInterfaceProblems problem, Integer tryCount) {
		log.debug("handleServiceInterfaceContinueOrNot from " + type.toString() + " with problem " + problem.toString ());
		if (type == ServiceInterfaceTypes.Webservice) {
			switch (problem) {
				case ConnectionProblem:
					UserActionBox ua = new UserActionBox ("Asterisk Manager Webinterface Connection has failed.\n" +
														  "Closing Application",
														  InterfaceConstants.SettingsImages.error,
														  true);
					ua.addButton ("retry", 1);
					ua.addButton ("quit", 1);
					switch (ua.open ()) {
						case "retry": return true;
						case "quit": this.bailOut ();
					}
			}
		}

		if (type == ServiceInterfaceTypes.AsteriskManagerInterface) {
			switch (problem) {
				case ConnectionProblem:
					UserActionBox ua = new UserActionBox ("Asterisk Manager Interface Connection has disconnected/failed.\n"+
														  "Closing Application"	,
														  InterfaceConstants.SettingsImages.error,
														  true);
					ua.addButton ("ok", 1);
					ua.open ();
					return true;

			}
		}

		return false;
	}

	/**
	 * @return the type
	 */
	public ControllerTypes getType() {
		return type;
	}


	/**
	 *
	 * @param controller The controller to bring up and register
	 * @param needed define if the controller is a MUST-have in the init process - if true: whole application is shutdown if the controller does not init
	 */
	public void bringUp (ControllerInterface controller, boolean needed) {
		try {
			controller.registerInsufficientSettingsListener(this);
			controller.startUp();
			if (controller.isWidgetController())
				widgetController.add(controller);
		}
		catch (InitException e)
		{
			log.error("Failed to bring up "+controller.getName());
			if (needed) {
				log.error("closing down, since this Controller is needed!");				
				bailOut();
			}
		}
		
		if (! controllerHash.containsKey(controller.getName())) {
			log.error("registered the '"+controller.getName()+"' controller");
			controllerHash.put(controller.getName(), controller);
		}
		else
			log.error("Warning! The controller type '"+controller.getName()+" got doubled!");

	}


	/**
	 *
	 * @param service The serviceInterface to bring up and register
	 * @param needed define if the serviceInterface is a MUST-have in the init process - if true: whole application is shutdown if the serviceInterface does not init
	 */
	public void bringUp (IServiceInterface service, boolean needed) {
		try {
			service.setInsufficientServiceAuthenticationDataListener (this);
			service.setServiceInterfaceProblemListener (this);
			service.startUp();
		}
		catch (InitException e)
		{
			log.error("Failed to bring up "+service.getName());
			if (needed) {
				log.error("closing down, since this Service is needed!");
				bailOut();
			}
		}

		if (! servicesHash.containsKey(service.getName())) {
			log.error("registered the '"+service.getName()+"' ServiceInterface");
			servicesHash.put(service.getName(), service);
		}
		else
			log.error("Warning! The ServiceInterface type '"+service.getName()+" got doubled!");

	}

	/**
	 *
	 * @param job The Job to bring up and register
	 * @param needed define if the serviceInterface is a MUST-have in the init process - if true: whole application is shutdown if the serviceInterface does not init
	 */
	public void bringUp (IJob job, boolean needed) {
		try {
			job.startUp();
		}
		catch (InitException e)
		{
			log.error("Failed to bring up "+job.getName());
			if (needed) {
				log.error("closing down, since this Service is needed!");
				bailOut();
			}
		}

		if (! jobsHash.containsKey(job.getName())) {
			log.error("registered the '"+job.getName()+"' Job");
			jobsHash.put(job.getName(), job);
		}
		else
			log.error("Warning! The Job type '"+job.getName()+" got doubled!");

	}

	public void raiseJobCrashCount (JobTypes type) {
		if (jobCrashHash.containsKey (type))
			jobCrashHash.put(type, jobCrashHash.get(type) + 1);
		else
			jobCrashHash.put(type, 1);
	}

	public Integer getJobCrashCount (JobTypes type) {
		if (jobCrashHash.containsKey (type))
			return jobCrashHash.get(type);
		else
			return 0;
	}

	public void checkAndRestartJobs () {
		log.trace ("Job Watchdog Running");
		for(Entry<JobTypes, IJob> entry : jobsHash.entrySet()) {
			JobTypes type = entry.getKey ();
			IJob job = entry.getValue ();

			if (! job.isAlive ()) {
				if (getJobCrashCount(type) < ControllerConstants.MaxJobRestartCount) {
					log.error ("Job '" + job.getName () + "' crashed and needs to be restarted.");
					this.raiseJobCrashCount (entry.getKey ());

					jobsHash.remove (type);
					this.bringUp (new AutoConfigJob (new AutoConfig (this.settingsRef,
																	 (AsteriskManagerWebservice) this.getServiceInterface (ServiceInterfaceTypes.Webservice),
																	 dispatcherRef)),
								  BaseController.BRINGUP_CONTINUE_IF_FAIL);
				}
				else {
					log.error ("Job '" + job.getName () + "' crashed too many times and now gets removed.");
					jobsHash.remove (type);
				}
			}
		}

		this.jobWatchdogScheduled = false;
		scheduleJobWatchdog();
	}

	public void scheduleJobWatchdog () {

		if (jobWatchdogScheduled) {
			log.error ("Tried to schedule JobWatchdog multiple times - skipped.");
			return;
		}

		displayRef.timerExec (ControllerConstants.JobWatchdogInterval, new Runnable () {
			@Override
			public void run () {
				checkAndRestartJobs ();
			}
		});

		this.jobWatchdogScheduled = true;
	}

	public void iterate () {
		while (!displayRef.isDisposed()) {
			if (!displayRef.readAndDispatch ()) {
				displayRef.sleep ();
			}
			dispatcherRef.dispatch();
		}
	}
	
	public ControllerInterface getController (ControllerTypes type) {
		return controllerHash.get(type);
	}
	public IServiceInterface getServiceInterface (ServiceInterfaceTypes type) {
		return servicesHash.get(type);
	}
	public IJob getJob (JobTypes type) {
		return jobsHash.get(type);
	}
	
	/**
	 * close down all threads and go back to bootstrap
	 */
	public void bailOut() {
		/* close settings */
		// nothing to close for settings, maybe later if we collect statistic data

		/* close controllers */
		try {

			log.debug("Closing Jobs ... ");
			for (Entry<JobTypes,IJob> jobEntry : jobsHash.entrySet ())
				jobEntry.getValue ().shutdown ();


			log.debug("Closing ServiceInterfaces ... ");
			for (Entry<ServiceInterfaceTypes,IServiceInterface> serviceInterfaceEntry : servicesHash.entrySet ())
				serviceInterfaceEntry.getValue ().shutdown ();

			log.debug("Closing controllers ... ");
			for (Entry<ControllerTypes,ControllerInterface> controllerEntry : controllerHash.entrySet ())
				controllerEntry.getValue ().closeDown ();

			log.debug("Closing Jobs > ServiceInterfaces > Controllers done!");
		}
		catch (Exception e) {
			log.error("Warning: we had Exceptions while closing the controllers! But now watch the StackTrace ...", e);
		}
		
		System.runFinalization ();
		System.gc();
		System.exit (0);
	}

	public void initKeystore () {
		try {
			keystore = new SettingsWebserviceKeystore ();
		} catch (InitException e) {
			log.error ("Keystore problem - will not cache Webservice Authentication data.");
		}
	}

	@Override
	public void acknowledgeLoginData () {
		if (this.keystore.isWriteable ())
			this.keystore.acknowledgeCredentials ();
	}
}

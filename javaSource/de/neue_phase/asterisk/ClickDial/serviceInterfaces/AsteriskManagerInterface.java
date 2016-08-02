package de.neue_phase.asterisk.ClickDial.serviceInterfaces;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.constants.ServiceConstants;
import de.neue_phase.asterisk.ClickDial.controller.BaseController;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.ManagerProblemEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.ManagerProblemResolveEvent;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractAsteriskManagerInterfaceAuthData;
import de.neue_phase.asterisk.ClickDial.eventbus.events.SettingsUpdatedEvent;
import org.apache.log4j.Logger;

import org.asteriskjava.manager.*;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsTypes;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import org.asteriskjava.manager.action.PingAction;
import org.asteriskjava.manager.response.PingResponse;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Asterisk Connection controller oversees the managed asterisk connection
 * and is used to communicate with it
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class AsteriskManagerInterface extends TimerTask implements IServiceInterface {
	private AtomicBoolean problemTriggered					= new AtomicBoolean (false);
	private ControllerConstants.ServiceInterfaceTypes type 	= ControllerConstants.ServiceInterfaceTypes.AsteriskManagerInterface;
	private ManagerConnection mcon 							= null;
	private Integer authenticationTry 						= 0;
	private Integer problemTry		 						= 0;
	private Timer timer										= null;
	private Integer checkDisconnectedCount 					= 0;
	private Integer checkConnectedCount	 					= 0;
	private final Logger log 								= Logger.getLogger(this.getClass());

	public AsteriskManagerInterface () {
        EventBusFactory.getThreadPerTaskEventBus ().register (this);
	}

	/**
	 * trigger events to gather settings
	 */
	private void reconnect () {
        log.debug ("Asterisk Manager Connection reconnecting.");

		ExtractAsteriskManagerInterfaceAuthData authData = BaseController.getInstance ().getManagerAuthData ();

		if (authData == null) {
			log.error ("No authentication data for manager connection found - aborting reconnect.");
			return;
		}

		this.createConnection (authData.getConnectData ().getHostname (),
							   authData.getConnectData ().getPort (),
							   authData.getUser (),
							   authData.getPassword (),
							   authData.getTimeout ());
	}


	/**
	 * If the webservice
	 * @param problemType
	 */
	private void triggerServiceInterfaceProblem(ControllerConstants.ServiceInterfaceProblems problemType) {
        log.error ("Manager Connection Problem detected.");
        EventBusFactory.getDisplayThreadEventBus ().post (new ManagerProblemEvent (this.type, problemType));
	}

	/**
	 * get the type of this ServiceInterface
	 * @return the type
	 */
	@Override
	public ControllerConstants.ServiceInterfaceTypes getName () {
		return type;
	}

	/**
	 * @param event Event when settings got updated to recreate the connection
	 */
    @Subscribe public void handleSettingsUpdatedEvent (SettingsUpdatedEvent event) {
		if (event.getUpdatedTypes ().contains (SettingsTypes.asterisk)) {
			this.closeDown ();
			this.reconnect ();
		}
	}

    /**
     * check connection each 2 seconds
     */
    @Override
    public void run () {
		log.debug (String.format ("ManagerInterface: mcon state = '%s' | checkDisconnectedCount = '%d'",
								  mcon.getState ().toString (), checkDisconnectedCount));
		if (mcon.getState () == ManagerConnectionState.DISCONNECTED) {
			checkDisconnectedCount += 1;

			if (checkDisconnectedCount % 10 == 0) {
				problemTriggered.set (true);
				reconnect ();
			}
		}

		else if (mcon.getState () == ManagerConnectionState.RECONNECTING) {
			if (!problemTriggered.get ()) {
				problemTriggered.set (true);
				triggerServiceInterfaceProblem (ControllerConstants.ServiceInterfaceProblems.ConnectionProblem);
			}
		}

		else if (mcon.getState () == ManagerConnectionState.CONNECTED) {
			checkDisconnectedCount = 0;

			if (problemTriggered.get ()) {
				problemTriggered.set (false);
				EventBusFactory.getDisplayThreadEventBus ().post (new ManagerProblemResolveEvent ());
			}

			if (checkConnectedCount++ % ServiceConstants.AsteriskManagerPingInterval == 0) {
				log.debug ("Sending ping check to asterisk manager interface");
				if (! this.ping ()) { // if the ping check returns with false, we logoff the manager connection and reconnect the next run
					this.mcon.logoff ();
					log.error (String.format ("ping check returned error - logoff the manager connection and reconnect (mcon state = %s)",
											  mcon.getState ().toString ()));

				}
				checkConnectedCount = 0;
			}
		}


    }

	/**
	 * startUp Routine of AsteriskConnectionController
	 * @throws InitException if there is something wrong with the connection
	 */
	@Override 
	public void startUp () throws InitException {
		log.debug ("AsteriskManagerInterface startup()");
        this.reconnect ();

        // start the checker thread
		this.timer = new Timer ();
		this.timer.scheduleAtFixedRate (this, 0, ServiceConstants.AsteriskManagerConnectionCheckInterval);

		if (this.mcon.getState () != ManagerConnectionState.CONNECTED) {
            triggerServiceInterfaceProblem (ControllerConstants.ServiceInterfaceProblems.ConnectionProblem);
            throw new InitException ("AsteriskManagerWebservice could not be established");
        }

	}

	@Override
	public synchronized void shutdown () {
        this.timer.cancel ();

		try {
			this.mcon.logoff ();
		} catch (Exception e) {
			log.error ("Manager Connection Logoff failed.", e);
		}
	}

	/**
	 * close every resource
	 */
	public synchronized void closeDown () {
		this.shutdown ();
	}

	/**
	 *
	 * @param hostname Hostname to Connect
	 * @param port Port (TCP)
	 * @param user Username to use for the connect
	 * @param password Passwort to use for the connect
	 * @param timeout Time to wait on packets to be answered
	 */
	private void createConnection (String hostname, Integer port, String user, String password, Integer timeout) {
        log.debug ("Creating connection to "+ hostname);
		ManagerConnectionFactory mconfact = new ManagerConnectionFactory(hostname,
																		 port,
																		 user,
																		 password);

		mcon 	 = mconfact.createManagerConnection();
		mcon.setSocketTimeout (timeout);
		try {
			mcon.login();
			log.debug("Manager Connection established");
		}
		catch (AuthenticationFailedException e) {
            log.debug("Manager Connection authentication failed");
		}
		catch (Exception e) {
			log.error ("Manager Connection could not be established for generic reasons:", e);
		}
	}

	/** 
	 * give out the current ManagerState (DialWindow RightClick menu)
	 * @return the current connection state
	 */
	public ManagerConnectionState getState () {
		return mcon.getState(); 
	}
	
	/**
	 * give out the used Username 
	 * @return the username used to connect
	 */
	public String getUsedUsername () {
		return mcon.getUsername();
	}

	/**
	 * send a ping over the asterisk manager protocol to check if the connection
	 * is alive
	 * @return true = alive | false = dead
     */
	private synchronized boolean ping () {
		try {
			PingResponse ping = (PingResponse) this.mcon.sendAction (new PingAction (), ServiceConstants.AsteriskManagerPingTimeout);
			log.debug (String.format ("Ping action returned: %s", ping.getPing ()));
		}
		catch (TimeoutException e) {
			log.error ("Last ping check timed out. ", e);
			return false;
		}
		catch (IOException e) {
			log.error ("Socket not ready to send ping action", e);
			return false;
		}

		return true;
	}

}

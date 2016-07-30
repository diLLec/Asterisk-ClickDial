package de.neue_phase.asterisk.ClickDial.serviceInterfaces;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.ManagerInsufficientAuthDataEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.ManagerProblemEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.ManagerProblemResolveEvent;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractAsteriskManagerInterfaceAuthData;
import de.neue_phase.asterisk.ClickDial.eventbus.events.SettingsUpdatedEvent;
import org.apache.http.auth.AUTH;
import org.apache.log4j.Logger;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerConnectionState;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsTypes;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Asterisk Connection controller oversees the managed asterisk connection
 * and is used to communicate with it
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class AsteriskManagerInterface implements IServiceInterface, Runnable {

	private  enum ConnectionState {
		NOT_CONNECTED,
		CONNECTED_AUTH_FAIL,
		AUTHENTICATED
	}

    protected Thread        interfaceCheckThread            = null;
	protected AtomicBoolean problemTriggered				= new AtomicBoolean (false);
	protected AtomicBoolean shutdownInterface				= new AtomicBoolean (false);
	private volatile ConnectionState	internalState		= ConnectionState.NOT_CONNECTED;
	private ControllerConstants.ServiceInterfaceTypes type 	= ControllerConstants.ServiceInterfaceTypes.AsteriskManagerInterface;
	private ManagerConnection 			mcon 				= null;
	private ManagerConnectionFactory 	mconfact 			= null;
	private final Logger log 								= Logger.getLogger(this.getClass());
	private Integer authenticationTry 						= 0;
	private Integer problemTry		 						= 0;

	public AsteriskManagerInterface () {
        EventBusFactory.getThradPerTaskEventBus ().register (this);
	}

	/**
	 * trigger events to gather settings
	 */
	private void reconnect () {
        log.debug ("Asterisk Manager Connection reconnecting.");

		ManagerInsufficientAuthDataEvent event = new ManagerInsufficientAuthDataEvent (this.type,
																					   authenticationTry);
        EventBusFactory.getDisplayThreadEventBus ().post (event);
		ExtractAsteriskManagerInterfaceAuthData authData = event.getReponse (3000);

		if (authData == null)
            return;

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
        EventBusFactory.getDisplayThreadEventBus ().post (new ManagerProblemEvent (this.type,
                                                                                   problemType,
                                                                                   problemTry++)
        );
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

        Integer discoStateIntervals = 0;

        do {
            log.debug ("ManagerInterface: internaleState = " + internalState.toString () + " mcon state = " + mcon.getState ().toString () + " discoIntervals " + discoStateIntervals);
            if (mcon.getState () == ManagerConnectionState.DISCONNECTED) {
                discoStateIntervals += 1;

                if (discoStateIntervals % 10 == 0) {
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
                if (problemTriggered.get ()) {
                    problemTriggered.set (false);
                    discoStateIntervals = 0;
                    internalState = ConnectionState.AUTHENTICATED;
                    EventBusFactory.getDisplayThreadEventBus ().post (new ManagerProblemResolveEvent ());
                }
            }

            try {
                TimeUnit.SECONDS.sleep (2);
            } catch (InterruptedException e) {}
        } while (!shutdownInterface.get ());
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
        interfaceCheckThread = new Thread(this);
        interfaceCheckThread.start ();

		if (this.mcon.getState () != ManagerConnectionState.CONNECTED) {
            triggerServiceInterfaceProblem (ControllerConstants.ServiceInterfaceProblems.ConnectionProblem);
            throw new InitException ("AsteriskManagerWebservice could not be established");
        }

	}

	@Override
	public synchronized void shutdown () {
		this.shutdownInterface.set (true);
        interfaceCheckThread.interrupt ();

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
		mconfact = new ManagerConnectionFactory(hostname,
												port,
												user,
												password);

		mcon 	 = mconfact.createManagerConnection();
		mcon.setSocketTimeout (timeout);
		try {
			mcon.login();
			log.debug("Manager Connection established");
			problemTriggered.set (false);
		}
		catch (AuthenticationFailedException e) {
            log.debug("Manager Connection authentication failed");
			this.internalState = ConnectionState.CONNECTED_AUTH_FAIL;
		}
		catch (Exception e) {
			log.debug("Manager Connection could not be established", e);
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


}

package de.neue_phase.asterisk.ClickDial.serviceInterfaces;

import java.io.IOException;
import java.util.ArrayList;

import de.neue_phase.asterisk.ClickDial.controller.CallWindowController;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientServiceAuthenticationDataListener;
import de.neue_phase.asterisk.ClickDial.controller.listener.ServiceInterfaceProblemListener;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractAsteriskManagerInterfaceAuthData;
import de.neue_phase.asterisk.ClickDial.util.Dispatcher;
import de.neue_phase.asterisk.ClickDial.util.events.ClickDialEvent;
import de.neue_phase.asterisk.ClickDial.util.events.SettingsUpdatedEvent;
import de.neue_phase.asterisk.ClickDial.util.listener.SettingsUpdatedListener;
import org.apache.log4j.Logger;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants.SettingsImages;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsTypes;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.widgets.UserActionBox;

/**
 * Asterisk Connection controller oversees the managed asterisk connection
 * and is used to communicate with it
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class AsteriskManagerInterface implements IServiceInterface, SettingsUpdatedListener {

	private enum ConnectionState {
		NOT_CONNECTED,
		CONNECTED,
		AUTHENTICATED
	}

	private ConnectionState	authState						= ConnectionState.NOT_CONNECTED;
	private ControllerConstants.ServiceInterfaceTypes type = ControllerConstants.ServiceInterfaceTypes.AsteriskManagerInterface;
	private ManagerConnection 			mcon 				= null;
	private ManagerConnectionFactory 	mconfact 			= null;
	private final Logger log 								= Logger.getLogger(this.getClass());
	private Dispatcher dispatcher							= null;
	private Integer authenticationTry 						= 0;
	private Integer problemTry		 						= 0;
	protected InsufficientServiceAuthenticationDataListener insufficentAuthDatalistener = null;
	protected ServiceInterfaceProblemListener problemListener = null;

	public AsteriskManagerInterface (Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
		this.dispatcher.addEventListener (ClickDialEvent.Type.ClickDial_SettingsUpdatedEvent, this);
	}

	/**
	 *
	 * @param listener The listener we should inform when the webservice misses auth data
	 */
	@Override
	public void setInsufficientServiceAuthenticationDataListener (InsufficientServiceAuthenticationDataListener listener) {
		this.insufficentAuthDatalistener = listener;
	}

	@Override
	public void setServiceInterfaceProblemListener (ServiceInterfaceProblemListener listener) {
		this.problemListener = listener;
	}

	/**
	 * trigger all listeners for settings
	 */
	private void triggerInsufficientServiceAuthenticationDataListeners () {
		ExtractAsteriskManagerInterfaceAuthData authData = null;

		do {
			if ((authData = (ExtractAsteriskManagerInterfaceAuthData) insufficentAuthDatalistener.startSettingsProducer (this.type,
																														 authenticationTry)) != null) {
				this.createConnection (authData.getConnectData ().getHostname (),
									   authData.getConnectData ().getPort (),
									   authData.getUser (),
									   authData.getPassword (),
									   authData.getTimeout ());

				if (this.authState == ConnectionState.AUTHENTICATED)
					this.authenticationTry = 0;

				else if (this.authState == ConnectionState.NOT_CONNECTED) {
					this.triggerServiceInterfaceProblemListener (ControllerConstants.ServiceInterfaceProblems.ConnectionProblem); // this just informs the user that there is a problem
					authenticationTry += 1;
				}
				else if (this.authState == ConnectionState.CONNECTED)
					authenticationTry += 1; // next try

			}
		} while (authenticationTry < 2 && this.authState != ConnectionState.AUTHENTICATED);

	}

	private void triggerServiceInterfaceProblemListener(ControllerConstants.ServiceInterfaceProblems problemType) {
		this.problemListener.handleServiceInterfaceContinueOrNot (this.type, problemType, problemTry++);
	}

	@Override
	public ControllerConstants.ServiceInterfaceTypes getName () {
		return type;
	}

	@Override
	public void handleSettingsUpdatedEvent (SettingsUpdatedEvent event) {
		if (event.getUpdatedTypes ().contains (SettingsTypes.asterisk))
			this.triggerInsufficientServiceAuthenticationDataListeners ();
	}

	/**
	 * startUp Routine of AsteriskConnectionController
	 * @throws InitException if there is something wrong with the connection
	 */
	@Override 
	public void startUp () throws InitException {
		log.debug ("AsteriskManagerInterface startup()");
		this.triggerInsufficientServiceAuthenticationDataListeners ();

		if (this.mcon.getState () != ManagerConnectionState.CONNECTED)
			throw new InitException ("AsteriskManagerWebservice could not authenticate with the given credentials");
	}

	@Override
	public void shutdown () {
		try {
			this.mcon.logoff ();
		} catch (Exception e) {
			log.error ("Manager Connection Logoff failed.", e);
		}
	}

	/**
	 *
	 * @param hostname
	 * @param port
	 * @param user
	 * @param password
	 * @param timeout
	 * @return
	 */
	private void createConnection (String hostname, Integer port, String user, String password, Integer timeout) {
		mconfact = new ManagerConnectionFactory(hostname,
												port,
												user,
												password);

		mcon 	 = mconfact.createManagerConnection();
		mcon.setSocketTimeout (timeout);

		try {
			mcon.login();
			log.debug("Manager Connection established");
			this.authState = ConnectionState.AUTHENTICATED;
		}
		catch (AuthenticationFailedException e) {
			this.authState = ConnectionState.CONNECTED;
		}
		catch (Exception e) {
			log.debug("Manager Connection could not be established", e);
			this.authState = ConnectionState.NOT_CONNECTED;
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
	 * close every resource
	 */
	public void closeDown () {
		if (getState() == ManagerConnectionState.CONNECTED)
			mcon.logoff();

		mcon	 = null;
		mconfact = null;
		
	}

}

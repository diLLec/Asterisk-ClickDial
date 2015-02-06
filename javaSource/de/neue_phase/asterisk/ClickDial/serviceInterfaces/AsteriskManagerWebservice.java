package de.neue_phase.asterisk.ClickDial.serviceInterfaces;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientServiceAuthenticationDataListener;
import de.neue_phase.asterisk.ClickDial.controller.listener.ServiceInterfaceProblemListener;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractAsteriskManagerWebinterfaceAuthData;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AsteriskManagerWebservice implements IServiceInterface {

    private enum ConnectionState {
        NOT_CONNECTED, // no connection possible
        CONNECTED,     // connected but not authenticated
        AUTHENTICATED, // connected _and_ authenticated

    }

    private String serviceURL;
    private ConnectionState conState = ConnectionState.NOT_CONNECTED;
    private Boolean isAuthenticated = false;
    private Integer authenticationTry = 0;
    private Integer problemTry = 0;
    private ControllerConstants.ServiceInterfaceTypes type = ControllerConstants.ServiceInterfaceTypes.Webservice;

    protected final Logger log = Logger.getLogger(this.getClass());
    protected InsufficientServiceAuthenticationDataListener insufficentAuthDatalistener = null;
    protected ServiceInterfaceProblemListener problemListener = null;

    /**
     *
     * @param serviceURL The URL where the service can be found
     */
    public AsteriskManagerWebservice (String serviceURL) {
        this.serviceURL = serviceURL;
    }

    @Override
    public ControllerConstants.ServiceInterfaceTypes getName () {
        return this.type;
    }

    @Override
    public void startUp () throws InitException {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
        if (!urlValidator.isValid(this.serviceURL))
            throw new InitException ("Invalid service URL specified.");

        this.triggerInsufficientWebserviceAuthenticationDataListeners ();
        if (!this.isAuthenticated ())
            throw new InitException ("AsteriskManagerWebservice could not authenticate with the given credentials");
    }

    @Override
    public void shutdown () {
        // nothing to shutdown here
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
    private void triggerInsufficientWebserviceAuthenticationDataListeners () {
        ExtractAsteriskManagerWebinterfaceAuthData authData;

        do {
            if ((authData = (ExtractAsteriskManagerWebinterfaceAuthData) insufficentAuthDatalistener.startSettingsProducer (this.type,
                                                                                                                            authenticationTry)) != null) {
                this.authenticate (authData.getUsername (), authData.getPassword ());
                if (this.conState == ConnectionState.AUTHENTICATED) {
                    insufficentAuthDatalistener.acknowledgeLoginData ();
                    this.authenticationTry = 0;
                }

            }
            authenticationTry += 1;
        } while (!this.isAuthenticated () && authenticationTry <= 10);
    }

    private Boolean triggerServiceInterfaceProblemListener (ControllerConstants.ServiceInterfaceProblems problemType) {
        return this.problemListener.handleServiceInterfaceContinueOrNot (this.type, problemType, problemTry++);
    }

    private HttpResponse<JsonNode> doJsonRequest (HttpRequest request) {
        Boolean retry = false;

        do {
            try {
                HttpResponse<JsonNode> jsonResponse = request.asJson ();

                if (jsonResponse.getStatus () == 401) {
                    this.conState = ConnectionState.CONNECTED;
                    triggerInsufficientWebserviceAuthenticationDataListeners ();
                }

                return jsonResponse;
            } catch (UnirestException ex) {
                this.conState = ConnectionState.NOT_CONNECTED;
                log.error ("Could not connect to Asterisk Manager Webservice on '" + this.serviceURL + "'");
                if (triggerServiceInterfaceProblemListener (ControllerConstants.ServiceInterfaceProblems.ConnectionProblem))
                    retry = true;

            }
        } while (retry);

        return null;
    }

    /**
     *
     * @return Boolean
     */
    public Boolean testConnect () {
        HttpResponse<JsonNode> jsonResponse = this.doJsonRequest (Unirest.get (this.serviceURL + "/ping")
                                                                         .header ("accept", "application/json"));
        JSONObject body = jsonResponse.getBody ().getObject ();
        String status = (String) body.get ("responseStatus");
        if (status.equals ("OK")) {
            log.info ("Test Connect successful: " + body.toString ());
            if (conState == ConnectionState.NOT_CONNECTED)
                conState = ConnectionState.CONNECTED;
            return true;
        }
        else {
            log.info ("Test Connect failed: " + body.toString ());
            this.conState = ConnectionState.NOT_CONNECTED;
            return false;
        }

    }

    /**
     * Authenticate to the webservice and get the session cookie 'seid'
     * @param wsUser
     * @param wsPassword
     * @return
     */
    public ConnectionState authenticate (String wsUser, String wsPassword) {
        JSONObject authenticationData = new JSONObject ();
        authenticationData.put ("username", wsUser);
        authenticationData.put ("password", wsPassword);

        HttpResponse<JsonNode> jsonResponse = this.doJsonRequest (Unirest.post (this.serviceURL + "/authenticate")
                                                                         .header ("accept", "application/json")
                                                                         .header ("Content-Type", "application/json; charset=UTF-8")
                                                                         .body (new JsonNode (authenticationData.toString ()))
                                                                         .getHttpRequest ());
        JSONObject body = jsonResponse.getBody().getObject ();
        String status = (String) body.get("authenticationStatus");
        if (status.equals ("OK")) {
            log.info ("Authentication successful: " + body.toString ());
            return (this.conState = ConnectionState.AUTHENTICATED);
        }
        else {
            log.info ("Test Connect failed: " + body.toString ());
            return (this.conState = ConnectionState.CONNECTED);
        }
    }

    /**
     * Does this Webservice Object has an authenticated HTTP REST session?
     * @return TRUE = authenticated session | FALSE = not authenticated
     */
    public Boolean isAuthenticated () { return (this.conState == ConnectionState.AUTHENTICATED); }


    /**
     * Get AutoConfig data from webservice
     * @return JSONObject or null
     */
    public Map<String, String> getAutoConfigurationData () {

        HttpResponse<JsonNode> jsonResponse = doJsonRequest (Unirest.get (this.serviceURL + "/autoConfig")
                                                                     .header ("accept", "application/json")
                                                                     .header ("Content-Type", "application/json; charset=UTF-8"));
        JSONObject body = jsonResponse.getBody ().getObject ();
        String status = (String) body.get ("responseStatus");
        if (status.equals ("OK")) {
            log.info ("AutoConfig data received successful: " + body.toString ());
            HashMap<String, String> autoConfigData = new HashMap<> ();
            autoConfigData.put ("asterisk_hostname", (String) body.get ("asterisk_hostname"));
            autoConfigData.put ("asterisk_port", (String) body.get ("asterisk_port"));
            autoConfigData.put ("asterisk_user", (String) body.get ("asterisk_user"));
            autoConfigData.put ("asterisk_pass", (String) body.get ("asterisk_pass"));
            autoConfigData.put ("asterisk_channel", (String) body.get ("asterisk_channel"));
            autoConfigData.put ("asterisk_timeout", body.get ("asterisk_timeout").toString ());
            autoConfigData.put ("asterisk_callerid", (String) body.get ("asterisk_callerid"));
            autoConfigData.put ("webservice_version", (String) body.get ("webservice_version"));

            return autoConfigData;
        } else {
            log.info ("Failed to get AutoConfig data " + body.toString ());
            return null;
        }
    }
}

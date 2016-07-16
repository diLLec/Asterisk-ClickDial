package de.neue_phase.asterisk.ClickDial.serviceInterfaces;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ServiceInterfaceProblems;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.controller.util.AsteriskCallerId;
import de.neue_phase.asterisk.ClickDial.datasource.Contact;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.*;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractWebserviceAuthData;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AsteriskManagerWebservice implements IServiceInterface, Runnable {

    private enum ConnectionState {
        NOT_CONNECTED, // no connection possible
        CONNECTED,     // connected but not authenticated
        AUTHENTICATED, // connected _and_ authenticated
    }

    protected Thread interfaceCheckThread       = null;
    protected AtomicBoolean problemTriggered	= new AtomicBoolean (false);
    protected AtomicBoolean shutdownInterface	= new AtomicBoolean (false);
    private AtomicReference<ConnectionState> conState = new AtomicReference<ConnectionState> ();

    private String serviceURL;

    private Integer authenticationTry   = 0;
    private Integer problemTry          = 0;
    private ControllerConstants.ServiceInterfaceTypes type = ControllerConstants.ServiceInterfaceTypes.Webservice;
    private AsyncEventBus displayEventBus				   = EventBusFactory.getDisplayThreadEventBus ();

    protected final Logger log = Logger.getLogger(this.getClass());

    /**
     *
     * @param serviceURL The URL where the service can be found
     */
    public AsteriskManagerWebservice (String serviceURL) {
        this.serviceURL = serviceURL;
    }

    /**
     * @return the controller type
     */
    @Override
    public ControllerConstants.ServiceInterfaceTypes getName () {
        return this.type;
    }

    /**
     * check connection each 2 seconds
     */
    @Override
    public void run () {
        do {
            if (conState.get () == ConnectionState.CONNECTED) {
                problemTriggered.set (true);
                reAuthenticate (); // blocks
            }
            else if (conState.get() == ConnectionState.NOT_CONNECTED) {
                problemTriggered.set(true);
                triggerServiceInterfaceProblem (ServiceInterfaceProblems.ConnectionProblem); // blocks
            }
            try {
                TimeUnit.SECONDS.sleep (2);
            } catch (InterruptedException e) { log.debug ("Interrupted sleep.");}
        } while (!shutdownInterface.get ());
    }

    /**
     * Start the ServiceInterface up
     * @throws InitException
     */
    @Override
    public void startUp () throws InitException {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http","https"});
        if (!urlValidator.isValid(this.serviceURL))
            throw new InitException ("Invalid service URL specified.");

        this.reAuthenticate ();
        if (!this.isAuthenticated ())
            throw new InitException ("AsteriskManagerWebservice could not authenticate with the given credentials.");

        interfaceCheckThread = new Thread(this);
        interfaceCheckThread.start ();
    }

    @Override
    public void shutdown () {
        // nothing to shutdown here
        interfaceCheckThread.interrupt ();
        this.shutdownInterface.set (true);
    }


    /**
     * trigger event(s) that will announce that this component needs
     * new authentication data and try to contact webservice for authorization
     */
    private void reAuthenticate () {
        do {
            WebserviceInsufficientAuthDataEvent event = new WebserviceInsufficientAuthDataEvent (this.type,
                                                                                                 authenticationTry);
            displayEventBus.post (event);
            ExtractWebserviceAuthData authData = event.getReponse (3000);

            if (authData != null) {
                this.authenticate (authData.getUsername (), authData.getPassword ());
                if (conState.get () == ConnectionState.AUTHENTICATED) {
                    displayEventBus.post (new ServiceAcknowledgeAuthDataEvent ());
                    this.authenticationTry = 0;
                }

            }
            authenticationTry += 1;
        } while (!this.isAuthenticated () && authenticationTry <= 10);
    }

    /**
     * trigger event (s) that will announce that this component has a problem
     * @param problemType the common type of the problem
     * @return problem solved (true), or not (false)
     */
    private Boolean triggerServiceInterfaceProblem (ControllerConstants.ServiceInterfaceProblems problemType) {
        WebserviceProblemEvent event = new WebserviceProblemEvent (this.type, problemType, problemTry++);
        displayEventBus.post (event);
        return event.getReponse (3000);
    }

    /**
     * encapsulate the real request, since we need to set internal data
     * when errors occur
     *
     * @param request The request to put
     * @return json response or null on error
     */
    private HttpResponse<JsonNode> doJsonRequest (HttpRequest request) {
        Boolean retry = false;

        do {
            try {
                HttpResponse<JsonNode> jsonResponse = request.asJson ();

                if (jsonResponse.getStatus () == 401) {
                    conState.set (ConnectionState.CONNECTED);
                    return null;
                } else
                    return jsonResponse;

            } catch (Exception ex) {
                conState.set (ConnectionState.NOT_CONNECTED);
                log.error ("Could not connect to Asterisk Manager Webservice on '" + this.serviceURL + "'");
                retry = triggerServiceInterfaceProblem (ControllerConstants.ServiceInterfaceProblems.ConnectionProblem);
            }
        } while (retry);

        return null;
    }

    /**
     *
     * @return Boolean
     */
    public ConnectionState testConnect () {
        HttpResponse<JsonNode> jsonResponse = this.doJsonRequest (Unirest.get (this.serviceURL + "/ping")
                                                                         .header ("accept", "application/json"));
        if (jsonResponse == null) {
            try {
                JSONObject body = jsonResponse.getBody ().getObject ();
                String status = (String) body.get ("responseStatus");

                if (status.equals ("OK")) {
                    log.info ("Test Connect successful: " + body.toString ());
                    conState.set (ConnectionState.AUTHENTICATED);
                }
            } catch (JSONException ex) {
                log.error ("Bogus JSON Response on testConnect.", ex);
            }
        }

        return conState.get();

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
        if (jsonResponse != null) {
            try {
                JSONObject body = jsonResponse.getBody ().getObject ();
                String status = (String) body.get ("authenticationStatus");
                if (status.equals ("OK")) {
                    log.info ("Authentication successful: " + body.toString ());
                    return conState.getAndSet (ConnectionState.AUTHENTICATED);
                }
            } catch (JSONException ex) {
                log.error ("Bogus JSON Response on authenticate.", ex);
            }
        }

        return conState.get ();
    }

    /**
     * Does this Webservice Object has an authenticated HTTP REST session?
     * @return TRUE = authenticated session | FALSE = not authenticated
     */
    public Boolean isAuthenticated () { return (this.conState.get() == ConnectionState.AUTHENTICATED); }


    /**
     * Get AutoConfig data from webservice
     * @return JSONObject or null
     */
    public Map<String, String> getAutoConfigurationData () {

        HttpResponse<JsonNode> jsonResponse = doJsonRequest (Unirest.get (this.serviceURL + "/autoConfig")
                                                                     .header ("accept", "application/json")
                                                                     .header ("Content-Type", "application/json; charset=UTF-8"));
        if (jsonResponse != null) {
            try {
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
                } else
                    log.info ("Failed to get AutoConfig data " + body.toString ());
            } catch (JSONException ex) {
                log.error ("Bogus JSON Response on AutoConfig Query", ex);
            }
        }

        return null;
    }


    /**
     * EventBus listener which gets called from other components by AsyncEventBus
     * @param event the event with the query string
     */
    @Subscribe public void onQueryPhonebookTask (WebservicePhonebookQueryEvent event) {
        if (event.getQueryString () != null)
            event.setResponse (this.queryPhonebook (event.getQueryString ()));
        else
            log.error ("onQueryPhonebookTask: query string is null - skipping query");
    }

    /**
     * Query Phonebook
     * @return a list of Contacts
     */
    public ArrayList<Contact> queryPhonebook (String queryString) {
        ArrayList<Contact> returnList = new ArrayList<Contact> ();

        JSONObject queryData = new JSONObject ();
        queryData.put ("query_string", queryString);
        queryData.put ("query_pattern_type", "nameContains");

        HttpResponse<JsonNode> jsonResponse = this.doJsonRequest (Unirest.post (this.serviceURL + "/phonebookQuery")
                                                                          .header ("accept", "application/json")
                                                                          .header ("Content-Type", "application/json; charset=UTF-8")
                                                                          .body (new JsonNode (queryData.toString ()))
                                                                          .getHttpRequest ());

        if (jsonResponse != null) {
            try {
                JSONObject body = jsonResponse.getBody ().getObject ();
                String status = (String) body.get ("responseStatus");
                if (status.equals ("OK")) {

                    JSONArray contacts = body.getJSONArray ("contacts");
                    for (int i = 0; i < contacts.length (); i++) {
                        try {
                            Contact c = new Contact (contacts.getJSONObject (i).getString ("firstname"),
                                                     contacts.getJSONObject (i).getString ("lastname"),
                                                     contacts.getJSONObject (i).getString ("company"));

                            JSONObject numbers = contacts.getJSONObject (i).getJSONObject ("numbers");
                            try {
                                JSONArray business = numbers.getJSONArray ("business phone");
                                for (int j = 0; j < business.length (); j++)
                                    c.addPhoneNumber (business.getString (j), Contact.PhoneType.businessPhone);
                            } catch (JSONException ex) {
                            }

                            try {
                                JSONArray mobile = numbers.getJSONArray ("mobile");
                                for (int j = 0; j < mobile.length (); j++)
                                    c.addPhoneNumber (mobile.getString (j), Contact.PhoneType.mobile);
                            } catch (JSONException ex) {
                            }

                            returnList.add (c);
                        } catch (JSONException ex) {
                            log.debug ("Bogus contact found.", ex);
                        }
                    }

                } else
                    log.info ("Failed to get Phonebook data " + body.toString ());
            } catch (JSONException ex) {
                log.error ("Bogus JSON Response on Phonebook Query", ex);
            }
        }

        return returnList;
    }

    /**
     * event handler for call origination
     * @param event the event with the call data
     */
    @Subscribe public void onExecuteCTICallEvent (ExecuteCTICallEvent event) {
        event.setResponse (originateCall(event.getNumberToCall (),
                                         event.getTargetCallerId ()));
    }

    /**
     * Call Origination
     * @param targetNumber
     * @param signalledCallerid
     * @return call scheduled / call not scheduled
     */
    public Boolean originateCall (String targetNumber, String signalledCallerid) {
        JSONObject originationData = new JSONObject ();
        originationData.put ("call_destination", targetNumber);
        originationData.put ("signalled_callerid", signalledCallerid);

        HttpResponse<JsonNode> jsonResponse = this.doJsonRequest (Unirest.post (this.serviceURL + "/originateCall")
                                                                          .header ("accept", "application/json")
                                                                          .header ("Content-Type", "application/json; charset=UTF-8")
                                                                          .body (new JsonNode (originationData.toString ()))
                                                                          .getHttpRequest ());

        if (jsonResponse != null) {
            try {
                JSONObject body = jsonResponse.getBody ().getObject ();
                String status = (String) body.get ("responseStatus");
                if (status.equals ("OK")) {
                    log.info ("Call to '" + targetNumber + "' successful scheduled.");
                    return true;
                }
                else {
                    log.error ("Failed to schedule call to '" + targetNumber + "'");
                    return false;
                }

            } catch (JSONException ex) {
                log.error ("Bogus JSON Response on CallOrigination Request", ex);
            }
        }

        return false;
    }

    /**
     * event handler for changing the workstate
     * @param event the event with the workstate data
     */
    @Subscribe public void onSetWorkstateEvent (SetWorkstateEvent event) {
        if (setWorkstate (event.getTargetWorkstate ()))
            EventBusFactory.getDisplayThreadEventBus ().post (new UpdateWorkstateEvent(event.getTargetWorkstate ()));
    }

    /**
     * Set workstate
     * @return Successful/Failed
     */
    public Boolean setWorkstate (InterfaceConstants.WorkstateTypes targetWorkstate) {
        JSONObject workstateData = new JSONObject ();
        workstateData.put ("new_state", targetWorkstate.toString ());

        HttpResponse<JsonNode> jsonResponse = this.doJsonRequest (Unirest.post (this.serviceURL + "/workstate")
                                                                          .header ("accept", "application/json")
                                                                          .header ("Content-Type", "application/json; charset=UTF-8")
                                                                          .body (new JsonNode (workstateData.toString ()))
                                                                          .getHttpRequest ());

        if (jsonResponse != null) {
            try {
                JSONObject body = jsonResponse.getBody ().getObject ();
                String status = (String) body.get ("responseStatus");
                if (status.equals ("OK")) {
                    log.info ("workstate successfully changed to " + targetWorkstate.toString ());
                    return true;
                }
                else {
                    log.error ("Could not change the workstate to "+ targetWorkstate.toString ());
                    return false;
                }

            } catch (JSONException ex) {
                log.error ("Bogus JSON Response on CallOrigination Request", ex);
            }
        }

        return false;
    }

    /**
     * Get Workstate
     * @return the current workstate of the user or null if query/data failed
     */
    public InterfaceConstants.WorkstateTypes getWorkstate () {
        HttpResponse<JsonNode> jsonResponse = this.doJsonRequest (Unirest.get (this.serviceURL + "/workstate")
                                                                          .header ("accept", "application/json")
                                                                          .header ("Content-Type", "application/json; charset=UTF-8")
                                                                          .getHttpRequest ());

        if (jsonResponse != null) {
            try {
                JSONObject body = jsonResponse.getBody ().getObject ();
                String status = (String) body.get ("responseStatus");

                if (status.equals ("OK")) {
                    String workstate = (String) body.get ("workstate");

                    try  {
                        return InterfaceConstants.WorkstateTypes.valueOf (workstate);
                    }
                    catch (Exception e) {
                        // the one we are looking for is IllegalArgumentException, but if there is another error we would not fall into here
                        log.error ("Did not find the workstate enum value given out of the workstate Webservice request '"+workstate+"'");
                        return null; // error
                    }

                }
                else {
                    log.error ("Could not get Workstate.");
                    return null; // error
                }

            } catch (JSONException ex) {
                log.error ("Bogus JSON Response on CallOrigination Request", ex);
            }
        }

        return null; // error
    }

}

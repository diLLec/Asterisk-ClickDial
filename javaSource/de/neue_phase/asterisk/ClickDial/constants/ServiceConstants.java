package de.neue_phase.asterisk.ClickDial.constants;

public class ServiceConstants {

    /** The URL where the webservice can be reached - we hardcode this for now */
    public static final String WebserviceURL = "http://192.168.56.250/webservice/webserviceJson.php";

    /** on which interval the connection is checked */
    public static final Integer WebserviceConnectionCheckInterval = 1000;

    /** How Long to wait for a Webservice Event (!) to come back*/
    public static final Integer WebserviceTimeout = 1000;

    /** How long to wait for a connect (the time it takes to connect to a server) */
    public static final Integer WebserviceConnectTimeout = 1000;

    /** How long to wait for data to be received */
    public static final Integer WebserviceSocketTimeout = 2000;

    /** on which interval the connection is checked */
    public static final Integer AsteriskManagerConnectionCheckInterval = 500;
}

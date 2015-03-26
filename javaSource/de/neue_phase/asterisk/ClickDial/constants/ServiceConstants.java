package de.neue_phase.asterisk.ClickDial.constants;

public class ServiceConstants {

    /** The URL where the webservice can be reached - we hardcode this for now */
    public static final String WebserviceURL = "http://192.168.56.250/webservice/webserviceJson.php";

    /** The interval in which the AutoConfigJob will check for new AutoConfig data */
    public static final Integer	AutoConfigJobInterval = 60000; // 5 minutes

    /** The variance which will be added to the Interval above to prevent peaks on the Webservice */
    public static final Integer	AutoConfigJobIntervalVariance = 5000; // 5 seconds variant

    /** How Long to wait for a Webservice Event (!) to come back*/
    public static final Integer WebserviceTimeout = 1000;
}

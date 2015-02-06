package de.neue_phase.asterisk.ClickDial.serviceInterfaces.exception;

public class WebserviceNotAuthenticatedException extends WebserviceConnectionException {
    public WebserviceNotAuthenticatedException () {
        super ("Webservice does not has an authenticated session.");
    }
}

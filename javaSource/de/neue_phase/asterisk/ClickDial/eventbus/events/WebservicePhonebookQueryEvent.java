package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.datasource.Contact;

import java.util.ArrayList;

public class WebservicePhonebookQueryEvent extends AsyncCallWaitEvent<ArrayList<Contact>> {
    private String queryString;

    public WebservicePhonebookQueryEvent (String queryString) {
        super();
        this.queryString = queryString;
    }

    public String getQueryString () {
        return queryString;
    }
}

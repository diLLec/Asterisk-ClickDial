package de.neue_phase.asterisk.ClickDial.datasource;

import com.google.common.eventbus.EventBus;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.WebservicePhonebookQueryEvent;

public class WebservicePhonebookDataSource extends DataSourceTile {

    /**
     * as DataSourceHolder spawns a thread for us, we will not need to
     * use an async event bus here. Anyhow, we use EventBus to get the
     * loose coupling of components
     */
    private EventBus syncEventBus = EventBusFactory.getSyncEventBus ();

    @Override
    public void query (String query) {
        super.query (query);
        WebservicePhonebookQueryEvent event = new WebservicePhonebookQueryEvent (query);
        syncEventBus.post (event);
        try {
            resultSetListener.addResultSet (event.getReponse (3000));
        } catch (Exception e) {
            log.error ("Failed to get data from Webservice Phonebook.", e);
        }
    }

    @Override
    public String getName() {
        return "WebservicePhonebook";
    }

    @Override
    public void close () {

    }
}

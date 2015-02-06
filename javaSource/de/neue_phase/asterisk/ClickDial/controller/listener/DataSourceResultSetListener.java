package de.neue_phase.asterisk.ClickDial.controller.listener;

import de.neue_phase.asterisk.ClickDial.datasource.Contact;
import java.util.ArrayList;

/**
 * Created by mky on 26.01.2015.
 */
public interface DataSourceResultSetListener {
    public void addResultSet (ArrayList<Contact> resultSet);
}

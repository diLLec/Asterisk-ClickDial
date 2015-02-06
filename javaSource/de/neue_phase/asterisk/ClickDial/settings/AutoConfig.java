package de.neue_phase.asterisk.ClickDial.settings;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerWebservice;
import de.neue_phase.asterisk.ClickDial.util.Dispatcher;
import de.neue_phase.asterisk.ClickDial.util.events.SettingsUpdatedEvent;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AutoConfig {

    private SettingsHolder holder                       = null;
    private AsteriskManagerWebservice webservice        = null;
    private Boolean shutdown                            = false;
    protected final Logger log 				            = Logger.getLogger(this.getClass());
    private Map<String, String> autoConfigData          = null;
    private Dispatcher dispatcher                       = null;

    /**
     * constructor
     * @param holder
     * @param webservice
     */
    public AutoConfig (SettingsHolder holder, AsteriskManagerWebservice webservice, Dispatcher dispatcher) {
        this.holder     = holder;
        this.webservice = webservice;
        this.dispatcher = dispatcher;
    }

    /**
     *
     * @return AutoConfig call was successful (note: it was also successful if we got data from webservice but nothing has changed)
     */
    public Boolean checkAutoConfigData () {
        Map<String, String> newAutoConfigData = this.webservice.getAutoConfigurationData ();

        if (newAutoConfigData != null) {
            if (this.autoConfigData != null) {
                // already received AutoConfig data - check if there are any changes.
                MapDifference<String, String> diff = Maps.difference (this.autoConfigData, newAutoConfigData);
                if (diff.entriesDiffering ().size () > 0) {
                    this.updateConfigData (diff.entriesDiffering ().keySet (), newAutoConfigData); // update changed fields only
                    autoConfigData = newAutoConfigData;
                }
            }
            else {
                this.updateConfigData (newAutoConfigData.keySet (), newAutoConfigData); // update all fields
                autoConfigData = newAutoConfigData;
            }

            return true;
        }
        else {
            log.error ("Failed to get AutoConfig data");
            return false;
        }
    }

    public void updateConfigData (Set<String> differingEntries, Map<String, String> newAutoConfigData) {
        log.info ("Update Settings by new AutoConfig Data on fields: "  + differingEntries.toString ());

        ArrayList<SettingsConstants.SettingsTypes> updatedGroups = new ArrayList<> ();

        for(String differingEntry : differingEntries) {
            boolean updatedField = false;

            for (SettingsAbstractMaster s : holder.getRegisteredSettingGroups ()) {
                if (s.getFieldNames ().contains (differingEntry)) {
                    if (!s.setAutoConfigField (differingEntry, newAutoConfigData.get (differingEntry))) {
                        log.error ("Settings field '" + differingEntry + "' on '" + s.getType () + "' is no AutoConfig enabled field - update skipped.");
                        updatedField = false;
                        updatedGroups.add (s.getType ());
                    } else
                        updatedField = true;

                    break;
                }
            }

            if (!updatedField)
                log.error ("AutoConfig Setting '"+differingEntry+"' could not be found/updated.");
        }

        if (updatedGroups.size () > 0)
            dispatcher.dispatchEvent (new SettingsUpdatedEvent (updatedGroups)); // inform any consumer who is interested in updated settings.
    }


}

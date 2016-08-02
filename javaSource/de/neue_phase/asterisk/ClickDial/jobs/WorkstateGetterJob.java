package de.neue_phase.asterisk.ClickDial.jobs;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.JobConstants;
import de.neue_phase.asterisk.ClickDial.constants.ServiceConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.SetWorkstateEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.UpdateWorkstateEvent;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerWebservice;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class WorkstateGetterJob extends TimerTask implements IJob, Runnable {

    private AsteriskManagerWebservice webservice    = null;
    private Timer timer                             = null;
    private Date lastrun                            = null;


    protected final Logger log 	   = Logger.getLogger(this.getClass());

    private InterfaceConstants.WorkstateTypes currentWorkstate = InterfaceConstants.WorkstateTypes.Arbeit;
    private InterfaceConstants.WorkstateTypes newWorkstate = InterfaceConstants.WorkstateTypes.Arbeit;

    public WorkstateGetterJob (AsteriskManagerWebservice webservice) {
        this.webservice = webservice;
    }


    @Override
    public void startUp () throws InitException {
        this.timer = new Timer ();
        this.timer.scheduleAtFixedRate (this,
                                        0,
                                        JobConstants.WorkstateGetterJobInterval +
                                                new Random().nextInt (JobConstants.WorkstateGetterIntervalVariance));
    }

    @Override
    public ControllerConstants.JobTypes getName () {
        return ControllerConstants.JobTypes.WorkstateGetter;
    }

    @Override
    public void shutdown () {
        this.timer.cancel ();
    }

    @Override
    public boolean isAlive () {
        long diff = new Date().getTime () - this.lastrun.getTime ();
        return (TimeUnit.MILLISECONDS.toSeconds (diff) > JobConstants.WorkstateGetterJobInterval * JobConstants.JobMaxSlippedIntervalTimeMultiplier);
    }

    /**
     * If the workstate is changed via gui {@link SetWorkstateEvent} is triggered. We then change it here too, since otherwise
     * we would detect a change outside the gui and therefore log and fire {@link UpdateWorkstateEvent}
     * @param event The event
     */
    @Subscribe
    synchronized public void onUpdateWorkstateEvent (UpdateWorkstateEvent event) { // from Display UI thread
        log.debug ("WorkstateGetterJob: Updated workstate from GUI.");
        this.currentWorkstate = event.getTargetWorkstate ();
    }

    @Override
    public void run () {
        this.log.trace ("Checking for Workstate data.");
        this.lastrun = new Date ();
        if (( newWorkstate = webservice.getWorkstate ()) == null) {
            this.log.error ("Failed to check Workstate data. Connection Problem?");
        }
        else if  (newWorkstate != currentWorkstate) {
            log.info ("Workstate changed outside of ClickDial Application (to: '"+newWorkstate.toString ()+"')");
            EventBusFactory.getDisplayThreadEventBus ().post (new UpdateWorkstateEvent (newWorkstate)); // will also update my own currentWorkstate
        }
    }
}

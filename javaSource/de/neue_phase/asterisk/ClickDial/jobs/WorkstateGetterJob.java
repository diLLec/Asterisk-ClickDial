package de.neue_phase.asterisk.ClickDial.jobs;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.ServiceConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.eventbus.DisplayThreadSyncExecutor;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.UpdateWorkstateEvent;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerWebservice;
import org.apache.log4j.Logger;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkstateGetterJob implements IJob, Runnable {

    private AtomicBoolean shutdown                  = new AtomicBoolean (false);
    private AsteriskManagerWebservice webservice    = null;
    private Thread runner                           = null;

    private final Random random             = new Random();
    private Integer defaultCheckInterval    = ServiceConstants.WorkstateGetterJobInterval + random.nextInt (ServiceConstants.WorkstateGetterIntervalVariance);

    protected final Logger log 	   = Logger.getLogger(this.getClass());

    private InterfaceConstants.WorkstateTypes currentWorkstate = InterfaceConstants.WorkstateTypes.Arbeit;
    private InterfaceConstants.WorkstateTypes newWorkstate = InterfaceConstants.WorkstateTypes.Arbeit;

    public WorkstateGetterJob (AsteriskManagerWebservice webservice) {
        this.webservice = webservice;
    }


    @Override
    public void startUp () throws InitException {
        this.runner = new Thread (this);
        this.runner.start ();
    }

    @Override
    public ControllerConstants.JobTypes getName () {
        return ControllerConstants.JobTypes.WorkstateGetter;
    }

    @Override
    public void shutdown () {
        shutdown.set (true);
        runner.interrupt ();
    }

    @Override
    public boolean isAlive () {
        return runner.isAlive ();
    }

    @Override
    public void run () {
        do {
            try {
                Thread.sleep (this.defaultCheckInterval);
            } catch (InterruptedException e) {
                log.debug("Infinite loop sleep interrupted - shutdown?");
            }

            this.log.trace ("Checking for Workstate data.");
            if (( newWorkstate = webservice.getWorkstate ()) == null)
                this.log.error ("Failed to check Workstate data. Connection Problem?");

            if (newWorkstate != currentWorkstate) {
                log.info ("Workstate changed outside of ClickDial Application (to: '"+newWorkstate.toString ()+"')");
                EventBusFactory.getDisplayThreadEventBus ().post (new UpdateWorkstateEvent (newWorkstate));
                currentWorkstate = newWorkstate;
            }

        } while (!shutdown.get ());
    }
}

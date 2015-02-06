package de.neue_phase.asterisk.ClickDial.jobs;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.ServiceConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.settings.AutoConfig;
import org.apache.log4j.Logger;
import java.util.Random;

public class AutoConfigJob implements Runnable, IJob {

    private final ControllerConstants.JobTypes type = ControllerConstants.JobTypes.AutoConfig;
    private final Random random    = new Random();
    private AutoConfig autoConfig  = null;
    protected final Logger log 	   = Logger.getLogger(this.getClass());
    private Boolean shutdown       = false;
    private Thread runner          = null;

    private Integer defaultCheckInterval = ServiceConstants.AutoConfigJobInterval + random.nextInt (ServiceConstants.AutoConfigJobIntervalVariance);

    public AutoConfigJob (AutoConfig autoConfig) {
        this.autoConfig = autoConfig;

    }

    @Override
    public void startUp () throws InitException {
        // initial run to check if the AutoConfig webservice call is OK
        // also to ensure, that the configuration is in place before it is used

        if (! this.autoConfig.checkAutoConfigData ())
            throw new InitException ("Initial run of AutoConfig has failed - AutoConfig disabled.");
        else {
            this.runner = new Thread (this);
            this.runner.start ();
        }
    }

    @Override
    public ControllerConstants.JobTypes getName () {
        return this.type;
    }

    /**
     * Timer to renew AutoConfig data
     */
    @Override
    public void run () {
        do {
            try {
                Thread.sleep (this.defaultCheckInterval);
            } catch (InterruptedException e) {
                log.debug("Infinite loop sleep interrupted - shutdown?");
            }

            this.log.trace ("Checking for AutoConfig data.");
            if (! this.autoConfig.checkAutoConfigData ())
                this.log.error ("Failed to check AutoConfig data. Connection Problem?");

        } while (!shutdown);

    }

    /**
     * Shutdown the AutoConfigJob
     */
    @Override
    public synchronized void shutdown () {
        this.shutdown = true;
        this.runner.interrupt ();
    }

    /**
     *
     * @return yes/no
     */
    public boolean isAlive () {
        return this.runner.isAlive ();
    }
}

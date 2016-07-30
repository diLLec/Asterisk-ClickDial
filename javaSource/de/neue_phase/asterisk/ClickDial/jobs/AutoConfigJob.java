package de.neue_phase.asterisk.ClickDial.jobs;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.JobConstants;
import de.neue_phase.asterisk.ClickDial.constants.ServiceConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.settings.AutoConfig;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoConfigJob extends TimerTask implements IJob {

    private final ControllerConstants.JobTypes type = ControllerConstants.JobTypes.AutoConfig;
    private final Random random                     = new Random();
    private AutoConfig autoConfig                   = null;
    protected final Logger log 	                    = Logger.getLogger(this.getClass());
    private Timer timer                             = null;
    private Date lastrun                            = null;

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
            this.timer = new Timer ();
            this.timer.scheduleAtFixedRate (this,
                                            0,
                                            JobConstants.AutoConfigJobInterval +
                                                    new Random().nextInt (JobConstants.AutoConfigJobIntervalVariance));
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
        lastrun = new Date();

        this.log.trace ("Checking for AutoConfig data.");
        if (! this.autoConfig.checkAutoConfigData ())
            this.log.error ("Failed to check AutoConfig data. Connection Problem?");

    }

    /**
     * Shutdown the AutoConfigJob
     */
    @Override
    public void shutdown () {
        timer.cancel ();
    }

    /**
     *
     * @return yes/no
     */
    public boolean isAlive () {
        long diff = new Date().getTime () - this.lastrun.getTime ();
        return (TimeUnit.MILLISECONDS.toSeconds (diff) > JobConstants.AutoConfigJobInterval * JobConstants.JobMaxSlippedIntervalTimeMultiplier);
    }
}

package de.neue_phase.asterisk.ClickDial.jobs;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;

public interface IJob {
    public void startUp () throws InitException;
    public ControllerConstants.JobTypes getName ();
    public void shutdown ();
    public boolean isAlive ();
}

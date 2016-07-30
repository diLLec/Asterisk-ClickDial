package de.neue_phase.asterisk.ClickDial.jobs;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.controller.BaseController;
import de.neue_phase.asterisk.ClickDial.controller.SettingsController;
import de.neue_phase.asterisk.ClickDial.controller.exception.UnknownObjectException;
import de.neue_phase.asterisk.ClickDial.jobs.exceptions.JobCreationException;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerWebservice;
import de.neue_phase.asterisk.ClickDial.settings.AutoConfig;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import org.apache.log4j.Logger;

public class JobFactory {

    protected final static Logger log 						= Logger.getLogger (JobFactory.class);

    /**
     *
     * @param type the type of the job
     * @return the fully instantiated Job object
     * @throws JobCreationException
     */
    public static IJob createJob (ControllerConstants.JobTypes type) throws JobCreationException {
        if (type.equals (ControllerConstants.JobTypes.AutoConfig))
            return createAutoConfigJob ();

        else if (type.equals (ControllerConstants.JobTypes.WorkstateGetter))
            return createWorkstateGetter ();

        else if (type.equals (ControllerConstants.JobTypes.ScreenLockWatcherJob))
            return createScreenLockWatcherJob ();

        else
            throw new JobCreationException ("Sorry - factory is not able to create this type of Job '"+type.toString ()+"'");
    }

    /**
     * Create an AutoConfigJob object. If that fails throw an exception
     * @return AutoConfigJob object
     * @throws JobCreationException
     */
    private static AutoConfigJob createAutoConfigJob () throws JobCreationException {
        try {
            AsteriskManagerWebservice webservice = (AsteriskManagerWebservice) BaseController.getInstance ().getServiceInterface (ControllerConstants.ServiceInterfaceTypes.Webservice);
            return new AutoConfigJob (new AutoConfig (SettingsHolder.getInstance (), webservice));
        }
        catch (UnknownObjectException e) {
            log.error ("Can't get AsteriskManagerWebservice to construct AutoConfig job.", e);
            throw new JobCreationException ("Error while creating AutoConfigJob.");
        }

    }

    /**
     * Create a WorkstateGetterJob object. If that fails throw an exception
     * @return object
     * @throws JobCreationException
     */
    private static WorkstateGetterJob createWorkstateGetter () throws JobCreationException {
        try {
            AsteriskManagerWebservice webservice = (AsteriskManagerWebservice) BaseController.getInstance ().getServiceInterface (ControllerConstants.ServiceInterfaceTypes.Webservice);
            return new WorkstateGetterJob (webservice);
        }
        catch (UnknownObjectException e) {
            log.error ("Can't get AsteriskManagerWebservice to construct AutoConfig job.", e);
            throw new JobCreationException ("Error while creating AutoConfigJob.");
        }
    }

    /**
     * Creates a new ScreenLockWatcherJob
     * @return object
     * @throws JobCreationException
     */
    private static ScreenLockWatcherJob createScreenLockWatcherJob () throws JobCreationException {
        return new ScreenLockWatcherJob();
    }

}

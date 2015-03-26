package de.neue_phase.asterisk.ClickDial.serviceInterfaces;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientServiceAuthenticationDataListener;
import de.neue_phase.asterisk.ClickDial.controller.listener.ServiceInterfaceProblemListener;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IServiceInterface {
    public void startUp () throws InitException;
    public void shutdown ();
    public ControllerConstants.ServiceInterfaceTypes getName();
}

package de.neue_phase.asterisk.ClickDial.serviceInterfaces;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientServiceAuthenticationDataListener;
import de.neue_phase.asterisk.ClickDial.controller.listener.ServiceInterfaceProblemListener;

public interface IServiceInterface {
    public void setInsufficientServiceAuthenticationDataListener (InsufficientServiceAuthenticationDataListener bc);
    public void setServiceInterfaceProblemListener (ServiceInterfaceProblemListener listener);
    public void startUp () throws InitException;
    public void shutdown ();
    public ControllerConstants.ServiceInterfaceTypes getName();
}

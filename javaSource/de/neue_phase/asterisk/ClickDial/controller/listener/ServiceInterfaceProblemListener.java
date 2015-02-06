package de.neue_phase.asterisk.ClickDial.controller.listener;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;

public interface ServiceInterfaceProblemListener {
    public Boolean handleServiceInterfaceContinueOrNot(ControllerConstants.ServiceInterfaceTypes type,
                                                       ControllerConstants.ServiceInterfaceProblems problem,
                                                       Integer tryCount);

}

package de.neue_phase.asterisk.ClickDial.eventbus;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.apache.log4j.Logger;

public class ExceptionHandler implements SubscriberExceptionHandler {

    protected final Logger log 								= Logger.getLogger(this.getClass());

    @Override
    public void handleException (Throwable throwable, SubscriberExceptionContext subscriberExceptionContext) {
        log.error ("Failure on dispatching.", throwable);
    }
}

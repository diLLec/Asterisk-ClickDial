package de.neue_phase.asterisk.ClickDial.eventbus.events;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * {@literal <<PROTOTYPE>>}
 *
 * @author     Mikhail Vladimirov
 * see: http://blog.webagesolutions.com/archives/1027
 */
abstract class AsyncCallWaitEvent<T> {
    private T response = null;
    private CountDownLatch latch = new CountDownLatch(1);

    public T getReponse(long timeoutMS) {
        try {
            latch.await(timeoutMS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void setResponse (T response) {
        this.response = response;
        latch.countDown();
    }
}


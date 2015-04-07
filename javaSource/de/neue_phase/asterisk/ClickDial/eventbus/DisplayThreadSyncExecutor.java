package de.neue_phase.asterisk.ClickDial.eventbus;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import java.util.concurrent.Executor;

public class DisplayThreadSyncExecutor implements Executor {
    protected final Logger log 	= Logger.getLogger(this.getClass());
    protected Display display;

    public DisplayThreadSyncExecutor (Display disp) {
        this.display = disp;
    }

    @Override
    public void execute (Runnable command) {
        this.display.syncExec (command);
    }
}

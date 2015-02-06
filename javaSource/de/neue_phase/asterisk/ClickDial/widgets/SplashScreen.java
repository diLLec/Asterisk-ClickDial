/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.widgets;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.boot.Bootstrap;

/**
 * the Screen presented when booting the application
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SplashScreen {
	private Display 	display 	= Display.getCurrent();
	private final Shell shell 		= new Shell(Bootstrap.primaryShell, SWT.APPLICATION_MODAL);
	private ProgressBar bar  		= null;
	private Label descText			= null;
	private int	  lastCount			= 0;
	private int   maxCount			= 0;
	private Rectangle shellSize 	= null;
	
	/* the logging facility */
	private final Logger    log 				= Logger.getLogger(this.getClass());
	
	/**
	 * constructor
	 * @param maxCount The maximum of times this splash screen can raise it's progress bar
	 */
	public SplashScreen(int maxCount) {

		 /*  set the Layout to FillLayout - we have a
		  *  	<image>
		  *  	<progress>
		  *  	<say-what-you-are-doing-text-area>
		  *  Layout
		*/  
		if (display == null)
			log.error ("FATAL! Splash screen has gotten a display which equals null!");
	 
		/* add the image */
		final Label label = new Label(shell, SWT.NONE);
		File f = new File(	InterfaceConstants.SplashScreen_splashImage );
		
		int heightIntend = 0;
		if (f.exists()) 
		{ 
			/* load the image */
			Image splashImage = new Image(display, f.getPath());
		    label.setImage(splashImage);
		    label.pack();
		    Rectangle labelSize = splashImage.getBounds();
		    
		    /* size the splash screen by image */
			shell.setSize(labelSize.width, labelSize.height + 65);
			
			/* place the image */
			label.setLocation(1, 1);
			heightIntend = labelSize.height;
			
			
		}
		else
			shell.setSize(200, 200);
		
		shellSize = shell.getBounds();
		System.out.println("ShellSize: " + shellSize);
		/* add the progress bar */
		bar = new ProgressBar(shell, SWT.NONE);
		bar.setMaximum(maxCount);
		bar.setSize(shellSize.width, 20);
		bar.setLocation(1, heightIntend + 5);
		heightIntend += bar.getBounds().height;
		this.maxCount = maxCount;

		/* add the say-what-you-are-doing-text-area */
		descText = new Label(shell, SWT.NONE);
		descText.setText("Starting up ... ");
		descText.setAlignment(SWT.CENTER);
		descText.setSize(shellSize.width, 18);
		descText.setLocation(1, heightIntend + 10);
		
		shell.setLocation(  
							(Bootstrap.priMonSize.width -  shellSize.width) / 2 , 
							(Bootstrap.priMonSize.height - shellSize.height) / 2
						 );

        Listener l = new Listener() {
            public void handleEvent(Event e)  {
				if (e.type == SWT.Paint) {
				}
            }
        };
        shell.addListener(SWT.Paint, l);
        shell.addListener(SWT.FOCUSED, l);
	}
	
	/**
	 * raise the progress bar counter 
	 */
	private void raiseCounter () {
		bar.setSelection(++lastCount);
	}

	/** 
	 * set the new Describing text, what is currently done
	 * @param text new text which will be shown below the progress bar
	 */
	public void setDescribingText (String text) {
		descText.setText(text);
		this.raiseCounter();
	}
	
	/**
	 * open the shell 
	 */
	public void open () {
		shell.open ();
	}
	/**
	 * dispose the shell 
	 */
	public void dispose () {
		shell.dispose ();
	}
	/**
	 * iterate blocking till we die
	 */
	public void goIteration () {

		while (lastCount < this.maxCount && 
				!shell.isDisposed () && 
				shell.isVisible ()) 
		{
			if (!display.readAndDispatch()) 
					display.sleep();
		}
		
		shell.dispose();
	}
}

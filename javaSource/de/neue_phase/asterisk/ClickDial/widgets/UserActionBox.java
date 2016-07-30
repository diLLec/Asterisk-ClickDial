package de.neue_phase.asterisk.ClickDial.widgets;

import java.io.File;

import de.neue_phase.asterisk.ClickDial.controller.BaseController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.widgets.Shell;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants.SettingsImages;
import de.neue_phase.asterisk.ClickDial.boot.Bootstrap;

/**
 * A box presented if a user interaction (i.e. an error) is needed
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class UserActionBox implements SelectionListener {
		private Shell  shell 	= null;
		 
		private String returnValue  = "";
		private int    buttonXoffset  = 0;
		private int    buttonHeight = 0;
		private Label  text;
		private Label  image;

		public UserActionBox(String message) {
			this (message, SettingsImages.info, false);
		}

		public UserActionBox(String message, SettingsImages image) {
			this (message, SettingsImages.info, false);
		}
		
		public UserActionBox(String message, SettingsImages image, boolean moveAbove) {
			shell 	= new Shell (BaseController.getInstance ().getPrimaryShell (), SWT.APPLICATION_MODAL | SWT.TITLE);
			shell.setText("User interaction needed");
			text = new Label (shell, SWT.NORMAL | SWT.WRAP);
			text.setText(message);
			text.setLocation(50, 15);
			text.pack();
			
			setImage ( image );
			
			Rectangle r = text.getBounds();
			buttonHeight = r.y + r.height + 35;
			
		}
		
		public String open () {

			Rectangle r = text.getBounds();
			System.out.println("Button x offset: " + buttonXoffset + " rwidth: " + r.width);
			if (buttonXoffset > r.x  + r.width)
				shell.setSize(buttonXoffset   + 20, buttonHeight + 65);
			else
				shell.setSize(r.x + r.width   + 20, buttonHeight + 65);

			/* place the box centered on the primary screen */
			Rectangle shellSize = shell.getBounds();
			r = Bootstrap.priMonSize;
			shell.setLocation( (r.width - shellSize.width) / 2, (r.height - shellSize.height) / 2);
			
			shell.open();
			
			while (!shell.isDisposed () && shell.isVisible ()) {
				if (!BaseController.getInstance ().getMainDisplay ().readAndDispatch())
					BaseController.getInstance ().getMainDisplay ().sleep();
			}
			
			return returnValue;
		}
		
		public void addButton (String naming, int reValue) {
			Button b; 
			b = new Button (shell, SWT.PUSH);
			b.setText(naming);
			b.addSelectionListener(this);
			b.setLocation(10 + buttonXoffset, buttonHeight);
			b.pack();
			buttonXoffset += b.getBounds().width + 10;
		}

		public void widgetDefaultSelected(SelectionEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void widgetSelected(SelectionEvent arg0) {
			returnValue = ((Button) arg0.widget).getText();
			shell.dispose();
		}
		
		private void setImage (SettingsImages imageName) {
			
			File f = new File(	InterfaceConstants.SettingsTypeIcons_Path + 
								imageName.toString() + 
								InterfaceConstants.SettingsTypeIcons_Suffix);
			System.out.println("Searching "+imageName.toString()+" image here: " + f.getPath());
			if (f.exists()) {
				image = new Label (shell, SWT.NORMAL);
				image.setImage(new Image(BaseController.getInstance ().getMainDisplay (), f.getPath()));
				image.setLocation(15, 15);
				image.pack();
			}
		}
		
		
}

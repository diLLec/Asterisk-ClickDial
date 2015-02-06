package de.neue_phase.asterisk.ClickDial.widgets;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.boot.Bootstrap;
import de.neue_phase.asterisk.ClickDial.controller.DialWindowController;

/**
 * the dial window is the short widget, which will take numbers
 * and put them (over callbacks in controller classes) to asterisk 
 * for dialing 
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class DialWindow {

	private Shell 					shell 					= null;
	private Display 				displayRef 				= null;
	private Text					dialArea				= null;
	private ClickDialContentProposalAdapter  dialAreaAutocompleteAdapter = null;
	private DialWindowController	ctrl 					= null;
	private Menu 					rightClickMenu			= null;
	private MenuItem 				connectionState			= null;
	
	/* the logging facility */
	private final Logger    log 				= Logger.getLogger(this.getClass());
	
	public DialWindow(Display displayRef, DialWindowController ctrl) {
		
		this.ctrl 		= ctrl;
		this.displayRef = displayRef;

		shell = new Shell (Bootstrap.primaryShell, SWT.NO_TRIM);

		cutRegionFromImage ();
		addTextArea ();
	}
	
	/**
	 * place the shell correctly and 
	 * open the shell
	 * 
	 */
	public void startMe () {
		
		/* place the dial window in the lower right corner of the primary screen */
		Rectangle shellSize = shell.getBounds();
		shell.setLocation(Bootstrap.priMonSize.width -  shellSize.width - 15,
						  Bootstrap.priMonSize.height -  shellSize.height - 55); 
		
		shell.open();		
	}
	
	/**
	 * add the dial window text area where 
	 * the dial string will be entered 
	 */
	
	private void addTextArea () {
		/* create the text area where we can dial */
		dialArea = new Text(this.shell, SWT.APPLICATION_MODAL);
		dialArea.setLocation(60, 18);
		dialArea.setSize(150, 14);
		dialArea.setBackground(InterfaceConstants.DialWindowBackground);

		// add autocomplete adapter
		try {
			this.dialAreaAutocompleteAdapter = new ClickDialContentProposalAdapter(dialArea,
																				   new TextContentAdapter (),
																				   new SimpleContentProposalProvider(new String[] {"searching ..."}),
																				   KeyStroke.getInstance (InterfaceConstants.DialWindowAutocompleteHotkey),
																				   null);
			this.dialAreaAutocompleteAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			this.dialAreaAutocompleteAdapter.addContentProposalListener ((IContentProposalListener) this.ctrl);
			this.dialAreaAutocompleteAdapter.addContentProposalListener ((IContentProposalListener2) this.ctrl);
			this.dialAreaAutocompleteAdapter.setPopupSize (new Point(150,150));

		}
		catch (ParseException e) {
			log.error ("DialWindowAutocompleteHotkey ("+InterfaceConstants.DialWindowAutocompleteHotkey+") can't be parsed by KeyStroke.getInstance");
		}

	}

	public void updateAutocompleteProposals (String[] proposals) {
		for (int i=0; i < proposals.length; i++)
			log.debug("new proposal "+i+": " + proposals[i]);

		this.dialAreaAutocompleteAdapter.close ();
		this.dialAreaAutocompleteAdapter.setContentProposalProvider (new SimpleContentProposalProvider(proposals));
		this.dialAreaAutocompleteAdapter.open();
		this.dialAreaAutocompleteAdapter.setProposalPopupFocus ();

	}

	/**
	 * - use the alpha mask of the image to present the shell in a 
	 * image described manner
	 * 
	 * - add a listener for the right click menu
	 * - add a listener to make the shell movable
	 * - add a listener for paint req's
	 * 
	 */
	private void cutRegionFromImage () {
		
		final Image image = new Image(displayRef, InterfaceConstants.DialWindowBackgroundImage);
		
        Region region = new Region();
        final ImageData imageData = image.getImageData();
        if (imageData.alphaData != null) {
        	Rectangle pixel = new Rectangle(0, 0, 1, 1);
        	for (int y = 0; y < imageData.height; y++) {
	            for (int x = 0; x < imageData.width; x++) {
	                if (imageData.getAlpha(x, y) == 255) {
	                	pixel.x = imageData.x + x;
    					pixel.y = imageData.y + y;
	                    region.add(pixel);
	                } 
	            }
	        }
        } else {
        	ImageData mask = imageData.getTransparencyMask();
        	Rectangle pixel = new Rectangle(0, 0, 1, 1);
    		for (int y = 0; y < mask.height; y++) {
    			for (int x = 0; x < mask.width; x++) {
    				if (mask.getPixel(x, y) != 0) {
    					pixel.x = imageData.x + x;
    					pixel.y = imageData.y + y;
    					region.add(pixel);
    				}
    			}
    		}
        }
        
        shell.setRegion(region);
        Listener l = new Listener() {
        	int startX, startY;
            public void handleEvent(Event e)  {
            	/* 
            	 * make the shell moveable
            	 * 	- click with the left mouse button
            	 */
				if (e.type == SWT.MouseDown && e.button == 1) {
					startX = e.x;
					startY = e.y; 
				}
				if (e.type == SWT.MouseMove && (e.stateMask & SWT.BUTTON1) != 0) {
					Point p = shell.toDisplay(e.x, e.y);
					p.x -= startX;
					p.y -= startY;
					shell.setLocation(p);
				}
				
				/*
				 * popup the menu if clicked right
				 */
				
				if (e.type == SWT.MouseDown && e.button == 3) {
					buildRightClickMenu (shell.toDisplay(e.x, e.y));
				}
				
				/*
				 * redraw the background image on paint req
				 */
				if (e.type == SWT.Paint) {
					e.gc.drawImage(image, imageData.x, imageData.y);
				}
            }
        };
        shell.addListener(SWT.MouseDown, l);
        shell.addListener(SWT.MouseMove, l);
        shell.addListener(SWT.Paint, 	 l);

        shell.setSize(imageData.x + imageData.width, imageData.y + imageData.height);
        
	}
	
	/**
	 * build the menu which is shown if somebody clicks "right" on
	 * the dial window surface
	 * @param pos
	 * @return
	 */
	
	private void buildRightClickMenu (Point pos) {
		if (rightClickMenu == null) {
			/* if not constructed yet - construct it */
			rightClickMenu = new Menu (shell);
			connectionState = new MenuItem(rightClickMenu, SWT.PUSH);

			MenuItem item;
			item = new MenuItem(rightClickMenu, SWT.SEPARATOR);

			item = new MenuItem(rightClickMenu, SWT.PUSH);
			item.setText("open configuration");
			item.addSelectionListener(ctrl);
			item = new MenuItem(rightClickMenu, SWT.PUSH);
			item.setText("about");
			item.addSelectionListener(ctrl);
			
			item = new MenuItem(rightClickMenu, SWT.SEPARATOR);

			item = new MenuItem(rightClickMenu, SWT.PUSH);
			item.addSelectionListener(ctrl);
			item.setText("exit");
		}
		
		File f = new File(InterfaceConstants.SettingsTypeIcons_Path +  
						  ctrl.getAsteriskConnectionStageIcon() + 
						  InterfaceConstants.SettingsTypeIcons_Suffix);
		
		if (f.exists())
			connectionState.setImage(new Image(displayRef, f.getPath()));
		
		connectionState.setText(ctrl.getAsteriskConnectionState());

		File f2 = new File(InterfaceConstants.SettingsTypeIcons_Path +
								  ctrl.getWebserviceConnectionStateIcon () +
								  InterfaceConstants.SettingsTypeIcons_Suffix);

		if (f2.exists())
			connectionState.setImage(new Image(displayRef, f2.getPath()));

		connectionState.setText(ctrl.getWebserviceConnectionState());

		
		/* set the position and make it visible */
		rightClickMenu.setLocation(pos.x, pos.y);
		rightClickMenu.setVisible(true);		
	}
	
	/**
	 * get the length of the dial window text
	 * @return the length of the dial window text
	 */
	public int getTextLength () {
		return this.dialArea.getText().length();
	}

	/**
	 * get the text of the dial window
	 * @return the text of the dial window
	 */
	public String getText () {
		return this.dialArea.getText();		
	}
	
	/**
	 * close down routine
	 */
	public void dispose () {
		rightClickMenu.dispose();
		connectionState.dispose();
		shell.dispose();
	}

	/**
	 * toggle the shell hide/show
	 */
	public void toggleHideShow () {
		shell.setVisible( ! shell.isVisible() );
		if (shell.isVisible())
			shell.setFocus();
	}


}

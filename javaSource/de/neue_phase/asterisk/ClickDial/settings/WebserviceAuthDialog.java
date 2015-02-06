package de.neue_phase.asterisk.ClickDial.settings;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;



/**
 * User authentication dialog
 */
public class WebserviceAuthDialog extends Dialog {
    protected Text usernameField;
    protected Text passwordField;
    protected String message;

    protected Authentication userAuthentication = null;

    private class Authentication {
        public String username;
        public String password;

        public Authentication (String username, String password)
        {
            this.username = username;
            this.password = password;
        }

    }
    /**
     * Gets user and password from a user. May be called from any thread
     *
     * @return UserAuthentication that contains the userid and the password or
     *         <code>null if the dialog has been cancelled
     */
    public static String[] getAuthentication(String message) {
        class UIOperation implements Runnable {
            public Authentication authentication;
            public void run() {
                authentication = WebserviceAuthDialog.askForAuthentication(message);
            }
        }

        UIOperation uio = new UIOperation();
        if (Display.getCurrent() != null) {
            uio.run();
        } else {
            Display.getDefault().syncExec(uio);
        }
        if (uio.authentication == null)
            return null;
        else
            return new String[]{uio.authentication.username, uio.authentication.password};
    }
    /**
     * Gets user and password from a user Must be called from UI thread
     *
     * @return UserAuthentication that contains the userid and the password or
     *         <code>null if the dialog has been cancelled
     */
    protected static Authentication askForAuthentication(String message) {
        WebserviceAuthDialog ui = new WebserviceAuthDialog(null, message);
        ui.open();
        return ui.getAuthData ();
    }

    public Authentication getAuthData () {
        return this.userAuthentication;
    }
    /**
     * Creates a new WebserviceAuthDialog.
     *
     * @param parentShell
     *            parent Shell or null
     */
    protected WebserviceAuthDialog(Shell parentShell, String message) {
        super(parentShell);
        setBlockOnOpen(true);
        this.message = message;
    }
    /**
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Authentication");
    }
    /**
     */
    public void create() {
        super.create();
        //give focus to username field
        usernameField.selectAll();
        usernameField.setFocus();
    }
    /**
     */
    protected Control createDialogArea(Composite parent) {
        Composite main = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        main.setLayout(layout);
        main.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(main, SWT.WRAP);
        label.setText(this.message);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        label.setLayoutData(data);

        createUsernameFields(main);
        createPasswordFields(main);

        return main;
    }
    /**
     * Creates the three widgets that represent the user name entry area.
     */
    protected void createPasswordFields(Composite parent) {
        new Label(parent, SWT.NONE).setText("Password");

        passwordField = new Text(parent, SWT.BORDER | SWT.PASSWORD);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
        passwordField.setLayoutData(data);

        new Label(parent, SWT.NONE); //spacer
    }
    /**
     * Creates the three widgets that represent the user name entry area.
     */
    protected void createUsernameFields(Composite parent) {
        new Label(parent, SWT.NONE).setText("Username");

        usernameField = new Text(parent, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
        usernameField.setLayoutData(data);

        new Label(parent, SWT.NONE); //spacer
    }

    /**
     * Notifies that the ok button of this dialog has been pressed.
     */
    protected void okPressed() {
        userAuthentication = new Authentication(usernameField.getText(),
                                                passwordField.getText());
        super.okPressed();
    }

}
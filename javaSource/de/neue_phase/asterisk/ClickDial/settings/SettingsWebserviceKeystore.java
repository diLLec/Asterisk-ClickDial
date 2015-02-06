package de.neue_phase.asterisk.ClickDial.settings;

import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractAsteriskManagerWebinterfaceAuthData;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ISettingsExtractModel;
import org.apache.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.*;

/**
 * see: http://stackoverflow.com/questions/6243446/how-to-store-a-simple-key-string-inside-java-keystore
 */
public class SettingsWebserviceKeystore {

    protected enum State {
        NULL,
        EMPTY,
        LOADED,
        FAILED
    }

    protected enum CredentialState {
        UNCHECKED,
        ACKNOWLEDGED
    }

    protected State keystoreState   = State.NULL;
    protected CredentialState credState       = CredentialState.UNCHECKED;
    protected final Logger log 	    = Logger.getLogger(this.getClass());
    protected KeyStore ks           = null;
    protected SecretKeyFactory factory = null;
    protected KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection("7XWR9WXgJdtVe4abiv77LTqYAtfnt9emKcyne9AjVaXXHAuEPALAjPRiJJWJssCypAxqpEPJkWuyrJUekHzW9AWE9LKKpqttchoMJ".toCharArray ());
    protected char[] openPassword   = "mPCEYMJCKHkosKuiCbKiHuuwPwxqccAv73ncJqgWq9F4XJnw7WrLXaifYYLWupJ4UdNohFVnooNg3AfPm7quJFvhyRn4RUymCeCHd".toCharArray ();
    protected File keystoreFile     = new File (SettingsConstants.configSearchLocation +
                                                        this.getClass ().getSimpleName () +
                                                        SettingsConstants.configSearchSuffix);

    public SettingsWebserviceKeystore () throws InitException {
        try {
            this.ks = KeyStore.getInstance ("JCEKS");
            this.factory = SecretKeyFactory.getInstance("PBE");
            loadKeystore();
        } catch (GeneralSecurityException e) {
            log.error ("Failed to instantiate/Load the WebserviceKeystore", e);
            keystoreState = State.FAILED;
            throw new InitException ("Failed to instantiate/Load the WebserviceKeystore");
        }
    }

    /**
     * save the keystore
     */
    private void saveKeystore () {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream (keystoreFile);
            ks.store(fos, openPassword);
            keystoreState = State.LOADED;
        } catch (Exception e) {
            log.error ("WebserviceKeystore could not be saved to '"+keystoreFile.getAbsolutePath ()+"'", e);
            keystoreState = State.FAILED;
        } finally {
            try {
                if (fos != null)
                    fos.close ();
            } catch (IOException e) {}
        }
    }

    /**
     * load the keystore
     */
    private void loadKeystore () {
        java.io.FileInputStream fis = null;
        try {
            fis = new FileInputStream (keystoreFile);
            ks.load (fis, openPassword);
            keystoreState = State.LOADED;
        } catch (Exception e) {
            log.error ("WebserviceKeystore could not be loaded from '"+keystoreFile.getAbsolutePath ()+"'", e);
            try {
                log.info ("Instantiating empty keystore.");
                ks.load (null, openPassword); // instantiate
                keystoreState = State.EMPTY;
            } catch (Exception e2) {
                log.error ("Could not instantiate empty keystore.", e2);
                keystoreState = State.FAILED;
            }
        } finally {
            try {
                if (fis != null)
                    fis.close ();
            } catch (IOException e) {}
        }
    }

    /**
     *
     * @param entryName
     * @return entry value or null
     */
    private String getProtectedKeystoreEntry (String entryName) {
        try {
            log.debug ("getProtectedKeystoreEntry");
            if (this.keystoreState != State.LOADED || ! ks.isKeyEntry (entryName)) {
                log.debug ("Entry '"+entryName+"' not found in Keystore or keystore not loaded (current state: '"+keystoreState.toString ()+"'.");
                return null; // entry not in there
            }

            KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry)ks.getEntry(entryName, protParam);
            PBEKeySpec keySpec = (PBEKeySpec)factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);
            return new String(keySpec.getPassword());
        } catch (GeneralSecurityException e) {
            log.error (entryName + " could not be extracted out of Keystore.", e);
            this.keystoreState = State.FAILED;
            return null;
        }
    }

    /**
     * returns if the container is loaded and got data in it
     * @return true = data there | false = no data available
     */
    public Boolean hasWebserviceAuthData () {
        try {
            return (this.keystoreState == State.LOADED && (ks.isKeyEntry ("wsUsername") && ks.isKeyEntry ("wsPassword")));
        } catch (GeneralSecurityException e) {
            log.error ("Problem while accessing the keystore", e);
            return null;
        }
    }

    /**
     * returns if the keystore is loaded so that we can put data in it
     * @return true = loaded | false = failed or not loaded in some kind
     */
    public Boolean isWriteable () {
        return (this.keystoreState == State.LOADED || keystoreState == State.EMPTY);
    }

    public void acknowledgeCredentials () {
        this.credState = CredentialState.ACKNOWLEDGED;
    }

    /**
     * @return true = credentials have been acknowledged by consumer
     */
    public Boolean areCredentialsAcknowledged () {
        return (this.credState == CredentialState.ACKNOWLEDGED);
    }

    /**
     *
     * @return The username as a string or null
     */
    public String getWebserviceUsername () {
        return this.getProtectedKeystoreEntry ("wsUsername");
    }

    /**
     *
     * @return The password as a string or null
     */
    public String getWebservicePassword () {
        return this.getProtectedKeystoreEntry ("wsPassword");
    }

    /**
     * @return true (success) | false (failure)
     */
    public Boolean setWebserviceAuthData (String username, String password) {

        if (this.keystoreState != State.EMPTY && this.keystoreState != State.LOADED) {
            log.error ("Can't update keystore which is in State '"+this.keystoreFile.toString ()+"'");
            return false;
        }

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance ("PBE");
            SecretKey usernameSecret = factory.generateSecret(new PBEKeySpec (username.toCharArray()));
            SecretKey passwordSecret = factory.generateSecret(new PBEKeySpec (password.toCharArray()));

            ks.setEntry ("wsUsername", new KeyStore.SecretKeyEntry(usernameSecret), protParam);
            ks.setEntry ("wsPassword", new KeyStore.SecretKeyEntry(passwordSecret), protParam);

            this.credState = CredentialState.UNCHECKED;
            this.saveKeystore ();
            return true;
        } catch (GeneralSecurityException e) {
            log.error ("Auth Data could not be stored in Keystore.", e);
            keystoreState = State.FAILED;
            return false;
        }
    }

    /**
     *
     * @return extract (auth Data)
     */
    public ISettingsExtractModel getWebserviceAuthData () {
        return new ExtractAsteriskManagerWebinterfaceAuthData (this.getWebserviceUsername (),
                                                               this.getWebservicePassword ());
    }
}

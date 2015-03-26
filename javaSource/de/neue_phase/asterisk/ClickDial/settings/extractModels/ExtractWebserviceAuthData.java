package de.neue_phase.asterisk.ClickDial.settings.extractModels;

public class ExtractWebserviceAuthData implements ISettingsExtractModel {
    private String username;
    private String password;

    public ExtractWebserviceAuthData (String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername () {
        return username;
    }

    public String getPassword () {
        return password;
    }
}

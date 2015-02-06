package de.neue_phase.asterisk.ClickDial.settings.extractModels;

public class ExtractAsteriskManagerInterfaceConnectionData implements ISettingsExtractModel {
    private String hostname;
    private Integer port;

    public ExtractAsteriskManagerInterfaceConnectionData (String hostname, String port) {
        this.hostname = hostname;
        this.port = new Integer(port);
    }

    public ExtractAsteriskManagerInterfaceConnectionData (String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname () {
        return hostname;
    }

    public Integer getPort () {
        return port;
    }

    public String getConnectionString () {
        return hostname +":"+ port.toString ();
    }
}

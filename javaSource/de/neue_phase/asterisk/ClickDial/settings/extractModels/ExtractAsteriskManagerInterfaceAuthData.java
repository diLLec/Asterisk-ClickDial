package de.neue_phase.asterisk.ClickDial.settings.extractModels;

public class ExtractAsteriskManagerInterfaceAuthData implements ISettingsExtractModel {
    private ExtractAsteriskManagerInterfaceConnectionData connectData = null;
    private String user;
    private String password;
    private Integer timeout;
    private String channel;

    public ExtractAsteriskManagerInterfaceAuthData (ExtractAsteriskManagerInterfaceConnectionData connectData,
                                                    String user,
                                                    String password,
                                                    Integer timeout,
                                                    String channel) {
        this.connectData = connectData;
        this.user = user;
        this.password = password;
        this.timeout = timeout;
        this.channel = channel;
    }

    public ExtractAsteriskManagerInterfaceConnectionData getConnectData () {
        return connectData;
    }

    public String getUser () {
        return user;
    }

    public String getPassword () {
        return password;
    }

    public Integer getTimeout () {
        return timeout;
    }

    public String getChannel () {
        return channel;
    }
}

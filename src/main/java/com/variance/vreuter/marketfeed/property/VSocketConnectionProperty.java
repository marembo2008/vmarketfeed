/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.property;

import com.anosym.vjax.annotations.Comment;
import com.anosym.vjax.annotations.Marshallable;


/**
 *
 * @author Administrator
 */
public class VSocketConnectionProperty implements VConnectionProperty {

    private String serverAddress;
    private int serverPort;
    private String defaultServerAddress = "192.168.0.12";

    public VSocketConnectionProperty() {
        this("192.168.0.12", 5003);
    }

    public VSocketConnectionProperty(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Comment("The Server IP or Server Name Address. If a name is specified, the system will do a DNS name look up.")
    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    @Comment("The Server Port at which the TOF application is listening on. By default this is port 5003")
    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    @Marshallable(marshal = false)
    public VServerConnectionType getConnectionType() {
        return VServerConnectionType.SOCKET_CONNECTION;
    }
}

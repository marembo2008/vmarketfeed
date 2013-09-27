/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.property;

import com.anosym.vjax.annotations.Marshallable;


/**
 *
 * @author Administrator
 */
public class VSerialConnectionProperty implements VConnectionProperty {

    private String comPortConnection;
    private int baudrate;
    private int databits;
    private int startBits;
    private int stopBits;

    public VSerialConnectionProperty() {
        comPortConnection = "COM1";
        baudrate = 9600;
        databits = 8;
        startBits = 1;
        stopBits = 1;
    }

    public int getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public int getDatabits() {
        return databits;
    }

    public void setDatabits(int databits) {
        this.databits = databits;
    }

    public int getStartBits() {
        return startBits;
    }

    public void setStartBits(int startBits) {
        this.startBits = startBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public String getComPortConnection() {
        return comPortConnection;
    }

    public void setComPortConnection(String comPortConnection) {
        this.comPortConnection = comPortConnection;
    }

    @Override
    @Marshallable(marshal = false)
    public VServerConnectionType getConnectionType() {
        return VServerConnectionType.SERIAL_CONNECTION;
    }
}

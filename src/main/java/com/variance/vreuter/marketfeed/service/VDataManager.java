/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.service;

/**
 *
 * @author Administrator
 */
public interface VDataManager { 

    public void handleData(byte[] data);

    public byte[] getData(); 

    public byte[] getInitializationData();

    public byte[] format(byte[] data); 

    public void addMarketfeedListener(VMarketfeedListener marketfeedListener);

    public void removeMarketfeedListener(VMarketfeedListener marketfeedListener);

    @javax.annotation.PostConstruct
    public void onStart();

    public boolean isErrorInInitialization();
    /**
     * Called to indicate to the data manager that no more request can be accepted
     */
    public void marketfeedServiceDisconnected();
}

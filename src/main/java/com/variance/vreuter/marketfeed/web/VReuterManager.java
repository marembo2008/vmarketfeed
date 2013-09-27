/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.variance.vreuter.marketfeed.connection.VDealingServerConnection;
import com.variance.vreuter.marketfeed.service.VDataManager;
import com.variance.vreuter.marketfeed.service.VMarketfeedListener;

/**
 *
 * @author Administrator
 */
public class VReuterManager implements VReuterManagerRemote {

    private VDealingServerConnection serverConnection;
    private VDataManager dataManager;
    public VReuterManager REUTER_MANAGER = getInstance();

    private VReuterManager getInstance() {
        if (REUTER_MANAGER == null) {
            return new VReuterManager();
        }
        return REUTER_MANAGER;
    }

    private VReuterManager() {
        onStart();
    }

    private void onStart() {
//        if (serverConnection != null) {
//            throw new RuntimeException("Server Connection Already Established............!!");
//        }
//        try {
//            //start the manager first
//            dataManager = new VMarketfeedProtocolService(new VDealingManager());
//            serverConnection = VDealingServerConnectionManager.getServerConnection(dataManager);
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
    }

    @Override
    public void addMarketfeedListener(VMarketfeedListener marketfeedListener) {
        dataManager.addMarketfeedListener(marketfeedListener);
    }

    @Override
    public void removeMarketfeedListener(VMarketfeedListener marketfeedListener) {
        dataManager.removeMarketfeedListener(marketfeedListener);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.dataManager != null ? this.dataManager.hashCode() : 0);
        return hash;
    }
}

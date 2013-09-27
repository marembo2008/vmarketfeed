/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

/**
 *
 * @author Administrator
 */
public interface VReuterManagerRemote {

    public void addMarketfeedListener(com.variance.vreuter.marketfeed.service.VMarketfeedListener marketfeedListener);

    public void removeMarketfeedListener(com.variance.vreuter.marketfeed.service.VMarketfeedListener marketfeedListener);
}

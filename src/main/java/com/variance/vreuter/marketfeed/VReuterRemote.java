/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed;

/**
 *
 * @author Administrator
 */
public interface VReuterRemote {

    public com.variance.vreuter.marketfeed.property.VReuterProperty getReuterProperties();

    public void setReportLogging(java.io.OutputStream reportLogging);

    public void setErrorLogging(java.io.OutputStream errorLogging);

    void updateSynchronously();

    @javax.annotation.PostConstruct
    public void onStart();

    public void setReuterProperties(com.variance.vreuter.marketfeed.property.VReuterProperty reuterProperties);

    @javax.annotation.PreDestroy
    public void onDestroy();

    public void doUpdate();
}

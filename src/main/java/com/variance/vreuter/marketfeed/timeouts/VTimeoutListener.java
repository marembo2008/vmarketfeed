/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.timeouts;

/**
 *
 * @author Administrator
 */
public interface VTimeoutListener {

    void notifyStarted(VTimer timer);

    void notifyStopped(VTimer timer);

    void notifyPaused(VTimer timer);

    void notifyResumed(VTimer timer);
}

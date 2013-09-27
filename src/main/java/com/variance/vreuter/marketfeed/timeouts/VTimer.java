/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.timeouts;

/**
 *
 * @author Administrator
 */
public interface VTimer {

    void start();

    void stop();

    void pause();

    void resume();
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.service;

/**
 *
 * @author Administrator
 */
public class VMarketfeedException extends RuntimeException {

    public VMarketfeedException(Throwable cause) {
        super(cause);
    }

    public VMarketfeedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VMarketfeedException(String message) {
        super(message);
    }

    public VMarketfeedException() {
    }
}

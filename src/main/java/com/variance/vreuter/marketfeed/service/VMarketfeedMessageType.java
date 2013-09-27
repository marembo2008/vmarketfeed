/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.service;

/**
 *
 * @author Administrator
 */
public enum VMarketfeedMessageType {

    UPDATE(316),
    STATUS_RESPONSE(407),
    RECORD_RESPONSE(340),
    SNAPSHOT_REQUEST(333),
    DATA_UPDATE_REQUEST(332);
    private int typeIndex;

    private VMarketfeedMessageType(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    public static VMarketfeedMessageType getInstance(int typeIndex) {
        for (VMarketfeedMessageType mmt : values()) {
            if (mmt.getTypeIndex() == typeIndex) {
                return mmt;
            }
        }
        return null;
    }
}

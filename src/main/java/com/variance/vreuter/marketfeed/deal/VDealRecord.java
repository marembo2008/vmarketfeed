/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal;

import com.variance.vreuter.marketfeed.util.VColumnRecord;

/**
 *
 * @author marembo
 */
public class VDealRecord {

    private VColumnRecord column;
    private String value;

    public VDealRecord() {
    }

    public VDealRecord(VColumnRecord column, String value) {
        this.column = column;
        this.value = value;
    }

    public VColumnRecord getColumn() {
        return column;
    }

    public void setColumn(VColumnRecord column) {
        this.column = column;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VDealRecord other = (VDealRecord) obj;
        if (this.column != other.column && (this.column == null || !this.column.equals(other.column))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.column != null ? this.column.hashCode() : 0);
        return hash;
    }
}

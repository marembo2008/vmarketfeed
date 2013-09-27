/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.util;

import com.anosym.utilities.Duplex;
import com.anosym.vjax.annotations.Comment;
import com.anosym.vjax.annotations.Markup;
import com.anosym.vjax.annotations.Position;
import com.anosym.vjax.annotations.WhenNull;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;

/**
 *
 * @author Administrator
 */
public class VColumnRecord extends Duplex<String, VRecordResponseData> implements Comparable<VColumnRecord> {

    private int columnIndex;
    private String defaultValue = "";
    private boolean alwaysUseDefaultValue;
    private boolean sequential;

    public VColumnRecord(String firstValue, VRecordResponseData secondValue) {
        super(firstValue, secondValue);
    }

    public VColumnRecord(String firstValue, VRecordResponseData secondValue, String defaultValue) {
        super(firstValue, secondValue);
        this.defaultValue = defaultValue;
    }

    public VColumnRecord() {
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    @Comment("Defines this field as generated sequentially.")
    public boolean isSequential() {
        return sequential;
    }

    @Comment("Maps to a column whose value is always constant regardless of the value received from the TOF server")
    public boolean isAlwaysUseDefaultValue() {
        return alwaysUseDefaultValue;
    }

    @Override
    public void setSecondValue(VRecordResponseData secondValue) {
        super.setSecondValue(secondValue);
    }

    @Override
    public void setFirstValue(String firstValue) {
        super.setFirstValue(firstValue);
    }

    public void setAlwaysUseDefaultValue(boolean alwaysUseDefailtValue) {
        this.alwaysUseDefaultValue = alwaysUseDefailtValue;
    }

    @Comment("When the TOF Server sends and empty field value, then the default value will be used for that field.")
    @WhenNull(mode = WhenNull.NullMode.MARSHALL_NULL)
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Comment("When the Column Records Have to be indexed."
    + "\nThis value shows the relative index of each column. This is important when the data manager"
    + "has to save the column records in a certain order")
    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    @Comment("The Table column that maps to the TOF data field")
    @Markup(name = "table-column", property = "firstValue")
    @Position(index = 0)
    public String getFirstValue() {
        return super.getFirstValue();
    }

    @Override
    @Comment("The TOF data field that is mapped to the Table column")
    @Markup(name = "data-field", property = "secondValue")
    @Position(index = 1)
    public VRecordResponseData getSecondValue() {
        return super.getSecondValue();
    }

    @Override
    public int compareTo(VColumnRecord record) {
        return ((Integer) this.columnIndex).compareTo(record.columnIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VColumnRecord other = (VColumnRecord) obj;
        if (this.columnIndex != other.columnIndex) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.columnIndex;
        return hash;
    }
}

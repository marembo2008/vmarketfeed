/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal;

import com.anosym.vjax.annotations.DynamicMarkup;
import com.anosym.vjax.annotations.IgnoreGeneratedAttribute;
import com.anosym.vjax.annotations.Marshallable;
import com.anosym.vjax.util.VMarkupGenerator;
import com.variance.vreuter.marketfeed.deal.VDealColumn.VDealColumnGenerator;
import com.variance.vreuter.marketfeed.util.VColumnRecord;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marembo
 */
@DynamicMarkup(markupGenerator = VDealColumnGenerator.class)
@IgnoreGeneratedAttribute
public class VDealColumn {

  public static class VDealColumnGenerator implements VMarkupGenerator<VDealColumn> {

    @Override
    public String generateMarkup(Object property) {
      //there is no instance, so it does not matter
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String markup(VDealColumn instance) {
      //return the mapping
      return instance.columnRecord.getFirstValue();
    }
  }
  private VColumnRecord columnRecord;
  private List<VColumnRecord> additionalColumnRecords;
  private String columnValue;

  public VDealColumn(VColumnRecord columnRecord, String columnValue) {
    this();
    this.columnRecord = columnRecord;
    this.columnValue = columnValue;
  }

  public VDealColumn() {
    additionalColumnRecords = new ArrayList<VColumnRecord>();
  }

  @Marshallable(marshal = false)
  public List<VColumnRecord> getAdditionalColumnRecords() {
    return additionalColumnRecords;
  }

  public void setAdditionalColumnRecords(List<VColumnRecord> additionalColumnRecords) {
    this.additionalColumnRecords = additionalColumnRecords;
  }

  @Marshallable(marshal = false)
  public VColumnRecord getColumnRecord() {
    return columnRecord;
  }

  public void setColumnRecord(VColumnRecord columnRecord) {
    this.columnRecord = columnRecord;
  }

  @Marshallable(marshal = false)
  public String getColumnValue() {
    return columnValue;
  }

  public void setColumnValue(String columnValue) {
    this.columnValue = columnValue;
  }

  @Override
  public String toString() {
    return columnValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VDealColumn other = (VDealColumn) obj;
    if (this.columnRecord != other.columnRecord && (this.columnRecord == null || !this.columnRecord.equals(other.columnRecord))) {
      return false;
    }
    if (this.additionalColumnRecords != other.additionalColumnRecords && (this.additionalColumnRecords == null || !this.additionalColumnRecords.equals(other.additionalColumnRecords))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + (this.columnRecord != null ? this.columnRecord.hashCode() : 0);
    hash = 37 * hash + (this.additionalColumnRecords != null ? this.additionalColumnRecords.hashCode() : 0);
    return hash;
  }
}

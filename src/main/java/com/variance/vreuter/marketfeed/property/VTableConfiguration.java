/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.property;

import com.anosym.vjax.annotations.CollectionElement;
import com.anosym.vjax.annotations.Comment;
import com.anosym.vjax.annotations.Position;
import com.variance.vreuter.marketfeed.deal.VDealType;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.util.VColumnRecord;
import java.util.*;

/**
 *
 * @author Administrator
 */
public class VTableConfiguration {

    private String tableName;
    private List<VDealType> dealTypes;
    private List<VColumnRecord> columnRecordId;
    private Comparator<VColumnRecord> comp = new Comparator<VColumnRecord>() {

        @Override
        public int compare(VColumnRecord o1, VColumnRecord o2) {
            if (o1 == o2 && o1 == null) {
                return 0;
            }
            if (o1 == null || o1.getSecondValue() == null) {
                return -1;
            }
            if (o2 == null || o2.getSecondValue() == null) {
                return 1;
            }
            return Integer.valueOf(o1.getSecondValue().getDataIndex()).compareTo(o2.getSecondValue().getDataIndex());
        }
    };

    public VTableConfiguration() {
        columnRecordId = new ArrayList<VColumnRecord>();
        dealTypes = new ArrayList<VDealType>();
        for (VRecordResponseData vr : VRecordResponseData.values()) {
            columnRecordId.add(new VColumnRecord("", vr));
        }
        dealTypes.addAll(Arrays.asList(VDealType.basicValues()));
        tableName = "deal_table";
    }

    public VTableConfiguration(String tableName) {
        this();
        this.tableName = tableName;
    }

    @Comment("The deal types that will be inserted to this table")
    @CollectionElement("deal-type")
    public List<VDealType> getDealTypes() {
        return dealTypes;
    }

    /**
     * Returns null if the association is not defined
     *
     * @param responseData
     * @return
     */
    public VColumnRecord findDefinedAssociation(VRecordResponseData responseData) {
        for (VColumnRecord rec : columnRecordId) {
            if (rec.getSecondValue() == responseData) {
                return rec;
            }
        }
        return null;
    }

    public void setDealTypes(List<VDealType> dealTypes) {
        this.dealTypes.clear();
        this.dealTypes.addAll(dealTypes);
    }

    @Comment("The table columns associated with the dealing record. Delete any value not required")
    @CollectionElement("column-record")
    public List<VColumnRecord> getColumnRecordId() {
        return columnRecordId;
    }

    public void setColumnRecordId(List<VColumnRecord> columnRecordId) {
        System.out.println("Setting column record id: this#" + this.columnRecordId.hashCode() + ", set#" + columnRecordId.hashCode());
        this.columnRecordId = columnRecordId;
        Collections.sort(columnRecordId, comp);
    }

    @Comment("The table name used to record the dealing information")
    @Position(index = 0)
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}

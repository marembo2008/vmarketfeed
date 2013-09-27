/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal.managers;

import com.anosym.utilities.Duplex;
import com.variance.vreuter.marketfeed.deal.VDealColumn;
import com.variance.vreuter.marketfeed.deal.VDealType;
import com.variance.vreuter.marketfeed.deal.VMarketfeedDeal;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.property.VDataSourceConfiguration;
import com.variance.vreuter.marketfeed.property.VTableConfiguration;
import com.variance.vreuter.marketfeed.util.VColumnRecord;
import com.variance.vreuter.marketfeed.util.VOutpuFeedMonth;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marembo
 */
public abstract class VDealingManager implements VDealingManagerRemote {

  @Override
  public void handleConversation(Duplex<Integer, Map<Integer, String>> dealConversation) {
  }

  public boolean isMoneyMarketDeal(Map<Integer, String> deal) {
    VDataSourceConfiguration databaseConfiguration = getReuter().getReuterProperties().getDataSourceConfiguration();
    VRecordResponseData recordType = VRecordResponseData.PURE_DEAL_TYPE;
    String dealTypeStr = deal.get(recordType.getDataIndex());
    if (dealTypeStr == null) {
      return false;
    }
    int _dealType_ = Integer.parseInt(dealTypeStr);
    VDealType dealType = databaseConfiguration.findDealTypeInstance(_dealType_);
    return dealType.isMoneyMarketDeal();
  }

  @Override
  public void handleDeal(Duplex<Integer, Duplex<Integer, Map<Integer, String>>> dealInfo) {
    VDataSourceConfiguration databaseConfiguration = getReuter().getReuterProperties().getDataSourceConfiguration();
    Duplex<Integer, Map<Integer, String>> values = dealInfo.getSecondValue();
    Map<Integer, String> deal = values.getSecondValue();
    //then form two implementations of the vcolumn, one fx one mm
    VRecordResponseData recordType = VRecordResponseData.PURE_DEAL_TYPE;
    String dealTypeStr = deal.get(recordType.getDataIndex());
    if (dealTypeStr == null) {
      return;
    }
    int _dealType_ = Integer.parseInt(dealTypeStr);
    VDealType dealType = databaseConfiguration.findDealTypeInstance(_dealType_);
    VTableConfiguration vtc;
    if (isMoneyMarketDeal(deal)) {
      vtc = databaseConfiguration.getTableConfiguration(VDataSourceConfiguration.MM_TABLE_CONFIG_NAME);
    } else {
      vtc = databaseConfiguration.getTableConfiguration(VDataSourceConfiguration.FX_TABLE_CONFIG_NAME);
    }
    if (vtc != null) {
      VMarketfeedDeal marketfeedDeal;
      List<VDealColumn> dealColumns = new ArrayList<VDealColumn>();
      for (Integer id : deal.keySet()) {

        VColumnRecord assoc = vtc.findDefinedAssociation(VRecordResponseData.findInstance(id));

        if (assoc != null && assoc.getFirstValue() != null && !assoc.getFirstValue().isEmpty()) {
          if (assoc.getSecondValue() == VRecordResponseData.CURRENCY_2) {
//                        System.out.println("");
          }
          VDealColumn column = null;
          //have already set this column record?
          for (VDealColumn dc : dealColumns) {
            if (dc.getColumnRecord().getFirstValue().equalsIgnoreCase(assoc.getFirstValue())) {
              column = dc;
              column.getAdditionalColumnRecords().add(assoc);
              break;
            }
          }
          if (column == null) {
            column = new VDealColumn();
            dealColumns.add(column);
            column.setColumnValue("");
          }
          column.setColumnRecord(assoc);
          String value = deal.get(id);
          if ((assoc.isAlwaysUseDefaultValue() || value == null || value.isEmpty()) && assoc.getDefaultValue() != null) {
            value = assoc.getDefaultValue();
          }
          if (value != null && !value.isEmpty()) {
            //for dates, it must be properly normalized
            if (assoc.getSecondValue().isDateResponseData()) {
              //convert the value into date time
              String[] pps = value.trim().split(" ");
              //dd/mm/yyyy
              Calendar date = Calendar.getInstance();
              date.set(Integer.parseInt(pps[2]), VOutpuFeedMonth.valueOf(pps[1]).ordinal(), Integer.parseInt(pps[0]));
              column.setColumnValue(new VCalendarConverter().convert(date));
            } else if (assoc.getSecondValue().isTimeResponseData()) {
              //then we check if the column has a value already, then we merge them rather than replace
              String colValue = column.getColumnValue();
              if (colValue != null && !colValue.isEmpty()) {
                //merge the date and time
                //the date is already in iso format
                colValue += " " + value;
              } else {
                colValue = value;
              }
              column.setColumnValue(colValue);
            } else {
              column.setColumnValue(value);
            }
          }
        }
      }
      //find the VColumnRecords that do not have values
      List<VColumnRecord> colRecs = new ArrayList<VColumnRecord>(vtc.getColumnRecordId());
      for (ListIterator<VColumnRecord> it = colRecs.listIterator(); it.hasNext();) {
        VColumnRecord rec = it.next();
        if (rec.getFirstValue() == null || rec.getFirstValue().isEmpty() || rec.getSecondValue() == VRecordResponseData.TICKET_ID) {
          //if it is not associated: no data mapping
          it.remove();
          continue;
        }
        for (VDealColumn dealCol : dealColumns) {
          if (dealCol.getColumnRecord().getSecondValue() == rec.getSecondValue()) {
            it.remove();
            break;
          }
        }
      }
      if (!colRecs.isEmpty()) {
        for (VColumnRecord colRec : colRecs) {
          VDealColumn col = new VDealColumn(colRec, "");
          dealColumns.add(col);
        }
      }
      if (!dealColumns.isEmpty()) {
        //add the column record id
        VColumnRecord ticketId = vtc.findDefinedAssociation(VRecordResponseData.TICKET_ID);
        VDealColumn _ticketIdCol = null;
        if (ticketId != null) {
          _ticketIdCol = new VDealColumn(ticketId, dealInfo.getFirstValue() + "");
          dealColumns.add(0, _ticketIdCol);
        }
        marketfeedDeal = new VMarketfeedDeal(dealColumns);
        marketfeedDeal.setTicketIdColumn(_ticketIdCol);
        if (dealType != null) {
          try {
            /*
             * MM deal column should have 25 columns in order to
             * match with the RBM template Therefore we obtain
             * the number of currently defined columns as mapped
             * by the user the difference if any is supposed to
             * be automatically filled with blank valued columns
             * the method addEmptyColumn deal inserts a column
             * deal at the next available slot in the list
             */
            if (dealType.isMoneyMarketDeal()) {
              recordMoneyMarketDeal(marketfeedDeal);
            } else {
              recordForeignExchangeDeal(marketfeedDeal);
            }
          } catch (Exception e) {
            Logger.getLogger(VDealingManager.class.getName()).log(Level.SEVERE, dealTypeStr, e);
          }
        }
      }
    }
  }
}

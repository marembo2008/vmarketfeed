/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.anosym.utilities.Duplex;
import com.anosym.utilities.MapQueue;
import com.anosym.utilities.Utility;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VDealRecord;
import com.variance.vreuter.marketfeed.deal.VDealType;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.property.VDataSourceConfiguration;
import com.variance.vreuter.marketfeed.property.VTableConfiguration;
import com.variance.vreuter.marketfeed.service.VDataManager;
import com.variance.vreuter.marketfeed.service.VMarketfeedListener;
import com.variance.vreuter.marketfeed.util.VColumnRecord;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author marembo
 */
@Named("dataViewer")
@SessionScoped
public class VDealingManager implements Serializable, VMarketfeedListener {

  private List<List<VDealRecord>> currentMMDeals;
  private List<List<VDealRecord>> currentFXDeals;
  @EJB
  private VDataManager protocolService;
  @EJB
  private VReuterRemote reuter;
  private int activeIndex;

  @PostConstruct
  private void onStart() {
    try {
      protocolService.addMarketfeedListener(this);
      currentFXDeals = new ArrayList<List<VDealRecord>>();
      currentMMDeals = new ArrayList<List<VDealRecord>>();
    } catch (Exception e) {
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
    }
  }

  public void setActiveIndex(int activeIndex) {
  }

  public int getActiveIndex() {
    return activeIndex;
  }

  public void onTabChanged(TabChangeEvent tce) {
    Tab t = tce.getTab();
    String id = t.getId();
    System.out.println("Tab Id: " + id);
    if ("fx-deals".equals(id + "")) {
      activeIndex = 0;
    } else {
      activeIndex = 1;
    }
  }

  @Override
  public synchronized void dealForwarded(Duplex<Integer, Duplex<Integer, Map<Integer, String>>> forwardedDeal,
          MapQueue<Integer, Duplex<Integer, Map<Integer, String>>> remainingDeals) {
    System.out.println("Deals: " + forwardedDeal.getSecondValue().getSecondValue());
    //To avoid table generation in record, we parse the values here to a deal record, which will automatically have the column names and values.
    //add ticket
    VDataSourceConfiguration databaseConfiguration = reuter.getReuterProperties().getDataSourceConfiguration();
    VRecordResponseData recordType = VRecordResponseData.PURE_DEAL_TYPE;
    String dealTypeStr = forwardedDeal.getSecondValue().getSecondValue().get(recordType.getDataIndex());
    if (dealTypeStr == null) {
      return;
    }
    int _dealType_ = Integer.parseInt(dealTypeStr);
    VDealType dealType = databaseConfiguration.findDealTypeInstance(_dealType_);
    VTableConfiguration vtc;
    List<VDealRecord> records = new ArrayList<VDealRecord>();
    if (dealType.isMoneyMarketDeal()) {
      currentMMDeals.add(records);
      vtc = databaseConfiguration.getTableConfiguration(VDataSourceConfiguration.MM_TABLE_CONFIG_NAME);
    } else {
      currentFXDeals.add(records);
      vtc = databaseConfiguration.getTableConfiguration(VDataSourceConfiguration.FX_TABLE_CONFIG_NAME);
    }
    //ticket id is not supplied by the map.
    VColumnRecord ticketId = vtc.findDefinedAssociation(VRecordResponseData.TICKET_ID);
    if (ticketId != null) {
      records.add(new VDealRecord(ticketId, forwardedDeal.getFirstValue().toString()));
    }
    for (Integer i : forwardedDeal.getSecondValue().getSecondValue().keySet()) {
      VColumnRecord column = vtc.findDefinedAssociation(VRecordResponseData.findInstance(i));
      if (column != null && (!Utility.isNullOrEmpty(column.getFirstValue()) || column.isAlwaysUseDefaultValue())) {
        if (column.isAlwaysUseDefaultValue()) {
          records.add(new VDealRecord(column, column.getDefaultValue()));
        } else {
          records.add(new VDealRecord(column, getDealValue(forwardedDeal.getSecondValue().getSecondValue().get(i), i)));
        }
      }
    }
    //add any user defined association that has always defined field or which is missing from the records.
    for (VColumnRecord cr : getColumns(dealType.isMoneyMarketDeal())) {
      VDealRecord dr = new VDealRecord(cr, "");
      if (!records.contains(dr)) {
        records.add(dr);
      } else {
        //we get the current record for this column, and use the reference to check sequantial or serializable
        int ix = records.indexOf(dr);
        dr = records.get(ix);
      }
      if (cr.isAlwaysUseDefaultValue()) {
        if (cr.isSequential()) {
          dr.setValue(System.currentTimeMillis() + "");
        } else {
          dr.setValue(cr.getDefaultValue());
        }
      }
    }
    Collections.sort(records, new Comparator<VDealRecord>() {
      @Override
      public int compare(VDealRecord o1, VDealRecord o2) {
        return Integer.valueOf(o1.getColumn().getColumnIndex()).compareTo(o2.getColumn().getColumnIndex());
      }
    });
  }

  public List<VColumnRecord> getColumns(boolean isMM) {
    return isMM ? defaultMMColumns() : defaultFXColumns();
  }

  public List<List<VDealRecord>> getCurrentMMDeals() {
    return currentMMDeals;
  }

  public synchronized List<List<VDealRecord>> getCurrentFXDeals() {
    return currentFXDeals;
  }

  public boolean hasColumn(VColumnRecord columnRecord, boolean mm) {
    if (!mm) {
      for (VColumnRecord vc : defaultFXColumns()) {
        if (vc.getSecondValue() == columnRecord.getSecondValue()) {
          return true;
        }
      }
    } else {
      for (VColumnRecord vc : defaultMMColumns()) {
        if (vc.getSecondValue() == columnRecord.getSecondValue()) {
          return true;
        }
      }
    }
    return false;
  }

  private List<VColumnRecord> defaultFXColumns() {
    List<VColumnRecord> ss = new ArrayList<VColumnRecord>();
    VDataSourceConfiguration databaseConfiguration = reuter.getReuterProperties().getDataSourceConfiguration();
    VTableConfiguration vtc = databaseConfiguration.getTableConfiguration(VDataSourceConfiguration.FX_TABLE_CONFIG_NAME);
    for (VColumnRecord vcr : vtc.getColumnRecordId()) {
      if (!Utility.isNullOrEmpty(vcr.getFirstValue())) {
        ss.add(vcr);
      }
    }
    Collections.sort(ss);
    return ss;
  }

  public List<VColumnRecord> getFxColumns() {
    return defaultFXColumns();
  }

  private List<VColumnRecord> defaultMMColumns() {
    List<VColumnRecord> ss = new ArrayList<VColumnRecord>();
    VDataSourceConfiguration databaseConfiguration = reuter.getReuterProperties().getDataSourceConfiguration();
    VTableConfiguration vtc = databaseConfiguration.getTableConfiguration(VDataSourceConfiguration.MM_TABLE_CONFIG_NAME);
    for (VColumnRecord vcr : vtc.getColumnRecordId()) {
      if (!Utility.isNullOrEmpty(vcr.getFirstValue())) {
        ss.add(vcr);
      }
    }
    Collections.sort(ss);
    return ss;
  }

  public List<VColumnRecord> getMmColumns() {
    List<VColumnRecord> columnRecords = defaultMMColumns();
    System.out.println(columnRecords);
    return columnRecords;
  }

  public String getDealValue(String value, Integer id) {
    if (id == VRecordResponseData.DEAL_TYPE.getDataIndex()) {
      if (value.equals("B")) {
        return "Buy";
      } else if (value.equals("S")) {
        return "Sell";
      } else if (value.equals("L")) {
        return "Placement";
      } else if (value.equals("D")) {
        return "Deposit";
      }
      return value;
    }
    return value;
  }

  @Override
  public void conversationForwarded(Duplex<Integer, Map<Integer, String>> forwardedConversation, MapQueue<Integer, Map<Integer, String>> remainingConversations) {
  }
}

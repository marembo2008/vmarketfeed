/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal.managers;

import com.anosym.utilities.Duplex;
import com.flemax.jdynamics.configuration.JDataSourceType;
import com.flemax.jdynamics.configuration.JDatabaseConfiguration;
import com.flemax.jdynamics.database.JDatabase;
import com.flemax.jdynamics.database.JDatabaseService;
import com.flemax.jdynamics.database.JDatabaseTable;
import com.flemax.jdynamics.database.JTableColumn;
import com.flemax.jdynamics.database.JTableRow;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VDealColumn;
import com.variance.vreuter.marketfeed.deal.VMarketfeedDeal;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.property.VDataSourceConfiguration;
import com.variance.vreuter.marketfeed.property.VDataSourceConfiguration.VDataSource;
import com.variance.vreuter.marketfeed.property.VTableConfiguration;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 * Responsible for the creation of deals
 *
 * @author Administrator
 */
@Singleton(name = "databaseDealingManager")
@DependsOn("VReuter")
public class VDatabaseDealingManager extends VDealingManager {

  @EJB
  private VReuterRemote reuter;
  private JDatabase database;

  @PostConstruct
  @Override
  public void onStart() {
    startDealingManager();
  }

  @Override
  public void startDealingManager() {
    try {
      VDataSourceConfiguration datasourceConfiguration = reuter.getReuterProperties().getDataSourceConfiguration();
      VDataSource dataSource = null;
      for (VDataSource ds : datasourceConfiguration.getDataSourceTypes()) {
        if (ds.getDataSourceType() == JDataSourceType.DATABASE) {
          dataSource = ds;
          break;
        }
      }
      JDatabaseConfiguration conf = (JDatabaseConfiguration) dataSource.getConfigurations();
      database = JDatabaseService.createDatabaseInstance(conf);
      if (!database.isDatabaseExists()) {
        throw new RuntimeException("Specified Database does not exist");
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @PreDestroy
  @Override
  public void onDestroy() {
    if (database != null) {
      database.disconnect();
    }
  }

  @Override
  public void handleConversation(Duplex<Integer, Map<Integer, String>> dealConversation) {
    int ticketId = dealConversation.getFirstValue();
    Map<Integer, String> dealInfo = dealConversation.getSecondValue();
    VDataSourceConfiguration databaseConfiguration = reuter.getReuterProperties().getDataSourceConfiguration();
    //now lets get the conversation text information
    //then simply update the tables, or insert, which exists
    String tableName = databaseConfiguration.getConversationTable();
    JDatabaseTable convTable = database.findTable(tableName);
    if (convTable == null) {
      System.err.println("Conversation Text Table not specified");
      return;
    }
    if (dealInfo.isEmpty()) {
      return;
    }
    //we have only the ticket_id, and conversation_text column
    convTable.clear();
    JTableRow row = convTable.createRow();
    JTableColumn tkCol = row.getSettableColumn(databaseConfiguration.getConversationTextMapping().get(VRecordResponseData.TICKET_ID));
    if (tkCol != null) {
      tkCol.setColumnValue(ticketId);
      //find the other fields.
      //though for now it is only one field
      //we do it generically
      for (int vrd : dealInfo.keySet()) {
        VRecordResponseData vrData = VRecordResponseData.findInstance(vrd);
        if (vrData != null) {
          JTableColumn convCol = row.getSettableColumn(databaseConfiguration.getConversationTextMapping().get(vrData));
          if (convCol != null) {
            convCol.setColumnValue(dealInfo.get(vrd));
          }
        }
      }
      //save the table
      if (!convTable.isEmpty()) {
        database.persist(convTable);
      }
    }
  }

  @Override
  public VReuterRemote getReuter() {
    return reuter;
  }

  @Override
  public synchronized void recordForeignExchangeDeal(VMarketfeedDeal deal) {
    if (database.isInitialized()) {
      VTableConfiguration vtab = reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.FX_TABLE_CONFIG_NAME);
      JDatabaseTable jtab = this.database.findTable(vtab.getTableName());
      //create the ticket id first
      JTableRow row = jtab.createRow();
      if (row != null) {
        //set other columns
        for (VDealColumn vdCol : deal) {
          JTableColumn col = row.getSettableColumn(vdCol.getColumnRecord().getFirstValue());
          if (col != null) {
            col.setColumnValue(vdCol.getColumnValue());
          }
        }
        this.database.addRow(row);
        jtab.clear();
      }
    } else {
      System.err.println("Uninitialized database");
    }
  }

  @Override
  public synchronized void recordMoneyMarketDeal(VMarketfeedDeal deal) {
    if (database.isInitialized()) {
      VTableConfiguration vtab = reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.MM_TABLE_CONFIG_NAME);
      JDatabaseTable jtab = this.database.findTable(vtab.getTableName());
      //set other columns
      JTableRow row = jtab.createRow();
      if (row != null) {
        for (VDealColumn vdCol : deal) {
          JTableColumn col = row.getSettableColumn(vdCol.getColumnRecord().getFirstValue());
          if (col != null) {
            col.setColumnValue(vdCol.getColumnValue());
          }

        }
        this.database.addRow(row);
        jtab.clear();
      }
    } else {
      System.err.println("Uninitialized database");
    }
  }
}

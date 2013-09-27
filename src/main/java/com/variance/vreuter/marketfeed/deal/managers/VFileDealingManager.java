/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal.managers;

import com.anosym.utilities.FormattedCalendar;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VDealColumn;
import com.variance.vreuter.marketfeed.deal.VMarketfeedDeal;
import com.variance.vreuter.marketfeed.property.VDataSourceConfiguration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 *
 * @author Marembo
 */
@Singleton(name = "fileDealingManager")
@DependsOn("VReuter")
public class VFileDealingManager extends VDealingManager {

  @EJB
  private VReuterRemote reuter;
  private FileOutputStream marketfeedFXStream;
  private FileOutputStream marketfeedMMStream;
  private File currentFXFile;
  private File currentMMFile;

  private File getFXFile() {
    File dir = new File(reuter.getReuterProperties().getFileDirectory());
    VDataSourceConfiguration dbConfig = reuter.getReuterProperties().getDataSourceConfiguration();
    String fx_table = dbConfig.getTableConfiguration(VDataSourceConfiguration.VTableKey.FX_TABLE_CONFIG_NAME).getTableName();
    File fx_dir = new File(dir, "foreign-exchange");
    if (!fx_dir.exists()) {
      fx_dir.mkdirs();
    }
    Calendar c = Calendar.getInstance();
    String timestamp = FormattedCalendar.toISOString(c);
    timestamp = timestamp.replaceAll("\\D", "");
    String fx_file = fx_table + "_" + timestamp + ".txt";
    File fx = new File(fx_dir, fx_file);
    return fx;
  }

  private File getMMFile() {
    File dir = new File(reuter.getReuterProperties().getFileDirectory());
    VDataSourceConfiguration dbConfig = reuter.getReuterProperties().getDataSourceConfiguration();
    String mm_table = dbConfig.getTableConfiguration(VDataSourceConfiguration.VTableKey.MM_TABLE_CONFIG_NAME).getTableName();
    File mm_dir = new File(dir, "money-market");
    if (!mm_dir.exists()) {
      mm_dir.mkdirs();
    }
    Calendar c = Calendar.getInstance();
    String timestamp = FormattedCalendar.toISOString(c);
    timestamp = timestamp.replaceAll("\\D", "");
    String fx_file = mm_table + "_" + timestamp + ".txt";
    File fx = new File(mm_dir, fx_file);
    return fx;
  }

  @Override
  @PreDestroy
  public void onDestroy() {
    try {
      if (this.marketfeedFXStream != null) {
        this.marketfeedFXStream.close();
        this.marketfeedFXStream = null;
      }
      if (this.marketfeedMMStream != null) {
        this.marketfeedMMStream.close();
        this.marketfeedMMStream = null;
      }
    } catch (IOException ex) {
      Logger.getLogger(VFileDealingManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void recordForeignExchangeDeal(VMarketfeedDeal deal) {
    for (VDealColumn dc : deal) {
      if (dc.getColumnRecord().isAlwaysUseDefaultValue() && dc.getColumnRecord().isSequential()) {
        dc.setColumnValue(System.currentTimeMillis() + "");
      }
    }
    System.err.println("");
    String seperator = reuter.getReuterProperties().getDataSourceConfiguration().getDataSeperator();
    if (seperator == null) {
      seperator = ";";
    }
    try {
      deal.sort();
      String values = "";
      for (VDealColumn col : deal) {
        if (!values.isEmpty()) {
          values += seperator;
        }
        values += col.getColumnValue();
      }
      getFXStream().write(values.getBytes());
      getFXStream().write("\n".getBytes());
    } catch (IOException ex) {
      Logger.getLogger(VFileDealingManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private FileOutputStream getMMStream() {
    File file = getMMFile();
    if (currentMMFile == null
            || (currentMMFile != null
            && currentMMFile.length() > (1024 * 1024)
            && !file.equals(currentMMFile))) {
      currentMMFile = file;
      try {
        if (marketfeedMMStream != null) {
          try {
            marketfeedMMStream.close();
          } catch (Exception e) {
          }
        }
        marketfeedMMStream = new FileOutputStream(file, true);
      } catch (FileNotFoundException ex) {
        Logger.getLogger(VFileDealingManager.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return marketfeedMMStream;
  }

  private FileOutputStream getFXStream() {
    File file = getFXFile();
    if (currentFXFile == null
            || (currentFXFile != null
            && currentFXFile.length() > (1024 * 1024)
            && !file.equals(currentFXFile))) {
      currentFXFile = file;
      try {
        if (marketfeedFXStream != null) {
          try {
            marketfeedFXStream.close();
          } catch (Exception e) {
          }
        }
        marketfeedFXStream = new FileOutputStream(file, true);
      } catch (FileNotFoundException ex) {
        Logger.getLogger(VFileDealingManager.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return marketfeedFXStream;
  }

  @Override
  public void recordMoneyMarketDeal(VMarketfeedDeal deal) {
    for (VDealColumn dc : deal) {
      if (dc.getColumnRecord().isAlwaysUseDefaultValue() && dc.getColumnRecord().isSequential()) {
        dc.setColumnValue(System.currentTimeMillis() + "");
      }
    }
    String seperator = reuter.getReuterProperties().getDataSourceConfiguration().getDataSeperator();
    if (seperator == null) {
      seperator = ";";
    }
    try {
      deal.sort();
      String values = "";
      for (VDealColumn col : deal) {
        if (!values.isEmpty()) {
          values += seperator;
        }
        values += col.getColumnValue();
      }
      getMMStream().write(values.getBytes());
      getMMStream().write("\n".getBytes());
    } catch (IOException ex) {
      Logger.getLogger(VFileDealingManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void startDealingManager() {
  }

  @Override
  public VReuterRemote getReuter() {
    return reuter;
  }

  @Override
  public void onStart() {
  }
}

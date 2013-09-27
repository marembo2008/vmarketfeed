/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal.managers;

import com.anosym.vjax.VMarshaller;
import com.anosym.vjax.VXMLBindingException;
import com.anosym.vjax.xml.VDocument;
import com.anosym.vjax.xml.VNamespace;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VMarketfeedDeal;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 *
 * @author Marembo
 */
@Singleton(name = "xmlDealingManager")
@DependsOn("VReuter")
public class VXmlDealingManager extends VDealingManager {

  @EJB
  private VReuterRemote reuter;
  private VMarshaller<VMarketfeedDeal> marshaller;
  private File moneyMarketPath;
  private File foreignExchangePath;

  @Override
  @PostConstruct
  public void onStart() {
    System.setProperty(VNamespace.DEFUALT_NAMESPACE_PROPERTY_BINDING, "false");
    marshaller = new VMarshaller<VMarketfeedDeal>();
    String path = reuter.getReuterProperties().getFileDirectory();
    this.moneyMarketPath = new File(path, "money-market");
    this.foreignExchangePath = new File(path, "foreign-exchange");
    if (!moneyMarketPath.exists()) {
      moneyMarketPath.mkdirs();

    }
    if (!foreignExchangePath.exists()) {
      foreignExchangePath.mkdirs();

    }
  }

  @Override
  @PreDestroy
  public void onDestroy() {
    //nothing more to care about
    marshaller = null;
  }

  @Override
  public void startDealingManager() {
  }

  @Override
  public VReuterRemote getReuter() {
    return reuter;
  }

  @Override
  public void recordForeignExchangeDeal(VMarketfeedDeal deal) {
    try {
      File docPath = new File(foreignExchangePath, deal.getTicketIdColumn().getColumnValue() + ".fx");
      VDocument doc = this.marshaller.marshallDocument(deal);
      doc.setDocumentName(docPath);
      doc.writeDocument();
    } catch (VXMLBindingException ex) {
      Logger.getLogger(VXmlDealingManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void recordMoneyMarketDeal(VMarketfeedDeal deal) {
    try {
      File docPath = new File(moneyMarketPath, deal.getTicketIdColumn().getColumnValue() + ".mm");
      VDocument doc = this.marshaller.marshallDocument(deal);
      doc.setDocumentName(docPath);
      doc.writeDocument();
    } catch (VXMLBindingException ex) {
      Logger.getLogger(VXmlDealingManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}

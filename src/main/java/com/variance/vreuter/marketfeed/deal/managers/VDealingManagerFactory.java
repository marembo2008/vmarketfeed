/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal.managers;

import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VDealing;
import com.variance.vreuter.marketfeed.property.VDataSourceConfiguration.VDataSource;
import java.util.List;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;

/**
 *
 * @author marembo
 */
@Singleton
@DependsOn("VReuter")
public class VDealingManagerFactory {

  private VDealingManagerRemote dealingManager;
  @EJB
  private VReuterRemote reuter;

  @Produces
  @VDealing
  public VDealingManagerRemote createInstance(
          @New VFileDealingManager fileDealingManager,
          @New VXmlDealingManager xmlDealingManager,
          @New VDatabaseDealingManager databaseDealingManager) {
    List<VDataSource> dataSourceTypes = reuter.getReuterProperties().getDataSourceConfiguration().getDataSourceTypes();
    for (VDataSource dst : dataSourceTypes) {
      if (dst.getDataSourceType().isActive()) {
        switch (dst.getDataSourceType()) {
          case DATABASE:
            dealingManager = databaseDealingManager;
            break;
          case XLS:
          case FLAT_FILE:
            dealingManager = fileDealingManager;
            break;
          case XML:
            dealingManager = xmlDealingManager;
            break;
        }
        break;
      }
    }
    if (dealingManager != null) {
      dealingManager.startDealingManager();
    }
    return dealingManager;
  }
}

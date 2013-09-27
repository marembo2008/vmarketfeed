/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal.managers;

import com.anosym.utilities.Duplex;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VMarketfeedDeal;
import java.util.Map;

/**
 * The dealing manager must be responsible for updating the latest deal ticket received from the
 *
 * @author Administrator
 */
public interface VDealingManagerRemote {

  public VReuterRemote getReuter();

  @javax.annotation.PostConstruct
  public void onStart();

  @javax.annotation.PreDestroy
  public void onDestroy();

  public void handleConversation(Duplex<Integer, Map<Integer, String>> dealConversation);

  public void handleDeal(Duplex<Integer, Duplex<Integer, Map<Integer, String>>> dealInfo);

  /**
   * This cannot be called in the start method so that when dealing manager fails to initialize it
   * may not cause the system to fail. This is important especially when the specified dealing
   * manager is instantiated but is not needed
   */
  public void startDealingManager();

  public void recordForeignExchangeDeal(VMarketfeedDeal deal);

  public void recordMoneyMarketDeal(VMarketfeedDeal deal);
}

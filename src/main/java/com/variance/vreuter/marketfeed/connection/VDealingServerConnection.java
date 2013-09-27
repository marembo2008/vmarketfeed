/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.connection;

import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.service.VDataManager;
import javax.ejb.Remote;

/**
 * Simple connection
 *
 * @author Administrator
 */
@Remote
public interface VDealingServerConnection {

  public void onStart();

  public void onDestroy();

  void setDataManager(VDataManager dataManager);

  void setReuterManager(VReuterRemote reuterManager);
}

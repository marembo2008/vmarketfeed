/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import java.io.Serializable;
import javax.annotation.PreDestroy;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 *
 * @author Administrator
 */
@WebListener
public class VSessionListener implements Serializable, HttpSessionListener {

//  @Inject
//  private VLoginManager loginManager;

  /**
   * Used to simply instantiate a session
   *
   * @return
   */
  public boolean isRendered() {
//    try {
//      this.loginManager.removeUser();
//    } catch (Exception e) {
//    }
//    return true;
    return true;
  }

  @PreDestroy
  public void onDestroy() {
//    loginManager.toLogin();
  }

  @Override
  public void sessionCreated(HttpSessionEvent se) {
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
//    HttpSession session = se.getSession();
//    if (session != null) {
//      loginManager.handleSessionDestroyed(session.getId());
//    }
  }
}

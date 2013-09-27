/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Administrator
 */
@Named("navManager")
@Stateless
public class VNavigationManager {

  @Inject
  private VLoginManagerRemote loginManager;

  public String toHome() {
    if (!loginManager.isSessionActive()) {
      return "loginpage?faces-redirect=true";
    }
    return "TicketDetails?faces-redirect=true";
  }

  public String toContactUs() {
    if (!loginManager.isSessionActive()) {
      return "loginpage?faces-redirect=true";
    }
    return "ContactUsPage?faces-redirect=true";
  }

  public String toProperty() {
    if (!loginManager.isSessionActive()) {
      return "loginpage?faces-redirect=true";
    }
    return "ReuterProperty?faces-redirect=true";
  }

  public String getHome() {
    if (!loginManager.isSessionActive()) {
      return "loginpage?faces-redirect=true";
    }
    return "TicketDetails?faces-redirect=true";
  }

  public String getContactUs() {
    if (!loginManager.isSessionActive()) {
      return "loginpage?faces-redirect=true";
    }
    return "ContactUsPage?faces-redirect=true";
  }

  public String getProperty() {
    if (!loginManager.isSessionActive()) {
      return "loginpage?faces-redirect=true";
    }
    if (loginManager.isAdministrator()) {
      return "ReuterProperty?faces-redirect=true";
    }
    return null;
  }
}

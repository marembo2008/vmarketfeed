/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.anosym.jflemax.validation.annotation.Principal;
import com.variance.vreuter.marketfeed.web.util.Credential;

/**
 *
 * @author Administrator
 */
public interface VLoginManagerRemote {

  void login(Credential credentials);

  void onStart();

  String createUser();

  @Principal
  Credential getCurrentCredential();

  public com.variance.vreuter.marketfeed.web.util.Credential getCredential();

  public void setCredential(com.variance.vreuter.marketfeed.web.util.Credential credential);

  public java.lang.String login();

  public java.lang.String getCurrentYear();

  public com.variance.vreuter.marketfeed.web.util.Credential getNewUser();

  public void setNewUser(com.variance.vreuter.marketfeed.web.util.Credential newUser);

  public boolean isAdministrator();

  public java.lang.String doCreateUser();

  public com.variance.vreuter.marketfeed.web.util.ContactInformation getContactInformation();

  public void setContactInformation(com.variance.vreuter.marketfeed.web.util.ContactInformation contactInformation);

  public java.lang.String sendQuery();

  public java.lang.String toLogin();

  public java.lang.String getCurrentSessionUser();

  void removeUser();

  public void handleSessionDestroyed(String sessionId);

  public boolean isSessionActive();
}

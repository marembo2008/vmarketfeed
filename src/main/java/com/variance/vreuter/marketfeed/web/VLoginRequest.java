/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.variance.vreuter.marketfeed.web.util.Credential;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 *
 * @author marembo
 */
@Named(value = "loginRequest")
@RequestScoped
public class VLoginRequest {

  @Inject
  private VLoginManager loginManager;
  private Credential credential;

  public void setCredential(Credential credential) {
    this.credential = credential;
  }

  public Credential getCredential() {
    return credential != null ? credential : (credential = new Credential());
  }

  public String login() {
    try {
      loginManager.setCredential(credential);
      return loginManager.login();
    } finally {
      credential = null;
    }
  }
}

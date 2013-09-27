/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web.util;

import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public final class Credential implements Serializable {

  private String userName;
  private String userPassword;
  private boolean administrator;

  public Credential() {
  }

  public Credential(String userName, String userPassword) {
    this(userName, userPassword, false);
  }

  public Credential(String userName, String userPassword, boolean administrator) {
    this.setUserName(userName);
    this.setUserPassword(userPassword);
    this.administrator = administrator;
  }

  public boolean isAdministrator() {
    return administrator;
  }

  public void setAdministrator(boolean administrator) {
    this.administrator = administrator;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserPassword() {
    return userPassword;
  }

  public void setUserPassword(String userPassword) {
    this.userPassword = userPassword;
  }

  @Override
  public String toString() {
    return "Credential{\n" + "userName=" + userName + "\nuserPassword=" + userPassword + "\nadministrator=" + administrator + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Credential other = (Credential) obj;
    if ((this.userName == null) ? (other.userName != null) : !this.userName.equals(other.userName)) {
      return false;
    }
    if ((this.userPassword == null) ? (other.userPassword != null) : !this.userPassword.equals(other.userPassword)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + (this.userName != null ? this.userName.hashCode() : 0);
    hash = 79 * hash + (this.userPassword != null ? this.userPassword.hashCode() : 0);
    return hash;
  }
}

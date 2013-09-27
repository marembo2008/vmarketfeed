/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.property;

import com.anosym.vjax.annotations.EnumMarkup;


/**
 *
 * @author Administrator
 */
@EnumMarkup("connection-type")
public enum VServerConnectionType {

  SERIAL_CONNECTION(false, new VSerialConnectionProperty()),
  SOCKET_CONNECTION(true, new VSocketConnectionProperty());

  private VServerConnectionType(boolean active, VConnectionProperty connectionProperty) {
    this.active = active;
    this.connectionProperty = connectionProperty;
  }
  private boolean active;
  private VConnectionProperty connectionProperty;

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public VConnectionProperty getConnectionProperty() {
    return connectionProperty;
  }

  public void setConnectionProperty(VConnectionProperty connectionProperty) {
    this.connectionProperty = connectionProperty;
  }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal;

/**
 *
 * @author Administrator
 */
public class VDealType {

  public static VDealType FOREX_OUTRIGHT = new VDealType(4, "Forex Outright");
  public static VDealType FOREX_SWAP = new VDealType(8, "Forex Swap");
  public static VDealType FOREX_FRA = new VDealType(32, "Forward Rate Agreement");
  public static VDealType FOREX_SPOT = new VDealType(2, "Forex Spot");
  public static VDealType FOREX_DEPOSIT = new VDealType(16, "Forex Deposit");
  public static VDealType UNKNOWN_DEAL = new VDealType(-90, "Unknown Deal Type");

  public VDealType() {
  }

  public VDealType(int dealIndex, String description) {
    this.dealIndex = dealIndex;
    this.description = description;
  }

  public boolean isMoneyMarketDeal() {
    //System.err.println("deal type check "+this.getDealIndex()+" "+this.getDescription());
    if (this.equals(VDealType.FOREX_DEPOSIT) || this.equals(VDealType.FOREX_FRA)) {
      return true;
    }
    return false;
  }

  public static VDealType[] basicValues() {
    VDealType[] types = {FOREX_DEPOSIT, FOREX_FRA, FOREX_OUTRIGHT, FOREX_SPOT, FOREX_SWAP, UNKNOWN_DEAL};
    return types;
  }

  public String getDescription() {
    return description;
  }

  public void setDealIndex(int dealIndex) {
    this.dealIndex = dealIndex;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getDealIndex() {
    return dealIndex;
  }

  @Override
  public String toString() {
    return description;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VDealType other = (VDealType) obj;
    if (this.dealIndex != other.dealIndex) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 37 * hash + this.dealIndex;
    hash = 37 * hash + (this.description != null ? this.description.hashCode() : 0);
    return hash;
  }
  private int dealIndex;
  private String description;
}

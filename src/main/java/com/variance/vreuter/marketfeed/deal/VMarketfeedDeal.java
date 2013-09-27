/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal;

import com.anosym.vjax.annotations.Markup;
import com.anosym.vjax.annotations.Marshallable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class is immutable
 *
 * @author Marembo
 */
@Markup(name = "CREATECONTRACT_FSFS_REQ", property = "VMarketfeedDeal")
public final class VMarketfeedDeal implements Iterable<VDealColumn> {

  private List<VDealColumn> marketfeedDealDetails;
  private ListIterator<VDealColumn> dealIterator;
  private VDealColumn ticketIdColumn;

  public VMarketfeedDeal(List<VDealColumn> marketfeedDealDetails) {
    this.marketfeedDealDetails = new ArrayList<VDealColumn>(marketfeedDealDetails);
    this.sort();
  }

  /*
   * This method inserts the dealColumn in the first non occupied slot in the list.
   * it is used to enter empty columns to achieve the correct sequence and count of columns
   */
  public void addEmptyDealColumn(VDealColumn dealColumn) {

    int prevIndex = -1;
    int colIndex = 0;
    for (int i = 0; i < marketfeedDealDetails.size(); i++) {
      VDealColumn column = marketfeedDealDetails.get(i);
      colIndex = column.getColumnRecord().getColumnIndex();
      if (prevIndex == -1) {
        if (colIndex != 0) {
          dealColumn.getColumnRecord().setColumnIndex(0);
          marketfeedDealDetails.add(0, dealColumn);
          return;
        } else {
          prevIndex = colIndex;
        }
      } else {
        if ((colIndex - prevIndex) > 1) {
          dealColumn.getColumnRecord().setColumnIndex(prevIndex + 1);
          marketfeedDealDetails.add(i, dealColumn);
          return;
        } else {
          prevIndex++;
        }
      }
    }
    dealColumn.getColumnRecord().setColumnIndex(colIndex + 1);
    marketfeedDealDetails.add(dealColumn);
  }

  public int getNumberOfColumns() {
    return marketfeedDealDetails.size();
  }

  @Marshallable(marshal = false)
  public VDealColumn getTicketIdColumn() {
    return ticketIdColumn;
  }

  public void setTicketIdColumn(VDealColumn ticketIdColumn) {
    this.ticketIdColumn = ticketIdColumn;
  }

  public void sort() {
    Collections.sort(marketfeedDealDetails, new Comparator<VDealColumn>() {
      @Override
      public int compare(VDealColumn o1, VDealColumn o2) {
        return o1.getColumnRecord().compareTo(o2.getColumnRecord());
      }
    });
  }

  @com.anosym.vjax.annotations.Iterable
  public VDealColumn getNext() {
    if (marketfeedDealDetails != null) {
      if (dealIterator == null) {
        dealIterator = marketfeedDealDetails.listIterator();
      }
      if (dealIterator.hasNext()) {
        return dealIterator.next();
      }
      return null;
    }
    return null;
  }

  public void reset() {
    this.dealIterator = null;
  }

  @Override
  public Iterator<VDealColumn> iterator() {
    return this.marketfeedDealDetails.listIterator();
  }
}

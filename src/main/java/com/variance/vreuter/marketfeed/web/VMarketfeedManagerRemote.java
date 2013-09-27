/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.anosym.utilities.Duplex;
import com.anosym.utilities.MapQueue;
import com.variance.vreuter.marketfeed.service.VMarketfeedListener;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public interface VMarketfeedManagerRemote extends VMarketfeedListener, Serializable {

    @Override
    public void dealForwarded(Duplex<Integer, Duplex<Integer, Map<Integer, String>>> forwardedDeal, MapQueue<Integer, Duplex<Integer, Map<Integer, String>>> remainingDeals);

    @Override
    public void conversationForwarded(Duplex<Integer, Map<Integer, String>> forwardedConversation, MapQueue<Integer, Map<Integer, String>> remainingConversations);

    public List<Integer> getReceivedConversationTickets();

    public List<Integer> getReceivedTickets();

    public List<Integer> getUpdatedConversationTickets();

    public Map<Integer, Map<Integer, String>> getReceivedConversationTicketDeals();

    public Map<Integer, Map<Integer, String>> getReceivedTicketDeals();

    public int getSelectedTicketDeal();

    public void setSelectedTicketDeal(int selectedTicketDeal);

    public Map<Integer, Map<Integer, String>> getUpdatedConversationTicketDeals();

    public Map<Integer, String> getUpdatedTicketDeals();

    public List<Integer> getUpdatedTickets();

    public boolean isShowTicketConversation();

    public boolean isShowTicketDetails();

    public void setMarketfeedDetailData(com.variance.vreuter.marketfeed.web.VMarketfeedManager.MarketfeedDetailData marketfeedDetailData);

    public com.variance.vreuter.marketfeed.web.VMarketfeedManager.MarketfeedDetailData getMarketfeedDetailData();

    public java.util.List<com.variance.vreuter.marketfeed.web.VMarketfeedManager.MarketfeedDetailData> getMarketfeedDetailDataList();

    public void showSelectedTicket();

    public boolean isUpdatesAvailable();

    public java.util.List<java.lang.Integer> getTicket();

    public java.lang.String getColumnValue(int recordId);

    public boolean isTicketDealsAvailable();

    public java.util.List<java.lang.String> getHeaderValues();

    public java.lang.String getConversationValue(int tkId, java.lang.String recordId);

    public java.lang.String getDetailValue(int tkId, java.lang.String recordId);

    public void lastTickets();

    public void firstTickets();

    public void previousTickets();

    public void nextTickets();

    public boolean isEnableLastTicket();

    public boolean isEnableFirstTicket();

    public boolean isEnablePreviousTicket();

    public boolean isEnableNextTicket();

    @javax.annotation.PostConstruct
    public void onStart();

    public boolean isShowSingleTicket();

    public boolean isShowAllTickets();

    public void setDealAwaitingUpdate();

    public boolean isDealAwaitingUpdate();

    public void setDealUpdated();

    public boolean isDealUpdate();

    public com.variance.vreuter.marketfeed.web.VMarketfeedManager.MarketfeedDeal getMarketfeedDeal();

    public void setMarketfeedDeal(com.variance.vreuter.marketfeed.web.VMarketfeedManager.MarketfeedDeal marketfeedDeal);

    public com.variance.vreuter.marketfeed.web.VMarketfeedManager.MarketfeedDeal[] getMarketfeedDeals();

    public com.variance.vreuter.marketfeed.web.VMarketfeedManager.TicketDisplay getTicketDisplay();

    public void setTicketDisplay(com.variance.vreuter.marketfeed.web.VMarketfeedManager.TicketDisplay ticketDisplay);

    public com.variance.vreuter.marketfeed.web.VMarketfeedManager.TicketDisplay[] getTicketDisplays();

    public void searchPerformed();

    public int getSearchedTicket();

    public void setSearchedTicket(int searchedTicket);

    public java.lang.String getTicketDescription();

    public void setTicketDescription(java.lang.String ticketDescription);

    public void showUpdatedTickets();

    public void showAwaitingUpdateTickets();

    public void startSession();

    public void endSession();

    public int getNumberOfTickets();

    public java.util.List<com.variance.vreuter.marketfeed.deal.VDealColumn> getSelectedTicketDetails();

    public java.util.List<com.variance.vreuter.marketfeed.util.VColumnRecord> getTicketColumns();
}

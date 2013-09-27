/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.anosym.utilities.Constraint;
import com.anosym.utilities.ConstraintArrayList;
import com.anosym.utilities.Duplex;
import com.anosym.utilities.MapQueue;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VDealColumn;
import com.variance.vreuter.marketfeed.deal.VDealType;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.property.VDataSourceConfiguration;
import com.variance.vreuter.marketfeed.property.VTableConfiguration;
import com.variance.vreuter.marketfeed.util.VColumnRecord;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Administrator
 */
@ApplicationScoped
@Singleton
@Startup
@Named("viewManager")
public class VMarketfeedManager implements VMarketfeedManagerRemote {

  public static enum MarketfeedDetailData {

    TICKET_DETAILS("Ticket Details"),
    TICKET_CONVERSATION("Ticket Conversation");
    private String description;

    private MarketfeedDetailData(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    @Override
    public String toString() {
      return description;
    }
  }

  public static enum MarketfeedDeal {

    TICKET_DEAL_UPDATED("Tickets Updated"),
    TICKET_DEAL_AWAITING_UPDATE("Tickets Awaiting Update");
    private String description;

    private MarketfeedDeal(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    @Override
    public String toString() {
      return getDescription();
    }
  }

  public static enum TicketDisplay {

    ALL_TICKETS("Display All Tickets"),
    SINGLE_TICKET("Display Single Ticket");
    private String description;

    private TicketDisplay(String description) {
      this.description = description;
    }

    @Override
    public String toString() {
      return description;
    }
  }

  private static class SessionData {

    int currentUpdatedTicketIndex;
    int searchedTicket;
    boolean updateTicketView = true;
    int selectedTicketDeal;
    MarketfeedDeal marketfeedDeal;
    TicketDisplay ticketDisplay;
    String ticketDescription;
    int ticketMax = TICKET_MAX_LIST;

    public SessionData() {
      marketfeedDeal = MarketfeedDeal.TICKET_DEAL_UPDATED;
      ticketDisplay = TicketDisplay.SINGLE_TICKET;
      ticketDescription = "Updated Tickets";
      searchedTicket = 1; //by defualt
    }
  }
  //updated updatedTickets
  private List<Integer> updatedTickets;
  //ticket number - updated ticket deal pair
  private Map<Integer, Map<Integer, String>> updatedTicketDeals;
  //updatedTickets received but not updated
  private List<Integer> receivedTickets;
  //ticket number - received ticket deal pair
  private Map<Integer, Map<Integer, String>> receivedTicketDeals;
  //updated conversation updatedTickets
  private List<Integer> updatedConversationTickets;
  //ticket number - updated conversation ticket deal pair
  private Map<Integer, Map<Integer, String>> updatedConversationTicketDeals;
  //conversation updatedTickets received but not updated
  private List<Integer> receivedConversationTickets;
  //ticket number - received conversation ticket deal pair
  private Map<Integer, Map<Integer, String>> receivedConversationTicketDeals;
  private MarketfeedDetailData marketfeedDetailData;
  private static final int TICKET_MAX_LIST = 5;
  private static final int TICKET_MAX_TABLE = 10;
  @EJB
  private VReuterRemote reuter;
  private Map<String, SessionData> sessionData;

  private String getSessionId() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
    if (httpSession == null) {
      return null;
    }
    return httpSession.getId();
  }

  @Override
  public void startSession() {
    String sessionId = getSessionId();
    if (!sessionData.containsKey(sessionId)) {
      SessionData sd = new SessionData();
      if (!updatedTickets.isEmpty()) {
        sd.selectedTicketDeal = updatedTickets.get(0);
      }
      sessionData.put(sessionId, sd);
    }
  }

  @Override
  public void endSession() {
    String sessionId = getSessionId();
    if (sessionData.containsKey(sessionId)) {
      sessionData.remove(sessionId);
    }
  }

  @PostConstruct
  @Override
  public void onStart() {
    System.err.println("Created Marketfeed Manager instance........................");
    updatedTickets = new ConstraintArrayList<Integer>(new Constraint<Integer>() {

      @Override
      public boolean accept(Integer element) {
        return !updatedTickets.contains(element);
      }
    });
    receivedTickets = new ConstraintArrayList<Integer>(new Constraint<Integer>() {

      @Override
      public boolean accept(Integer element) {
        return !receivedTickets.contains(element);
      }
    });
    updatedTicketDeals = new HashMap<Integer, Map<Integer, String>>();
    receivedTicketDeals = new HashMap<Integer, Map<Integer, String>>();
    updatedConversationTickets = new ConstraintArrayList<Integer>(new Constraint<Integer>() {

      @Override
      public boolean accept(Integer element) {
        return !updatedConversationTickets.contains(element);
      }
    });
    receivedConversationTickets = new ConstraintArrayList<Integer>(new Constraint<Integer>() {

      @Override
      public boolean accept(Integer element) {
        return !receivedConversationTickets.contains(element);
      }
    });
    updatedConversationTicketDeals = new HashMap<Integer, Map<Integer, String>>();
    receivedConversationTicketDeals = new HashMap<Integer, Map<Integer, String>>();
    marketfeedDetailData = MarketfeedDetailData.TICKET_DETAILS;
    sessionData = new HashMap<String, SessionData>();
  }

  @Override
  public void showUpdatedTickets() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      sd.updateTicketView = true;
      sd.ticketDescription = "Updated Tickets";
    }
  }

  @Override
  public void showAwaitingUpdateTickets() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      sd.updateTicketView = true;
      sd.ticketDescription = "Tickets Awaiting Updates";
    }
  }

  @Override
  public int getNumberOfTickets() {
    return updatedTickets.size();
  }

  @Override
  public String getTicketDescription() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      return sd.ticketDescription;
    }
    return null;
  }

  @Override
  public void setTicketDescription(String ticketDescription) {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      sd.ticketDescription = ticketDescription;
    }
  }

  @Override
  public int getSearchedTicket() {
    try {
      return sessionData.get(getSessionId()).searchedTicket;
    } catch (Exception e) {
      return 0;
    }
  }

  @Override
  public void setSearchedTicket(int searchedTicket) {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      sd.searchedTicket = searchedTicket;
      this.setSelectedTicketDeal(searchedTicket);
    }
  }

  @Override
  public synchronized void searchPerformed() {
    //also determine the range
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      int tkIndex = updatedTickets.indexOf(sd.selectedTicketDeal);
      sd.currentUpdatedTicketIndex = (tkIndex / sd.ticketMax) * sd.ticketMax;
      this.setTicketDisplay(TicketDisplay.SINGLE_TICKET);
    }
  }

  @Override
  public TicketDisplay getTicketDisplay() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      return sd.ticketDisplay;
    }
    return TicketDisplay.SINGLE_TICKET;
  }

  @Override
  public void setTicketDisplay(TicketDisplay ticketDisplay) {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      sd.ticketDisplay = ticketDisplay;
      sd.ticketMax = (sd.ticketDisplay == TicketDisplay.ALL_TICKETS) ? TICKET_MAX_TABLE : TICKET_MAX_LIST;
    }
  }

  @Override
  public TicketDisplay[] getTicketDisplays() {
    return TicketDisplay.values();
  }

  @Override
  public MarketfeedDeal[] getMarketfeedDeals() {
    return MarketfeedDeal.values();
  }

  @Override
  public MarketfeedDeal getMarketfeedDeal() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      return sd.marketfeedDeal;
    }
    return MarketfeedDeal.TICKET_DEAL_UPDATED;
  }

  @Override
  public void setMarketfeedDeal(MarketfeedDeal marketfeedDeal) {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      sd.marketfeedDeal = marketfeedDeal;
    }
  }

  @Override
  public boolean isDealUpdate() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      boolean value = sd.marketfeedDeal == MarketfeedDeal.TICKET_DEAL_UPDATED;
      return value;
    }
    return false;
  }

  @Override
  public void setDealUpdated() {
//        this.dealDetails = !(this.dealConversations = true);
  }

  @Override
  public boolean isDealAwaitingUpdate() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      return sd.marketfeedDeal == MarketfeedDeal.TICKET_DEAL_AWAITING_UPDATE;
    }
    return false;
  }

  @Override
  public void setDealAwaitingUpdate() {
//        this.dealConversations = !(this.dealDetails = true);
  }

  @Override
  public boolean isShowAllTickets() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      return sd.ticketDisplay == TicketDisplay.ALL_TICKETS;
    }
    return false;
  }

  @Override
  public boolean isShowSingleTicket() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      return sd.ticketDisplay == TicketDisplay.SINGLE_TICKET;
    }
    return false;
  }

  @Override
  public synchronized void dealForwarded(Duplex<Integer, Duplex<Integer, Map<Integer, String>>> forwardedDeal, MapQueue<Integer, Duplex<Integer, Map<Integer, String>>> remainingDeals) {
    //if anything, we remove the current deal from the deals awaiting update

    if (this.updatedTickets.add(forwardedDeal.getFirstValue())) {
      //we put it rather than offer or add so that if it already exists, it will be updated rather than added
      this.updatedTicketDeals.put(forwardedDeal.getFirstValue(), forwardedDeal.getSecondValue().getSecondValue());
      //update the status of the received updatedTickets and received deals
      this.receivedTickets.remove(forwardedDeal.getFirstValue());
      this.receivedTicketDeals.remove(forwardedDeal.getFirstValue());
    }
    if (remainingDeals != null) {//then add the received updatedTickets
      for (Integer i : remainingDeals) {
        this.receivedTicketDeals.put(i, remainingDeals.get(i).getSecondValue());
      }
    }
  }

  @Override
  public synchronized void conversationForwarded(Duplex<Integer, Map<Integer, String>> forwardedConversation, MapQueue<Integer, Map<Integer, String>> remainingConversations) {
    //if anything, we remove the current deal from the deals awaiting update
    if (this.updatedConversationTickets.add(forwardedConversation.getFirstValue())) {
      //we put it rather than offer or add so that if it already exists, it will be updated rather than added
      this.updatedConversationTicketDeals.put(forwardedConversation.getFirstValue(), forwardedConversation.getSecondValue());
      //update the status of the received updatedTickets and received deals
      this.receivedConversationTickets.remove(forwardedConversation.getFirstValue());
      this.receivedConversationTicketDeals.remove(forwardedConversation.getFirstValue());
    }
    if (remainingConversations != null) {//then add the received updatedTickets
      for (Integer i : remainingConversations) {
        this.receivedConversationTicketDeals.put(i, remainingConversations.get(i));
      }
    }
  }

  @Override
  public List<MarketfeedDetailData> getMarketfeedDetailDataList() {
    return new ArrayList<MarketfeedDetailData>(Arrays.asList(MarketfeedDetailData.values()));
  }

  @Override
  public MarketfeedDetailData getMarketfeedDetailData() {
    return marketfeedDetailData;
  }

  @Override
  public void setMarketfeedDetailData(MarketfeedDetailData marketfeedDetailData) {
    this.marketfeedDetailData = marketfeedDetailData;
  }

  @Override
  public boolean isShowTicketDetails() {
    return !isShowAllTickets() && (marketfeedDetailData == MarketfeedDetailData.TICKET_DETAILS);
  }

  @Override
  public boolean isShowTicketConversation() {
    return !isShowAllTickets() && (marketfeedDetailData == MarketfeedDetailData.TICKET_CONVERSATION) && !reuter.getReuterProperties().getDataSourceConfiguration().isDealAndConversationCombined();
  }

  @Override
  public synchronized List<Integer> getReceivedConversationTickets() {
    return new ArrayList<Integer>(receivedConversationTickets);
  }

  @Override
  public synchronized List<Integer> getReceivedTickets() {
    return updatedTickets;
  }

  @Override
  public synchronized List<Integer> getUpdatedConversationTickets() {
    return new ArrayList<Integer>(updatedConversationTickets);
  }

  @Override
  public synchronized boolean isEnableNextTicket() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd != null) {
      int s = this.updatedTickets.size();
      return !(s > 0 && s > (sd.currentUpdatedTicketIndex + sd.ticketMax));
    }
    return false;
  }

  @Override
  public synchronized boolean isEnablePreviousTicket() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return false;
    }
    int s = this.updatedTickets.size();
    int l = s / sd.ticketMax;
    return !(s > 0 && (l > 1 || (l * sd.ticketMax) < s) && (sd.currentUpdatedTicketIndex >= sd.ticketMax));
  }

  @Override
  public synchronized boolean isEnableFirstTicket() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return false;
    }
    int s = this.updatedTickets.size();
    return !(s > 0 && (sd.currentUpdatedTicketIndex >= sd.ticketMax) && s > sd.ticketMax);
  }

  @Override
  public synchronized boolean isEnableLastTicket() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return false;
    }
    int s = this.updatedTickets.size();
    int l = s / sd.ticketMax;
    return !(s > 0 && (l > 1 || (l * sd.ticketMax) < s) && (sd.currentUpdatedTicketIndex + sd.ticketMax) < s);
  }

  @Override
  public synchronized void nextTickets() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return;
    }
    if (updatedTickets.size() > (sd.currentUpdatedTicketIndex + sd.ticketMax)) {
      sd.currentUpdatedTicketIndex += sd.ticketMax;
    }
  }

  @Override
  public void previousTickets() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return;
    }
    if (sd.currentUpdatedTicketIndex >= sd.ticketMax) {
      sd.currentUpdatedTicketIndex -= sd.ticketMax;
    }
  }

  @Override
  public void firstTickets() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return;
    }
    sd.currentUpdatedTicketIndex = 0;
  }

  @Override
  public synchronized void lastTickets() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return;
    }
    int s = updatedTickets.size();
    if (s > sd.ticketMax) {
      //divide it according to the ticket max
      int len = s / sd.ticketMax;
      if ((len * sd.ticketMax) == s) {
        sd.currentUpdatedTicketIndex = s - sd.ticketMax;
      } else {
        sd.currentUpdatedTicketIndex = len * sd.ticketMax;
      }
    }
  }

  @Override
  public synchronized Map<Integer, Map<Integer, String>> getReceivedConversationTicketDeals() {
    Map<Integer, Map<Integer, String>> map = new HashMap<Integer, Map<Integer, String>>();
    for (int key : receivedConversationTicketDeals.keySet()) {
      map.put(key, new HashMap<Integer, String>(receivedConversationTicketDeals.get(key)));
    }
    return map;
  }

  @Override
  public synchronized Map<Integer, Map<Integer, String>> getReceivedTicketDeals() {
    Map<Integer, Map<Integer, String>> map = new HashMap<Integer, Map<Integer, String>>();
    for (int key : receivedTicketDeals.keySet()) {
      map.put(key, new HashMap<Integer, String>(receivedTicketDeals.get(key)));
    }
    return map;
  }

  @Override
  public synchronized int getSelectedTicketDeal() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return 0;
    }
    if (updatedTickets.isEmpty()) {
      return sd.selectedTicketDeal;
    }
    if (sd.selectedTicketDeal == 0) {
      return updatedTickets.get(0);
    }
    return sd.selectedTicketDeal;
  }

  @Override
  public synchronized List<VDealColumn> getSelectedTicketDetails() {
    List<VDealColumn> cols = new ArrayList<VDealColumn>();
    List<VColumnRecord> recs = getTicketColumns();
    for (VColumnRecord r : recs) {
      VDealColumn col = new VDealColumn(r, getDetailValue(getSelectedTicketDeal(), r.getFirstValue()));
      cols.add(col);
    }
    return cols;
  }

  @Override
  public synchronized List<VColumnRecord> getTicketColumns() {
    List<VColumnRecord> records = new ArrayList<VColumnRecord>();
    Map<Integer, String> vals = getUpdatedTicketDeals();
    if (vals == null || vals.isEmpty()) {
      return reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.FX_TABLE_CONFIG_NAME).getColumnRecordId();
      //return records;
    }
    for (int i : vals.keySet()) {
      VRecordResponseData data = VRecordResponseData.findInstance(i);
      if (data != null) {
        VColumnRecord rec = null;
        if (isMMDeal(vals)) {
          rec = reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.MM_TABLE_CONFIG_NAME).findDefinedAssociation(data);
        } else {
          rec = reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.FX_TABLE_CONFIG_NAME).findDefinedAssociation(data);
        }
        if (rec != null) {
          records.add(rec);
        }
      }
    }
    return records;
  }

  @Override
  public synchronized void setSelectedTicketDeal(int selectedTicketDeal) {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return;
    }
    sd.selectedTicketDeal = selectedTicketDeal;
  }

  @Override
  public synchronized Map<Integer, Map<Integer, String>> getUpdatedConversationTicketDeals() {
    Map<Integer, Map<Integer, String>> map = new HashMap<Integer, Map<Integer, String>>();
    for (int key : updatedConversationTicketDeals.keySet()) {
      map.put(key, new HashMap<Integer, String>(updatedConversationTicketDeals.get(key)));
    }
    return map;
  }

  @Override
  public synchronized Map<Integer, String> getUpdatedTicketDeals() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return new HashMap<Integer, String>();
    }
    Map<Integer, String> details = updatedTicketDeals.get(sd.selectedTicketDeal);
    return getSeparatedTicketDeals(details);
  }

  private Map<Integer, String> getSeparatedTicketDeals(final Map<Integer, String> dealTicket) {
    if (dealTicket == null || dealTicket.isEmpty()) {
      return dealTicket;
    }
    Map<Integer, String> deal = Collections.synchronizedMap(new HashMap<Integer, String>(dealTicket));
    // Since the deal ticket may be modified outside of our reach
    // and we also want to remove elements from the deal map. Iterating over it
    // and removing elements in it will only cause ConcurrentModificationException
    // so we create a tmp map to iterate over, as we remove elements from the deal map.
    Map<Integer, String> dealTmp = Collections.synchronizedMap(new HashMap<Integer, String>(dealTicket));
    synchronized (deal) {
      boolean isMM = isMMDeal(deal);
      VTableConfiguration tableConfig;
      if (isMM) {
        tableConfig = reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.MM_TABLE_CONFIG_NAME);
      } else {
        tableConfig = reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.FX_TABLE_CONFIG_NAME);
      }
      List<VColumnRecord> columnRecords = tableConfig.getColumnRecordId();
      for (Iterator<Integer> it = dealTmp.keySet().iterator(); it.hasNext();) {
        int id = it.next();
        VRecordResponseData vrrd = VRecordResponseData.findInstance(id);
        boolean isMMField = false;
        for (VColumnRecord columnRecord : columnRecords) {
          if (columnRecord.getSecondValue() == vrrd) {
            isMMField = true;
            break;
          }
        }
        if (!isMMField) {
          deal.remove(id);
        }
      }
    }
    return deal;

  }

  private synchronized boolean isMMDeal(Map<Integer, String> deal) {
    VRecordResponseData recordType = VRecordResponseData.PURE_DEAL_TYPE;
    if (deal == null || deal.isEmpty()) {
      return false;
    }
    String dealType = deal.get(recordType.getDataIndex());
    if (dealType != null) {
      if (dealType != null) {
        try {
          int _dealType_ = Integer.parseInt(dealType);
          VDealType vdt = reuter.getReuterProperties().getDataSourceConfiguration().findDealTypeInstance(_dealType_);
          if (vdt != null) {
            return vdt.isMoneyMarketDeal();
          }
        } catch (Exception e) {
        }
      }
    }
    return false;
  }

  @Override
  public synchronized String getDetailValue(int tkId, String recordId) {
    if (tkId == 0 || recordId == null || recordId.isEmpty()) {
      return null;
    }
    int id = Integer.parseInt(recordId);
    Map<Integer, String> val = updatedTicketDeals.get(tkId);
    if (val == null || val.isEmpty()) {
      return null;
    }
    if (id == VRecordResponseData.PURE_DEAL_TYPE.getDataIndex()) {
      String type = val.get(id);
      int dId = Integer.parseInt(type);
      return reuter.getReuterProperties().getDataSourceConfiguration().findDealTypeInstance(dId) + "";
    } else if (id == VRecordResponseData.DEAL_TYPE.getDataIndex()) {
      String type = val.get(id);
      int dId = Integer.parseInt(type);
      switch (dId) {
        case 1:
          return "Buy";
        case 2:
          return "Sell";
        case 3:
          return "Buy and Sell";
        default:
          return type;
      }
    }
    return val.get(id);
  }

  @Override
  public synchronized String getConversationValue(int tkId, String recordId) {
    if (tkId == 0 || recordId == null || recordId.isEmpty()) {
      return null;
    }
    int id = Integer.parseInt(recordId);
    Map<Integer, String> val = updatedConversationTicketDeals.get(tkId);
    if (val == null || val.isEmpty()) {
      return null;
    }
    String text = val.get(id);
    String[] parts = text.split("#");
    String conv = "";
    //TKT EDIT OF CNV 2606 BY USRI 1456GMT 05SEP2001 ^ WE SELL 20 MIO GBP AT 1.5010 GBP/USD ^ STATUS CONFIRMED ^
    int maxLen = 80;
    for (String t : parts) {
      if (t.trim().isEmpty()) {
        continue;
      }
      if (t.length() > maxLen) {
        //divide it to smaller sizes
        int ix = 0;
        int ixM = maxLen;
        while (t.length() > ixM) {
          conv += (t.substring(ix, ixM) + "<br>");
          ix = ixM;
          ixM += maxLen;
        }
        if (t.length() < ix) {
          conv += t.substring(ix) + "<br>";
        }
      } else {
        conv += (t + "<br>");
      }
    }
    return conv;
  }

  @Override
  public synchronized List<String> getHeaderValues() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return new ArrayList<String>();
    }
    if (sd.selectedTicketDeal == 0) {
      return new ArrayList<String>();
    }
    return new ArrayList<String>(getUpdatedTicketDeals().values());
  }

  @Override
  public synchronized boolean isTicketDealsAvailable() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return false;
    }
    boolean cnd = false;
    if (sd.selectedTicketDeal == 0) {
      cnd = false;
    } else {
      Map<Integer, String> m = updatedTicketDeals.get(sd.selectedTicketDeal);
      cnd = (m != null && !m.isEmpty());
    }
    return cnd;
  }

  @Override
  public synchronized String getColumnValue(int recordId) {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return null;
    }
    if (updatedTicketDeals.isEmpty()) {
      return null;
    }
    Map<Integer, String> val = updatedTicketDeals.get(sd.selectedTicketDeal);
    if (val == null || val.isEmpty()) {
      return null;
    }
    return val.get(recordId);
  }

  @Override
  public synchronized List<Integer> getUpdatedTickets() {
//        VRecordResponseData dealType = vre
    return updatedTickets;
//        SessionData sd = this.sessionData.get(getSessionId());
//        if (sd == null) {
//            return null;
//        }
//        int max = sd.currentUpdatedTicketIndex + sd.ticketMax;
//        if (max > updatedTickets.size()) {
//            max = updatedTickets.size();
//        }
//        if (max < sd.currentUpdatedTicketIndex) {
//            return null;
//        }
//        return updatedTickets.subList(sd.currentUpdatedTicketIndex, max);
  }

  @Override
  public List<Integer> getTicket() {
    SessionData sd = this.sessionData.get(getSessionId());
    if (sd == null) {
      return null;
    }
    List<Integer> tk = new ArrayList<Integer>();
    if (sd.selectedTicketDeal > 0) {
      tk.add(sd.selectedTicketDeal);
    }
    return tk;
  }

  @Override
  public synchronized boolean isUpdatesAvailable() {
    return !updatedTickets.isEmpty();
  }

  @Override
  public void showSelectedTicket() {
  }

  public double getTicketTotals() {
//        for(int i: updatedTickets){
//            Map<Integer, String> val = receivedTicketDeals.get(i);
//            String value = val.get(VRecordResponseData.)
//        }
    return 0.0;
  }
}

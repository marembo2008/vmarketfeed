/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.service;

import com.anosym.utilities.Duplex;
import com.anosym.utilities.HashMapQueue;
import com.anosym.utilities.MapQueue;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VDealing;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.deal.managers.VDatabaseDealingManager;
import com.variance.vreuter.marketfeed.property.VReuterProperty.VTicketRequestCriterion;
import static com.variance.vreuter.marketfeed.service.VStatusResponse.NO_SUCH_NAME;
import com.variance.vreuter.marketfeed.web.VMarketfeedManagerRemote;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * The class is responsible for managing the message protocols. it encodes and decodes the market
 * feed protocols and forwards the relevant results to the dealing manager
 *
 * @author Administrator
 */
@Singleton
@Startup
public class VMarketfeedProtocolService implements VDataManager {

  @EJB
  private VReuterRemote reuter;
  /**
   * Responsible for creating the deals from the deal information and setting them into the
   * data-source
   */
  @EJB
  @VDealing
  private VDatabaseDealingManager dealingManager;
  private int index0 = 1;
  private int index1 = 0;
  private final byte[] REQUEST_TAG = {65, 65};
  private static final int FIELD_LIST_DATABASE_STATUS = 500;
  private static final int FIELD_LIST_CONVERSATION_TEXT = 504;
//    private Map<String, String> ticketTagInformation;
  /**
   * Data to be forwarded to the dealing server
   */
  private volatile MapQueue<Integer, byte[]> serviceData;
  /**
   * Data from the TOF server to be decoded
   */
  private volatile Queue<byte[]> feedData;
  /*
   * The dealing data field values. The deal manager decodes the data and then
   * saves it to the database
   */
  private volatile MapQueue<Integer, Duplex<Integer, Map<Integer, String>>> dealInformationQueue;
  private volatile MapQueue<Integer, Map<Integer, String>> dealConversationQueue;
  private volatile MapQueue<Integer, Duplex<Integer, Map<Integer, String>>> marketfeedDealInformationQueue;
  private volatile MapQueue<Integer, Map<Integer, String>> marketfeedDealConversationQueue;
  /**
   * Listeners to be notified periodically of current state of the deals received from the server To
   * add this services for web based feed, include this instance as a managed property.
   */
  private List<VMarketfeedListener> marketfeedListeners;
  private boolean errorInitializing;
  private boolean initialized;
  @EJB
  private VMarketfeedManagerRemote marketfeedManager;

  @PostConstruct
  @Override
  public void onStart() {
    System.err.println("Created Marketfeed Protocol Service..............");
    serviceData = new HashMapQueue<Integer, byte[]>();
    feedData = new LinkedList<byte[]>();
    this.dealInformationQueue = new HashMapQueue<Integer, Duplex<Integer, Map<Integer, String>>>();
    this.dealConversationQueue = new HashMapQueue<Integer, Map<Integer, String>>();
    this.marketfeedDealInformationQueue = new HashMapQueue<Integer, Duplex<Integer, Map<Integer, String>>>();
    this.marketfeedDealConversationQueue = new HashMapQueue<Integer, Map<Integer, String>>();
    marketfeedListeners = new ArrayList<VMarketfeedListener>();
    this.addMarketfeedListener(marketfeedManager);
  }

  @Override
  public synchronized void addMarketfeedListener(VMarketfeedListener marketfeedListener) {
    if (!marketfeedListeners.contains(marketfeedListener)) {
      marketfeedListeners.add(marketfeedListener);
    }
  }

  @Override
  public boolean isErrorInInitialization() {
    return errorInitializing && reuter.getReuterProperties().getTicketRequestCriterion().isRequestAllAvailableTickets();
  }

  @Override
  public void removeMarketfeedListener(VMarketfeedListener marketfeedListener) {
    marketfeedListeners.remove(marketfeedListener);
  }

  private synchronized void updateDealListeners(Duplex<Integer, Duplex<Integer, Map<Integer, String>>> deal) {
    for (VMarketfeedListener l : marketfeedListeners) {
      l.dealForwarded(deal, null);
    }
  }

  private synchronized void updateConversationListeners(Duplex<Integer, Map<Integer, String>> conversation) {
    for (VMarketfeedListener l : marketfeedListeners) {
      l.conversationForwarded(conversation, null);
    }
  }

  public String getDealValue(String value, Integer id) {
    if (id == VRecordResponseData.DEAL_TYPE.getDataIndex()) {
      int dId = Integer.parseInt(value);
      switch (dId) {
        case 1:
          return "B";
        case 2:
          return "S";
        case 5:
        case 7:
          return "L";
        case 6:
        case 8:
          return "D";
        default:
          return value;
      }
    }
    return value;
  }

  public Duplex<Integer, Duplex<Integer, Map<Integer, String>>>[] processBuyAndSellDeals(
          Duplex<Integer, Duplex<Integer, Map<Integer, String>>> deal) {
    for (Integer id : deal.getSecondValue().getSecondValue().keySet()) {
      if (id == VRecordResponseData.DEAL_TYPE.getDataIndex()) {
        String dealType = deal.getSecondValue().getSecondValue().get(id);
        int dId = Integer.parseInt(dealType);
        switch (dId) {
          case 1:
          case 2:
          case 5:
          case 7:
          case 6:
          case 8:
            String value = deal.getSecondValue().getSecondValue().get(id);
            value = getDealValue(value, id);
            deal.getSecondValue().getSecondValue().put(id, value);
            return new Duplex[]{deal};
          case 3: {
            //Buy and Sell.
            Duplex<Integer, Duplex<Integer, Map<Integer, String>>> buyDeal = new Duplex<Integer, Duplex<Integer, Map<Integer, String>>>();
            Duplex<Integer, Duplex<Integer, Map<Integer, String>>> sellDeal = new Duplex<Integer, Duplex<Integer, Map<Integer, String>>>();
            buyDeal.setFirstValue(deal.getFirstValue());
            sellDeal.setFirstValue(deal.getFirstValue());
            Duplex<Integer, Map<Integer, String>> buyDealTypes = new Duplex<Integer, Map<Integer, String>>();
            Duplex<Integer, Map<Integer, String>> sellDealTypes = new Duplex<Integer, Map<Integer, String>>();
            buyDealTypes.setFirstValue(deal.getSecondValue().getFirstValue());
            sellDealTypes.setFirstValue(deal.getSecondValue().getFirstValue());
            buyDeal.setSecondValue(buyDealTypes);
            sellDeal.setSecondValue(sellDealTypes);
            Map<Integer, String> buyDealOptions = new HashMap<Integer, String>();
            Map<Integer, String> sellDealOptions = new HashMap<Integer, String>();
            buyDealOptions.put(514, "B");
            sellDealOptions.put(514, "S");
            buyDealTypes.setSecondValue(buyDealOptions);
            sellDealTypes.setSecondValue(sellDealOptions);
            for (Map.Entry<Integer, String> e : deal.getSecondValue().getSecondValue().entrySet()) {
              if (e.getKey() == 542 || e.getKey() == 514) { //we do not need to set the deal type here again.
                continue;
              } else if (e.getKey() == 541) {
                buyDealOptions.put(542, e.getValue());
                sellDealOptions.put(542, e.getValue());
              } else if (e.getKey() == 522) {
                buyDealOptions.put(522, e.getValue());
              } else if (e.getKey() == 523) {
                sellDealOptions.put(522, e.getValue());
              } else if (e.getKey() == 525 || e.getKey() == 526) {
                buyDealOptions.put(e.getKey(), e.getValue());
              } else if (e.getKey() == 527 || e.getKey() == 528) {
                sellDealOptions.put(e.getKey() - 2, e.getValue());
              } else {
                buyDealOptions.put(e.getKey(), e.getValue());
                sellDealOptions.put(e.getKey(), e.getValue());
              }
            }
            return new Duplex[]{buyDeal, sellDeal};
          }
          case 4: {
            //Sell and Buy.
            Duplex<Integer, Duplex<Integer, Map<Integer, String>>> buyDeal = new Duplex<Integer, Duplex<Integer, Map<Integer, String>>>();
            Duplex<Integer, Duplex<Integer, Map<Integer, String>>> sellDeal = new Duplex<Integer, Duplex<Integer, Map<Integer, String>>>();
            buyDeal.setFirstValue(deal.getFirstValue());
            sellDeal.setFirstValue(deal.getFirstValue());
            Duplex<Integer, Map<Integer, String>> buyDealTypes = new Duplex<Integer, Map<Integer, String>>();
            Duplex<Integer, Map<Integer, String>> sellDealTypes = new Duplex<Integer, Map<Integer, String>>();
            buyDealTypes.setFirstValue(deal.getSecondValue().getFirstValue());
            sellDealTypes.setFirstValue(deal.getSecondValue().getFirstValue());
            buyDeal.setSecondValue(buyDealTypes);
            sellDeal.setSecondValue(sellDealTypes);
            Map<Integer, String> buyDealOptions = new HashMap<Integer, String>();
            Map<Integer, String> sellDealOptions = new HashMap<Integer, String>();
            buyDealOptions.put(514, "B");
            sellDealOptions.put(514, "S");
            buyDealTypes.setSecondValue(buyDealOptions);
            sellDealTypes.setSecondValue(sellDealOptions);
            for (Map.Entry<Integer, String> e : deal.getSecondValue().getSecondValue().entrySet()) {
              if (e.getKey() == 542 || e.getKey() == 514) {
                continue;
              } else if (e.getKey() == 541) {
                buyDealOptions.put(542, e.getValue());
                sellDealOptions.put(542, e.getValue());
              } else if (e.getKey() == 522) {
                sellDealOptions.put(522, e.getValue());
              } else if (e.getKey() == 523) {
                buyDealOptions.put(522, e.getValue());
              } else if (e.getKey() == 525 || e.getKey() == 526) {
                sellDealOptions.put(e.getKey(), e.getValue());
              } else if (e.getKey() == 527 || e.getKey() == 528) {
                buyDealOptions.put(e.getKey() - 2, e.getValue());
              } else {
                buyDealOptions.put(e.getKey(), e.getValue());
                sellDealOptions.put(e.getKey(), e.getValue());
              }
            }
            return new Duplex[]{buyDeal, sellDeal};
          }
        }

      }
      //at the same time, lets check if it is Buy and Sell or Sell and Buy.
    }
    return new Duplex[]{deal};
  }

  private void forwardDeals(Duplex<Integer, Duplex<Integer, Map<Integer, String>>> deal) {
    try {
      //we process this deal, specifically for RBM by splitting Sell and Buy and Buy and Sell deals
      //to Buy deals and Sell deals.
      for (Duplex<Integer, Duplex<Integer, Map<Integer, String>>> d : processBuyAndSellDeals(deal)) {
        //if an exception occurs, lets not update the deal listeners to.
        dealingManager.handleDeal(d);
        reuter.getReuterProperties().getTicketRequestCriterion().setFromTicketId(deal.getFirstValue());
        reuter.doUpdate(); //we do asynchronous update here
        updateDealListeners(d);
      }
    } catch (Exception e) {
      Logger.getLogger(VMarketfeedProtocolService.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  private void forwardConversations(Duplex<Integer, Map<Integer, String>> conversation) {
    try {
      dealingManager.handleConversation(conversation);
      updateConversationListeners(conversation);
    } catch (Exception e) {
      Logger.getLogger(VMarketfeedProtocolService.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  /**
   * Called to indicate to the data manager that no more request can be accepted
   */
  @Override
  public void marketfeedServiceDisconnected() {
  }

  /**
   * generates a unique 2-character string used as a TAG. based on the ticket id
   */
  private String generateTagIdentifier() {
    if (index1 == 26) {
      index1 = 0;
      index0++;
    }
    if (index0 == 26) {
      index0 = 0;
    }
    if (index0 == 0 && index1 == 0) {
      index1++;
    }
    byte[] bbs = {(byte) (65 + (index0 % 26)), (byte) (65 + (index1 % 26))};
    index1++;
    String s = new String(bbs);
    return s;
  }

  /**
   * Requests a deal information
   *
   * @param ticketNumber
   * @return
   */
  private byte[] requestDealInformation(int ticketNumber, String tagId) {
    //<FS>333<US>tag<GS>tcid # ticket number<FS>
    String tcid = reuter.getReuterProperties().getDealingServerTCID() + "#" + ticketNumber;
    byte[] request = {VSeparatorCharacterConstants.FS, 51, 51, 51, VSeparatorCharacterConstants.US};
    //add the tag identifier
    byte[] tagBs = tagId.getBytes();
    int i = request.length;
    request = Arrays.copyOf(request, request.length + tagBs.length + 1);
    System.arraycopy(tagBs, 0, request, i, tagBs.length);
    //add the GS
    request[request.length - 1] = VSeparatorCharacterConstants.GS;
    //add the ticket tcid
    byte[] _tcid = tcid.getBytes();
    byte[] _request = new byte[_tcid.length + request.length + 1];
    System.arraycopy(request, 0, _request, 0, request.length);
    System.arraycopy(_tcid, 0, _request, request.length, _tcid.length);
    _request[_request.length - 1] = VSeparatorCharacterConstants.FS;
    return _request;
  }

  /**
   * Requests a conversation text and returns a tag identifying the request
   *
   * @param ticketNumber
   * @return
   */
  private byte[] requestConversationtext(int ticketNumber, String tagId) {
    //<FS>333<US>tag<GS>tcid # ticket number<FS>
    String tcid = reuter.getReuterProperties().getDealingServerTCID() + "#" + ticketNumber + "C";
    byte[] request = {VSeparatorCharacterConstants.FS, 51, 51, 51, VSeparatorCharacterConstants.US};
    //add the tag identifier
    byte[] tagBs = tagId.getBytes();
    int i = request.length;
    request = Arrays.copyOf(request, request.length + tagBs.length + 1);
    System.arraycopy(tagBs, 0, request, i, tagBs.length);
    //add the GS
    request[request.length - 1] = VSeparatorCharacterConstants.GS;
    //add the ticket tcid
    byte[] _tcid = tcid.getBytes();
    byte[] _request = new byte[_tcid.length + request.length + 1];
    System.arraycopy(request, 0, _request, 0, request.length);
    System.arraycopy(_tcid, 0, _request, request.length, _tcid.length);
    _request[_request.length - 1] = VSeparatorCharacterConstants.FS;
    return _request;
  }

  /**
   * Requests a database status information and returns a tag identifying the request
   *
   * @return
   */
  private byte[] requestDatabaseStatusInformation() {
    //<FS>332<US>tag<GS>tcid # INFO<FS>
    String tcid = reuter.getReuterProperties().getDealingServerTCID() + "#INFO";
    byte[] _tcid = tcid.getBytes();
    byte[] request = {VSeparatorCharacterConstants.FS, 51, 51, 50, VSeparatorCharacterConstants.US, 65, 65, VSeparatorCharacterConstants.GS};
    byte[] _request = new byte[_tcid.length + request.length + 1];
    System.arraycopy(request, 0, _request, 0, request.length);
    System.arraycopy(_tcid, 0, _request, request.length, _tcid.length);
    _request[_request.length - 1] = VSeparatorCharacterConstants.FS;
    return _request;
  }

  /**
   * Formats the data by adding STX, ETX and BCC
   *
   * @param data
   * @return
   */
  @Override
  public byte[] format(byte[] data) {
    byte[] fData = new byte[data.length + 3];
    System.arraycopy(data, 0, fData, 1, data.length + 3);
    fData[0] = VSeparatorCharacterConstants.STX;
    fData[fData.length - 2] = VSeparatorCharacterConstants.ETX;
    byte checksum = 0;
    for (byte b : fData) {
      if (b != VSeparatorCharacterConstants.STX) {
        checksum += b;
      }
    }
    System.out.println(checksum);
    String binaryStream = Integer.toBinaryString(Math.abs(checksum));
    System.out.println(binaryStream);
    //set the most significant bit to 0
    binaryStream = "0" + binaryStream.substring(1);
    System.out.println(binaryStream);
    checksum = Byte.parseByte(binaryStream, 2);
    System.out.println(checksum);
    fData[fData.length - 1] = checksum;
    return null;
  }

  @Override
  public void handleData(byte[] data) {
    if (data == null) {
      return;
    }
//    System.out.println(new String(data));
    Duplex<VMarketfeedMessageType, Integer> type = findMessageType(data);
    switch (type.getFirstValue()) {
      case RECORD_RESPONSE:
        initialized = true;
        errorInitializing = false;
        decode340(type.getSecondValue(), data);
        break;
      case UPDATE:
        decode316(type.getSecondValue(), data);
        break;
      case STATUS_RESPONSE:
        handleStatusResponse(type.getSecondValue(), data);
      default:
        System.err.println("Invalid Server Response:  " + type.getFirstValue());
    }
  }

  private void handleStatusResponse(int i, byte[] data) {
    if (!initialized) {
      errorInitializing = true;
    }
    //here we simply log this errors for tcp/ip connection
    String tag;
    int statusId;
    String statusMessage;
    //read tag
    int ix = i;
    for (; ix < data.length; ix++) {
      if (data[ix] == VSeparatorCharacterConstants.GS) {
        break;
      }
    }
    byte[] tagBs = new byte[ix - i];
    System.arraycopy(data, i, tagBs, 0, ix - i);
    tag = new String(tagBs);
    i = ++ix;
    //read the next byte, assume that we may not have single byte for the id
    for (; ix < data.length; ix++) {
      if (data[ix] == VSeparatorCharacterConstants.RS || data[ix] == VSeparatorCharacterConstants.FS) {
        break;
      }
    }
    byte[] stId = new byte[ix - i];
    System.arraycopy(data, i, stId, 0, ix - i);
    statusId = Integer.parseInt(new String(stId));
    i = ++ix;
    //lets get the status response
    VStatusResponse response = VStatusResponse.findInstance(statusId);
    //then the following have message identifiers
    String dataIdentifier = "";
    switch (response) {
      case NO_SUCH_NAME:
        noSuchName.getAndSet(true);
      //we simply let it flow
      case TEMPLATE_ERROR:
      case TECHNICAL_ERROR:
      case REQUEST_REJECTED:
        //all of this get the data identifier
        for (; ix < data.length; ix++) {
          if (data[ix] == VSeparatorCharacterConstants.FS) {
            break;
          }
        }
        stId = new byte[ix - i];
        System.arraycopy(data, i, stId, 0, ix - i);
        dataIdentifier = new String(stId);
        break;

    }
    statusMessage = "Server Status Update: Tag=" + tag + "\tData Identifier: " + dataIdentifier;
    response.setStatusMessage(statusMessage);
    System.err.println(response);
  }
  private int nextTicket = 0;
  private int originalNextTicket = 0;
  private boolean ticketRequestDefaulted = false;
  //we wont do any request as long as the ticket has not been successfully acquired.
  private final Duplex<Integer, Boolean> ticket_requests = new Duplex<Integer, Boolean>();
  /**
   * True if invalid ticket number has been requested.
   */
  private AtomicBoolean noSuchName = new AtomicBoolean(false);

  @Override
  public byte[] getData() {
    VTicketRequestCriterion criterion = reuter.getReuterProperties().getTicketRequestCriterion();
    if (noSuchName.compareAndSet(true, false)) {
      //we have reached the end of the ticket file, and no such ticket has been specified.
      return null;
    }
    if (!criterion.isRequestAllAvailableTickets()) {
      if (criterion.isRequestForParticularTicketRange()) {
        synchronized (ticket_requests) {
          if (ticket_requests.getFirstValue() == null || ticket_requests.getSecondValue()) {
            nextTicket = criterion.nextTicketInRange();
            ticket_requests.set(nextTicket, false);
          } else {
            //we return null. We cannot request the next ticket unless the previous one was successful.
            //the implementation should at least wait before requesting the next ticket data.
            return null;
          }
        }
      } else {
        //the next ticket property should be sent
        int nextTicketTmp = criterion.getFromTicketId();
        if (nextTicketTmp != originalNextTicket) {
          System.err.println("Original Ticket: " + originalNextTicket);
          System.err.println("Current Original Ticket: " + nextTicketTmp);
          originalNextTicket = nextTicketTmp;
          nextTicket = originalNextTicket;
        }
        if (!(criterion.isRequestForParticularTicketRange() && nextTicket > criterion.getToTicketId())) {
          if (nextTicket == reuter.getReuterProperties().getMaximumTicketQueue()) {
            nextTicket = originalNextTicket;
          }
          //otherwise, create the next message to request
          String tagId = generateTagIdentifier();
          synchronized (this) {
            //offer the request for data update information
            byte[] updateInfo = this.requestDealInformation(nextTicket, tagId);
            if (!serviceData.containsKey(nextTicket)) {
              this.serviceData.offer(nextTicket, updateInfo);
              if (reuter.getReuterProperties().isEnableConversationTextRequest()) {
                //generate tag id for conversation text
                String st = generateTagIdentifier();
                byte[] conversationText = this.requestConversationtext(nextTicket, st);
                this.serviceData.offer(nextTicket, conversationText);
              }
            }
          }
          nextTicket++;
        }
      }
    } else {
      if (!ticketRequestDefaulted) {
        errorInitializing = true;
        ticketRequestDefaulted = true;
      }
    }
    if (serviceData != null) {
      if (!serviceData.isEmpty()) {
        Duplex<Integer, byte[]> bb = serviceData.poll();
        System.out.println("Request:  " + new String(bb.getSecondValue()));
        return bb.getSecondValue();
      } else {
        return null;
      }
    }
    return null;
  }

  @Override
  public byte[] getInitializationData() {
    if (reuter.getReuterProperties().getTicketRequestCriterion().isRequestAllAvailableTickets()) {
      return requestDatabaseStatusInformation();
    } else {
      //then we start requesting for the tickets according to the ticket criterion
      return getData();
    }
  }

  private Duplex<VMarketfeedMessageType, Integer> findMessageType(byte[] data) {
    //we start parsing the data
    //remove the start of message character
    if (data[0] != VSeparatorCharacterConstants.FS) {
      throw new VMarketfeedException("Invalid Marketfeed Message");
    }
    //lets decode out the messaget type
    int i = 1;
    byte[] messageType = new byte[3]; //we know its three bytes
    int j = 0;
    while (data[i] != VSeparatorCharacterConstants.US) {
      messageType[j++] = data[i++];
    }
    i++;
    //get message type
    String mId = new String(messageType);
    int mId_ = Integer.parseInt(mId);
    return new Duplex<VMarketfeedMessageType, Integer>(VMarketfeedMessageType.getInstance(mId_), i);
  }

  /**
   * Decodes a 340 response data
   *
   * @param data
   * @return
   */
  private void decode340(int i, byte[] data) {
    int k = i;
    int ix = i;
    for (; ix < data.length; ix++) {
      if (data[ix] == VSeparatorCharacterConstants.GS) {
        break;
      }
    }
    if (ix == i) {
      throw new VMarketfeedException("Invalid Message format: ");
    }
    byte[] tag = Arrays.copyOfRange(data, k, ix);
    i = ++ix;
    if (Arrays.equals(tag, REQUEST_TAG)) {
      decodeDatabaseInformation(ix, data);
    } else {
      decodeDealDetail(i, data, new String(tag));
    }
  }

  /**
   * Decodes database information from the response
   *
   * @param i
   * @param data
   */
  private void decodeDatabaseInformation(int i, byte[] data) {
    System.out.println(new String(data));
    //lets determine the data identifier
    int ix = i;
    for (; ix < data.length; ix++) {
      if (data[ix] == VSeparatorCharacterConstants.US) {
        break;
      }
    }
    if (i == ix) {
      throw new VMarketfeedException("Invalid Data Identifier");
    }
    byte[] dataId = new byte[ix - i];
    System.arraycopy(data, i, dataId, 0, ix - i);
    //is it an information request we sent
    String dataIdStr = new String(dataId).trim();
    if (!dataIdStr.equals(reuter.getReuterProperties().getDealingServerTCID() + "#INFO")) {
      throw new VMarketfeedException("Invalid Data. Expected Response: " + dataIdStr);
    }
    i = ++ix;
    //read the field list number
    for (; ix < data.length; ix++) {
      if (data[ix] == VSeparatorCharacterConstants.US) {
        break;
      }
    }
    if (i == ix) {
      throw new VMarketfeedException("Invalid Field List Identifier");
    }
    byte[] fieldListNumber = new byte[ix - i];
    System.arraycopy(data, i, fieldListNumber, 0, ix - i);
    String num = new String(fieldListNumber);
    int fieldList = Integer.parseInt(num);
    //we expect this to be database status information
    if (fieldList != FIELD_LIST_DATABASE_STATUS) {
      throw new VMarketfeedException("Expected Database Status Information");
    }
    i++;
    //read out the zero which we currently ignore and then start reading data fields
    while (data[i] != VSeparatorCharacterConstants.RS) {
      i++;
      continue;
    }
    i++;
    decodeDealTicketRequest(decodeDataFields(i, data));

  }

  private synchronized void decodeDealTicketRequest(List<Duplex<Integer, String>> dataFields) {
    //at this point  we have only two fields,
    //oldest deal in the database and newest deal in the database
    //generate unique identifier for ticket
    int oldestTicket = 0;
    int newestTicket = 0;
    for (Duplex<Integer, String> dup : dataFields) {
      Integer df = dup.getFirstValue(); //this must represent either an oldest deal or newest deal
      if (df == 533) {
        String tcStr = dup.getSecondValue().trim();
        tcStr = tcStr.substring(tcStr.indexOf('#') + 1);
        oldestTicket = Integer.parseInt(tcStr);
      } else if (df == 536) { //only the new tickets available in the database
        //parse the string into ticket id
        String tcStr = dup.getSecondValue().trim();
        tcStr = tcStr.substring(tcStr.indexOf('#') + 1);
        newestTicket = Integer.parseInt(tcStr);
        break;
      }
    }
    int maxTicketQueue = reuter.getReuterProperties().getMaximumTicketQueue();
    boolean rolledOver = oldestTicket > newestTicket;
    if (rolledOver) {
      //start from 1 - newest ticket
      for (int ticketId = 1; ticketId <= newestTicket; ticketId++) {
        String tagId = generateTagIdentifier();
        //offer the request for data update information
        byte[] updateInfo = this.requestDealInformation(ticketId, tagId);
        if (!serviceData.containsKey(ticketId)) {
          this.serviceData.offer(ticketId, updateInfo);
          if (reuter.getReuterProperties().isEnableConversationTextRequest()) {
            //generate tag id for conversation text
            String st = generateTagIdentifier();
            byte[] conversationText = this.requestConversationtext(ticketId, st);
            this.serviceData.offer(ticketId, conversationText);
          }
        }
      }
      //and then from oldestTicket to maxTicketQueue
      newestTicket = maxTicketQueue;
      //since it is rolled over, then the maximum we can iterate from the oldestTicket to the newest ticket is up to max queue.
    }
    for (int ticketId = oldestTicket; ticketId <= newestTicket; ticketId++) {
      //get the last ticket we requested. and as long as the old ticket is less than the last request, ignore.
      //this last ticket is saved on ticket request criterion.
      if (ticketId <= reuter.getReuterProperties().getTicketRequestCriterion().getFromTicketId()) {
        continue;
      }
      String tagId = generateTagIdentifier();
      //offer the request for data update information
      byte[] updateInfo = this.requestDealInformation(ticketId, tagId);
      if (!serviceData.containsKey(ticketId)) {
        this.serviceData.offer(ticketId, updateInfo);
        if (reuter.getReuterProperties().isEnableConversationTextRequest()) {
          //generate tag id for conversation text
          String st = generateTagIdentifier();
          byte[] conversationText = this.requestConversationtext(ticketId, st);
          this.serviceData.offer(ticketId, conversationText);
        }
      }
    }
  }

  private void decodeDealDetail(int i, byte[] data, String tag) {
    try {
      //we need to determine what type of deals we have
      //decode the right deal and forward it to the deal manager
      //lets determine the data identifier
      int ix = i;
      for (; ix < data.length; ix++) {
        if (data[ix] == VSeparatorCharacterConstants.US) {
          break;
        }
      }
      if (i == ix) {
        throw new VMarketfeedException("Invalid Data Identifier");
      }
      byte[] dataId = new byte[ix - i];
      System.arraycopy(data, i, dataId, 0, ix - i);
      //this should be the right ticket number
      String dataIdStr = new String(dataId).trim();
      String _ticketId_ = dataIdStr.substring(dataIdStr.indexOf("#") + 1);
      int ix_ = _ticketId_.indexOf("C");
      if (ix_ != -1) {
        _ticketId_ = _ticketId_.substring(0, ix_);
      }
      int ticket_id = Integer.parseInt(_ticketId_);
      i = ++ix;
      //read the field list number
      for (; ix < data.length; ix++) {
        if (data[ix] == VSeparatorCharacterConstants.US) {
          break;
        }
      }
      if (i == ix) {
        throw new VMarketfeedException("Invalid Field List Identifier");
      }
      byte[] fieldListNumber = new byte[ix - i];
      System.arraycopy(data, i, fieldListNumber, 0, ix - i);
      String num = new String(fieldListNumber);
      int fieldList = Integer.parseInt(num);
      //we expect this not to be database status information
      if (fieldList == FIELD_LIST_DATABASE_STATUS) {
        throw new VMarketfeedException("Expected Deal Information or Conversation Text");
      }
      i++;
      //read out the zero which we currently ignore and then start reading data fields
      while (data[i] != VSeparatorCharacterConstants.RS) {
        i++;
        continue;
      }
      i++;
      Duplex<Integer, Map<Integer, String>> dataFields = decodeDealDetails(i, data);
      synchronized (this) {
        //lets confirm that it is a conversation text.
        //If it is, then we offer it to the conversationqueue
        if (!reuter.getReuterProperties().getDataSourceConfiguration().isDealAndConversationCombined() && fieldList == FIELD_LIST_CONVERSATION_TEXT) {
          this.dealConversationQueue.offer(ticket_id, dataFields.getSecondValue());
          forwardConversations(this.dealConversationQueue.poll());
        } else {
          this.dealInformationQueue.offer(ticket_id, dataFields);
          forwardDeals(this.dealInformationQueue.poll());
        }

        this.notifyAll();
      }
    } finally {
      //we have successfully requested this ticket
      if (reuter.getReuterProperties().getTicketRequestCriterion().isRequestForParticularTicketRange()) {
        synchronized (ticket_requests) {
          ticket_requests.set(null, null);
        }
      }
    }
  }

  /**
   * decodes a 316 update information and does a request for the deal information for the specified
   * ticket number
   *
   * @param data
   * @return
   */
  private void decode316(int i, byte[] data) {
    int k = i;
    int ix = i;
    for (; ix < data.length; ix++) {
      if (data[ix] == VSeparatorCharacterConstants.GS) {
        break;
      }
    }
    if (ix == i) {
      throw new VMarketfeedException("Invalid Message format: ");
    }
    byte[] tag = Arrays.copyOfRange(data, k, ix);
    if (Arrays.equals(tag, REQUEST_TAG)) {
      //lets determine the data identifier
      i = ++ix;
      for (; ix < data.length; ix++) {
        if (data[ix] == VSeparatorCharacterConstants.US) {
          break;
        }
      }
      if (i == ix) {
        throw new VMarketfeedException("Invalid Data Identifier");
      }
      byte[] dataId = new byte[ix - i];
      System.arraycopy(data, i, dataId, 0, ix - i);
      //is it an information request we sent
      String dataIdStr = new String(dataId).trim();
      if (!dataIdStr.equals(reuter.getReuterProperties().getDealingServerTCID() + "#INFO")) {
        throw new VMarketfeedException("Invalid Data. Expected Response");
      }
      i = ++ix;
      //read out the zero which we currently ignore and then start reading data fields
      while (data[i] != VSeparatorCharacterConstants.RS) {
        i++;
        continue;
      }
      i++;
      decodeDealTicketRequest(decodeDataFields(i, data));
    } else {
      throw new VMarketfeedException("Unexpected Request Tag");
    }
  }

  /**
   * Returns data fields data value pairs
   *
   * @param i the index where the data begins
   * @param data the data to decode
   * @return
   */
  private List<Duplex<Integer, String>> decodeDataFields(int i, byte[] data) {
    List<Duplex<Integer, String>> dataFields = new ArrayList<Duplex<Integer, String>>();
    do {
      if (data[i] == VSeparatorCharacterConstants.FS) {
        break;
      }
      if (data[i] == VSeparatorCharacterConstants.RS) {
        i++;
      }
      int s = i;
      while (data[i] != VSeparatorCharacterConstants.US) {
        i++;
        continue;
      }
      byte[] dataId = Arrays.copyOfRange(data, s, i);
      Integer dataFieldId = Integer.parseInt(new String(dataId));
      s = i++;
      while (data[i] != VSeparatorCharacterConstants.RS && data[i] != VSeparatorCharacterConstants.FS) {
        i++;
        continue;
      }
      byte[] dataValue = Arrays.copyOfRange(data, s, i);
      String dataFieldValue = new String(dataValue).trim();
      dataFields.add(new Duplex<Integer, String>(dataFieldId, dataFieldValue));
    } while (data[i] != VSeparatorCharacterConstants.FS);
    return dataFields;
  }

  /**
   * Returns data fields data value pairs
   *
   * @param i the index where the data begins
   * @param data the data to decode
   * @return
   */
  private Duplex<Integer, Map<Integer, String>> decodeDealDetails(int i, byte[] data) {
    Map<Integer, String> dataFields = new HashMap<Integer, String>();
    HashMap<Integer, String> newfields = VRecordResponseData.getUserDefinedFields();
    dataFields.putAll(newfields);
    int dealType = 0;
    do {
      if (data[i] == VSeparatorCharacterConstants.FS) {
        break;
      }
      if (data[i] == VSeparatorCharacterConstants.RS) {
        i++;
      }
      int s = i;
      while (data[i] != VSeparatorCharacterConstants.US) {
        i++;
        continue;
      }
      byte[] dataId = Arrays.copyOfRange(data, s, i);
      Integer dataFieldId = Integer.parseInt(new String(dataId));
      s = i++;
      while (data[i] != VSeparatorCharacterConstants.RS && data[i] != VSeparatorCharacterConstants.FS) {
        i++;
        continue;
      }
      byte[] dataValue = Arrays.copyOfRange(data, s, i);
      String dataFieldValue = new String(dataValue).trim();
      dataFields.put(dataFieldId, dataFieldValue);
      if (dataFieldId == VRecordResponseData.PURE_DEAL_TYPE.getDataIndex()) {
        dealType = Integer.parseInt(dataFieldValue.trim());
      }
    } while (data[i] != VSeparatorCharacterConstants.FS);

    return new Duplex<Integer, Map<Integer, String>>(dealType, dataFields);
  }
}
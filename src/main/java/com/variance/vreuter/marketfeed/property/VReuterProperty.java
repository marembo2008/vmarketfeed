/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.property;

import com.anosym.vjax.annotations.CollectionElement;
import com.anosym.vjax.annotations.Comment;
import com.anosym.vjax.annotations.Constant;
import com.anosym.vjax.annotations.Informational;
import com.anosym.vjax.annotations.Markup;
import com.anosym.vjax.annotations.Marshallable;
import com.anosym.vjax.annotations.Namespace;
import com.anosym.vjax.annotations.Position;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Administrator
 */
@Namespace(prefix = "mf", uri = "http://reuters.variance.com/marketfeed")
@Comment(""
        + "Note the following:"
        + "\n1. DATABASE IS GENERIC. IT MAY IMPLY XML DATASOURCE OR RELATIONAL DATASOURCE."
        + "\n2. Not all of this property set may be required, and so any that is not required is to be ignored and left with their default value."
        + "\n3.Some properties may require the entire server restart. This may include connection properties and file operations.")
public class VReuterProperty {

  private final String TEST_TCID = "SIMB";
  private final String LIVE_TCID = "RBML";
  private boolean liveMode = false;

  @Comment("Changing the behaviour of the request when the server is running is not advised."
          + "\nThis is especially so when the default request is in effect which the current system, after initialization,"
          + "\nhas no control. When the settings are changed, it is advisable to restart the Glassfish service. "
          + "\nNote that changing from any other criterion to the requesting of all available tickets will not work "
          + "\nunless the server is restarted")
  public static class VTicketRequestCriterion {

    private boolean requestAllAvailableTickets;
    private boolean requestFromParticularTicketNumber;
    private boolean requestForParticularTicketRange;
    private int fromTicketId;
    private int toTicketId;
    private boolean rollOver;
    private int nextTicketInRange;

    public VTicketRequestCriterion() {
      requestAllAvailableTickets = true;
    }

    public void setRollOver(boolean rollOver) {
      this.rollOver = rollOver;
    }

    @Comment("Application to request for particular ticket range. "
            + "\nThis implies that if the maximum ticket id is reached, it rolls over and starts "
            + "\nfrom the initial ticket. Generally this is set when the range is from 0 - to maximum ticket possible in the system."
            + "\nBy default this is false.")
    public boolean isRollOver() {
      return rollOver;
    }

    public int nextTicketInRange() {
      //Logic, we request the current ticket number. and then increment the ticket.
      nextTicketInRange = (nextTicketInRange == 0) ? fromTicketId : nextTicketInRange;
      int nextTicketToRequest = nextTicketInRange;
      if (rollOver) {
        if (nextTicketInRange == toTicketId) {
          nextTicketInRange = fromTicketId;
        }
      }
      nextTicketInRange++;
      return nextTicketToRequest;
    }

    @Comment("The initial ticket to request when the system connects to the server."
            + "\nThis is only applicable if the request from a particular ticket number or request ticket range is specified")
    public int getFromTicketId() {
      return fromTicketId;
    }

    public void setFromTicketId(int fromTicketId) {
      this.fromTicketId = fromTicketId;
    }

    @Comment("If selected, the system requests the server for all available tickets it currently hold in it its queue."
            + "\nThis is the default behaviour.")
    @Position(index = 0)
    public boolean isRequestAllAvailableTickets() {
      return requestAllAvailableTickets;
    }

    public void setRequestAllAvailableTickets(boolean requestAllAvailableTickets) {
      this.requestAllAvailableTickets = requestAllAvailableTickets;
    }

    @Comment("The system only requests for tickets within the bounds specified by from ticket and to to_ticket fields")
    @Position(index = 1)
    public boolean isRequestForParticularTicketRange() {
      return requestForParticularTicketRange;
    }

    public void setRequestForParticularTicketRange(boolean requestForParticularTicketRange) {
      this.requestForParticularTicketRange = requestForParticularTicketRange;
    }

    @Comment("The system requests for tickets from the specified ticket number.However, when the system restarts, the last ticket"
            + "requested will be the next point  the system will start the next ticket request.")
    @Position(index = 2)
    public boolean isRequestFromParticularTicketNumber() {
      return requestFromParticularTicketNumber;
    }

    public void setRequestFromParticularTicketNumber(boolean requestFromParticularTicketNumber) {
      this.requestFromParticularTicketNumber = requestFromParticularTicketNumber;
    }

    @Comment("Upper bound for the tickets to be requested.\nApplies only if range request is selected.")
    public int getToTicketId() {
      return toTicketId;
    }

    public void setToTicketId(int toTicketId) {
      this.toTicketId = toTicketId;
    }
  }
  private List<VServerConnectionType> serverConnectionTypes;
  private int gmtOffset;
  private VDataSourceConfiguration dataSourceConfiguration;
  private boolean enableConversationTextRequest;
  private int propertyReloadMinutes;
  private VLoggingProperty loggingProperty;
  private boolean reuterPropertySet;
  private String fileDirectory;
  private VTicketRequestCriterion ticketRequestCriterion;
  private int maximumTicketQueue;

  public VReuterProperty() {
    this.dataSourceConfiguration = new VDataSourceConfiguration();
    this.loggingProperty = new VLoggingProperty();
    this.gmtOffset = 3;
    this.serverConnectionTypes = new ArrayList<VServerConnectionType>(Arrays.asList(VServerConnectionType.values()));
    this.enableConversationTextRequest = true;
    this.propertyReloadMinutes = 1;
    this.fileDirectory = System.getProperty("user.home", "") + File.separator + "reuters";
    this.ticketRequestCriterion = new VTicketRequestCriterion();
    this.maximumTicketQueue = 9999;
  }

  @Comment("This specifies the length of the queue. \n"
          + "It determines the largest ticket number that can be used for the specified dealing server."
          + "\nThis value must be the same as the one specified in the dealing server.")
  public int getMaximumTicketQueue() {
    return maximumTicketQueue;
  }

  public void setMaximumTicketQueue(int maximumTicketNumber) {
    this.maximumTicketQueue = maximumTicketNumber;
  }

  @Comment("The criterion the system will use to request tickets from the server")
  public VTicketRequestCriterion getTicketRequestCriterion() {
    return ticketRequestCriterion;
  }

  public void setTicketRequestCriterion(VTicketRequestCriterion ticketRequestCriterion) {
    this.ticketRequestCriterion = ticketRequestCriterion;
  }

  @Comment("When this file is generated, this property is set to false.\n"
          + "This implies that this property must be set manually or otherwise by an external agent.\n"
          + "All modules that are dependent on this property will not function unless this is set to true.\n"
          + "This is especially for reading the TOF Marketfeed Details from the TOF server.")
  @Position(index = 1)
  @Markup(name = "Activate-System")
  public boolean isReuterPropertySet() {
    return reuterPropertySet;
  }

  public void setReuterPropertySet(boolean reuterPropertySet) {
    this.reuterPropertySet = reuterPropertySet;
  }

  @Comment("The File Directory Represents The relative path for all operation of files to be created or read where relavant")
  @Position(index = 0)
  public String getFileDirectory() {
    return fileDirectory;
  }

  public void setFileDirectory(String fileDirectory) {
    try {
      this.fileDirectory = fileDirectory;
      File f = new File(fileDirectory);
      if (!f.exists()) {
        f.mkdirs();
      }
    } catch (Exception e) {
    }
  }

  @Comment("Describes the Error and Report Logging of the Server. By default this is turned on.\n"
          + "You can turn it off even if the server is running by modifying the loggingEnabled field.")
  @Position(index = 2)
  public VLoggingProperty getLoggingProperty() {
    return loggingProperty;
  }

  public void setLoggingProperty(VLoggingProperty loggingProperty) {
    this.loggingProperty = loggingProperty;
  }

  @Comment("How often to reload the property")
  public int getPropertyReloadMinutes() {
    return propertyReloadMinutes;
  }

  public void setPropertyReloadMinutes(int propertyReloadMinutes) {
    this.propertyReloadMinutes = propertyReloadMinutes;
  }

  @Comment("Whenever a new deal is requested, "
          + "\nthis property determines whether a deal conversation is to be requested at the same time,"
          + "\nbeside the deal details.\nBy default this is true")
  public boolean isEnableConversationTextRequest() {
    return enableConversationTextRequest;
  }

  public void setEnableConversationTextRequest(boolean enableConversationTextRequest) {
    this.enableConversationTextRequest = enableConversationTextRequest;
  }

  @Informational
  @Comment("CURRENTLY SUPPORTED CONNECTION TYPES TO THE DEALING SERVER")
  @Markup(name = "connection-types")
  @CollectionElement("server-connection")
  public List<VServerConnectionType> getServerConnectionTypes() {
    return serverConnectionTypes;
  }

  @Marshallable(marshal = false)
  public VConnectionProperty getConnectionProperty() {
    for (VServerConnectionType type : serverConnectionTypes) {
      if (type.isActive()) {
        return type.getConnectionProperty();
      }
    }
    return null;
  }

  @Comment("Data Source configuration. Used to map the tof data to a specific data source")
  public VDataSourceConfiguration getDataSourceConfiguration() {
    return dataSourceConfiguration;
  }

  public void setDataSourceConfiguration(VDataSourceConfiguration dataSourceeConfiguration) {
    this.dataSourceConfiguration = dataSourceeConfiguration;
  }

  @Comment("The Greenwich Mean Time difference.\n"
          + "This is normally the difference between the local time and the time at zero degree longitude")
  public int getGmtOffset() {
    return gmtOffset;
  }

  public void setGmtOffset(int gmtOffset) {
    this.gmtOffset = gmtOffset;
  }

  public void setLiveMode(boolean liveMode) {
    this.liveMode = liveMode;
  }

  public boolean isLiveMode() {
    return liveMode;
  }

  /**
   * The dealing server identification
   *
   * @return
   */
  @Comment("The unique identification of the dealing server."
          + "\nThis should be provided before the installation and commissioning of the Dealing Server system."
          + "\nThe system will fail to function if this is empty or not specified correctly."
          + "\nThis value cannot be set after the system has been commissioned")
  /**
   * TODO(marembo): Informational attribute should be constant by default This should be implemented
   * in the marshalling stage It should be an error to declare both Informational and Constant on a
   * property.
   */
  @Informational //Should be constant by default?
  @Constant
  public String getDealingServerTCID() {
    return liveMode ? LIVE_TCID : TEST_TCID;
  }
}

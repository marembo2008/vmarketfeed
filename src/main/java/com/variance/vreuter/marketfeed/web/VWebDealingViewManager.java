/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.util.VColumnRecord;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import javax.ejb.EJB;
import javax.el.ValueExpression;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;

/**
 *
 * @author Administrator
 */
@Named("dealingViewManager")
@SessionScoped
public class VWebDealingViewManager implements Serializable {

  @EJB
  private VReuterRemote reuter;
  private HtmlPanelGroup dataGroup;
  private HtmlPanelGroup ticketDetailGroups;
  private HtmlPanelGroup ticketConversationGroup;
  @Inject
  private VMarketfeedManagerRemote vmarketFeedManager;

  private ValueExpression createValueExpression(String valueExpression, Class<?> valueType) {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    return facesContext.getApplication().getExpressionFactory().createValueExpression(
            facesContext.getELContext(), valueExpression, valueType);
  }

  private String getTicketIdColumn() {
    for (VColumnRecord v : vmarketFeedManager.getTicketColumns()) {
      String t = v.getFirstValue();
      if (t != null && !t.isEmpty()) {
        VRecordResponseData d = v.getSecondValue();
        if (d == VRecordResponseData.TICKET_ID || d == VRecordResponseData.CONVERSATION_TEXT) {
          return t;
        }
      }
    }
    return null;
  }

  private void populateDataTable() {
    if (dataGroup == null) {
      dataGroup = new HtmlPanelGroup();
    }
    boolean idSet = false;
    for (List<VColumnRecord> res : getResponseDataHeader()) {
      System.out.println("populateDataTablei: Columns: " + res);
      DataTable dataTable = new DataTable();
      dataTable.setId("ticketDetailsId");
      dataTable.setValueExpression("value",
              createValueExpression("#{viewManager.updatedTickets}", List.class));
      dataTable.setVar("tkId");
      dataTable.setPaginator(true);
      dataTable.setScrollable(true);
      if (!idSet) {
        idSet = true;
        //by default add the ticket id column
        Column idCol = new Column();
        idCol.setStyleClass("column_props");
        //facet
        HtmlOutputText idHeader = new HtmlOutputText();
        idHeader.setStyleClass("header");
        idHeader.setValue(getTicketIdColumn());
        idCol.setHeader(idHeader);
        //then value
        HtmlOutputText idValue = new HtmlOutputText();
        idValue.setStyleClass("header");
        idValue.setValueExpression("value",
                createValueExpression("#{tkId}", Integer.class));
        idCol.getChildren().add(idValue);
        dataTable.getChildren().add(idCol);
      }
      /*
       * // Create the rest of the columns
       *
       */
      //the following attributes should be added
      for (VColumnRecord v : res) {
        VRecordResponseData d = v.getSecondValue();
        if (d == VRecordResponseData.TICKET_ID) {
          continue;
        }
        Column c = new Column();
        c.setStyleClass("column_props");
        dataTable.getChildren().add(c);
        // Create <h:outputText value="ID"> for <f:facet name="header"> of 'ID' column.
        HtmlOutputText header = new HtmlOutputText();
        header.setStyleClass("header");
        header.setValue(v.getFirstValue());
        c.setHeader(header);
        // Create <h:outputText value="#{dataItem.id}"> for the body of 'ID' column.
        HtmlOutputText cValue = new HtmlOutputText();
        cValue.setId("ID" + d.getDataIndex());
        cValue.setStyleClass("header");
        cValue.setValueExpression("value",
                createValueExpression("#{viewManager.getDetailValue(tkId, \"" + d.getDataIndex() + "\")}", String.class));
        c.getChildren().add(cValue);
      }
      dataGroup.getChildren().add(dataTable);
    }
  }

  private void populateTicketDetails() {
    if (ticketDetailGroups == null) {
      ticketDetailGroups = new HtmlPanelGroup();
    }
    int i = 0;
    for (List<VColumnRecord> res : getResponseDataHeader()) {
      System.out.println("populateTicketDetails: Columns: " + res);
      DataTable dataTable = new DataTable();
      dataTable.setValueExpression("value",
              createValueExpression("#{viewManager.ticket}", List.class));
      dataTable.setVar("tkId");
      dataTable.setPaginator(true);
      dataTable.setScrollable(true);
      dataTable.setScrollWidth("400");
      dataTable.setScrollHeight("40");
      dataTable.setResizableColumns(true);
      if (i == 0) {
        //by default add the ticket id column
        Column idCol = new Column();
        idCol.setStyleClass("column_props");
        //facet
        HtmlOutputText idHeader = new HtmlOutputText();
        idHeader.setStyleClass("header");
        idHeader.setValue(getTicketIdColumn());
        idCol.setHeader(idHeader);
        //then value
        HtmlOutputText idValue = new HtmlOutputText();
        idValue.setStyleClass("header");
        idValue.setValueExpression("value",
                createValueExpression("#{tkId}", Integer.class));
        idCol.getChildren().add(idValue);
        dataTable.getChildren().add(idCol);
        i++;
      }
      /*
       * // Create the rest of the columns
       *
       */
      String colClasses = "column_props,";
      //the following attributes should be added
      for (VColumnRecord v : res) {
        VRecordResponseData d = v.getSecondValue();
        if (d == VRecordResponseData.TICKET_ID || d == VRecordResponseData.CONVERSATION_TEXT) {
          continue;
        }
        colClasses += "column_props,";
        Column c = new Column();
        dataTable.getChildren().add(c);
        c.setStyleClass("column_props");
        // Create <h:outputText value="ID"> for <f:facet name="header"> of 'ID' column.
        HtmlOutputText header = new HtmlOutputText();
        header.setStyleClass("header");
        header.setValue(v.getFirstValue());
        c.setHeader(header);
        // Create <h:outputText value="#{dataItem.id}"> for the body of 'ID' column.
        HtmlOutputText cValue = new HtmlOutputText();
        cValue.setStyleClass("header");
        cValue.setId("ID" + d.getDataIndex());
        cValue.setValueExpression("value",
                createValueExpression("#{viewManager.getDetailValue(tkId, \"" + d.getDataIndex() + "\")}", String.class));
        c.getChildren().add(cValue);
      }
//            colClasses = colClasses.substring(0, colClasses.lastIndexOf(","));
      ticketDetailGroups.getChildren().add(dataTable);
    }
  }

  private void populateTicketConversation() {
    if (ticketConversationGroup == null) {
      ticketConversationGroup = new HtmlPanelGroup();
    }
    HtmlDataTable dataTable = new HtmlDataTable();
    dataTable.setValueExpression("value",
            createValueExpression("#{viewManager.ticket}", List.class));
    dataTable.setVar("tkId");
    dataTable.setCellspacing("0");
    dataTable.setBorder(5);
    dataTable.setCellpadding("5");
    /*
     * Create the conversation columns
     *
     */
    String colClasses = "column_props,column_props";
    dataTable.setColumnClasses(colClasses);
    HtmlColumn c = new HtmlColumn();
    dataTable.getChildren().add(c);
    HtmlOutputText header = new HtmlOutputText();
    header.setStyleClass("header");
    header.setValue(VRecordResponseData.CONVERSATION_TEXT.getSimpleName());
    c.setHeader(header);
    HtmlOutputText cValue = new HtmlOutputText();
    cValue.setEscape(false);
    cValue.setStyleClass("blue");
    cValue.setValueExpression("value",
            createValueExpression("#{viewManager.getConversationValue(tkId, \"" + VRecordResponseData.CONVERSATION_TEXT.getDataIndex() + "\")}", String.class));
    c.getChildren().add(cValue);
    //add table to group
    ticketConversationGroup.getChildren().add(dataTable);
  }

  private List<List<VColumnRecord>> getResponseDataHeader() {
    List<List<VColumnRecord>> resData = new ArrayList<List<VColumnRecord>>();
    List<VColumnRecord> rData = new ArrayList<VColumnRecord>(vmarketFeedManager.getTicketColumns());
    if (rData.isEmpty()) {
      for (VColumnRecord v : reuter.getReuterProperties().getDataSourceConfiguration().getTableConfigurations().get(0).getColumnRecordId()) {
        String t = v.getFirstValue();
        if (t != null && !t.isEmpty()) {
          VRecordResponseData d = v.getSecondValue();
          if (d == VRecordResponseData.TICKET_ID || d == VRecordResponseData.CONVERSATION_TEXT) {
            continue;
          }
          rData.add(v);
        }
      }
    } else {
      for (ListIterator<VColumnRecord> it = rData.listIterator(); it.hasNext();) {
        VColumnRecord columnRecord = it.next();
        String t = columnRecord.getFirstValue();
        if (t != null && !t.isEmpty()) {
          VRecordResponseData d = columnRecord.getSecondValue();
          if (d == VRecordResponseData.TICKET_ID || d == VRecordResponseData.CONVERSATION_TEXT) {
            it.remove();
            continue;
          }

        } else {
          it.remove();
        }
      }
    }
    Collections.sort(rData);
    if (!rData.isEmpty()) {
      resData.add(rData);
    }
    return resData;
  }

  public void showTicketDetails() {
  }

  public void showTicketConversation() {
  }

  public HtmlPanelGroup getDataGroup() {
    if (dataGroup == null) {
      populateDataTable();
    }
    return dataGroup;
  }

  public void setDataGroup(HtmlPanelGroup dataGroup) {
    this.dataGroup = dataGroup;
  }

  public HtmlPanelGroup getTicketDetailGroups() {
    if (ticketDetailGroups == null) {
      populateTicketDetails();
    }
    return ticketDetailGroups;
  }

  public void setTicketDetailGroups(HtmlPanelGroup ticketDetailGroups) {
    this.ticketDetailGroups = ticketDetailGroups;
  }

  public HtmlPanelGroup getTicketConversationGroup() {
    if (ticketConversationGroup == null) {
      populateTicketConversation();
    }
    return ticketConversationGroup;
  }

  public void setTicketConversationGroup(HtmlPanelGroup ticketConversationGroupd) {
    this.ticketConversationGroup = ticketConversationGroupd;
  }
}

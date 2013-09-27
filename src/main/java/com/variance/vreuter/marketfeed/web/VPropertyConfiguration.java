/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.anosym.utilities.Utility;
import com.anosym.vjax.VMarshaller;
import com.anosym.vjax.VMarshallerConstants;
import com.anosym.vjax.VXMLBindingException;
import com.anosym.vjax.xml.VAttribute;
import com.anosym.vjax.xml.VContent;
import com.anosym.vjax.xml.VDocument;
import com.anosym.vjax.xml.VElement;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.property.VDataSourceConfiguration;
import com.variance.vreuter.marketfeed.property.VReuterProperty;
import com.variance.vreuter.marketfeed.util.VColumnRecord;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.TreeNode;

/**
 *
 * @author Marembo
 */
@Named("property")
@SessionScoped
public class VPropertyConfiguration implements Serializable {

  public class ColumnRecord {

    VElement columnId;

    public ColumnRecord() {
    }

    public ColumnRecord(VElement columnId) {
      this.columnId = columnId;
    }

    public String getIndex() {
      VElement colIndex = columnId.getChild("columnIndex");
      if (colIndex != null) {
        return colIndex.toContent();
      }
      return null;
    }

    public void setIndex(String index) {
      VElement colIndex = columnId.getChild("columnIndex");
      boolean error = false;
      if (colIndex != null) {
        String oldIndex = colIndex.toContent();
        try {
          if (oldIndex != null && Utility.isNumber(oldIndex)) {
            int oldIx = Integer.parseInt(oldIndex);
            if (mmIndeces.contains(index)) {
              message = null;
              if (!index.equals("0")) {
                //remove the previous one
                if (oldIx > 0) {
                  mmIndeces.add(oldIndex);
                  mmIndeces.remove(index);
                }
              }
            } else {
              error = true;
              message = "Index has been allocated: " + index;
            }
          }
        } catch (Exception e) {
        }
        if (!error) {
          message = null;
        }
        colIndex.setContent(index);
      }
    }

    public String getMappedName() {
      VElement name = columnId.getChild("table-column");
      return name.toContent();
    }

    public void setMappedName(String mappedName) {
      VElement name = columnId.getChild("table-column");
      name.setContent(mappedName);

    }

    public String getTofDataIndex() {
      VElement recId = columnId.getChild("data-field");
      VElement dIndex = recId.getChild("dataIndex");
      return dIndex.toContent();
    }

    public void setTofDataIndex(String tofDataIndex) {
      VElement recId = columnId.getChild("data-field");
      VElement dIndex = recId.getChild("dataIndex");
      dIndex.setContent(tofDataIndex);
    }

    public String getTofDataName() {
      VElement recId = columnId.getChild("data-field");
      VElement dIndex = recId.getChild("dataName");
      return dIndex.toContent();
    }

    public void setTofDataName(String tofDataName) {
      VElement recId = columnId.getChild("data-field");
      VElement dIndex = recId.getChild("dataName");
      dIndex.setContent(tofDataName);
    }

    public String getDefaultValue() {
      VElement dValue = columnId.getChild("defaultValue");
      return dValue.toContent();
    }

    public void setDefaultValue(String defaultValue) {
      VElement dValue = columnId.getChild("defaultValue");
      dValue.setContent(defaultValue);
    }

    public boolean isAlwaysUseDefaultValue() {
      VElement dValue = columnId.getChild("alwaysUseDefaultValue");
      return Boolean.valueOf(dValue.toContent());
    }

    public void setAlwaysUseDefaultValue(boolean defaultValue) {
      VElement dValue = columnId.getChild("alwaysUseDefaultValue");
      dValue.setContent(defaultValue + "");
    }
  }
  private static List<String> TABS = Arrays.asList("generalProperties", "mmMapping", "fxMapping");
  @EJB
  private VReuterRemote reuter;
  private VElement selectedElement;
  private VElement mainElement;
  //private VElement columnRecords;
  private VElement mmColumnRecords;
  private VElement fxColumnRecords;
  private List<VElement> propertyElements = new ArrayList<VElement>();
  private List<String> mmIndeces = new ArrayList<String>();
  private List<String> fxIndeces = new ArrayList<String>();
  private TreeNode properties;
  private int activeTab;
  private List<VColumnRecord> mmColumns;
  private List<VColumnRecord> fxColumns;

  public void onTabChanged(TabChangeEvent tc) {
    Tab t = tc.getTab();
    String id = t.getId();
    activeTab = TABS.indexOf(id);
  }

  public void setActiveTab(int activeTab) {
    //do do anything
  }

  public int getActiveTab() {
    return activeTab;
  }

  public List<VElement> getPropertyElements() {
    return propertyElements;
  }

  public VElement getPropertyElement() {
    return mainElement;
  }

  private void addNodes(TreeNode parent, VElement pElem) {
    for (VElement cElem : pElem.getChildren()) {
      if (cElem instanceof VContent) {
        //we changed how we handle element contents, do not set as VElement.
        continue;
      }
      VAttribute va = cElem.getAttribute(VMarshallerConstants.DISPLAYABLE_ATTRIBUTE);
      if ((va != null) ? va.getBoolenValue() : true) {
        if (cElem.getMarkup().equals(VMarshallerConstants.COLLECTION_ELEMENT_MARKUP)) {
          addNodes(parent, cElem);
        } else {
          TreeNode cNode = new DefaultTreeNode(cElem, parent);
          addNodes(cNode, cElem);
        }
      }
    }
  }

  private void createNodes(VElement elem) {
    properties = new DefaultTreeNode(elem, null);
    for (VElement e : elem.getChildren()) {
      if (e instanceof VContent) {
        //we changed how we handle element contents, do not set as VElement.
        continue;
      }
      VAttribute va = e.getAttribute(VMarshallerConstants.DISPLAYABLE_ATTRIBUTE);
      if ((va != null) ? va.getBoolenValue() : true) {
        if (e.getMarkup().equals(VMarshallerConstants.COLLECTION_ELEMENT_MARKUP)) {
          addNodes(properties, e);
        } else {
          TreeNode pNode = new DefaultTreeNode(e, properties);
          addNodes(pNode, e);
        }
      }
    }
  }

  public TreeNode getProperties() {
    return properties;
  }

  public void setProperties(TreeNode properties) {
    this.properties = properties;
  }

  public List<VColumnRecord> getMoneyMarketColumnRecords() {
    if (mmColumns == null) {
      mmColumns = reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.VTableKey.MM_TABLE_CONFIG_NAME).getColumnRecordId();
      Collections.sort(mmColumns, new Comparator<VColumnRecord>() {
        @Override
        public int compare(VColumnRecord o1, VColumnRecord o2) {
          return Integer.valueOf(o1.getSecondValue().getDataIndex()).compareTo(o2.getSecondValue().getDataIndex());
        }
      });
    }
    return mmColumns;
  }

  public List<VColumnRecord> getForeignExchangeColumnRecords() {
    if (fxColumns == null) {
      fxColumns = reuter.getReuterProperties().getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.VTableKey.FX_TABLE_CONFIG_NAME).getColumnRecordId();
      Collections.sort(fxColumns, new Comparator<VColumnRecord>() {
        @Override
        public int compare(VColumnRecord o1, VColumnRecord o2) {
          return Integer.valueOf(o1.getSecondValue().getDataIndex()).compareTo(o2.getSecondValue().getDataIndex());
        }
      });
    }
    return fxColumns;
  }

  @PostConstruct
  public void onStart() {
    try {
      //System.out.println("onStart called");
      //create the mutable tree node and model
      VReuterProperty props = reuter.getReuterProperties();
      VMarshaller<VReuterProperty> m = new VMarshaller<VReuterProperty>();
      mainElement = m.marshall(props);
      VDocument doc = new VDocument("doc");
      doc.setRootElement(mainElement);
      addElements(mainElement);
      createNodes(mainElement);
      //select items
      for (int i = 0; i < VRecordResponseData.values().length; i++) {
        mmIndeces.add(i + "");
        fxIndeces.add(i + "");
      }
    } catch (VXMLBindingException ex) {
      Logger.getLogger(VPropertyConfiguration.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public String reloadProperties() {
    onStart();
    return null;
  }

  private void addElements(VElement elem) {
    for (VElement e : elem.getChildren()) {
      if (e instanceof VContent) {
        //we changed how we handle element contents, do not set as VElement.
        continue;
      }
      if (!e.getMarkup().equals("tableConfigurationMappings")) {
        if (!e.getMarkup().equals(VMarshallerConstants.COLLECTION_ELEMENT_MARKUP)) {
          this.propertyElements.add(e);
        }
        if (e.hasChildren()) {
          addElements(e);
        }
      }
    }
  }

  private class ColumnRecordDataModel implements SelectableDataModel<ColumnRecord> {

    private List<ColumnRecord> columnRecordMappings;

    ColumnRecordDataModel(List<ColumnRecord> columnRecordMappings) {
      this.columnRecordMappings = columnRecordMappings;
    }

    @Override
    public Object getRowKey(ColumnRecord object) {
      return columnRecordMappings.indexOf(object);
    }

    @Override
    public ColumnRecord getRowData(String rowKey) {
      return columnRecordMappings.get(Integer.parseInt(rowKey));
    }
  }
  private ColumnRecordDataModel mmRecordModel;
  private ColumnRecordDataModel fxRecordModel;

  public SelectableDataModel<ColumnRecord> getMmRecordModel() {
    if (mmRecordModel == null) {
      mmRecordModel = new ColumnRecordDataModel(mmColumnRecordMappings);
    }
    return mmRecordModel;
  }

  public SelectableDataModel<ColumnRecord> getFxRecordModel() {
    if (fxRecordModel == null) {
      fxRecordModel = new ColumnRecordDataModel(fxColumnRecordMappings);
    }
    return fxRecordModel;
  }

  public long getReloadTime() {
    return this.reuter.getReuterProperties().getPropertyReloadMinutes() * 60 * 1000;
  }

  public boolean isRenderElement() {
    return selectedElement != null;
  }

  public boolean isRenderElementContent() {
    boolean render = isRenderElement() && !selectedElement.hasChildren();
    return render;
  }

  public boolean isRenderElementContentArea() {
    return isRenderElement() && !selectedElement.hasChildren() && selectedElement.toContent() != null && selectedElement.toContent().length() > 50;
  }

  public boolean isRenderElementContentText() {
    return isRenderElement() && !selectedElement.hasChildren() && (selectedElement.toContent() == null || selectedElement.toContent().length() < 50);
  }

  public boolean isColumnRecordMapping() {
    return this.selectedElement != null && this.selectedElement.getMarkup().equalsIgnoreCase("columnRecordId");
  }
  private List<ColumnRecord> mmColumnRecordMappings;
  private ColumnRecord mmColumnRecord;
  private List<ColumnRecord> fxColumnRecordMappings;
  private ColumnRecord fxColumnRecord;

  public ColumnRecord getMmColumnRecord() {
    return mmColumnRecord;
  }

  public ColumnRecord getFxColumnRecord() {
    return fxColumnRecord;
  }

  public void setMmColumnRecord(ColumnRecord mmColumnRecord) {
    this.mmColumnRecord = mmColumnRecord;
  }

  public void setFxColumnRecord(ColumnRecord fxColumnRecord) {
    this.fxColumnRecord = fxColumnRecord;
  }

  public List<ColumnRecord> getMmColumnRecords() {
    if (mmColumnRecordMappings == null) {
      mmColumnRecordMappings = new ArrayList<ColumnRecord>();
      if (mmColumnRecords != null && mmColumnRecords.hasChildren()) {
        for (VElement el : mmColumnRecords.getChildren()) {
          ColumnRecord cr = new ColumnRecord(el);
          mmColumnRecordMappings.add(cr);
          //remove the id of the column from the list of selection if it is not null or equal to zero
          String index = cr.getIndex();
          if (index != null && !index.isEmpty()) {
            if (!index.equals("0")) {
              for (ListIterator<String> it = mmIndeces.listIterator(); it.hasNext();) {
                if (it.next().equals(index)) {
                  it.remove();
                  break;
                }
              }
            }
          }
        }
      }
    }
    return mmColumnRecordMappings;
  }

  public List<ColumnRecord> getFxColumnRecords() {

    if (fxColumnRecordMappings == null) {
      fxColumnRecordMappings = new ArrayList<ColumnRecord>();
      if (fxColumnRecords != null && fxColumnRecords.hasChildren()) {
        for (VElement el : fxColumnRecords.getChildren()) {
          ColumnRecord cr = new ColumnRecord(el);
          fxColumnRecordMappings.add(cr);
          //remove the id of the column from the list of selection if it is not null or equal to zero
          String index = cr.getIndex();
          if (index != null && !index.isEmpty()) {
            if (!index.equals("0")) {
              for (ListIterator<String> it = mmIndeces.listIterator(); it.hasNext();) {
                if (it.next().equals(index)) {
                  it.remove();
                  break;
                }
              }
            }
          }
        }
      }
    }
    return fxColumnRecordMappings;
  }

  public VElement getSelectedElement() {
    return selectedElement;
  }

  public boolean isRenderable(VElement elem) {
    return elem != null && !elem.isParent() && (elem.getParent() == null || elem.getParent().getAttribute(VMarshallerConstants.ENUM_ATTRIBUTE) == null);
  }

  public void setSelectedElement(VElement selectedElement) {
    this.selectedElement = selectedElement;
  }
  private String message;

  public String getMessage() {
    return message;
  }

  public boolean isIndexError() {
    return message != null && !message.isEmpty();
  }

  public void checkError() {
    if (this.isIndexError()) {
      FacesContext fc = FacesContext.getCurrentInstance();
      UIComponent cmp = fc.getViewRoot().findComponent("errorDialog");
      if (cmp instanceof Dialog) {
        ((Dialog) cmp).setVisible(true);
      }
    }
  }

  public void mmValidator(FacesContext cxt, UIComponent cmp, Object value) {
    //check if the new value is already assigned
    if (value == null) {
      throw new ValidatorException(
              new FacesMessage(
              FacesMessage.SEVERITY_ERROR,
              "Index must be specified",
              "Index of column must be specified"));
    }
    String index = value.toString(); //automatically
    if (index.equals("0") && !mmIndeces.contains(index)) {
      throw new ValidatorException(
              new FacesMessage(
              FacesMessage.SEVERITY_ERROR,
              "Index must be uniquely specified",
              "The Index specified is already used"));
    }
  }

  public void fxValidator(FacesContext cxt, UIComponent cmp, Object value) {
    //check if the new value is already assigned
    if (value == null) {
      throw new ValidatorException(
              new FacesMessage(
              FacesMessage.SEVERITY_ERROR,
              "Index must be specified",
              "Index of column must be specified"));
    }
    String index = value.toString(); //automatically
    if (index.equals("0") && !fxIndeces.contains(index)) {
      throw new ValidatorException(
              new FacesMessage(
              FacesMessage.SEVERITY_ERROR,
              "Index must be uniquely specified",
              "The Index specified is already used"));
    }
  }

  public String update() {
    try {
      System.out.println("fxColumns: " + fxColumns);
      System.out.println("mmColumns: " + mmColumns);
      VReuterProperty property = reuter.getReuterProperties();
      if (fxColumns != null && !fxColumns.isEmpty()) {
        property.getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.VTableKey.FX_TABLE_CONFIG_NAME).setColumnRecordId(fxColumns);
        System.out.println("set fxColumns: " + property.getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.VTableKey.FX_TABLE_CONFIG_NAME).getColumnRecordId());
      }
      if (mmColumns != null && !mmColumns.isEmpty()) {
        property.getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.VTableKey.MM_TABLE_CONFIG_NAME).setColumnRecordId(mmColumns);
        System.out.println("set mmColumns: " + property.getDataSourceConfiguration().getTableConfiguration(VDataSourceConfiguration.VTableKey.MM_TABLE_CONFIG_NAME).getColumnRecordId());
      }
      reuter.setReuterProperties(property);
      FacesContext.getCurrentInstance().addMessage("Success Message", new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully updated properties", "Successfully updated properties"));
    } catch (Exception ex) {
      Logger.getLogger(VPropertyConfiguration.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public String updateGeneralProperties() {
    if (mainElement != null) {
      try {
        VMarshaller<VReuterProperty> m = new VMarshaller<VReuterProperty>();
        VElement mainElement_ = m.marshall(reuter.getReuterProperties());
        VElement col = mainElement_.findChild("tableConfigurationMappings");
        mainElement.addChild(col);
        reuter.setReuterProperties(m.unmarshall(mainElement));
      } catch (VXMLBindingException ex) {
        Logger.getLogger(VPropertyConfiguration.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return null;
  }
}

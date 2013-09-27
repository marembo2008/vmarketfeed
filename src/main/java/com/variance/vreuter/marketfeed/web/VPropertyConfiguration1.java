/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.anosym.vjax.VMarshaller;
import com.anosym.vjax.VMarshallerConstants;
import com.anosym.vjax.VXMLBindingException;
import com.anosym.vjax.xml.VElement;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.property.VReuterProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.swing.tree.TreeModel;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.TreeNode;

/**
 *
 * @author Marembo
 */
public class VPropertyConfiguration1 {

    public static class ColumnRecord {

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
            if (colIndex != null) {
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
    }
    @EJB
    private VReuterRemote reuter;
    private TreeModel model;
    private TreeNode treeNode;
    private VElement selectedElement;
    private TreeNode selectedElementNode;
    private VElement mainElement;
    private VElement columnRecords;

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public boolean isNodeSelected() {
        if (selectedElementNode == null) {
            return false;
        }
        VElement sElement = null;
        Object obj = selectedElementNode.getData();
        if (obj instanceof VElement) {
            sElement = (VElement) obj;
        }
        return this.selectedElementNode != null && sElement != null && !sElement.hasChildren();
    }

    @PostConstruct
    public void onStart() {
        try {
            //create the mutable tree node and model
            VReuterProperty props = reuter.getReuterProperties();
            VMarshaller<VReuterProperty> m = new VMarshaller<VReuterProperty>();
            mainElement = m.marshall(props);
            //add children
            treeNode = new DefaultTreeNode("Root", null);
            addElementNode(mainElement, treeNode);
            //create column indeces
            for (int i = 0; i < VRecordResponseData.values().length; i++) {
                columnIndexItems.add(new SelectItem(i));
            }
        } catch (VXMLBindingException ex) {
            Logger.getLogger(VPropertyConfiguration1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reload() {
        treeNode = null;
        onStart();
    }

    private void addElementNode(VElement elem, TreeNode parent) {
        if (elem.getMarkup().equalsIgnoreCase("columnRecordId")) {
            TreeNode node = new DefaultTreeNode(elem, parent);
            columnRecords = elem.getChild(VMarshallerConstants.COLLECTION_ELEMENT_MARKUP);
            elem.removeChild(columnRecords);
            return;
        }
        TreeNode node = new DefaultTreeNode(elem, parent);
        if (elem.hasChildren()) {
            for (VElement e : elem.getChildren()) {
                addElementNode(e, node);
            }
        }
    }
    private List<SelectItem> columnIndexItems = new ArrayList<SelectItem>();

    public List<SelectItem> getColumnIndexItems() {
        return columnIndexItems;
    }

    public void processValueChange(Integer event) {
        if (columnRecord != null) {
            String oldindex = columnRecord.getIndex();
            System.out.println("Value Change Event:    " + oldindex);
            if (oldindex != null && !oldindex.isEmpty() && !oldindex.equals("0")) {
                int oi = Integer.parseInt(oldindex);
                SelectItem item = new SelectItem(oi);
                if (!columnIndexItems.contains(item)) {
                    columnIndexItems.add(item);
                }
            }
        }
        if (event != 0) {
            for (ListIterator<SelectItem> it_ = columnIndexItems.listIterator(); it_.hasNext();) {
                SelectItem it = it_.next();
                if (it.getValue().equals(event)) {
                    it_.remove();
                    break;
                }
            }
        }
    }

    private class ColumnRecordDataModel implements SelectableDataModel<ColumnRecord> {

        @Override
        public Object getRowKey(ColumnRecord object) {
            return columnRecordMappings.indexOf(object);
        }

        @Override
        public ColumnRecord getRowData(String rowKey) {
            return columnRecordMappings.get(Integer.parseInt(rowKey));
        }
    }
    private ColumnRecordDataModel recordModel;

    public SelectableDataModel<ColumnRecord> getRecordModel() {
        if (recordModel == null) {
            recordModel = new ColumnRecordDataModel();
        }
        return recordModel;
    }

    public long getReloadTime() {
        return this.reuter.getReuterProperties().getPropertyReloadMinutes() * 60 * 1000;
    }

    public boolean isContentElement() {
        return this.selectedElement != null && this.selectedElement.getChildren().isEmpty() && !selectedElement.getMarkup().equals("columnRecordId");
    }

    public void nodeSelected(NodeSelectEvent event) {
        nodeExpand(null);
    }

    public void nodeExpand(NodeExpandEvent event) {
        if (selectedElementNode != null) {
            Object obj = selectedElementNode.getData();
            if (obj instanceof VElement) {
                selectedElement = (VElement) obj;
            }
        }
    }

    public void nodeCollapse(NodeCollapseEvent event) {
        nodeExpand(null);
    }

    public boolean isRenderElement() {
        System.err.println("IsRenderElement:  " + selectedElement);
        return selectedElement != null;
    }

    public boolean isRenderElementContent() {
        return isRenderElement() && !selectedElement.hasChildren();
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
    private List<ColumnRecord> columnRecordMappings;
    private ColumnRecord columnRecord;

    public ColumnRecord getColumnRecord() {
        return columnRecord;
    }

    public void setColumnRecord(ColumnRecord columnRecord) {
        this.columnRecord = columnRecord;
    }

    public List<ColumnRecord> getColumnRecords() {
        if (columnRecordMappings == null) {
            columnRecordMappings = new ArrayList<ColumnRecord>();
            for (VElement el : columnRecords.getChildren()) {
                ColumnRecord cr = new ColumnRecord(el);
                columnRecordMappings.add(cr);
            }
        }
        return columnRecordMappings;
    }

    public VElement getSelectedElement() {
        return selectedElement;
    }

    public void setSelectedElement(VElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    public TreeNode getSelectedElementNode() {
        return selectedElementNode;
    }

    public void setSelectedElementNode(TreeNode selectedElementNode) {
        this.selectedElementNode = selectedElementNode;
    }

    public TreeModel getModel() {
        return model;
    }

    public void setModel(TreeModel model) {
        this.model = model;
    }

    public void elementSelected(ActionEvent evt) {
    }

    public void valueChanged(ValueChangeEvent event) {
        System.out.println(event.getNewValue());
    }
    private volatile boolean saved = false;

    public String getUpdate() {
        if (mainElement != null) {
            try {
                VMarshaller<VReuterProperty> m = new VMarshaller<VReuterProperty>();
                VElement nodeElement = (VElement) treeNode.getChildren().get(0).getData();
                VElement el = mainElement.findChild("columnRecordId");
                el.addChild(columnRecords);
                reuter.setReuterProperties(m.unmarshall(mainElement));
                saved = true;
                new Thread() {

                    @Override
                    public void run() {
                        final Object s = new Object();
                        synchronized (s) {
                            try {
                                s.wait(3000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(VPropertyConfiguration1.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        saved = false;
                    }
                }.start();
            } catch (VXMLBindingException ex) {
                Logger.getLogger(VPropertyConfiguration1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public String getSaved() {
        return saved ? "Successfully Saved" : "";
    }
}

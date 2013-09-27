/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.property;

import com.anosym.utilities.Constraint;
import com.anosym.utilities.ConstraintArrayList;
import com.anosym.vjax.annotations.CollectionElement;
import com.anosym.vjax.annotations.Comment;
import com.anosym.vjax.annotations.Constant;
import com.anosym.vjax.annotations.Displayable;
import com.anosym.vjax.annotations.EnumMarkup;
import com.anosym.vjax.annotations.MapElement;
import com.anosym.vjax.annotations.Marshallable;
import com.anosym.vjax.annotations.Position;
import com.flemax.jdynamics.configuration.JDataSourceType;
import com.flemax.jdynamics.configuration.JDatabaseConfiguration;
import com.variance.vreuter.marketfeed.deal.VDealType;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import java.util.*;

/**
 *
 * @author Administrator
 */
public class VDataSourceConfiguration {

    @EnumMarkup("table-key")
    public static enum VTableKey {

        FX_TABLE_CONFIG_NAME,
        MM_TABLE_CONFIG_NAME
    }

    public class VTableName {

        private VTableKey tableKey;
        private String tableName;

        public VTableName(VTableKey tableKey, String tableName) {
            this.tableKey = tableKey;
            this.tableName = tableName;
        }

        public VTableName(VTableKey tableKey) {
            this.tableKey = tableKey;
        }

        public VTableName() {
        }

        @Displayable(false)
        public VTableKey getTableKey() {
            return tableKey;
        }

        @Position(index = 0)
        @Marshallable(marshal = true, write = false)
        @Constant
        public String getTableNameKey() {
            return tableKey.name();
        }

        public void setTableKey(VTableKey tableKey) {
            this.tableKey = tableKey;
        }

        @Position(index = 1)
        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
    }

    public static class VDataSource {

        private JDataSourceType dataSourceType;
        private Object configurations;

        public VDataSource(JDataSourceType dataSourceType) {
            this.dataSourceType = dataSourceType;
        }

        public VDataSource() {
        }

        public VDataSource(JDataSourceType dataSourceType, Object configurations) {
            this.dataSourceType = dataSourceType;
            this.configurations = configurations;
        }

        public Object getConfigurations() {
            return configurations;
        }

        public void setConfigurations(Object configurations) {
            this.configurations = configurations;
        }

        public JDataSourceType getDataSourceType() {
            return dataSourceType;
        }

        public void setDataSourceType(JDataSourceType dataSourceType) {
            this.dataSourceType = dataSourceType;
        }
    }
    public static final VTableKey FX_TABLE_CONFIG_NAME = VTableKey.FX_TABLE_CONFIG_NAME;
    public static final VTableKey MM_TABLE_CONFIG_NAME = VTableKey.MM_TABLE_CONFIG_NAME;
    private Map<VTableKey, VTableConfiguration> tableConfigurationMappings;
    private boolean dealAndConversationCombined;
    private String conversationTable;
    private Map<VRecordResponseData, String> conversationTextMapping;
    private List<VDealType> availableDealTypes;
    private List<VDataSource> dataSourceTypes;
    private List<VTableName> tableNames;
    private String dataSeperator;

    public VDataSourceConfiguration() {
        dataSeperator = ",";
        tableConfigurationMappings = new EnumMap<VTableKey, VTableConfiguration>(VTableKey.class);
        conversationTextMapping = new EnumMap<VRecordResponseData, String>(VRecordResponseData.class);
        conversationTextMapping.put(VRecordResponseData.TICKET_ID, "ticket_id");
        conversationTextMapping.put(VRecordResponseData.CONVERSATION_TEXT, "conv_text");
        tableNames = new ArrayList<VTableName>();
        //add at least to show as an example
        this.tableConfigurationMappings.put(FX_TABLE_CONFIG_NAME, new VTableConfiguration());
        this.tableConfigurationMappings.put(MM_TABLE_CONFIG_NAME, new VTableConfiguration());
        tableNames.add(new VTableName(VTableKey.FX_TABLE_CONFIG_NAME, ""));
        tableNames.add(new VTableName(VTableKey.MM_TABLE_CONFIG_NAME, ""));
        this.dealAndConversationCombined = true;
        this.conversationTable = "deal_conversations";
        availableDealTypes = new ConstraintArrayList<VDealType>(new Constraint<VDealType>() {

            @Override
            public boolean accept(VDealType element) {
                return !availableDealTypes.contains(element);
            }
        });
        availableDealTypes.addAll(Arrays.asList(VDealType.basicValues()));
        dataSourceTypes = new ArrayList<VDataSource>();
        for (JDataSourceType dst : JDataSourceType.values()) {
            if (dst == JDataSourceType.DATABASE) {
                dataSourceTypes.add(new VDataSource(dst, new JDatabaseConfiguration()));
            } else {
                dataSourceTypes.add(new VDataSource(dst));
            }
        }
    }

    @Comment("Data Seperator/Delimiter. Used only if relevant."
    + "\nDelimits individual TOF data field mostly for xls or flat format datasources.")
    public String getDataSeperator() {
        return dataSeperator;
    }

    public void setDataSeperator(String dataSeperator) {
        this.dataSeperator = dataSeperator;
    }

    @Comment("The currently supported data source for the marketfeed ticket data."
    + "\nEnsure that only one data source is enabled."
    + "\nThe system does not guarantee which data source will be used if more than one is enabled."
    + "\nFurther more, if the loaded data source does not exists or cannot be accessed, the system will fail.")
    @CollectionElement("data-source")
    @Position(index = 0)
    public List<VDataSource> getDataSourceTypes() {
        return dataSourceTypes;
    }

    public void setDataSourceTypes(List<VDataSource> dataSourceTypes) {
        this.dataSourceTypes = dataSourceTypes;
    }

    public VDealType findDealTypeInstance(int dealIndex) {
        for (VDealType v : availableDealTypes) {
            if (v.getDealIndex() == dealIndex) {
                return v;
            }
        }
        return VDealType.UNKNOWN_DEAL; //does not know its index currently
    }

    @Marshallable(marshal = false)
    public List<VDealType> getAvailableDealTypes() {
        return availableDealTypes;
    }

    public String getConversationTable() {
        return conversationTable;
    }

    public void setConversationTable(String conversationTable) {
        this.conversationTable = conversationTable;
    }

    @MapElement(key = "conversation-field", value = "mapping-table-column")
    public Map<VRecordResponseData, String> getConversationTextMapping() {
        return conversationTextMapping;
    }

    public void setConversationTextMapping(Map<VRecordResponseData, String> conversationTextMapping) {
        this.conversationTextMapping.clear();
        this.conversationTextMapping.putAll(conversationTextMapping);
    }

    @Comment("Associates the Money market and foreign exchange deals with specific tables in the"
    + "\ndatabase. When the names have been set, there is no guarantee they will take effect"
    + "\nimmediately, so a restart of the server may be appropriate.")
    @CollectionElement("table")
    public List<VTableName> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<VTableName> tableNames) {
        this.tableNames = tableNames;
        // See setting setTableConfigurationMappings(Map) for the same implementation
        // Since we cannot tell when either of the same is set, we do this on both
        // of the setters.
        for (VTableName vtn : tableNames) {
            setTableName(vtn);
        }
    }

    private void setTableName(VTableName vtn) {
        if (tableConfigurationMappings.containsKey(vtn.getTableKey())) {
            this.tableConfigurationMappings.get(vtn.getTableKey()).setTableName(vtn.getTableName());
        } else {
            this.tableConfigurationMappings.put(vtn.getTableKey(), new VTableConfiguration(vtn.getTableName()));
        }
    }

    @Comment("If true, implies that deal data and conversation data are combined in a single table."
    + "\nOtherwise the relevant properties of conversation text data must be specified.")
    public boolean isDealAndConversationCombined() {
        return dealAndConversationCombined;
    }

    public void setDealAndConversationCombined(boolean dealAndConversationCombined) {
        this.dealAndConversationCombined = dealAndConversationCombined;
    }

    @MapElement(key = "table", value = "configuration")
    @Displayable(false)
    public Map<VTableKey, VTableConfiguration> getTableConfigurationMappings() {
        return tableConfigurationMappings;
    }

    public void setTableConfigurationMappings(Map<VTableKey, VTableConfiguration> tableConfigurationMappings) {
        this.tableConfigurationMappings = tableConfigurationMappings;
        // See setting setTableNames(List) for the same implementation
        // Since we cannot tell when either of the same is set, we do this on both
        // of the setters.
        for (VTableName vtn : tableNames) {
            setTableName(vtn);
        }
    }

    @Marshallable(marshal = false)
    public List<VTableConfiguration> getTableConfigurations() {
        return new ArrayList<VTableConfiguration>(tableConfigurationMappings.values());
    }

    @Marshallable(marshal = false)
    public VTableConfiguration getTableConfiguration(VTableKey tableKey) {
        return this.tableConfigurationMappings.get(tableKey);
    }

    public VTableConfiguration setTableConfiguration(VTableKey tableKey, VTableConfiguration tableConfiguration) {
        return this.tableConfigurationMappings.put(tableKey, tableConfiguration);
    }
}

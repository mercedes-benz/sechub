// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanTypeSummaryDetailData {

    private static final Logger LOG = LoggerFactory.getLogger(ScanTypeSummaryDetailData.class);

    @JsonDeserialize(using = FindingSummaryDetailDataTreeMapDeserializer.class)
    Map<String, ScanTypeSummaryFindingOverviewData> high = new TreeMap<>();

    @JsonDeserialize(using = FindingSummaryDetailDataTreeMapDeserializer.class)
    Map<String, ScanTypeSummaryFindingOverviewData> medium = new TreeMap<>();

    @JsonDeserialize(using = FindingSummaryDetailDataTreeMapDeserializer.class)
    Map<String, ScanTypeSummaryFindingOverviewData> low = new TreeMap<>();

    /**
     * Adds given finding to calculation data. Be aware: there is no duplication
     * check
     * 
     * @param finding the finding to inspect and add to calculation
     */
    public void addToCalculation(SecHubFinding finding) {
        switch (finding.getSeverity()) {
        case HIGH, CRITICAL -> incrementSummary(high, finding);
        case MEDIUM -> incrementSummary(medium, finding);
        case UNCLASSIFIED, LOW, INFO -> incrementSummary(low, finding);
        }
    }

    protected void incrementSummary(Map<String, ScanTypeSummaryFindingOverviewData> targetDetailMap, SecHubFinding finding) {
        Integer cweId = finding.getCweId();
        String name = finding.getName() != null ? finding.getName() : "no_name";
        
        ScanTypeSummaryFindingOverviewData summaryDetailData = targetDetailMap.get(name);
        if (summaryDetailData == null) {
            
            summaryDetailData = new ScanTypeSummaryFindingOverviewData(cweId, name);
            targetDetailMap.put(name, summaryDetailData);
        }
        
        summaryDetailData.incrementCount();
    }

    public List<ScanTypeSummaryFindingOverviewData> getHigh() {
        return new ArrayList<>(high.values());
    }

    public List<ScanTypeSummaryFindingOverviewData> getMedium() {
        return new ArrayList<>(medium.values());
    }

    public List<ScanTypeSummaryFindingOverviewData> getLow() {
        return new ArrayList<>(low.values());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class ScanTypeSummaryFindingOverviewData {
        private Integer cweId;
        private String name;
        private long count;

        ScanTypeSummaryFindingOverviewData(Integer cweId, String name) {
            this.cweId = cweId;
            this.name = name;
        }

        public void incrementCount() {
            this.count++;
        }

        public Integer getCweId() {
            return cweId;
        }

        public String getName() {
            return name;
        }

        public long getCount() {
            return count;
        }
    }

    private static class FindingSummaryDetailDataTreeMapDeserializer extends StdDeserializer<Map<String, ScanTypeSummaryFindingOverviewData>> {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        public FindingSummaryDetailDataTreeMapDeserializer() {
            this(null);
        }

        protected FindingSummaryDetailDataTreeMapDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public TreeMap<String, ScanTypeSummaryFindingOverviewData> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
            TreeMap<String, ScanTypeSummaryFindingOverviewData> treeMap = new TreeMap<>();
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            node.fields().forEachRemaining(entry -> {
                try {
                    String key = entry.getKey();
                    ScanTypeSummaryFindingOverviewData value = entry.getValue().traverse(jsonParser.getCodec()).readValueAs(ScanTypeSummaryFindingOverviewData.class);
                    treeMap.put(key, value);
                } catch (IOException e) {
                    LOG.debug("JSON deserialization failed \n" + e);
                }
            });
            return treeMap;
        }
    }
}

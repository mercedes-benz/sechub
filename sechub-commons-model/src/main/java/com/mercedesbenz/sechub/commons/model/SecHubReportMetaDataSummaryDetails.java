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
public class SecHubReportMetaDataSummaryDetails {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubReportMetaDataSummaryDetails.class);

    @JsonDeserialize(using = TreeMapDeserializer.class)
    Map<String, SeverityDetails> high = new TreeMap<>();

    @JsonDeserialize(using = TreeMapDeserializer.class)
    Map<String, SeverityDetails> medium = new TreeMap<>();

    @JsonDeserialize(using = TreeMapDeserializer.class)
    Map<String, SeverityDetails> low = new TreeMap<>();

    public void detailsHelper(SecHubFinding finding) {
        switch (finding.getSeverity()) {
        case HIGH, CRITICAL -> detailsFiller(high, finding);
        case MEDIUM -> detailsFiller(medium, finding);
        case UNCLASSIFIED, LOW, INFO -> detailsFiller(low, finding);
        }
    }

    protected void detailsFiller(Map<String, SeverityDetails> helperMap, SecHubFinding finding) {
        Integer cweId = finding.getCweId();
        String name = finding.getName() != null ? finding.getName() : "no_name";
        SeverityDetails severityDetails = helperMap.get(name);
        if (severityDetails != null) {
            severityDetails.incrementCount();
        } else {
            helperMap.put(name, new SeverityDetails(cweId, name));
        }
    }

    public List<SeverityDetails> getHigh() {
        return new ArrayList<>(high.values());
    }

    public List<SeverityDetails> getMedium() {
        return new ArrayList<>(medium.values());
    }

    public List<SeverityDetails> getLow() {
        return new ArrayList<>(low.values());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class SeverityDetails {
        private Integer cweId;
        private String name;
        private long count;

        SeverityDetails(Integer cweId, String name) {
            this.cweId = cweId;
            this.name = name;
            this.count = 1;
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

    private static class TreeMapDeserializer extends StdDeserializer<Map<String, SeverityDetails>> {

        public TreeMapDeserializer() {
            this(null);
        }

        protected TreeMapDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public TreeMap<String, SeverityDetails> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
            TreeMap<String, SeverityDetails> treeMap = new TreeMap<>();
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            node.fields().forEachRemaining(entry -> {
                try {
                    String key = entry.getKey();
                    SeverityDetails value = entry.getValue().traverse(jsonParser.getCodec()).readValueAs(SeverityDetails.class);
                    treeMap.put(key, value);
                } catch (IOException e) {
                    LOG.debug("JSON deserialization failed \n" + e);
                }
            });
            return treeMap;
        }
    }
}

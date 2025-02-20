package com.mercedesbenz.sechub.commons.communication;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mercedesbenz.sechub.commons.model.JSONable;

public class CommunicationDataConverterConfig implements JSONable<CommunicationDataConverterConfig> {

    private Send send = new Send();

    private static final CommunicationDataConverterConfig importInstance = new CommunicationDataConverterConfig();

    private Receive receive = new Receive();

    public Send getSend() {
        return send;
    }

    public Receive getReceive() {
        return receive;
    }

    @Override
    public Class<CommunicationDataConverterConfig> getJSONTargetClass() {
        return CommunicationDataConverterConfig.class;
    }

    public class Send {
        @JsonProperty("targetType")
        private CommunicationDataConversionType targetType;

        @JsonProperty("mapping")
        private Map<String, String> mapping;

        // Getters and Setters
        public CommunicationDataConversionType getTargetType() {
            return targetType;
        }

        public void setTargetType(CommunicationDataConversionType targetType) {
            this.targetType = targetType;
        }

        public Map<String, String> getMapping() {
            return mapping;
        }

    }

    public class Receive {
        @JsonProperty("sourceType")
        private CommunicationDataConversionType sourceType;

        private Map<String, String> mapping;

        // Getters and Setters
        public CommunicationDataConversionType getSourceType() {
            return sourceType;
        }

        public void setSourceType(CommunicationDataConversionType sourceType) {
            this.sourceType = sourceType;
        }

        public Map<String, String> getMapping() {
            return mapping;
        }

    }

    public static CommunicationDataConverterConfig fromJSONString(String json) {
        return importInstance.fromJSON(json);
    }
}

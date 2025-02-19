package com.mercedesbenz.sechub.commons.communication;

import static java.util.Objects.*;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.mercedesbenz.sechub.commons.communication.CommunicationDataConverterConfig.Receive;
import com.mercedesbenz.sechub.commons.communication.CommunicationDataConverterConfig.Send;
import com.mercedesbenz.sechub.commons.model.JSONConverter;

/**
 * Converts data for communication between sending and receiving. The way of
 * conversion can be configured by configuration object.
 *
 * A usage example:
 *
 * <pre>
 * <code>
 * CommunicationDataConverter converter = new CommunicationDataConverter(config);
 *
 * val requestData = new HashMap<String,String>();
 * requestData.put("cweId", finding.getCweId());
 * requestData.put("language", finding.getLanguage());
 * requestData.put("snippet", finding.getSnippet());
 *
 * String jsonToSend = converter.convertForSending(data);
 *
 * String jsonReceived = issueDescriberAiService.describe(jsonToSend);
 *
 * Map<String, String> resultMappingData= converter.convertFromReceived(jsonReceived);
 * String failed = resultMappingData.get("ai_service_failure")
 * String descriptionAsMarkdown = resultMappingData.get("content_markdown");
 * // ...
 * </code>
 * </pre>
 *
 *
 */
public class CommunicationDataConverter {

    private CommunicationDataConverterConfig config;

    /**
     * Creates the converter
     *
     * @param config the configuration to use, may not be <code>null</code>
     *
     * @throws IllegalArgumentException when send or receive
     *                                  {@link CommunicationDataConversionType} is
     *                                  not supported
     */
    public CommunicationDataConverter(CommunicationDataConverterConfig config) {
        requireNonNull(config, "Config may not be null!");

        Send send = requireNonNull(config.getSend(), "send may not be null in config");
        if (send.getTargetType() != CommunicationDataConversionType.JSON) {
            throw new IllegalArgumentException("target type not supported:" + send.getTargetType());
        }
        Receive receive = requireNonNull(config.getReceive(), "receive may not be null in config");
        if (receive.getSourceType() != CommunicationDataConversionType.JSON) {
            throw new IllegalArgumentException("source type not supported:" + receive.getSourceType());
        }

        this.config = config;
    }

    /**
     * Converts given data in configured way to wanted string representation to
     * send.
     *
     * @param data the data to convert, if <code>null</code> the returned string
     *             will always be empty
     * @return string
     */
    public String convertForSending(Map<String, String> data) {
        if (data == null) {
            return "";
        }

        Send send = config.getSend();

        SortedMap<String, String> resultMap = new TreeMap<>();

        send.getMapping().forEach((source, target) -> {
            resultMap.put(target, data.get(source));
        });

        return JSONConverter.get().toJSON(resultMap);
    }

    /**
     * Converts received string input to wanted, configured format
     *
     * @param received the received data as a string, if the data is
     *                 <code>null</code> an empty map will be returned.
     * @return map, never <code>null</code>
     */
    public Map<String, String> convertFromReceived(String received) {
        if (received == null) {
            return new HashMap<>(0);
        }
        Map<?, ?> map = JSONConverter.get().fromJSON(Map.class, received);
        SortedMap<String, String> resultMap = new TreeMap<>();
        Receive receive = config.getReceive();

        receive.getMapping().forEach((source, target) -> {
            resultMap.put(target, (String) map.get(source));
        });

        return resultMap;
    }

}

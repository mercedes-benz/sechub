// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;

/**
 * This class can transform metadata representations inside a map into a
 * {@link SecHubConfigurationMetaData} object and vice versa
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class SecHubConfigurationMetaDataMapTransformer {

    private static final String PREFIX_METADATA_LABELS = "metadata.labels.";

    /**
     * Transforms a map into a {@link SecHubConfigurationMetaData} object. The map
     * keys are lower cased flat variants of the hierarchical field names. There
     * will be no validation for content correctness (e.g. label key names have some
     * constraints) - this must be done afterwards.<br>
     * <br>
     * An example:<br>
     * "metadata.labels.${labelKeyName}" will be transformed to a label entry with
     * name "${labelKeyName}"
     *
     * @param map
     * @return meta data configuration object
     */
    public SecHubConfigurationMetaData transform(Map<String, String> map) {
        SecHubConfigurationMetaData metaData = new SecHubConfigurationMetaData();
        if (map == null || map.isEmpty()) {
            return metaData;
        }

        Map<String, String> labels = metaData.getLabels();
        for (String parameterkey : map.keySet()) {
            if (parameterkey == null) {
                continue;
            }

            if (parameterkey.startsWith(PREFIX_METADATA_LABELS)) {
                String labelKey = parameterkey.substring(PREFIX_METADATA_LABELS.length());
                String labelValue = map.get(parameterkey);

                labels.put(labelKey, labelValue);
            }
        }

        return metaData;
    }

    /**
     *
     * Transforms a {@link SecHubConfigurationMetaData} object into a map. The map
     * keys are lower cased flat variants of the hierarchical field names. There
     * will be no validation for content correctness (e.g. label key names have some
     * constraints) - this must be done afterwards.<br>
     * <br>
     * An example:<br>
     * a label entry with name "${labelKeyName}" will be transformed to
     * "metadata.labels.${labelKeyName}"
     *
     * @param mapContainingMetaDataParameters
     * @return meta data configuration object
     */
    public Map<String, String> transform(SecHubConfigurationMetaData metaData) {
        Map<String, String> result = new TreeMap<>();
        if (metaData == null) {
            return result;
        }
        Map<String, String> labels = metaData.getLabels();
        if (labels != null) {
            for (Entry<String, String> entry : labels.entrySet()) {
                result.put(PREFIX_METADATA_LABELS + entry.getKey(), entry.getValue());

            }
        }
        return result;
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Regions;
import com.amazonaws.util.EC2MetadataUtils;

public class AwsRegionResolver {

    private static final String CURRENT = "current";
    private static final String DEFAULT = "default";
    private static final Logger LOG = LoggerFactory.getLogger(AwsRegionResolver.class);

    public static String resolve(String regionAsText) {
        LOG.debug("Region to resolve: {}", regionAsText);

        String result = null;

        if (DEFAULT.equalsIgnoreCase(regionAsText)) {
            result = Regions.DEFAULT_REGION.getName();
        } else if (CURRENT.equalsIgnoreCase(regionAsText)) {
            result = EC2MetadataUtils.getEC2InstanceRegion();
            if (result == null) {
                LOG.warn("Was not able to resolve current region!");
                return null;
            }
        } else if (regionAsText != null && !regionAsText.isBlank()) {
            result = regionAsText;
        }
        LOG.info("Resolved AWS region from '{}' to '{}'", regionAsText, result);
        return result;
    }

}

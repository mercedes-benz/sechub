// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMetaDataInspection implements MetaDataInspection {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingMetaDataInspection.class);

    private String id;

    public LoggingMetaDataInspection(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public MetaDataInspection notice(String key, Object value) {
        LOG.debug("handle key:{}, value:{}", key, value);
        return this;
    }

}

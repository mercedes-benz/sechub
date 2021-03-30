// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

public enum SecHubTimeUnit {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({"millisecond", "milliseconds"})
    MILLISECOND(1),
    
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({"second", "seconds"})
    SECOND(1000),
    
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({"minute", "minutes"})
    MINUTE(1000 * 60),

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({"hour", "hours"})
    HOUR(1000 * 60 * 60),

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonAlias({"day", "days"})
    DAY(1000 * 60 * 60 * 24);
    
    private int multiplicatorMilliseconds;

    SecHubTimeUnit(int multiplicatorMilliseconds) {
        this.multiplicatorMilliseconds = multiplicatorMilliseconds;
    }
    
    public long getMultiplicatorMilliseconds() {
        return multiplicatorMilliseconds;
    }
}

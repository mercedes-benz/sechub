// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import com.fasterxml.jackson.annotation.JsonFormat;

public enum ActionType {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    INPUT,
    
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    CLICK,
    
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    PASSWORD,
    
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    USERNAME,
    
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    WAIT;
    
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}

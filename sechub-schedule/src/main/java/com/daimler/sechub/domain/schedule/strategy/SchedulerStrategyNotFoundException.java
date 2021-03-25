// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.strategy;

public class SchedulerStrategyNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public SchedulerStrategyNotFoundException(String message) {
        super(message);
    }

}

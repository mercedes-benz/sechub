// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

public class CheckmarxFullScanNecessaryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CheckmarxFullScanNecessaryException(String checkMarxMessage) {
        super(checkMarxMessage);
    }

    public String getCheckmarxMessage() {
        return getMessage();
    }
}
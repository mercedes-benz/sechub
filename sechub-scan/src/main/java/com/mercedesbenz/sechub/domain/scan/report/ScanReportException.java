// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

public class ScanReportException extends Exception {

    private static final long serialVersionUID = 2830735712090000359L;

    public ScanReportException(String message) {
        super(message);
    }

    public ScanReportException(String message, Exception reason) {
        super(message, reason);
    }

}

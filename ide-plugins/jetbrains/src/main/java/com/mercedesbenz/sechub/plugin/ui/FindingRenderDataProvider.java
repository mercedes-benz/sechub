// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

import javax.swing.*;

public interface FindingRenderDataProvider {

    public Icon getIconForScanType(ScanType scanType);

    public Icon getIconForTrafficLight (TrafficLight trafficLight);

    public default String getTextForSeverity(Severity severity){
        if (severity ==null){
            return null;
        }
        switch(severity){
            case INFO -> {
                return "Info";
            }
            case UNCLASSIFIED -> {
                return "Unclassified";
            }
            case LOW -> {
                return "Low";
            }
            case MEDIUM -> {
                return "Medium";
            }
            case HIGH -> {
                return "High";
            }
            case CRITICAL -> {
                return "Critical";
            }
        }
        return severity.toString();

    }

    public default String getTextForScanType(ScanType scanType) {
        if (scanType==null){
            return null;
        }
        switch (scanType){
            case CODE_SCAN -> {
                return "Code";
            }
            case WEB_SCAN -> {
                return "Web";
            }
            case INFRA_SCAN -> {
                return "Infra";
            }
            case LICENSE_SCAN -> {
                return "License";
            }
            case SECRET_SCAN -> {
                return "Secret";
            }
            case REPORT -> {
                return "Report";
            }
            case ANALYTICS -> {
                return "Analytics";
            }
            case UNKNOWN -> {
                return "Unknown";
            }
        }
        return null;
    }
}

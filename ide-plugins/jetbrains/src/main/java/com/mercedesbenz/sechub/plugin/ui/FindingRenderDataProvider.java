// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import javax.swing.*;

import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;
import com.mercedesbenz.sechub.api.internal.gen.model.Severity;
import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;

public interface FindingRenderDataProvider {

    Icon getIconForScanType(ScanType scanType);

    Icon getIconForTrafficLight(TrafficLight trafficLight);

    default String getTextForSeverity(Severity severity) {
        if (severity == null) {
            return null;
        }
        switch (severity) {
        case INFO -> {
            return "Info";
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

    default String getTextForScanType(ScanType scanType) {
        if (scanType == null) {
            return null;
        }
        switch (scanType) {
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

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;
import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;
import com.mercedesbenz.sechub.plugin.ui.FindingRenderDataProvider;

public class IntellijRenderDataProvider implements FindingRenderDataProvider {

    @Override
    public Icon getIconForScanType(ScanType scanType) {
        if (scanType == null) {
            return AllIcons.Nodes.Unknown;
        }
        switch (scanType) {
        case CODE_SCAN -> {
            return AllIcons.FileTypes.Any_type;
        }
        case WEB_SCAN -> {
            return AllIcons.General.Web;
        }
        case INFRA_SCAN -> {
            return AllIcons.Webreferences.Server;
        }
        case LICENSE_SCAN -> {
            return AllIcons.General.TodoImportant;
        }
        case SECRET_SCAN -> {
            return AllIcons.CodeWithMe.CwmPermissions;
        }
        case REPORT -> {
            return AllIcons.FileTypes.Properties;
        }
        case ANALYTICS -> {
            return AllIcons.General.InspectionsEye;
        }
        case UNKNOWN -> {
            return AllIcons.FileTypes.Unknown;
        }
        }
        return AllIcons.FileTypes.Any_type;
    }

}

// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import com.daimler.sechub.developertools.admin.ui.action.adapter.ShowProductExecutorTemplatesDialogAction;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Necessarity;
import com.daimler.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Type;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;

public class ShowProductExecutorTemplatesDialogActionFactory {

    public static ShowProductExecutorTemplatesDialogAction createCheckmarxV1Action(UIContext context) {
        TemplatesDialogData data = new TemplatesDialogData();
        data.add(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId(), Type.MAPPING, Necessarity.OPTIONAL,
                "Here we can define presetid mappings for new projects");
        data.add(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId(), Type.MAPPING, Necessarity.MANDATORY,
                "Here we must define teamId mapping for new projects");

        data.add("checkmarx.engineConfigurationName", Type.KEY_VALUE, Necessarity.OPTIONAL, "The engine configuration name. If not set default will be used",
                "somevalue");
        data.add("checkmarx.clientSecret", Type.KEY_VALUE, Necessarity.OPTIONAL, "Normally a static 'secret' - if it ever changes, we can change this here");
        data.add("checkmarx.fullscan.always", Type.KEY_VALUE, Necessarity.OPTIONAL,
                "If enabled, Checkmarx will always do a fullscan. If disabled, checkmarx will do a delta scan and in case it does not work, a fullscan will be performed. Default value is: false",
                "true");

        ShowProductExecutorTemplatesDialogAction action = new ShowProductExecutorTemplatesDialogAction(context, ProductIdentifier.CHECKMARX, 1, data);
        return action;
    }

    public static ShowProductExecutorTemplatesDialogAction createPDS_CODESCAN_V1Action(UIContext context) {
        return createPDSV1Action(context, ProductIdentifier.PDS_CODESCAN, 1);
    }

    public static ShowProductExecutorTemplatesDialogAction createPDS_WEBSCAN_V1Action(UIContext context) {
        return createPDSV1Action(context, ProductIdentifier.PDS_WEBSCAN, 1);
    }

    public static ShowProductExecutorTemplatesDialogAction createPDS_INFRASCAN_V1Action(UIContext context) {
        return createPDSV1Action(context, ProductIdentifier.PDS_INFRASCAN, 1);
    }

    private static ShowProductExecutorTemplatesDialogAction createPDSV1Action(UIContext context, ProductIdentifier identifier, int version) {
        TemplatesDialogData data = new TemplatesDialogData();

        data.add("pds.config.productidentifier", Type.KEY_VALUE, Necessarity.MANDATORY,
                "Contains the product identifier, so PDS knows which part is to call on it's side.", "product-id");

        data.add("pds.productexecutor.forbidden.targettype.intranet", Type.KEY_VALUE, Necessarity.OPTIONAL,
                "When this key is set to true, than this pds instance does not scan intranet", "true");
        data.add("pds.productexecutor.forbidden.targettype.internet", Type.KEY_VALUE, Necessarity.OPTIONAL,
                "When this key is set to true, than this pds instance does not scan internet", "true");
        data.add("pds.productexecutor.timetowait.nextcheck.minute", Type.KEY_VALUE, Necessarity.OPTIONAL,
                "<html>The value will be used to wait for next check on PDS server.<br>When not set the default from PDS install is used instead.</html>");
        data.add("pds.productexecutor.timeout.minutes", Type.KEY_VALUE, Necessarity.OPTIONAL,
                "<html>The value will be used to define timeout for PDS communication.<br>When not define the default from PDS install is used instead.</html>");
        data.add("pds.productexecutor.trustall.certificates", Type.KEY_VALUE, Necessarity.OPTIONAL,
                "When 'true' then all certificates are accepted. Do not use this in production!");

        ShowProductExecutorTemplatesDialogAction action = new ShowProductExecutorTemplatesDialogAction(context, identifier, version, data);
        return action;
    }
}

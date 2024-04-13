// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.commons.pds.PDSKey;
import com.mercedesbenz.sechub.commons.pds.PDSKeyProvider;
import com.mercedesbenz.sechub.developertools.admin.ui.action.adapter.ShowProductExecutorTemplatesDialogAction;
import com.mercedesbenz.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData;
import com.mercedesbenz.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Necessarity;
import com.mercedesbenz.sechub.developertools.admin.ui.action.adapter.TemplatesDialogData.Type;
import com.mercedesbenz.sechub.domain.scan.product.pds.SecHubProductExecutionPDSKeyProvider;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier;

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

    public static ShowProductExecutorTemplatesDialogAction createPDS_ANALYTICS_V1Action(UIContext context) {
        return createPDSV1Action(context, ProductIdentifier.PDS_ANALYTICS, 1);
    }

    public static ShowProductExecutorTemplatesDialogAction createPDS_PREPARE_V1Action(UIContext context) {
        return createPDSV1Action(context, ProductIdentifier.PDS_PREPARE, 1);
    }

    public static ShowProductExecutorTemplatesDialogAction createPDS_WEBSCAN_V1Action(UIContext context) {
        return createPDSV1Action(context, ProductIdentifier.PDS_WEBSCAN, 1);
    }

    public static ShowProductExecutorTemplatesDialogAction createPDS_INFRASCAN_V1Action(UIContext context) {
        return createPDSV1Action(context, ProductIdentifier.PDS_INFRASCAN, 1);
    }

    public static ShowProductExecutorTemplatesDialogAction createPDS_LICENSESCAN_V1Action(UIContext context) {
        return createPDSV1Action(context, ProductIdentifier.PDS_LICENSESCAN, 1);
    }

    private static ShowProductExecutorTemplatesDialogAction createPDSV1Action(UIContext context, ProductIdentifier identifier, int version) {
        TemplatesDialogData templateDialogData = new TemplatesDialogData();

        /** Add necessary parts from providers */
        fetchKeysAndAddContent(templateDialogData, PDSConfigDataKeyProvider.values());
        fetchKeysAndAddContent(templateDialogData, SecHubProductExecutionPDSKeyProvider.values());

        ShowProductExecutorTemplatesDialogAction action = new ShowProductExecutorTemplatesDialogAction(context, identifier, version, templateDialogData);
        return action;
    }

    private static void fetchKeysAndAddContent(TemplatesDialogData data, PDSKeyProvider<?>[] providers) {
        for (PDSKeyProvider<?> provider : providers) {
            PDSKey key = provider.getKey();
            if (key.isGenerated()) {
                /*
                 * generated keys are automatically sent and not necessary to be edited by users
                 */
                continue;
            }
            data.add(key.getId(), Type.KEY_VALUE, calculateNecessarity(key), key.getDescription(), null, key.getDefaultValue());
        }
    }

    private static Necessarity calculateNecessarity(PDSKey key) {
        if (key.isMandatory()) {
            return Necessarity.MANDATORY;
        }
        if (key.isDefaultRecommended()) {
            return Necessarity.RECOMMENDED;
        }
        return Necessarity.OPTIONAL;
    }
}

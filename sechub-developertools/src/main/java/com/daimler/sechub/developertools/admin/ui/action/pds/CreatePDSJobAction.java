// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.pds;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.daimler.sechub.developertools.admin.ui.ThreeButtonDialogResult;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class CreatePDSJobAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public CreatePDSJobAction(UIContext context) {
        super("Create PDS job", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {
        Optional<String> productId = getUserInput("(Fake) sechub job uuid", InputCacheIdentifier.PDS_PRODUCT_ID);
        if (!productId.isPresent()) {
            output("canceled product id");
            return;
        }
        ThreeButtonDialogResult<String> threeButtonDialog = getUserInputFromField("Enter params (key1=value1;key2=value2...");
        if (threeButtonDialog.isCanceled()) {
            output("canceled params");
            return;
            
        }
        String paramsAsString = threeButtonDialog.getValue();
        String[] keyValues = paramsAsString.split(";");
        Map<String, String> params = new LinkedHashMap<>();
        for (String keyValue: keyValues) {
            String[] splitted =  keyValue.split("=");
            
            String key = splitted[0];
            String value = splitted[1];
            
            params.put(key, value);
        }
        output("Params parsed into:\n"+params);
        
        Optional<String> jobUUID = getUserInput("(Fake) sechub job uuid", InputCacheIdentifier.PDS_SECHUB_JOBUUID);
        if (!jobUUID.isPresent()) {
            output("canceled sechu jobuuid");
            return;
        }
        
        String result = pds.createPDSJob(UUID.fromString(jobUUID.get()), productId.get(), params);
        
        outputAsBeautifiedJSONOnSuccess(result);

    }

}
// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.pds;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration.PDSAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class CreatePDSJobAction extends AbstractPDSAction {
    private static final long serialVersionUID = 1L;

    public CreatePDSJobAction(UIContext context) {
        super("Create PDS job", context);
    }

    @Override
    protected void executePDS(PDSAdministration pds) {
        Optional<String> productId = getUserInput("Product ID", InputCacheIdentifier.PDS_PRODUCT_ID);
        if (!productId.isPresent()) {
            output("canceled product id");
            return;
        }
        Optional<String> optParam = getUserInput("Enter params (key1=value1;key2=value2...", InputCacheIdentifier.PDS_JOB_PARAMS);
        if (!optParam.isPresent()) {
            output("canceled params");
            return;

        }
        Map<String, String> params = new LinkedHashMap<>();
        String paramsAsString = optParam.get();
        if (paramsAsString != null) {
            String[] keyValues = paramsAsString.split(";");
            for (String keyValue : keyValues) {
                String[] splitted = keyValue.split("=");

                String key = splitted[0];
                String value = splitted[1];

                params.put(key, value);
            }
        }
        output("Params parsed into:\n" + params);

        Optional<String> sechubJobUUID = getUserInput("SecHub job uuid", InputCacheIdentifier.PDS_SECHUB_JOBUUID);
        if (!sechubJobUUID.isPresent()) {
            output("canceled sechu jobuuid");
            return;
        }

        String result = pds.createPDSJob(UUID.fromString(sechubJobUUID.get()), productId.get(), params);

        outputAsBeautifiedJSONOnSuccess(result);

    }

}
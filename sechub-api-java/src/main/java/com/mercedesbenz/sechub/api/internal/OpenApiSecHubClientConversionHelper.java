// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal;

import java.util.List;

import com.mercedesbenz.sechub.api.internal.gen.AdminApi;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileFetch;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileFetchConfigurationsInner;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileUpdate;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileUpdateConfigurationsInner;

public class OpenApiSecHubClientConversionHelper {

    private AdminApi adminApi;

    public OpenApiSecHubClientConversionHelper(AdminApi adminApi) {
        this.adminApi = adminApi;
    }

    public OpenApiExecutionProfileUpdate fetchProfileAndConvertToUpdateObject(String profileId) throws ApiException {
        OpenApiExecutionProfileUpdate update = new OpenApiExecutionProfileUpdate();

        OpenApiExecutionProfileFetch fetched = adminApi.adminFetchesExecutionProfile(profileId);
        update.setDescription(fetched.getDescription());
        update.setEnabled(fetched.getEnabled());
        List<OpenApiExecutionProfileFetchConfigurationsInner> fetchedConfigurations = fetched.getConfigurations();

        for (OpenApiExecutionProfileFetchConfigurationsInner fetchedConfiguration : fetchedConfigurations) {
            /* we only need the uuid on server side - everything else is ignored */
            String uuid = fetchedConfiguration.getUuid();

            /* add to update again */
            OpenApiExecutionProfileUpdateConfigurationsInner existingItem = new OpenApiExecutionProfileUpdateConfigurationsInner();
            existingItem.setUuid(uuid);
            update.addConfigurationsItem(existingItem);
        }
        return update;
    }

}

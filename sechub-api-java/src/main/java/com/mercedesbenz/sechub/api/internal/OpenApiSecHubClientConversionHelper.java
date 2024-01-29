// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.internal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.internal.gen.AdminApi;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileFetch;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileFetchConfigurationsInner;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileUpdate;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiExecutionProfileUpdateConfigurationsInner;
import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiScanJob;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public class OpenApiSecHubClientConversionHelper {

    private static final Logger LOG = LoggerFactory.getLogger(OpenApiSecHubClientConversionHelper.class);

    public OpenApiSecHubClientConversionHelper() {
    }

    public OpenApiExecutionProfileUpdate fetchProfileAndConvertToUpdateObject(String profileId, AdminApi adminApi) throws ApiException {
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

    public OpenApiScanJob convertToOpenApiScanJob(SecHubConfigurationModel configuration) {

        String configAsJson = JSONConverter.get().toJSON(configuration, true);
        LOG.debug("configAsJson=\n{}", configAsJson);

        OpenApiScanJob openApiScanJob = JSONConverter.get().fromJSON(OpenApiScanJob.class, configAsJson);
        if (LOG.isDebugEnabled()) {
            String openApiJSON = JSONConverter.get().toJSON(openApiScanJob, true);
            LOG.debug("openApiJSON=\n{}", openApiJSON);
        }

        return openApiScanJob;
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareStatus;
import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModule;

@Service
public class PrepareWrapperPreparationService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperPreparationService.class);

    @Autowired
    PrepareWrapperEnvironment environment;

    @Autowired
    PrepareWrapperContextFactory factory;

    @Autowired
    List<PrepareWrapperModule> modules = new ArrayList<>();

    public AdapterExecutionResult startPreparation() throws IOException {

        LOG.debug("Start preparation");
        PrepareWrapperContext context = factory.create(environment);
        SecHubRemoteDataConfiguration remoteDataConfiguration = context.getRemoteDataConfiguration();

        if (remoteDataConfiguration == null) {
            LOG.warn("No Remote configuration was found");
            return createAdapterExecutionResult(PrepareStatus.OK, SecHubMessageType.WARNING, "No Remote Configuration found.");
        }

        for (PrepareWrapperModule module : modules) {

            if (module.prepare(context)) {
                context.getUserMessages().add(new SecHubMessage(SecHubMessageType.INFO, "Executed prepare module: " + module.getClass().getSimpleName()));
                PrepareResult result = new PrepareResult(PrepareStatus.OK);
                return new AdapterExecutionResult(result.toString(), context.getUserMessages());
            }
        }

        return createAdapterExecutionResult(PrepareStatus.FAILED, SecHubMessageType.ERROR, "No module was able to prepare the defined remote data.");
    }

    private AdapterExecutionResult createAdapterExecutionResult(PrepareStatus status, SecHubMessageType type, String message) {
        PrepareResult result = new PrepareResult(status);
        SecHubMessage secHubMessage = new SecHubMessage(type, message);
        Collection<SecHubMessage> messages = new ArrayList<>();
        messages.add(secHubMessage);
        return new AdapterExecutionResult(result.toString(), messages);
    }
}

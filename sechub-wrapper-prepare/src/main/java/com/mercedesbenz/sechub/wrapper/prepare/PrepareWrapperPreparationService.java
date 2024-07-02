// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

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
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModule;

@Service
public class PrepareWrapperPreparationService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperPreparationService.class);

    @Autowired
    PrepareWrapperEnvironment environment;

    @Autowired
    PrepareWrapperContextFactory contextFactory;

    @Autowired
    List<PrepareWrapperModule> modules = new ArrayList<>();

    @Autowired
    PrepareWrapperProxySupport proxySupport;

    public AdapterExecutionResult startPreparation() throws IOException {

        LOG.debug("Start preparation");
        PrepareWrapperContext context = contextFactory.create(environment);
        SecHubRemoteDataConfiguration remoteDataConfiguration = context.getRemoteDataConfiguration();

        if (remoteDataConfiguration == null) {
            LOG.warn("No Remote configuration was found");
            return createAdapterExecutionResult(PrepareStatus.OK, SecHubMessageType.WARNING, "No Remote Configuration found.");
        }

        String location = remoteDataConfiguration.getLocation();
        if (location == null || location.isBlank()) {
            return createAdapterExecutionResult(PrepareStatus.FAILED, SecHubMessageType.ERROR, "Remote Configuration has no location defined!");
        }

        LOG.info("Start preparation with following modules: {}", modules);
        setUpSystemProperties(context);

        for (PrepareWrapperModule module : modules) {
            if (!module.isEnabled()) {
                LOG.debug("Module: {} is not enabled - so skipped", module);
                continue;
            }

            if (module.isResponsibleToPrepare(context)) {
                LOG.debug("Module: {} is responsible and will be used to prepare", module);

                module.prepare(context);

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

    private void setUpSystemProperties(PrepareWrapperContext context) {
        proxySupport.setUpProxy(context.getRemoteDataConfiguration().getLocation());
    }
}

package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperRemoteConfigurationExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareStatus;
import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.moduls.PrepareWrapperModule;

@Service
public class PrepareWrapperPreparationService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperPreparationService.class);

    @Autowired
    PrepareWrapperEnvironment environment;

    @Autowired
    PrepareWrapperContextFactory factory;

    @Autowired
    PrepareWrapperRemoteConfigurationExtractor extractor;

    private List<PrepareWrapperModule> modules = new ArrayList<>();

    public AdapterExecutionResult startPreparation() throws IOException {

        LOG.debug("Start preparation");
        PrepareWrapperContext context = factory.create(environment);
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = extractor.extractRemoteConfiguration(context.getSecHubConfiguration());

        if (remoteDataConfigurationList.isEmpty()) {
            LOG.warn("No Remote configuration was found");
            PrepareResult result = new PrepareResult(PrepareStatus.OK);
            SecHubMessage message = new SecHubMessage(SecHubMessageType.WARNING, "No Remote Configuration found");
            Collection<SecHubMessage> collection = new ArrayList<>();
            collection.add(message);
            return new AdapterExecutionResult(result.toString(), collection);
        }
        for (PrepareWrapperModule module : modules) {
            SecHubConfigurationModel sechubConfiguration = context.getSecHubConfiguration();
            if (module.isAbleToPrepare(sechubConfiguration)) {
                module.prepare(sechubConfiguration, context.getEnvironment().getPdsPrepareUploadFolderDirectory());
            }
        }
        PrepareResult result = new PrepareResult(PrepareStatus.OK);
        return new AdapterExecutionResult(result.toString());
    }

}

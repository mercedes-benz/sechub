package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import java.util.ArrayList;
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
import com.mercedesbenz.sechub.wrapper.prepare.moduls.PrepareWrapperModule;

@Service
public class PrepareWrapperPreparationService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperPreparationService.class);

    @Autowired
    PrepareWrapperEnvironment environment;

    @Autowired
    PrepareWrapperContextFactory factory;

    private List<PrepareWrapperModule> modules = new ArrayList<>();

    public AdapterExecutionResult startPreparation() {

        LOG.debug("Start preparation");
        PrepareWrapperContext context = factory.create(environment);
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = resolveRemoteConfigurationsFromSecHubModel(context);
        if (remoteDataConfigurationList.isEmpty()) {
            // TODO: 27.03.24 laura is it ok that no remote data was configured?
            LOG.warn("No Remote configuration was found");
            PrepareResult result = new PrepareResult(PrepareStatus.OK);
            return new AdapterExecutionResult(result.toString());
        }

        // TODO: 26.03.24 laura start GIT and SKOPEO Services with model
        for (PrepareWrapperModule module : modules) {
            SecHubConfigurationModel sechubConfiguration = context.getSecHubConfiguration();
            if (module.isAbleToPrepare(sechubConfiguration)) {
                module.prepare(sechubConfiguration, remoteDataConfigurationList);
            }
        }
        PrepareResult result = new PrepareResult(PrepareStatus.OK);
        return new AdapterExecutionResult(result.toString());
    }

    private List<SecHubRemoteDataConfiguration> resolveRemoteConfigurationsFromSecHubModel(PrepareWrapperContext context) {
        List<SecHubRemoteDataConfiguration> result = new ArrayList<>();
        if (context.getSecHubConfiguration() == null || context.getRemoteCredentialContainer() == null) {
            throw new IllegalStateException("Context was not initialized correctly. SecHub or credential configuration was null");
        }

        var dataOpt = context.getSecHubConfiguration().getData();
        if (dataOpt.isPresent()) {
            var data = dataOpt.get();
            List<SecHubSourceDataConfiguration> sourceDataList = data.getSources();
            List<SecHubBinaryDataConfiguration> binaryDataList = data.getBinaries();
            for (SecHubSourceDataConfiguration sourceData : sourceDataList) {
                var remoteOpt = sourceData.getRemote();
                remoteOpt.ifPresent(result::add);
            }
            for (SecHubBinaryDataConfiguration binaryData : binaryDataList) {
                var remoteOpt = binaryData.getRemote();
                remoteOpt.ifPresent(result::add);
            }
        }
        return result;
    }
}

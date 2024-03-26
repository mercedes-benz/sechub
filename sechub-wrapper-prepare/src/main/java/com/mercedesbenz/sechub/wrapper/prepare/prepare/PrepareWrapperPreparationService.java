package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperResultStatus;

@Service
public class PrepareWrapperPreparationService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperPreparationService.class);

    @Autowired
    PrepareWrapperEnvironment environment;

    @Autowired
    PrepareWrapperContextFactory factory;

    @Autowired
    PrepareWrapperResultStatus status;

    public void startPreparation() {
        LOG.debug("Start preparation");
        PrepareWrapperContext context = factory.create(environment);
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = getRemoteConfigurationsFromSecHubModel(context);
        if (remoteDataConfigurationList.isEmpty()) {
            LOG.debug("No Remote configuration was found");
            return;
        }

        // TODO: 26.03.24 laura start GIT and SKOPEO Services
    }

    private List<SecHubRemoteDataConfiguration> getRemoteConfigurationsFromSecHubModel(PrepareWrapperContext context) {
        List<SecHubRemoteDataConfiguration> result = new ArrayList<>();
        if (context.getSecHubConfiguration().getData().isPresent()) {
            List<SecHubSourceDataConfiguration> sourceDataList = context.getSecHubConfiguration().getData().get().getSources();
            List<SecHubBinaryDataConfiguration> binaryDataList = context.getSecHubConfiguration().getData().get().getBinaries();

            for (SecHubSourceDataConfiguration sourceData : sourceDataList) {
                if (sourceData.getRemote().isEmpty()) {
                    continue;
                }
                result.add(sourceData.getRemote().get());
            }
            for (SecHubBinaryDataConfiguration binaryData : binaryDataList) {
                if (binaryData.getRemote().isEmpty()) {
                    continue;
                }
                result.add(binaryData.getRemote().get());
            }
        }
        return result;
    }
}

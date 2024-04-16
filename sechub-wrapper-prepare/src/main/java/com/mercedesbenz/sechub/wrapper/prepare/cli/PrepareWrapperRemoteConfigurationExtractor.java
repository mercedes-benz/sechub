package com.mercedesbenz.sechub.wrapper.prepare.cli;

import com.mercedesbenz.sechub.commons.model.SecHubBinaryDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PrepareWrapperRemoteConfigurationExtractor {

    public List<SecHubRemoteDataConfiguration> extractRemoteConfiguration(SecHubConfigurationModel model) {
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        if (model == null) {
            throw new IllegalStateException("Context was not initialized correctly. SecHub configuration was null");
        }

        var dataOpt = model.getData();
        if (dataOpt.isPresent()) {
            var data = dataOpt.get();
            List<SecHubSourceDataConfiguration> sourceDataList = data.getSources();
            List<SecHubBinaryDataConfiguration> binaryDataList = data.getBinaries();
            for (SecHubSourceDataConfiguration sourceData : sourceDataList) {
                var remoteOpt = sourceData.getRemote();
                remoteOpt.ifPresent(remoteDataConfigurationList::add);
            }
            for (SecHubBinaryDataConfiguration binaryData : binaryDataList) {
                var remoteOpt = binaryData.getRemote();
                remoteOpt.ifPresent(remoteDataConfigurationList::add);
            }
        }
        return remoteDataConfigurationList;
    }

}

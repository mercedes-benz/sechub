package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubBinaryDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;

@Component
public class PrepareWrapperRemoteConfigurationExtractor {

    public SecHubRemoteDataConfiguration extract(SecHubConfigurationModel model) {
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
        if (remoteDataConfigurationList.isEmpty()) {
            return null;
        } else if (remoteDataConfigurationList.size() > 1) {
            throw new IllegalStateException("Only one remote data configuration is allowed.");
        } else {
            return remoteDataConfigurationList.get(0);
        }
    }

}

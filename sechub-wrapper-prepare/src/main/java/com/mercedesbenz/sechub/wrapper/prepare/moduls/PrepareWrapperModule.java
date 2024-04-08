// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;

@Service
public interface PrepareWrapperModule {

    public boolean isAbleToPrepare(SecHubConfigurationModel model);

    public void prepare(SecHubConfigurationModel model, List<SecHubRemoteDataConfiguration> remoteDataConfigurationList);
}

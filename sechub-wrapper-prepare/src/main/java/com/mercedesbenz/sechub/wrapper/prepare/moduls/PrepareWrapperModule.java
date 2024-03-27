// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public interface PrepareWrapperModule {

    public boolean isAbleToPrepare(SecHubConfigurationModel model);

    public void prepare(SecHubConfigurationModel model);
}

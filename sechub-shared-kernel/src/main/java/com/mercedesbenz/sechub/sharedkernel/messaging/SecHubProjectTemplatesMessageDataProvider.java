// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubProjectTemplates;

public class SecHubProjectTemplatesMessageDataProvider implements MessageDataProvider<SecHubProjectTemplates> {

    @Override
    public SecHubProjectTemplates get(String json) {
        if (json == null) {
            return null;
        }
        return SecHubProjectTemplates.fromString(json);
    }

    @Override
    public String getString(SecHubProjectTemplates data) {
        if (data == null) {
            return null;
        }
        return data.toJSON();
    }

}

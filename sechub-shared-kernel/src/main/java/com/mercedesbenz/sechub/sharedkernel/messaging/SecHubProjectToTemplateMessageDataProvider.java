// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectToTemplate;

public class SecHubProjectToTemplateMessageDataProvider implements MessageDataProvider<SecHubProjectToTemplate> {

    @Override
    public SecHubProjectToTemplate get(String json) {
        if (json == null) {
            return null;
        }
        return SecHubProjectToTemplate.fromString(json);
    }

    @Override
    public String getString(SecHubProjectToTemplate data) {
        if (data == null) {
            return null;
        }
        return data.toJSON();
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectTemplateData;

public class SecHubProjectTemplatesMessageDataProvider implements MessageDataProvider<SecHubProjectTemplateData> {

    @Override
    public SecHubProjectTemplateData get(String json) {
        if (json == null) {
            return null;
        }
        return SecHubProjectTemplateData.fromString(json);
    }

    @Override
    public String getString(SecHubProjectTemplateData data) {
        if (data == null) {
            return null;
        }
        return data.toJSON();
    }

}

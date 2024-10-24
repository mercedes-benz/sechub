// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class SecHubProjectTemplates implements JSONable<SecHubProjectTemplates> {

    private static final SecHubProjectTemplates CONVERTER = new SecHubProjectTemplates();

    private String projectId;

    private List<String> templateIds = new ArrayList<>();

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public List<String> getTemplateIds() {
        return templateIds;
    }

    public static SecHubProjectTemplates fromString(String json) {
        return CONVERTER.fromJSON(json);
    }

    @Override
    public Class<SecHubProjectTemplates> getJSONTargetClass() {
        return SecHubProjectTemplates.class;
    }

}

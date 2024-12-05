// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.template;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class SecHubProjectTemplateData implements JSONable<SecHubProjectTemplateData> {

    private static final SecHubProjectTemplateData CONVERTER = new SecHubProjectTemplateData();

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

    public static SecHubProjectTemplateData fromString(String json) {
        return CONVERTER.fromJSON(json);
    }

    @Override
    public Class<SecHubProjectTemplateData> getJSONTargetClass() {
        return SecHubProjectTemplateData.class;
    }

}

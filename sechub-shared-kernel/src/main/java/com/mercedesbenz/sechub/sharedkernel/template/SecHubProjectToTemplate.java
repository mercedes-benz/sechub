// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.template;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class SecHubProjectToTemplate implements JSONable<SecHubProjectToTemplate> {

    private static final SecHubProjectToTemplate CONVERTER = new SecHubProjectToTemplate();

    private String projectId;

    private String templateId;

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public static SecHubProjectToTemplate fromString(String json) {
        return CONVERTER.fromJSON(json);
    }

    @Override
    public Class<SecHubProjectToTemplate> getJSONTargetClass() {
        return SecHubProjectToTemplate.class;
    }

}

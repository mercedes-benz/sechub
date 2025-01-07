// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.domain.administration.user.User;

public class ProjectDetailInformation {

    public static final String PROPERTY_USERS = "users";
    public static final String PROPERTY_PROJECT_ID = "projectId";
    public static final String PROPERTY_WHITELIST = "whiteList";
    public static final String PROPERTY_METADATA = "metaData";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_ACCESSLEVEL = "accessLevel";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_TEMPLATE_IDS = "templateIds";

    private String projectId;

    private List<String> users = new ArrayList<>();
    private List<String> whitelist = new ArrayList<>();
    private List<String> templateIds = new ArrayList<>();
    private Map<String, String> metaData = new HashMap<>();
    private String owner;
    private String description;
    private String accessLevel;

    ProjectDetailInformation() {
        /* for JSON */
    }

    public ProjectDetailInformation(Project project) {
        this.projectId = project.getId();

        for (User user : project.getUsers()) {
            this.users.add(user.getName());
        }

        project.getWhiteList().forEach(uri -> this.whitelist.add(uri.toASCIIString()));

        project.getMetaData().forEach(entry -> this.metaData.put(entry.key, entry.value));

        project.getTemplateIds().forEach(templateid -> this.templateIds.add(templateid));

        this.owner = project.getOwner().getName();

        this.description = project.getDescription();

        this.accessLevel = project.getAccessLevel().getId();
    }

    public String getOwner() {
        return owner;
    }

    public List<String> getWhiteList() {
        return whitelist;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public List<String> getUsers() {
        return users;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getDescription() {
        return description;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public List<String> getTemplateIds() {
        return templateIds;
    }
}

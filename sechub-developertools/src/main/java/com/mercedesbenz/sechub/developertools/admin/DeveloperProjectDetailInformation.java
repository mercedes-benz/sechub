// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class DeveloperProjectDetailInformation implements JSONable<DeveloperProjectDetailInformation> {

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
    private Map<String, String> metaData = new HashMap<>();
    private List<String> templateIds = new ArrayList<>();;
    private String owner;
    private String description;
    private String accessLevel;

    public DeveloperProjectDetailInformation() {
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

    @Override
    public Class<DeveloperProjectDetailInformation> getJSONTargetClass() {
        return DeveloperProjectDetailInformation.class;
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.databind.JsonNode;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxContext;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxEngineConfiguration;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxSastScanSettings;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxSessionData;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport.Access;

public class CheckmarxProjectSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxProjectSupport.class);

    public void ensureProjectExists(CheckmarxContext context) throws AdapterException {
        CheckmarxAdapterConfig config = context.getConfig();
        String projectName = config.getProjectId();
        String teamId = config.getTeamIdForNewProjects();

        Map<String, String> map = new LinkedHashMap<>();
        map.put("projectName", projectName);
        map.put("teamId", teamId);
        String url = context.getAPIURL("projects", map);
        RestOperations restTemplate = context.getRestOperations();

        // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814285654/Swagger+Examples+v8.8.0+-+v2
        // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/564330665/Get+All+Project+Details+-+GET+projects+v8.8.0+and+up
        // example:
        // CxRestAPI/projects?projectName=myProject&teamId=00000000-1111-1111-b111-989c9070eb11
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            context.setSessionData(extractFirstProjectFromJsonWithProjectArray(context.json(), response.getBody()));
            context.setNewProject(false);
            return;
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() != 404) {
                /* only 404 - not found is accepted */
                throw context.asAdapterException(
                        CheckmarxAdapter.CHECKMARX_MESSAGE_PREFIX + "HTTP status=" + e.getStatusCode() + " (expected was only 404 for non existing project)",
                        e);
            }
        }
        /* 404 error - okay, lets create */
        context.setSessionData(createProject(context));
        context.setNewProject(true);
    }

    private CheckmarxSessionData createProject(CheckmarxContext context) throws AdapterException {
        CheckmarxAdapterConfig config = context.getConfig();
        String projectName = config.getProjectId();
        String teamId = config.getTeamIdForNewProjects();

        Map<String, String> json = new TreeMap<>();
        json.put("name", projectName);
        json.put("owningTeam", teamId);
        json.put("isPublic", "false");

        String url = context.getAPIURL("projects");
        String jsonAsString = context.json().toJSON(json);
        RestOperations restTemplate = context.getRestOperations();

        // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/222265747/Create+Project+with+Default+Configuration+-+POST+projects
        // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/814285654/Swagger+Examples+v8.8.0+-+v2
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "application/json;v=2.0");

        HttpEntity<String> request = new HttpEntity<>(jsonAsString, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        CheckmarxSessionData sessionData = extractProjectFromJsonWithProjectCreationData(projectName, context.json(), response.getBody());

        updatePresetIdAndEngineConfigurationIfNecessary(context, sessionData);

        return sessionData;
    }

    /**
     * Update preset id of checkmarx project when in adapter config set
     *
     * @param config
     * @param checkmarxScanSettings
     * @param updateContext
     */
    protected void updatePresetIdWhenSetInAdapterConfig(CheckmarxAdapterConfig config, CheckmarxSastScanSettings checkmarxScanSettings,
            InternalUpdateContext updateContext) {

        boolean updatePresetId = false;
        String projectId = config.getProjectId();

        /* check if the preset id needs to be updated */
        Long presetId = config.getPresetIdForNewProjectsOrNull();

        if (presetId == null) {
            LOG.debug("No presetId defined, so keep default preset.");
            presetId = checkmarxScanSettings.getPresetId();
        } else {
            LOG.debug("Wanted preset id {} for project {}", presetId, projectId);
            updatePresetId = true;
        }

        updateContext.setUpdatePresetId(updatePresetId);
        updateContext.setPresetId(presetId);
    }

    protected void updateEngineCondfigurationIdWhenSecHubAndCheckmarxDiffer(CheckmarxAdapterConfig config,
            List<CheckmarxEngineConfiguration> engineConfigurations, CheckmarxSastScanSettings checkmarxScanSettings, InternalUpdateContext updateContext) {

        boolean updateEngineConfiguration = false;
        String sechubEngineConfigurationName = config.getEngineConfigurationName();
        String projectId = config.getProjectId();
        long engineConfigurationId = checkmarxScanSettings.getEngineConfigurationId();

        LOG.debug("SecHub engine configuration name {}", sechubEngineConfigurationName);

        CheckmarxEngineConfiguration sechubEngineConfiguration = findEngineConfigurationByName(sechubEngineConfigurationName, engineConfigurations);

        if (sechubEngineConfiguration == null) {
            LOG.warn("[ALERT] ILLEGAL_STATE CHECKMARX_ADAPTER no engine configuration available!");
        } else {
            LOG.debug("Found SecHub engine configuration {}", sechubEngineConfiguration.toString());

            if (sechubEngineConfiguration.getId() != engineConfigurationId) {
                LOG.debug("SecHub engine configuration id {} and Checkmarx engine configuration id {} are different.", sechubEngineConfiguration.getId(),
                        engineConfigurationId);
                LOG.debug("Wanted engine configuration id {} for project {}", sechubEngineConfiguration.getId(), projectId);
                engineConfigurationId = sechubEngineConfiguration.getId();
                updateEngineConfiguration = true;
            } else {
                LOG.debug("Engine configuration id is already set to {} for project", sechubEngineConfigurationName, projectId);
            }
        }

        updateContext.setUpdateEngineConfiguration(updateEngineConfiguration);
        updateContext.setEngineConfigurationId(engineConfigurationId);
    }

    class InternalUpdateContext {
        private boolean updatePresetId;
        private boolean updateEngineConfiguration;
        private long presetId;
        private long engineConfigurationId;

        public long getEngineConfigurationId() {
            return engineConfigurationId;
        }

        public void setEngineConfigurationId(long engineConfigurationId) {
            this.engineConfigurationId = engineConfigurationId;
        }

        public long getPresetId() {
            return presetId;
        }

        public void setPresetId(long presetId) {
            this.presetId = presetId;
        }

        public boolean isUpdateOfPresetIdNecessary() {
            return updatePresetId;
        }

        public void setUpdatePresetId(boolean updatePresetId) {
            this.updatePresetId = updatePresetId;
        }

        public boolean isUpdateOfEngineConfigurationNecessary() {
            return updateEngineConfiguration;
        }

        public void setUpdateEngineConfiguration(boolean updateEngineConfiguration) {
            this.updateEngineConfiguration = updateEngineConfiguration;
        }

        public boolean isUpdateNecessary() {
            return (isUpdateOfEngineConfigurationNecessary() || isUpdateOfPresetIdNecessary());
        }
    }

    protected boolean updatePresetIdAndEngineConfigurationIfNecessary(CheckmarxContext context, CheckmarxSessionData sessionData) throws AdapterException {
        boolean updated = false;
        InternalUpdateContext updateContext = new InternalUpdateContext();
        CheckmarxAdapterConfig config = context.getConfig();

        CheckmarxSastScanSettings checkmarxSastScanSettings = fetchCurrentSastScanSettings(context, sessionData);
        List<CheckmarxEngineConfiguration> engineConfigurations = fetchEngineConfigurations(context, sessionData);

        updatePresetIdWhenSetInAdapterConfig(config, checkmarxSastScanSettings, updateContext);
        updateEngineCondfigurationIdWhenSecHubAndCheckmarxDiffer(config, engineConfigurations, checkmarxSastScanSettings, updateContext);

        /* check if the engine configuration needs to be updated */
        if (updateContext.isUpdateNecessary()) {
            LOG.debug("Update scan settings.");

            updateSastScanSettings(context, updateContext.getPresetId(), updateContext.getEngineConfigurationId(), checkmarxSastScanSettings);

            if (updateContext.isUpdateOfPresetIdNecessary()) {
                LOG.debug("Updated preset id {}", updateContext.getPresetId());
            }

            if (updateContext.isUpdateOfEngineConfigurationNecessary()) {
                LOG.debug("Updated engine configuration id {}", updateContext.getEngineConfigurationId());
            }

            updated = true;
        } else {
            LOG.debug("No update necessary.");
        }

        return updated;
    }

    protected void updateSastScanSettings(CheckmarxContext context, Long presetId, Long engineConfigurationId, CheckmarxSastScanSettings currentSettings)
            throws AdapterException {
        MultiValueMap<String, String> headers3 = new LinkedMultiValueMap<>();
        headers3.set("Content-Type", "application/json;v=1.1");
        RestOperations restTemplate3 = context.getRestOperations();

        /* write */
        Map<String, Long> updateJSON = new TreeMap<>();
        updateJSON.put("projectId", currentSettings.getProjectId());
        updateJSON.put("presetId", presetId);
        updateJSON.put("engineConfigurationId", engineConfigurationId);

        String updateScanSettingsURL = context.getAPIURL("sast/scanSettings");

        String updateJSONAsString = context.json().toJSON(updateJSON);
        HttpEntity<String> request2 = new HttpEntity<>(updateJSONAsString, headers3);
        restTemplate3.put(updateScanSettingsURL, request2);

        LOG.debug("Did change preset id from {} to {} for project {}", currentSettings.getPresetId(), presetId, context.getConfig().getProjectId());
        LOG.debug("Did change engine configuration id from {} to {} for project {}", currentSettings.getPresetId(), presetId,
                context.getConfig().getProjectId());
    }

    private CheckmarxSastScanSettings fetchCurrentSastScanSettings(CheckmarxContext context, CheckmarxSessionData sessionData) throws AdapterException {
        CheckmarxSastScanSettings settings;
        // project scan settings cannot be defined at creation time.
        // see
        // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/1140555950/CxSAST+REST+API+-+Swagger+Examples+v8.9.0+-+v1.1
        // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/334299447/Get+Scan+Settings+by+Project+Id+-+GET+sast+scanSettings+projectId+v8.7.0+and+up
        MultiValueMap<String, String> headers2 = new LinkedMultiValueMap<>();
        headers2.set("Content-Type", "application/json;v=1.1");
        RestOperations restTemplate2 = context.getRestOperations();

        /* read current setup */
        String fetchScanSettingsURL = context.getAPIURL("sast/scanSettings/" + sessionData.getProjectId());
        ResponseEntity<String> scanSettingsResponse = restTemplate2.getForEntity(fetchScanSettingsURL, String.class);
        settings = extractSastScanSettingsFromGet(scanSettingsResponse.getBody(), context.json());
        return settings;
    }

    CheckmarxSessionData extractFirstProjectFromJsonWithProjectArray(JSONAdapterSupport support, String json) throws AdapterException {
        CheckmarxSessionData data = new CheckmarxSessionData();
        Access rootNode = support.fetchRootNode(json);
        Access first = support.fetchArray(0, rootNode.asArray());
        data.setProjectId(first.fetch("id").asLong());
        data.setProjectName(first.fetch("name").asText());
        return data;
    }

    CheckmarxSessionData extractProjectFromJsonWithProjectCreationData(String projectName, JSONAdapterSupport support, String json) throws AdapterException {
        CheckmarxSessionData data = new CheckmarxSessionData();
        Access rootNode = support.fetchRootNode(json);
        data.setProjectId(rootNode.fetch("id").asLong());
        data.setProjectName(projectName);
        return data;
    }

    // https://swagger-open-api.herokuapp.com/v1/swagger#/SAST/ScanSettingsV1_1__GetByprojectId
    CheckmarxSastScanSettings extractSastScanSettingsFromGet(String json, JSONAdapterSupport support) throws AdapterException {
        Access rootNode = support.fetchRootNode(json);

        CheckmarxSastScanSettings settings = new CheckmarxSastScanSettings();
        settings.setProjectId(rootNode.fetch("project").fetch("id").asLong());
        settings.setPresetId(rootNode.fetch("preset").fetch("id").asLong());
        settings.setEngineConfigurationId(rootNode.fetch("engineConfiguration").fetch("id").asLong());
        return settings;
    }

    private List<CheckmarxEngineConfiguration> fetchEngineConfigurations(CheckmarxContext context, CheckmarxSessionData sessionData) throws AdapterException {
        List<CheckmarxEngineConfiguration> engineConfigurations;

        // https://checkmarx.atlassian.net/wiki/spaces/KC/pages/223543515/Get+All+Engine+Configurations+-+GET+sast+engineConfigurations+v8.6.0+and+up
        MultiValueMap<String, String> headers2 = new LinkedMultiValueMap<>();
        headers2.set("Content-Type", "application/json;v=1.1");
        RestOperations restTemplate2 = context.getRestOperations();

        /* read engine configurations */
        String fetchEngineConfigurationsURL = context.getAPIURL("sast/engineConfigurations");
        ResponseEntity<String> engineConfigurationsResponse = restTemplate2.getForEntity(fetchEngineConfigurationsURL, String.class);
        engineConfigurations = extractEngineConfigurationsFromGet(engineConfigurationsResponse.getBody(), context.json());
        return engineConfigurations;
    }

    protected List<CheckmarxEngineConfiguration> extractEngineConfigurationsFromGet(String json, JSONAdapterSupport support) throws AdapterException {
        Access rootNode = support.fetchRootNode(json);

        List<CheckmarxEngineConfiguration> engineConfigurations = new LinkedList<>();
        for (JsonNode node : rootNode.asArray()) {
            CheckmarxEngineConfiguration engineConfiguration = new CheckmarxEngineConfiguration();
            engineConfiguration.setId(node.get("id").asLong());
            engineConfiguration.setName(node.get("name").asText());

            engineConfigurations.add(engineConfiguration);
        }

        return engineConfigurations;
    }

    protected CheckmarxEngineConfiguration findEngineConfigurationByName(String name, List<CheckmarxEngineConfiguration> engineConfigurations) {
        return engineConfigurations.stream().filter(engineConfiguration -> name.equals(engineConfiguration.getName())).findFirst().orElse(null);
    }
}

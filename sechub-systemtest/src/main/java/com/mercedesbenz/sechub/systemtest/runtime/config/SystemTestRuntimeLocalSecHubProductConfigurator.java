// SPDX-License-Identifier: MIT

package com.mercedesbenz.sechub.systemtest.runtime.config;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.ExecutionProfileCreate;
import com.mercedesbenz.sechub.api.ExecutorConfiguration;
import com.mercedesbenz.sechub.api.ExecutorConfigurationInfo;
import com.mercedesbenz.sechub.api.ExecutorConfigurationSetup;
import com.mercedesbenz.sechub.api.ExecutorConfigurationSetupCredentials;
import com.mercedesbenz.sechub.api.Project;
import com.mercedesbenz.sechub.api.ProjectWhiteList;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.systemtest.config.CredentialsDefinition;
import com.mercedesbenz.sechub.systemtest.config.ProjectDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;

public class SystemTestRuntimeLocalSecHubProductConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeLocalSecHubProductConfigurator.class);

    public void configure(SystemTestRuntimeContext context) throws SecHubClientException {
        if (!context.isLocalRun()) {
            return;
        }
        if (!context.isLocalSecHubConfigured()) {
            return;
        }

        /* remove old stuff of former run - we insist on a clean setup on local runs! */
        deleteExistingProjects(context);
        deleteExistingProfiles(context);

        createProjects(context);
        assignAdminAsUserToProjects(context);

        Map<String, List<UUID>> profileIdsToExecutorUUIDs = createExecutorConfigurationsAndMapToProfileIds(context);
        createProfilesAndAssignToProjects(context);

        addExecutorConfigurationsToProfiles(context, profileIdsToExecutorUUIDs);
    }

    private void deleteExistingProjects(SystemTestRuntimeContext context) throws SecHubClientException {
        SecHubClient client = context.getLocalAdminSecHubClient();

        for (String projectId : context.createSetForLocalSecHubProjectIdDefinitions()) {
            if (context.isDryRun()) {
                LOG.info("Dry run: delete existing project '{}' is skipped", projectId);
                continue;
            }
            if (client.isProjectExisting(projectId)) {
                LOG.warn("Project '{}' did already exist - will delete old project!", projectId);
                client.deleteProject(projectId);
            }
        }
    }

    private void deleteExistingProfiles(SystemTestRuntimeContext context) throws SecHubClientException {
        Set<String> profileIds = context.createSetForLocalSecHubProfileIdsInExecutors();

        SecHubClient client = context.getLocalAdminSecHubClient();

        for (String profileId : profileIds) {
            if (context.isDryRun()) {
                LOG.info("Dry run: delete existing profile '{}' is skipped", profileId);
                continue;
            }
            if (client.isExecutionProfileExisting(profileId)) {
                client.deleteExecutionProfile(profileId);
            }

        }
    }

    private void createProjects(SystemTestRuntimeContext context) throws SecHubClientException {
        SecHubConfigurationDefinition config = context.getLocalSecHubConfigurationOrFail();

        if (config.getProjects().isEmpty()) {
            LOG.warn("No project defined - skip project configuration. Should only happen when only PDS is tested without SecHub.");
            return;
        }

        SecHubClient client = context.getLocalAdminSecHubClient();

        for (ProjectDefinition projectDefinition : config.getProjects().get()) {
            String projectName = projectDefinition.getName();

            if (context.isDryRun()) {
                LOG.info("Dry run: create project '{}' is skipped", projectName);
                continue;
            }
            Project project = new Project();
            project.setApiVersion("1.0");
            project.setDescription("System test project " + projectName);
            project.setName(projectName);
            project.setOwner(client.getUserId());// we use the administrator as owner of the project

            ProjectWhiteList whiteList = project.getWhiteList();
            for (String whiteListEntry : projectDefinition.getWhitelistedURIs()) {
                whiteList.getUris().add(whiteListEntry);
            }
            client.createProject(project);

        }
    }

    private Map<String, List<UUID>> createExecutorConfigurationsAndMapToProfileIds(SystemTestRuntimeContext context) throws SecHubClientException {
        Map<String, List<UUID>> map = new LinkedHashMap<>();
        if (context.isDryRun()) {
            LOG.info("Dry run: Skip executor configuration creation");
        } else {

            for (SecHubExecutorConfigDefinition executorConfigDefinition : context.getLocalSecHubExecutorConfigurationsOrFail()) {

                createAndMapExecutorConfiguration(executorConfigDefinition, map, context);
            }
        }

        return map;
    }

    private void createAndMapExecutorConfiguration(SecHubExecutorConfigDefinition executorConfigDefinition, Map<String, List<UUID>> map,
            SystemTestRuntimeContext context) throws SecHubClientException {

        SecHubClient client = context.getLocalAdminSecHubClient();

        String name = executorConfigDefinition.getName();

        List<ExecutorConfigurationInfo> executorConfigurations = client.fetchAllExecutorConfigurationInfo();
        Set<UUID> oldEntries = new HashSet<>();
        for (ExecutorConfigurationInfo info : executorConfigurations) {
            if (name.equals(info.getName())) {
                oldEntries.add(info.getUuid());
            }
        }
        for (UUID oldEntry : oldEntries) {
            LOG.info("Delete existing executor configuration: {} - {}", name, oldEntry);
            client.deleteExecutorConfiguration(oldEntry);
        }

        String pdsProductId = executorConfigDefinition.getPdsProductId();
        ScanType scanType = context.getScanTypeForPdsProduct(pdsProductId);
        String secHubProductIdentifier = resolveSecHubProductIdentifierForPdsWithSCanType(scanType);

        /* create and configure new executor configuration */
        ExecutorConfiguration config = new ExecutorConfiguration();
        config.setEnabled(true);
        config.setName(name);
        config.setExecutorVersion(executorConfigDefinition.getVersion());
        config.setProductIdentifier(secHubProductIdentifier);

        ExecutorConfigurationSetup setup = config.getSetup();
        setup.setBaseURL(executorConfigDefinition.getBaseURL());

        handleCredentials(executorConfigDefinition, setup, context);
        handleParametersForNewExecutorConfiguration(executorConfigDefinition, setup, pdsProductId, scanType);

        /* store executor configuration */
        UUID uuid = client.createExecutorConfiguration(config);

        /* map profiles with created executor configuration */
        Set<String> profileIds = executorConfigDefinition.getProfiles();
        for (String profileId : profileIds) {
            List<UUID> list = map.computeIfAbsent(profileId, (key) -> new ArrayList<>());
            list.add(uuid);
        }
    }

    private void handleCredentials(SecHubExecutorConfigDefinition executorConfigDefinition, ExecutorConfigurationSetup setup,
            SystemTestRuntimeContext context) {
        ExecutorConfigurationSetupCredentials executorSetupCredentials = new ExecutorConfigurationSetupCredentials();

        Optional<CredentialsDefinition> executorConfigOptCredentials = executorConfigDefinition.getCredentials();

        if (executorConfigOptCredentials.isPresent()) {
            /*
             * we must provide secret environment variables here - to avoid them inside
             * logs...
             */
            CredentialsDefinition executorCredentialsDefinition = executorConfigOptCredentials.get();

            String userId = context.getTemplateEngine().replaceSecretEnvironmentVariablesWithValues(executorCredentialsDefinition.getUserId(),
                    context.getEnvironmentProvider());
            executorSetupCredentials.setUser(userId);

            String apiToken = context.getTemplateEngine().replaceSecretEnvironmentVariablesWithValues(executorCredentialsDefinition.getApiToken(),
                    context.getEnvironmentProvider());
            executorSetupCredentials.setPassword(apiToken);

        }
        setup.setCredentials(executorSetupCredentials);
    }

    private void handleParametersForNewExecutorConfiguration(SecHubExecutorConfigDefinition executorConfigDefinition, ExecutorConfigurationSetup setup,
            String pdsProductId, ScanType scanType) {
        Map<String, String> parameters = executorConfigDefinition.getParameters();
        /* define internal standard settings which can be overriden by developers */
        setup.addParameter(PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED, "true");
        setup.addParameter(PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE, "false");

        /* developer settings: */
        for (String paramKey : parameters.keySet()) {
            String paramValue = parameters.get(paramKey);

            setup.addParameter(paramKey, paramValue);

        }
        /* fix parts, which cannot be overriden */
        setup.addParameter(PARAM_KEY_PDS_CONFIG_PRODUCTIDENTIFIER, pdsProductId);
        setup.addParameter(PARAM_KEY_PDS_SCAN_TARGET_TYPE, scanType.name());
    }

    private String resolveSecHubProductIdentifierForPdsWithSCanType(ScanType scanType) {
        return "PDS_" + (scanType.name().replace("_", ""));
    }

    private void createProfilesAndAssignToProjects(SystemTestRuntimeContext context) throws SecHubClientException {
        List<SecHubExecutorConfigDefinition> executorDefinitions = context.getLocalSecHubExecutorConfigurationsOrFail();
        for (SecHubExecutorConfigDefinition executorDefinition : executorDefinitions) {
            for (String profileId : executorDefinition.getProfiles()) {
                if (context.isDryRun()) {
                    LOG.info("Dry run: fetch/create profile '{}' is skipped", profileId);
                    continue;
                }
                createProfileAndAssignProjects(profileId, context);
            }
        }
    }

    private void createProfileAndAssignProjects(String profileId, SystemTestRuntimeContext context) throws SecHubClientException {

        SecHubClient client = context.getLocalAdminSecHubClient();
        SecHubConfigurationDefinition config = context.getLocalSecHubConfigurationOrFail();

        ExecutionProfileCreate profile = new ExecutionProfileCreate();
        profile.setEnabled(true);

        List<String> projectIdsForThisProfile = new ArrayList<>();
        for (ProjectDefinition projectDefinition : config.getProjects().get()) {
            if (projectDefinition.getProfiles().contains(profileId)) {
                projectIdsForThisProfile.add(projectDefinition.getName());
            }
        }
        profile.setProjectIds(projectIdsForThisProfile);
        profile.setDescription("Generated by system test framework");

        client.createExecutionProfile(profileId, profile);
    }

    private void addExecutorConfigurationsToProfiles(SystemTestRuntimeContext context, Map<String, List<UUID>> profileIdsToExecutorUUIDs)
            throws SecHubClientException {
        /* assign executor configurations to profiles */
        SecHubClient client = context.getLocalAdminSecHubClient();
        for (String profileId : profileIdsToExecutorUUIDs.keySet()) {
            for (UUID executorConfigurationUUID : profileIdsToExecutorUUIDs.get(profileId)) {
                if (context.isDryRun()) {
                    LOG.info("Dry run: add executor config '{}' to profile '{}' is skipped", executorConfigurationUUID, profileId);
                    continue;
                }
                client.addExecutorConfigurationToProfile(executorConfigurationUUID, profileId);
            }
        }
    }

    private void assignAdminAsUserToProjects(SystemTestRuntimeContext context) throws SecHubClientException {
        SecHubConfigurationDefinition config = context.getLocalSecHubConfigurationOrFail();
        if (config.getProjects().isEmpty()) {
            return;
        }

        SecHubClient client = context.getLocalAdminSecHubClient();
        String userId = client.getUserId();
        for (ProjectDefinition projectDefinition : config.getProjects().get()) {
            String projectid = projectDefinition.getName();
            if (context.isDryRun()) {
                LOG.info("Dry run: assign user '{}' to project '{}' is skipped", userId, projectid);
                continue;
            }
            client.assignUserToProject(userId, projectid);
        }
    }

}

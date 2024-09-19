// SPDX-License-Identifier: MIT

package com.mercedesbenz.sechub.systemtest.runtime.config;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;

import java.net.URI;
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

import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.*;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.systemtest.config.CredentialsDefinition;
import com.mercedesbenz.sechub.systemtest.config.ProjectDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;

public class SystemTestRuntimeLocalSecHubProductConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeLocalSecHubProductConfigurator.class);

    public void configure(SystemTestRuntimeContext context) throws ApiException {
        if (!context.isLocalRun()) {
            return;
        }
        if (!context.isLocalSecHubConfigured()) {
            return;
        }

        /* Make sure that existing projects are removed */
        deleteExistingProjects(context);

        /* Make sure that existing profiles are removed */
        try {
            deleteExistingProfiles(context);
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                LOG.error("Failed to delete existing profiles with reason {}", e.getMessage());
                throw e;
            }
        }

        createProjects(context);
        assignAdminAsUserToProjects(context);

        Map<String, List<UUID>> profileIdsToExecutorUUIDs = createExecutorConfigurationsAndMapToProfileIds(context);
        createProfilesAndAssignToProjects(context);

        addExecutorConfigurationsToProfiles(context, profileIdsToExecutorUUIDs);
    }

    private void deleteExistingProjects(SystemTestRuntimeContext context) throws ApiException {
        SecHubClient client = context.getLocalAdminSecHubClient();

        for (String projectId : context.createSetForLocalSecHubProjectIdDefinitions()) {
            if (context.isDryRun()) {
                LOG.info("Dry run: delete existing project '{}' is skipped", projectId);
                continue;
            }
            List<String> projectIds = client.withProjectAdministrationApi().adminListAllProjects();
            if (projectIds.contains(projectId)) {
                LOG.warn("Project '{}' did already exist - will delete old project!", projectId);
                client.withProjectAdministrationApi().adminDeleteProject(projectId);
            }
        }
    }

    private void deleteExistingProfiles(SystemTestRuntimeContext context) throws ApiException {
        Set<String> profileIds = context.createSetForLocalSecHubProfileIdsInExecutors();

        SecHubClient client = context.getLocalAdminSecHubClient();

        for (String profileId : profileIds) {
            if (context.isDryRun()) {
                LOG.info("Dry run: delete existing profile '{}' is skipped", profileId);
                continue;
            }
            try {
                client.withConfigurationApi().adminDeleteExecutionProfile(profileId);
            } catch (ApiException e) {
                if (e.getCode() != 404) {
                    throw e;
                }
            }
        }
    }

    private void createProjects(SystemTestRuntimeContext context) throws ApiException {
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
            ProjectInput project = new ProjectInput();
            project.setApiVersion("1.0");
            project.setDescription("System test project " + projectName);
            project.setName(projectName);
            project.setOwner(client.getUserId());// we use the administrator as owner of the project

            ProjectWhitelist whiteList = project.getWhiteList();
            if (whiteList != null) {
                for (String whiteListEntry : projectDefinition.getWhitelistedURIs()) {
                    if (whiteList.getUris() != null) {
                        whiteList.getUris().add(URI.create(whiteListEntry));
                    }
                }
            }

            client.withProjectAdministrationApi().adminCreateProject(project);

        }
    }

    private Map<String, List<UUID>> createExecutorConfigurationsAndMapToProfileIds(SystemTestRuntimeContext context) throws ApiException {
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
            SystemTestRuntimeContext context) throws ApiException {

        SecHubClient client = context.getLocalAdminSecHubClient();

        String name = executorConfigDefinition.getName();

        ProductExecutorConfigList productExecutorConfigList = client.withConfigurationApi().adminFetchExecutorConfigurationList();
        Set<UUID> oldEntries = new HashSet<>();
        if (productExecutorConfigList.getExecutorConfigurations() != null) {
            for (ProductExecutorConfigListEntry entry : productExecutorConfigList.getExecutorConfigurations()) {
                if (name.equals(entry.getName())) {
                    oldEntries.add(entry.getUuid());
                }
            }
        }
        for (UUID uuid : oldEntries) {
            LOG.info("Delete existing executor configuration: {} - {}", name, uuid);
            client.withConfigurationApi().adminDeleteExecutorConfiguration(uuid);
        }

        String pdsProductId = executorConfigDefinition.getPdsProductId();
        ScanType scanType = context.getScanTypeForPdsProduct(pdsProductId);
        ProductIdentifier productIdentifier = resolveSecHubProductIdentifierForPdsWithSCanType(scanType);

        /* create and configure new executor configuration */
        ProductExecutorConfig config = new ProductExecutorConfig();
        config.setEnabled(true);
        config.setName(name);
        config.setExecutorVersion(executorConfigDefinition.getVersion());
        config.setProductIdentifier(productIdentifier);

        ProductExecutorConfigSetup setup = new ProductExecutorConfigSetup();
        setup.setBaseURL(executorConfigDefinition.getBaseURL());
        config.setSetup(setup);

        handleCredentials(executorConfigDefinition, setup, context);
        handleParametersForNewExecutorConfiguration(executorConfigDefinition, setup, pdsProductId, scanType);

        /* store executor configuration */
        String uuidString = client.withConfigurationApi().adminCreateExecutorConfiguration(config);
        UUID uuid = UUID.fromString(uuidString);

        /* map profiles with created executor configuration */
        Set<String> profileIds = executorConfigDefinition.getProfiles();
        for (String profileId : profileIds) {
            List<UUID> list = map.computeIfAbsent(profileId, (key) -> new ArrayList<>());
            list.add(uuid);
        }
    }

    private void handleCredentials(SecHubExecutorConfigDefinition executorConfigDefinition, ProductExecutorConfigSetup setup,
            SystemTestRuntimeContext context) {
        ProductExecutorConfigSetupCredentials executorSetupCredentials = new ProductExecutorConfigSetupCredentials();

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

    private void handleParametersForNewExecutorConfiguration(SecHubExecutorConfigDefinition executorConfigDefinition, ProductExecutorConfigSetup setup,
            String pdsProductId, ScanType scanType) {
        Map<String, String> parameters = executorConfigDefinition.getParameters();
        /* define internal standard settings which can be overriden by developers */
        setup.addJobParametersItem(new ProductExecutorConfigSetupJobParameter().key(PARAM_KEY_PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED).value("true"));
        setup.addJobParametersItem(new ProductExecutorConfigSetupJobParameter().key(PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE).value("false"));

        /* developer settings: */
        for (String paramKey : parameters.keySet()) {
            String paramValue = parameters.get(paramKey);
            ProductExecutorConfigSetupJobParameter jobParameter = new ProductExecutorConfigSetupJobParameter().key(paramKey).value(paramValue);
            setup.addJobParametersItem(jobParameter);

        }
        /* fix parts, which cannot be overriden */
        setup.addJobParametersItem(new ProductExecutorConfigSetupJobParameter().key(PARAM_KEY_PDS_CONFIG_PRODUCTIDENTIFIER).value(pdsProductId));
        setup.addJobParametersItem(new ProductExecutorConfigSetupJobParameter().key(PARAM_KEY_PDS_SCAN_TARGET_TYPE).value(scanType.name()));
    }

    private ProductIdentifier resolveSecHubProductIdentifierForPdsWithSCanType(ScanType scanType) {
        String productIdentifierString = "PDS_" + (scanType.name().replace("_", ""));
        return ProductIdentifier.fromValue(productIdentifierString);
    }

    private void createProfilesAndAssignToProjects(SystemTestRuntimeContext context) throws ApiException {
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

    private void createProfileAndAssignProjects(String profileId, SystemTestRuntimeContext context) throws ApiException {

        SecHubClient client = context.getLocalAdminSecHubClient();
        SecHubConfigurationDefinition config = context.getLocalSecHubConfigurationOrFail();

        ProductExecutionProfile productExecutionProfile = new ProductExecutionProfile();
        productExecutionProfile.setEnabled(true);

        List<String> projectIdsForThisProfile = new ArrayList<>();
        for (ProjectDefinition projectDefinition : config.getProjects().get()) {
            if (projectDefinition.getProfiles().contains(profileId)) {
                projectIdsForThisProfile.add(projectDefinition.getName());
            }
        }
        productExecutionProfile.setProjectIds(projectIdsForThisProfile);

        client.withConfigurationApi().adminCreateExecutionProfile(profileId, productExecutionProfile);
    }

    private void addExecutorConfigurationsToProfiles(SystemTestRuntimeContext context, Map<String, List<UUID>> profileIdsToExecutorUUIDs) throws ApiException {
        /* assign executor configurations to profiles */
        SecHubClient client = context.getLocalAdminSecHubClient();

        for (String profileId : profileIdsToExecutorUUIDs.keySet()) {
            ProductExecutionProfile productExecutionProfile = new ProductExecutionProfile();
            productExecutionProfile.setEnabled(true);
            List<ProductExecutorConfig> productExecutorConfigs = new ArrayList<>();

            for (UUID executorConfigurationUUID : profileIdsToExecutorUUIDs.get(profileId)) {
                if (context.isDryRun()) {
                    LOG.info("Dry run: add executor config '{}' to profile '{}' is skipped", executorConfigurationUUID, profileId);
                    continue;
                }
                ProductExecutorConfig productExecutorConfig = new ProductExecutorConfig();
                productExecutorConfig.setUuid(executorConfigurationUUID);
                productExecutorConfigs.add(productExecutorConfig);
            }

            productExecutionProfile.setConfigurations(productExecutorConfigs);

            client.withConfigurationApi().adminUpdateExecutionProfile(profileId, productExecutionProfile);
        }
    }

    private void assignAdminAsUserToProjects(SystemTestRuntimeContext context) throws ApiException {
        SecHubConfigurationDefinition config = context.getLocalSecHubConfigurationOrFail();
        if (config.getProjects().isEmpty()) {
            return;
        }

        SecHubClient client = context.getLocalAdminSecHubClient();
        String userId = client.getUserId();
        for (ProjectDefinition projectDefinition : config.getProjects().get()) {
            String projectId = projectDefinition.getName();
            if (context.isDryRun()) {
                LOG.info("Dry run: assign user '{}' to project '{}' is skipped", userId, projectId);
                continue;
            }
            client.withProjectAdministrationApi().adminAssignUserToProject(projectId, userId);
        }
    }

}

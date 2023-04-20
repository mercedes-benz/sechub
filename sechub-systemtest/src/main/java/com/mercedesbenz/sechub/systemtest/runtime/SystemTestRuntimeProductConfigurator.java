package com.mercedesbenz.sechub.systemtest.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.Project;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.systemtest.config.LocalSecHubDefinition;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.ProjectDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;

public class SystemTestRuntimeProductConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeProductConfigurator.class);

    public void applyConfigurationWhenLocal(SystemTestRuntimeContext context) throws SecHubClientException {
        if (!context.isLocalRun()) {
            return;
        }
        if (!context.isLocalSecHubConfigured()) {
            return;
        }
        
        LocalSetupDefinition localSetup = context.getLocalSetupOrFail();
        LocalSecHubDefinition secHub = localSetup.getSecHub();
        
        SecHubConfigurationDefinition config = secHub.getConfigure();

        addProjects(context, config);
        assignAdminAsUserToProjects(context, config);

    }
    private void assignAdminAsUserToProjects(SystemTestRuntimeContext context, SecHubConfigurationDefinition config) throws SecHubClientException {
        if (config.getProjects().isEmpty()) {
            return;
        }
        for (ProjectDefinition projectDefinition : config.getProjects().get()) {
            String projectName = projectDefinition.getName();

            SecHubClient client = context.getLocalAdminSecHubClient();
            
            client.assignUserToProject(client.getUsername(), projectName);
            if (! client.isUserAssignedToProject()) {
                
            }
        }
    }
        
    
    private void addProjects(SystemTestRuntimeContext context, SecHubConfigurationDefinition config) throws SecHubClientException {
        if (config.getProjects().isEmpty()) {
            LOG.warn("No project defined - skip project configuration. Should only happen when only PDS is tested without SecHub.");
            return;
        }
        for (ProjectDefinition projectDefinition : config.getProjects().get()) {
            String projectName = projectDefinition.getName();

            SecHubClient client = context.getLocalAdminSecHubClient();

            if (context.isDryRun()) {
                LOG.info("Dry run: create project :"+projectName+" is skipped");
                continue;
            }
            if (client.isProjectExisting(projectName)) {
                LOG.warn("Project '{}' does already exist - skip creation", projectName);
                continue;
            }
            Project project = new Project();
            project.setApiVersion("1.0");
            project.setDescription("Test project");
            project.setName(projectName);
            project.setOwner(client.getUsername());// we use the administrator as owner of the project
            

            client.createProject(project);
        }
    }

}

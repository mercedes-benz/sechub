// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.java.demo.playground;

import static com.mercedesbenz.sechub.api.java.demo.DemoUtils.*;

import java.util.List;
import java.util.UUID;

import com.mercedesbenz.sechub.api.ExecutionProfileCreate;
import com.mercedesbenz.sechub.api.ExecutorConfiguration;
import com.mercedesbenz.sechub.api.ExecutorConfigurationSetup;
import com.mercedesbenz.sechub.api.ExecutorConfigurationSetupCredentials;
import com.mercedesbenz.sechub.api.ExecutorConfigurationSetupJobParameter;
import com.mercedesbenz.sechub.api.OpenUserSignup;
import com.mercedesbenz.sechub.api.Project;
import com.mercedesbenz.sechub.api.SecHubClient;
import com.mercedesbenz.sechub.api.SecHubClientException;
import com.mercedesbenz.sechub.api.SecHubStatus;
import com.mercedesbenz.sechub.api.UserSignup;

public class DemoAdminApiPlayground {
    SecHubClient client;
    long identifier = System.currentTimeMillis();
    private String userName;
    private String projectName;

    public DemoAdminApiPlayground(SecHubClient access) {
        userName = "un" + identifier;
        projectName = "pn" + identifier;

        this.client = access;
    }

    public void run() throws Exception {
        logTitle("Start testing admin API");

        checkStatus();

        signupNewUser();
        checkUserSignupListForNewUser();

        acceptUserAndCheckListedAsUser();

        createProjectAndCheckFoundInList();

        UUID uuid = createExecutorConfiguration();

        String profileId = createProfile();

        client.addExecutorConfigurationToProfile(uuid, profileId);

        logSuccess("Added executor configuration with uuid: " + uuid + " to profile : " + profileId);

    }

    private void checkStatus() throws SecHubClientException {
        client.triggerRefreshOfSecHubSchedulerStatus();
        logSuccess("Triggered refresh of sechub scheduler status");
        waitMilliseconds(500);

        fetchAndLogSechubStatus();

    }

    private void fetchAndLogSechubStatus() throws SecHubClientException {
        SecHubStatus status = client.fetchSecHubStatus();
        logSuccess("Sechub status: " + status);
    }

    private String createProfile() throws SecHubClientException {
        /* create a profile */
        String profileName = "profile" + identifier;
        ExecutionProfileCreate profile = new ExecutionProfileCreate();
        profile.setDescription("Something");
        profile.setEnabled(true);
        profile.getProjectIds().add(projectName);
        client.createExecutionProfile(profileName, profile);

        logSuccess("Profile: " + profile + " created");

        return profileName;
    }

    private UUID createExecutorConfiguration() throws SecHubClientException {
        /* create an executor configuration */
        ExecutorConfiguration configuration = new ExecutorConfiguration();

        configuration.setEnabled(false);
        configuration.setExecutorVersion(1);
        configuration.setName("PDS_TEST1");
        configuration.setProductIdentifier("PDS_CODESCAN");

        ExecutorConfigurationSetupJobParameter parameter1 = new ExecutorConfigurationSetupJobParameter();
        ExecutorConfigurationSetup setup = configuration.getSetup();
        setup.getJobParameters().add(parameter1);
        setup.setBaseURL("https://example.com:8443");

        ExecutorConfigurationSetupCredentials credentials = new ExecutorConfigurationSetupCredentials();
        credentials.setUser("user");
        credentials.setPassword("pwd");

        setup.setCredentials(credentials);

        UUID uuid = client.createExecutorConfiguration(configuration);
        logSuccess("Executor configuration with uuid: " + uuid + " created");
        return uuid;
    }

    private void createProjectAndCheckFoundInList() throws SecHubClientException {

        Project project = new Project();
        project.setOwner(userName);
        project.setName(projectName);
        project.setApiVersion("1.0");
        project.setDescription("description1");

        client.createProject(project);

        logSuccess("Project " + projectName + " created");

        List<String> projects = client.fetchAllProjectIds();
        assumeEquals(true, projects.contains(projectName), "Project " + projectName + " was found in list");
    }

    private void acceptUserAndCheckListedAsUser() throws SecHubClientException {
        client.acceptOpenSignup(userName);
        waitMilliseconds(300);

        List<String> usersList = client.fetchAllUserIds();
        logSuccess("List of users has entries: " + usersList.size());

        assumeEquals(true, usersList.contains(userName), "Accepted user is found in user list after signup");
    }

    private void checkUserSignupListForNewUser() throws SecHubClientException {
        List<OpenUserSignup> waitingSignups = client.fetchAllOpenSignups();
        boolean foundUserSignup = false;
        for (OpenUserSignup signup : waitingSignups) {
            foundUserSignup = signup.getUserId().equals(userName);
            if (foundUserSignup) {
                break;
            }
        }
        assumeEquals(true, foundUserSignup, "Signup for user: " + userName + " was found in list of signups");
    }

    private void signupNewUser() throws SecHubClientException {

        UserSignup signUp = new UserSignup();
        signUp.setApiVersion("1.0");
        signUp.setEmailAddress(userName + "@example.com");
        signUp.setUserId(userName);
        client.createSignup(signUp);

        waitMilliseconds(300);
    }

}

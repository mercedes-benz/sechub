// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.internal.DefaultJobOverviewData;
import com.mercedesbenz.sechub.api.internal.DefaultSchedulerData;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;

public class MockedSecHubClient extends AbstractSecHubClient {

    private static final Logger LOG = LoggerFactory.getLogger(MockedSecHubClient.class);

    private static final String REF_MARKER = "->";

    private Map<String, ExecutionProfile> executionProfiles = new HashMap<>();
    private Map<UUID, ExecutorConfiguration> executorConfigurations = new HashMap<>();
    private Map<UUID, Job> jobs = new HashMap<>();
    private Map<String, Project> projects = new TreeMap<>();
    private Map<String, OpenUserSignup> openSignups = new TreeMap<>();
    private MockDataAccess mockDataAccess;
    private Map<String, User> users = new TreeMap<>();

    private Set<String> userToProjectAssignments = new HashSet<>();

    private MockedSecHubClient(URI serverUri, String username, String apiToken, boolean trustAll) {
        super(serverUri, username, apiToken, trustAll);

        mockDataAccess = new MockDataAccess();
    }

    public static MockedSecHubClient from(URI serverUri, String username, String apiToken, boolean trustAll) {
        return new MockedSecHubClient(serverUri, username, apiToken, trustAll);
    }

    @Override
    public void acceptOpenSignup(String signupUsername) throws SecHubClientException {
        OpenUserSignup signup = openSignups.get(signupUsername);
        if (signup == null) {
            throw new SecHubClientException("Signup for user:" + signupUsername + " not found!");
        }
        User user = createUser(signup);
        users.put(user.getUserId(), user);

        openSignups.remove(signupUsername);
    }

    @Override
    public void addExecutorConfigurationToProfile(UUID uuidOfExecutorConfigToAdd, String profileId) throws SecHubClientException {
        ExecutorConfiguration configuration = findConfigurationOrFail(uuidOfExecutorConfigToAdd);
        ExecutionProfile profile = findProfileOrFail(profileId);
        profile.getConfigurations().add(configuration);
    }

    private ExecutionProfile findProfileOrFail(String profileId) throws SecHubClientException {
        ExecutionProfile profile = executionProfiles.get(profileId);
        if (profile == null) {
            throw new SecHubClientException("Did not find profile for id:" + profileId);
        }
        return profile;
    }

    private ExecutorConfiguration findConfigurationOrFail(UUID uuidOfExecutorConfigToAdd) throws SecHubClientException {
        ExecutorConfiguration configuration = executorConfigurations.get(uuidOfExecutorConfigToAdd);
        if (configuration == null) {
            throw new SecHubClientException("executor configuration not found:" + uuidOfExecutorConfigToAdd);
        }
        return configuration;
    }

    @Override
    public void approveJob(String projectId, UUID jobUUID) throws SecHubClientException {
        Job job = findJobOrFail(jobUUID);
        job.getStatus().setState(ExecutionState.READY_TO_START);
    }

    @Override
    public void assignUserToProject(String userId, String projectId) throws SecHubClientException {
        userToProjectAssignments.add(createUserToProjectUniqueIdentifier(userId, projectId));
    }

    @Override
    public void createExecutionProfile(String profileName, ExecutionProfileCreate profileToCreate) throws SecHubClientException {

        ExecutionProfile profile = new ExecutionProfile();
        profile.setEnabled(profileToCreate.isEnabled());
        profile.setDescription(profileToCreate.getDescription());
        profile.getProjectIds().addAll(profileToCreate.getProjectIds());

        executionProfiles.put(profileName, profile);
    }

    @Override
    public UUID createExecutorConfiguration(ExecutorConfiguration config) throws SecHubClientException {
        UUID uuid = UUID.randomUUID();
        executorConfigurations.put(uuid, config);
        return uuid;
    }

    @Override
    public UUID createJob(SecHubConfigurationModel configuration) throws SecHubClientException {
        UUID uuid = UUID.randomUUID();
        Job job = new Job(uuid);
        jobs.put(uuid, job);
        return uuid;
    }

    @Override
    public void createProject(Project project) throws SecHubClientException {
        String projectName = project.getName();

        if (projects.containsKey(projectName)) {
            throw new SecHubClientException("Project:" + projectName + " already exists!");
        }
        projects.put(projectName, project);

    }

    @Override
    public void createSignup(UserSignup signUp) throws SecHubClientException {
        String userId = signUp.getUserId();
        if (openSignups.containsKey(userId)) {
            throw new SecHubClientException("User already exists!");
        }
        OpenUserSignup openSignup = new OpenUserSignup();
        openSignup.setEmailAddress(signUp.getEmailAddress());
        openSignup.setUserId(userId);
        openSignups.put(userId, openSignup);
    }

    @Override
    public void deleteExecutionProfile(String profileId) throws SecHubClientException {
        executionProfiles.remove(profileId);
    }

    @Override
    public void deleteExecutorConfiguration(UUID executorUUID) throws SecHubClientException {
        ExecutorConfiguration removed = executorConfigurations.remove(executorUUID);
        if (removed == null) {
            LOG.warn("Executor configuration did not exist for uuid:" + executorUUID + " - cannot remove");
            return;
        }
        for (ExecutionProfile executionProfile : executionProfiles.values()) {
            executionProfile.getConfigurations().remove(removed);
        }
    }

    @Override
    public void deleteProject(String projectId) throws SecHubClientException {
        projects.remove(projectId);

        removeReferenceEntries(projectId, userToProjectAssignments);
    }

    private void removeReferenceEntries(String referenceId, Set<String> all) {
        Set<String> assignmentIdToRemove = new HashSet<>(all);
        for (String id : all) {
            if (id.endsWith(REF_MARKER + referenceId)) {
                assignmentIdToRemove.add(id);
            }
        }

        all.removeAll(assignmentIdToRemove);
    }

    @Override
    public SecHubReport downloadSecHubReportAsJson(String projectId, UUID jobUUID) throws SecHubClientException {
        SecHubReport secHubReport = mockDataAccess.reports.get(jobUUID);
        if (secHubReport == null) {
            throw new SecHubClientException("No sechub report available for job uuid:" + jobUUID);
        }
        return secHubReport;
    }

    @Override
    public List<ExecutorConfigurationInfo> fetchAllExecutorConfigurationInfo() throws SecHubClientException {
        List<ExecutorConfigurationInfo> result = new ArrayList<>();
        for (ExecutorConfiguration executorConfiguration : executorConfigurations.values()) {
            ExecutorConfigurationInfo info = new ExecutorConfigurationInfo();
            info.setEnabled(executorConfiguration.isEnabled());
            info.setName(executorConfiguration.getName());
            /*
             * TODO Albert Tregnaghi, 2023-09-08: set UUID to info when
             * https://github.com/mercedes-benz/sechub/issues/2537 is done
             */
            result.add(info);
        }
        return result;
    }

    @Override
    public List<OpenUserSignup> fetchAllOpenSignups() throws SecHubClientException {
        return new ArrayList<>(openSignups.values());
    }

    @Override
    public List<String> fetchAllProjectIds() throws SecHubClientException {
        return new ArrayList<>(projects.keySet());
    }

    @Override
    public List<String> fetchAllUserIds() throws SecHubClientException {
        return new ArrayList<>(users.keySet());
    }

    @Override
    public JobStatus fetchJobStatus(String projectId, UUID jobUUID) throws SecHubClientException {
        Job job = findJobOrFail(jobUUID);
        return job.getStatus();
    }

    @Override
    public SecHubStatus fetchSecHubStatus() throws SecHubClientException {
        return getMockDataAccess().getSecHubStatus();
    }

    public MockDataAccess getMockDataAccess() {
        return mockDataAccess;
    }

    @Override
    public boolean isExecutionProfileExisting(String profileId) throws SecHubClientException {
        return executionProfiles.containsKey(profileId);
    }

    @Override
    public boolean isProjectExisting(String projectId) throws SecHubClientException {
        return projects.containsKey(projectId);
    }

    @Override
    public boolean isServerAlive() throws SecHubClientException {
        return mockDataAccess.serverAlive;
    }

    @Override
    public boolean isUserAssignedToProject(String userId, String projectId) throws SecHubClientException {
        String id = createUserToProjectUniqueIdentifier(userId, projectId);
        return userToProjectAssignments.contains(id);
    }

    @Override
    public void triggerRefreshOfSecHubSchedulerStatus() throws SecHubClientException {
        informNothingDoneButOnlySimulated("trigger refresh sechub scheduler status");
    }

    @Override
    public void unassignUserFromProject(String userId, String projectId) throws SecHubClientException {
        userToProjectAssignments.remove(createUserToProjectUniqueIdentifier(userId, projectId));
    }

    @Override
    public void upload(String projectId, UUID jobUUID, SecHubConfigurationModel configuration, Path workingDirectory) throws SecHubClientException {
        informNothingDoneButOnlySimulated("upload");
    }

    @Override
    public Path downloadFullScanLog(UUID sechubJobUUID, Path downloadFilePath) throws SecHubClientException {
        informNothingDoneButOnlySimulated("download full scan log for sechub job: " + sechubJobUUID + " into path:" + downloadFilePath);
        final File targetFile = calculateFullScanLogFile(sechubJobUUID, downloadFilePath);
        return targetFile.toPath();

    }

    private User createUser(OpenUserSignup found) {
        return new User(found.getUserId(), found.getEmailAddress());
    }

    private String createUserToProjectUniqueIdentifier(String user, String projectId) {
        return "u2p:" + user + REF_MARKER + projectId;
    }

    private Job findJobOrFail(UUID jobUUID) throws SecHubClientException {
        Job job = jobs.get(jobUUID);
        if (job == null) {
            throw new SecHubClientException("Did not find job with uuid:" + jobUUID);
        }
        return job;
    }

    private void informNothingDoneButOnlySimulated(String what) {
        LOG.info("Simulate {}", what);
    }

    /**
     * This class is an access point for simulated data which has no official API
     * method to change it, but can be changed for the mock.
     *
     * @author Albert Tregnaghi
     *
     */
    public class MockDataAccess {

        private SecHubStatus sechubStatus;
        private Map<UUID, SecHubReport> reports = new HashMap<>();

        private boolean serverAlive;
        private String serverVersion = "0.0.0-mocked";

        public String getServerVersion() {
            return serverVersion;
        }

        public void setServerVersion(String serverVersion) {
            this.serverVersion = serverVersion;
        }

        public MockDataAccess() {

            DefaultJobOverviewData jobOverview = new DefaultJobOverviewData();
            jobOverview.setAll(123456);
            jobOverview.setCanceled(1);
            jobOverview.setCanceled(2);
            jobOverview.setEnded(3);
            jobOverview.setInitializating(4);
            jobOverview.setReadyToStart(5);
            jobOverview.setStarted(6);

            DefaultSchedulerData scheduler = new DefaultSchedulerData(true, jobOverview);
            sechubStatus = new SecHubStatus(scheduler);
        }

        public SecHubStatus getSecHubStatus() {
            return sechubStatus;
        }

        public void setServerAlive(boolean serverAlive) {
            this.serverAlive = serverAlive;
        }

        public void setSecHubReportForJob(UUID jobUUID, SecHubReport report) {
            reports.put(jobUUID, report);
        }
    }

    @Override
    public String getServerVersion() throws SecHubClientException {
        return mockDataAccess.getServerVersion();
    }

    @Override
    public void requestNewApiToken(String emailAddress) throws SecHubClientException {
        LOG.info("new api token was requested for email address: {}", emailAddress);
    }

}

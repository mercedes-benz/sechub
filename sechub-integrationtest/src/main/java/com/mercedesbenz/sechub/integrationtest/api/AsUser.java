// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.integrationtest.JSONTestSupport;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestTemplateFile;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubClientExecutor.ExecutionResult;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubJobAutoDumper;
import com.mercedesbenz.sechub.integrationtest.internal.SimpleTestStringList;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData;
import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;
import com.mercedesbenz.sechub.integrationtest.internal.TestRestHelper;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;
import com.mercedesbenz.sechub.test.SecHubTestURLBuilder;
import com.mercedesbenz.sechub.test.TestUtil;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfile;
import com.mercedesbenz.sechub.test.executionprofile.TestExecutionProfileList;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfig;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorConfigList;
import com.mercedesbenz.sechub.test.executorconfig.TestExecutorSetupJobParam;

public class AsUser {
    private static final Logger LOG = LoggerFactory.getLogger(AsUser.class);
    private JSONTestSupport jsonTestSupport = JSONTestSupport.DEFAULT;
    private SecHubJobAutoDumper autoDumper = new SecHubJobAutoDumper();
    TestUser user;
    private TextFileWriter writer;
    private boolean enableHTMLautoDumps;

    AsUser(TestUser user) {
        this.user = user;
    }

    public WithSecHubClient withSecHubClient() {
        return new WithSecHubClient(this);
    }

    public AsUser enablePDSAutoDumpOnErrorsForSecHubJob(UUID sechubJobUUID) {
        this.autoDumper.enablePDSAutoDumpOnErrorsForSecHubJob();
        this.autoDumper.setSecHubJobUUID(sechubJobUUID);
        return this;
    }

    public AsUser uploadSourcecode(TestProject project, UUID jobUUID, File file, String checkSum) {
        /* @formatter:off */
        autoDumper.execute(() -> getRestHelper().upload(getUrlBuilder().
		        buildUploadSourceCodeUrl(project.getProjectId(),jobUUID),file,checkSum)
		);
        /* @formatter:on */
        return this;
    }

    public AsUser uploadBinaries(TestProject project, UUID jobUUID, File file, String checkSum) {
        /* @formatter:off */
        autoDumper.execute(() ->
        getRestHelper().upload(getUrlBuilder().
                buildUploadBinariesUrl(project.getProjectId(),jobUUID),file,checkSum))
        ;
        /* @formatter:on */
        return this;
    }

    public List<String> listAllUserIds() {
        return autoDumper.execute(() -> {
            String json = getRestHelper().getJSON(getUrlBuilder().buildAdminListsUsersUrl());
            SimpleTestStringList list = JSONConverter.get().fromJSON(SimpleTestStringList.class, json);
            return list;
        });
    }

    /**
     * Upload given resource as source code, checksum will be automatically
     * calculated
     *
     * @param userWantingToSignup
     * @return
     */
    public AsUser uploadSourcecode(TestProject project, UUID jobUUID, String pathInsideResources) {
        autoDumper.execute(() -> {
            File uploadFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath(pathInsideResources);
            String checkSum = TestAPI.createSHA256Of(uploadFile);
            uploadSourcecode(project, jobUUID, uploadFile, checkSum);
        });
        return this;
    }

    /**
     * Upload given resource as binaries, checksum will be automatically calculated
     *
     * @param userWantingToSignup
     * @return
     */
    public AsUser uploadBinaries(TestProject project, UUID jobUUID, String pathInsideResources) {
        autoDumper.execute(() -> {
            File uploadFile = IntegrationTestFileSupport.getTestfileSupport().createFileFromResourcePath(pathInsideResources);
            String checkSum = TestAPI.createSHA256Of(uploadFile);
            uploadBinaries(project, jobUUID, uploadFile, checkSum);
        });
        return this;
    }

    /**
     * Accept the user wanting to signup
     *
     * @param userWantingToSignup
     * @return
     */
    public AsUser acceptSignup(TestUser userWantingToSignup) {
        if (userWantingToSignup == null) {
            fail("user may not be null!");
            return null;
        }

        /* @formatter:off */
		getRestHelper().post(getUrlBuilder().
		    		buildAdminAcceptsUserSignUpUrl(userWantingToSignup.getUserId()));
		/* @formatter:on */
        return this;
    }

    private TestRestHelper getRestHelper() {
        return getContext().getRestHelper(user);
    }

    /**
     * Signup given (new) user
     *
     * @param user
     * @return this
     */
    public AsUser signUpAs(TestUser user) {

        String json = "{\"apiVersion\":\"1.0\",\r\n" + "		\"userId\":\"" + user.getUserId() + "\",\r\n" + "		\"emailAddress\":\"" + user.getEmail()
                + "\"}";
        getRestHelper().postJson(getUrlBuilder().buildUserSignUpUrl(), json);
        return this;

    }

    public AsUser requestNewApiTokenFor(String emailAddress) {
        getRestHelper().postJson(getUrlBuilder().buildAnonymousRequestNewApiToken(emailAddress), "");
        return this;
    }

    private SecHubTestURLBuilder getUrlBuilder() {
        return getContext().getUrlBuilder();
    }

    private IntegrationTestContext getContext() {
        return IntegrationTestContext.get();
    }

    /**
     * User trigger create of project
     *
     * @param project project instance
     * @param owner   owner of the project
     * @throws RestClientException
     */
    public AsUser createProject(TestProject project, TestUser owner) {
        return createProject(project, owner.getUserId());
    }

    /**
     * User trigger create of project
     *
     * @param project     project instance
     * @param ownerUserId owner of the project
     * @throws RestClientException
     */
    public AsUser createProject(TestProject project, String ownerUserId) {
        if (ownerUserId == null) {
            // we use always the user how creates the project as owner when not explicit set
            ownerUserId = this.user.getUserId();
        }
        /* @formatter:off */
		StringBuilder json = new StringBuilder();
		TestJSONHelper jsonHelper = TestJSONHelper.get();
		json.append("{\n" +
				" \"apiVersion\":\"1.0\",\n" +
				" \"name\":\""+project.getProjectId()+"\",\n" +
				" \"owner\":\""+ownerUserId+"\",\n" +
				" \"description\":\""+project.getDescription()+"\"");
		if (! project.getWhiteListUrls().isEmpty()) {
			json.append(",\n \"whiteList\" : {\"uris\":[");

			for (Iterator<String> it = project.getWhiteListUrls().iterator();it.hasNext();) {
				String url = it.next();
				json.append("\""+url+"\"");
				if (it.hasNext()){
					json.append(",");
				}
			}
			json.append("]\n");
			json.append("                 }\n");
		}

		json.append("}\n");
		jsonHelper.assertValidJson(json.toString());
		/* @formatter:on */
        getRestHelper().postJson(getUrlBuilder().buildAdminCreatesProjectUrl(), json.toString());
        return this;
    }

    public AsUser createProductExecutionProfile(String profileId, TestExecutionProfile profile) {
        String url = getUrlBuilder().buildAdminCreatesProductExecutionProfile(profileId);
        String json = JSONConverter.get().toJSON(profile);
        getRestHelper().postJson(url, json);
        return this;
    }

    public AsUser updateProductExecutionProfile(String profileId, TestExecutionProfile profile) {
        String url = getUrlBuilder().buildAdminUpdatesProductExecutionProfile(profileId);
        String json = JSONConverter.get().toJSON(profile);
        getRestHelper().putJSON(url, json);
        return this;
    }

    public TestExecutionProfile fetchProductExecutionProfile(String profileId) {
        String url = getUrlBuilder().buildAdminFetchesProductExecutionProfile(profileId);
        String json = getRestHelper().getJSON(url);
        return JSONConverter.get().fromJSON(TestExecutionProfile.class, json);
    }

    public TestExecutionProfileList fetchProductExecutionProfiles() {
        String json = fetchProductExecutionProfilesAsJSON();
        return JSONConverter.get().fromJSON(TestExecutionProfileList.class, json);
    }

    public String fetchProductExecutionProfilesAsJSON() {
        String url = getUrlBuilder().buildAdminFetchesListOfProductExecutionProfiles();
        String json = getRestHelper().getJSON(url);
        return json;
    }

    public void deleteProductExecutionProfile(String profileId) {
        deleteProductExecutionProfile(profileId, true);
    }

    void deleteProductExecutionProfile(String profileId, boolean protectDefaultProfiles) {
        if (protectDefaultProfiles) {
            TestAPI.assertNoDefaultProfileId(profileId);
        }
        String url = getUrlBuilder().buildAdminDeletesProductExecutionProfile(profileId);
        getRestHelper().delete(url);
    }

    public UUID createProductExecutorConfig(TestExecutorConfig config) {
        String url = getUrlBuilder().buildAdminCreatesProductExecutorConfig();
        String json = JSONConverter.get().toJSON(config);
        String result = getRestHelper().postJson(url, json);
        return UUID.fromString(result);
    }

    public AsUser addConfigurationToProfile(String profileId, UUID... uuids) {
        String url = getUrlBuilder().buildAdminFetchesProductExecutionProfile(profileId);
        String json = getRestHelper().getJSON(url);
        TestExecutionProfile profile = JSONConverter.get().fromJSON(TestExecutionProfile.class, json);

        for (UUID uuid : uuids) {
            TestExecutorConfig config = new TestExecutorConfig(uuid);
            // we do not need to load, because update service does only need uuids, other
            // parts are ignored*/
            profile.configurations.add(config);
        }

        return updateProductExecutionProfile(profileId, profile);
    }

    public AsUser removeConfigurationFromProfile(String profileId, UUID... uuids) {
        String url = getUrlBuilder().buildAdminFetchesProductExecutionProfile(profileId);
        String json = getRestHelper().getJSON(url);
        TestExecutionProfile profile = JSONConverter.get().fromJSON(TestExecutionProfile.class, json);

        for (UUID uuid : uuids) {
            TestExecutorConfig config = new TestExecutorConfig(uuid);
            // we do not need to load, because update service does only need uuids, other
            // parts are ignored*/
            profile.configurations.remove(config);// equals implemented with uuid, so works..
        }

        return updateProductExecutionProfile(profileId, profile);
    }

    public AsUser removeProjectIdsFromProfile(String profileId, String... projectIds) {
        for (String projectId : projectIds) {
            getRestHelper().delete(getUrlBuilder().buildAdminRemovesProjectFromExecutionProfile(profileId, projectId));
        }
        return this;
    }

    public TestExecutorConfigList fetchProductExecutorConfigList() {
        String json = fetchProductExecutorConfigListAsJSON();
        return JSONConverter.get().fromJSON(TestExecutorConfigList.class, json);
    }

    public String fetchProductExecutorConfigListAsJSON() {
        String url = getUrlBuilder().buildAdminFetchesListOfProductExecutionConfigurations();
        String json = getRestHelper().getJSON(url);
        return json;
    }

    public String fetchProductExecutorConfigAsJSON(UUID uuid) {
        String url = getUrlBuilder().buildAdminFetchesProductExecutorConfig(uuid);
        return getRestHelper().getJSON(url);
    }

    public TestExecutorConfig fetchProductExecutorConfig(UUID uuid) {
        String json = fetchProductExecutorConfigAsJSON(uuid);
        TestExecutorConfig result = JSONConverter.get().fromJSON(TestExecutorConfig.class, json);
        return result;
    }

    public void updateProdcutExecutorConfig(UUID uuid, TestExecutorConfig config) {
        String url = getUrlBuilder().buildAdminUpdatesProductExecutorConfig(uuid);
        String json = JSONConverter.get().toJSON(config);
        getRestHelper().putJSON(url, json);
    }

    public AsUser deleteProductExecutorConfig(UUID uuid) {
        String url = getUrlBuilder().buildAdminDeletesProductExecutorConfig(uuid);
        getRestHelper().delete(url);
        return this;
    }

    public AsUser addProjectsToProfile(String profileId, TestProject... projects) {
        List<String> projectIds = new ArrayList<>();
        for (TestProject project : projects) {
            projectIds.add(project.getProjectId());
        }
        return addProjectIdsToProfile(profileId, projectIds.toArray(new String[projectIds.size()]));
    }

    public AsUser addProjectIdsToProfile(String profileId, String... projectIds) {
        for (String projectId : projectIds) {
            getRestHelper().post(getUrlBuilder().buildAdminAddsProjectToExecutionProfile(profileId, projectId));
        }
        return this;
    }

    public String getServerURL() {
        return getUrlBuilder().buildServerURL();
    }

    public String getStringFromURL(String link) {
        return getRestHelper().getStringFromURL(link);
    }

    /**
     * Assigns owner to a project
     *
     * @param targetUser
     * @param project
     * @return this
     */
    public AsUser assignOwnerToProject(TestUser targetUser, TestProject project) {
        LOG.debug("assigning owner:{} to project:{}", user.getUserId(), project.getProjectId());
        getRestHelper().postJson(getUrlBuilder().buildAdminChangesProjectOwnerUrl(project.getProjectId(), targetUser.getUserId()), "");
        return this;
    }

    /**
     * Assigns user to a project
     *
     * @param targetUser
     * @param project
     * @return this
     */
    public AsUser assignUserToProject(TestUser targetUser, TestProject project) {
        LOG.debug("assigning user:{} to project:{}", user.getUserId(), project.getProjectId());
        getRestHelper().postJson(getUrlBuilder().buildAdminAssignsUserToProjectUrl(project.getProjectId(), targetUser.getUserId()), "");
        return this;
    }

    /**
     * Unassigns user from project
     *
     * @param targetUser
     * @param project
     * @return this
     */
    public AsUser unassignUserFromProject(TestUser targetUser, TestProject project) {
        LOG.debug("unassigning user:{} from project:{}", user.getUserId(), project.getProjectId());
        getRestHelper().delete(getUrlBuilder().buildAdminUnassignsUserFromProjectUrl(project.getProjectId(), targetUser.getUserId()));
        return this;
    }

    /**
     * Creates a code scan job and returns corresponding job UUID. But job is NOT
     * approved and so not started!
     *
     * @param project
     * @param runMode
     * @return job UUID
     */
    public UUID createCodeScan(TestProject project, IntegrationTestMockMode runMode) {
        return createCodeScanJobAndFetchJobUUID(IntegrationTestTemplateFile.CODE_SCAN_1_SOURCE_EMBEDDED, project, runMode, Collections.emptyMap());

    }

    public UUID createCodeScanWithTemplate(IntegrationTestTemplateFile template, TestProject project, IntegrationTestMockMode runMode, TemplateData data) {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.putAll(data.getVariables());
        int index = 0;
        for (String id : data.getReferenceIds()) {
            index++;
            variableMap.put("__use" + index + "__", id);
        }
        return createCodeScanJobAndFetchJobUUID(template, project, runMode, variableMap);
    }

    private UUID createCodeScanJobAndFetchJobUUID(IntegrationTestTemplateFile template, TestProject project, IntegrationTestMockMode runMode,
            Map<String, String> variableMap) {
        assertProject(project).doesExist();
        if (runMode == null) {
            runMode = IntegrationTestMockMode.CODE_SCAN__CHECKMARX__MULTI__ZERO_WAIT;
        }
        String response = createCodeScanJob(template, project, runMode, variableMap);
        return fetchJobUUID(response);
    }

    private static Map<String, String> configTemplateCache = new HashMap<>();

    private String getConfigTemplate(IntegrationTestTemplateFile templateFile) {
        String templateFilename = templateFile.getTemplateFilename();
        String template = configTemplateCache.get(templateFilename);
        if (template == null) {
            template = IntegrationTestFileSupport.getTestfileSupport().loadTestFile(templateFilename);
            configTemplateCache.put(templateFilename, template);
        }
        return template;
    }

    private String createCodeScanJob(IntegrationTestTemplateFile template, TestProject project, IntegrationTestMockMode runMode,
            Map<String, String> customVariableMap) {

        String templateJson = getConfigTemplate(template);
        String projectId = project.getProjectId();

        Map<String, String> map = new HashMap<>();
        map.putAll(customVariableMap);
        // add default variables
        map.put("__projectname__", projectId);
        if (!customVariableMap.containsKey("__folder__")) {
            String folder = null;
            if (runMode != null) {
                folder = runMode.getMockDataIdentifier();
            }
            if (folder == null) {
                folder = "notexisting";
            }
            map.put("__folder__", folder);
        }

        for (String variableName : map.keySet()) {
            String replacement = map.get(variableName);
            templateJson = templateJson.replaceAll(variableName, replacement);
        }
        String url = getUrlBuilder().buildAddJobUrl(projectId);
        return getRestHelper().postJson(url, templateJson);
    }

    private String createWebScanJob(TestProject project, IntegrationTestMockMode runMode, IntegrationTestTemplateFile customTemplateFile) {
        List<String> whites = project.getWhiteListUrls();
        String acceptedURI1 = createTargetURIForSechubConfiguration(runMode, whites);

        return createWebScanJobForTargetURL(project, acceptedURI1, customTemplateFile);
    }

    private String createWebScanJobForTargetURL(TestProject project, String targetURL, IntegrationTestTemplateFile customTemplateFile) {
        IntegrationTestTemplateFile templateToUse = customTemplateFile;
        if (templateToUse == null) {
            templateToUse = IntegrationTestTemplateFile.WEBSCAN_1;
        }
        String json = getConfigTemplate(templateToUse);
        String projectId = project.getProjectId();

        json = json.replaceAll("__projectId__", projectId);

        json = json.replaceAll("__acceptedUri1__", targetURL);
        String url = getUrlBuilder().buildAddJobUrl(projectId);
        return getRestHelper().postJson(url, json);
    }

    public UUID createJobAndReturnJobUUID(TestProject project, SecHubConfigurationModel config) {
        String resultAsString = createJobAndReturnResultAsString(project, config);
        return fetchJobUUID(resultAsString);
    }

    public String createJobAndReturnResultAsString(TestProject project, SecHubConfigurationModel config) {
        String projectId = project.getProjectId();
        config.setProjectId(projectId);

        String json = JSONConverter.get().toJSON(config);
        String url = getUrlBuilder().buildAddJobUrl(projectId);
        String resultAsString = getRestHelper().postJson(url, json);
        return resultAsString;
    }

    private String createTargetURIForSechubConfiguration(IntegrationTestMockMode runMode, List<String> whites) {
        String acceptedURI1 = null;
        if (runMode != null) {
            acceptedURI1 = runMode.getMockDataIdentifier();
        }
        if (acceptedURI1 != null) {
            return acceptedURI1;
        }
        if (whites == null || whites.isEmpty()) {
            return "https://undefined.com";
        }
        /* okay, no runmode used having whitelist entry */
        List<String> copy = new ArrayList<>(whites);
        for (IntegrationTestMockMode mode : IntegrationTestMockMode.values()) {
            String target = mode.getMockDataIdentifier();
            if (target != null) {
                /* we drop all existing run mode parts here - to avoid side effects */
                copy.remove(target);
            }
        }
        return copy.iterator().next();
    }

    public void approveJob(TestProject project, UUID jobUUID) {
        getRestHelper().put(getUrlBuilder().buildApproveJobUrl(project.getProjectId(), jobUUID.toString()));
    }

    public AsUser updateWhiteListForProject(TestProject project, List<String> uris) {
        String json = getConfigTemplate(IntegrationTestTemplateFile.UPDATE_WHITELIST);
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = uris.iterator(); it.hasNext();) {
            sb.append("\\\"");
            sb.append(it.next());
            sb.append("\\\"");
            if (it.hasNext()) {
                sb.append(" , ");
            }
        }
        json = json.replaceAll("__acceptedUris__", sb.toString());
        getRestHelper().postJson(getUrlBuilder().buildUpdateProjectWhiteListUrl(project.getProjectId()), json);
        return this;

    }

    public AsUser updateMetaDataForProject(TestProject project, Map<String, String> metaData) {
        String json = getConfigTemplate(IntegrationTestTemplateFile.UPDATE_METADATA);
        StringBuilder sb = new StringBuilder();

        Iterator<Entry<String, String>> iterator = metaData.entrySet().iterator();
        iterator.forEachRemaining(entry -> {
            sb.append("\\\"");
            sb.append(entry.getKey());
            sb.append("\\\":");
            sb.append("\\\"");
            sb.append(entry.getValue());
            sb.append("\\\"");
            if (iterator.hasNext()) {
                sb.append(",");
            }
        });

        json = json.replaceAll("__metaData__", sb.toString());
        getRestHelper().postJson(getUrlBuilder().buildUpdateProjectMetaData(project.getProjectId()), json);
        return this;
    }

    public String getJobStatus(TestProject project, UUID jobUUID) {
        return getJobStatus(project.getProjectId(), jobUUID);
    }

    public String getJobStatus(String projectId, UUID jobUUID) {
        return getRestHelper().getJSON(getUrlBuilder().buildGetJobStatusUrl(projectId, jobUUID.toString()));
    }

    public String getJobReport(TestProject project, UUID jobUUID) {
        return getJobReport(project.getProjectId(), jobUUID);
    }

    public String getJobReport(String projectId, UUID jobUUID) {
        waitForJobToFinish(projectId, jobUUID);

        /* okay report is available - so do download */
        return getRestHelper().getJSON(getUrlBuilder().buildGetJobReportUrl(projectId, jobUUID));
    }

    private TextFileWriter getWriter() {
        if (writer == null) {
            writer = new TextFileWriter();
        }
        return writer;
    }

    public AsUser enableAutoDumpForHTMLReports() {
        this.enableHTMLautoDumps = true;
        return this;
    }

    public String getHTMLJobReport(TestProject project, UUID jobUUID) {
        waitForJobToFinish(project.getProjectId(), jobUUID);

        /* okay report is available - so do download */
        String html = getRestHelper().getStringFromURL(getUrlBuilder().buildGetJobReportUrl(project.getProjectId(), jobUUID), MediaType.TEXT_HTML);
        if (enableHTMLautoDumps) {
            try {
                getWriter().save(new File("./build/test-results/html-reports/" + jobUUID + ".html"), html, false);
            } catch (IOException e) {
                throw new IllegalStateException("Was not able to dump HTML data", e);
            }
        }
        return html;
    }

    public String getSpdxReport(TestProject project, UUID jobUUID) {
        return getSpdxReport(project.getProjectId(), jobUUID);
    }

    public String getSpdxReport(String projectId, UUID jobUUID) {
        waitForJobToFinish(projectId, jobUUID);

        /* okay report is available - so do download */
        return getRestHelper().getJSON(getUrlBuilder().buildGetJobReportUrlSpdx(projectId, jobUUID));
    }

    private void waitForJobToFinish(String projectId, UUID jobUUID) {
        long waitTimeInMillis = 1000;
        int count = 0;
        boolean jobEnded = false;
        String jobstatus = null;
        while (count < 10) {
            jobstatus = getJobStatus(projectId, jobUUID);
            if (jobstatus.indexOf("ENDED") != -1) {
                jobEnded = true;
                break;
            }
            TestUtil.waitMilliseconds(waitTimeInMillis);
            ++count;
        }
        if (!jobEnded) {
            throw new IllegalStateException("Even after " + count + " retries, every waiting " + waitTimeInMillis
                    + " ms, no job report state ENDED was accessible!\nLAST fetched jobstatus for " + jobUUID + " in project " + projectId + " was:\n"
                    + jobstatus);
        }
    }

    /**
     * When not changed by project specific mock data setup this will result in a
     * RED traffic light result
     *
     * @param project
     * @return execution result
     */
    public ExecutionResult createWebScanAndFetchScanData(TestProject project) {
        ExecutionResult result = withSecHubClient().startSynchronScanFor(project, IntegrationTestJSONLocation.CLIENT_JSON_WEBSCAN_RED_ZERO_WAIT);
        return result;
    }

    public String restartJobAndFetchJobStatus(TestProject project, UUID sechubJobUUID) {
        restartJob(sechubJobUUID);
        waitForJobDoneAndEvenWaitWhileJobIsFailing(project, sechubJobUUID);
        return getJobStatus(project.getProjectId(), sechubJobUUID);
    }

    public String restartJobHardAndFetchJobStatus(TestProject project, UUID sechubJobUUID) {
        restartJobHard(sechubJobUUID);
        waitForJobDoneAndEvenWaitWhileJobIsFailing(project, sechubJobUUID);
        return getJobStatus(project.getProjectId(), sechubJobUUID);
    }

    /**
     * Creates a webscan job for project (but job is not approved, so will not be
     * started)
     *
     * @param project
     * @return uuid for created job
     */
    public UUID createWebScan(TestProject project) {
        return createWebScan(project, null, true);
    }

    /**
     * Creates a webscan job for project (but job is not approved, so will not be
     * started)
     *
     * @param project
     * @return uuid for created job
     */
    public UUID createWebScan(TestProject project, IntegrationTestTemplateFile templateFile) {
        return createWebScan(project, null, true, templateFile);
    }

    /**
     * Creates a webscan job for project (but job is not approved, so will not be
     * started)
     *
     * @param project
     * @param useLongRunningButGreen
     * @return uuid for created job
     */
    public UUID createWebScan(TestProject project, IntegrationTestMockMode runMode) {
        return createWebScan(project, runMode, true);
    }

    /**
     * Creates a webscan job for project (but job is not approved, so will not be
     * started)
     *
     * @param project
     * @param checkExists
     * @return uuid for created job
     */
    public UUID createWebScan(TestProject project, boolean checkExists) {
        return createWebScan(project, null, checkExists);
    }

    /**
     * Creates a webscan job for project (but job is not approved, so will not be
     * started)
     *
     * @param project     the used test project
     * @param runMode     wanted mock mode. If <code>null</code> the default
     *                    {@link IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__ZERO_WAIT}
     *                    will be used
     * @param checkExists
     * @return uuid for created job
     */
    public UUID createWebScan(TestProject project, IntegrationTestMockMode runMode, boolean checkExists) {
        return createWebScan(project, runMode, checkExists, null);
    }

    /**
     * Creates a webscan job for project (but job is not approved, so will not be
     * started)
     *
     * @param project            the used test project
     * @param runMode            wanted mock mode. If <code>null</code> the default
     *                           {@link IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__ZERO_WAIT}
     *                           will be used
     * @param checkExists
     * @param customTemplateFile if <code>null</code> the default
     *                           (IntegrationTestTemplateFile#WEBSCAN_1) will be
     *                           used, otherwise the given one
     * @return uuid for created job
     */
    public UUID createWebScan(TestProject project, IntegrationTestMockMode runMode, boolean checkExists, IntegrationTestTemplateFile customTemplateFile) {
        if (checkExists) {
            assertProject(project).doesExist();
        }

        if (runMode == null) {
            runMode = IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__ZERO_WAIT;
        }
        String jsonResponse = createWebScanJob(project, runMode, customTemplateFile);

        return fetchJobUUID(jsonResponse);
    }

    /**
     * Creates a webscan for given target URL and returns job UUID. But be aware:
     * The target URL must be whitelisted before!
     *
     * @param project
     * @param targetURL
     * @return uuid
     */
    public UUID createWebScan(TestProject project, String targetURL) {
        String jsonResponse = createWebScanJobForTargetURL(project, targetURL, null);
        return fetchJobUUID(jsonResponse);
    }

    private UUID fetchJobUUID(String jsonResponse) {
        try {
            JsonNode jsonNode = JSONTestSupport.DEFAULT.fromJson(jsonResponse);
            JsonNode jobId = jsonNode.get("jobId");
            if (jobId == null) {
                fail("No jobID entry found in json:\n" + jsonResponse);
                return null;
            }
            return UUID.fromString(jobId.textValue());
        } catch (IllegalArgumentException e) {
            fail("Job did not return with a valid UUID!:" + jsonResponse);
            throw new IllegalStateException("fail not working");
        } catch (IOException e) {
            throw new IllegalStateException("io failure, should not occure", e);
        }
    }

    public File downloadAsTempFileFromURL(String url, UUID jobUUID, String fileName) {
        String prefix = "sechub-file-redownload-" + jobUUID.toString();
        return downloadAsTempFileFromURL(url, jobUUID, prefix, fileName);
    }

    public File downloadAsTempFileFromURL(String url, UUID jobUUID, String prefix, String fileEnding) {

        // Optional Accept header
        RequestCallback requestCallback = request -> request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        ResponseExtractor<File> responseExtractor = response -> {
            Path path = TestUtil.createTempFileInBuildFolder(prefix, fileEnding);
            Files.copy(response.getBody(), path, StandardCopyOption.REPLACE_EXISTING);
            if (TestUtil.isDeletingTempFiles()) {
                path.toFile().deleteOnExit();
            }
            return path.toFile();
        };
        RestTemplate template = getRestHelper().getTemplate();
        File x = template.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
        return x;
    }

    public String getServerVersion() {
        return getRestHelper().getStringFromURL(getUrlBuilder().buildGetServerRuntimeDataUrl());
    }

    public boolean getIsAlive() {
        getRestHelper().head(getUrlBuilder().buildCheckIsAliveUrl());
        return true;
    }

    public File downloadFullScanDataFor(UUID sechubJobUUID) {
        String url = getUrlBuilder().buildAdminDownloadsZipFileContainingFullScanDataFor(sechubJobUUID);
        File file = downloadAsTempFileFromURL(url, sechubJobUUID, "download-fullscan", ".zip");
        return file;
    }

    public AsUser grantSuperAdminRightsTo(TestUser targetUser) {
        String url = getUrlBuilder().buildAdminGrantsSuperAdminRightsTo(targetUser.getUserId());
        getRestHelper().post(url);
        return this;
    }

    public AsUser revokeSuperAdminRightsFrom(TestUser targetUser) {
        String url = getUrlBuilder().buildAdminRevokesSuperAdminRightsFrom(targetUser.getUserId());
        getRestHelper().post(url);
        return this;
    }

    public String getScanLogsForProject(TestProject project1) {
        String url = getUrlBuilder().buildAdminFetchesScanLogsForProject(project1.getProjectId());
        return getRestHelper().getJSON(url);
    }

    /**
     * Disbles job processing by scheduler.<br>
     * <br>
     * <b> WARNING:</b> You must ensure that your test will do a
     * <code>as(SUPER_ADMIN).enableSchedulerJobProcessing();</code> at the end of
     * your test (no matter if test fails somewhere in your test case), otherwise
     * you got a extreme side effect to your other integration tests...
     *
     * @return
     */
    public AsUser disableSchedulerJobProcessing() {
        String url = getUrlBuilder().buildAdminDisablesSchedulerJobProcessing();
        getRestHelper().post(url);
        return this;
    }

    public AsUser enableSchedulerJobProcessing() {
        String url = getUrlBuilder().buildAdminEnablesSchedulerJobProcessing();
        getRestHelper().post(url);
        return this;
    }

    public AsUser deleteProject(TestProject project) {
        String url = getUrlBuilder().buildAdminDeletesProject(project.getProjectId());
        getRestHelper().delete(url);
        return this;

    }

    public AsUser cancelJob(UUID jobUUID) {
        autoDumper.execute(() -> {
            String url = getUrlBuilder().buildAdminCancelsJob(jobUUID);
            getRestHelper().post(url);
        });
        return this;
    }

    public AsUser setProjectMockConfiguration(TestProject project, String json) {
        String url = getUrlBuilder().buildSetProjectMockConfiguration(project.getProjectId());
        getRestHelper().putJSON(url, json);
        return this;
    }

    public String getProjectMockConfiguration(TestProject project1) {
        String url = getUrlBuilder().buildGetProjectMockConfiguration(project1.getProjectId());
        return getRestHelper().getJSON(url);

    }

    public AsUser updateMapping(String mappingId, MappingData mappingData) {
        String url = getUrlBuilder().buildUpdateMapping(mappingId);
        getRestHelper().putJSON(url, mappingData.toJSON());
        return this;
    }

    public MappingData getMappingData(String mappingId) {
        String url = getUrlBuilder().buildGetMapping(mappingId);
        return MappingData.fromString(getRestHelper().getJSON(url));
    }

    public ProjectFalsePositivesDefinition getFalsePositiveConfigurationOfProject(TestProject project) {

        String url = getUrlBuilder().buildGetFalsePositiveConfigurationOfProject(project.getProjectId());
        String json = getRestHelper().getJSON(url);

        return create(project, json);

    }

    public AsUser restartJob(UUID jobUUID) {
        String url = getUrlBuilder().buildAdminRestartsJob(jobUUID);
        getRestHelper().post(url);
        return this;

    }

    public AsUser restartJobHard(UUID jobUUID) {
        String url = getUrlBuilder().buildAdminRestartsJobHard(jobUUID);
        getRestHelper().post(url);
        return this;

    }

    public ProjectFalsePositivesDefinition startFalsePositiveDefinition(TestProject project) {
        return new ProjectFalsePositivesDefinition(project);
    }

    public UUID triggerAsyncCodeScanGreenSuperFastWithPseudoZipUpload(TestProject project) {
        return triggerAsyncCodeScanApproveWithSourceUploadAndGetJobUUID(project, IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__10_MS_WATING,
                TestDataConstants.RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT);
    }

    public UUID triggerAsyncWebScanGreenLongRunningAndGetJobUUID(TestProject project) {
        UUID uuid = createWebScan(project, IntegrationTestMockMode.WEBSCAN__NETSPARKER_GREEN__10_SECONDS_WAITING);
        approveJob(project, uuid);
        return uuid;
    }

    public UUID triggerAsyncCodeScanWithPseudoZipUpload(TestProject project, IntegrationTestMockMode mode) {
        return triggerAsyncCodeScanApproveWithSourceUploadAndGetJobUUID(project, mode, TestDataConstants.RESOURCE_PATH_ZIPFILE_ONLY_TEST1_TXT);
    }

    public UUID triggerAsyncCodeScanApproveWithSourceUploadAndGetJobUUID(TestProject project, IntegrationTestMockMode mode, String pathInsideResources) {
        UUID uuid = triggerAsyncCodeScanAndGetJobUUID(project, mode);
        uploadSourcecode(project, uuid, pathInsideResources);

        approveJob(project, uuid);
        return uuid;
    }

    private UUID triggerAsyncCodeScanAndGetJobUUID(TestProject project, IntegrationTestMockMode runMode) {
        UUID uuid = createCodeScan(project, runMode);
        assertNotNull(uuid);
        return uuid;
    }

    /**
     * Change product executor job parameter by REST API - will fail when user has
     * not the permission to do this.
     *
     * @param executorConfigUUID
     * @param key
     * @param newValue
     * @throws IllegalArgumentException when executorConfigUUID is null
     * @throws IllegalStateException    when key was not found
     */
    public AsUser changeProductExecutorJobParameter(TestExecutorConfig executorConfig, String key, String newValue) {
        ensureExecutorConfigUUID(executorConfig);

        UUID executorConfigUUID = executorConfig.uuid;
        if (executorConfigUUID == null) {
            throw new IllegalArgumentException("Invalid test case: executorConfigUUID may not be null! Name was:" + executorConfig.name);
        }
        TestExecutorConfig config = fetchProductExecutorConfig(executorConfigUUID);
        boolean changed = false;
        for (TestExecutorSetupJobParam param : config.setup.jobParameters) {
            if (param.key.equals(key)) {
                param.value = newValue;
                changed = true;
            }
        }
        if (changed) {
            updateProdcutExecutorConfig(executorConfigUUID, config);
        } else {
            throw new IllegalStateException("Invalid test case situation: key:" + key + " was not found!");
        }
        return this;

    }

    ProjectFalsePositivesDefinition create(TestProject project, String json) {
        ProjectFalsePositivesDefinition def = new ProjectFalsePositivesDefinition(project);

        try {
            JsonNode jsonNode = jsonTestSupport.fromJson(json);
            ArrayNode falsePositives = (ArrayNode) jsonNode.get("falsePositives");
            if (falsePositives == null) {
                fail("No false positives found in json:" + json);
            }
            for (JsonNode falsePositive : falsePositives) {
                JsonNode jobData = falsePositive.get("jobData");

                String jobUUID = jobData.get("jobUUID").asText();
                int findingId = jobData.get("findingId").asInt();

                JsonNode commentNode = jobData.get("comment");
                String comment = null;
                if (commentNode != null) {
                    comment = commentNode.asText();
                }
                def.add(findingId, UUID.fromString(jobUUID), comment);

            }
        } catch (IOException e) {
            throw new IllegalStateException("JSON not valid", e);
        }
        return def;
    }

    public class ProjectFalsePositivesDefinition {

        private TestProject project;

        private class JobData {
            private int findingId;
            private UUID jobUUID;
            private String comment;
        }

        private List<JobData> jobData = new ArrayList<>();
        private WithSecHubClient withSechubClient;
        private IntegrationTestJSONLocation location;

        public ProjectFalsePositivesDefinition(TestProject project) {
            this(project, null, null);
        }

        public ProjectFalsePositivesDefinition(TestProject project, WithSecHubClient withSechubClient, IntegrationTestJSONLocation location) {
            this.project = project;
            this.withSechubClient = withSechubClient;
            this.location = location;
        }

        public boolean isContaining(int findingId, UUID jobUUID) {
            JobData found = findJobData(findingId, jobUUID);
            if (found == null) {
                return false;
            }
            return true;
        }

        public JobData findJobData(int findingId, UUID jobUUID) {
            for (JobData d : jobData) {
                if (d.findingId != findingId) {
                    continue;
                }
                if (!(d.jobUUID.equals(jobUUID))) {
                    continue;
                }
                return d;
            }
            return null;
        }

        public void markAsFalsePositive() {
            if (withSechubClient == null) {
                markAsFalsePositiveByREST();
            } else {
                markFalsePositiveBySecHubClient();
            }
        }

        private void markFalsePositiveBySecHubClient() {
            String json = buildJSON();

            IntegrationTestFileSupport testfileSupport = IntegrationTestFileSupport.getTestfileSupport();
            File file = testfileSupport.createTempFile("mark_as_false_positive", ".json");
            testfileSupport.writeTextFile(file, json);

            withSechubClient.markAsFalsePositive(project, location, file.getAbsolutePath());

        }

        private void markAsFalsePositiveByREST() {
            String json = buildJSON();

            String url = getUrlBuilder().buildUserAddsFalsePositiveDataListForProject(project.getProjectId());
            getRestHelper().putJSON(url, json);
        }

        public void unmarkFalsePositive() {
            if (withSechubClient == null) {
                unmarkFalsePositiveByREST();
            } else {
                unmarkFalsePositiveBySecHubClient();
            }
        }

        private void unmarkFalsePositiveBySecHubClient() {
            String json = buildJSON();
            IntegrationTestFileSupport testfileSupport = IntegrationTestFileSupport.getTestfileSupport();
            File file = testfileSupport.createTempFile("unmark_false_positive", ".json");
            testfileSupport.writeTextFile(file, json);

            withSechubClient.unmarkAsFalsePositive(project, location, file.getAbsolutePath());
        }

        private void unmarkFalsePositiveByREST() {
            Iterator<JobData> it = jobData.iterator();
            while (it.hasNext()) {
                JobData data = it.next();
                String url = getUrlBuilder().buildUserRemovesFalsePositiveEntryFromProject(project.getProjectId(), "" + data.jobUUID, "" + data.findingId);
                getRestHelper().delete(url);
            }

        }

        private String buildJSON() {
            String content = "{\"apiVersion\":\"1.0\",\"type\":\"falsePositiveJobDataList\",\"jobData\":[";
            Iterator<JobData> it = jobData.iterator();
            while (it.hasNext()) {
                JobData data = it.next();
                /*
                 * handle strange failure output of github actions build (happens occasionally)
                 */
                if (data == null) {
                    String json = JSONConverter.get().toJSON(jobData);
                    LOG.error("Job data list contains a job data object being null, see JSON:\n{}", json);
                    fail("job data list contains entry being null!");
                }
                if (data.jobUUID == null) {
                    String json = JSONConverter.get().toJSON(jobData);
                    LOG.error("Job data list contains a job data object having no job uuid, see JSON:\n{}", json);
                    fail("Job data list contains at least one job data without a jobuuid! see logs for output, finding id is :" + data.findingId);
                }
                if (data.comment == null) {
                    content += "{\"jobUUID\":\"" + data.jobUUID.toString() + "\",\"findingId\":" + data.findingId + "}";
                } else {
                    content += "{\"jobUUID\":\"" + data.jobUUID.toString() + "\",\"findingId\":" + data.findingId + ",\"comment\":\"" + data.comment + "\"}";
                }
                if (it.hasNext()) {
                    content += ",";
                }
            }
            content += "]}";
            return content;
        }

        public ProjectFalsePositivesDefinition add(int findingId, UUID jobUUID) {
            return add(findingId, jobUUID, null);
        }

        public ProjectFalsePositivesDefinition add(int findingId, UUID jobUUID, String comment) {

            JobData data = new JobData();
            data.findingId = findingId;
            data.jobUUID = jobUUID;
            data.comment = comment;
            jobData.add(data);
            return this;
        }
    }

    public void changeProjectAccessLevel(TestProject project, ProjectAccessLevel accessLevel) {
        String url = getUrlBuilder().buildAdminChangesProjectAccessLevelUrl(project.getProjectId(), accessLevel.getId());

        getRestHelper().post(url);
    }

    public TestUserDetailInformation fetchUserDetails(TestUser user) {
        String url = getUrlBuilder().buildAdminShowsUserDetailsUrl(user.getUserId());
        String json = getRestHelper().getJSON(url);
        return TestJSONHelper.get().createFromJSON(json, TestUserDetailInformation.class);
    }

    public void changeEmailAddress(TestUser user, String newEmailAddress) {
        changeEmailAddress(user.getUserId(), newEmailAddress);
    }

    public void changeEmailAddress(String userId, String newEmailAddress) {
        String url = getUrlBuilder().buildAdminChangesUserEmailAddress(userId, newEmailAddress);
        getRestHelper().put(url);
    }

    public void updateAutoCleanupConfiguration(TestAutoCleanupData data) {
        String json = TestJSONHelper.get().createJSON(data);
        updateAutoCleanupConfiguration(json);
    }

    public void updateAutoCleanupConfiguration(String json) {
        String url = getUrlBuilder().buildAdminUpdatesAutoCleanupConfigurationUrl();
        getRestHelper().putJSON(url, json);
    }

    public TestAutoCleanupData fetchAutoCleanupConfiguration() {
        String url = getUrlBuilder().buildAdminFetchesAutoCleanupConfigurationUrl();

        String json = getRestHelper().getJSON(url);
        return TestJSONHelper.get().createFromJSON(json, TestAutoCleanupData.class);
    }

    public boolean getBooleanFromURL(String url) {
        String result = getStringFromURL(url);
        if (result != null) {
            String lowerCased = result.toLowerCase();
            if (lowerCased.equals("true")) {
                return true;
            }
            if (lowerCased.equals("false")) {
                return false;
            }
        }
        throw new IllegalStateException("Cannot fetch boolean result from url:" + url + " - was not a boolean but " + result);
    }

    /**
     * Fetches user job info list without using a limit parameter. The default
     * without limit is one, so we get only ONE entry or none (if no job has been
     * started). If the REST call would return more than one entry, this method will
     * fail, because it would be not the expected and documented behavior!
     *
     * @param project
     * @return info or <code>null</code>, if no job available at all
     */
    public TestSecHubJobInfoForUserListPage fetchUserJobInfoListOneEntryOrNull(TestProject project) {
        TestSecHubJobInfoForUserListPage listPage = fetchUserJobInfoList(project, null, null, null, null);
        if (listPage.getContent().isEmpty()) {
            return null;
        }
        assertEquals("Without parameter job list may container either 0 or 1 entries", 1, listPage.getContent().size());
        return listPage;
    }

    public TestSecHubJobInfoForUserListPage fetchUserJobInfoList(TestProject project, int size) {
        return fetchUserJobInfoList(project, String.valueOf(size), null, null, null);
    }

    public TestSecHubJobInfoForUserListPage fetchUserJobInfoList(TestProject project, int size, int page) {
        return fetchUserJobInfoList(project, String.valueOf(size), String.valueOf(page), null, null);
    }

    public TestSecHubJobInfoForUserListPage fetchUserJobInfoList(TestProject project, int size, int page, boolean withMetaData) {
        return fetchUserJobInfoList(project, String.valueOf(size), String.valueOf(page), String.valueOf(withMetaData), null);
    }

    public TestSecHubJobInfoForUserListPage fetchUserJobInfoList(TestProject project, int size, int page, boolean withMetaData,
            Map<String, String> additionalParameters) {
        return fetchUserJobInfoList(project, String.valueOf(size), String.valueOf(page), String.valueOf(withMetaData), additionalParameters);
    }

    private TestSecHubJobInfoForUserListPage fetchUserJobInfoList(TestProject project, String size, String page, String withMetaData,
            Map<String, String> additionalParametersOrNull) {

        String url = getUrlBuilder().buildUserFetchesListOfJobsForProject(project.getProjectId(), size, page, withMetaData, additionalParametersOrNull);
        String json = getRestHelper().getJSON(url);

        TestSecHubJobInfoForUserListPage listPage = TestJSONHelper.get().createFromJSON(json, TestSecHubJobInfoForUserListPage.class);
        return listPage;

    }

    /**
     * Tries to create a SecHub job by given configuration string.
     *
     * @param project
     * @param sechubConfigAsString
     * @return result as string
     */
    public String tryToCreateJobByJson(TestProject project, String sechubConfigAsString) {

        String url = getUrlBuilder().buildAddJobUrl(project.getProjectId());
        return getRestHelper().postJson(url, sechubConfigAsString);

    }

}

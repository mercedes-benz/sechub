// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanApiConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.systemtest.config.CredentialsDefinition;
import com.mercedesbenz.sechub.systemtest.config.DefaultFallback;
import com.mercedesbenz.sechub.systemtest.config.LocalSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;
import com.mercedesbenz.sechub.systemtest.config.RemoteSetupDefinition;
import com.mercedesbenz.sechub.systemtest.config.RunSecHubJobDefinition;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.config.UploadDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.init.SystemTestRuntimePreparator;
import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;
import com.mercedesbenz.sechub.test.TestUtil;

class SystemTestRuntimePreparatorTest {

    private LocationSupport locationSupport;
    private SystemTestRuntimePreparator preparatorToTest;
    private EnvironmentProvider environmentProvider;
    private SystemTestRuntimeContext context;

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimePreparatorTest.class);

    @BeforeEach
    void beforeEach() throws IOException {
        Path aTemporaryFolder = TestUtil.createTempDirectoryInBuildFolder("systemtest_prep_testfolder");

        locationSupport = mock(LocationSupport.class);
        when(locationSupport.getPDSSolutionRoot()).thenReturn(aTemporaryFolder);

        environmentProvider = mock(EnvironmentProvider.class);

        context = new SystemTestRuntimeContext();
        context.environmentProvider = environmentProvider;
        context.locationSupport = locationSupport;
        context.workspaceRoot = aTemporaryFolder;
        context.additionalResourcesRoot = aTemporaryFolder;

        preparatorToTest = new SystemTestRuntimePreparator();

    }

    @Test
    void variable_handling_in_a_remote_config_even_comments_can_have_variables() {
        /* prepare */
        SystemTestConfiguration originConfiguration = new SystemTestConfiguration();
        originConfiguration.getVariables().put("var1", "value1");
        originConfiguration.getSetup()
                .setComment("This is a comment - even this is replaceable - because we just change the complete JSON... var1=${variables.var1}");

        context.originConfiguration = originConfiguration;

        /* execute */
        preparatorToTest.prepare(context);

        /* test */
        assertEquals("This is a comment - even this is replaceable - because we just change the complete JSON... var1=value1",
                context.getConfiguration().getSetup().getComment());

    }

    @Test
    void local_run_default_preparation_sechub_admin_has_expected_default_credentials() {
        /* prepare */
        SystemTestConfiguration originConfiguration = new SystemTestConfiguration();
        LocalSetupDefinition localSetup = new LocalSetupDefinition();
        originConfiguration.getSetup().setLocal(Optional.of(localSetup));

        context.originConfiguration = originConfiguration;

        // setup context - local/remote run. important for preparator!
        context.localRun = true;

        LOG.debug("originConfiguration for local run: {} \n{}", JSONConverter.get().toJSON(originConfiguration, true));

        /* execute */
        preparatorToTest.prepare(context);

        /* test */
        SystemTestConfiguration preparedConfig = context.getConfiguration();
        LOG.debug("preparedConfig: \n{}", JSONConverter.get().toJSON(preparedConfig, true));

        // check credential defaults defined
        Optional<LocalSetupDefinition> localOpt2 = preparedConfig.getSetup().getLocal();
        assertTrue(localOpt2.isPresent());
        LocalSetupDefinition localSetup2 = localOpt2.get();
        CredentialsDefinition sechubAdmin2 = localSetup2.getSecHub().getAdmin();
        assertEquals("admin", sechubAdmin2.getUserId());
        assertEquals("myTop$ecret!", sechubAdmin2.getApiToken());

    }

    @Test
    void local_run_default_preparation_pds_techuser_has_expected_default_credentials() {
        /* prepare */
        SystemTestConfiguration originConfiguration = new SystemTestConfiguration();
        LocalSetupDefinition localSetup = new LocalSetupDefinition();
        originConfiguration.getSetup().setLocal(Optional.of(localSetup));

        PDSSolutionDefinition solution1 = new PDSSolutionDefinition();
        solution1.setName("solution1");
        solution1.setBaseDirectory("./src/test/resources/fake-root/sechub-pds-solutions/faked-gosec");
        localSetup.getPdsSolutions().add(solution1);

        context.originConfiguration = originConfiguration;

        // setup context - local/remote run. important for preparator!
        context.localRun = true;

        LOG.debug("originConfiguration for local run: {} \n{}", JSONConverter.get().toJSON(originConfiguration, true));

        /* execute */
        preparatorToTest.prepare(context);

        /* test */
        SystemTestConfiguration preparedConfig = context.getConfiguration();
        LOG.debug("preparedConfig: \n{}", JSONConverter.get().toJSON(preparedConfig, true));

        // check credential defaults defined
        Optional<LocalSetupDefinition> localOpt2 = preparedConfig.getSetup().getLocal();
        assertTrue(localOpt2.isPresent());
        LocalSetupDefinition localSetup2 = localOpt2.get();
        PDSSolutionDefinition solution2 = localSetup2.getPdsSolutions().iterator().next();
        CredentialsDefinition techUser = solution2.getTechUser();
        assertEquals("techuser", techUser.getUserId());
        assertEquals("pds-apitoken", techUser.getApiToken());

    }

    @Test
    void local_run_default_preparation_pds_adminuser_has_expected_default_credentials() {
        /* prepare */
        SystemTestConfiguration originConfiguration = new SystemTestConfiguration();
        LocalSetupDefinition localSetup = new LocalSetupDefinition();
        originConfiguration.getSetup().setLocal(Optional.of(localSetup));

        PDSSolutionDefinition solution1 = new PDSSolutionDefinition();
        solution1.setName("solution1");
        solution1.setBaseDirectory("./src/test/resources/fake-root/sechub-pds-solutions/faked-gosec");
        localSetup.getPdsSolutions().add(solution1);

        context.originConfiguration = originConfiguration;

        // setup context - local/remote run. important for preparator!
        context.localRun = true;

        LOG.debug("originConfiguration for local run: {} \n{}", JSONConverter.get().toJSON(originConfiguration, true));

        /* execute */
        preparatorToTest.prepare(context);

        /* test */
        SystemTestConfiguration preparedConfig = context.getConfiguration();
        LOG.debug("preparedConfig: \n{}", JSONConverter.get().toJSON(preparedConfig, true));

        // check credential defaults defined
        Optional<LocalSetupDefinition> localOpt2 = preparedConfig.getSetup().getLocal();
        assertTrue(localOpt2.isPresent());
        LocalSetupDefinition localSetup2 = localOpt2.get();
        PDSSolutionDefinition solution2 = localSetup2.getPdsSolutions().iterator().next();
        CredentialsDefinition techUser = solution2.getAdmin();
        assertEquals("admin", techUser.getUserId());
        assertEquals("pds-apitoken", techUser.getApiToken());

    }

    @ParameterizedTest
    @EnumSource(PreparationTestData.class)
    void default_preparation_as_expected(PreparationTestData data) {
        /* prepare */
        SystemTestConfiguration originConfiguration = new SystemTestConfiguration();
        if (data.scope.local) {
            LocalSetupDefinition localSetup = new LocalSetupDefinition();
            originConfiguration.getSetup().setLocal(Optional.of(localSetup));
        } else {
            RemoteSetupDefinition remoteSetup = new RemoteSetupDefinition();
            originConfiguration.getSetup().setRemote(Optional.of(remoteSetup));
        }

        RunSecHubJobDefinition secHubJob = new RunSecHubJobDefinition();
        prepareUpload(data, secHubJob);
        prepareScanConfigurationsForScanTypes(data, secHubJob);
        prepareProject(data, secHubJob);

        TestDefinition testdefinition = new TestDefinition();
        testdefinition.getExecute().setRunSecHubJob(Optional.of(secHubJob));

        originConfiguration.getTests().add(testdefinition);

        context.originConfiguration = originConfiguration;

        // setup context - local/remote run. important for preparator!
        context.localRun = data.scope.local;

        LOG.debug("originConfiguration for {} - local run: {} \n{}", data.name(), context.isLocalRun(), JSONConverter.get().toJSON(originConfiguration, true));

        /* execute */
        preparatorToTest.prepare(context);

        /* test */
        SystemTestConfiguration preparedConfig = context.getConfiguration();
        LOG.debug("preparedConfig for {}:\n{}", data.name(), JSONConverter.get().toJSON(preparedConfig, true));

        TestDefinition test1 = preparedConfig.getTests().iterator().next();
        RunSecHubJobDefinition job = test1.getExecute().getRunSecHubJob().get();

        String expectedReferenceId = data.scope.refId;
        if (expectedReferenceId == null) {
            /* not defined means the default fallback must be injected by preparator */
            expectedReferenceId = DefaultFallback.FALLBACK_UPLOAD_REF_ID.getValue();
        }
        assertUploadRefUsesReferenceId(job, expectedReferenceId);
        assertJobScansUseReferenceId(job, expectedReferenceId);

        String expectedProjectName = data.scope.getDefinedProjectName();
        if (expectedProjectName == null) {
            /* not defined means the default fallback must be injected by preparator */
            /* we use the ref id here also for defined project names */
            expectedProjectName = DefaultFallback.FALLBACK_PROJECT_NAME.getValue();
        }
        assertProjectName(job, expectedProjectName);

    }

    private void prepareProject(PreparationTestData data, RunSecHubJobDefinition secHubJob) {
        if (!data.scope.hasDedicatedProjectName()) {
            return;
        }
        secHubJob.setProject(data.scope.getDefinedProjectName());

    }

    private void assertProjectName(RunSecHubJobDefinition job, String expectedProjectName) {
        assertEquals(expectedProjectName, job.getProject());
    }

    private void assertUploadRefUsesReferenceId(RunSecHubJobDefinition job, String expectedReferenceId) {
        UploadDefinition upload1 = job.getUploads().iterator().next();
        if (upload1.getReferenceId().isEmpty()) {
            fail("upload reference id not set!");
        }
        String refId = upload1.getReferenceId().get();
        assertEquals(expectedReferenceId, refId);

    }

    private void assertJobScansUseReferenceId(RunSecHubJobDefinition job, String expectedReferenceId) {
        if (job.getCodeScan().isPresent()) {
            assertContainsReference(job.getCodeScan().get().getNamesOfUsedDataConfigurationObjects(), expectedReferenceId);
        }
        if (job.getSecretScan().isPresent()) {
            assertContainsReference(job.getSecretScan().get().getNamesOfUsedDataConfigurationObjects(), expectedReferenceId);
        }
        if (job.getLicenseScan().isPresent()) {
            assertContainsReference(job.getLicenseScan().get().getNamesOfUsedDataConfigurationObjects(), expectedReferenceId);
        }
        if (job.getWebScan().isPresent() && job.getWebScan().get().getApi().isPresent()) {
            assertContainsReference(job.getWebScan().get().getApi().get().getNamesOfUsedDataConfigurationObjects(), expectedReferenceId);
        }
    }

    private void assertContainsReference(Set<String> list, String exptectedReferenceId) {
        if (list.contains(exptectedReferenceId)) {
            return;
        }
        assertEquals(Arrays.asList(exptectedReferenceId), list, "Reference id is not contained as expected");

    }

    private void prepareScanConfigurationsForScanTypes(PreparationTestData data, RunSecHubJobDefinition secHubJob) {
        if (data.scope.isTypeContained(ScanType.CODE_SCAN)) {
            SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
            prepareRefidIfDefined(data, codeScan);
            secHubJob.setCodeScan(Optional.of(codeScan));
        }
        if (data.scope.isTypeContained(ScanType.SECRET_SCAN)) {
            SecHubSecretScanConfiguration secretScan = new SecHubSecretScanConfiguration();
            prepareRefidIfDefined(data, secretScan);
            secHubJob.setSecretScan(Optional.of(secretScan));
        }
        if (data.scope.isTypeContained(ScanType.LICENSE_SCAN)) {
            SecHubLicenseScanConfiguration licenseScan = new SecHubLicenseScanConfiguration();
            prepareRefidIfDefined(data, licenseScan);
            secHubJob.setLicenseScan(Optional.of(licenseScan));
        }
        if (data.scope.isTypeContained(ScanType.WEB_SCAN)) {
            SecHubWebScanConfiguration webScan = new SecHubWebScanConfiguration();
            SecHubWebScanApiConfiguration apiConfig = new SecHubWebScanApiConfiguration();
            webScan.setApi(Optional.of(apiConfig));
            prepareRefidIfDefined(data, apiConfig);
            secHubJob.setWebScan(Optional.of(webScan));
        }
        if (data.scope.isTypeContained(ScanType.INFRA_SCAN)) {
            SecHubInfrastructureScanConfiguration infraScan = new SecHubInfrastructureScanConfiguration();
            /* we cannot set a reference for infra scans */
            secHubJob.setInfraScan(Optional.of(infraScan));
        }
    }

    private void prepareRefidIfDefined(PreparationTestData data, SecHubDataConfigurationUsageByName target) {
        if (data.scope.refId == null) {
            return;
        }
        target.getNamesOfUsedDataConfigurationObjects().add(data.scope.refId);
    }

    private void prepareUpload(PreparationTestData data, RunSecHubJobDefinition secHubJob) {
        UploadDefinition upload1 = new UploadDefinition();
        if (data.scope.sourceUpload) {
            upload1.setSourceFolder(Optional.of("src-folder1"));
        }
        if (data.scope.binaryUpload) {
            upload1.setBinariesFolder(Optional.of("bin-folder1"));
        }
        if (data.scope.refId != null) {
            upload1.setReferenceId(Optional.of(data.scope.refId));
        }
        secHubJob.getUploads().add(upload1);
    }

    private record TestScope(boolean local, String refId, boolean sourceUpload, boolean binaryUpload, ScanType... types) {

        private boolean isTypeContained(ScanType typeToSearch) {
            for (ScanType currentType : types) {
                if (currentType.equals(typeToSearch)) {
                    return true;
                }
            }
            return false;
        }

        private boolean hasDedicatedProjectName() {
            // we just reuse refid for simplicity
            return refId != null;
        }

        private String getDefinedProjectName() {
            // we just reuse refid for simplicity
            return refId;
        }
    }

    private static final boolean LOCAL = false;
    private static final boolean REMOTE = false;

    private static final boolean SRC_UPLOAD = true;
    private static final boolean NO_SRC_UPLOAD = false;

    private static final boolean BIN_UPLOAD = true;
    private static final boolean NO_BIN_UPLOAD = false;

    private static final String DEFINED_REF_ID = "defined-ref-id";

    private enum PreparationTestData {

        /*
         * next data represents upload + scan have NO dedicated reference id, means
         * default reference id will be set
         */
        LOCAL_BINYARY_UPLOAD__CODE_SCAN(new TestScope(LOCAL, null, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.CODE_SCAN)),
        LOCAL_BINYARY_UPLOAD__LICENSE_SCAN(new TestScope(LOCAL, null, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.LICENSE_SCAN)),
        LOCAL_BINYARY_UPLOAD__WEB_SCAN(new TestScope(LOCAL, null, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.WEB_SCAN)),
        LOCAL_BINYARY_UPLOAD__INFRA_SCAN(new TestScope(LOCAL, null, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.INFRA_SCAN)),

        LOCAL_SOURCE_UPLOAD__CODE_SCAN(new TestScope(LOCAL, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.CODE_SCAN)),
        LOCAL_SOURCE_UPLOAD__LICENSE_SCAN(new TestScope(LOCAL, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.LICENSE_SCAN)),
        LOCAL_SOURCE_UPLOAD__WEB_SCAN(new TestScope(LOCAL, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.WEB_SCAN)),
        LOCAL_SOURCE_UPLOAD__INFRA_SCAN(new TestScope(LOCAL, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.INFRA_SCAN)),
        LOCAL_SOURCE_UPLOAD__SECRET_SCAN(new TestScope(LOCAL, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.SECRET_SCAN)),

        REMOTE_BINYARY_UPLOAD__CODE_SCAN(new TestScope(REMOTE, null, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.CODE_SCAN)),
        REMOTE_BINYARY_UPLOAD__LICENSE_SCAN(new TestScope(REMOTE, null, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.LICENSE_SCAN)),
        REMOTE_BINYARY_UPLOAD__WEB_SCAN(new TestScope(REMOTE, null, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.WEB_SCAN)),
        REMOTE_BINYARY_UPLOAD__INFRA_SCAN(new TestScope(REMOTE, null, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.INFRA_SCAN)),

        REMOTE_SOURCE_UPLOAD__CODE_SCAN(new TestScope(REMOTE, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.CODE_SCAN)),
        REMOTE_SOURCE_UPLOAD__LICENSE_SCAN(new TestScope(REMOTE, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.LICENSE_SCAN)),
        REMOTE_SOURCE_UPLOAD__WEB_SCAN(new TestScope(REMOTE, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.WEB_SCAN)),
        REMOTE_SOURCE_UPLOAD__INFRA_SCAN(new TestScope(REMOTE, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.INFRA_SCAN)),
        REMOTE_SOURCE_UPLOAD__SECRET_SCAN(new TestScope(REMOTE, null, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.SECRET_SCAN)),

        /*
         * next data represents upload + scan have a dedicated reference id, which is
         * not changed
         */
        DEFINED_REF_ID_LOCAL_BINYARY_UPLOAD__CODE_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.CODE_SCAN)),
        DEFINED_REF_ID_LOCAL_BINYARY_UPLOAD__LICENSE_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.LICENSE_SCAN)),
        DEFINED_REF_ID_LOCAL_BINYARY_UPLOAD__WEB_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.WEB_SCAN)),
        DEFINED_REF_ID_LOCAL_BINYARY_UPLOAD__INFRA_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.INFRA_SCAN)),

        DEFINED_REF_ID_LOCAL_SOURCE_UPLOAD__CODE_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.CODE_SCAN)),
        DEFINED_REF_ID_LOCAL_SOURCE_UPLOAD__LICENSE_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.LICENSE_SCAN)),
        DEFINED_REF_ID_LOCAL_SOURCE_UPLOAD__WEB_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.WEB_SCAN)),
        DEFINED_REF_ID_LOCAL_SOURCE_UPLOAD__INFRA_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.INFRA_SCAN)),
        DEFINED_REF_ID_LOCAL_SOURCE_UPLOAD__SECRET_SCAN(new TestScope(LOCAL, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.SECRET_SCAN)),

        DEFINED_REF_ID_REMOTE_BINYARY_UPLOAD__CODE_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.CODE_SCAN)),
        DEFINED_REF_ID_REMOTE_BINYARY_UPLOAD__LICENSE_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.LICENSE_SCAN)),
        DEFINED_REF_ID_REMOTE_BINYARY_UPLOAD__WEB_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.WEB_SCAN)),
        DEFINED_REF_ID_REMOTE_BINYARY_UPLOAD__INFRA_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, NO_SRC_UPLOAD, BIN_UPLOAD, ScanType.INFRA_SCAN)),

        DEFINED_REF_ID_REMOTE_SOURCE_UPLOAD__CODE_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.CODE_SCAN)),
        DEFINED_REF_ID_REMOTE_SOURCE_UPLOAD__LICENSE_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.LICENSE_SCAN)),
        DEFINED_REF_ID_REMOTE_SOURCE_UPLOAD__WEB_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.WEB_SCAN)),
        DEFINED_REF_ID_REMOTE_SOURCE_UPLOAD__INFRA_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.INFRA_SCAN)),
        DEFINED_REF_ID_REMOTE_SOURCE_UPLOAD__SECRET_SCAN(new TestScope(REMOTE, DEFINED_REF_ID, SRC_UPLOAD, NO_BIN_UPLOAD, ScanType.SECRET_SCAN)),

        ;

        private TestScope scope;

        private PreparationTestData(TestScope scope) {
            this.scope = scope;
        }

    }

}

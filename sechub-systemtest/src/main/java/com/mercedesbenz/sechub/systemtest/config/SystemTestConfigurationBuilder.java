package com.mercedesbenz.sechub.systemtest.config;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationUsageByName;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.systemtest.runtime.testengine.TestTemplateSupport;

public class SystemTestConfigurationBuilder {

    private SystemTestConfiguration configuration;

    SystemTestConfigurationBuilder() {
        this.configuration = new SystemTestConfiguration();
    }

    public class StepBuilder<T> extends AbstractDefinitionBuilder<StepBuilder<T>> {
        private T parent;
        private ExecutionStepDefinition stepDefinition;

        public StepBuilder(T parent, List<ExecutionStepDefinition> targetList) {
            this.parent = parent;
            this.stepDefinition = new ExecutionStepDefinition();
            targetList.add(stepDefinition);
        }

        public StepBuilder<T> comment(String id) {
            this.stepDefinition.setComment(id);
            return this;
        }

        @Override
        protected AbstractDefinition resolveDefinition() {
            return stepDefinition;
        }

        public ScriptBuilder script() {
            return new ScriptBuilder();
        }

        public T endStep() {
            return parent;
        }

        public class ScriptBuilder extends AbstractDefinitionBuilder<ScriptBuilder> {

            private ScriptDefinition scriptCallDefinition;

            private ScriptBuilder() {
                scriptCallDefinition = new ScriptDefinition();
                StepBuilder.this.stepDefinition.setScript(Optional.of(scriptCallDefinition));
            }

            public ScriptBuilder path(String path) {
                scriptCallDefinition.setPath(path);
                return this;
            }

            public ScriptBuilder arguments(String... args) {
                scriptCallDefinition.setArguments(Arrays.asList(args));
                return this;
            }

            public StepBuilder<T> endScript() {
                return StepBuilder.this;
            }

            public ScriptBuilder envVariable(String variableName, String value) {
                scriptCallDefinition.getEnvVariables().put(variableName, value);
                return this;
            }

            public ScriptBuilder workingDir(String workingDirectory) {
                scriptCallDefinition.setWorkingDir(workingDirectory);
                return this;
            }

            public ProcessDefinitionBuilder process() {
                return new ProcessDefinitionBuilder(this);
            }

            @Override
            protected AbstractDefinition resolveDefinition() {
                return scriptCallDefinition;
            }
        }

        public class ProcessDefinitionBuilder extends AbstractDefinitionBuilder<ProcessDefinitionBuilder> {

            private StepBuilder<T>.ScriptBuilder scriptBuilder;
            private ProcessDefinition process;

            public ProcessDefinitionBuilder(StepBuilder<T>.ScriptBuilder scriptBuilder) {
                this.scriptBuilder = scriptBuilder;
                this.process = scriptBuilder.scriptCallDefinition.getProcess();
            }

            /**
             * Use this method to mark that the current stage will wait for this process to
             * end. Until the process has not ended the switch to next stage is blocked.
             *
             * @return builder
             */
            public ProcessDefinitionBuilder markStageWaits() {
                process.setStageWaits(true);
                return this;
            }

            public ProcessDefinitionBuilder withTimeOut(int amount, TimeUnit unit) {
                TimeUnitDefinition unitDef = new TimeUnitDefinition(amount, unit);
                process.setTimeOut(unitDef);
                return this;
            }

            public ScriptBuilder endProcess() {
                return scriptBuilder;
            }

            @Override
            protected AbstractDefinition resolveDefinition() {
                return process;
            }

        }

    }

    /**
     * Abstract sechub defintion builder
     *
     * @author Albert Tregnaghi
     *
     * @param <T> Reference for constructor and inheritance... (caller class with
     *            generics)
     * @param <D> the SecHub definition
     * @param <X> Plain result target (caller class without any generics)
     */
    private abstract class AbstractSecHubDefinitionBuilder<T extends AbstractSecHubDefinitionBuilder<?, ?, ?>, D extends AbstractSecHubDefinition, X>
            extends AbstractDefinitionBuilder<X> {
        private D sechubDefinition;

        AbstractSecHubDefinitionBuilder(Class<T> clazz, D sechubDefinition) {
            this.sechubDefinition = sechubDefinition;
        }

        protected D getSechubDefinition() {
            return sechubDefinition;
        }

        @SuppressWarnings("unchecked")
        public X url(URL url) {
            sechubDefinition.setUrl(url == null ? null : url.toExternalForm());
            return (X) this;
        }

        @Override
        protected final AbstractDefinition resolveDefinition() {
            return sechubDefinition;
        }

    }

    public class RemoteSetupBuilder extends AbstractDefinitionBuilder<RemoteSetupBuilder> {
        private RemoteSetupDefinition remoteSetup;

        private RemoteSetupBuilder() {
            remoteSetup = new RemoteSetupDefinition();
            configuration.getSetup().setRemote(Optional.of(remoteSetup));
        }

        public SecHubSetupBuilder secHub() {
            return new SecHubSetupBuilder();
        }

        public class SecHubSetupBuilder extends AbstractSecHubDefinitionBuilder<SecHubSetupBuilder, RemoteSecHubDefinition, SecHubSetupBuilder> {

            public SecHubSetupBuilder() {
                super(SecHubSetupBuilder.class, remoteSetup.getSecHub());
            }

            public SecHubSetupBuilder user(String userId, String apiToken) {
                CredentialsDefinition userCredentials = getSechubDefinition().getUser();

                userCredentials.setUserId(userId);
                userCredentials.setApiToken(apiToken);
                return this;
            }

            public RemoteSetupBuilder endSecHub() {
                return RemoteSetupBuilder.this;
            }

        }

        public SystemTestConfigurationBuilder endRemoteSetup() {
            return SystemTestConfigurationBuilder.this;
        }

        @Override
        protected AbstractDefinition resolveDefinition() {
            return remoteSetup;
        }

    }

    public class LocalSetupBuilder extends AbstractDefinitionBuilder<LocalSetupBuilder> {

        private LocalSetupDefinition localSetup;

        private LocalSetupBuilder() {
            localSetup = new LocalSetupDefinition();
            configuration.getSetup().setLocal(Optional.of(localSetup));
        }

        public SolutionSetupBuilder addSolution(String id) {
            return new SolutionSetupBuilder(id);
        }

        public SystemTestConfigurationBuilder endLocalSetup() {
            return SystemTestConfigurationBuilder.this;
        }

        public SecHubSetupBuilder secHub() {
            return new SecHubSetupBuilder();
        }

        @Override
        protected AbstractDefinition resolveDefinition() {
            return localSetup;
        }

        public class SecHubSetupBuilder extends AbstractSecHubDefinitionBuilder<SecHubSetupBuilder, LocalSecHubDefinition, SecHubSetupBuilder> {

            public SecHubSetupBuilder() {
                super(SecHubSetupBuilder.class, localSetup.getSecHub());
            }

            public LocalSetupBuilder endSecHub() {
                return LocalSetupBuilder.this;
            }

            public StepBuilder<SecHubSetupBuilder> addStartStep() {
                return new StepBuilder<SecHubSetupBuilder>(this, getSechubDefinition().getStart());
            }

            public StepBuilder<SecHubSetupBuilder> addStopStep() {
                return new StepBuilder<SecHubSetupBuilder>(this, getSechubDefinition().getStop());
            }

            public SecHubSetupBuilder admin(String userId, String apiToken) {
                CredentialsDefinition adminCredentials = getSechubDefinition().getAdmin();

                adminCredentials.setUserId(userId);
                adminCredentials.setApiToken(apiToken);
                return this;
            }

            public ConfigurationBuilder configure() {
                return new ConfigurationBuilder();
            }

            public class ConfigurationBuilder {

                public SecHubSetupBuilder endConfigure() {
                    return SecHubSetupBuilder.this;
                }

                public ExecutorConfigBuilder addExecutor() {
                    return new ExecutorConfigBuilder();
                }

                public class ExecutorConfigBuilder extends AbstractDefinitionBuilder<ExecutorConfigBuilder> {
                    SecHubExecutorConfigDefinition executor;

                    private ExecutorConfigBuilder() {
                        executor = new SecHubExecutorConfigDefinition();
                        getSechubDefinition().getConfigure().getExecutors().add(executor);
                    }

                    @Override
                    protected AbstractDefinition resolveDefinition() {
                        return executor;
                    }

                    public ConfigurationBuilder endExecutor() {
                        return ConfigurationBuilder.this;
                    }

                    /**
                     * If enabled, the framework will use REST API calls to ensure SecHub is up and
                     * running.
                     *
                     * @return
                     */
                    public ExecutorConfigBuilder waitForAvailable() {
                        return waitForAvailable(true);
                    }

                    public ExecutorConfigBuilder waitForAvailable(boolean waitForAvailable) {
                        getSechubDefinition().setWaitForAvailable(Optional.of(waitForAvailable));
                        return this;
                    }

                    public ExecutorConfigBuilder forProfile(String profileId) {
                        executor.getProfiles().add(profileId);
                        return this;
                    }

                    public ExecutorConfigBuilder version(int version) {
                        executor.setVersion(version);
                        return this;
                    }

                    public ExecutorConfigBuilder forProfile(String... profileIds) {
                        for (String profileId : profileIds) {
                            executor.getProfiles().add(profileId);
                        }
                        return this;
                    }

                    public ExecutorConfigBuilder pdsProductId(String productId) {
                        executor.setPdsProductId(productId);
                        return this;
                    }

                    public ExecutorConfigBuilder parameter(String key, String value) {
                        executor.getParameters().put(key, value);
                        return this;
                    }

                    public ExecutorConfigBuilder name(String name) {
                        executor.setName(name);
                        return this;
                    }
                }

            }
        }

        public class SolutionSetupBuilder extends AbstractDefinitionBuilder<SolutionSetupBuilder> {

            private PDSSolutionDefinition setup;

            public SolutionSetupBuilder(String name) {
                setup = new PDSSolutionDefinition();
                setup.setName(name);
            }

            @Override
            protected AbstractDefinition resolveDefinition() {
                return setup;
            }

            public LocalSetupBuilder endSolution() {
                localSetup.getPdsSolutions().add(setup);
                return LocalSetupBuilder.this;
            }

            public StepBuilder<SolutionSetupBuilder> addStartStep() {
                return new StepBuilder<SolutionSetupBuilder>(this, setup.getStart());
            }

            public StepBuilder<SolutionSetupBuilder> addStopStep() {
                return new StepBuilder<SolutionSetupBuilder>(this, setup.getStop());
            }

            /**
             * If enabled, the framework will use REST API calls to ensure PDS is up and
             * running.
             *
             * @return
             */
            public SolutionSetupBuilder waitForAvailable() {
                return waitForAvailable(true);
            }

            public SolutionSetupBuilder waitForAvailable(boolean waitForAvailable) {
                setup.setWaitForAvailable(Optional.of(waitForAvailable));
                return this;
            }

            /**
             * If defined, the server configuration file location will not be calculated,
             * but the defined part will be used.
             *
             * @param pathToPdsServerConfigFile
             * @return
             */
            public SolutionSetupBuilder pathToServerConfigFile(String pathToPdsServerConfigFile) {
                setup.setPathToPdsServerConfigFile(pathToPdsServerConfigFile);
                return this;
            }

            public SolutionSetupBuilder url(URL url) {
                setup.setUrl(url == null ? null : url.toExternalForm());
                return this;
            }

            public SolutionSetupBuilder techUser(String userId, String apiToken) {

                CredentialsDefinition credentials = setup.getTechUser();
                credentials.setUserId(userId);
                credentials.setApiToken(apiToken);

                return this;
            }

        }

    }

    public SystemTestConfigurationBuilder addVariable(String name, String value) {
        configuration.getVariables().put(name, value);

        return this;
    }

    public RemoteSetupBuilder remoteSetup() {
        return new RemoteSetupBuilder();
    }

    public LocalSetupBuilder localSetup() {
        return new LocalSetupBuilder();
    }

    public SystemTestConfiguration build() {
        return configuration;
    }

    public class TestBuilder extends AbstractDefinitionBuilder<TestBuilder> {

        TestDefinition test;

        @Override
        protected AbstractDefinition resolveDefinition() {
            return test;
        }

        public class AssertsBuilder {

            public class AssertBuilder extends AbstractDefinitionBuilder<AssertBuilder> {
                TestAssertDefinition assertDefinition;

                public AssertBuilder() {
                    assertDefinition = new TestAssertDefinition();
                    test.getAssert().add(assertDefinition);
                }

                @Override
                protected AbstractDefinition resolveDefinition() {
                    return assertDefinition;
                }

                public AssertsBuilder endAssert() {
                    return AssertsBuilder.this;
                }

                public SecHubResultAssertBuilder secHubResult() {
                    return new SecHubResultAssertBuilder();
                }

                public class SecHubResultAssertBuilder extends AbstractDefinitionBuilder<SecHubResultAssertBuilder> {
                    AssertSechubResultDefinition sechubResultDefinition;

                    public SecHubResultAssertBuilder() {
                        sechubResultDefinition = new AssertSechubResultDefinition();
                        assertDefinition.setSechubResult(Optional.of(sechubResultDefinition));
                    }

                    @Override
                    protected AbstractDefinition resolveDefinition() {
                        return sechubResultDefinition;
                    }

                    public AssertBuilder endSecHubResult() {
                        return AssertBuilder.this;
                    }

                    /**
                     * Check if the SecHub result is the same as inside given file. Variable parts
                     * can be handled by place holders - look at
                     * {@link TestTemplateSupport#isTemplateMatching(String, String)} for details.
                     *
                     * @param pathToFile
                     * @return builder
                     */
                    public SecHubResultAssertBuilder equalsFile(String pathToFile) {
                        AssertEqualsFileDefinition definition = new AssertEqualsFileDefinition();
                        definition.setPath(pathToFile);

                        sechubResultDefinition.setEqualsFile(Optional.of(definition));
                        return this;
                    }

                    /**
                     * Assert that the JSON sechub report does contain all of the given strings
                     *
                     * @param containedStrings
                     * @return builder
                     */
                    public SecHubResultAssertBuilder containsStrings(String... containedStrings) {
                        AssertContainsStringsDefinition definition = new AssertContainsStringsDefinition();
                        definition.setValues(Arrays.asList(containedStrings));
                        sechubResultDefinition.setContainsStrings(Optional.of(definition));
                        return this;
                    }

                    public SecHubResultAssertBuilder hasTrafficLight(TrafficLight trafficLight) {
                        sechubResultDefinition.setHasTrafficLight(Optional.of(trafficLight));
                        return this;
                    }

                }
            }

            public AssertBuilder assertThat() {
                return new AssertBuilder();
            }

            public TestBuilder endAsserts() {
                return TestBuilder.this;
            }

        }

        public AssertsBuilder asserts() {
            return new AssertsBuilder();
        }

        public class SecHubRunBuilder extends AbstractDefinitionBuilder<SecHubRunBuilder> {

            private RunSecHubJobDefinition runSecHubJob;

            private SecHubRunBuilder() {

                runSecHubJob = new RunSecHubJobDefinition();

                TestExecutionDefinition execute = test.getExecute();
                execute.setRunSecHubJob(Optional.of(runSecHubJob));

            }

            @Override
            protected AbstractDefinition resolveDefinition() {
                return runSecHubJob;
            }

            public TestBuilder endRunSecHub() {
                return TestBuilder.this;
            }

            public SecHubRunBuilder project(String projectId) {
                runSecHubJob.setProject(projectId);
                return SecHubRunBuilder.this;
            }

            public UploadsBuilder uploads() {
                return new UploadsBuilder();
            }

            public class UploadsBuilder {

                public UploadsBuilder addBinaryUploadWithDefaultRef(String path) {
                    return addBinaryUpload(null, path);
                }

                public UploadsBuilder addBinaryUpload(String referenceId, String path) {
                    return upload(referenceId).binariesFolder(path).endUpload();
                }

                public UploadsBuilder addSourceUploadWithDefaultRef(String path) {
                    return addSourceUpload(null, path);
                }

                public UploadsBuilder addSourceUpload(String referenceId, String path) {
                    return upload(referenceId).sourceFolder(path).endUpload();
                }

                private UploadBuilder upload(String referenceId) {
                    return new UploadBuilder(referenceId);
                }

                public SecHubRunBuilder endUploads() {
                    return SecHubRunBuilder.this;
                }

                public class UploadBuilder extends AbstractDefinitionBuilder<UploadBuilder> {

                    private UploadDefinition uploadDefinition;

                    private UploadBuilder(String referenceId) {
                        uploadDefinition = new UploadDefinition();
                        uploadDefinition.setReferenceId(Optional.ofNullable(referenceId));
                        runSecHubJob.getUploads().add(uploadDefinition);
                    }

                    @Override
                    protected AbstractDefinition resolveDefinition() {
                        return uploadDefinition;
                    }

                    public UploadsBuilder endUpload() {
                        return UploadsBuilder.this;
                    }

                    public UploadBuilder sourceFolder(String path) {
                        uploadDefinition.setSourceFolder(Optional.of(path));
                        return this;
                    }

                    public UploadBuilder binariesFolder(String path) {
                        uploadDefinition.setBinariesFolder(Optional.of(path));
                        return this;
                    }

                }
            }

            public abstract class AbstractWithUploadReferencesScanConfigBuilder<T extends AbstractWithUploadReferencesScanConfigBuilder<T, C>, C extends SecHubDataConfigurationUsageByName> {

                private C configuration;

                private AbstractWithUploadReferencesScanConfigBuilder(C configuration) {
                    this.configuration = configuration;
                }

                protected C getConfiguration() {
                    return configuration;
                }

                @SuppressWarnings("unchecked")
                public T use(String... referenceIds) {
                    configuration.getNamesOfUsedDataConfigurationObjects().addAll(Arrays.asList(referenceIds));
                    return (T) this;
                }

                public SecHubRunBuilder endScan() {
                    return SecHubRunBuilder.this;
                }
            }

            public CodeScanConfigBuilder codeScan() {
                return new CodeScanConfigBuilder();
            }

            public LicenseScanConfigBuilder licenseScan() {
                return new LicenseScanConfigBuilder();
            }

            public WebScanConfigBuilder webScan() {
                return new WebScanConfigBuilder();
            }

            public InfraScanConfigBuilder infraScan() {
                return new InfraScanConfigBuilder();
            }

            public SecretScanConfigBuilder secretScan() {
                return new SecretScanConfigBuilder();
            }

            public class WebScanConfigBuilder {
                private SecHubWebScanConfiguration webScanConfig;

                private WebScanConfigBuilder() {
                    webScanConfig = new SecHubWebScanConfiguration();
                    runSecHubJob.setWebScan(Optional.of(webScanConfig));
                }

                public SecHubRunBuilder endScan() {
                    return SecHubRunBuilder.this;
                }

                public WebScanConfigBuilder url(String urlAsString) {
                    webScanConfig.setUrl(URI.create(urlAsString));
                    return this;
                }
            }

            public class InfraScanConfigBuilder {
                private InfraScanConfigBuilder() {
                    SecHubInfrastructureScanConfiguration configuration = new SecHubInfrastructureScanConfiguration();
                    runSecHubJob.setInfraScan(Optional.of(configuration));
                }

                public SecHubRunBuilder endScan() {
                    return SecHubRunBuilder.this;
                }
            }

            public class SecretScanConfigBuilder extends AbstractWithUploadReferencesScanConfigBuilder<SecretScanConfigBuilder, SecHubSecretScanConfiguration> {
                private SecretScanConfigBuilder() {
                    super(new SecHubSecretScanConfiguration());
                    runSecHubJob.setSecretScan(Optional.of(getConfiguration()));
                }
            }

            public class CodeScanConfigBuilder extends AbstractWithUploadReferencesScanConfigBuilder<CodeScanConfigBuilder, SecHubCodeScanConfiguration> {
                private CodeScanConfigBuilder() {
                    super(new SecHubCodeScanConfiguration());
                    runSecHubJob.setCodeScan(Optional.of(getConfiguration()));
                }
            }

            public class LicenseScanConfigBuilder
                    extends AbstractWithUploadReferencesScanConfigBuilder<LicenseScanConfigBuilder, SecHubLicenseScanConfiguration> {
                private LicenseScanConfigBuilder() {
                    super(new SecHubLicenseScanConfiguration());
                    runSecHubJob.setLicenseScan(Optional.of(getConfiguration()));
                }
            }

        }

        private TestBuilder(String testName) {
            test = new TestDefinition();
            configuration.getTests().add(test);

            test.setName(testName);
        }

        public SecHubRunBuilder runSecHubJob() {
            return new SecHubRunBuilder();
        }

        public SystemTestConfigurationBuilder endTest() {
            return SystemTestConfigurationBuilder.this;
        }

        public StepBuilder<TestBuilder> prepareStep() {
            return new StepBuilder<TestBuilder>(this, test.getPrepare());
        }

    }

    public TestBuilder test(String testName) {
        return new TestBuilder(testName);
    }

}

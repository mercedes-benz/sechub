package com.mercedesbenz.sechub.systemtest.config;

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

public class SystemTestConfigurationBuilder {

    private SystemTestConfiguration configuration;

    SystemTestConfigurationBuilder() {
        this.configuration = new SystemTestConfiguration();
    }

    public class StepBuilder<T> {
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

        public ScriptBuilder script() {
            return new ScriptBuilder();
        }

        public T endStep() {
            return parent;
        }

        public class ScriptBuilder {

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
        }

        public class ProcessDefinitionBuilder {

            private StepBuilder<T>.ScriptBuilder scriptBuilder;
            private ProcessDefinition process;

            public ProcessDefinitionBuilder(StepBuilder<T>.ScriptBuilder scriptBuilder) {
                this.scriptBuilder = scriptBuilder;
                this.process = scriptBuilder.scriptCallDefinition.getProcess();
            }

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

        }
    }

    private abstract class AbstractSecHubDefinitionBuilder<T extends AbstractSecHubDefinitionBuilder<?, ?>, D extends AbstractSecHubDefinition> {
        private D sechubDefinition;

        AbstractSecHubDefinitionBuilder(Class<T> clazz, D sechubDefinition) {
            this.sechubDefinition = sechubDefinition;
        }

        protected D getSechubDefinition() {
            return sechubDefinition;
        }

        @SuppressWarnings("unchecked")
        public T url(URL url) {
            sechubDefinition.setUrl(url);
            return (T) this;
        }
    }

    public class RemoteSetupBuilder {
        private RemoteSetupDefinition remoteSetup;

        private RemoteSetupBuilder() {
            remoteSetup = new RemoteSetupDefinition();
            configuration.getSetup().setRemote(Optional.of(remoteSetup));
        }

        public SecHubSetupBuilder secHub() {
            return new SecHubSetupBuilder();
        }

        public class SecHubSetupBuilder extends AbstractSecHubDefinitionBuilder<SecHubSetupBuilder, RemoteSecHubDefinition> {

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

    }

    public class LocalSetupBuilder {

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

        public class SecHubSetupBuilder extends AbstractSecHubDefinitionBuilder<SecHubSetupBuilder, LocalSecHubDefinition> {

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

                public class ExecutorConfigBuilder {
                    SecHubExecutorConfigDefinition executor;

                    private ExecutorConfigBuilder() {
                        executor = new SecHubExecutorConfigDefinition();
                        getSechubDefinition().getConfigure().getExecutors().add(executor);
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
                        return waitForAVailable(true);
                    }

                    public ExecutorConfigBuilder waitForAVailable(boolean waitForAVailable) {
                        getSechubDefinition().setWaitForAvailable(Optional.of(waitForAVailable));
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

                    public ExecutorConfigBuilder credentials(String userId, String apiToken) {

                        CredentialsDefinition credentials = executor.getCredentials();
                        credentials.setUserId(userId);
                        credentials.setApiToken(apiToken);

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
                }

            }
        }

        public class SolutionSetupBuilder {

            private PDSSolutionDefinition setup;

            public SolutionSetupBuilder(String name) {
                setup = new PDSSolutionDefinition();
                setup.setName(name);
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
                return waitForAVailable(true);
            }

            public SolutionSetupBuilder waitForAVailable(boolean waitForAVailable) {
                setup.setWaitForAvailable(Optional.of(waitForAVailable));
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
                setup.setUrl(url);
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

    public class TestBuilder {

        TestDefinition test;

        public class SecHubRunBuilder {

            private RunSecHubJobDefinition runSecHubJob;

            private SecHubRunBuilder() {

                runSecHubJob = new RunSecHubJobDefinition();

                TestExecutionDefinition execute = test.getExecute();
                execute.setRunSecHubJob(Optional.of(runSecHubJob));

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
                public UploadBuilder upload() {
                    return new UploadBuilder();
                }

                public SecHubRunBuilder endUploads() {
                    return SecHubRunBuilder.this;
                }

                public class UploadBuilder {

                    private UploadDefinition uploadDefinition;

                    private UploadBuilder() {
                        uploadDefinition = new UploadDefinition();
                        runSecHubJob.getUploads().add(uploadDefinition);
                    }

                    public UploadsBuilder endUpload() {
                        return UploadsBuilder.this;
                    }

                    public UploadBuilder sources(String path) {
                        uploadDefinition.setSourceFolder(Optional.of(path));
                        return this;
                    }

                    public UploadBuilder binaries(String path) {
                        uploadDefinition.setBinariesFolder(Optional.of(path));
                        return this;
                    }

                    public UploadBuilder withReferenceId(String id) {
                        uploadDefinition.setReferenceId(Optional.of(id));
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
                private WebScanConfigBuilder() {
                    SecHubWebScanConfiguration configuration = new SecHubWebScanConfiguration();
                    runSecHubJob.setWebScan(Optional.of(configuration));
                }

                public SecHubRunBuilder endScan() {
                    return SecHubRunBuilder.this;
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

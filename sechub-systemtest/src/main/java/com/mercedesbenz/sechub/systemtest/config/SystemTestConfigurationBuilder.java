package com.mercedesbenz.sechub.systemtest.config;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

        public RemoteSecHubSetupBuilder secHub() {
            return new RemoteSecHubSetupBuilder();
        }

        public class RemoteSecHubSetupBuilder extends AbstractSecHubDefinitionBuilder<RemoteSecHubSetupBuilder, RemoteSecHubDefinition> {

            public RemoteSecHubSetupBuilder() {
                super(RemoteSecHubSetupBuilder.class, remoteSetup.getSecHub());
            }

            public RemoteSecHubSetupBuilder user(String userId, String apiToken) {
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

        public LocalSecHubSetupBuilder secHub() {
            return new LocalSecHubSetupBuilder();
        }

        public class LocalSecHubSetupBuilder extends AbstractSecHubDefinitionBuilder<LocalSecHubSetupBuilder, LocalSecHubDefinition> {

            public LocalSecHubSetupBuilder() {
                super(LocalSecHubSetupBuilder.class, localSetup.getSecHub());
            }

            public LocalSetupBuilder endSecHub() {
                return LocalSetupBuilder.this;
            }

            public StepBuilder<LocalSecHubSetupBuilder> addStartStep() {
                return new StepBuilder<LocalSecHubSetupBuilder>(this, getSechubDefinition().getStart());
            }

            public StepBuilder<LocalSecHubSetupBuilder> addStopStep() {
                return new StepBuilder<LocalSecHubSetupBuilder>(this, getSechubDefinition().getStop());
            }

            public LocalSecHubSetupBuilder admin(String userId, String apiToken) {
                CredentialsDefinition adminCredentials = getSechubDefinition().getAdmin();

                adminCredentials.setUserId(userId);
                adminCredentials.setApiToken(apiToken);
                return this;
            }

            public SecHubConfigurationBuilder configure() {
                return new SecHubConfigurationBuilder();
            }

            public class SecHubConfigurationBuilder {

                public LocalSecHubSetupBuilder endConfigure() {
                    return LocalSecHubSetupBuilder.this;
                }

                public SecHubExecutorConfigBuilder addExecutor() {
                    return new SecHubExecutorConfigBuilder();
                }

                public class SecHubExecutorConfigBuilder {
                    SecHubExecutorConfigDefinition executor;

                    private SecHubExecutorConfigBuilder() {
                        executor = new SecHubExecutorConfigDefinition();
                        getSechubDefinition().getConfigure().getExecutors().add(executor);
                    }

                    public SecHubConfigurationBuilder endExecutor() {
                        return SecHubConfigurationBuilder.this;
                    }

                    /**
                     * If enabled, the framework will use REST API calls to ensure SecHub is up and
                     * running.
                     *
                     * @return
                     */
                    public SecHubExecutorConfigBuilder waitForAvailable() {
                        return waitForAVailable(true);
                    }

                    public SecHubExecutorConfigBuilder waitForAVailable(boolean waitForAVailable) {
                        getSechubDefinition().setWaitForAvailable(Optional.of(waitForAVailable));
                        return this;
                    }

                    public SecHubExecutorConfigBuilder forProfile(String profileId) {
                        executor.getProfiles().add(profileId);
                        return this;
                    }

                    public SecHubExecutorConfigBuilder version(int version) {
                        executor.setVersion(version);
                        return this;
                    }

                    public SecHubExecutorConfigBuilder credentials(String userId, String apiToken) {

                        CredentialsDefinition credentials = executor.getCredentials();
                        credentials.setUserId(userId);
                        credentials.setApiToken(apiToken);

                        return this;
                    }

                    public SecHubExecutorConfigBuilder forProfile(String... profileIds) {
                        for (String profileId : profileIds) {
                            executor.getProfiles().add(profileId);
                        }
                        return this;
                    }

                    public SecHubExecutorConfigBuilder pdsProductId(String productId) {
                        executor.setPdsProductId(productId);
                        return this;
                    }

                    public SecHubExecutorConfigBuilder parameter(String key, String value) {
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

            public SecHubRunBuilder uploadBinaries(String path) {
                runSecHubJob.getUpload().setBinariesFolder(path);
                return SecHubRunBuilder.this;
            }

            public SecHubRunBuilder uploadSources(String path) {
                runSecHubJob.getUpload().setSourceFolder(path);
                return SecHubRunBuilder.this;
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

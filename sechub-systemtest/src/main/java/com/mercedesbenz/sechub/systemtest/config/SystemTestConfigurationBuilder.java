package com.mercedesbenz.sechub.systemtest.config;

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

            public ProcessDefinitionBuilder waitForStage() {
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

        public class SecHubSetupBuilder {

            private LocalSecHubDefinition localSechub;

            public SecHubSetupBuilder() {
                localSechub = localSetup.getSecHub();
            }

            public LocalSetupBuilder endSecHub() {
                return LocalSetupBuilder.this;
            }

            public StepBuilder<SecHubSetupBuilder> addStartStep() {
                return new StepBuilder<SecHubSetupBuilder>(this, localSechub.getStart());
            }

            public StepBuilder<SecHubSetupBuilder> addStopStep() {
                return new StepBuilder<SecHubSetupBuilder>(this, localSechub.getStop());
            }

            public SecHubConfigurationBuilder configure() {
                return new SecHubConfigurationBuilder();
            }

            public class SecHubConfigurationBuilder {

                public SecHubSetupBuilder endConfigure() {
                    return SecHubSetupBuilder.this;
                }

                public SecHubExecutorConfigBuilder addExecutor() {
                    return new SecHubExecutorConfigBuilder();
                }

                public class SecHubExecutorConfigBuilder {
                    SecHubExecutorConfigDefinition executor;

                    private SecHubExecutorConfigBuilder() {
                        executor = new SecHubExecutorConfigDefinition();
                        localSechub.getConfigure().getExecutors().add(executor);
                    }

                    public SecHubConfigurationBuilder endExecutor() {
                        return SecHubConfigurationBuilder.this;
                    }

                    public SecHubExecutorConfigBuilder forProfile(String profileId) {
                        executor.setProfile(profileId);
                        return this;
                    }

                    public SecHubExecutorConfigBuilder pdsProductId(String productId) {
                        executor.setPdsProductId(productId);
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
             * If enabled, the framework will use REST api calls to ensure the PDS solution
             * is up and running.
             *
             * @return
             */
            public SolutionSetupBuilder waitForAVailable() {
                return waitForAVailable(true);
            }

            public SolutionSetupBuilder waitForAVailable(boolean waitForAVailable) {
                setup.setWaitForAvailable(waitForAVailable);
                return this;
            }

        }
    }

    public SystemTestConfigurationBuilder addVariable(String name, String value) {
        configuration.getVariables().put(name, value);

        return this;
    }

    public LocalSetupBuilder localSetup() {
        return new LocalSetupBuilder();
    }

    public SystemTestConfiguration build() {
        return configuration;
    }

}

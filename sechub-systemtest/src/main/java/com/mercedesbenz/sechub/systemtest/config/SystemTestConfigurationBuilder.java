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
    
    private abstract class AbstractSecHubDefinitionBuilder<T extends AbstractSecHubDefinitionBuilder<?>>{
        private AbstractSecHubDefinition sechubDefinition;

        AbstractSecHubDefinitionBuilder(Class<T> clazz,  AbstractSecHubDefinition sechubDefinition){
            this.sechubDefinition=sechubDefinition;
        }
        @SuppressWarnings("unchecked")
        public T user(String userId, String apiToken) {
            CredentialsDefinition userCredentials = sechubDefinition.getUser();
            
            userCredentials.setUserId(userId);
            userCredentials.setApiToken(apiToken);
            return (T) this;
        }
        @SuppressWarnings("unchecked")
        public T admin(String userId, String apiToken) {
            CredentialsDefinition adminCredentials = sechubDefinition.getAdmin();
            
            adminCredentials.setUserId(userId);
            adminCredentials.setApiToken(apiToken);
            return (T) this;
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

        public class RemoteSecHubSetupBuilder extends AbstractSecHubDefinitionBuilder<RemoteSecHubSetupBuilder>{


            public RemoteSecHubSetupBuilder() {
                super(RemoteSecHubSetupBuilder.class, remoteSetup.getSecHub());
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

        public class LocalSecHubSetupBuilder extends AbstractSecHubDefinitionBuilder<LocalSecHubSetupBuilder>{

            private LocalSecHubDefinition localSechub;

            public LocalSecHubSetupBuilder() {
                super(LocalSecHubSetupBuilder.class, localSetup.getSecHub());
                localSechub = localSetup.getSecHub();
            }

            public LocalSetupBuilder endSecHub() {
                return LocalSetupBuilder.this;
            }

            public StepBuilder<LocalSecHubSetupBuilder> addStartStep() {
                return new StepBuilder<LocalSecHubSetupBuilder>(this, localSechub.getStart());
            }

            public StepBuilder<LocalSecHubSetupBuilder> addStopStep() {
                return new StepBuilder<LocalSecHubSetupBuilder>(this, localSechub.getStop());
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

    public RemoteSetupBuilder remoteSetup() {
        return new RemoteSetupBuilder();
    }

    public LocalSetupBuilder localSetup() {
        return new LocalSetupBuilder();
    }

    public SystemTestConfiguration build() {
        return configuration;
    }

}

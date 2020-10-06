package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class DefaultIntegrationTestExecutorConfigurations {
    
    private static final String INTTEST_NAME_PREFIX = "INTTEST_";
    
    public static TestExecutorConfig NETSPARKER = createNetsparkerConfig();
    public static TestExecutorConfig CHECKMARX = createCheckmarxConfig();
    public static TestExecutorConfig NESSUS = createNessusConfig();

    private static TestExecutorConfig createNetsparkerConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.enabled=true;
        config.executorVersion=1;
        config.productIdentifier=TestExecutorProductIdentifier.NETSPARKER.name();
        config.name=INTTEST_NAME_PREFIX+"Netsparker config";
        config.setup.baseURL="https://netsparker.example.com";
        return config;
    }
    
    private static TestExecutorConfig createCheckmarxConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.enabled=true;
        config.executorVersion=1;
        config.productIdentifier=TestExecutorProductIdentifier.CHECKMARX.name();
        config.name=INTTEST_NAME_PREFIX+"Checkmarx config";
        config.setup.baseURL="https://checkmarx.example.com";
        return config;
    }
    
    private static TestExecutorConfig createNessusConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.enabled=true;
        config.executorVersion=1;
        config.productIdentifier=TestExecutorProductIdentifier.NESSUS.name();
        config.name=INTTEST_NAME_PREFIX+"Checkmarx config";
        config.setup.baseURL="https://nessus.example.com";
        return config;
    }
}

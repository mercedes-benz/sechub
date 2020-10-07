package com.daimler.sechub.integrationtest.internal;

import com.daimler.sechub.integrationtest.api.TestExecutorProductIdentifier;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class IntegrationTestDefaultExecutorConfigurations {
    
    private static final String INTTEST_NAME_PREFIX = "INTTEST_";
    
    public static final TestExecutorConfig NETSPARKER_V1 = defineNetsparkerConfig();
    public static final TestExecutorConfig CHECKMARX_V1 = defineCheckmarxConfig();
    public static final TestExecutorConfig NESSUS_V1 = defineNessusConfig();

    private static TestExecutorConfig defineNetsparkerConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.enabled=true;
        config.executorVersion=1;
        config.productIdentifier=TestExecutorProductIdentifier.NETSPARKER.name();
        config.name=INTTEST_NAME_PREFIX+"Netsparker V1";
        config.setup.baseURL="https://netsparker.example.com";
        config.uuid=null;// not initialized - is done at creation time by scenario initializer!
        return config;
    }
    
    private static TestExecutorConfig defineCheckmarxConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.enabled=true;
        config.executorVersion=1;
        config.productIdentifier=TestExecutorProductIdentifier.CHECKMARX.name();
        config.name=INTTEST_NAME_PREFIX+"Checkmarx V2";
        config.setup.baseURL="https://checkmarx.example.com";
        config.uuid=null;// not initialized - is done at creation time by scenario initializer!
        return config;
    }
    
    private static TestExecutorConfig defineNessusConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.enabled=true;
        config.executorVersion=1;
        config.productIdentifier=TestExecutorProductIdentifier.NESSUS.name();
        config.name=INTTEST_NAME_PREFIX+"Checkmarx V3";
        config.setup.baseURL="https://nessus.example.com";
        config.uuid=null;// not initialized - is done at creation time by scenario initializer!
        return config;
    }
}

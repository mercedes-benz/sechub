// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry;
import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry.EnvironmentVariableKeyValueEntry;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.pds.encryption.PDSCipherAlgorithm;
import com.mercedesbenz.sechub.pds.encryption.PDSEncryptionConfiguration;
import com.mercedesbenz.sechub.pds.security.PDSSecurityConfiguration;
import com.mercedesbenz.sechub.pds.storage.PDSS3PropertiesSetup;
import com.mercedesbenz.sechub.pds.storage.PDSSharedVolumePropertiesSetup;

@Component
public class PDSStartupAssertEnvironmentVariablesUsed {

    private static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";

    @PDSMustBeDocumented(value = "When true, the startup assertion for forcing usage of some environment variables is disabled", scope = "startup")
    @Value("${pds.startup.assert.environment-variables-used.disabled:false}")
    boolean disabled;

    @Autowired
    PDSS3PropertiesSetup s3Setup;

    @Autowired
    PDSSharedVolumePropertiesSetup sharedVolumeSetup;

    @Autowired
    SystemEnvironmentVariableSupport envVariableSupport;

    @Autowired
    PDSSecurityConfiguration securityConfiguration;

    @Autowired
    PDSEncryptionConfiguration encryptionConfiguration;

    @Autowired
    Environment environment;

    private SecureEnvironmentVariableKeyValueRegistry sensitiveDataRegistry;

    private static final Logger LOG = LoggerFactory.getLogger(PDSStartupAssertEnvironmentVariablesUsed.class);

    @EventListener(ApplicationStartedEvent.class)
    public void assertOnApplicationStart() {
        this.sensitiveDataRegistry = createRegistryForOnlyAllowedAsEnvironmentVariables(false);

        if (disabled) {
            LOG.warn("Environment variable usage assertion has been disabled! This is only allowed for tests! Do not disable in production!");
            return;
        }

        assertSensitiveDataMustBeDefinedByEnvironmentVariables();
    }

    private void assertSensitiveDataMustBeDefinedByEnvironmentVariables() {
        StringBuilder problems = new StringBuilder();
        for (EnvironmentVariableKeyValueEntry entry : sensitiveDataRegistry.getEntries()) {
            String value = entry.getValue();
            boolean same = envVariableSupport.isValueLikeEnvironmentVariableValue(entry.getVariableName(), value);

            if (!same) {
                problems.append("Key: '").append(entry.getKey()).append("' was not defined by environment variable:").append(entry.getVariableName());
                problems.append("\n");
            }

        }
        if (problems.isEmpty()) {
            return;
        }
        throw new IllegalStateException(
                "Some PDS startup settings must be defined by environment variables, but were definded in a different way:\n" + problems);
    }

    public SecureEnvironmentVariableKeyValueRegistry createRegistryForOnlyAllowedAsEnvironmentVariables(boolean createFallbacks) {
        SecureEnvironmentVariableKeyValueRegistry sensitiveDataRegistry = new SecureEnvironmentVariableKeyValueRegistry();

        if (createFallbacks) {

            if (s3Setup == null) {
                s3Setup = new PDSS3PropertiesSetup();
            }
            if (sharedVolumeSetup == null) {
                sharedVolumeSetup = new PDSSharedVolumePropertiesSetup();
            }
            if (securityConfiguration == null) {
                securityConfiguration = PDSSecurityConfiguration.create("test-user", "test-user-token", "test-admin", "test-admintoken");
            }
            if (encryptionConfiguration == null) {
                encryptionConfiguration = PDSEncryptionConfiguration.create(PDSCipherAlgorithm.NONE, null);
            }
        }
        s3Setup.registerOnlyAllowedAsEnvironmentVariables(sensitiveDataRegistry);
        sharedVolumeSetup.registerOnlyAllowedAsEnvironmentVariables(sensitiveDataRegistry);
        securityConfiguration.registerOnlyAllowedAsEnvironmentVariables(sensitiveDataRegistry);
        encryptionConfiguration.registerOnlyAllowedAsEnvironmentVariables(sensitiveDataRegistry);

        // some additional parts which shall only be available as environment variables
        // - h2 databases allow no setup here, so not mandatory
        sensitiveDataRegistry.register(sensitiveDataRegistry.newEntry().key(SPRING_DATASOURCE_PASSWORD)
                .nullableValue(environment != null ? environment.getProperty(SPRING_DATASOURCE_PASSWORD) : null));

        return sensitiveDataRegistry;
    }

}

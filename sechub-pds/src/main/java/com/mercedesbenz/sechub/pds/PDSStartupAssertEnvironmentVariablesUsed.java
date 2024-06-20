// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry;
import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry.EnvironmentVariableKeyValueEntry;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.pds.security.PDSSecurityConfiguration;
import com.mercedesbenz.sechub.pds.storage.PDSS3PropertiesSetup;
import com.mercedesbenz.sechub.pds.storage.PDSSharedVolumePropertiesSetup;

@Component
public class PDSStartupAssertEnvironmentVariablesUsed {

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

    private SecureEnvironmentVariableKeyValueRegistry sensitiveDataRegistry;

    private static final Logger LOG = LoggerFactory.getLogger(PDSStartupAssertEnvironmentVariablesUsed.class);

    @EventListener(ApplicationStartedEvent.class)
    public void assertOnApplicationStart() {
        this.sensitiveDataRegistry = createRegistryForOnlyAllowedAsEnvironmentVariables();

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

    public SecureEnvironmentVariableKeyValueRegistry createRegistryForOnlyAllowedAsEnvironmentVariables() {
        SecureEnvironmentVariableKeyValueRegistry sensitiveDataRegistry = new SecureEnvironmentVariableKeyValueRegistry();

        if (s3Setup == null) {
            s3Setup = useFallback(new PDSS3PropertiesSetup());
        }
        if (sharedVolumeSetup == null) {
            sharedVolumeSetup = useFallback(new PDSSharedVolumePropertiesSetup());
        }
        if (securityConfiguration == null) {
            securityConfiguration = useFallback(new PDSSecurityConfiguration());
        }

        s3Setup.registerOnlyAllowedAsEnvironmentVariables(sensitiveDataRegistry);
        sharedVolumeSetup.registerOnlyAllowedAsEnvironmentVariables(sensitiveDataRegistry);
        securityConfiguration.registerOnlyAllowedAsEnvironmentVariables(sensitiveDataRegistry);

        // some additional parts which shall only be available as environment variables
        sensitiveDataRegistry.register(sensitiveDataRegistry.newEntry().key("spring.datasource.password"));

        return sensitiveDataRegistry;
    }

    private <T> T useFallback(T fallbackObject) {
        if (fallbackObject == null) {
            throw new IllegalArgumentException("Fallback object may never be null!");
        }

        LOG.warn("Using fallback for : {} - may only happen when used outside spring container", fallbackObject.getClass());

        return fallbackObject;
    }

}

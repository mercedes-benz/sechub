// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

class SecHubExecutionContextTest {

    private static final String EXECUTED_BY_TEST = "executed-by-test...";
    private UUID uuid;
    private SecHubConfiguration config;

    @BeforeEach
    void beforeEach() {
        uuid = UUID.randomUUID();
        config = mock(SecHubConfiguration.class);
    }

    @Test
    void constructor_without_operation_type_is_operation_type_SCAN() {
        /* execute */
        SecHubExecutionContext context = new SecHubExecutionContext(uuid, config, EXECUTED_BY_TEST);

        /* test */
        assertEquals(SecHubExecutionOperationType.SCAN, context.getOperationType());
    }

    @EnumSource(SecHubExecutionOperationType.class)
    @ParameterizedTest
    void constructor_with_operation_type_is_given_operation_type(SecHubExecutionOperationType type) {
        /* execute */
        SecHubExecutionContext context = new SecHubExecutionContext(uuid, config, EXECUTED_BY_TEST, type);

        /* test */
        assertEquals(type, context.getOperationType());
    }

    @Test
    void constructor_with_operation_type_NULL_is_operation_type_SCAN() {
        /* prepare */
        SecHubExecutionOperationType type = null;

        /* execute */
        SecHubExecutionContext context = new SecHubExecutionContext(uuid, config, EXECUTED_BY_TEST, type);

        /* test */
        assertEquals(SecHubExecutionOperationType.SCAN, context.getOperationType());
    }

}

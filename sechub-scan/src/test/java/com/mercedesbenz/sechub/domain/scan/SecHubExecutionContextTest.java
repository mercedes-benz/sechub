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
    private UUID sechubJobUUID;
    private SecHubConfiguration config;
    private UUID executionUUID;

    @BeforeEach
    void beforeEach() {
        executionUUID = UUID.randomUUID();
        sechubJobUUID = UUID.randomUUID();
        config = mock(SecHubConfiguration.class);
    }

    @Test
    void constructor_without_operation_type_is_operation_type_SCAN() {
        /* execute */
        SecHubExecutionContext context = new SecHubExecutionContext(sechubJobUUID, config, EXECUTED_BY_TEST, executionUUID);

        /* test */
        assertEquals(SecHubExecutionOperationType.SCAN, context.getOperationType());
    }

    @EnumSource(SecHubExecutionOperationType.class)
    @ParameterizedTest
    void constructor_with_operation_type_is_given_operation_type(SecHubExecutionOperationType type) {
        /* execute */
        SecHubExecutionContext context = new SecHubExecutionContext(sechubJobUUID, config, EXECUTED_BY_TEST, executionUUID, type);

        /* test */
        assertEquals(type, context.getOperationType());
    }

    @Test
    void constructor_with_operation_type_NULL_is_operation_type_SCAN() {
        /* prepare */
        SecHubExecutionOperationType operationTypeAsNull = null;

        /* execute */
        SecHubExecutionContext context = new SecHubExecutionContext(sechubJobUUID, config, EXECUTED_BY_TEST, executionUUID, operationTypeAsNull);

        /* test */
        assertEquals(SecHubExecutionOperationType.SCAN, context.getOperationType());
    }

    @Test
    void after_construction_execution_uuid_is_as_defined_and_not_same_as_jobuuid() {
        /* prepare */
        SecHubExecutionOperationType operationTypeAsNull = null;

        /* execute */
        SecHubExecutionContext context = new SecHubExecutionContext(sechubJobUUID, config, EXECUTED_BY_TEST, executionUUID, operationTypeAsNull);

        /* test */
        assertNotEquals(sechubJobUUID, context.getExecutionUUID());
        assertEquals(executionUUID, context.getExecutionUUID());
        assertNotNull(context.getExecutionUUID());
    }

}

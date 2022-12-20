// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;

class ProcessHandlingDataFactoryTest {

    private static final int TEST_VALUE_CONFIGURABLE_MIN_MINUTES_TO_WAIT_FOR_PRODUCT = 1;

    private static final int TEST_VALUE_CONFIGURABLE_MAX_MINUTES_TO_WAIT_FOR_PRODUCT = 5000;

    private static final int TEST_VALUE_OF_SYSTEM_WIDE_MINUTES_TO_WAIT_FOR_PRODUCT = 4711;

    private ProcessHandlingDataFactory factoryToTest;
    private PDSJobConfiguration configuration;
    private PDSServerConfigurationService serverConfigurationService;

    @BeforeEach
    void beforeEach() {
        serverConfigurationService = mock(PDSServerConfigurationService.class);
        configuration = new PDSJobConfiguration();

        factoryToTest = new ProcessHandlingDataFactory();
        factoryToTest.serverConfigurationService = serverConfigurationService;

        when(serverConfigurationService.getMinutesToWaitForProduct()).thenReturn(TEST_VALUE_OF_SYSTEM_WIDE_MINUTES_TO_WAIT_FOR_PRODUCT);
        when(serverConfigurationService.getMaximumConfigurableMinutesToWaitForProduct()).thenReturn(TEST_VALUE_CONFIGURABLE_MAX_MINUTES_TO_WAIT_FOR_PRODUCT);
        when(serverConfigurationService.getMinimumConfigurableMinutesToWaitForProduct()).thenReturn(TEST_VALUE_CONFIGURABLE_MIN_MINUTES_TO_WAIT_FOR_PRODUCT);

    }

    @ParameterizedTest
    @ValueSource(ints = { 1, TEST_VALUE_OF_SYSTEM_WIDE_MINUTES_TO_WAIT_FOR_PRODUCT, TEST_VALUE_OF_SYSTEM_WIDE_MINUTES_TO_WAIT_FOR_PRODUCT + 1 })
    void createForLaunchOperation_valid_job_parameters_do_result_data_with_this_value(int givenMinutes) {
        /* prepare */
        configuration.getParameters()
                .add(new PDSExecutionParameterEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES, "" + givenMinutes));

        /* execute */
        ProductLaunchProcessHandlingData created = factoryToTest.createForLaunchOperation(configuration);

        /* test */
        assertEquals(givenMinutes, created.getMinutesToWaitBeforeProductTimeout());
    }

    @Test
    void createForLaunchOperation_without_job_parameter_the_system_wide_value_is_used() {

        /* execute */
        ProductLaunchProcessHandlingData created = factoryToTest.createForLaunchOperation(configuration);

        /* test */
        assertEquals(TEST_VALUE_OF_SYSTEM_WIDE_MINUTES_TO_WAIT_FOR_PRODUCT, created.getMinutesToWaitBeforeProductTimeout());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    void createForLaunchOperation_invalid_job_parameters_lower_than_min_do_result_data_with_the_min_value(int givenMinutes) {
        /* prepare */
        configuration.getParameters()
                .add(new PDSExecutionParameterEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES, "" + givenMinutes));

        /* execute */
        ProductLaunchProcessHandlingData created = factoryToTest.createForLaunchOperation(configuration);

        /* test */
        assertEquals(TEST_VALUE_CONFIGURABLE_MIN_MINUTES_TO_WAIT_FOR_PRODUCT, created.getMinutesToWaitBeforeProductTimeout());
    }

    @ParameterizedTest
    @ValueSource(ints = { 6000, 7000 })
    void createForLaunchOperation_invalid_job_parameters_greater_than_max_do_result_data_with_the_max_value(int givenMinutes) {
        /* prepare */
        configuration.getParameters()
                .add(new PDSExecutionParameterEntry(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES, "" + givenMinutes));

        /* execute */
        ProductLaunchProcessHandlingData created = factoryToTest.createForLaunchOperation(configuration);

        /* test */
        assertEquals(TEST_VALUE_CONFIGURABLE_MAX_MINUTES_TO_WAIT_FOR_PRODUCT, created.getMinutesToWaitBeforeProductTimeout());
    }

}

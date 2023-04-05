package com.mercedesbenz.sechub.systemtest.runtime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;
import com.mercedesbenz.sechub.test.TestUtil;

class SystemTestRuntimePreparatorTest {

    private LocationSupport locationSupport;
    private SystemTestRuntimePreparator preparatorToTest;
    private EnvironmentProvider environmentProvider;
    private SystemTestRuntimeContext context;

    @BeforeEach
    void beforeEach() throws IOException {
        locationSupport = mock(LocationSupport.class);
        environmentProvider = mock(EnvironmentProvider.class);

        context = new SystemTestRuntimeContext();
        context.environmentProvider = environmentProvider;
        context.locationSupport = locationSupport;
        context.workspaceRoot = TestUtil.createTempDirectoryInBuildFolder("systemtest_prep_workspaceroot");

        preparatorToTest = new SystemTestRuntimePreparator();

    }

    @Test
    void variable_handling_in_a_remote_config_even_comments_can_have_variables() {
        /* prepare */
        SystemTestConfiguration originConfiguration = new SystemTestConfiguration();
        originConfiguration.getVariables().put("var1", "value1");
        originConfiguration.getSetup()
                .setComment("This is a comment - even this is replaceable - because we just change the complete JSON... var1=${variables.var1}");

        context.originConfiguration = originConfiguration;

        /* execute */
        preparatorToTest.prepare(context);

        /* test */
        assertEquals("This is a comment - even this is replaceable - because we just change the complete JSON... var1=value1",
                context.getConfiguration().getSetup().getComment());

    }

}

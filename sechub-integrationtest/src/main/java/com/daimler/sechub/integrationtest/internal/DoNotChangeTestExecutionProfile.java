// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

/**
 * Do not change those profiles in tests!
 * @author Albert Tregnaghi
 *
 */
public class DoNotChangeTestExecutionProfile extends TestExecutionProfile{

    List<TestExecutorConfig> initialConfigurationsWithoutUUID = new ArrayList<>();  
}

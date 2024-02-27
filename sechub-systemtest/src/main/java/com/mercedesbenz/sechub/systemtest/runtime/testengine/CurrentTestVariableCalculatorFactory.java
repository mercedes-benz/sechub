// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeContext;

interface CurrentTestVariableCalculatorFactory {

    CurrentTestVariableCalculator create(TestDefinition test, SystemTestRuntimeContext context);
}
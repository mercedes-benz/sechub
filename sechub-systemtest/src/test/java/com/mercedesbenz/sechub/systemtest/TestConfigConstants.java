package com.mercedesbenz.sechub.systemtest;

import com.mercedesbenz.sechub.systemtest.config.RuntimeVariable;
import com.mercedesbenz.sechub.systemtest.template.TemplateVariableType;

public class TestConfigConstants {

    public static final String RUNTIME_WORKSPACE_ROOT = TemplateVariableType.RUNTIME_VARIABLES.getFullPrefix()
            + RuntimeVariable.WORKSPACE_ROOT.getVariableName();

}

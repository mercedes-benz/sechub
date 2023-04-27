package com.mercedesbenz.sechub.systemtest;

import com.mercedesbenz.sechub.systemtest.config.RuntimeVariable;
import com.mercedesbenz.sechub.systemtest.template.TemplateVariableType;

public class TestConfigConstants {

    public static final String RUNTIME_WORKSPACE_ROOT = TemplateVariableType.RUNTIME_VARIABLES.getFullPrefix()
            + RuntimeVariable.WORKSPACE_ROOT.getVariableName();

    public static final int DEFAULT_SECHUB_INTTEST_PORT = 8443;
    public static final int DEFAULT_PDS_INTTEST_PORT = 8444;

    public static final String SYSTEM_PROPERTY_SECHUB_INTTEST_PORT = "sechub.integrationtest.serverport";
    public static final String SYSTEM_PROPERTY_PDS_INTTEST_PORT = "sechub.integrationtest.pdsport";

}

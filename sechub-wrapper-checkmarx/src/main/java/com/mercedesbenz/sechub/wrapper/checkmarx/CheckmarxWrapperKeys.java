package com.mercedesbenz.sechub.wrapper.checkmarx;

import com.mercedesbenz.sechub.commons.pds.PDSVariable;
import com.mercedesbenz.sechub.commons.pds.PDSVariableType;

public enum CheckmarxWrapperKeys implements PDSVariable {

    CHECKMARX_USER(CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_USER, PDSVariableType.ENVIRONMENT_VARIABLE),

    CHECKMARX_PASSOWRD(CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_PASSWORD, PDSVariableType.ENVIRONMENT_VARIABLE),

    CHECKMARX_SERVER_BASE_URL(CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_BASE_URL, PDSVariableType.JOB_PARAMETER_OR_ENVIRONENT_VARIABLE),

    CHECKMARX_ENGINE_CONFIGURATION_NAME(CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME,
            PDSVariableType.JOB_PARAMETER_OR_ENVIRONENT_VARIABLE),

    CHECKMARX_ALWAYS_FULLSCAN_ENABLED(CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_ALWAYS_FULLSCAN_ENABLED,
            PDSVariableType.JOB_PARAMETER_OR_ENVIRONENT_VARIABLE),

    CHECKMARX_RESULT_CHECK_PERIOD_MINUTES(CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESULT_CHECK_PERIOD_MINUTES,
            PDSVariableType.JOB_PARAMETER_OR_ENVIRONENT_VARIABLE),

    CHECKMARX_RESULT_CHECK_TIMOUT_MINUTES(CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESULT_CHECK_TIMOUT_MINUTES,
            PDSVariableType.JOB_PARAMETER_OR_ENVIRONENT_VARIABLE),

    CHECKMARX_MOCKING_ENABLED(CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_MOCKING_ENABLED, PDSVariableType.JOB_PARAMETER_OR_ENVIRONENT_VARIABLE),

    ;

    private String key;
    private PDSVariableType type;

    CheckmarxWrapperKeys(String key, PDSVariableType type) {
        this.key = key;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getVariableId() {
        return getKey();
    }

    @Override
    public PDSVariableType getVariableType() {
        return type;
    }
}

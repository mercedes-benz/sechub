// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.PDSSolutionVariableKey;
import com.mercedesbenz.sechub.commons.pds.PDSSolutionVariableType;

/**
 * An enumeration of the keys used by the Checkmarx wrapper. The default PDS
 * keys are not listed here. They can be found in
 * {@link PDSConfigDataKeyProvider} . All of the keys must be defined at the PDS
 * config file to be available as job parameters!
 *
 * @author Albert Tregnaghi
 *
 */
public enum CheckmarxWrapperKeys implements PDSSolutionVariableKey {
    /* @formatter:off */
    CHECKMARX_USER(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_USER,
            PDSSolutionVariableType.MANDATORY_JOB_PARAMETER,
            "The user name used to communicate with Checkmarx. You can use env:$YOUR_USER_VARIABLENAME to use environment variables instead of real credentials."
            ),

    CHECKMARX_PASSOWRD(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_PASSWORD,
            PDSSolutionVariableType.MANDATORY_JOB_PARAMETER,
            "The password used to communicate with Checkmarx. You can use env:$YOUR_PWD_VARIABLENAME to use environment variables instead of real credentials."
            ),

    CHECKMARX_SERVER_BASE_URL(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_BASE_URL,
            PDSSolutionVariableType.MANDATORY_JOB_PARAMETER,
            "The base URL of the Checkmarx server."
            ),

    CHECKMARX_ENGINE_CONFIGURATION_NAME(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "The engine to use - when empty, the default engine will be used."),

    CHECKMARX_ALWAYS_FULLSCAN_ENABLED(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_ALWAYS_FULLSCAN_ENABLED,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "When 'true', Checkmarx will do a full scan and not a delta scan."),

    CHECKMARX_RESULT_CHECK_PERIOD_MILLISECONDS(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESULT_CHECK_PERIOD_MILLISECONDS,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "The time period in milliseconds when the next check for Checkmarx resuls will be done. An example: If you define `180000`,"
            + "every 3 minutes (3*60*1000 milliseconds) PDS will check the current state of the job."
            ),

    CHECKMARX_RESULT_CHECK_TIMOUT_MINUTES(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESULT_CHECK_TIMOUT_MINUTES,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "The maximum time in minutes when the checkmarx communication does time out and the connection will be terminated."),

    CHECKMARX_MOCKING_ENABLED(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_MOCKING_ENABLED,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "When 'true' than, instead of the real Checkmarx adapter, a mock adapter will be used. This"
            + " is only necessary for tests."),

    CHECKMARX_TEAM_ID_MAPPING(
            CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID,
            PDSSolutionVariableType.MANDATORY_JOB_PARAMETER,
            "Can be either defined directly as job parameter (json mapping), or we can send it automatically by reusing an existing SecHub mapping. As an example: '"+PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_MAPPINGS+"="+
            CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID+","+CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID+"'"),

    CHECKMARX_PRESET_ID_MAPPING(
            CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "If not the default preset Id shall be used, it can be either defined directly as job paramter (json mapping), or we can send it automatically by reusing an existing SecHub mapping. As an example: '"+PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_MAPPINGS+"="+
            CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID+","+CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID+"'"),


    CHECKMARX_RESILIENCE_BAD_REQUEST_MAX_RETRIES(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_BAD_REQUEST_MAX_RETRIES,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "Maximum amounts of retries for bad request handling"),

    CHECKMARX_RESILIENCE_BAD_REQUEST_RETRY_WAIT_MILLISECONDS(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_BAD_REQUEST_RETRY_WAIT_MILLISECONDS,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "Time in milliseconds to wait before next retry when a bad request happend"),

    CHECKMARX_RESILIENCE_SERVER_ERROR_MAX_RETRIES(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_SERVER_ERROR_MAX_RETRIES,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "Maximum amounts of retries for internal server error handling"),

    CHECKMARX_RESILIENCE_SERVER_ERROR_RETRY_WAIT_MILLISECONDS(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_SERVER_ERROR_RETRY_WAIT_MILLISECONDS,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "Time in milliseconds to wait before next retry when an internal server error happend"),

    CHECKMARX_RESILIENCE_NETWORK_ERROR_MAX_RETRIES(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_NETWORK_ERROR_MAX_RETRIES,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "Maximum amounts of retries for network error handling"),

    CHECKMARX_RESILIENCE_NETWORK_EROR_RETRY_WAIT_MILLISECONDS(
            CheckmarxWrapperKeyConstants.KEY_PDS_CHECKMARX_RESILIENCE_NETWORK_EROR_RETRY_WAIT_MILLISECONDS,
            PDSSolutionVariableType.OPTIONAL_JOB_PARAMETER,
            "Time in milliseconds to wait before next retry when a network error happend"),




    /* @formatter:on */
    ;

    private String key;
    private PDSSolutionVariableType type;
    private String description;

    CheckmarxWrapperKeys(String key, PDSSolutionVariableType type, String description) {
        this.key = key;
        this.type = type;
        this.description = description;
    }

    @Override
    public String getVariableKey() {
        return key;
    }

    @Override
    public PDSSolutionVariableType getVariableType() {
        return type;
    }

    @Override
    public String getVariableDescription() {
        return description;
    }
}

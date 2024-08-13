// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import com.epages.restdocs.apispec.Schema;

enum OpenApiSchema {
    MAPPING_CONFIGURATION("MappingConfiguration"),

    JOB_STATUS("JobStatus"),

    USER_DETAILS("UserDetails"),

    USER_LIST("ListOfUsers"),

    SIGNUP_LIST("ListOfSignups"), SCAN_JOB("ScanJob"),

    JOB_ID("JobId"),

    SECHUB_REPORT("SecHubReport"),

    MOCK_DATA_CONFIGURATION("MockDataConfiguration"),

    PROJECT_WHITELIST("ProjectWhitelistUpdate"),

    PROJECT("Project"), PROJECT_LIST("ListOfProjects"),

    PROJECT_DETAILS("ProjectDetails"),

    EXECUTION_PROFILE("ExecutionProfile"),

    EXECUTOR_CONFIGURATION("ExecutorConfiguration"),

    EXECUTOR_CONFIGURATION_WITH_UUID("ExecutorConfigurationWithUUID"),

    EXECUTOR_CONFIGURATION_ID("ExecutorConfigurationId"),

    EXECUTOR_CONFIGURATION_LIST("ListOfExecutorConfigurations"),

    EXECUTION_PROFILE_LIST("ListOfExecutionProfiles"),

    EXECUTION_PROFILE_FETCH("ExecutionProfileFetch"),

    EXECUTION_PROFILE_UPDATE("ExecutionProfileUpdate"),

    EXECUTION_PROFILE_CREATE("ExecutionProfileCreate"),

    STATUS_INFORMATION("StatusInformation"),

    RUNNING_JOB_LIST("ListOfRunningJobs"),

    FALSE_POSITIVES("FalsePositives"),

    FULL_SCAN_DATA_ZIP("FullScanDataZIP"),

    USER_SIGNUP("UserSignup"),

    PROJECT_SCAN_LOGS("ProjectScanLogs"),

    PROJECT_META_DATA("ProjectMetaData"),

    SERVER_RUNTIME_DATA("ServerRuntimeData"),

    PROJECT_JOB_LIST("ProjectJobList"),

    ENCRYPTION_STATUS("EncryptionStatus"),

    ;

    private final Schema schema;

    private OpenApiSchema(String schemaName) {
        schema = new Schema(schemaName);
    }

    Schema getSchema() {
        return schema;
    }
}

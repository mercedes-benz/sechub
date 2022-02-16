// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

/**
 * Log constants to use. Attention: MDC variants must be used wise! If you do
 * not cleanup MDC or set always to correct values next logs can contain old MDC
 * data. MDC is thread local so when threads are reused...
 *
 * @author Albert Tregnaghi
 *
 */
public class LogConstants {

    public static final String MDC_SECHUB_JOB_UUID = "sechub_job_uuid";

    public static final String MDC_SECHUB_PROJECT_ID = "sechub_project_id";

    /**
     * Log constant for MDC audit logs, reserved for audit log service only
     */
    public static final String MDC_SECHUB_AUDIT_USERID = "sechub_audit_userid";
}

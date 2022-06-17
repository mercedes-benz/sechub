// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

/**
 * Log constants to use. Attention: MDC variants must be used wise! If you do
 * not cleanup MDC or set always to correct values next logs can contain old MDC
 * data. MDC is thread local so when threads are reused...
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSLogConstants {

    public static final String MDC_SECHUB_JOB_UUID = "sechub_job_uuid";

    public static final String MDC_PDS_JOB_UUID = "pds_job_uuid";
}

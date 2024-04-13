// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

public class PDSProductExecutorKeyConstants {

    private static final String SECHUB_PRODUCT_EXECUTOR_PDS_PREFIX = "sechub.productexecutor.pds.";

    public static final String TIME_TO_WAIT_NEXT_CHECK_MILLIS = SECHUB_PRODUCT_EXECUTOR_PDS_PREFIX + "timetowait.nextcheck.milliseconds";

    public static final String TIME_OUT_IN_MINUTES = SECHUB_PRODUCT_EXECUTOR_PDS_PREFIX + "timeout.minutes";

    public static final String TRUST_ALL_CERTIFICATES = SECHUB_PRODUCT_EXECUTOR_PDS_PREFIX + "trustall.certificates";

    public static final String ADAPTER_RESILIENCE_RETRY_MAX = SECHUB_PRODUCT_EXECUTOR_PDS_PREFIX + "adapter.resilience.retry.max";

    public static final String ADAPTER_RESILIENCE_RETRY_WAIT_MILLISECONDS = SECHUB_PRODUCT_EXECUTOR_PDS_PREFIX + "adapter.resilience.retry.wait.milliseconds";

    public static final String FORBIDDEN_TARGET_TYPE_PREFIX = SECHUB_PRODUCT_EXECUTOR_PDS_PREFIX + "forbidden.targettype.";

}

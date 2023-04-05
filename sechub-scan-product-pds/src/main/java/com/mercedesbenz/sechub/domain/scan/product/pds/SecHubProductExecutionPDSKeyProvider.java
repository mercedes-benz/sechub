// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import com.mercedesbenz.sechub.commons.pds.PDSKeyProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;

/**
 * These providers/keys are used by sechub PDS product executors at runtime
 * while communicating with PDS servers.
 *
 * @author Albert Tregnaghi
 *
 */
public enum SecHubProductExecutionPDSKeyProvider implements PDSKeyProvider<SecHubProductExecutionPDSKey> {

    PDS_FORBIDS_TARGETTYPE_INTERNET(createSupportTargetType(NetworkTargetType.INTERNET)),

    PDS_FORBIDS_TARGETTYPE_INTRANET(createSupportTargetType(NetworkTargetType.INTRANET)),

    TIME_TO_WAIT_FOR_NEXT_CHECKOPERATION_IN_MILLISECONDS(new AdapterSetupPDSKey(PDSProductExecutorKeyConstants.TIME_TO_WAIT_NEXT_CHECK_MILLIS,
            "When this is set, the value will be used to wait for next check on PDS server. If not, the default from PDS install set up is used instead.")),

    TIME_TO_WAIT_BEFORE_TIMEOUT_IN_MINUTES(new AdapterSetupPDSKey(PDSProductExecutorKeyConstants.TIME_OUT_IN_MINUTES,
            "When this is set, the value will be used to wait before timeout happens when no communication with PDS server is possible. If not, the default from PDS install set up is used instead.")),

    TRUST_ALL_CERTIFICATES(new AdapterSetupPDSKey(PDSProductExecutorKeyConstants.TRUST_ALL_CERTIFICATES,
            "When 'true' then all certificates are accepted. Do not use this in production!")),

    ;

    private SecHubProductExecutionPDSKey key;

    SecHubProductExecutionPDSKeyProvider(SecHubProductExecutionPDSKey key) {
        notNull(key, "Key may not be null!");
        this.key = key;
    }

    public SecHubProductExecutionPDSKey getKey() {
        return key;
    }

    private static ForbiddenTargetTypePDSKey createSupportTargetType(NetworkTargetType type) {
        return new ForbiddenTargetTypePDSKey("pds.productexecutor.forbidden.targettype." + type.name().toLowerCase(),
                "When this key is set to true, then this PDS instance does not scan " + type.name() + "!", type);
    }

}

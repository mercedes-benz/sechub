// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.domain.scan.TargetType;
import static com.daimler.sechub.sharedkernel.util.Assert.*;

/**
 * Enumeration of keys providers for keys used by product executors to define communication between SecHub and PDS.
 * @author Albert Tregnaghi
 *
 */
public enum PDSProductExecutorKeyProvider implements PDSSecHubConfigDataKeyProvider<PDSProductExecutorKey>{

    PDS_FORBIDS_TARGETTYPE_INTERNET(createSupportTargetType(TargetType.INTERNET)),

    PDS_FORBIDS_TARGETTYPE_INTRANET(createSupportTargetType(TargetType.INTRANET)),

    TIME_TO_WAIT_FOR_NEXT_CHECKOPERATION(new PDSProductAdapterKey("pds.productexecutor.timetowait.nextcheck.minutes",
            "When this is set the value will be used to wait for next check on PDS server. If not, the default from PDS install set up is used instead.")), 
    
    TIME_TO_WAIT_BEFORE_TIMEOUT(new PDSProductAdapterKey("pds.productexecutor.timeout.minutes",
            "When this is set the value will be used to wait before timeout happens happens when no communication with PDS server is possible. If not, the default from PDS install set up is used instead.")), 
    
    TRUST_ALL_CERTIFICATES(new PDSProductAdapterKey("pds.productexecutor.trustall.certificates", "When 'true' then all certificates are accepted. Do not use this in production!")), 
    
    ;

    private PDSProductExecutorKey key;

    PDSProductExecutorKeyProvider(PDSProductExecutorKey key) {
        notNull(key, "Key may not be null!");
        this.key = key;
    }

    public PDSProductExecutorKey getKey() {
        return key;
    }

    private static PDSForbiddenTargetTypeInputKey createSupportTargetType(TargetType type) {
        return new PDSForbiddenTargetTypeInputKey("pds.productexecutor.forbidden.targettype." + type.name().toLowerCase(),
                "When this key is set to true, than this pds instance does not scan " + type.name() + "!", type);
    }

}

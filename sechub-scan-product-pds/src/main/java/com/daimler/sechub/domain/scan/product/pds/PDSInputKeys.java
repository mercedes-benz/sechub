package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.domain.scan.TargetType;

/**
 * Enumeration of input keys used to define communication between SecHub and PDS.
 * @author Albert Tregnaghi
 *
 */
public enum PDSInputKeys implements PDSSecHubConfigDataKeyProvider<PDSInputKey>{

    PDS_FORBIDS_TARGETTYPE_INTERNET(createSupportTargetType(TargetType.INTERNET)),

    PDS_FORBIDS_TARGETTYPE_INTRANET(createSupportTargetType(TargetType.INTRANET)),

    TIME_TO_WAIT_FOR_NEXT_CHECKOPERATION(new PDSInputKey("pds2sechub.timetowait.nextcheck.minutes",
            "When this is set the value will be used to wait for next check on PDS server. If not, the default from PDS install set up is used instead.")), 
    
    TIME_TO_WAIT_BEFORE_TIMEOUT(new PDSInputKey("pds2sechub.timeout.minutes",
            "When this is set the value will be used to wait before timeout happens happens when no communication with PDS server is possible. If not, the default from PDS install set up is used instead.")), 
    
    TRUST_ALL_CERTRIFICATES(new PDSInputKey("pds2sechub.trustall.certificates", "When 'true' then all certificates are accepted. Do not use this in production!")), 
    
    ;

    private PDSInputKey key;

    PDSInputKeys(PDSInputKey key) {
        this.key = key;
    }

    public PDSInputKey getKey() {
        return key;
    }

    private static PDSForbiddenTargetTypeInputKey createSupportTargetType(TargetType type) {
        return new PDSForbiddenTargetTypeInputKey("pds2sechub.forbidden.targettype." + type.name(),
                "When this key is set to false, than this pds instance does not scan " + type.name() + "!", type);
    }

}

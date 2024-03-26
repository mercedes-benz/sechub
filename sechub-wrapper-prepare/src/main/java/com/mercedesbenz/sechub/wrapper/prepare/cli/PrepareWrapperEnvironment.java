// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

@Component
public class PrepareWrapperEnvironment {

    /********************************/
    /* PDS common environment setup */
    /********************************/

    @Value("${" + PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION + "}")
    private String sechubConfigurationModelAsJson;

    /**********************************************/
    /* Prepare wrapper specific environment setup */
    /**********************************************/

    @Value("${" + PrepareWrapperKeyConstants.KEY_PDS_PREPARE_REMOTE_CREDENTIAL_CONFIGURATION + "}")
    private String remoteCredentialConfigurationAsJSON;

    public String getRemoteCredentialConfigurationAsJSON() {
        return remoteCredentialConfigurationAsJSON;
    }

    public String getSechubConfigurationModelAsJson() {
        return sechubConfigurationModelAsJson;
    }

}

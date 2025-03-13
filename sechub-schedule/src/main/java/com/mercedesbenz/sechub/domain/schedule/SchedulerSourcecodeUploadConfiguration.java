// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@Component
public class SchedulerSourcecodeUploadConfiguration {

    @Value("${sechub.server.upload.validate.zip:true}")
    @MustBeDocumented(value = "With `false` ZIP validation on sechub server side is disabled. ZIP validation must be done by the delegated security products! You should disable the validation only for testing security product behaviours!", scope = SCOPE_JOB)
    private boolean validateZip;

    @MustBeDocumented(value = "With `false` source code checksum validation (sha256) on sechub server side is disabled. Sha256 validation must be done by the delegated security products! You should disable the validation only for testing security product behaviours!", scope = SCOPE_JOB)
    @Value("${sechub.server.upload.validate.checksum:true}")
    private boolean validateChecksum;

    public boolean isZipValidationEnabled() {
        return validateZip;
    }

    public boolean isChecksumValidationEnabled() {
        return validateChecksum;
    }

}
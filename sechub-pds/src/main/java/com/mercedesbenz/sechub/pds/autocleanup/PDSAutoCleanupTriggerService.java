// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import static com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConstants.*;
import static com.mercedesbenz.sechub.pds.usecase.PDSDocumentationScopeConstants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseSystemExecutesAutoCleanup;

@Service
public class PDSAutoCleanupTriggerService {

    @Autowired
    PDSAutoCleanupService autoCleanupService;

    @PDSMustBeDocumented(value = TRIGGER_STEP_MUST_BE_DOCUMENTED, scope = SCOPE_AUTO_CLEANUP)
    @Scheduled(initialDelayString = TRIGGER_INITIAL_DELAY_STRING, fixedDelayString = TRIGGER_FIXED_DELAY_STRING)
    @UseCaseSystemExecutesAutoCleanup(@PDSStep(number = TRIGGER_STEP_NUMBER, name = TRIGGER_STEP_NAME, description = TRIGGER_STEP_DESCRIPTION))
    public void triggerAutoCleanup() {
        autoCleanupService.cleanup();
    }
}

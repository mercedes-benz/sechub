// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.autocleanup;

import static com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupConstants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.autocleanup.UseCaseScheduleAutoCleanExecution;

@Service
public class ScheduleAutoCleanupTriggerService {

    @Autowired
    ScheduleAutoCleanupService autoCleanupService;

    @MustBeDocumented(value = TRIGGER_STEP_MUST_BE_DOCUMENTED, scope = DocumentationScopeConstants.SCOPE_AUTO_CLEANUP)
    @Scheduled(initialDelayString = TRIGGER_INITIAL_DELAY_STRING, fixedDelayString = TRIGGER_FIXED_DELAY_STRING)
    @UseCaseScheduleAutoCleanExecution(@Step(number = TRIGGER_STEP_NUMBER, name = TRIGGER_STEP_NAME, description = TRIGGER_STEP_DESCRIPTION))
    public void triggerAutoCleanup() {
        autoCleanupService.cleanup();
    }
}

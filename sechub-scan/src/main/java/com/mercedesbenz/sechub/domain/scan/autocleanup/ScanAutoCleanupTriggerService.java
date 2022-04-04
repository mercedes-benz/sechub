// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.autocleanup;

import static com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupConstants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.autocleanup.UseCaseScanAutoCleanExecution;

@Service
public class ScanAutoCleanupTriggerService {

    @Autowired
    ScanAutoCleanupService autoCleanupService;

    @MustBeDocumented(TRIGGER_STEP_MUST_BE_DOCUMENTED)
    @Scheduled(initialDelayString = TRIGGER_INITIAL_DELAY_STRING, fixedDelayString = TRIGGER_FIXED_DELAY_STRING)
    @UseCaseScanAutoCleanExecution(@Step(number = TRIGGER_STEP_NUMBER, name = TRIGGER_STEP_NAME, description = TRIGGER_STEP_DESCRIPTION))
    public void triggerAutoCleanup() {
        autoCleanupService.cleanup();
    }
}

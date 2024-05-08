// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.test.sechub.domain.testonly;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.test.SimulatedDomainMessageService;

/**
 * Simulation to check if stacktrace caller identification works - only
 * necessary for sender
 *
 * @author Albert Tregnaghi
 *
 */
public class SimulatedCaller {

    private SimulatedDomainMessageService simulatedDomainMessageService;

    public SimulatedCaller(SimulatedDomainMessageService simulatedDomainMessageService) {
        this.simulatedDomainMessageService = simulatedDomainMessageService;
    }

    public void simulateCallerSendAsync(int inspectId, DomainMessage domainMessage) {
        simulatedDomainMessageService.simulateServiceSendAsync(inspectId, domainMessage);
    }

    public void simulateCallereSendSync(int inspectId, DomainMessage domainMessage) {
        simulatedDomainMessageService.simulateServiceSendSync(inspectId, domainMessage);
    }
}

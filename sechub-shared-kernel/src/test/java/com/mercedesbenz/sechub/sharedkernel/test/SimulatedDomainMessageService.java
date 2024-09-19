// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.test;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.EventInspector;

public class SimulatedDomainMessageService {

    private EventInspector inspectorService;

    public SimulatedDomainMessageService(EventInspector inspectorService) {
        this.inspectorService = inspectorService;
    }

    public void simulateServiceSendAsync(int inspectId, DomainMessage domainMessage) {
        inspectorService.inspectSendAsynchron(domainMessage, inspectId);
    }

    public void simulateServiceSendSync(int inspectId, DomainMessage domainMessage) {
        inspectorService.inspectSendSynchron(domainMessage, inspectId);
    }
}
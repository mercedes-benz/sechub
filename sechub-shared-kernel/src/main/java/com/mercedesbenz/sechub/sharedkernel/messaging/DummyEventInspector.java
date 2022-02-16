// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

/**
 * This inspector does nothing
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile("!" + Profiles.INTEGRATIONTEST)
public class DummyEventInspector implements EventInspector {

    private int UNUSED_ID = -1;

    @Override
    public int createInspectionId() {
        /* always return same identifier */
        return UNUSED_ID;
    }

    @Override
    public void inspectReceiveSynchronMessage(DomainMessage request, int inspectId, SynchronMessageHandler handler) {
    }

    @Override
    public void inspectReceiveAsynchronMessage(DomainMessage request, int inspectId, AsynchronMessageHandler handler) {
    }

    @Override
    public void inspectSendSynchron(DomainMessage request, int inspectId) {
    }

    @Override
    public void inspectSendAsynchron(DomainMessage request, int inspectId) {
    }

}

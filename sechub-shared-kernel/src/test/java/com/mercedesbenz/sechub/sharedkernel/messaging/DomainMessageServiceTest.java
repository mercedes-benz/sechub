// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DomainMessageServiceTest {

    @Test
    public void smokeTest_sendAsynchron() {
        /* prepare */
        List<AsynchronMessageHandler> injectedAsynchronousHandlers = new ArrayList<>();
        List<SynchronMessageHandler> injectedSynchronousHandlers = new ArrayList<>();

        DomainMessageService service = new DomainMessageService(injectedSynchronousHandlers, injectedAsynchronousHandlers);
        service.eventInspector = new DummyEventInspector();

        DomainMessage request = new DomainMessage(MessageID.USER_REMOVED_FROM_PROJECT);

        /* execute */
        service.sendAsynchron(request);

    }

}

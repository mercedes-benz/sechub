// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

public interface SynchronMessageHandler {

    /**
     * Handles synchronous message. Important: You must add handling methods with
     * {@link IsRecevingSyncMessage} or {@link IsRecevingSyncMessages} otherwise the
     * handler will not be called by internal framework!
     *
     * @param request
     * @return
     */
    public DomainMessageSynchronousResult receiveSynchronMessage(DomainMessage request);

}

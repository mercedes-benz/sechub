// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

public interface AsynchronMessageHandler {

    /*
     * TODO Albert Tregnaghi, 2018-07-25: what about using annotations also to
     * remove the boiler plate code for this method + dispatching case statements ?
     */

    /**
     * Handles asynchronous message. Important: You must add handling methods with
     * {@link IsReceivingAsyncMessage} otherwise the handler will not be called by
     * internal framework!
     *
     * @param request
     */
    public void receiveAsyncMessage(DomainMessage request);

}

// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

/**
 * Implementations inspect event receiving and sending
 * 
 * @author Albert Tregnaghi
 *
 */
public interface EventInspector {

    /**
     * Creates a new inspection id
     * 
     * @return new inspection id
     */
    int createInspectionId();

    /**
     * Inspects that new send synchron event is triggered
     * 
     * @param request
     * @paran inspectId
     */
    void inspectSendSynchron(DomainMessage request, int inspectId);

    /**
     * Inspects that new send synchron event is triggered
     * 
     * @param request
     * @param inspectId
     */
    void inspectSendAsynchron(DomainMessage request, int inspectId);

    /**
     * Inspect that given request is received by given handler
     * 
     * @param request
     * @param inspectId
     * @param handler
     */
    void inspectReceiveSynchronMessage(DomainMessage request, int inspectId, SynchronMessageHandler handler);

    /**
     * Inspect that given request is received by given handler
     * 
     * @param request
     * @param inspectId
     * @param handler
     */
    void inspectReceiveAsynchronMessage(DomainMessage request, int inspectId, AsynchronMessageHandler handler);

}

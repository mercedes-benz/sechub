// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

/**
 * This is only possible when sending synchronous events. This class represents
 * a result of synchronous event.
 *
 * @author Albert Tregnaghi
 *
 */
public class DomainMessageSynchronousResult extends DomainMessagePart {

    private String errorMessage;
    private boolean failed;

    public DomainMessageSynchronousResult(MessageID id) {
        super(id);
    }

    public DomainMessageSynchronousResult(MessageID id, Throwable t) {
        super(id);
        if (t != null) {
            this.failed = true;
            this.errorMessage = t.getMessage();
        }
    }

    public boolean hasFailed() {
        return failed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

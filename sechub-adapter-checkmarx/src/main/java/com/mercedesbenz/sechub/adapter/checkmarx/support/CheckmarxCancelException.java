// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

/**
 * This exception is thrown if the job in the Checkmarx queue could not be
 * canceled.
 *
 * Since the SecHub job will be canceled this exception will always lead to a
 * non-failing result.
 */
public class CheckmarxCancelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CheckmarxCancelException(String checkMarxMessage) {
        super(checkMarxMessage);
    }

    public String getCheckmarxMessage() {
        return getMessage();
    }

}

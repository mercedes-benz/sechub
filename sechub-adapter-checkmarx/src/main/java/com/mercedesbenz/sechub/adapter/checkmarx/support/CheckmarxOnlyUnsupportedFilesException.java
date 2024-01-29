// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx.support;

/**
 * This is a marker exception, which occurs only when none of the uploaded files
 * can be handled by Checkmarx.
 *
 * This kind of exception should always lead to a non failing result. The upload
 * was done, but the files are just not supported.
 *
 * @author Albert Tregnaghi
 *
 */
public class CheckmarxOnlyUnsupportedFilesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CheckmarxOnlyUnsupportedFilesException(String checkMarxMessage) {
        super(checkMarxMessage);
    }

    public String getCheckmarxMessage() {
        return getMessage();
    }
}
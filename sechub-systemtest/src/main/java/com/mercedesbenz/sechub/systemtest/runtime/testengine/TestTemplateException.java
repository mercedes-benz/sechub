// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

/**
 * An exception which occurs when a template contains failures
 *
 * @author Albert Tregnaghi
 *
 */
public class TestTemplateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TestTemplateException(String message) {
        super(message);
    }

}

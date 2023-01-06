// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core;

public interface FailableRunnable<E extends Exception> {

    public void runOrFail() throws E;
}

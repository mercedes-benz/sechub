// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core;

public interface RunOrFail<E extends Exception> {

    public void runOrFail() throws E;
}

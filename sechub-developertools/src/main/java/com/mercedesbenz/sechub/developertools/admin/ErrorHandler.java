// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin;

public interface ErrorHandler {

    public void resetErrors();

    public void handleError(String message);

    public boolean hasErrors();

}

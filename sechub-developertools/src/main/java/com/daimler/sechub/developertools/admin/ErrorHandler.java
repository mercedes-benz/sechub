// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin;

public interface ErrorHandler {

    public void resetErrors();

    public void handleError(String message);

    public boolean hasErrors();

}

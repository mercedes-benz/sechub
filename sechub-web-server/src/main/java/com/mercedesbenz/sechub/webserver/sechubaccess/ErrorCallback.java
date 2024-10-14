// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.sechubaccess;

public interface ErrorCallback<T> {
    T handleExceptionAndReturnFallback(Exception e);
}
package com.mercedesbenz.sechub.webui.sechubaccess;

public interface ErrorCallback<T> {
    T handleExceptionAndReturnFallback(Exception e);
}
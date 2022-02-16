// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.core;

public class StorageException extends RuntimeException {

    private static final long serialVersionUID = 4711156650442491478L;

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
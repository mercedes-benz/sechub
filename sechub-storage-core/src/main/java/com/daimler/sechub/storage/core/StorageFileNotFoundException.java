// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.core;

public class StorageFileNotFoundException extends StorageException {

    private static final long serialVersionUID = -8773166342983291310L;

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
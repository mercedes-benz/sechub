// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

public class InitializationVector {

    private byte[] initializationBytes;

    // Only for tests and internal usage
    InitializationVector() {
        this(new byte[] {});
    }

    public InitializationVector(byte[] bytes) {
        this.initializationBytes = bytes;
    }

    public byte[] getInitializationBytes() {
        return initializationBytes;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + hashCode();
    }
}
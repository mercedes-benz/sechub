// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import java.util.Arrays;

public class InitializationVector {

    private byte[] initializationBytes;

    public InitializationVector(byte[] bytes) {
        this.initializationBytes = bytes;
    }

    public byte[] getInitializationBytes() {
        return initializationBytes;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + hashCode();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(initializationBytes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InitializationVector other = (InitializationVector) obj;
        return Arrays.equals(initializationBytes, other.initializationBytes);
    }
}
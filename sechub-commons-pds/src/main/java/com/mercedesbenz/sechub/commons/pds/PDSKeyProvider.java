// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

public interface PDSKeyProvider<T extends PDSKey> {

    public T getKey();

}

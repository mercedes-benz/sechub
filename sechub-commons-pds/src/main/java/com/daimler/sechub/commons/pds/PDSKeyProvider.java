// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.pds;

public interface PDSKeyProvider<T extends PDSKey> {

    public T getKey(); 

}

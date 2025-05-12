package com.mercedesbenz.sechub.commons.core.security;

import java.io.Serializable;

public interface CryptoAccessProvider<T extends Serializable> {

    /**
     * @return crypto access object, never <code>null</code>
     */
    public CryptoAccess<T> getCryptoAccess();
}

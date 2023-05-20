package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.io.Serializable;

import javax.crypto.SealedObject;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

public class ProtectedData<T extends Serializable> {
    private CryptoAccess<T> protectedDataAccess;
    private SealedObject protectedData;
    
    private ProtectedData(T object) {
        protectedDataAccess = new CryptoAccess<T>();

        protectedData = protectedDataAccess.seal(object);
    }

    public static ProtectedData<String> from(String sensitiveData) {
        return new ProtectedData<String>(sensitiveData);
    }
    
    public T getData() {
        return protectedDataAccess.unseal(protectedData);
    }
    
    public B64String getBase64EncodedData() {
        return null;
    }
}

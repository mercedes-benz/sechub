// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import javax.crypto.SecretKey;

public interface SecretKeyProvider {

    public int getLengthOfSecretInBits();

    public SecretKey getSecretKey();
}

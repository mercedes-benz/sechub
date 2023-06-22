// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

public interface ResilienceContext {

    public Exception getCurrentError();

    public int getAlreadyDoneRetries();

    /**
     * @return callback or <code>null</code>
     */
    public ResilienceCallback getCallback();

    public <V> V getValueOrNull(String key);

    public <V> void setValue(String key, V value);
}

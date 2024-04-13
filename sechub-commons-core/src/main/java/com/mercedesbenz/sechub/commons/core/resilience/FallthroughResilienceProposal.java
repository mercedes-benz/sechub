// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

public interface FallthroughResilienceProposal extends ResilienceProposal {
    /**
     *
     * Defines the amount of time (in milliseconds) where a fall through shall be
     * done. This means, the current error will be simply reused and given back.
     * <br>
     * <br>
     * This is interesting when having a server done, were a time out (e.g. HTTP
     * request) appears only a some time (e.g. 2 minutes). And we got n amount of
     * calls (e.g. 500...) and we do NOT want to have every call wait for 2 Minutes
     * but instead fail fast. So interesting in this case could be a time of
     * 1000*60*2 to do the fall through.
     *
     * @return milliseconds where a fall through shall be done
     */
    public long getMillisecondsForFallThrough();
}

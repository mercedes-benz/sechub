// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IllegalURItargetDetector {

    @Autowired
    LoopbackAddressFinder loopbackfinder;

    public boolean isIllegal(URI uri) {
        if (uri == null) {
            return true;
        }
        String host = uri.getHost();

        if (host == null) {
            return true;
        }
        /* loop back variants are all not a legal target */
        return loopbackfinder.isLoopback(host);
    }
}

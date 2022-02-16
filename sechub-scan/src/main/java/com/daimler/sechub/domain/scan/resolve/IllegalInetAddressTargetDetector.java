// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.resolve;

import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IllegalInetAddressTargetDetector {

    @Autowired
    LoopbackAddressFinder loopbackfinder;

    public boolean isIllegal(InetAddress ip) {
        if (ip == null) {
            return true;
        }
        /* loop back variants are all not a legal target */
        return loopbackfinder.isLoopback(ip);
    }
}

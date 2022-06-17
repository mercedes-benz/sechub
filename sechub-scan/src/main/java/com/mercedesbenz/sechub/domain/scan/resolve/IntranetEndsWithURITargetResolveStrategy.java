// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.resolve;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.scan.NetworkTarget;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;

@Component
public class IntranetEndsWithURITargetResolveStrategy implements URITargetResolveStrategy {

    public static final String PREFIX = "intranet-hostname-ends-with:";
    private Set<String> endsWithList = new LinkedHashSet<String>();

    public boolean initialize(String uriPattern) {
        if (uriPattern == null) {
            return false;
        }
        if (!uriPattern.startsWith(PREFIX)) {
            return false;
        }
        String values = uriPattern.substring(PREFIX.length()).trim();
        if (values.isEmpty()) {
            return false;
        }
        String[] uriEndings = values.split(",");
        for (String uriEnding : uriEndings) {
            endsWithList.add(uriEnding.trim());
        }
        return true;

    }

    @Override
    public NetworkTarget resolveTargetFor(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return new NetworkTarget(uri, NetworkTargetType.UNKNOWN);
        }
        String hostNameLowercased = host.toLowerCase();
        for (String endsWith : endsWithList) {
            if (hostNameLowercased.endsWith(endsWith)) {
                return new NetworkTarget(uri, NetworkTargetType.INTRANET);
            }
        }
        return new NetworkTarget(uri, NetworkTargetType.INTERNET);
    }

}

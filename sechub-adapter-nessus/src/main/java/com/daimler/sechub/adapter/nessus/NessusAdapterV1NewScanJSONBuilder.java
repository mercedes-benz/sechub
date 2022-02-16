// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

public class NessusAdapterV1NewScanJSONBuilder {

    private String uuid;
    private String name;
    private String description;
    LinkedHashSet<URI> targetURIs = new LinkedHashSet<>();
    LinkedHashSet<InetAddress> targetIPs = new LinkedHashSet<>();

    public NessusAdapterV1NewScanJSONBuilder uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public NessusAdapterV1NewScanJSONBuilder name(String name) {
        this.name = name;
        return this;
    }

    public NessusAdapterV1NewScanJSONBuilder description(String description) {
        this.description = description;
        return this;
    }

    public NessusAdapterV1NewScanJSONBuilder targetsURIs(Set<URI> targetURIs) {
        if (targetURIs == null) {
            return this;
        }
        this.targetURIs.addAll(targetURIs);
        return this;
    }

    public NessusAdapterV1NewScanJSONBuilder targetIPs(Set<InetAddress> targetIPs) {
        if (targetIPs == null) {
            return this;
        }
        this.targetIPs.addAll(targetIPs);
        return this;
    }

    public String build() {
        String targets = buildTargetsCommaSeparated();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("  \"uuid\":\"").append(uuid).append("\",");
        sb.append("  \"settings\":{\n");
        sb.append("      \"name\":\"").append(name).append("\",\n");
        sb.append("      \"description\":\"").append(description).append("\",\n");
        sb.append("      \"text_targets\":\"").append(targets).append("\"\n");
        sb.append("  }\n");
        sb.append("}");

        return sb.toString();
    }

    private String buildTargetsCommaSeparated() {
        List<String> simpleTargetList = new ArrayList<>();
        for (URI uri : targetURIs) {
            simpleTargetList.add(uri.toString());
        }
        for (InetAddress ip : targetIPs) {
            simpleTargetList.add(ip.getHostAddress());
        }
        return StringUtils.collectionToCommaDelimitedString(simpleTargetList);
    }

}

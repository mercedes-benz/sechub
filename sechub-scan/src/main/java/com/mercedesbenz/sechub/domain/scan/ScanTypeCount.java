// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;

/**
 * Provides methods to increment and fetch count information of severities for
 * defined scan type
 */
public class ScanTypeCount implements Comparable<ScanTypeCount> {

    private ScanType scanType;
    Map<Severity, AtomicLong> severityToAtomicLongCountMap = new HashMap<>();

    private ScanTypeCount(ScanType scanType) {
        this.scanType = scanType;

        /* init atomic long values */
        for (Severity severity : Severity.values()) {
            severityToAtomicLongCountMap.put(severity, new AtomicLong(0));
        }
    }

    public static ScanTypeCount of(ScanType scanType) {
        if (scanType == null) {
            throw new IllegalArgumentException("ScanType argument must exist");
        }
        return new ScanTypeCount(scanType);
    }

    public ScanType getScanType() {
        return scanType;
    }

    public long getCriticalSeverityCount() {
        return getCount(Severity.CRITICAL);
    }

    public long getHighSeverityCount() {
        return getCount(Severity.HIGH);
    }

    public long getMediumSeverityCount() {
        return getCount(Severity.MEDIUM);
    }

    public long getLowSeverityCount() {
        return getCount(Severity.LOW);
    }

    public long getInfoSeverityCount() {
        return getCount(Severity.INFO);
    }

    public long getUnclassifiedSeverityCount() {
        return getCount(Severity.UNCLASSIFIED);
    }

    @Override
    public int compareTo(ScanTypeCount o) {
        if (o == null) {
            return 1;
        }
        String descriptionA = this.scanType.getDescription();
        String descriptionB = o.scanType.getDescription();

        return descriptionA.compareTo(descriptionB);
    }

    /*
     * Why not making this method public but providing explicit getters? Because of
     * thymeleaf templating. Here usage of enums is sometimes a problem. It is much
     * easier to have dedicated getters... The setter is different, this is done
     * outside.
     */
    private long getCount(Severity severity) {
        return severityToAtomicLongCountMap.get(severity).longValue();
    }

    public void increment(Severity severity) {
        severityToAtomicLongCountMap.get(severity).incrementAndGet();
    }
}

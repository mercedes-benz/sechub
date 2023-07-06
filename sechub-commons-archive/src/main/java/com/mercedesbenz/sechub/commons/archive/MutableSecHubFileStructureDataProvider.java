// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.mercedesbenz.sechub.commons.model.ScanType;

public class MutableSecHubFileStructureDataProvider implements SecHubFileStructureDataProvider {

    private Set<String> acceptedReferenceNames = new LinkedHashSet<>();

    private boolean rootFolderAccepted;

    private Set<String> excludeFilePatterns = new TreeSet<>();

    private Set<String> includeFilePatterns = new TreeSet<>();

    private ScanType scanType;

    public void setRootFolderAccepted(boolean rootFolderAccepted) {
        this.rootFolderAccepted = rootFolderAccepted;
    }

    public void addAcceptedReferenceNames(Collection<String> uniqueNames) {
        if (uniqueNames == null) {
            return;
        }
        this.acceptedReferenceNames.addAll(uniqueNames);
    }

    @Override
    public boolean isRootFolderAccepted() {
        return rootFolderAccepted;
    }

    @Override
    public Set<String> getUnmodifiableSetOfAcceptedReferenceNames() {
        return Collections.unmodifiableSet(acceptedReferenceNames);
    }

    @Override
    public Set<String> getUnmodifiableIncludeFilePatterns() {
        return Collections.unmodifiableSet(includeFilePatterns);
    }

    public void addIncludeFilePatterns(Collection<String> includeFilePatterns) {
        if (includeFilePatterns == null) {
            return;
        }
        this.includeFilePatterns.addAll(includeFilePatterns);
    }

    @Override
    public Set<String> getUnmodifiableExcludeFilePatterns() {
        return Collections.unmodifiableSet(excludeFilePatterns);
    }

    public void addExcludeFilePatterns(Collection<String> excludeFilePatterns) {
        if (excludeFilePatterns == null) {
            return;
        }
        this.excludeFilePatterns.addAll(excludeFilePatterns);
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public ScanType getScanType() {
        return scanType;
    }

}

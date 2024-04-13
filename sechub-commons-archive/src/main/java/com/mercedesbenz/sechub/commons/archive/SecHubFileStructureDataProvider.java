// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.util.Set;

import com.mercedesbenz.sechub.commons.model.ScanType;

/**
 * Provides file structure data for file extraction. It represents a reduced
 * content view for a dedicated {@link ScanType}. It also provides optional
 * information about additionally included or excluded file patterns (e.g. from
 * executor configurations) which can be used to filter at extraction runtime.
 *
 * @author Albert Tregnaghi
 *
 */
public interface SecHubFileStructureDataProvider {

    /**
     * @return <code>true</code> when legacy file structure is supported by the scan
     *         type
     */
    boolean isRootFolderAccepted();

    /**
     * @return {@link ScanType} object, never <code>null</code> for which this file
     *         structure represents the content
     */
    ScanType getScanType();

    Set<String> getUnmodifiableSetOfAcceptedReferenceNames();

    /**
     * Creates a builder for file structure data
     *
     * @return builder
     */
    public static SecHubFileStructureDataProviderBuilder builder() {
        return new SecHubFileStructureDataProviderBuilder();
    }

    Set<String> getUnmodifiableIncludeFilePatterns();

    Set<String> getUnmodifiableExcludeFilePatterns();

}
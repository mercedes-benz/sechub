// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

public class SecHubFileStructureDataProviderIncludeExcludeFilter {

    /**
     * Will check if given path needs to be filtered when data provider content
     * contains dedicated information about excludes and includes
     *
     * @param path         the path to inspect. When <code>null</code> the path is
     *                     always filtered
     * @param dataProvider the data provider which gives information. When
     *                     <code>null</code> the path will never be filtered, except
     *                     when path is <code>null</code>
     * @return <code>true</code> when the given path is NOT accepted, means
     *         filtered.
     */
    public boolean isFiltered(String path, SecHubFileStructureDataProvider dataProvider) {
        if (path == null) {
            /* null is never accepted - means always filtered */
            return true;
        }
        if (dataProvider == null) {
            /* no data available - so filter nothing */
            return false;
        }

        if (isFilteredByNotIncluded(path, dataProvider)) {
            return true;
        }
        if (isFilteredByExcluded(path, dataProvider)) {
            return true;
        }
        return false;
    }

    private boolean isFilteredByNotIncluded(String path, SecHubFileStructureDataProvider dataProvider) {

        Set<String> includePatterns = dataProvider.getUnmodifiableIncludeFilePatterns();
        if (includePatterns == null || includePatterns.isEmpty()) {
            return false;
        }

        Set<String> safeIncludePatterns = convertToSetWithoutNullOrEmptyPatterns(includePatterns);
        /* @formatter:on */
        if (safeIncludePatterns.isEmpty()) {
            return false;
        }
        return !isPathMatchedByWildCardsInSet(path, safeIncludePatterns);
    }

    private boolean isFilteredByExcluded(String path, SecHubFileStructureDataProvider dataProvider) {
        Set<String> excludePatterns = dataProvider.getUnmodifiableExcludeFilePatterns();
        if (excludePatterns == null || excludePatterns.isEmpty()) {
            return false;
        }
        Set<String> safeExcludePatterns = convertToSetWithoutNullOrEmptyPatterns(excludePatterns);
        if (safeExcludePatterns.isEmpty()) {
            return false;
        }

        /* @formatter:off */
        return isPathMatchedByWildCardsInSet(path, safeExcludePatterns);
        /* @formatter:on */
    }

    private Set<String> convertToSetWithoutNullOrEmptyPatterns(Set<String> excludePatterns) {
        /* @formatter:off */
        Set<String> collect = excludePatterns.
                stream().
                filter((pattern) -> pattern != null && !pattern.isEmpty()).
                collect(Collectors.toSet());
        /* @formatter:on */
        return collect;
    }

    private boolean isPathMatchedByWildCardsInSet(String path, Set<String> wildCards) {
        if (wildCards == null || wildCards.isEmpty()) {
            return false;
        }

        for (String wildCard : wildCards) {
            if (isPathMatchedByWildCard(wildCard, path)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPathMatchedByWildCard(String wildCard, String path) {
        if (wildCard == null || wildCard.isEmpty()) {
            return false;
        }
        return FilenameUtils.wildcardMatch(path, wildCard, IOCase.INSENSITIVE);
    }

}

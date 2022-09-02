// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.mock;

/**
 * Combination object for an adapter
 *
 * @author Albert Tregnaghi
 *
 */
public class MockedAdapterSetupCombination {

    /**
     * Special identifier for a combination which is a fallback for all not defined
     */
    public static final String ANY_OTHER_TARGET = "{any-other-target}";

    private String id;
    private String mockDataIdentifier;
    private boolean throwsAdapterException;
    private String filePath;

    private long timeToElapseInMilliseconds;

    private boolean mockDataIdentifierUsedAsFolder;
    private boolean needsExistingFolder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMockDataIdentifier() {
        return mockDataIdentifier;
    }

    public void setMockDataIdentifier(String target) {
        this.mockDataIdentifier = target;
    }

    public boolean isThrowsAdapterException() {
        return throwsAdapterException;
    }

    public void setThrowsAdapterException(boolean throwsAdapterException) {
        this.throwsAdapterException = throwsAdapterException;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setTimeToElapseInMilliseconds(long timeToElapse) {
        this.timeToElapseInMilliseconds = timeToElapse;
    }

    public long getTimeToElapseInMilliseconds() {
        return timeToElapseInMilliseconds;
    }

    public boolean isMockDataIdentifierUsedAsFolder() {
        return mockDataIdentifierUsedAsFolder;
    }

    public void setMockDataIdentifierUsedAsFolder(boolean targetIsNeededAsFolderBySecHubClient) {
        this.mockDataIdentifierUsedAsFolder = targetIsNeededAsFolderBySecHubClient;
    }

    public boolean isNeedsExistingFolder() {
        return needsExistingFolder;
    }

    public void setNeedsExistingFolder(boolean targetNeedsExistingData) {
        this.needsExistingFolder = targetNeedsExistingData;
    }

}
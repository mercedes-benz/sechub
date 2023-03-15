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
    private String target;
    private boolean throwsAdapterException;
    private String filePath;

    private long timeToElapseInMilliseconds;

    private boolean targetUsedAsFolder;
    private boolean targetNeedsExistingData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
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

    public boolean isTargetUsedAsFolder() {
        return targetUsedAsFolder;
    }

    public void setTargetUsedAsFolder(boolean targetIsNeededAsFolderBySecHubClient) {
        this.targetUsedAsFolder = targetIsNeededAsFolderBySecHubClient;
    }

    public boolean isTargetNeedsExistingData() {
        return targetNeedsExistingData;
    }

    public void setTargetNeedsExistingData(boolean targetNeedsExistingData) {
        this.targetNeedsExistingData = targetNeedsExistingData;
    }

}
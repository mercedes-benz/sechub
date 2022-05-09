package com.mercedesbenz.sechub.commons.archive;

class MutableArchiveTransformationData implements ArchiveTransformationData {
    private boolean accepted;
    private String wantedPath;

    MutableArchiveTransformationData() {
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setWantedPath(String wantedPath) {
        this.wantedPath = wantedPath;
    }

    @Override
    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public String getChangedPath() {
        return wantedPath;
    }

    @Override
    public boolean isPathChangeWanted() {
        if (wantedPath == null) {
            return false;
        }
        return true;
    }
}
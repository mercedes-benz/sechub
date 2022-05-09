package com.mercedesbenz.sechub.commons.archive;

public interface ArchiveTransformationData {

    /**
     * @return <code>true</code> when the archive element is wanted inside
     *         transformed output
     */
    boolean isAccepted();

    /**
     *
     * @return wanted path if there is a path change wanted, otherwise
     *         <code>null</code>
     */
    String getChangedPath();

    /**
     *
     * @return <code>true</code> when a path change is wanted
     */
    boolean isPathChangeWanted();

}
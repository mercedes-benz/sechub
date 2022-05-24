// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

public class PDSMetaDataID {

    public static final String KEY_TARGET_URI = "pds.webscan.targeturi";

    /*
     * FIXME Albert Tregnaghi, 2022-05-23: this should be only a prefix like done
     * for PDS job uuid! Reason: what happens when we have two different PDS runs on
     * same time? One set the upload done ... next one would also not upload...
     */
    public static final String KEY_FILEUPLOAD_DONE = "pds.fileupload.done";
    public static final String METADATA_KEY_FILEUPLOAD_DONE = "pds.metadata.fileupload.done";

    public static final String KEY_PDS_JOB_UUID_PREFIX = "pds.job.uuid";
}

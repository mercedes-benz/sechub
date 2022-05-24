// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.util.UUID;

public class PDSMetaDataID {

    public static final String KEY_TARGET_URI = "pds.webscan.targeturi";

    private static final String KEY_SOURCE_UPLOAD_DONE_PREFIX = "pds.source.upload.done";
    private static final String KEY_BINARY_UPLOAD_DONE_PREFIX = "pds.binary.upload.done";

    public static String createBinaryUploadDoneKey(UUID pdsJobUUID) {
        return KEY_BINARY_UPLOAD_DONE_PREFIX + "." + pdsJobUUID;
    }

    public static String createSourceUploadDoneKey(UUID pdsJobUUID) {
        return KEY_SOURCE_UPLOAD_DONE_PREFIX + "." + pdsJobUUID;
    }
}

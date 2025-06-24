// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core;

import java.util.Set;

public class CommonConstants {

    public static final String DOT_CHECKSUM = ".checksum";
    public static final String DOT_FILESIZE = ".filesize";

    public final static String FILENAME_SOURCECODE_ZIP = "sourcecode.zip";
    public static final String FILENAME_SOURCECODE_ZIP_CHECKSUM = FILENAME_SOURCECODE_ZIP + DOT_CHECKSUM;
    public static final String FILENAME_SOURCECODE_ZIP_FILESIZE = FILENAME_SOURCECODE_ZIP + DOT_FILESIZE;

    public final static String FILENAME_BINARIES_TAR = "binaries.tar";
    public final static String FILENAME_BINARIES_TAR_CHECKSUM = FILENAME_BINARIES_TAR + DOT_CHECKSUM;
    public final static String FILENAME_BINARIES_TAR_FILESIZE = FILENAME_BINARIES_TAR + DOT_FILESIZE;

    public final static String MULTIPART_FILE = "file";
    public final static String MULTIPART_CHECKSUM = "checkSum";

    public final static String FILE_SIZE_HEADER_FIELD_NAME = "x-file-size";

    /**
     * Reserved reference identifier for / folder outside data section of a
     * sourcecode.zip file
     */
    public static final String SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER = "__sourcecode_archive_root__";

    /**
     * Reserved reference identifier for / folder outside data section of a
     * binaries.tar file
     */
    public static final String BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER = "__binaries_archive_root__";

    public static final Set<String> getAllRootArchiveReferenceIdentifiers() {
        return Set.of(SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER, BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER);
    }

    public static final String META_DATA_KEY_PRODUCT_CANCELED = "PRODUCT_CANCELED";

}

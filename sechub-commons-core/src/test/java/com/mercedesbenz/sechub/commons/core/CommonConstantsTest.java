// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CommonConstantsTest {

    @Test
    void getAllRootArchiveReferenceIdentifiers_contains_expected_content() {
        /* @formatter:off */
       assertThat(CommonConstants.getAllRootArchiveReferenceIdentifiers()).
           contains(CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER).
           contains(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER);
       /* @formatter:on */
    }

    @Test
    void archive_root_identifiers_are_as_expected() {
        assertThat(CommonConstants.SOURCECODE_ARCHIVE_ROOT_REFERENCE_IDENTIFIER).isEqualTo("__sourcecode_archive_root__");
        assertThat(CommonConstants.BINARIES_ARCHIVE_ROOT_REFERENCE_IDENTIFIER).isEqualTo("__binaries_archive_root__");
    }

}

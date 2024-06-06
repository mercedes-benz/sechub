// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FileSizeTest {

    @ParameterizedTest
    @ValueSource(strings = { "10kb", "10KB", "10mb", "10MB", "10gb", "10GB" })
    void testFileSize(final String sizeStr) {
        /* execute */
        final FileSize result = new FileSize(sizeStr);

        /* test */
        assertThat(result.getSizeString(), is(""));
    }

}

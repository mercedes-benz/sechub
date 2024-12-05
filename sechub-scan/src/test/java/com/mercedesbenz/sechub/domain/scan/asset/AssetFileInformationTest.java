// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class AssetFileInformationTest {

    @ParameterizedTest
    @ValueSource(strings = { "test1.txt", "filename", "" })
    @NullSource
    void equals_returns_true_when_filename_and_checksum_are_same(String testText) {
        /* prepare */
        String sameFileName = testText;
        String sameChecksum = testText == null ? null : "checksum_dummy_" + testText;

        AssetFileData info1 = new AssetFileData();
        info1.setChecksum(sameChecksum);
        info1.setFileName(sameFileName);

        AssetFileData info2 = new AssetFileData();
        info2.setChecksum(sameChecksum);
        info2.setFileName(sameFileName);

        /* execute + test */
        assertThat(info1).isEqualTo(info2);
        assertThat(info2).isEqualTo(info1);
    }

    @Test
    void equals_returns_false_when_filenames_are_NOT_same() {
        /* prepare */
        String sameChecksum = "checksum_dummy";

        AssetFileData info1 = new AssetFileData();
        info1.setChecksum(sameChecksum);
        info1.setFileName("filename1");

        AssetFileData info2 = new AssetFileData();
        info2.setChecksum(sameChecksum);
        info2.setFileName("filename2");

        /* execute + test */
        assertThat(info1).isNotEqualTo(info2);
        assertThat(info2).isNotEqualTo(info1);
    }

    @Test
    void equals_returns_false_when_checksums_are_NOT_same() {
        /* prepare */
        String sameFileName = "filename";

        AssetFileData info1 = new AssetFileData();
        info1.setChecksum("checksum-1");
        info1.setFileName(sameFileName);

        AssetFileData info2 = new AssetFileData();
        info2.setChecksum("checksum-2");
        info2.setFileName(sameFileName);

        /* execute + test */
        assertThat(info1).isNotEqualTo(info2);
        assertThat(info2).isNotEqualTo(info1);
    }

    @Test
    void equals_returns_false_when_checksums_and_filename_are_NOT_same() {
        /* prepare */

        AssetFileData info1 = new AssetFileData();
        info1.setChecksum("checksum-1");
        info1.setFileName("file-1");

        AssetFileData info2 = new AssetFileData();
        info2.setChecksum("checksum-2");
        info2.setFileName("file-2");

        /* execute + test */
        assertThat(info1).isNotEqualTo(info2);
        assertThat(info2).isNotEqualTo(info1);
    }

}

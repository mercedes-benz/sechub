package com.mercedesbenz.sechub.wrapper.xray.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZipFileCreatorTest {

    ZipFileCreator creatorToTest;

    @BeforeEach
    void beforeEach() {
        creatorToTest = new ZipFileCreator();
    }

    @Test
    void saveInputStreamToZipFile_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> creatorToTest.zip(null, null));
    }

}
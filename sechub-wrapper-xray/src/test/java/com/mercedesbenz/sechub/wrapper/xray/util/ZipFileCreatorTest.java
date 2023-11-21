package com.mercedesbenz.sechub.wrapper.xray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class ZipFileCreatorTest {

    final String testinputString = "test data";
    InputStream inputStream;
    ZipFileCreator creatorToTest;
    IOHelper IOHelperToTest;

    @BeforeEach
    void beforeEach() {
        inputStream = new ByteArrayInputStream(testinputString.getBytes());
        creatorToTest = new ZipFileCreator();
        IOHelperToTest = new IOHelper();
    }

    @Test
    void readInputStreamAsString_return_content_as_string() throws XrayWrapperException {
        /* execute */
        String content = IOHelperToTest.readInputStreamAsString(inputStream);

        /* test */
        assertEquals(testinputString, content);
    }

    @Test
    void readInputStreamAsString_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> IOHelperToTest.readInputStreamAsString(null));
    }

    @Test
    void readInputStreamAsString_throws_xrayWrapperException() {
        /* prepare */
        InputStream mockedIs = mock(InputStream.class);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> IOHelperToTest.readInputStreamAsString(mockedIs));
    }

    @Test
    void saveInputStreamToZipFile_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> creatorToTest.zip(null, null));
    }

}
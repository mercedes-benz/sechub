package com.mercedesbenz.sechub.wrapper.xray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;

class InputStreamSaverTest {

    final String testinputString = "test data";
    InputStream inputStream;

    @BeforeEach
    void beforeEach() {
        inputStream = new ByteArrayInputStream(testinputString.getBytes());
    }

    @Test
    void readInputStreamAsString_return_content_as_string() throws XrayWrapperException {
        /* execute */
        String content = InputStreamSaver.readInputStreamAsString(inputStream);

        /* test */
        assertEquals(testinputString, content);
    }

    @Test
    void readInputStreamAsString_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> InputStreamSaver.readInputStreamAsString(null));
    }

    @Test
    void readInputStreamAsString_throws_xrayWrapperException() {
        /* prepare */
        InputStream mockedIs = mock(InputStream.class);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> InputStreamSaver.readInputStreamAsString(mockedIs));
    }

    @Test
    void saveInputStreamToZipFile_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> InputStreamSaver.saveInputStreamToZipFile(null, null));
    }

}
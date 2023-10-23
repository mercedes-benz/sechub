package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.readInputStreamAsString;
import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToZipFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;

class InputStreamSaverTest {

    final String testinputString = "test data";
    InputStream is;

    @BeforeEach
    void beforeEach() {
        is = new ByteArrayInputStream(testinputString.getBytes());
    }

    @Test
    void test_saveInputStreamToStringBuilder() throws XrayWrapperRuntimeException {
        /* execute */
        String content = readInputStreamAsString(is);

        /* test */
        assertEquals(testinputString, content);
    }

    @Test
    void test_saveInputStreamToStringBuilder_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> readInputStreamAsString(null));
    }

    @Test
    void test_saveInputStreamToStringBuilder_ioException() {
        /* prepare */
        InputStream mockedIs = mock(InputStream.class);

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> readInputStreamAsString(mockedIs));
    }

    @Test
    void test_saveInputStreamToZipFile_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> saveInputStreamToZipFile(null, null));
    }

}
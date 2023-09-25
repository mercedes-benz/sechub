package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToStringBuilder;
import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToZipFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputStreamSaverTest {

    String testinputString;
    InputStream is;

    @BeforeEach
    void beforeEach() {
        testinputString = "test data";
        is = new ByteArrayInputStream(testinputString.getBytes());
    }

    @Test
    public void test_saveInputStreamToStringBuilder() throws IOException {
        /* prepare */
        String s;

        /* execute */
        s = saveInputStreamToStringBuilder(is);

        /* test */
        assertEquals(testinputString, s);
    }

    @Test
    public void test_saveInputStreamToStringBuilder_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> saveInputStreamToStringBuilder(null));
    }

    @Test
    public void test_saveInputStreamToStringBuilder_ioException() {
        /* prepare */
        InputStream mockedIs = mock(InputStream.class);

        /* execute + test */
        assertThrows(IOException.class, () -> saveInputStreamToStringBuilder(mockedIs));
    }

    @Test
    public void test_saveInputStreamToZipFile_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> saveInputStreamToZipFile(null, null));
    }

}
// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class IOHelperTest {

    String testinputString;
    InputStream testInputStream;
    IOHelper IOHelperToTest;

    @BeforeEach
    void beforeEach() {
        testinputString = "test data";
        testInputStream = new ByteArrayInputStream(testinputString.getBytes());
        IOHelperToTest = new IOHelper();
    }

    @Test
    void readInputStreamAsString_return_content_as_string() throws XrayWrapperException {
        /* execute */
        String content = IOHelperToTest.readInputStreamAsString(testInputStream);

        /* test */
        assertEquals(testinputString, content);
    }

    @Test
    void readInputStreamAsString_throws_xrayWrapperException() {
        /* prepare */
        InputStream mockedIs = mock(InputStream.class);

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> IOHelperToTest.readInputStreamAsString(mockedIs));

        /* test */
        assertEquals("Could not read https input stream as string", exception.getMessage());
        assertEquals(XrayWrapperExitCode.IO_ERROR, exception.getExitCode());
    }

}

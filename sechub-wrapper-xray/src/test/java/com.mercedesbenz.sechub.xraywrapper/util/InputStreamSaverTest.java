package com.mercedesbenz.sechub.xraywrapper.util;

import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToStringBuilder;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.http.XrayAPIResponse;

class InputStreamSaverTest {

    // prepare
    XrayAPIResponse response;
    String testInput;
    InputStream is;

    @BeforeEach
    void beforeEach() {
        response = new XrayAPIResponse();
        testInput = "test data";
        is = new ByteArrayInputStream(testInput.getBytes());
    }

    @Test
    public void testSaveInputStreamToStringBuilder() {
        // prepare
        StringBuilder builder;
        String s;

        // execute
        try {
            builder = saveInputStreamToStringBuilder(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // assert
        s = builder.toString();
        assertEquals(testInput, s);

    }

}
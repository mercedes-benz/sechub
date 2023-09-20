package com.mercedesbenz.sechub.xraywrapper.helper;

import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToStringBuilder;
import static com.mercedesbenz.sechub.xraywrapper.util.InputStreamSaver.saveInputStreamToZipFile;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XrayAPIResponseTest {

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
    public void testSaveZipFile() {
        // todo test not valid for pipeline
        /*
         * // prepare String path = "src/test/resources/test_file"; // execute try {
         * response.saveZipFile(path, is); } catch (IOException e) { throw new
         * RuntimeException(e); }
         *
         * // assert File file = new File(path); assertTrue(file.exists());
         *
         * // clean Boolean b = file.delete();
         *
         */
    }

    // valid input stream and filename
    @Test
    public void testSaveZipFileInvalidPath() {
        // prepare
        String path = "src/test/resourcessssss/test_file";

        // execute
        IOException thrown = assertThrows(IOException.class, () -> saveInputStreamToZipFile(path, is), "IOException");

        // assert
        assertTrue(thrown.getMessage().contains("No such file or directory"));
    }

    // valid input stream
    @Test
    public void testSaveJsonBody() {
        // prepare
        String body = "";

        // execute
        try {
            StringBuilder content = saveInputStreamToStringBuilder(is);
            body = content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // assert
        assertEquals(testInput, body);
    }

}
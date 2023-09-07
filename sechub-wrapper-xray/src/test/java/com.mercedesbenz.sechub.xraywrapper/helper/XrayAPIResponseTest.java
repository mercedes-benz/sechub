package com.mercedesbenz.sechub.xraywrapper.helper;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.junit.jupiter.api.Test;

class XrayAPIResponseTest {

    // prepare
    XrayAPIResponse response = new XrayAPIResponse();
    String testInput = "test data";
    InputStream is = new ByteArrayInputStream(testInput.getBytes());

    // valid input stream and filename
    @Test
    public void testSaveZipFile() {
        // prepare
        String path = "src/test/resources/test_file";
        // execute
        try {
            response.saveZipFile(path, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // assert
        File file = new File(path);
        assertTrue(file.exists());

        // clean
        Boolean b = file.delete();
    }

    // valid input stream and filename
    @Test
    public void testSaveZipFileInvalidPath() {
        // prepare
        String path = "src/test/resourcessssss/test_file";

        // execute
        IOException thrown = assertThrows(IOException.class, () -> response.saveZipFile(path, is), "IOException");

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
            StringBuilder content = response.saveJsonBody(is);
            body = content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // assert
        assertEquals(testInput, body);
    }

}
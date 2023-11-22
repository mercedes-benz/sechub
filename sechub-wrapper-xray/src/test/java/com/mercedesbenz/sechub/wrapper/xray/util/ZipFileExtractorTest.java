package com.mercedesbenz.sechub.wrapper.xray.util;

import static com.mercedesbenz.sechub.test.TestFileReader.loadTextFile;
import static com.mercedesbenz.sechub.test.TestUtil.createTempDirectoryInBuildFolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportException;

class ZipFileExtractorTest {

    ZipFileExtractor zipFileExtractorToTest;

    @BeforeEach
    void beforeEach() {
        zipFileExtractorToTest = new ZipFileExtractor();
    }

    @Test
    void unzipFile_throws_xrayWrapperReportException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> zipFileExtractorToTest.unzipFile(null, null));
    }

    @Test
    void unzipFile_throws_rayWrapperReportException_with_invalid_file() {
        /* prepare */
        File file = new File("file");

        /* execute + test */
        assertThrows(XrayWrapperReportException.class, () -> zipFileExtractorToTest.unzipFile(file.toPath(), file.toPath()));
    }

    @Test
    void unzipFile_with_mocked_paths_and_ZipInputStream() throws XrayWrapperReportException {
        Path source = mock(Path.class);
        Path target = mock(Path.class);
        MockedConstruction<FileInputStream> mockFileInput = mockConstruction(FileInputStream.class);

        try (MockedConstruction<ZipInputStream> mockedClient = mockConstruction(ZipInputStream.class, (mock, context) -> {
            when(mock.getNextEntry()).thenReturn(null);
        })) {
            zipFileExtractorToTest.unzipFile(source, target);
        }
        mockFileInput.close();
    }

    @Test
    void unzipFile_with_zip_files() throws IOException, XrayWrapperReportException {
        /* prepare */
        Path target = createTempDirectoryInBuildFolder("xray-zipFileExtractor");
        Path source = Paths.get("src/test/resources/xray-zip-test/xray-zip-test.zip");
        Path expectedFile01 = Paths.get(target + "/xray-testfile-01.txt");
        Path expectedFile02 = Paths.get(target + "/xray-testfile-02.txt");
        Path expectedFile03 = Paths.get(target + "/xray-test-zip-folder/xray-testfile-03.txt");
        Path expectedFileSomeFile = Paths.get(target + "/xray-test-zip-folder/.somefile");

        /* execute */
        zipFileExtractorToTest.unzipFile(source, target);

        /* test */
        assertTrue(Files.exists(expectedFile01));
        assertTrue(Files.exists(expectedFile02));
        assertTrue(Files.exists(expectedFile03));
        assertTrue(Files.exists(expectedFileSomeFile));

        assertEquals("testfile01", loadTextFile(expectedFile01));
        assertEquals("testfile02", loadTextFile(expectedFile02));
        assertEquals("testfile03", loadTextFile(expectedFile03));
        assertEquals("somefile", loadTextFile(expectedFileSomeFile));
    }
}
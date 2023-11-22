package com.mercedesbenz.sechub.wrapper.xray.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
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
}
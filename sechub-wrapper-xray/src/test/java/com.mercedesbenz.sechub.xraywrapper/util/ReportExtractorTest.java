package xraywrapper.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.util.ReportExtractor;

class ReportExtractorTest {

    String source = "src/test/resources/test_file.zip";
    String target = "src/test/resources/test_file";

    // file exists
    @Test
    public void testFileExists() {
        // execute
        ReportExtractor.fileExists(source);
    }

    // file doesn't exist
    @Test
    public void testFileExistsInvalid() {
        // todo: SystemLamda or Mockito
    }

    @Test
    public void testUnzipReports() {
        // prepare
        Path src = Path.of(source);
        Path trg = Path.of(target);

        // execute
        try {
            ReportExtractor.unzipReports(src, trg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // assert
        assertTrue(Files.exists(Path.of(target)));

        // clean
        try {
            Files.delete(Path.of(target + "/test_file"));
            Files.delete(Path.of(target));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
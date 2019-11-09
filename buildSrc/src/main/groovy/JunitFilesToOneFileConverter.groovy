import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;


public class JunitFilesToOneFileConverter {

    void combineFiles(String sourcePath, String targetFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<testsuites>\n");
        Path path = Paths.get(sourcePath);
        try {
            for (Path file : Files.newDirectoryStream(path)) {
                if (Files.isRegularFile(file)) {
                    try {
                        List<String> x = Files.readAllLines(file);
                        int lineNr = 0;
                        for (String line : x) {
                            lineNr++;
                            if (lineNr == 1) {
                                continue;
                            }
                            sb.append(line);
                            sb.append("\n");
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            sb.append("\n</testsuites>");
            Path targetPath = Paths.get(targetFile);
            targetPath.getParent().toFile().mkdirs();
            Files.deleteIfExists(targetPath);

            BufferedWriter bw = Files.newBufferedWriter(targetPath, Charset.forName("UTF-8"), StandardOpenOption.CREATE_NEW)
            bw.write(sb.toString());
            bw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

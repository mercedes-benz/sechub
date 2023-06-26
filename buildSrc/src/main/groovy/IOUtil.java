// SPDX-License-Identifier: MIT

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

public class IOUtil {

    public static final void createBackupFile(String filePath, String backupPostFix) throws IOException{
        Path sourcePath = Paths.get(filePath);
        Path targetPath = Paths.get(filePath + "_" + backupPostFix);
        
        if (!Files.exists(sourcePath)) {
            return;
        }
        System.out.println("Create backup file: "+targetPath + "\nfrom: "+sourcePath);
        
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public static final void restoreBackupFile(String filePath, String backupPostFix) throws IOException{
        Path targetPath = Paths.get(filePath);
        Path sourcePath = Paths.get(filePath + "_" + backupPostFix);
        if (!Files.exists(sourcePath)) {
            return;
        }
        System.out.println("Restore: "+targetPath + "\nfrom backup file: "+sourcePath);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
}

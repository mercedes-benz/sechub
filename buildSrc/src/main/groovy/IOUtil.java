// SPDX-License-Identifier: MIT

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

public class IOUtil {

    /**
     * Creates a backup copy for the given file (if the file does exist) which
     * can be restored by IOUtil. The location of the backup is handled by IOUtil internally.
     * 
     * @param filePath the path for the file to backup
     * @param backupPostFix a special post fix for the backup file, the backup file has
     *        the same name as the origin one, but with the post fix.
     */
    public static final void createBackupFile(String filePath, String backupPostFix) throws IOException{
        Path sourcePath = Paths.get(filePath);
        Path targetPath = Paths.get(filePath + "_" + backupPostFix);
        
        if (!Files.exists(sourcePath)) {
            return;
        }
        System.out.println("Create backup file: "+targetPath + "\nfrom: "+sourcePath);
        
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Restores a previously created backup to the wanted file path (if a backup exists).
     * The location of the backup is handled by IOUtil internally.
     * 
     * @param filePath the path for the file to restore (not the backup file!)
     * @backupPostFix a special post fix for the backup file
     */
    public static final void restoreBackupFile(String filePath, String backupPostFix) throws IOException{
        Path targetPath = Paths.get(filePath);
        Path sourcePath = Paths.get(filePath + "_" + backupPostFix);
        if (!Files.exists(sourcePath)) {
            return;
        }
        System.out.println("Restore: "+targetPath + "\nfrom backup file: "+sourcePath);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Copy a file to another location
     * @sourcePath source path as string
     * @targetPath target path as string
     */
    public static final void copyFile(String sourcePath, String targetPath) throws IOException{
        
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);
        
        target.toFile().getParentFile().mkdirs();
        
        System.out.println("Copy: "+source + "\nto  : "+target);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}

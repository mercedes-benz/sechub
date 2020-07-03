package com.daimler.sechub.pds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PDSFileUnzipSupport {

    private static final Logger LOG = LoggerFactory.getLogger(PDSFileUnzipSupport.class);

    public void unzipArchive(File file, File destDir) throws IOException {
        if (! file.exists()) {
            LOG.error("cannot unzip {} because zip file does not exist!",file.getAbsolutePath());
            return;
        }
        if (! destDir.exists()) {
            destDir.mkdirs();
        }
        
        LOG.debug("start unzipping of {} into {}",file.getAbsolutePath(), destDir.getAbsolutePath());
        byte[] buffer = new byte[1024];
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
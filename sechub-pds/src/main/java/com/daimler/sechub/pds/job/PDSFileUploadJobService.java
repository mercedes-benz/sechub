package com.daimler.sechub.pds.job;

import static com.daimler.sechub.pds.util.PDSAssert.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.sechub.pds.util.PDSFileChecksumSHA256Service;

@Service
public class PDSFileUploadJobService {
    

    private static final Logger LOG = LoggerFactory.getLogger(PDSFileUploadJobService.class);

    private static final String DEFAULT_UPLOAD_BASE_PATH="./uploads/";


    private static final int MAX_FILENAME_LENGTH = 40;
    
    
    @Value("${sechub.pds.upload.basepath:"+DEFAULT_UPLOAD_BASE_PATH+"}")
    String uploadBasePath = DEFAULT_UPLOAD_BASE_PATH;

    @Autowired
    PDSFileChecksumSHA256Service checksumService;
    
    public void upload(UUID jobUUID, String fileName, MultipartFile file, String checkSum) {
        notNull(jobUUID, "job uuid may not be null");
        notNull(file, "file may not be null");
        notNull(checkSum, "checkSum may not be null");
        validateFileName(fileName);
        
        File jobFolder = ensureJobFolder(jobUUID);
        File uploadFile = new File(jobFolder,fileName);
        
        try {
            FileUtils.copyInputStreamToFile(file.getInputStream(), uploadFile);
        } catch (IOException e) {
            LOG.error("Was not able to store {} for job {}, reason:",fileName,jobUUID,e.getMessage());
            throw new IllegalArgumentException("Cannot store given file",e);
        }
        
        checksumService.hasCorrectChecksum(checkSum, uploadFile.getAbsolutePath());
    }
    
    public void deleteAllUploads(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null");
        
        File jobFolder = ensureJobFolder(jobUUID);
        try {
            FileUtils.deleteDirectory(jobFolder);
        } catch (IOException e) {
            LOG.error("Was not able to delete uploads for job {}, reason:",jobUUID,e.getMessage());
            throw new IllegalArgumentException("Cannot store given file",e);
        }
    }
    
    /* sanity check to avoid path traversal etc.*/
    private void validateFileName(String fileName) {
       notNull(fileName, "filename may not be null!");
       if (fileName.length()>MAX_FILENAME_LENGTH) {
           throw new IllegalArgumentException("filename exceeds maximum length of "+MAX_FILENAME_LENGTH+" chars");
       }
       for (char c: fileName.toCharArray()) {
           boolean accepted = Character.isDigit(c) || Character.isAlphabetic(c);
           accepted = accepted || c=='-' || c=='_' || c=='.';
           if (!accepted) {
               throw new IllegalArgumentException("filename contains illegal characters. Allowed is only [a-zA-Z\\.-_] maximum length of "+MAX_FILENAME_LENGTH+" chars");
           }
       }
    }

    private File ensureJobFolder(UUID jobUUID) {
        File file = new File(uploadBasePath,jobUUID.toString());
        file.mkdirs();
        return file;
    }

}

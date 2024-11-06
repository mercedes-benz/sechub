// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFile.AssetFileCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUploadsAssetFile;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.AssetStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.ServletOutputStream;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class AssetService {

    private static final Logger LOG = LoggerFactory.getLogger(AssetService.class);

    private AssetFileRepository repository;

    private UserInputAssertion inputAssertion;

    private CheckSumSupport checkSumSupport;

    private StorageService storageService;

    /* @formatter:off */
    AssetService(
            @Autowired AssetFileRepository repository,
            @Autowired UserInputAssertion inputAssertion,
            @Autowired CheckSumSupport checkSumSupport,
            @Autowired StorageService storageService
            ) {
        this.repository=repository;
        this.inputAssertion=inputAssertion;
        this.checkSumSupport=checkSumSupport;
        this.storageService=storageService;
    }
    /* @formatter:on */

    @UseCaseAdminUploadsAssetFile(@Step(number = 2, name = "Service tries to upload file for asset", description = "Uploaded file will be stored in database and in storage"))
    public void uploadAssetFile(String assetId, MultipartFile file, String checkSum) {
        inputAssertion.assertIsValidAssetId(assetId);

        inputAssertion.assertIsValidSha256Checksum(checkSum);

        String fileName = assertAssetFile(file);

        handleChecksumValidation(fileName, file, checkSum, assetId);

        try {
            /* now store */
            persistFileAndChecksumInDatabase(fileName, file, checkSum, assetId);

            storeUploadFileAndSha256Checksum(assetId, fileName, file, checkSum);
            
            LOG.info("Successfully uploaded asset file: {} for asset: {}", fileName, assetId);
            
        } catch (IOException e) {
            throw new SecHubRuntimeException("Was not able to upload file:" + fileName + " for asset:" + assetId, e);
        }
    }

    private void persistFileAndChecksumInDatabase(String fileName, MultipartFile file, String checkSum, String assetId) throws IOException {
        /* delete if exists */
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();
        repository.deleteById(key);

        AssetFile assetFile = new AssetFile(key);
        assetFile.setChecksum(checkSum);
        assetFile.setData(file.getBytes());
        
        repository.save(assetFile);
    }

    private String assertAssetFile(MultipartFile file) {
        notNull(file, "file may not be null!");
        String fileName = file.getName();

        inputAssertion.assertIsValidAssetFileName(fileName);

        long fileSize = file.getSize();

        if (fileSize <= 0) {
            throw new BadRequestException("Uploaded asset file may not be empty!");
        }
        return fileName;
    }

    private void handleChecksumValidation(String fileName, MultipartFile file, String checkSum, String assetid) {
        try (InputStream inputStream = file.getInputStream()) {
            /* validate */
            assertCheckSumCorrect(checkSum, inputStream);

        } catch (IOException e) {
            LOG.error("Was not able to validate uploaded file checksum for file: {} in asset: {}", fileName, assetid, e);
            throw new SecHubRuntimeException("Was not able to validate uploaded asset checksum");
        }
    }

    private void assertCheckSumCorrect(String checkSum, InputStream inputStream) {
        if (!checkSumSupport.hasCorrectSha256Checksum(checkSum, inputStream)) {
            LOG.error("Uploaded file has incorrect sha256 checksum! Something must have happened during the upload.");
            throw new NotAcceptableException("Sourcecode checksum check failed");
        }
    }

    private void storeUploadFileAndSha256Checksum(String assetId, String fileName, MultipartFile file, String checkSum) {
        AssetStorage assetStorage = storageService.createAssetStorage(assetId);
        try {
            store(assetId, fileName, file, checkSum, assetStorage);
        } finally {
            assetStorage.close();
        }
    }

    private void store(String assetId, String fileName, MultipartFile file, String checkSum, AssetStorage assetStorage) {

        try (InputStream inputStream = file.getInputStream()) {

            long fileSize = file.getSize();
            assetStorage.store(fileName, inputStream, fileSize);

            long checksumSizeInBytes = checkSum.getBytes().length;
            assetStorage.store(createFileNameForChecksum(fileName), new StringInputStream(checkSum), checksumSizeInBytes);

        } catch (IOException e) {
            LOG.error("Was not able to store file: {} for asset: {}!", fileName, assetId, e);
            throw new SecHubRuntimeException("Was not able to upload file:" + fileName + " for asset: " + assetId);
        }
    }

    private String createFileNameForChecksum(String fileName) {
        return fileName + DOT_CHECKSUM;
    }

    public void downloadAssetFile(String assetId, String fileName, ServletOutputStream outputStream) throws IOException {
        inputAssertion.assertIsValidAssetId(assetId);
        inputAssertion.assertIsValidAssetFileName(fileName);
        
        notNull(outputStream, "output stream may not be null!");
        
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();
        Optional<AssetFile> result = repository.findById(key);
        if (result.isEmpty()) {
            throw new NotFoundException("For asset:"+assetId+" no file with name:"+fileName+" exists!");
        }
        AssetFile assetFile = result.get();
        outputStream.write(assetFile.getData());
        
    }

}

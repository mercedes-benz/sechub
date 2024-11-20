// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.commons.core.ConfigurationFailureException;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFile.AssetFileCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesAssetCompletely;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesOneFileFromAsset;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDownloadsAssetFile;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAssetDetails;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAssetIds;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUploadsAssetFile;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.AssetStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

import jakarta.servlet.ServletOutputStream;

@Service
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
    public void uploadAssetFile(String assetId, MultipartFile multipartFile, String checkSum) {
        inputAssertion.assertIsValidAssetId(assetId);

        inputAssertion.assertIsValidSha256Checksum(checkSum);

        String fileName = assertAssetFile(multipartFile);

        handleChecksumValidation(fileName, multipartFile, checkSum, assetId);

        try {
            /* now store */
            byte[] bytes = multipartFile.getBytes();
            persistFileAndChecksumInDatabase(fileName, bytes, checkSum, assetId);

            ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase(fileName, assetId);

            LOG.info("Successfully uploaded file '{}' for asset '{}'", fileName, assetId);

        } catch (IOException e) {
            throw new SecHubRuntimeException("Was not able to upload file '" + fileName + "' for asset '" + assetId + "'", e);
        } catch (ConfigurationFailureException e) {
            throw new IllegalStateException("A configuration failure should not happen at this point!", e);
        }
    }

    /**
     * Ensures file for asset exists in database and and also in storage (with same
     * checksum). If the file is not inside database a {@link NotFoundException}
     * will be thrown. If the file is not available in storage, or the checksum in
     * storage is different than the checksum from database, the file will stored
     * again in storage (with data from database)
     *
     * @param fileName file name
     * @param assetId  asset identifier
     * @throws ConfigurationFailureException if there are configuration problems
     * @throws NotFoundException             when the asset or the file is not found
     *                                       in database
     *
     */
    public void ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase(String fileName, String assetId) throws ConfigurationFailureException {

        try (AssetStorage assetStorage = storageService.createAssetStorage(assetId)) {
            AssetFile assetFile = assertAssetFileFromDatabase(assetId, fileName);
            String checksumFromDatabase = assetFile.getChecksum();

            if (assetStorage.isExisting(fileName)) {
                String checksumFileName = fileName + DOT_CHECKSUM;
                if (assetStorage.isExisting(checksumFileName)) {

                    String checksumFromStorage = null;
                    try (InputStream inputStream = assetStorage.fetch(checksumFileName); Scanner scanner = new Scanner(inputStream)) {
                        checksumFromStorage = scanner.hasNext() ? scanner.next() : "";
                    }
                    if (checksumFromStorage.equals(checksumFromDatabase)) {
                        LOG.debug("Checksum for file '{}' in asset '{}' is '{}' in database and storage. Can be kept, no recration necessary", fileName,
                                assetId, checksumFromStorage, checksumFromDatabase);
                        return;
                    }
                    LOG.warn(
                            "Checksum for file '{}' in asset '{}' was '{}' instead of expected value from database '{}'. Will recreated file and checksum in storage.",
                            fileName, assetId, checksumFromStorage, checksumFromDatabase);
                } else {
                    LOG.warn("Asset storage for file '{}' in asset '{}' did exist, but checksum did not exist. Will recreated file and checksum in storage.",
                            fileName, assetId);
                }
            } else {
                LOG.info("Asset storage for file '{}' in asset '{}' does not exist and must be created.", fileName, assetId);
            }
            storeStream(fileName, checksumFromDatabase, assetStorage, assetFile.getData().length, new ByteArrayInputStream(assetFile.getData()));

        } catch (NotFoundException | IOException e) {
            throw new ConfigurationFailureException("Was not able to ensure file " + fileName + " in asset " + assetId, e);
        }

    }

    private void persistFileAndChecksumInDatabase(String fileName, byte[] bytes, String checkSum, String assetId) throws IOException {
        /* delete if exists */
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();
        repository.deleteById(key);

        AssetFile assetFile = new AssetFile(key);
        assetFile.setChecksum(checkSum);
        assetFile.setData(bytes);

        repository.save(assetFile);
    }

    private String assertAssetFile(MultipartFile file) {
        notNull(file, "file may not be null!");
        String fileName = file.getOriginalFilename();

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
            LOG.error("Was not able to validate uploaded file checksum for file '{}' in asset '{}'", fileName, assetid, e);
            throw new SecHubRuntimeException("Was not able to validate uploaded asset checksum");
        }
    }

    private void assertCheckSumCorrect(String checkSum, InputStream inputStream) {
        if (!checkSumSupport.hasCorrectSha256Checksum(checkSum, inputStream)) {
            LOG.error("Uploaded file has incorrect sha256 checksum! Something must have happened during the upload.");
            throw new NotAcceptableException("Sourcecode checksum check failed");
        }
    }

    private void storeStream(String fileName, String checkSum, AssetStorage assetStorage, long fileSize, InputStream inputStream) throws IOException {
        assetStorage.store(fileName, inputStream, fileSize);

        long checksumSizeInBytes = checkSum.getBytes().length;
        assetStorage.store(createFileNameForChecksum(fileName), new StringInputStream(checkSum), checksumSizeInBytes);
    }

    private String createFileNameForChecksum(String fileName) {
        return fileName + DOT_CHECKSUM;
    }

    @UseCaseAdminDownloadsAssetFile(@Step(number = 2, name = "Service downloads asset file from database"))
    public void downloadAssetFile(String assetId, String fileName, ServletOutputStream outputStream) throws IOException {
        inputAssertion.assertIsValidAssetId(assetId);
        inputAssertion.assertIsValidAssetFileName(fileName);

        notNull(outputStream, "output stream may not be null!");

        AssetFile assetFile = assertAssetFileFromDatabase(assetId, fileName);
        outputStream.write(assetFile.getData());

    }

    private AssetFile assertAssetFileFromDatabase(String assetId, String fileName) {
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();
        Optional<AssetFile> result = repository.findById(key);
        if (result.isEmpty()) {
            throw new NotFoundException("For asset:" + assetId + " no file with name:" + fileName + " exists!");
        }
        AssetFile assetFile = result.get();
        return assetFile;
    }

    @UseCaseAdminFetchesAssetIds(@Step(number = 2, name = "Service fetches all asset ids from database"))
    public List<String> fetchAllAssetIds() {
        return repository.fetchAllAssetIds();
    }

    /**
     * Fetches asset details (from database)
     *
     * @param assetId asset identifier
     * @return detail data
     * @throws NotFoundException when no asset exists for given identifier
     */
    @UseCaseAdminFetchesAssetDetails(@Step(number = 2, name = "Service fetches asset details for given asset id"))
    public AssetDetailData fetchAssetDetails(String assetId) {
        inputAssertion.assertIsValidAssetId(assetId);

        List<AssetFile> assetFiles = repository.fetchAllAssetFilesWithAssetId(assetId);
        if (assetFiles.isEmpty()) {
            throw new NotFoundException("No asset data available for asset id:" + assetId);
        }

        AssetDetailData data = new AssetDetailData();
        data.setAssetId(assetId);
        for (AssetFile assetFile : assetFiles) {
            AssetFileData information = new AssetFileData();
            information.setFileName(assetFile.getKey().getFileName());
            information.setChecksum(assetFile.getChecksum());
            data.getFiles().add(information);
        }

        return data;
    }

    @UseCaseAdminDeletesOneFileFromAsset(@Step(number = 2, name = "Services deletes file from asset"))
    public void deleteAssetFile(String assetId, String fileName) throws IOException {
        inputAssertion.assertIsValidAssetId(assetId);
        inputAssertion.assertIsValidAssetFileName(fileName);

        repository.deleteById(AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build());
        storageService.createAssetStorage(assetId).delete(fileName);
    }

    @UseCaseAdminDeletesAssetCompletely(@Step(number = 2, name = "Services deletes all asset parts"))
    @Transactional
    public void deleteAsset(String assetId) throws IOException {
        inputAssertion.assertIsValidAssetId(assetId);

        repository.deleteAssetFilesHavingAssetId(assetId);
        storageService.createAssetStorage(assetId).deleteAll();
    }

}

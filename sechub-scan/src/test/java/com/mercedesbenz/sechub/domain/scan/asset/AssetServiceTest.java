// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.core.ConfigurationFailureException;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFile.AssetFileCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.storage.core.AssetStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

class AssetServiceTest {

    private StorageService storageService;
    private AssetFileRepository repository;
    private UserInputAssertion inputAssertion;
    private CheckSumSupport checkSumSupport;
    private AssetService serviceToTest;

    @BeforeEach
    void beforeEach() {

        storageService = mock();
        repository = mock();
        inputAssertion = mock();

        serviceToTest = new AssetService(repository, inputAssertion, checkSumSupport, storageService);
    }

    @Test
    void fetchAllAssetIds_returns_result_from_repo() {
        /* prepare */
        when(repository.fetchAllAssetIds()).thenReturn(List.of("asset1","asset2"));

        /* execute */
        List<String> result = serviceToTest.fetchAllAssetIds();

        /* test */
        verify(repository).fetchAllAssetIds();
        assertThat(result).contains("asset1","asset2");
    }

    @Test
    void fetchAssetDetails_returns_details_based_on_repo_info() {
        /* prepare */
        String assetId = "test-asset-1";

        AssetFile assetFile1 = createAssetFileMock("file1.txt", "checksum1");
        AssetFile assetFile2 = createAssetFileMock("file2.txt", "checksum2");

        when(repository.fetchAllAssetFilesWithAssetId(assetId)).thenReturn(List.of(assetFile1, assetFile2));

        /* execute */
        AssetDetailData result = serviceToTest.fetchAssetDetails(assetId);

        /* test */
        verify(repository).fetchAllAssetFilesWithAssetId(assetId);
        assertThat(result.getAssetId()).isEqualTo(assetId);

        AssetFileData expectedInfo1 = new AssetFileData();
        expectedInfo1.setChecksum("checksum1");
        expectedInfo1.setFileName("file1.txt");

        AssetFileData expectedInfo2 = new AssetFileData();
        expectedInfo2.setChecksum("checksum2");
        expectedInfo2.setFileName("file2.txt");

        assertThat(result.getFiles()).contains(expectedInfo1, expectedInfo2);
    }

    @Test
    void ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase_throws_config_failure_when_not_in_database() throws Exception {
        /* prepare */
        String assetId = "asset1";
        String fileName = "file1.zip";
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();

        when(repository.findById(key)).thenReturn(Optional.empty());

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase(fileName, assetId))
                .isInstanceOf(ConfigurationFailureException.class);

    }

    @Test
    void ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase_not_in_storage_creates_it() throws Exception {
        /* prepare */
        String assetId = "asset1";
        String fileName = "file1.zip";
        String content = "testbytes";
        long contentSize = content.length();
        String checksum = "checksum";
        long checksumSize = checksum.length();
        byte[] bytes = content.getBytes();
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();

        AssetFile assetFile = mock();
        when(assetFile.getChecksum()).thenReturn(checksum);
        when(assetFile.getData()).thenReturn(bytes);
        when(assetFile.getKey()).thenReturn(key);

        when(repository.findById(key)).thenReturn(Optional.of(assetFile));
        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage(assetId)).thenReturn(assetStorage);

        when(assetStorage.isExisting(fileName)).thenReturn(false);

        /* execute */
        serviceToTest.ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase(fileName, assetId);

        /* test */
        verify(storageService).createAssetStorage(assetId);
        verify(assetStorage).isExisting(fileName);

        ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.captor();
        verify(assetStorage).store(eq(fileName), streamCaptor.capture(), eq(contentSize));
        assertThat(streamCaptor.getValue()).hasBinaryContent(bytes);

        ArgumentCaptor<InputStream> streamCaptor2 = ArgumentCaptor.captor();
        verify(assetStorage).store(eq(fileName + ".checksum"), streamCaptor2.capture(), eq(checksumSize));
        assertThat(streamCaptor2.getValue()).hasContent(checksum);

    }

    @Test
    void ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase_file_in_storage_but_no_checksum_fie_creates_it() throws Exception {
        /* prepare */
        String assetId = "asset1";
        String fileName = "file1.zip";
        String content = "testbytes";
        long contentSize = content.length();
        String checksum = "checksum";
        long checksumSize = checksum.length();
        byte[] bytes = content.getBytes();
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();

        AssetFile assetFile = mock();
        when(assetFile.getChecksum()).thenReturn(checksum);
        when(assetFile.getData()).thenReturn(bytes);
        when(assetFile.getKey()).thenReturn(key);

        when(repository.findById(key)).thenReturn(Optional.of(assetFile));
        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage(assetId)).thenReturn(assetStorage);

        when(assetStorage.isExisting(fileName)).thenReturn(true);
        when(assetStorage.isExisting(fileName + ".checksum")).thenReturn(false);

        /* execute */
        serviceToTest.ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase(fileName, assetId);

        /* test */
        verify(storageService).createAssetStorage(assetId);
        verify(assetStorage).isExisting(fileName);

        ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.captor();
        verify(assetStorage).store(eq(fileName), streamCaptor.capture(), eq(contentSize));
        assertThat(streamCaptor.getValue()).hasBinaryContent(bytes);

        ArgumentCaptor<InputStream> streamCaptor2 = ArgumentCaptor.captor();
        verify(assetStorage).store(eq(fileName + ".checksum"), streamCaptor2.capture(), eq(checksumSize));
        assertThat(streamCaptor2.getValue()).hasContent(checksum);

    }

    @Test
    void ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase_in_storage_but_checksum_difers_recreates_it() throws Exception {
        /* prepare */
        String assetId = "asset1";
        String fileName = "file1.zip";
        String content = "testbytes";
        long contentSize = content.length();
        String checksum = "checksum";
        long checksumSize = checksum.length();
        byte[] bytes = content.getBytes();
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();

        AssetFile assetFile = mock();
        when(assetFile.getChecksum()).thenReturn(checksum);
        when(assetFile.getData()).thenReturn(bytes);
        when(assetFile.getKey()).thenReturn(key);

        when(repository.findById(key)).thenReturn(Optional.of(assetFile));
        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage(assetId)).thenReturn(assetStorage);

        when(assetStorage.isExisting(fileName)).thenReturn(true);
        when(assetStorage.isExisting(fileName + ".checksum")).thenReturn(true);
        when(assetStorage.fetch(fileName + ".checksum")).thenReturn(mock());

        /* execute */
        serviceToTest.ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase(fileName, assetId);

        /* test */
        verify(storageService).createAssetStorage(assetId);
        verify(assetStorage).isExisting(fileName);

        ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.captor();
        verify(assetStorage).store(eq(fileName), streamCaptor.capture(), eq(contentSize));
        assertThat(streamCaptor.getValue()).hasBinaryContent(bytes);

        ArgumentCaptor<InputStream> streamCaptor2 = ArgumentCaptor.captor();
        verify(assetStorage).store(eq(fileName + ".checksum"), streamCaptor2.capture(), eq(checksumSize));
        assertThat(streamCaptor2.getValue()).hasContent(checksum);

    }

    @Test
    void ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase_in_storage_and_checksum_same_no_recreation() throws Exception {
        /* prepare */
        String assetId = "asset1";
        String fileName = "file1.zip";
        String content = "testbytes";
        String checksum = "checksum";
        byte[] bytes = content.getBytes();
        AssetFileCompositeKey key = AssetFileCompositeKey.builder().assetId(assetId).fileName(fileName).build();

        AssetFile assetFile = mock();
        when(assetFile.getChecksum()).thenReturn(checksum);
        when(assetFile.getData()).thenReturn(bytes);
        when(assetFile.getKey()).thenReturn(key);

        when(repository.findById(key)).thenReturn(Optional.of(assetFile));
        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage(assetId)).thenReturn(assetStorage);

        when(assetStorage.isExisting(fileName)).thenReturn(true);
        when(assetStorage.isExisting(fileName + ".checksum")).thenReturn(true);
        when(assetStorage.fetch(fileName + ".checksum")).thenReturn(new ByteArrayInputStream(checksum.getBytes()));

        /* execute */
        serviceToTest.ensureAssetFileInStorageAvailableAndHasSameChecksumAsInDatabase(fileName, assetId);

        /* test */
        verify(storageService).createAssetStorage(assetId);
        verify(assetStorage).isExisting(fileName);
        verify(assetStorage).isExisting(fileName + ".checksum");

        verify(assetStorage, never()).store(anyString(), any(InputStream.class), anyLong());

    }

    @Test
    void delete_non_existing_single_asset_file_throws_exception() throws IOException {
        /* prepare */
        String assetId = "asset1";
        String fileName = "file1.txt";

        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage(assetId)).thenReturn(assetStorage);
        when(repository.deleteSingleAssetFileHavingAssetId(fileName, assetId)).thenReturn(0);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.deleteAssetFile(assetId, fileName)).isInstanceOf(NotFoundException.class).hasMessageContaining(assetId)
                .hasMessageContaining(fileName);

        /* test */
        verify(repository).deleteSingleAssetFileHavingAssetId(fileName, assetId);
        verify(storageService).createAssetStorage(assetId);
        verify(assetStorage).delete(fileName);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 10, 231 })
    void when_given_file_for_given_assetid_was_deleted_no_exception_is_thrown(int numberOfDeletedEntries) throws IOException {
        /* prepare */
        String assetId = "asset1";
        String fileName = "file1.txt";

        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage(assetId)).thenReturn(assetStorage);
        when(repository.deleteSingleAssetFileHavingAssetId(fileName, assetId)).thenReturn(numberOfDeletedEntries);

        /* execute */
        serviceToTest.deleteAssetFile(assetId, fileName);

        /* test */
        verify(repository).deleteSingleAssetFileHavingAssetId(fileName, assetId);
        verify(storageService).createAssetStorage(assetId);
        verify(assetStorage).delete(fileName);
    }

    @Test
    void delete_non_existing_asset_throws_exception() throws IOException {
        /* prepare */
        String assetId = "asset1";

        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage(assetId)).thenReturn(assetStorage);
        when(repository.deleteAssetFilesHavingAssetId(assetId)).thenReturn(0);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.deleteAsset(assetId)).isInstanceOf(NotFoundException.class).hasMessageContaining(assetId);

        /* test */
        verify(repository).deleteAssetFilesHavingAssetId(assetId);
        verify(storageService).createAssetStorage(assetId);
        verify(assetStorage).deleteAll();
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 10, 231 })
    void when_at_least_one_file_for_given_assetid_was_deleted_no_exception_is_thrown(int numberOfDeletedEntries) throws IOException {
        /* prepare */
        String assetId = "asset1";

        AssetStorage assetStorage = mock();
        when(storageService.createAssetStorage(assetId)).thenReturn(assetStorage);
        when(repository.deleteAssetFilesHavingAssetId(assetId)).thenReturn(numberOfDeletedEntries);

        /* execute + test */
        serviceToTest.deleteAsset(assetId);

        /* test */
        verify(repository).deleteAssetFilesHavingAssetId(assetId);
        verify(storageService).createAssetStorage(assetId);
        verify(assetStorage).deleteAll();
    }

    private AssetFile createAssetFileMock(String fileName, String checksum) {
        AssetFileCompositeKey mockKey1 = mock(AssetFileCompositeKey.class);
        when(mockKey1.getFileName()).thenReturn(fileName);

        AssetFile assetFile1 = mock(AssetFile.class);
        when(assetFile1.getChecksum()).thenReturn(checksum);
        when(assetFile1.getKey()).thenReturn(mockKey1);
        return assetFile1;
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFile.AssetFileCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
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

    private AssetFile createAssetFileMock(String fileName, String checksum) {
        AssetFileCompositeKey mockKey1 = mock(AssetFileCompositeKey.class);
        when(mockKey1.getFileName()).thenReturn(fileName);

        AssetFile assetFile1 = mock(AssetFile.class);
        when(assetFile1.getChecksum()).thenReturn(checksum);
        when(assetFile1.getKey()).thenReturn(mockKey1);
        return assetFile1;
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;

import com.mercedesbenz.sechub.domain.scan.asset.AssetFile.AssetFileCompositeKey;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileRepositoryDBTest.SimpleTestConfiguration;

@DataJpaTest
@ContextConfiguration(classes = { AssetFile.class, AssetFileRepository.class, SimpleTestConfiguration.class })
class AssetFileRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AssetFileRepository repositoryToTest;

    @Test
    void fetchAllAssetIds_no_assets_exist() throws Exception {

        /* execute */
        List<String> result = repositoryToTest.fetchAllAssetIds();

        /* test */
        assertThat(result).isEmpty();

    }

    @Test
    void fetchAllAssetIds() throws Exception {

        /* prepare */
        AssetFile file1 = new AssetFile(AssetFileCompositeKey.builder().assetId("asset1").fileName("file1").build());
        file1.setChecksum("pseudo-checksum");
        file1.setData("testdata1".getBytes());
        entityManager.persist(file1);

        AssetFile file2a = new AssetFile(AssetFileCompositeKey.builder().assetId("asset2").fileName("file2a").build());
        file2a.setChecksum("pseudo-checksum");
        file2a.setData("testdata2a".getBytes());
        entityManager.persist(file2a);

        AssetFile file2b = new AssetFile(AssetFileCompositeKey.builder().assetId("asset2").fileName("file2b").build());
        file2b.setChecksum("pseudo-checksum");
        file2b.setData("testdata2b".getBytes());
        entityManager.persist(file2b);

        /* execute */
        List<String> result = repositoryToTest.fetchAllAssetIds();

        /* test */
        assertThat(result).contains("asset1", "asset2").hasSize(2);

    }

    @Test
    void fetchAllAssetFilesWithAssetId() throws Exception {

        /* prepare */
        AssetFile file1 = new AssetFile(AssetFileCompositeKey.builder().assetId("asset1").fileName("file1").build());
        file1.setChecksum("pseudo-checksum1");
        file1.setData("testdata1".getBytes());
        entityManager.persist(file1);

        AssetFile file2a = new AssetFile(AssetFileCompositeKey.builder().assetId("asset2").fileName("file2a").build());
        file2a.setChecksum("pseudo-checksum2a");
        file2a.setData("testdata2a".getBytes());
        entityManager.persist(file2a);

        AssetFile file2b = new AssetFile(AssetFileCompositeKey.builder().assetId("asset2").fileName("file2b").build());
        file2b.setChecksum("pseudo-checksum2b");
        file2b.setData("testdata2b".getBytes());
        entityManager.persist(file2b);

        /* execute */
        List<AssetFile> result = repositoryToTest.fetchAllAssetFilesWithAssetId("asset2");

        /* test */
        assertThat(result).hasSize(2);
        Iterator<AssetFile> iterator = result.iterator();
        AssetFile assetFile2a = iterator.next();
        AssetFile assetFile2b = iterator.next();

        assertThat(assetFile2a.getChecksum()).isEqualTo(file2a.getChecksum());
        assertThat(assetFile2b.getChecksum()).isEqualTo(file2b.getChecksum());
    }

    @Test
    void deleteAssetFilesHavingAssetId_when_asset_does_not_exist() {
        /* execute */
        int numberOfDeletedEntries = repositoryToTest.deleteAssetFilesHavingAssetId("not-existing-asset-id");

        /* test */
        assertEquals(0, numberOfDeletedEntries);
    }

    @Test
    void deleteAssetFilesHavingAssetId() {
        /* prepare */
        AssetFile file1 = new AssetFile(AssetFileCompositeKey.builder().assetId("asset1").fileName("file1").build());
        file1.setChecksum("pseudo-checksum1");
        file1.setData("testdata1".getBytes());
        entityManager.persist(file1);

        String assetId2 = "asset2";
        AssetFile file2a = new AssetFile(AssetFileCompositeKey.builder().assetId(assetId2).fileName("file2a").build());
        file2a.setChecksum("pseudo-checksum2a");
        file2a.setData("testdata2a".getBytes());
        entityManager.persist(file2a);

        AssetFile file2b = new AssetFile(AssetFileCompositeKey.builder().assetId(assetId2).fileName("file2b").build());
        file2b.setChecksum("pseudo-checksum2b");
        file2b.setData("testdata2b".getBytes());
        entityManager.persist(file2b);

        /* execute */
        int numberOfDeletedEntries = repositoryToTest.deleteAssetFilesHavingAssetId(assetId2);

        /* test */
        assertEquals(2, numberOfDeletedEntries);
    }

    @Test
    void deleteSingleAssetFileHavingAssetId_when_asset_does_not_exist() {
        /* execute */
        int numberOfDeletedEntries = repositoryToTest.deleteSingleAssetFileHavingAssetId("file-does-not_exist", "not-existing-asset-id");

        /* test */
        assertEquals(0, numberOfDeletedEntries);
    }

    @Test
    void deleteSingleAssetFileHavingAssetId() {
        /* prepare */
        AssetFile file1 = new AssetFile(AssetFileCompositeKey.builder().assetId("asset1").fileName("file1").build());
        file1.setChecksum("pseudo-checksum1");
        file1.setData("testdata1".getBytes());
        entityManager.persist(file1);

        String assetId2 = "asset2";
        String fileName2a = "file2a";
        AssetFile file2a = new AssetFile(AssetFileCompositeKey.builder().assetId(assetId2).fileName(fileName2a).build());
        file2a.setChecksum("pseudo-checksum2a");
        file2a.setData("testdata2a".getBytes());
        entityManager.persist(file2a);

        AssetFile file2b = new AssetFile(AssetFileCompositeKey.builder().assetId(assetId2).fileName("file2b").build());
        file2b.setChecksum("pseudo-checksum2b");
        file2b.setData("testdata2b".getBytes());
        entityManager.persist(file2b);

        /* execute */
        int numberOfDeletedEntries = repositoryToTest.deleteSingleAssetFileHavingAssetId(fileName2a, assetId2);

        /* test */
        assertEquals(1, numberOfDeletedEntries);
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}

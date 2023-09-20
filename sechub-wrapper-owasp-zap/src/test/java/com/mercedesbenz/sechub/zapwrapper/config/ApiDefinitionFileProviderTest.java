// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;

class ApiDefinitionFileProviderTest {

    private ApiDefinitionFileProvider providerToTest = new ApiDefinitionFileProvider();;

    @Test
    void sources_folder_is_null_results_in_empty_list() {
        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles(null, new SecHubScanConfiguration());

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void sechub_scan_config_is_null_results_in_empty_list() {
        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", null);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void missing_data_section_part_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {"apiVersion":"1.0","webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference"]}}}""";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void empty_sources_section_in_sechub_configuration_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {"apiVersion":"1.0","data":{"sources":[]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference"]}}}""";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void missing_filesystem_part_in_sechub_configuration_results_in_empty_openapi_files_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {"apiVersion":"1.0","data":{"sources":[{"name":"open-api-file-reference"}]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference"]}}}""";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void empty_filesystem_part_in_sechub_configuration_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {"apiVersion":"1.0","data":{"sources":[{"name":"open-api-file-reference","fileSystem":{}}]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference"]}}}""";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void binaries_instead_of_sources_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {"apiVersion":"1.0","data":{"binaries":[{"name":"open-api-file-reference"}]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference"]}}}""";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void folders_instead_of_files_inside_filesystem_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {"apiVersion":"1.0","data":{"sources":[{"name":"open-api-file-reference","fileSystem":{"folders":["openapifolder/"]}}]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference"]}}}""";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("test/path", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void data_section_name_differs_from_use_part_inside_openapi_definition_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {"apiVersion":"1.0","data":{"sources":[{"name":"open-api-file-reference","fileSystem":{"files":["openapi3.json"]}}]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["no-existing-reference"]}}}""";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("test/path", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void valid_sechub_scan_config_with_openapi_definition_file_results_in_list_with_one_file() {
        /* prepare */
        String sechubScanConfigJSON = """
                {"apiVersion":"1.0","data":{"sources":[{"name":"open-api-file-reference","fileSystem":{"files":["openapi3.json"]}}]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference"]}}}""";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("test/path", sechubScanConfiguration);

        /* test */
        assertEquals(1, result.size());
    }

    @ParameterizedTest
    @MethodSource("multipleFilesTestNamedArguments")
    void mutliple_files_result_in_correct_list_of_files(String sechubScanConfigJSON) {
        /* prepare */
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("test/path", sechubScanConfiguration);

        /* test */
        assertEquals(2, result.size());
    }

    static Stream<Arguments> multipleFilesTestNamedArguments() {
        /* @formatter:off */
        String moreThanOneDataSectionName = "Sources part more than one file in 2 data sections";
        String sechubConfigWithmoreThanOneDataSection = """
                {"apiVersion":"1.0","data":{"sources":[{"name":"open-api-file-reference","fileSystem":{"files":["openapi3.json"]}},
                {"name":"second-reference","fileSystem":{"files":["second-openapi-file.json"]}}]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference","second-reference"]}}}""";

        String filesystemPartHasMoreThanOneFileName = "Filesystem files part more than one file";
        String sechubConfigWithfilesystemPartHasMoreThanOneFile = """
                {"apiVersion":"1.0","data":{"sources":[{"name":"open-api-file-reference","fileSystem":{"files":["openapi3.json", "second-file.json"]}}]},
                "webScan":{"url":"https://localhost:8443","api":{"type":"openApi","use":["open-api-file-reference"]}}}""";

        return Stream.of(
              Arguments.of(
                      Named.of(moreThanOneDataSectionName,
                              sechubConfigWithmoreThanOneDataSection)),
              Arguments.of(
                      Named.of(filesystemPartHasMoreThanOneFileName,
                              sechubConfigWithfilesystemPartHasMoreThanOneFile)));
        /* @formatter:on */
    }

}

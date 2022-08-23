// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

class ApiDefinitionFileProviderTest {

    private ApiDefinitionFileProvider providerToTest = new ApiDefinitionFileProvider();;

    @Test
    void sources_folder_is_null_results_in_must_exit_exception() {
        /* execute + test */
        MustExitRuntimeException exception = assertThrows(MustExitRuntimeException.class,
                () -> providerToTest.fetchApiDefinitionFile(null, new SecHubScanConfiguration()));

        assertEquals("Sources folder must not be null!", exception.getMessage());
        assertEquals(MustExitCode.EXECUTION_FAILED, exception.getExitCode());
    }

    @Test
    void sechub_scan_config_is_null_results_in_must_exit_exception() {
        /* execute + test */
        MustExitRuntimeException exception = assertThrows(MustExitRuntimeException.class,
                () -> providerToTest.fetchApiDefinitionFile("/example/path/to/extracted/sources", null));

        assertEquals("SecHub scan config must not be null!", exception.getMessage());
        assertEquals(MustExitCode.EXECUTION_FAILED, exception.getExitCode());
    }

    @Test
    void missing_data_section_part_results_in_must_exit_runtime_exception() {
        /* prepare */
        String sechubScanConfigJSON = "{\"apiVersion\":\"1.0\","
                + "\"webScan\":{\"uri\":\"https://localhost:8443\",\"api\":{\"type\":\"openApi\",\"use\":[\"open-api-file-reference\"]}}}";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute + test */
        MustExitRuntimeException exception = assertThrows(MustExitRuntimeException.class,
                () -> providerToTest.fetchApiDefinitionFile("/example/path/to/extracted/sources", sechubScanConfiguration));

        assertEquals("Data section should not be empty since a sources folder was found.", exception.getMessage());
        assertEquals(MustExitCode.SECHUB_CONFIGURATION_INVALID, exception.getExitCode());
    }

    @ParameterizedTest
    @MethodSource("sourcesPartSizeTestNamedArguments")
    void sources_part_with_size_other_than_exactly_one_results_in_must_exit_runtime_exception(String sechubScanConfigJSON) {
        /* prepare */
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute + test */
        MustExitRuntimeException exception = assertThrows(MustExitRuntimeException.class,
                () -> providerToTest.fetchApiDefinitionFile("/example/path/to/extracted/sources", sechubScanConfiguration));

        assertEquals("Sources must contain exactly 1 entry.", exception.getMessage());
        assertEquals(MustExitCode.SECHUB_CONFIGURATION_INVALID, exception.getExitCode());
    }

    @Test
    void missing_filesystem_part_results_in_must_exit_runtime_exception() {
        /* prepare */
        String sechubScanConfigJSON = "{\"apiVersion\":\"1.0\",\"data\":{\"sources\":[{\"name\":\"open-api-file-reference\"}]},"
                + "\"webScan\":{\"uri\":\"https://localhost:8443\",\"api\":{\"type\":\"openApi\",\"use\":[\"open-api-file-reference\"]}}}";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute + test */
        MustExitRuntimeException exception = assertThrows(MustExitRuntimeException.class,
                () -> providerToTest.fetchApiDefinitionFile("/example/path/to/extracted/sources", sechubScanConfiguration));

        assertEquals("Sources filesystem part must be set at this stage.", exception.getMessage());
        assertEquals(MustExitCode.SECHUB_CONFIGURATION_INVALID, exception.getExitCode());

    }

    @ParameterizedTest
    @MethodSource("filesystemPartSizeTestNamedArguments")
    void filesystem_part_with_size_other_than_exactly_one_results_in_must_exit_runtime_exception(String sechubScanConfigJSON) {
        /* prepare */
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute + test */
        MustExitRuntimeException exception = assertThrows(MustExitRuntimeException.class,
                () -> providerToTest.fetchApiDefinitionFile("/example/path/to/extracted/sources", sechubScanConfiguration));

        assertEquals("Sources filesystem files part must contain exactly 1 entry.", exception.getMessage());
        assertEquals(MustExitCode.SECHUB_CONFIGURATION_INVALID, exception.getExitCode());
    }

    @Test
    void provider_returns_correct_path_for_valid_sechub_scan_config_with_openapi_definition_file() {
        /* prepare */
        String sechubScanConfigJSON = "{\"apiVersion\":\"1.0\",\"data\":{\"sources\":[{\"name\":\"open-api-file-reference\",\"fileSystem\":{\"files\":[\"openapi3.json\"]}}]},"
                + "\"webScan\":{\"uri\":\"https://localhost:8443\",\"api\":{\"type\":\"openApi\",\"use\":[\"open-api-file-reference\"]}}}";
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        Path path = providerToTest.fetchApiDefinitionFile("test/path", sechubScanConfiguration);

        /* test */
        assertEquals(path.toString(), "test/path/openapi3.json");
    }

    static Stream<Arguments> sourcesPartSizeTestNamedArguments() {
        /* @formatter:off */
        return Stream.of(
        		Arguments.of(Named.of("Sources part empty", "{\"apiVersion\":\"1.0\",\"data\":{\"sources\":[]},\"webScan\":{\"uri\":\"https://localhost:8443\",\"api\":{\"type\":\"openApi\",\"use\":[\"open-api-file-reference\"]}}}")),
        		Arguments.of(Named.of("Sources part more than one file", "{\"apiVersion\":\"1.0\",\"data\":{\"sources\":[{\"name\":\"open-api-file-reference\",\"fileSystem\":{\"files\":[\"openapi3.json\"]}},{\"name\":\"second-reference\",\"fileSystem\":{\"files\":[\"second-openapi-file.json\"]}}]},\"webScan\":{\"uri\":\"https://localhost:8443\",\"api\":{\"type\":\"openApi\",\"use\":[\"open-api-file-reference\",\"second-reference\"]}}}")),
        		Arguments.of(Named.of("Binaries part used instead of sources", "{\"apiVersion\":\"1.0\",\"data\":{\"binaries\":[{\"name\":\"open-api-file-reference\"}]},\"webScan\":{\"uri\":\"https://localhost:8443\",\"api\":{\"type\":\"openApi\",\"use\":[\"open-api-file-reference\"]}}}")));
        /* @formatter:on */
    }

    static Stream<Arguments> filesystemPartSizeTestNamedArguments() {
        /* @formatter:off */
        return Stream.of(
        		Arguments.of(Named.of("Filesystem part empty", "{\"apiVersion\":\"1.0\",\"data\":{\"sources\":[{\"name\":\"open-api-file-reference\",\"fileSystem\":{}}]},\"webScan\":{\"uri\":\"https://localhost:8443\",\"api\":{\"type\":\"openApi\",\"use\":[\"open-api-file-reference\"]}}}")),
        		Arguments.of(Named.of("Filesystem part more than one file", "{\"apiVersion\":\"1.0\",\"data\":{\"sources\":[{\"name\":\"open-api-file-reference\",\"fileSystem\":{\"files\":[\"openapi3.json\", \"second-file.json\"]}}]},\"webScan\":{\"uri\":\"https://localhost:8443\",\"api\":{\"type\":\"openApi\",\"use\":[\"open-api-file-reference\"]}}}")));
        /* @formatter:on */
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static org.junit.Assert.assertNull;
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

class ZapWrapperDataSectionFileProviderTest {

    private ZapWrapperDataSectionFileProvider providerToTest = new ZapWrapperDataSectionFileProvider();;

    @Test
    void open_api_sources_folder_is_null_results_in_empty_list() {
        /* execute */
        List<File> apiDefinitionFiles = providerToTest.fetchApiDefinitionFiles(null, new SecHubScanConfiguration());

        /* test */
        assertTrue(apiDefinitionFiles.isEmpty());
    }

    @Test
    void client_cert_sources_folder_is_null_results_in_cert_file_is_null() {
        /* execute */
        File certificateFile = providerToTest.fetchClientCertificateFile(null, new SecHubScanConfiguration());

        /* test */
        assertNull(certificateFile);
    }

    @Test
    void open_api_sechub_scan_config_is_null_results_in_empty_list() {
        /* execute */
        List<File> apiDefinitionFiles = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", null);

        /* test */
        assertTrue(apiDefinitionFiles.isEmpty());
    }

    @Test
    void client_cert_sechub_scan_config_is_null_results_in_cert_file_is_null() {
        /* execute */
        File certificateFile = providerToTest.fetchClientCertificateFile("/example/path/to/extracted/sources", null);

        /* test */
        assertNull(certificateFile);
    }

    @Test
    void open_api_missing_data_section_part_results_in_empty_list() {
        /* prepare */
        String openApiSechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration openApiSechubScanConfiguration = SecHubScanConfiguration.createFromJSON(openApiSechubScanConfigJSON);

        /* execute */
        List<File> apiDefinitionFiles = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", openApiSechubScanConfiguration);

        /* test */
        assertTrue(apiDefinitionFiles.isEmpty());
    }

    @Test
    void client_cert_missing_data_section_part_results_in_cert_file_is_null() {
        /* prepare */
        String clientCertSechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration clientCertSechubScanConfiguration = SecHubScanConfiguration.createFromJSON(clientCertSechubScanConfigJSON);

        /* execute */
        File certificateFile = providerToTest.fetchClientCertificateFile("/example/path/to/extracted/sources", clientCertSechubScanConfiguration);

        /* test */
        assertNull(certificateFile);
    }

    @Test
    void open_api_empty_sources_section_in_sechub_configuration_results_in_empty_list() {
        /* prepare */
        String openApiSechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration openApiSechubScanConfiguration = SecHubScanConfiguration.createFromJSON(openApiSechubScanConfigJSON);

        /* execute */
        List<File> apiDefinitionFiles = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", openApiSechubScanConfiguration);

        /* test */
        assertTrue(apiDefinitionFiles.isEmpty());
    }

    @Test
    void client_cert_empty_sources_section_in_sechub_configuration_results_in_cert_file_is_null() {
        /* prepare */
        String clientCertSechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration clientCertSechubScanConfiguration = SecHubScanConfiguration.createFromJSON(clientCertSechubScanConfigJSON);

        /* execute */
        File certificateFile = providerToTest.fetchClientCertificateFile("/example/path/to/extracted/sources", clientCertSechubScanConfiguration);

        /* test */
        assertNull(certificateFile);
    }

    @Test
    void open_api_missing_filesystem_part_in_sechub_configuration_results_in_empty_list() {
        /* prepare */
        String openApiSechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "open-api-file-reference"
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration openApiSechubScanConfiguration = SecHubScanConfiguration.createFromJSON(openApiSechubScanConfigJSON);

        /* execute */
        List<File> apiDefinitionFiles = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", openApiSechubScanConfiguration);

        /* test */
        assertTrue(apiDefinitionFiles.isEmpty());
    }

    @Test
    void client_cert_missing_filesystem_part_in_sechub_configuration_results_in_cert_file_is_null() {
        /* prepare */
        String clientCertSechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "client-cert-file-reference"
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration clientCertSechubScanConfiguration = SecHubScanConfiguration.createFromJSON(clientCertSechubScanConfigJSON);

        /* execute */
        File certificateFile = providerToTest.fetchClientCertificateFile("/example/path/to/extracted/sources", clientCertSechubScanConfiguration);

        /* test */
        assertNull(certificateFile);
    }

    @Test
    void open_api_empty_filesystem_part_in_sechub_configuration_results_in_empty_list() {
        /* prepare */
        String openApiSechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "open-api-file-reference",
                      "fileSystem" : { }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration openApiSechubScanConfiguration = SecHubScanConfiguration.createFromJSON(openApiSechubScanConfigJSON);

        /* execute */
        List<File> apiDefinitionFiles = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", openApiSechubScanConfiguration);

        /* test */
        assertTrue(apiDefinitionFiles.isEmpty());
    }

    @Test
    void client_cert_empty_filesystem_part_in_sechub_configuration_results_in_cert_file_is_null() {
        /* prepare */
        String clientCertSechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "client-cert-file-reference",
                      "fileSystem" : { }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration clientCertSechubScanConfiguration = SecHubScanConfiguration.createFromJSON(clientCertSechubScanConfigJSON);

        /* execute */
        File certificateFile = providerToTest.fetchClientCertificateFile("/example/path/to/extracted/sources", clientCertSechubScanConfiguration);

        /* test */
        assertNull(certificateFile);
    }

    @Test
    void open_api_binaries_instead_of_sources_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "binaries" : [ {
                      "name" : "open-api-file-reference"
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("/example/path/to/extracted/sources", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void client_cert_binaries_instead_of_sources_results_in_cert_file_is_null() {
        /* prepare */
        String sechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "binaries" : [ {
                      "name" : "client-cert-file-reference"
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        File result = providerToTest.fetchClientCertificateFile("/example/path/to/extracted/sources", sechubScanConfiguration);

        /* test */
        assertNull(result);
    }

    @Test
    void open_api_folders_instead_of_files_inside_filesystem_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "open-api-file-reference",
                      "fileSystem" : {
                        "folders" : [ "openapifolder/" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("test/path", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void client_cert_folders_instead_of_files_inside_filesystem_results_in_cert_file_is_null() {
        /* prepare */
        String sechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "client-cert-file-reference",
                      "fileSystem" : {
                        "folders" : [ "clientCertfolder/" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        File result = providerToTest.fetchClientCertificateFile("test/path", sechubScanConfiguration);

        /* test */
        assertNull(result);
    }

    @Test
    void open_api_data_section_name_differs_from_use_part_inside_openapi_definition_results_in_empty_list() {
        /* prepare */
        String sechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "open-api-file-reference",
                      "fileSystem" : {
                        "files" : [ "openapi3.json" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "no-existing-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("test/path", sechubScanConfiguration);

        /* test */
        assertTrue(result.isEmpty());
    }

    @Test
    void client_cert_data_section_name_differs_from_use_part_inside_openapi_definition_results_in_cert_file_is_null() {
        /* prepare */
        String sechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "client-cert-file-reference",
                      "fileSystem" : {
                        "files" : [ "clientCert.p12" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "no-existing-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        File result = providerToTest.fetchClientCertificateFile("test/path", sechubScanConfiguration);

        /* test */
        assertNull(result);
    }

    @Test
    void open_api_valid_sechub_scan_config_with_openapi_definition_file_results_in_list_with_one_file() {
        /* prepare */
        String sechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "open-api-file-reference",
                      "fileSystem" : {
                        "files" : [ "openapi3.json" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("test/path", sechubScanConfiguration);

        /* test */
        assertEquals(1, result.size());
    }

    @Test
    void client_cert_valid_sechub_scan_config_with_openapi_definition_file_results_in_client_cert_file() {
        /* prepare */
        String sechubScanConfigJSON = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "client-cert-file-reference",
                      "fileSystem" : {
                        "files" : [ "clientCert.p12" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        File result = providerToTest.fetchClientCertificateFile("test/path", sechubScanConfiguration);

        /* test */
        assertEquals("test/path/clientCert.p12", result.toString());
    }

    @ParameterizedTest
    @MethodSource("multipleFilesTestNamedArguments")
    void multiple_open_api_files_result_in_multiple_open_api_files_available(String sechubScanConfigJSON) {
        /* prepare */
        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON);

        /* execute */
        List<File> result = providerToTest.fetchApiDefinitionFiles("test/path", sechubScanConfiguration);

        /* test */
        assertEquals(2, result.size());
    }

    @Test
    void client_cert_sechub_config_with_more_than_one_data_section_results_in_first_client_cert_file() {
        /* prepare */
        String sechubConfigWithmoreThanOneDataSection = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "client-cert-file-reference",
                      "fileSystem" : {
                        "files" : [ "clientCert.p12" ]
                      }
                    }, {
                      "name" : "other-client-cert-file-reference",
                      "fileSystem" : {
                        "files" : [ "other-clientCert.p12" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference", "other-client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubConfigWithmoreThanOneDataSection);

        /* execute */
        File result = providerToTest.fetchClientCertificateFile("test/path", sechubScanConfiguration);

        /* test */
        assertEquals("test/path/other-clientCert.p12", result.toString());
    }

    @Test
    void client_cert_sechub_config_with_filesystem_part_has_more_than_one_file_results_in_first_client_cert_file() {
        /* prepare */
        String sechubConfigWithfilesystemPartHasMoreThanOneFile = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "client-cert-file-reference",
                      "fileSystem" : {
                        "files" : [ "clientCert.p12", "second-clientCert.p12" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "clientCertificate" : {
                      "password" : "secret",
                      "use" : [ "client-cert-file-reference" ]
                    }
                  }
                }
                """;

        SecHubScanConfiguration sechubScanConfiguration = SecHubScanConfiguration.createFromJSON(sechubConfigWithfilesystemPartHasMoreThanOneFile);

        /* execute */
        File result = providerToTest.fetchClientCertificateFile("test/path", sechubScanConfiguration);

        /* test */
        assertEquals("test/path/clientCert.p12", result.toString());
    }

    static Stream<Arguments> multipleFilesTestNamedArguments() {
        /* @formatter:off */
        String moreThanOneDataSectionName = "Sources part more than one file in 2 data sections";
        String sechubConfigWithmoreThanOneDataSection = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "open-api-file-reference",
                      "fileSystem" : {
                        "files" : [ "openapi3.json" ]
                      }
                    }, {
                      "name" : "second-reference",
                      "fileSystem" : {
                        "files" : [ "second-openapi-file.json" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference", "second-reference" ]
                    }
                  }
                }
                """;

        String filesystemPartHasMoreThanOneFileName = "Filesystem files part more than one file";
        String sechubConfigWithfilesystemPartHasMoreThanOneFile = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "open-api-file-reference",
                      "fileSystem" : {
                        "files" : [ "openapi3.json", "second-file.json" ]
                      }
                    } ]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "api" : {
                      "type" : "openApi",
                      "use" : [ "open-api-file-reference" ]
                    }
                  }
                }
                """;

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

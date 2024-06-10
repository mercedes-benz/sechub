// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.archive.DirectoryAndFileSupport;
import com.mercedesbenz.sechub.test.TestUtil;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.modules.git.GitLocationConverter;
import com.mercedesbenz.sechub.wrapper.prepare.modules.git.GitPrepareInputValidator;
import com.mercedesbenz.sechub.wrapper.prepare.modules.git.GitPrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.modules.git.GitWrapper;
import com.mercedesbenz.sechub.wrapper.prepare.modules.git.JGitAdapter;
import com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo.SkopeoLocationConverter;
import com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo.SkopeoPrepareInputValidator;
import com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo.SkopeoPrepareWrapperModule;
import com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo.SkopeoWrapper;
import com.mercedesbenz.sechub.wrapper.prepare.upload.FileNameSupport;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperArchiveCreator;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperFileUploadService;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperS3PropertiesSetup;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperSechubConfigurationSupport;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperSharedVolumePropertiesSetup;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperStorageService;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadService;

/* @formatter:off */
@SpringBootTest(classes = { PrepareWrapperContextFactory.class,
        PrepareWrapperPreparationService.class,
        PrepareWrapperPojoFactory.class,
        PrepareWrapperPDSUserMessageSupportFactory.class,
        PrepareWrapperRemoteConfigurationExtractor.class,
        GitPrepareWrapperModule.class,
        PrepareWrapperModule.class,
        GitPrepareInputValidator.class,
        JGitAdapter.class,
        SkopeoPrepareWrapperModule.class,
        SkopeoWrapper.class,
        SkopeoPrepareInputValidator.class,
        PrepareWrapperStorageService.class,
        PrepareWrapperUploadService.class,
        PrepareWrapperFileUploadService.class,
        PrepareWrapperSechubConfigurationSupport.class,
        PrepareWrapperArchiveCreator.class,
        ArchiveSupport.class,
        DirectoryAndFileSupport.class,
        SkopeoLocationConverter.class,
        GitLocationConverter.class,
        FileNameSupport.class,
        PrepareWrapperSharedVolumePropertiesSetup.class,
        PrepareWrapperS3PropertiesSetup.class })
/* @formatter:on */
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:init-testdata-prepare-wrapper-spring-boot.properties")
class PrepareWrapperApplicationSpringBootTest {

    @Autowired
    PrepareWrapperPreparationService preparationService;

    @MockBean
    PrepareWrapperEnvironment environment;

    @MockBean
    GitWrapper gitWrapper;

    private UUID sechubJobUUID;

    @BeforeEach
    void beforeEach() throws Exception {
        Path testFolder = TestUtil.createTempDirectoryInBuildFolder("prepare-wrapper-sb");

        sechubJobUUID = UUID.randomUUID();
        when(environment.getSechubJobUUID()).thenReturn(sechubJobUUID.toString());
        when(environment.getPdsUserMessagesFolder()).thenReturn(testFolder.resolve("/messages").toString());
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(testFolder.resolve("workspace").toString());
    }

    @Test
    void start_preparation_remote_data_but_cannot_handled__results_in_failed() throws IOException {
        /* prepare */
        when(environment.getSechubConfigurationModelAsJson()).thenReturn(
                """
                {
                  "projectId" : "project1",
                  "data" : {
                    "binaries" : [ {
                      "name" : "remote_example_name",
                      "remote" : {
                        "location" : "https://not-any.repo/",
                        "type" : "not-git"
                      }
                    } ]
                  },
                  "codeScan" : {
                    "use" : [ "remote_example_name" ]
                  }
                }
                """
                );


        /* execute */
        AdapterExecutionResult result = preparationService.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=FAILED", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No module was able to prepare the defined remote data.", result.getProductMessages().get(0).getText());
    }

    @Test
    void start_preparation_remote_data_handled_by_git_but_location_not_correct_results_validator_exception() throws IOException {
        /* prepare */
        when(environment.getSechubConfigurationModelAsJson()).thenReturn(
                """
                {
                  "projectId" : "project1",
                  "data" : {
                    "binaries" : [ {
                      "name" : "remote_example_name",
                      "remote" : {
                        "location" : "https://not-any.repo/",
                        "type" : "git"
                      }
                    } ]
                  },
                  "codeScan" : {
                    "use" : [ "remote_example_name" ]
                  }
                }
                """
                );


        /* execute + test */
        assertThrows(PrepareWrapperInputValidatorException.class, ()-> preparationService.startPreparation());

    }

    @Test
    void start_preparation_remote_data_handled_by_git_and_location_correct_results_but_failing_git_download_leads_to_illegal_state_exception() throws IOException {
        /* prepare */
        when(environment.getSechubConfigurationModelAsJson()).thenReturn(
                """
                {
                  "projectId" : "project1",
                  "data" : {
                    "binaries" : [ {
                      "name" : "remote_example_name",
                      "remote" : {
                        "location" : "https://somewhere.example.com/testrepository.git",
                        "type" : "git"
                      }
                    } ]
                  },
                  "codeScan" : {
                    "use" : [ "remote_example_name" ]
                  }
                }
                """
                );


        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, ()-> preparationService.startPreparation());

        /* test */
        String message = exception.getMessage();
        String expected = "Download of git repository was not successful"; // the mocked git wrapper does not download
        if(!message.contains(expected)) {
            assertEquals(expected, message); // we use equals here to have a better comparison in IDE (fails here always)
        }

    }

    @Test
    void start_preparation_remote_data_handled_by_skopeo_and_location_correct_results_but_failing_skopeo_download_leads_to_io_exception_with_message() throws IOException {
        /* prepare */
        when(environment.getSechubConfigurationModelAsJson()).thenReturn(
                """
                {
                  "projectId" : "project1",
                  "data" : {
                    "binaries" : [ {
                      "name" : "remote_example_name",
                      "remote" : {
                        "location" : "https://somewhere.example.com",
                        "type" : "docker"
                      }
                    } ]
                  },
                  "codeScan" : {
                    "use" : [ "remote_example_name" ]
                  }
                }
                """
                );


        /* execute + test */
        IOException exception = assertThrows(IOException.class, ()-> preparationService.startPreparation());

        /* test */
        String message = exception.getMessage();
        String expected = "Error while executing Skopeo download process for: https://somewhere.example.com"; // the mocked skopeo wrapper does not download
        if(!message.contains(expected)) {
            assertEquals(expected, message); // we use equals here to have a better comparison in IDE (fails here always)
        }

    }

}
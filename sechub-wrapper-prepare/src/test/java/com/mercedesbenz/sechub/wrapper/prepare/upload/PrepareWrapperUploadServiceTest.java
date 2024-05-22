package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.model.SecHubBinaryDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

class PrepareWrapperUploadServiceTest {

    PrepareWrapperUploadService uploadServiceToTest;

    PrepareWrapperArchiveCreator archiveCreator;

    PrepareWrapperFileUploadService fileUploadService;

    PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;

    FileNameSupport fileNameSupport;

    CheckSumSupport checkSumSupport;

    @BeforeEach
    void beforeEach() {
        uploadServiceToTest = new PrepareWrapperUploadService();

        archiveCreator = mock(PrepareWrapperArchiveCreator.class);
        fileUploadService = mock(PrepareWrapperFileUploadService.class);
        sechubConfigurationSupport = mock(PrepareWrapperSechubConfigurationSupport.class);
        fileNameSupport = mock(FileNameSupport.class);
        checkSumSupport = mock(CheckSumSupport.class);

        uploadServiceToTest.archiveCreator = archiveCreator;
        uploadServiceToTest.fileUploadService = fileUploadService;
        uploadServiceToTest.sechubConfigurationSupport = sechubConfigurationSupport;
        uploadServiceToTest.fileNameSupport = fileNameSupport;
        uploadServiceToTest.checkSumSupport = checkSumSupport;
    }

    @Test
    void upload_throws_exception_when_data_is_not_configured() {
        /* prepare */
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        when(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).thenReturn("path");
        when(context.getEnvironment().getSechubStoragePath()).thenReturn("path");
        UUID uuid = UUID.randomUUID();
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());

        SecHubConfigurationModel model = mock(SecHubConfigurationModel.class);
        when(sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context)).thenReturn(model);

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> uploadServiceToTest.upload(context));

        /* test */
        assertEquals("SecHubConfigurationModel data is not configured.", exception.getMessage());
    }

    @Test
    void upload_calls_uploadService_when_sources_are_not_empty() throws Exception {
        /* prepare */
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        when(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).thenReturn("path");
        when(context.getEnvironment().getSechubStoragePath()).thenReturn("path");
        UUID uuid = UUID.randomUUID();
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());

        SecHubConfigurationModel model = mock(SecHubConfigurationModel.class);
        SecHubDataConfiguration dataConfiguration = mock(SecHubDataConfiguration.class);
        when(model.getData()).thenReturn(Optional.of(dataConfiguration));
        when(sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context)).thenReturn(model);

        List<SecHubSourceDataConfiguration> list = mock(List.class);
        when(dataConfiguration.getSources()).thenReturn(list);

        /* execute */
        uploadServiceToTest.upload(context);

        /* test */
        verify(fileUploadService).uploadFile(any(), any(), any(), any());
    }

    @Test
    void upload_calls_uploadService_when_binaries_are_not_empty() throws Exception {
        /* prepare */
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        when(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).thenReturn("path");
        when(context.getEnvironment().getSechubStoragePath()).thenReturn("path");
        UUID uuid = UUID.randomUUID();
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());

        SecHubConfigurationModel model = mock(SecHubConfigurationModel.class);
        SecHubDataConfiguration dataConfiguration = mock(SecHubDataConfiguration.class);
        when(model.getData()).thenReturn(Optional.of(dataConfiguration));
        when(sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context)).thenReturn(model);

        List<SecHubBinaryDataConfiguration> list = mock(List.class);
        when(dataConfiguration.getBinaries()).thenReturn(list);

        /* execute */
        uploadServiceToTest.upload(context);

        /* test */
        verify(fileUploadService).uploadFile(any(), any(), any(), any());
    }

}
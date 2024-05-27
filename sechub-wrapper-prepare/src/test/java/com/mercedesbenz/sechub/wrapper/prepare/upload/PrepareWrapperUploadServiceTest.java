package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
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
import com.mercedesbenz.sechub.wrapper.prepare.modules.ToolContext;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

class PrepareWrapperUploadServiceTest {

    private PrepareWrapperUploadService uploadServiceToTest;
    private PrepareWrapperFileUploadService fileUploadService;
    private PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;

    @BeforeEach
    void beforeEach() {
        uploadServiceToTest = new PrepareWrapperUploadService();

        fileUploadService = mock(PrepareWrapperFileUploadService.class);
        sechubConfigurationSupport = mock(PrepareWrapperSechubConfigurationSupport.class);
        PrepareWrapperArchiveCreator archiveCreator = mock(PrepareWrapperArchiveCreator.class);
        FileNameSupport fileNameSupport = mock(FileNameSupport.class);
        CheckSumSupport checkSumSupport = mock(CheckSumSupport.class);

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
        when(context.getEnvironment().getPdsJobWorkspaceLocation()).thenReturn("path");
        when(context.getEnvironment().getSechubStoragePath()).thenReturn("path");
        UUID uuid = UUID.randomUUID();
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());

        ToolContext toolContext = mock(ToolContext.class);
        Path testPath = Path.of("path");
        when(toolContext.getUploadDirectory()).thenReturn(testPath);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testPath);

        SecHubConfigurationModel model = mock(SecHubConfigurationModel.class);
        when(sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context, toolContext)).thenReturn(model);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> uploadServiceToTest.upload(context, toolContext));

        /* test */
        assertEquals("SecHubConfigurationModel data is not configured.", exception.getMessage());
    }

    @Test
    void upload_calls_uploadService_when_sources_are_not_empty() throws Exception {
        /* prepare */
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        when(context.getEnvironment().getPdsJobWorkspaceLocation()).thenReturn("path");
        when(context.getEnvironment().getSechubStoragePath()).thenReturn("path");
        UUID uuid = UUID.randomUUID();
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());

        SecHubConfigurationModel model = mock(SecHubConfigurationModel.class);
        SecHubDataConfiguration dataConfiguration = mock(SecHubDataConfiguration.class);
        when(model.getData()).thenReturn(Optional.of(dataConfiguration));

        ToolContext toolContext = mock(ToolContext.class);
        Path testPath = Path.of("path");
        when(toolContext.getUploadDirectory()).thenReturn(testPath);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testPath);

        when(sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context, toolContext)).thenReturn(model);

        List<SecHubSourceDataConfiguration> list = mock(List.class);
        when(dataConfiguration.getSources()).thenReturn(list);

        /* execute */
        uploadServiceToTest.upload(context, toolContext);

        /* test */
        verify(fileUploadService).uploadFile(any(), any(), any(), any());
    }

    @Test
    void upload_calls_uploadService_when_binaries_are_not_empty() throws Exception {
        /* prepare */
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        when(context.getEnvironment().getPdsJobWorkspaceLocation()).thenReturn("path");
        when(context.getEnvironment().getSechubStoragePath()).thenReturn("path");
        UUID uuid = UUID.randomUUID();
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());
        when(context.getEnvironment().getSechubJobUUID()).thenReturn(uuid.toString());

        SecHubConfigurationModel model = mock(SecHubConfigurationModel.class);
        SecHubDataConfiguration dataConfiguration = mock(SecHubDataConfiguration.class);
        when(model.getData()).thenReturn(Optional.of(dataConfiguration));

        ToolContext toolContext = mock(ToolContext.class);
        Path testPath = Path.of("path");
        when(toolContext.getUploadDirectory()).thenReturn(testPath);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testPath);

        when(sechubConfigurationSupport.replaceRemoteDataWithFilesystem(context, toolContext)).thenReturn(model);

        List<SecHubBinaryDataConfiguration> list = mock(List.class);
        when(dataConfiguration.getBinaries()).thenReturn(list);

        /* execute */
        uploadServiceToTest.upload(context, toolContext);

        /* test */
        verify(fileUploadService).uploadFile(any(), any(), any(), any());
    }

}
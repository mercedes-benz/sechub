package com.mercedesbenz.sechub.wrapper.prepare.cli;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareStatus;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperPreparationService;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperResultStorageService;

class PrepareWrapperCLITest {

    PrepareWrapperCLI prepareWrapperCLIToTest;
    PrepareWrapperPreparationService preparationService;
    PrepareWrapperResultStorageService storageService;

    @BeforeEach
    void beforeEach() {
        prepareWrapperCLIToTest = new PrepareWrapperCLI();
        preparationService = mock(PrepareWrapperPreparationService.class);
        storageService = mock(PrepareWrapperResultStorageService.class);

        prepareWrapperCLIToTest.preparationService = preparationService;
        prepareWrapperCLIToTest.storageService = storageService;
    }

    @Test
    void prepareWrapperCLI_stores_result_when_preparation_service_succeeds() throws IOException {
        /* execute */
        prepareWrapperCLIToTest.run();

        /* test */
        verify(preparationService).startPreparation();
        verify(storageService).store(any());
    }

    @Test
    void prepareWrapperCLI_stores_result_when_preparation_service_fails() throws IOException {
        /* prepare */
        PrepareResult prepareResult = new PrepareResult(PrepareStatus.FAILED);
        AdapterExecutionResult result = new AdapterExecutionResult(prepareResult.toString(), new ArrayList<>());
        when(preparationService.startPreparation()).thenReturn(result);

        /* execute */
        prepareWrapperCLIToTest.run();

        /* test */
        verify(preparationService).startPreparation();
        verify(storageService).store(result);
    }

    @Test
    void prepareWrapperCLI_stores_result_when_preparation_service_throws_exception() throws IOException {
        /* prepare */
        IllegalArgumentException exception = new IllegalArgumentException("IllegalArgument Exception");
        when(preparationService.startPreparation()).thenThrow(exception);

        /* execute */
        prepareWrapperCLIToTest.run();

        /* test */
        verify(preparationService).startPreparation();
        verify(storageService).store(any());
    }
}
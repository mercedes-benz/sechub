package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;

class WrapperSkopeoTest {

    WrapperSkopeo wrapperToTest;
    PDSProcessAdapterFactory processAdapterFactory;

    @BeforeEach
    void beforeEach() throws IOException {
        wrapperToTest = new WrapperSkopeo();
        processAdapterFactory = mock(PDSProcessAdapterFactory.class);
        doNothing().when(processAdapterFactory).startProcess(any());

        wrapperToTest.processAdapterFactory = processAdapterFactory;
    }

    // TODO: 07.05.24 laura add tests

}
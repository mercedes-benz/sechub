package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;

class PrepareWrapperModuleSkopeoTest {

    PrepareWrapperModuleSkopeo wrapperToTest;
    SkopeoInputValidator skopeoInputValidator;
    WrapperSkopeo skopeo;

    @BeforeEach
    void beforeEach() {
        wrapperToTest = new PrepareWrapperModuleSkopeo();
        skopeoInputValidator = mock(SkopeoInputValidator.class);
        skopeo = mock(WrapperSkopeo.class);

        wrapperToTest.skopeoInputValidator = skopeoInputValidator;
        wrapperToTest.skopeo = skopeo;
    }

    // TODO: 07.05.24 laura add tests
}
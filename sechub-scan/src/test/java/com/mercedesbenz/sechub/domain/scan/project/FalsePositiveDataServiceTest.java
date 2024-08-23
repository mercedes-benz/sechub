// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

public class FalsePositiveDataServiceTest {

    private static final String PROJECT_ID = "testprojectId";
    private FalsePositiveDataService serviceToTest;

    private static final FalsePositiveDataListValidation falsePositiveListValidation = mock();

    private static final ScanProjectConfigService configService = mock();
    private static final ScanAssertService scanAssertService = mock();
    private static final UserInputAssertion userInputAssertion = mock();
    private static final AuditLogService auditLogService = mock();

    private ScanProjectConfig config;

    @Before
    public void before() {
        /* @formatter:off */
        Mockito.reset(userInputAssertion,
                configService,
                falsePositiveListValidation,
                scanAssertService,
                auditLogService);

        serviceToTest = new FalsePositiveDataService(null,
                userInputAssertion,
                configService,
                falsePositiveListValidation,
                null,
                null,
                scanAssertService,
                auditLogService);
        /* @formatter:on */

        when(falsePositiveListValidation.validate(any(FalsePositiveDataList.class))).thenReturn(new ValidationResult());

        /* we mock config service */
        config = new ScanProjectConfig();
        config.setData("{}");

        when(configService.getOrCreate(eq(PROJECT_ID), eq(ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION), eq(Boolean.FALSE), any())).thenReturn(config);

    }

    @Test
    public void check_validations_are_triggered() {
        /* prepare */
        FalsePositiveDataList data = new FalsePositiveDataList();

        /* execute */
        serviceToTest.addFalsePositives(PROJECT_ID, data);

        /* test */
        verify(userInputAssertion).assertIsValidProjectId(PROJECT_ID);
        verify(scanAssertService).assertUserHasAccessToProject(PROJECT_ID);
        verify(falsePositiveListValidation).validate(any(FalsePositiveDataList.class));
    }

}

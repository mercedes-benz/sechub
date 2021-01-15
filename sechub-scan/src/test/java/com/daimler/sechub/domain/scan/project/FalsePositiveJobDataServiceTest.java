// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.ScanAssertService;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class FalsePositiveJobDataServiceTest {

    private static final String PROJECT_ID = "testprojectId";
    private FalsePositiveJobDataService serviceToTest;
    private FalsePositiveJobDataListValidation falsePositiveListValidation;
    private ScanProjectConfigService configService;
    private ScanAssertService scanAssertService;
    private UserInputAssertion userInputAssertion;
    private ScanProjectConfig config;
    private AuditLogService auditLogService;

    @Before
    public void before() {
        serviceToTest = new FalsePositiveJobDataService();

        falsePositiveListValidation = mock(FalsePositiveJobDataListValidation.class);
        configService = mock(ScanProjectConfigService.class);
        userInputAssertion = mock(UserInputAssertion.class);
        scanAssertService = mock(ScanAssertService.class);
        auditLogService = mock(AuditLogService.class);

        serviceToTest.falsePositiveJobDataListValidation = falsePositiveListValidation;
        serviceToTest.configService = configService;
        serviceToTest.scanAssertService = scanAssertService;
        serviceToTest.userInputAssertion = userInputAssertion;
        serviceToTest.auditLogService = auditLogService;

        when(falsePositiveListValidation.validate(any(FalsePositiveJobDataList.class))).thenReturn(new ValidationResult());

        /* we mock config service */
        config = new ScanProjectConfig();
        config.setData("{}");

        when(configService.getOrCreate(eq(PROJECT_ID), eq(ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION), eq(Boolean.FALSE), any())).thenReturn(config);

    }

    @Test
    public void check_validations_are_triggered() {
        /* prepare */
        FalsePositiveJobDataList data = new FalsePositiveJobDataList();

        /* execute */
        serviceToTest.addFalsePositives(PROJECT_ID, data);

        /* test */
        verify(userInputAssertion).isValidProjectId(PROJECT_ID);
        verify(scanAssertService).assertUserHasAccessToProject(PROJECT_ID);
        verify(falsePositiveListValidation).validate(any(FalsePositiveJobDataList.class));
    }

}

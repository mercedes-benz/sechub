package com.daimler.sechub.domain.scan.project;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.ScanAssertService;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class FalsePositiveJobServiceTest {

    private FalsePositiveJobDataService serviceToTest;
    private FalsePositiveJobDataListValidation falsePositiveListValidation;
    private ScanProjectConfigService configService;
    private ScanAssertService scanAssertService;
    private UserInputAssertion userInputAssertion;
    private ScanProjectConfig config;
    
    @Before
    public void before() {
        serviceToTest = new FalsePositiveJobDataService();
        
        falsePositiveListValidation = mock(FalsePositiveJobDataListValidation.class);
        configService=mock(ScanProjectConfigService.class);
        userInputAssertion=mock(UserInputAssertion.class);
        scanAssertService=mock(ScanAssertService.class);
        
        serviceToTest.falsePositiveJobDataListValidation=falsePositiveListValidation;
        serviceToTest.configService=configService;
        serviceToTest.scanAssertService=scanAssertService;
        serviceToTest.userInputAssertion=userInputAssertion;
        
        when(falsePositiveListValidation.validate(any(FalsePositiveJobDataList.class))).thenReturn(new ValidationResult());
        config = new ScanProjectConfig();
        when(configService.getOrCreate("project2",ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION, false, "{}")).thenReturn(config);
        
    }
    
    @Test
    public void check_validations_are_triggered() {
        /* prepare */
        FalsePositiveJobDataList data = new FalsePositiveJobDataList();
        
        /* execute */
        serviceToTest.addFalsePositives("project2", data);
        
        /* test */
        verify(userInputAssertion).isValidProjectId("project2");
        verify(scanAssertService).assertUserHasAccessToProject("project2");
        verify(falsePositiveListValidation).validate(any(FalsePositiveJobDataList.class));
    }

}

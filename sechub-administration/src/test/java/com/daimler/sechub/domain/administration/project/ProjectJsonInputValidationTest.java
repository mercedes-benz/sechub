// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;

import com.daimler.sechub.sharedkernel.validation.ApiVersionValidation;
import com.daimler.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;
import com.daimler.sechub.sharedkernel.validation.UserIdValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class ProjectJsonInputValidationTest {

    private ProjectJsonInputValidation toTest;
    private ProjectJsonInput input;
    private Errors errors;
    private ApiVersionValidationFactory apiVersionValidationFactory;
    private ApiVersionValidation apiValidation;
    private ProjectIdValidation projectIdValidation;
    private UserIdValidation userIdValidation;
    private ValidationResult okResult;
    private ValidationResult failedResult;

    @Before
    public void before() throws Exception {
        apiVersionValidationFactory = mock(ApiVersionValidationFactory.class);
        apiValidation = mock(ApiVersionValidation.class);
        projectIdValidation = mock(ProjectIdValidation.class);
        userIdValidation = mock(UserIdValidation.class);

        toTest = new ProjectJsonInputValidation();
        toTest.apiVersionValidationFactory = apiVersionValidationFactory;
        when(apiVersionValidationFactory.createValidationAccepting(any())).thenReturn(apiValidation);
        toTest.projectIdValidation = projectIdValidation;
        toTest.userIdValidation = userIdValidation;

        input = mock(ProjectJsonInput.class);
        errors = mock(Errors.class);

        okResult = mock(ValidationResult.class);
        when(okResult.isValid()).thenReturn(true);

        failedResult = mock(ValidationResult.class);
        when(failedResult.isValid()).thenReturn(false);

        toTest.postConstruct();// simulate post construct...
    }

    @Test
    public void asInput_returns_object() {
        assertEquals(input, toTest.asInput(input));
    }

    @Test
    public void when_useridvaluation_invalid_api_error() {
        /* prepare */
        when(userIdValidation.validate(any())).thenReturn(failedResult);

        /* execute */
        toTest.checkOwnerUserId(errors, input);

        /* test */
        verify(errors).rejectValue(eq(ProjectJsonInput.PROPERTY_OWNER), eq("api.error.userid.invalid"), any());
    }

    @Test
    public void when_useridvaluation_valid_no_api_error() {
        /* prepare */
        when(userIdValidation.validate(any())).thenReturn(okResult);

        /* execute */
        toTest.checkOwnerUserId(errors, input);

        /* test */
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_OWNER), eq("api.error.userid.invalid"), any());
    }

    @Test
    public void when_projectidvaluation_invalid_api_error() {
        /* prepare */
        when(projectIdValidation.validate(any())).thenReturn(failedResult);

        /* execute */
        toTest.checkProjectId(errors, input);

        /* test */
        verify(errors).rejectValue(eq(ProjectJsonInput.PROPERTY_NAME), eq("api.error.projectid.invalid"), any());
    }

    @Test
    public void when_projectidvaluation_valid_no_api_error() {
        /* prepare */
        when(projectIdValidation.validate(any())).thenReturn(okResult);

        /* execute */
        toTest.checkProjectId(errors, input);

        /* test */
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_NAME), eq("api.error.projectid.invalid"), any());
    }

    @Test
    public void when_apivalidation_invalid_api_error() {
        /* prepare */
        when(input.getApiVersion()).thenReturn("x");
        when(apiValidation.validate(any())).thenReturn(failedResult);

        /* execute */
        toTest.checkApiVersion(errors, input);

        /* test */
        verify(errors).rejectValue(eq(ProjectJsonInput.PROPERTY_API_VERSION), eq("api.error.unsupported.version"), any());
    }

    @Test
    public void when_apivalidation_null_required_field_api_error() {
        /* prepare */
        when(input.getApiVersion()).thenReturn(null);
        when(apiValidation.validate(any())).thenReturn(okResult);

        /* execute */
        toTest.checkApiVersion(errors, input);

        /* test */
        verify(errors).rejectValue(eq(ProjectJsonInput.PROPERTY_API_VERSION), eq("field.required"), any(), any());
    }

    @Test
    public void when_apivalidation_empty_required_field_api_error() {
        /* prepare */
        when(input.getApiVersion()).thenReturn("");
        when(apiValidation.validate(any())).thenReturn(okResult);

        /* execute */
        toTest.checkApiVersion(errors, input);

        /* test */
        verify(errors).rejectValue(eq(ProjectJsonInput.PROPERTY_API_VERSION), eq("field.required"), any(), any());
    }

    @Test
    public void when_apivalidation_valid_no_api_error() {
        /* prepare */
        when(apiValidation.validate(any())).thenReturn(okResult);

        /* execute */
        toTest.checkApiVersion(errors, input);

        /* test */
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_NAME), eq("api.error.projectid.invalid"), any());
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_API_VERSION), any(), any());
    }

}

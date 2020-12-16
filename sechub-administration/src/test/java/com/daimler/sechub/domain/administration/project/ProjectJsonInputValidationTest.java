// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.Errors;

import com.daimler.sechub.domain.administration.project.ProjectJsonInput.ProjectMetaData;
import com.daimler.sechub.domain.administration.project.ProjectJsonInput.ProjectWhiteList;
import com.daimler.sechub.sharedkernel.validation.ApiVersionValidation;
import com.daimler.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;
import com.daimler.sechub.sharedkernel.validation.ProjectMetaDataValidation;
import com.daimler.sechub.sharedkernel.validation.URIValidation;
import com.daimler.sechub.sharedkernel.validation.UserIdValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;
import com.daimler.sechub.test.TestUtil;

public class ProjectJsonInputValidationTest {

    private ProjectJsonInputValidation toTest;
    private ProjectJsonInput input;
    private Errors errors;
    private ApiVersionValidationFactory apiVersionValidationFactory;
    private ApiVersionValidation apiValidation;
    private ProjectIdValidation projectIdValidation;
    private UserIdValidation userIdValidation;
    private URIValidation whiteListValidation;
    private ProjectMetaDataValidation metaDataValidation;
    private ValidationResult okResult;
    private ValidationResult failedResult;

    @Before
    public void before() throws Exception {
        apiVersionValidationFactory = mock(ApiVersionValidationFactory.class);
        apiValidation = mock(ApiVersionValidation.class);
        projectIdValidation = mock(ProjectIdValidation.class);
        userIdValidation = mock(UserIdValidation.class);
        whiteListValidation = mock(URIValidation.class);
        metaDataValidation = mock(ProjectMetaDataValidation.class);

        toTest = new ProjectJsonInputValidation();
        toTest.apiVersionValidationFactory = apiVersionValidationFactory;
        when(apiVersionValidationFactory.createValidationAccepting(any())).thenReturn(apiValidation);
        toTest.projectIdValidation = projectIdValidation;
        toTest.userIdValidation = userIdValidation;
        toTest.whitelistValidation = whiteListValidation;
        toTest.metaDataValidation = metaDataValidation;

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
    
    @Test
    public void when_whitelisturivalidation_valid_no_api_error() throws URISyntaxException {
        /* prepare */
        when(whiteListValidation.validate(any())).thenReturn(okResult);
        
        /* execute */
        toTest.checkWhitelist(errors, input);
        
        /* test */
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_WHITELIST), eq("api.error.whitelist.invalid"), any());
    }
    
    @Test
    public void when_whitelisturivalidation_with_empty_uri_invalid_api_error() throws URISyntaxException {
        /* prepare */
        ProjectWhiteList whiteList = new ProjectWhiteList();
        whiteList.getUris().add(URI.create(""));
        
        when(input.getWhiteList()).thenReturn(Optional.of(whiteList));
        when(whiteListValidation.validate(any())).thenReturn(failedResult);
        
        /* execute */
        toTest.checkWhitelist(errors, input);
        
        /* test */
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_WHITELIST), eq("api.error.whitelist.invalid"), any());
        
    }
    
    @Test
    public void when_metadatavalidation_valid_no_api_error() throws URISyntaxException {
        /* prepare */
        when(metaDataValidation.validate(any())).thenReturn(okResult);
        
        /* execute */
        toTest.checkMetaData(errors, input);
        
        /* test */
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_METADATA), eq("api.error.metadata.invalid"), any());
    }
    
    @Test
    public void when_metadatavalidation_with_too_long_key_invalid_api_error() {
        /* prepare */
        String key = TestUtil.createRAndomString(61);
        ProjectMetaData metaData = new ProjectMetaData();
        metaData.getMetaDataMap().put(key, "value");
        
        when(input.getMetaData()).thenReturn(Optional.of(metaData));
        when(metaDataValidation.validate(any())).thenReturn(failedResult);
        
        /* execute */
        toTest.checkWhitelist(errors, input);
        
        /* test */
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_METADATA), eq("api.error.metadata.invalid"), any());
        
    }
    
    @Test
    public void when_metadatavalidation_with_too_long_value_invalid_api_error() {
        /* prepare */
        String value = TestUtil.createRAndomString(260);
        ProjectMetaData metaData = new ProjectMetaData();
        metaData.getMetaDataMap().put("key", value);
        
        when(input.getMetaData()).thenReturn(Optional.of(metaData));
        when(metaDataValidation.validate(any())).thenReturn(failedResult);
        
        /* execute */
        toTest.checkWhitelist(errors, input);
        
        /* test */
        verify(errors, never()).rejectValue(eq(ProjectJsonInput.PROPERTY_METADATA), eq("api.error.metadata.invalid"), any());
        
    }

}

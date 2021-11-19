// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterContext;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;

public class CheckmarxOAuthSupportTest {

    private static final int ONE_HOUR_IN_SECONDS = 3600;
    private CheckmarxOAuthSupport supportToTest;

    @BeforeEach
    void before() throws Exception {
        supportToTest = new CheckmarxOAuthSupport();
    }

    @Test
    void isRefreshTokenNecessary_expires_2_seconds_tested_immediately_token_refresh_is_necessary() {
        CheckmarxOAuthData oauthData = new CheckmarxOAuthData();
        oauthData.expiresInSeconds = 2;

        assertTrue(supportToTest.isTokenRefreshNecessary(createMockedContext(oauthData)));
    }

    private CheckmarxAdapterContext createMockedContext(CheckmarxOAuthData oauthData) {
        CheckmarxAdapterContext context = mock(CheckmarxAdapterContext.class);
        when(context.getoAuthData()).thenReturn(oauthData);
        return context;
    }

    @Test
    void refreshBearerTokenWhenNecessary_leads_to_newer_oauth_data_object() throws Exception {
        /* prepare */
        CheckmarxOAuthData alreadyExistingOauthData = new CheckmarxOAuthData();
        alreadyExistingOauthData.expiresInSeconds = 2;

        CheckmarxAdapterContext context = createContextWithRestSimulation(alreadyExistingOauthData);

        /* check precondition */
        assertTrue(supportToTest.isTokenRefreshNecessary(context));

        /* execute */
        supportToTest.refreshBearerTokenWhenNecessary(context);// this will refresh, because 2 seconds

        /* test */
        ArgumentCaptor<CheckmarxOAuthData> captor = ArgumentCaptor.forClass(CheckmarxOAuthData.class);
        verify(context,times(1)).markAuthenticated(captor.capture()); // only one time for 2 calls!

        CheckmarxOAuthData newOauthData = captor.getValue();
        assertNotSame(newOauthData, alreadyExistingOauthData);
        assertEquals("mytoken1", newOauthData.getAccessToken());
        assertEquals(3600, newOauthData.getExpiresInSeconds());
        
    }
    
    @Test
    void refreshBearerTokenWhenNecessary_leads_NOT_to_newer_oauth_data_object_when_no_refresh_necessary() throws Exception {
        /* prepare */
        CheckmarxOAuthData alreadyExistingOauthData = new CheckmarxOAuthData();
        alreadyExistingOauthData.expiresInSeconds = 20;

        CheckmarxAdapterContext context = createContextWithRestSimulation(alreadyExistingOauthData);

        /* check precondition */
        assertFalse(supportToTest.isTokenRefreshNecessary(context));

        /* execute */
        supportToTest.refreshBearerTokenWhenNecessary(context);// this will NOT refresh, because 20 seconds

        /* test */
        verify(context,times(0)).markAuthenticated(any()); // never called
        
    }

    private CheckmarxAdapterContext createContextWithRestSimulation(CheckmarxOAuthData alreadyExistingOauthData) {
        CheckmarxAdapterContext context = createMockedContext(alreadyExistingOauthData);
        when(context.json()).thenReturn(JSONAdapterSupport.FOR_UNKNOWN_ADAPTER);

        String url = "https://somerest.api.example.com/auth/identity/connect/token";
        when(context.getAPIURL("auth/identity/connect/token")).thenReturn(url);

        CheckmarxAdapterConfig config = mock(CheckmarxAdapterConfig.class);
        RestOperations restOperations = mock(RestOperations.class);

        ResponseEntity<String> repsonseEntity1 = new ResponseEntity<>("{\"access_token\": \"mytoken1\",\"expires_in\": 3600,\"token_type\": \"Bearer\"}",
                HttpStatus.OK);
        ResponseEntity<String> repsonseEntity2 = new ResponseEntity<>("{\"access_token\": \"mytoken2\",\"expires_in\": 3600,\"token_type\": \"Bearer\"}",
                HttpStatus.OK);

        when(restOperations.postForEntity(eq(url), any(), eq(String.class))).thenReturn(repsonseEntity1).thenReturn(repsonseEntity2);
        when(context.getConfig()).thenReturn(config);
        when(context.getRestOperations()).thenReturn(restOperations);
        return context;
    }

    @Test
    void isRefreshTokenNecessary_expires_7_seconds_tested_immediately_token_refresh_is_NOT_necessary() {
        CheckmarxOAuthData oauthData = new CheckmarxOAuthData();
        oauthData.expiresInSeconds = 7;

        assertFalse(supportToTest.isTokenRefreshNecessary(createMockedContext(oauthData)));
    }

    @Test
    void isRefreshTokenNecessary_expires_one_hour_immediately_tested_no_token_refresh_necessary() {
        CheckmarxOAuthData oauthData = new CheckmarxOAuthData();
        oauthData.expiresInSeconds = ONE_HOUR_IN_SECONDS;

        assertFalse(supportToTest.isTokenRefreshNecessary(createMockedContext(oauthData)));
    }

    @Test
    void isRefreshTokenNecessary_expires_in_one_hour__oauthdata_created_59minutes_minus_50_seconds_before__no_token_refresh_necessary() {
        CheckmarxOAuthData oauthData = new CheckmarxOAuthData();
        oauthData.creationTimeMillis = Instant.now().minus(59, ChronoUnit.MINUTES).minusSeconds(50).toEpochMilli();
        oauthData.expiresInSeconds = ONE_HOUR_IN_SECONDS;

        assertFalse(supportToTest.isTokenRefreshNecessary(createMockedContext(oauthData)));
    }

    @Test
    void isRefreshTokenNecessary_expires_in_one_hour__oauthdata_created_59minutes_and_57_seconds_before__token_refresh_IS_necessary() {
        CheckmarxOAuthData oauthData = new CheckmarxOAuthData();
        oauthData.creationTimeMillis = Instant.now().minus(59, ChronoUnit.MINUTES).minusSeconds(57).toEpochMilli();
        oauthData.expiresInSeconds = ONE_HOUR_IN_SECONDS;

        assertTrue(supportToTest.isTokenRefreshNecessary(createMockedContext(oauthData)));
    }

    @Test
    void test_data_can_be_extracted() throws AdapterException {
        /* prepare */
        String data = "{\"access_token\":\"12345MeUdnk6O_-EEp93I1e8rsdlHvBg\",\"expires_in\":86400,\"token_type\":\"Bearer\"}{Cache-Control=[no-store, no-cache, max-age=0, private], Pragma=[no-cache], Content-Length=[1786], Content-Type=[application/json; charset=utf-8], Server=[Microsoft-IIS/8.5], X-AspNet-Version=[4.0.30319], X-Powered-By=[ASP.NET], Date=[Tue, 25 Sep 2018 13:29:26 GMT]}";

        /* execute */
        CheckmarxOAuthData result = supportToTest.extractFromJson(JSONAdapterSupport.FOR_UNKNOWN_ADAPTER, data);

        /* test */
        assertNotNull(result);
        assertEquals("Bearer", result.getTokenType());
        assertEquals("12345MeUdnk6O_-EEp93I1e8rsdlHvBg", result.getAccessToken());
        assertEquals(86400, result.getExpiresInSeconds());
    }

}

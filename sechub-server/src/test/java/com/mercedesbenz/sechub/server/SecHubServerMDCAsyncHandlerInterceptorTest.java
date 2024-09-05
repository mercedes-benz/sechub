// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import com.mercedesbenz.sechub.sharedkernel.LogConstants;
import com.mercedesbenz.sechub.test.SecHubTestURLBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecHubServerMDCAsyncHandlerInterceptorTest {

    private static final String DELETE_ME_ON_CLEAR = "delete-this-key";
    private SecHubServerMDCAsyncHandlerInterceptor interceptorToTest;
    private Object handler;
    private HttpServletResponse response;
    private HttpServletRequest request;

    @Before
    public void before() throws Exception {
        MDC.put(DELETE_ME_ON_CLEAR, "should be cleared");
        interceptorToTest = new SecHubServerMDCAsyncHandlerInterceptor();

        response = mock(HttpServletResponse.class);
        request = mock(HttpServletRequest.class);
    }

    @Test
    public void mdc_is_cleared() throws Exception {
        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertNull(MDC.get(DELETE_ME_ON_CLEAR));
    }

    @Test
    public void when_url_is_null_MDC_contains_not_jobuuid() throws Exception {
        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertNull(MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
    }

    @Test
    public void when_url_is_localhost_MDC_contains_not_jobuuid_no_projectid() throws Exception {
        /* prepare */
        when(request.getRequestURI()).thenReturn("https://localhost");

        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertNull(MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
        assertNull(MDC.get(LogConstants.MDC_SECHUB_PROJECT_ID));
    }

    @Test
    public void when_url_is_localhost_project_MDC_contains_not_jobuuid_no_projectid() throws Exception {
        /* prepare */
        when(request.getRequestURI()).thenReturn("https://localhost/api/project");

        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertNull(MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
    }

    @Test
    public void when_url_is_localhost_project_myprojectId_MDC_contains_not_jobuuid_but_projectid() throws Exception {
        /* prepare */
        when(request.getRequestURI()).thenReturn("https://localhost/api/project/myprojectId");

        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertNull(MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
        assertEquals("myprojectId", MDC.get(LogConstants.MDC_SECHUB_PROJECT_ID));
    }

    @Test
    public void when_url_is_localhost_project_projectId_job_notUUID_contains_not_jobuuid_but_projectid() throws Exception {
        /* prepare */
        when(request.getRequestURI()).thenReturn("https://localhost/api/project/myprojectId/job/jobUUID");

        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertNull(MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
        assertEquals("myprojectId", MDC.get(LogConstants.MDC_SECHUB_PROJECT_ID));
    }

    @Test
    public void when_url_is_localhost_project_projectId_job_real_UUID_contains_jobuuid_and_projectid() throws Exception {
        /* prepare */
        UUID uuid = UUID.randomUUID();
        when(request.getRequestURI()).thenReturn("https://localhost/api/project/myprojectId/job/" + uuid.toString());

        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertEquals(uuid.toString(), MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
        assertEquals("myprojectId", MDC.get(LogConstants.MDC_SECHUB_PROJECT_ID));
    }

    @Test
    public void when_url_is_user_removes_false_positives_from_project_job_uuid_is_set_and_projectId_as_well() throws Exception {
        /* prepare */
        UUID uuid = UUID.randomUUID();
        when(request.getRequestURI())
                .thenReturn(SecHubTestURLBuilder.https(8443).buildUserRemovesFalsePositiveEntryFromProject("myprojectId", uuid.toString(), "findingid1"));

        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertEquals(uuid.toString(), MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
        assertEquals("myprojectId", MDC.get(LogConstants.MDC_SECHUB_PROJECT_ID));
    }

    @Test
    public void when_url_is_user_removes_project_data_false_positives_from_project_no_job_uuid_is_expected() throws Exception {
        /* prepare */
        when(request.getRequestURI())
                .thenReturn("https://localhost/api/project/myprojectId/false-positive/project-data/unique-id");

        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertNull(MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
        assertEquals("myprojectId", MDC.get(LogConstants.MDC_SECHUB_PROJECT_ID));
    }

    @Test
    public void when_url_is_user_buildApproveJobUrluuid_is_set_and_projectId_as_well() throws Exception {
        /* prepare */
        UUID uuid = UUID.randomUUID();
        when(request.getRequestURI()).thenReturn(SecHubTestURLBuilder.https(8443).buildApproveJobUrl("myprojectId", uuid.toString()));

        /* execute */
        interceptorToTest.preHandle(request, response, handler);

        /* test */
        assertEquals(uuid.toString(), MDC.get(LogConstants.MDC_SECHUB_JOB_UUID));
        assertEquals("myprojectId", MDC.get(LogConstants.MDC_SECHUB_PROJECT_ID));
    }

}

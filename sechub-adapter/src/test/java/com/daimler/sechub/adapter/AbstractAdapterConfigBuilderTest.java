// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static org.junit.Assert.*;
import java.util.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.adapter.testclasses.TestAdapterConfigBuilder;
import com.daimler.sechub.adapter.testclasses.TestAdapterConfigInterface;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class AbstractAdapterConfigBuilderTest {

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    @Test
    public void getter_for_set_password_results_in_same_string_even_when_sealed() {
        /*
         * we use now a sealed object to contain password in config builder and config
         * it self
         */
        TestAdapterConfigBuilder builderToTest = new TestAdapterConfigBuilder();

        /* execute */
        TestAdapterConfigInterface config = builderToTest.setPasswordOrAPIToken("my-password").build();

        /* test */
        assertEquals("my-password", config.getPasswordOrAPIToken());

    }

    @Test
    public void when_projectId_is_set_its_also_in_configuration() {
        /* execute */
        TestAdapterConfigInterface configToTest = validConfigAnd().setProjectId("myproject").build();

        /* test */
        assertEquals("myproject", configToTest.getProjectId());

    }

    @Test
    public void isProxyDefined_false_no_proxy_set_getProxyHostname_returns_null() {
        /* prepare */
        TestAdapterConfigInterface config = validConfigAnd().setProxyPort(888).build();

        /* execute + test */
        assertNull(config.getProxyHostname());
        assertFalse(config.isProxyDefined());

    }

    @Test
    public void defining_a_proxy_hostname_but_no_port_throws_illegal_state() {
        /* prepare */
        expected.expect(IllegalStateException.class);

        /* execute */
        validConfigAnd().setProxyHostname("proxyname").build();

    }

    @Test
    public void isProxyDefined_true_when_proxy_set_getProxyHostname_and_port_config_returns_both_values() {
        /* prepare */
        TestAdapterConfigInterface config = validConfigAnd().setProxyHostname("proxyname").setProxyPort(9858).build();

        /* execute + test */
        assertEquals("proxyname", config.getProxyHostname());
        assertEquals(9858, config.getProxyPort());
        assertTrue(config.isProxyDefined());
    }

    @Test
    public void get_trace_id_returned_as_defined() throws Exception {
        /* prepare */
        /* execute */
        TestAdapterConfigInterface cfg = validConfigAnd().setTraceID("myTraceID").build();

        /* test */
        assertEquals("myTraceID", cfg.getTraceID());
    }

    @Test
    public void get_trace_id_returned_not_null_even_when_defined_null_as_defined() throws Exception {

        /* prepare */
        /* execute */
        TestAdapterConfigInterface cfg = validConfigAnd().setTraceID(null).build();

        /* test */
        assertNotNull(cfg.getTraceID());
    }

    @Test
    public void config_without_timetowait_set_has_one_minute() {
        TestAdapterConfigInterface cf1 = validConfigAnd().build();

        assertEquals(1 * 60 * 1000, cf1.getTimeToWaitForNextCheckOperationInMilliseconds());
    }

    @Test
    public void config_with_timetowait_n1_has_one_minute() {
        TestAdapterConfigInterface cf1 = validConfigAnd().setTimeToWaitForNextCheckOperationInMinutes(-1).build();

        assertEquals(1 * 60 * 1000, cf1.getTimeToWaitForNextCheckOperationInMilliseconds());
    }

    @Test
    public void config_without_timeout_set_has_5_days_per_default() {
        TestAdapterConfigInterface cf1 = validConfigAnd().build();

        /* 5 days a 24 hours a 60 minutes a 60 seconds a 1000 milliseconds */
        assertEquals(5 * 24 * 60 * 60 * 1000, cf1.getTimeOutInMilliseconds());
    }

    @Test
    public void config_with_timeout_n1_set_has_5_days_per_default() {
        TestAdapterConfigInterface cf1 = validConfigAnd().setTimeOutInMinutes(-1).build();

        /* 5 days a 24 hours a 60 minutes a 60 seconds a 1000 milliseconds */
        assertEquals(5 * 24 * 60 * 60 * 1000, cf1.getTimeOutInMilliseconds());
    }

    @Test
    public void config_with_timeout_set_with_3_minutes_has_3_minutes() {
        TestAdapterConfigInterface cf1 = validConfigAnd().setTimeOutInMinutes(3).build();

        /* 3 hours a 60 minutes a 60 seconds a 1000 milliseconds */
        assertEquals(3 * 60 * 1000, cf1.getTimeOutInMilliseconds());
    }

    @Test
    public void config_with_time_to_check_set_with_3_minutes_has_3_minutes() {
        TestAdapterConfigInterface cf1 = validConfigAnd().setTimeToWaitForNextCheckOperationInMinutes(3).build();

        /* 3 hours a 60 minutes a 60 seconds a 1000 milliseconds */
        assertEquals(3 * 60 * 1000, cf1.getTimeToWaitForNextCheckOperationInMilliseconds());
    }

    @Test
    public void config_with_time_to_check_set_with_3x60x1000_millis_has_3_minutes() {
        TestAdapterConfigInterface cf1 = validConfigAnd().setTimeToWaitForNextCheckOperationInMilliseconds(3 * 60 * 1000).build();

        /* 3 hours a 60 minutes a 60 seconds a 1000 milliseconds */
        assertEquals(3 * 60 * 1000, cf1.getTimeToWaitForNextCheckOperationInMilliseconds());
    }

    @Test
    public void config_userid_password_set_has_correct_Base64_token() {
        /* prepare */
        String pwdOrApiToken = "pwd-developer";
        String user = "developer";

        /* execute */
        TestAdapterConfigInterface cf1 = validConfigAnd().setUser(user).setPasswordOrAPIToken(pwdOrApiToken).build();

        /* test */
        String base64Encoded = cf1.getCredentialsBase64Encoded();
        String base64Decoded = new String(Base64.getDecoder().decode(base64Encoded));
        assertEquals(user + ":" + pwdOrApiToken, base64Decoded);
    }

    private TestAdapterConfigBuilder validConfigAnd() {
        return new TestAdapterConfigBuilder().setProductBaseUrl("baseUrl");
    }

}

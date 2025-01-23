// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.netsparker;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.BasicLoginConfig;
import com.mercedesbenz.sechub.adapter.FormScriptLoginConfig;
import com.mercedesbenz.sechub.adapter.LoginScriptAction;
import com.mercedesbenz.sechub.adapter.LoginScriptPage;
import com.mercedesbenz.sechub.adapter.SecHubTimeUnitData;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;
import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.login.ActionType;

public class NetsparkerAdapterV1Test {

    private NetsparkerAdapterV1 adapterToTest;
    private NetsparkerAdapterContext context;
    private NetsparkerAdapterConfig config;
    private RestTemplate template;
    private JSONAdapterSupport jsonAdapterSupport;

    @Before
    public void before() {
        // System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        adapterToTest = new NetsparkerAdapterV1();

        context = mock(NetsparkerAdapterContext.class);
        config = mock(NetsparkerAdapterConfig.class);
        template = mock(RestTemplate.class);

        jsonAdapterSupport = new JSONAdapterSupport(adapterToTest, context);

        when(context.getConfig()).thenReturn(config);
        when(context.getRestOperations()).thenReturn(template);
        when(config.getProductBaseURL()).thenReturn("baseURL");
        when(context.getProductContextId()).thenReturn("netsparkerId");
        when(context.json()).thenReturn(jsonAdapterSupport);
    }

    @Test
    public void build_json_for_new_scan_with_basic_auth_contains_basic_login_parts() throws Exception {
        /* prepare */
        BasicLoginConfig basicLoginConfig = mock(BasicLoginConfig.class);
        when(config.getLoginConfig()).thenReturn(basicLoginConfig);
        when(basicLoginConfig.asBasic()).thenReturn(basicLoginConfig);
        when(basicLoginConfig.isBasic()).thenReturn(true);

        when(basicLoginConfig.getLoginURL()).thenReturn(new URL("https://www.example.com/"));
        when(basicLoginConfig.getUser()).thenReturn("weblogin-user");
        when(basicLoginConfig.getPassword()).thenReturn("weblogin-password");
        when(basicLoginConfig.getRealm()).thenReturn("www.example.com");

        /* execute */
        String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);

        /* test */
        String expected = TestNetsparkerAdapterFileSupport.getTestfileSupport().loadTestFile("json/basic_weblogin_expected1.json");
        assertEquals(expected, json);
    }

    @Test
    public void build_json_for_new_scan_with_max_scan_duration_one_hour() throws Exception {
        /* prepare */
        SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(60, SecHubTimeUnit.MINUTE);
        when(config.getMaxScanDuration()).thenReturn(maxScanDuration);
        when(config.hasMaxScanDuration()).thenReturn(true);

        /* execute */
        String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);

        /* test */
        String expected = TestNetsparkerAdapterFileSupport.getTestfileSupport().loadTestFile("json/max_duration_one_hour_expected.json");
        assertEquals(expected, json);
    }

    /*
     * In Netsparker the max scan duration has to be in hours not minutes. As a
     * result, 5 minutes need to be converter to hours. 5 minutes will be rounded up
     * to one hour (60 minutes), because 1 hour is the minimum possible value for
     * max scan duration in Netsparker.
     */
    @Test
    public void build_json_for_new_scan_with_max_scan_duration_5_minutes() throws Exception {
        /* prepare */
        SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(5, SecHubTimeUnit.MINUTE);
        when(config.getMaxScanDuration()).thenReturn(maxScanDuration);
        when(config.hasMaxScanDuration()).thenReturn(true);

        /* execute */
        String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);

        /* test */

        // the minimum scan duration for Netsparker is 1 hour
        String expected = TestNetsparkerAdapterFileSupport.getTestfileSupport().loadTestFile("json/max_duration_one_hour_expected.json");
        assertEquals(expected, json);
    }

    /*
     * In Netsparker the max scan duration has to be in hours not minutes. As a
     * result, 129 minutes need to be converter to hours. 129 minutes will be
     * rounded down to 2 hours (120 minutes).
     */
    @Test
    public void build_json_for_new_scan_with_max_scan_duration_129_minutes() throws Exception {
        /* prepare */
        SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(129, SecHubTimeUnit.MINUTE);
        when(config.getMaxScanDuration()).thenReturn(maxScanDuration);
        when(config.hasMaxScanDuration()).thenReturn(true);

        /* execute */
        String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);

        /* test */
        String expected = TestNetsparkerAdapterFileSupport.getTestfileSupport().loadTestFile("json/max_duration_three_hours_expected.json");
        assertEquals(expected, json);
    }

    /*
     * In Netsparker the max scan duration has to be in hours not minutes. As a
     * result, 179 minutes need to be converter to hours. 179 minutes will be
     * rounded down to 2 hours (120 minutes).
     */
    @Test
    public void build_json_for_new_scan_with_max_scan_duration_180_minutes() throws Exception {
        /* prepare */
        SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(180, SecHubTimeUnit.MINUTE);
        when(config.getMaxScanDuration()).thenReturn(maxScanDuration);
        when(config.hasMaxScanDuration()).thenReturn(true);

        /* execute */
        String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);

        /* test */
        String expected = TestNetsparkerAdapterFileSupport.getTestfileSupport().loadTestFile("json/max_duration_three_hours_expected.json");
        assertEquals(expected, json);
    }

    @Test
    public void build_json_for_new_scan_with_form_auth_scripts() throws Exception {
        /* prepare */
        FormScriptLoginConfig formScriptLoginConfig = mock(FormScriptLoginConfig.class);
        when(config.getLoginConfig()).thenReturn(formScriptLoginConfig);
        when(formScriptLoginConfig.asFormScript()).thenReturn(formScriptLoginConfig);
        when(formScriptLoginConfig.isFormScript()).thenReturn(true);

        when(formScriptLoginConfig.getLoginURL()).thenReturn(new URL("https://www.example.com/login"));
        when(formScriptLoginConfig.getUserName()).thenReturn("weblogin-user");
        when(formScriptLoginConfig.getPassword()).thenReturn("weblogin-password");

        LinkedList<LoginScriptPage> pages = new LinkedList<>();

        /* page 1 */
        LoginScriptPage page1 = mock(LoginScriptPage.class);

        LoginScriptAction action1 = mock(LoginScriptAction.class);
        when(action1.getActionType()).thenReturn(ActionType.CLICK);
        when(action1.isClick()).thenReturn(true);
        when(action1.getSelector()).thenReturn("#openLoginForm");

        List<LoginScriptAction> page1Actions = new LinkedList<>();
        page1Actions.add(action1);
        when(page1.getActions()).thenReturn(page1Actions);

        /* page 2 */
        LoginScriptPage page2 = mock(LoginScriptPage.class);
        when(page2.getActions()).thenReturn(new LinkedList<>());

        LoginScriptAction action2 = mock(LoginScriptAction.class);
        when(action2.getActionType()).thenReturn(ActionType.USERNAME);
        when(action2.isUserName()).thenReturn(true);
        when(action2.getSelector()).thenReturn("#username");

        LoginScriptAction action3 = mock(LoginScriptAction.class);
        when(action3.getActionType()).thenReturn(ActionType.CLICK);
        when(action3.isClick()).thenReturn(true);
        when(action3.getSelector()).thenReturn("#next");

        List<LoginScriptAction> page2Actions = new LinkedList<>();
        page2Actions.add(action2);
        page2Actions.add(action3);
        when(page2.getActions()).thenReturn(page2Actions);

        /* page 3 */
        LoginScriptPage page3 = mock(LoginScriptPage.class);
        when(page3.getActions()).thenReturn(new LinkedList<>());

        LoginScriptAction action4 = mock(LoginScriptAction.class);
        when(action4.getActionType()).thenReturn(ActionType.PASSWORD);
        when(action4.isPassword()).thenReturn(true);
        when(action4.getSelector()).thenReturn("#password");

        LoginScriptAction action5 = mock(LoginScriptAction.class);
        when(action5.getActionType()).thenReturn(ActionType.INPUT);
        when(action5.isInput()).thenReturn(true);
        when(action5.getSelector()).thenReturn("#email");
        when(action5.getValue()).thenReturn("user@example.org");
        when(action5.getDescription()).thenReturn("The email field");

        LoginScriptAction action6 = mock(LoginScriptAction.class);
        when(action6.getActionType()).thenReturn(ActionType.WAIT);
        when(action6.isWait()).thenReturn(true);
        when(action6.getValue()).thenReturn("3240");
        when(action6.getUnit()).thenReturn(SecHubTimeUnit.MILLISECOND);

        LoginScriptAction action7 = mock(LoginScriptAction.class);
        when(action7.getActionType()).thenReturn(ActionType.CLICK);
        when(action7.isClick()).thenReturn(true);
        when(action7.getSelector()).thenReturn("#login");

        List<LoginScriptAction> page3Actions = new LinkedList<>();
        page3Actions.add(action4);
        page3Actions.add(action5);
        page3Actions.add(action6);
        page3Actions.add(action7);
        when(page3.getActions()).thenReturn(page3Actions);

        /* page 4 */
        LoginScriptPage page4 = mock(LoginScriptPage.class);

        LoginScriptAction action8 = mock(LoginScriptAction.class);
        when(action8.getActionType()).thenReturn(ActionType.WAIT);
        when(action8.isWait()).thenReturn(true);
        when(action8.getValue()).thenReturn("2");
        when(action8.getUnit()).thenReturn(SecHubTimeUnit.SECOND);

        List<LoginScriptAction> page4Actions = new LinkedList<>();
        page4Actions.add(action8);
        when(page4.getActions()).thenReturn(page4Actions);

        pages.add(page1);
        pages.add(page2);
        pages.add(page3);
        pages.add(page4);

        when(formScriptLoginConfig.getPages()).thenReturn(pages);

        /* execute */
        String json = adapterToTest.buildJsonForCreateNewScan(jsonAdapterSupport, config);

        /* test */
        String expected = TestNetsparkerAdapterFileSupport.getTestfileSupport().loadTestFile("json/form_scripts_weblogin.json");
        assertEquals(expected, json);
    }

    @Test
    public void a_fetch_report__triggers_rest_tempate_with_correct_params() {
        /* prepare */

        @SuppressWarnings("unchecked")
        ResponseEntity<String> response = mock(ResponseEntity.class);
        when(template.getForEntity(eq("baseURL/api/1.0/scans/report/netsparkerId?Type=Vulnerabilities&Format=Xml"), eq(String.class))).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        /* execute */
        adapterToTest.fetchReport(context);

        /* test */
        verify(template).getForEntity("baseURL/api/1.0/scans/report/netsparkerId?Type=Vulnerabilities&Format=Xml", String.class);
    }

    @Test
    public void isAbleTo_extract_id_from_netsparker_v1_0_40_109_result_when_create_new_scan_triggered() throws AdapterException {
        /* prepare */
        String body = TestNetsparkerAdapterFileSupport.getTestfileSupport().loadTestFile("netsparker_v1.0.40.109_new_scan_output.json");

        /* execute */
        String id = adapterToTest.extractIDFromScanResult(body, context);

        /* test */
        assertEquals("a42ab3cf-58e8-455e-6668-a88503af65fe", id);

    }

    @Test
    public void api_prefix_is_api_slash_1_0() throws Exception {

        /* test */
        assertEquals("api/1.0", adapterToTest.getAPIPrefix());
    }
}
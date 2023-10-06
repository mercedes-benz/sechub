// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.HTTPHeaderConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.BasicLoginConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ProxyInformation;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.zapwrapper.config.auth.SessionManagementType;
import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.RuleReference;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;
import com.mercedesbenz.sechub.zapwrapper.helper.IncludeExcludeToZapURLHelper;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapPDSEventHandler;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapProductMessageHelper;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapURLType;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiFacade;
import com.mercedesbenz.sechub.zapwrapper.scan.ZapScanner.UserInformation;
import com.mercedesbenz.sechub.zapwrapper.util.SystemUtil;

class ZapScannerTest {

    private ZapScanner scannerToTest;

    private ClientApiFacade clientApiFacade;
    private ZapScanContext scanContext;
    private ZapPDSEventHandler zapPDSEventHandler;
    private SystemUtil systemUtil;

    private ZapProductMessageHelper helper;
    private String contextName = "context-name";

    @BeforeEach
    void beforeEach() {
        // create mocks
        clientApiFacade = mock(ClientApiFacade.class);
        scanContext = mock(ZapScanContext.class);
        systemUtil = mock(SystemUtil.class);
        helper = mock(ZapProductMessageHelper.class);

        zapPDSEventHandler = mock(ZapPDSEventHandler.class);

        // assign mocks
        scannerToTest = ZapScanner.from(clientApiFacade, scanContext);
        scannerToTest.systemUtil = systemUtil;

        // set global behavior
        when(scanContext.getContextName()).thenReturn(contextName);
        when(scanContext.getZapProductMessageHelper()).thenReturn(helper);
        when(scanContext.getZapPDSEventHandler()).thenReturn(zapPDSEventHandler);

        doNothing().when(helper).writeProductError(any());
        doNothing().when(helper).writeProductMessages(any());
        doNothing().when(helper).writeSingleProductMessage(any());
        doNothing().when(helper).writeUserMessagesWithScannedURLs(any());

        doNothing().when(systemUtil).waitForMilliseconds(ZapScanner.CHECK_SCAN_STATUS_TIME_IN_MILLISECONDS);
        when(systemUtil.getCurrentTimeInMilliseconds()).thenCallRealMethod();
    }

    @Test
    void setup_standard_configuration_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(clientApiFacade.createNewSession(scanContext.getContextName(), "true")).thenReturn(null);
        when(clientApiFacade.configureMaximumAlertsForEachRule("0")).thenReturn(null);
        when(clientApiFacade.enableAllPassiveScannerRules()).thenReturn(null);
        when(clientApiFacade.enableAllActiveScannerRulesForPolicy(null)).thenReturn(null);
        when(clientApiFacade.configureAjaxSpiderBrowserId("firefox-headless")).thenReturn(null);

        /* execute */
        scannerToTest.setupStandardConfiguration();

        /* test */
        verify(clientApiFacade, times(1)).createNewSession(scanContext.getContextName(), "true");
        verify(clientApiFacade, times(1)).configureMaximumAlertsForEachRule("0");
        verify(clientApiFacade, times(1)).enableAllPassiveScannerRules();
        verify(clientApiFacade, times(1)).enableAllActiveScannerRulesForPolicy(null);
        verify(clientApiFacade, times(1)).configureAjaxSpiderBrowserId("firefox-headless");
    }

    @Test
    void deactivate_rules_ruleset_or_rules_to_deactivate_null_results_in_nothing_is_configured() throws ClientApiException {
        /* prepare */
        DeactivatedRuleReferences deactivatedReferences = mock(DeactivatedRuleReferences.class);
        when(deactivatedReferences.getDeactivatedRuleReferences()).thenReturn(null);

        /* execute */
        scannerToTest.deactivateRules(null, null);
        scannerToTest.deactivateRules(new ZapFullRuleset(), null);
        scannerToTest.deactivateRules(null, new DeactivatedRuleReferences());
        scannerToTest.deactivateRules(new ZapFullRuleset(), deactivatedReferences);

        /* test */
        verify(clientApiFacade, never()).disablePassiveScannerRule(any());
        verify(clientApiFacade, never()).disableActiveScannerRuleForPolicy(any(), any());
    }

    @Test
    void deactivate_rules_results_in_rules_are_deactivated() throws ClientApiException {
        /* prepare */
        DeactivatedRuleReferences deactivatedReferences = new DeactivatedRuleReferences();
        // passive rules to deactivate
        deactivatedReferences.addRuleReference(new RuleReference("Timestamp-Disclosure-10096", "first-info"));
        // active rules to deactivate
        deactivatedReferences.addRuleReference(new RuleReference("Cross-Site-Scripting-(Reflected)-40012", "second-info"));
        deactivatedReferences.addRuleReference(new RuleReference("Path-Traversal-6", "third-info"));

        String json = TestFileReader.loadTextFile("src/test/resources/zap-available-rules/zap-full-ruleset.json");
        ZapFullRuleset ruleSet = new ZapFullRuleset().fromJSON(json);

        when(clientApiFacade.disablePassiveScannerRule(any())).thenReturn(null);
        when(clientApiFacade.disableActiveScannerRuleForPolicy(any(), any())).thenReturn(null);

        /* execute */
        scannerToTest.deactivateRules(ruleSet, deactivatedReferences);

        /* test */
        verify(clientApiFacade, times(1)).disablePassiveScannerRule(any());
        verify(clientApiFacade, times(2)).disableActiveScannerRuleForPolicy(any(), any());
    }

    @Test
    void setup_addtional_proxy_information_with_proxy_information_null_results_in_proxy_disabled()
            throws ClientApiException {
        /* prepare */
        when(clientApiFacade.setHttpProxyEnabled("false")).thenReturn(null);

        /* execute */
        scannerToTest.setupAdditonalProxyConfiguration(null);

        /* test */
        verify(clientApiFacade, times(1)).setHttpProxyEnabled("false");
    }

    @Test
    void setup_addtional_proxy_information_results_in_proxy_enabled() throws ClientApiException {
        /* prepare */
        String host = "127.0.0.1";
        int port = 8000;
        var portAsString = String.valueOf(port);
        ProxyInformation proxyInformation = new ProxyInformation(host, port);

        when(clientApiFacade.configureHttpProxy(host, portAsString, null, null, null)).thenReturn(null);
        when(clientApiFacade.setHttpProxyEnabled("true")).thenReturn(null);
        when(clientApiFacade.setHttpProxyAuthEnabled("false")).thenReturn(null);

        /* execute */
        scannerToTest.setupAdditonalProxyConfiguration(proxyInformation);

        /* test */
        verify(clientApiFacade, times(1)).configureHttpProxy(host, portAsString, null, null, null);
        verify(clientApiFacade, times(1)).setHttpProxyEnabled("true");
        verify(clientApiFacade, times(1)).setHttpProxyAuthEnabled("false");
    }

    @Test
    void create_context_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String expectedContextId = "random-id";
        when(clientApiFacade.createNewContext(contextName)).thenReturn(expectedContextId);

        /* execute */
        String contextId = scannerToTest.createContext();

        /* test */
        assertEquals(expectedContextId, contextId);
        verify(scanContext, times(2)).getContextName();
        verify(clientApiFacade, times(1)).createNewContext(contextName);
    }

    @Test
    void add_replacer_rules_for_headers_with_no_headers_results_add_replacer_rule_is_never_called() throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubwebScanConfig = new SecHubWebScanConfiguration();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubwebScanConfig);

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(response);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        verify(clientApiFacade, never()).addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("headerPartWithoutOnlyForUrlsTestNamedArguments")
    void add_replacer_rules_for_headers_with_no_onlyForUrls_results_add_replacer_rule_is_called_once_for_each_header(String sechubScanConfigJSON)
            throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(response);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        int times = sechubWebScanConfig.getHeaders().get().size();
        verify(clientApiFacade, times(times)).addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("headerPartWithOnlyForUrlsTestNamedArguments")
    void add_replacer_rules_for_headers_with_onlyForUrls_results_add_replacer_rule_is_called_once_for_each_onylForUrl(String sechubScanConfigJSON)
            throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(response);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        int times = sechubWebScanConfig.getHeaders().get().size();
        for (HTTPHeaderConfiguration header : sechubWebScanConfig.getHeaders().get()) {
            if (header.getOnlyForUrls().isPresent()) {
                // minus 1 because the method will called for any header at least once
                times += header.getOnlyForUrls().get().size() - 1;
            }
        }
        verify(clientApiFacade, times(times)).addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-include-exclude.json" })
    void set_includes_and_excludes_api_facade_is_called_once_for_each_include_and_once_for_exclude(String sechubConfigFile)
            throws ClientApiException, MalformedURLException {
        /* prepare */
        String json = TestFileReader.loadTextFile(sechubConfigFile);

        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();
        IncludeExcludeToZapURLHelper helper = new IncludeExcludeToZapURLHelper();

        URL targetUrl = sechubWebScanConfig.getUrl().toURL();
        List<String> includesList = sechubWebScanConfig.getIncludes().get();
        Set<URL> includes = new HashSet<>(helper.createListOfUrls(ZapURLType.INCLUDE, targetUrl, includesList, new ArrayList<>()));
        when(scanContext.getZapURLsIncludeSet()).thenReturn(includes);

        List<String> excludesList = sechubWebScanConfig.getExcludes().get();
        Set<URL> excludes = new HashSet<>(helper.createListOfUrls(ZapURLType.EXCLUDE, targetUrl, excludesList, new ArrayList<>()));
        when(scanContext.getZapURLsExcludeSet()).thenReturn(excludes);

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.addIncludeUrlPatternToContext(any(), any())).thenReturn(response);
        when(clientApiFacade.accessUrlViaZap(any(), any())).thenReturn(response);
        when(clientApiFacade.addExcludeUrlPatternToContext(any(), any())).thenReturn(response);

        /* execute */
        scannerToTest.addIncludedAndExcludedUrlsToContext();

        /* test */
        verify(clientApiFacade, times(includes.size())).addIncludeUrlPatternToContext(any(), any());
        verify(clientApiFacade, times(includes.size())).accessUrlViaZap(any(), any());
        verify(clientApiFacade, times(excludes.size())).addExcludeUrlPatternToContext(any(), any());
    }

    @Test
    void import_openapi_file_but_api_file_is_null_api_facade_is_never_called() throws ClientApiException {
        /* prepare */
        String contextId = "context-id";
        when(scanContext.getApiDefinitionFiles()).thenReturn(Collections.emptyList());

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.importOpenApiFile(any(), any(), any())).thenReturn(response);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(clientApiFacade, never()).importOpenApiFile(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json" })
    void import_openapi_file_api_facade_is_called_once(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        String contextId = "context-id";
        String json = TestFileReader.loadTextFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();

        List<File> apiFiles = new ArrayList<>();
        apiFiles.add(new File("examplefile.json"));

        when(scanContext.getApiDefinitionFiles()).thenReturn(apiFiles);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.importOpenApiFile(any(), any(), any())).thenReturn(response);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(clientApiFacade, times(1)).importOpenApiFile(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json",
            "src/test/resources/sechub-config-examples/form-based-auth.json" })
    void configure_login_inside_zap_using_no_auth_and_unsupported_auth_return_null(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        String contextId = "context-id";
        String json = TestFileReader.loadTextFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();

        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        /* execute */
        UserInformation userInformation = scannerToTest.configureLoginInsideZapContext(contextId);

        /* test */
        assertEquals(null, userInformation);
    }

    @Test
    void configure_login_inside_zap_using_basic_auth_results_in_expected_calls() throws ClientApiException, MalformedURLException {
        /* prepare */
        String contextId = "context-id";
        String userId = "user-id";
        URL targetUrl = URI.create("https:127.0.0.1:8000").toURL();
        String json = TestFileReader.loadTextFile("src/test/resources/sechub-config-examples/basic-auth.json");
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();
        BasicLoginConfiguration basicLoginConfiguration = sechubWebScanConfig.getLogin().get().getBasic().get();
        String userName = new String(basicLoginConfiguration.getUser());

        ApiResponse response = mock(ApiResponse.class);

        when(scanContext.getTargetUrl()).thenReturn(targetUrl);
        when(scanContext.getAuthenticationType()).thenReturn(AuthenticationType.HTTP_BASIC_AUTHENTICATION);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiFacade.configureAuthenticationMethod(eq(contextId), eq(AuthenticationType.HTTP_BASIC_AUTHENTICATION.getZapAuthenticationMethod()), any()))
                .thenReturn(response);
        when(clientApiFacade.setSessionManagementMethod(eq(contextId), eq(SessionManagementType.HTTP_AUTH_SESSION_MANAGEMENT.getZapSessionManagementMethod()),
                any())).thenReturn(response);
        when(clientApiFacade.createNewUser(contextId, userName)).thenReturn(userId);
        when(clientApiFacade.configureAuthenticationCredentials(eq(contextId), eq(userId), any())).thenReturn(response);
        when(clientApiFacade.setForcedUser(contextId, userId)).thenReturn(response);
        when(clientApiFacade.setForcedUserModeEnabled(true)).thenReturn(response);

        /* execute */
        UserInformation userInformation = scannerToTest.configureLoginInsideZapContext(contextId);

        /* test */
        assertEquals(userName, userInformation.userName());
        assertEquals(userId, userInformation.zapuserId());

        verify(scanContext, times(2)).getTargetUrl();
        verify(scanContext, times(1)).getAuthenticationType();

        verify(clientApiFacade, times(1)).configureAuthenticationMethod(eq(contextId),
                eq(AuthenticationType.HTTP_BASIC_AUTHENTICATION.getZapAuthenticationMethod()), any());
        verify(clientApiFacade, times(1)).setSessionManagementMethod(eq(contextId),
                eq(SessionManagementType.HTTP_AUTH_SESSION_MANAGEMENT.getZapSessionManagementMethod()), any());
        verify(clientApiFacade, times(1)).createNewUser(contextId, userName);
        verify(clientApiFacade, times(1)).configureAuthenticationCredentials(eq(contextId), eq(userId), any());
        verify(clientApiFacade, times(1)).setForcedUser(contextId, userId);
        verify(clientApiFacade, times(1)).setForcedUserModeEnabled(true);
    }

    @Test
    void generate_report_calls_api_facade_once() throws ClientApiException {
        /* prepare */
        when(scanContext.getReportFile())
                .thenReturn(Paths.get("src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json"));
        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.generateReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any())).thenReturn(response);

        /* execute */
        scannerToTest.generateZapReport();

        /* test */
        verify(clientApiFacade, times(1)).generateReport(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any());
    }

    @Test
    void cleanup_after_scan() throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubwebScanConfig = new SecHubWebScanConfiguration();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubwebScanConfig);

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.removeReplacerRule(any())).thenReturn(response);

        /* execute */
        scannerToTest.cleanUp();

        /* test */
        verify(clientApiFacade, never()).removeReplacerRule(any());
    }

    @ParameterizedTest
    @MethodSource("headerPartWithoutOnlyForUrlsTestNamedArguments")
    void cleanup_after_scan_without_onylForUrls_headers_set_cleans_up_all_replacer_rules(String sechubScanConfigJSON) throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.removeReplacerRule(any())).thenReturn(response);

        /* execute */
        scannerToTest.cleanUp();

        /* test */
        int times = sechubWebScanConfig.getHeaders().get().size();
        verify(clientApiFacade, times(times)).removeReplacerRule(any());
    }

    @ParameterizedTest
    @MethodSource("headerPartWithOnlyForUrlsTestNamedArguments")
    void cleanup_after_scan_with_onylForUrls_headers_set_cleans_up_all_replacer_rules(String sechubScanConfigJSON) throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        ApiResponse response = mock(ApiResponse.class);
        when(clientApiFacade.removeReplacerRule(any())).thenReturn(response);

        /* execute */
        scannerToTest.cleanUp();

        /* test */
        int times = sechubWebScanConfig.getHeaders().get().size();
        for (HTTPHeaderConfiguration header : sechubWebScanConfig.getHeaders().get()) {
            if (header.getOnlyForUrls().isPresent()) {
                // minus 1 because the method will called for any header at least once
                times += header.getOnlyForUrls().get().size() - 1;
            }
        }
        verify(clientApiFacade, times(times)).removeReplacerRule(any());
    }

    @Test
    void wait_for_ajaxSpider_scan_is_cancelled_results_in_exception_with_dedicated_exit_code() throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(zapPDSEventHandler).cancelScan(contextName);

        when(scanContext.getMaxScanDurationInMilliSeconds()).thenReturn(20000L);
        when(scanContext.isActiveScanEnabled()).thenReturn(true);

        when(clientApiFacade.stopAjaxSpider()).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForAjaxSpiderResults();
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(zapPDSEventHandler, times(2)).isScanCancelled();
        verify(scanContext, times(1)).getMaxScanDurationInMilliSeconds();
        verify(scanContext, times(1)).isActiveScanEnabled();
        verify(clientApiFacade, times(1)).stopAjaxSpider();
    }

    @Test
    void wait_for_ajaxSpider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(scanContext.getMaxScanDurationInMilliSeconds()).thenReturn(1000L);
        when(scanContext.isActiveScanEnabled()).thenReturn(true);

        when(clientApiFacade.stopAjaxSpider()).thenReturn(null);
        when(clientApiFacade.getAjaxSpiderStatus()).thenReturn("stopped");

        /* execute */
        scannerToTest.waitForAjaxSpiderResults();

        /* test */
        verify(scanContext, times(1)).getMaxScanDurationInMilliSeconds();
        verify(scanContext, times(1)).isActiveScanEnabled();
        verify(clientApiFacade, atLeast(1)).getAjaxSpiderStatus();
        verify(clientApiFacade, times(1)).stopAjaxSpider();
    }

    @Test
    void wait_for_spider_scan_is_cancelled_results_in_exception_with_dedicated_exit_code() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        when(zapPDSEventHandler.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(zapPDSEventHandler).cancelScan(contextName);

        when(scanContext.getMaxScanDurationInMilliSeconds()).thenReturn(20000L);
        when(scanContext.isActiveScanEnabled()).thenReturn(true);

        when(clientApiFacade.stopSpiderScan(scanId)).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForSpiderResults(scanId);
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(zapPDSEventHandler, times(2)).isScanCancelled();
        verify(scanContext, times(1)).getMaxScanDurationInMilliSeconds();
        verify(scanContext, times(1)).isActiveScanEnabled();
        verify(clientApiFacade, times(1)).stopSpiderScan(scanId);
    }

    @Test
    void wait_for_spider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(scanContext.getMaxScanDurationInMilliSeconds()).thenReturn(1000L);
        when(scanContext.isActiveScanEnabled()).thenReturn(true);
        ZapProductMessageHelper messageHelper = mock(ZapProductMessageHelper.class);
        when(scanContext.getZapProductMessageHelper()).thenReturn(messageHelper);
        doNothing().when(messageHelper).writeUserMessagesWithScannedURLs(any());

        when(clientApiFacade.stopSpiderScan(scanId)).thenReturn(null);
        when(clientApiFacade.getSpiderStatusForScan(scanId)).thenReturn(42);
        when(clientApiFacade.getAllSpiderUrls()).thenReturn(null);

        /* execute */
        scannerToTest.waitForSpiderResults(scanId);

        /* test */
        verify(scanContext, times(1)).getMaxScanDurationInMilliSeconds();
        verify(scanContext, times(1)).isActiveScanEnabled();
        verify(scanContext, times(1)).getZapProductMessageHelper();
        verify(messageHelper, times(1)).writeUserMessagesWithScannedURLs(any());
        verify(clientApiFacade, atLeast(1)).getSpiderStatusForScan(scanId);
        verify(clientApiFacade, times(1)).stopSpiderScan(scanId);
        verify(clientApiFacade, times(1)).getAllSpiderUrls();
    }

    @Test
    void wait_for_passiveScan_scan_is_cancelled_results_in_exception_with_dedicated_exit_code() throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(zapPDSEventHandler).cancelScan(contextName);

        when(scanContext.getMaxScanDurationInMilliSeconds()).thenReturn(20000L);
        when(scanContext.isActiveScanEnabled()).thenReturn(false);
        when(scanContext.isAjaxSpiderEnabled()).thenReturn(false);

        when(clientApiFacade.getNumberOfPassiveScannerRecordsToScan()).thenReturn(12);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.passiveScan();
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(zapPDSEventHandler, times(2)).isScanCancelled();
        verify(scanContext, times(1)).getMaxScanDurationInMilliSeconds();
        verify(scanContext, times(1)).isActiveScanEnabled();
        verify(scanContext, times(1)).isAjaxSpiderEnabled();
        verify(clientApiFacade, atLeast(1)).getNumberOfPassiveScannerRecordsToScan();
    }

    @Test
    void wait_for_passiveScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(scanContext.getMaxScanDurationInMilliSeconds()).thenReturn(20000L);
        when(scanContext.isActiveScanEnabled()).thenReturn(false);
        when(scanContext.isAjaxSpiderEnabled()).thenReturn(false);

        when(clientApiFacade.getNumberOfPassiveScannerRecordsToScan()).thenReturn(0);

        /* execute */
        scannerToTest.passiveScan();

        /* test */
        verify(scanContext, times(1)).getMaxScanDurationInMilliSeconds();
        verify(scanContext, times(1)).isActiveScanEnabled();
        verify(scanContext, times(1)).isAjaxSpiderEnabled();
        verify(clientApiFacade, times(1)).getNumberOfPassiveScannerRecordsToScan();
    }

    @Test
    void wait_for_activeScan_scan_is_cancelled_results_in_exception_with_dedicated_exit_code() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        when(zapPDSEventHandler.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(zapPDSEventHandler).cancelScan(contextName);

        when(clientApiFacade.getActiveScannerStatusForScan(scanId)).thenReturn(42);
        when(clientApiFacade.stopActiveScan(scanId)).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForActiveScanResults(scanId);
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(zapPDSEventHandler, times(2)).isScanCancelled();
        verify(clientApiFacade, never()).getActiveScannerStatusForScan(scanId);
        verify(clientApiFacade, times(1)).stopActiveScan(scanId);
    }

    @Test
    void wait_for_activeScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(clientApiFacade.getActiveScannerStatusForScan(scanId)).thenReturn(100);
        when(clientApiFacade.stopActiveScan(scanId)).thenReturn(null);

        /* execute */
        scannerToTest.waitForActiveScanResults(scanId);

        /* test */
        verify(clientApiFacade, atLeast(1)).getActiveScannerStatusForScan(scanId);
        verify(clientApiFacade, times(1)).stopActiveScan(scanId);
    }

    @Test
    void run_ajaxSpider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(scanContext.getMaxScanDurationInMilliSeconds()).thenReturn(1000L);
        when(scanContext.isActiveScanEnabled()).thenReturn(true);

        when(clientApiFacade.stopAjaxSpider()).thenReturn(null);
        when(clientApiFacade.getAjaxSpiderStatus()).thenReturn("stopped");

        /* execute */
        scannerToTest.runAjaxSpider();

        /* test */
        verify(scanContext, times(1)).getMaxScanDurationInMilliSeconds();
        verify(scanContext, times(1)).isActiveScanEnabled();
        verify(clientApiFacade, atLeast(1)).getAjaxSpiderStatus();
        verify(clientApiFacade, times(1)).stopAjaxSpider();
    }

    @Test
    void run_spider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(scanContext.getMaxScanDurationInMilliSeconds()).thenReturn(1000L);
        when(scanContext.isActiveScanEnabled()).thenReturn(true);
        ZapProductMessageHelper messageHelper = mock(ZapProductMessageHelper.class);
        when(scanContext.getZapProductMessageHelper()).thenReturn(messageHelper);
        doNothing().when(messageHelper).writeUserMessagesWithScannedURLs(any());

        when(clientApiFacade.stopSpiderScan(scanId)).thenReturn(null);
        when(clientApiFacade.getSpiderStatusForScan(scanId)).thenReturn(42);
        when(clientApiFacade.getAllSpiderUrls()).thenReturn(null);
        when(clientApiFacade.startSpiderScan(any(), any(), any(), any(), any())).thenReturn(scanId);

        /* execute */
        scannerToTest.runSpider();

        /* test */
        verify(scanContext, times(1)).getMaxScanDurationInMilliSeconds();
        verify(scanContext, times(1)).isActiveScanEnabled();
        verify(scanContext, times(1)).getZapProductMessageHelper();
        verify(messageHelper, times(1)).writeUserMessagesWithScannedURLs(any());
        verify(clientApiFacade, atLeast(1)).getSpiderStatusForScan(scanId);
        verify(clientApiFacade, times(1)).stopSpiderScan(scanId);
        verify(clientApiFacade, times(1)).getAllSpiderUrls();
        verify(clientApiFacade, times(1)).startSpiderScan(any(), any(), any(), any(), any());
    }

    @Test
    void run_activeScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        scannerToTest.remainingScanTime = 100L;

        when(clientApiFacade.getActiveScannerStatusForScan(scanId)).thenReturn(100);
        when(clientApiFacade.stopActiveScan(scanId)).thenReturn(null);
        when(clientApiFacade.startActiveScan(any(), any(), any(), any(), any(), any())).thenReturn(scanId);
        when(clientApiFacade.atLeastOneURLDetected()).thenReturn(true);

        /* execute */
        scannerToTest.runActiveScan();

        /* test */
        verify(clientApiFacade, atLeast(1)).getActiveScannerStatusForScan(scanId);
        verify(clientApiFacade, times(1)).stopActiveScan(scanId);
        verify(clientApiFacade, times(1)).startActiveScan(any(), any(), any(), any(), any(), any());
    }

    static Stream<Arguments> headerPartWithoutOnlyForUrlsTestNamedArguments() {
        /* @formatter:off */
        return Stream.of(
        		Arguments.of(
        				Named.of("3 Headers without onlyForUrls",
        				"{\"apiVersion\":\"1.0\",\"webScan\":{\"url\":\"https://productfailure.demo.example.org\",\"headers\":[{\"name\":\"Authorization\",\"value\":\"{{.HEADER_VALUE}}\"},{\"name\":\"x-file-size\",\"value\":\"123456\"},{\"name\":\"custom-header\",\"value\":\"test-value\"}]}}")),
        		Arguments.of(
        				Named.of("2 Headers without onlyForUrls",
        				"{\"apiVersion\":\"1.0\",\"webScan\":{\"url\":\"https://productfailure.demo.example.org\",\"headers\":[{\"name\":\"x-file-size\",\"value\":\"123456\"},{\"name\":\"custom-header\",\"value\":\"test-value\"}]}}")));
        /* @formatter:on */
    }

    static Stream<Arguments> headerPartWithOnlyForUrlsTestNamedArguments() {
        /* @formatter:off */
        return Stream.of(
        		Arguments.of(
        				Named.of("2 Headers 2nd with onlyForUrls",
        				"{\"apiVersion\":\"1.0\",\"webScan\":{\"url\":\"https://productfailure.demo.example.org\",\"headers\":[{\"name\":\"Authorization\",\"value\":\"{{.HEADER_VALUE}}\"},{\"name\":\"x-file-size\",\"value\":\"123456\",\"onlyForUrls\":[\"https://productfailure.demo.example.org/admin\",\"https://productfailure.demo.example.org/upload/<*>\",\"https://productfailure.demo.example.org/<*>/special/\"]}]}}")),
        		Arguments.of(
        				Named.of("3 Headers 2nd and 3rd with onlyForUrls",
        				"{\"apiVersion\":\"1.0\",\"webScan\":{\"url\":\"https://productfailure.demo.example.org\",\"headers\":[{\"name\":\"Authorization\",\"value\":\"{{.HEADER_VALUE}}\"},{\"name\":\"x-file-size\",\"value\":\"123456\",\"onlyForUrls\":[\"https://productfailure.demo.example.org/admin\",\"https://productfailure.demo.example.org/upload/<*>\",\"https://productfailure.demo.example.org/<*>/special/\"]},{\"name\":\"test-name\",\"value\":\"test-value\",\"onlyForUrls\":[\"https://productfailure.demo.example.org/profile\",\"https://productfailure.demo.example.org/upload\"]}]}}")));
        /* @formatter:on */
    }

}

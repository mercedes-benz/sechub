// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.HTTPHeaderConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.commons.model.login.BasicLoginConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ProxyInformation;
import com.mercedesbenz.sechub.zapwrapper.config.ZAPAcceptedBrowserId;
import com.mercedesbenz.sechub.zapwrapper.config.ZapScanContext;
import com.mercedesbenz.sechub.zapwrapper.config.auth.AuthenticationType;
import com.mercedesbenz.sechub.zapwrapper.config.auth.SessionManagementType;
import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.RuleReference;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;
import com.mercedesbenz.sechub.zapwrapper.helper.IncludeExcludeToZapURLHelper;
import com.mercedesbenz.sechub.zapwrapper.helper.ScanDurationHelper;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapPDSEventHandler;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapProductMessageHelper;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiSupport;
import com.mercedesbenz.sechub.zapwrapper.scan.ZapScanner.UserInformation;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ZapScriptLogin;
import com.mercedesbenz.sechub.zapwrapper.util.SystemUtil;
import com.mercedesbenz.sechub.zapwrapper.util.UrlUtil;

class ZapScannerTest {

    private ZapScanner scannerToTest;

    private static final ClientApiSupport CLIENT_API_SUPPORT = mock();
    private static final ZapScanContext SCAN_CONTEXT = mock();
    private static final ZapPDSEventHandler ZAP_PDS_EVENT_HANDLER = mock();
    private static final SystemUtil SYSTEM_UTIL = mock();
    private static final ZapScriptLogin SCRIPT_LOGIN = mock();
    private static final ZapProductMessageHelper MESSAGE_HELPER = mock();

    private static final ApiResponse RESPONSE = mock();

    private static final String BROWSER_ID = ZAPAcceptedBrowserId.FIREFOX_HEADLESS.getBrowserId();
    private static final String CONTEXT_NAME = "context-name";

    @BeforeEach
    void beforeEach() {
        // Reset mocks
        /* @formatter:off */
        Mockito.reset(CLIENT_API_SUPPORT,
                      SCAN_CONTEXT,
                      ZAP_PDS_EVENT_HANDLER,
                      SYSTEM_UTIL,
                      SCRIPT_LOGIN,
                      MESSAGE_HELPER);
        /* @formatter:on */

        // create scanner to test
        /* @formatter:off */
        scannerToTest = new ZapScanner(CLIENT_API_SUPPORT,
                                       SCAN_CONTEXT,
                                       new ScanDurationHelper(),
                                       new UrlUtil(),
                                       SYSTEM_UTIL, SCRIPT_LOGIN);
        /* @formatter:on */

        // set global behavior
        when(SCAN_CONTEXT.getContextName()).thenReturn(CONTEXT_NAME);
        when(SCAN_CONTEXT.getZapProductMessageHelper()).thenReturn(MESSAGE_HELPER);
        when(SCAN_CONTEXT.getZapPDSEventHandler()).thenReturn(ZAP_PDS_EVENT_HANDLER);
        when(SCAN_CONTEXT.getAjaxSpiderBrowserId()).thenReturn(BROWSER_ID);

        when(SCRIPT_LOGIN.login(SCAN_CONTEXT, CLIENT_API_SUPPORT)).thenReturn("authSessionId");
        doNothing().when(SCRIPT_LOGIN).cleanUpScriptLoginData(anyString(), eq(CLIENT_API_SUPPORT));

        doNothing().when(MESSAGE_HELPER).writeProductError(any());
        doNothing().when(MESSAGE_HELPER).writeProductMessages(any());
        doNothing().when(MESSAGE_HELPER).writeSingleProductMessage(any());

        doNothing().when(SYSTEM_UTIL).waitForMilliseconds(anyInt());
        when(SYSTEM_UTIL.getCurrentTimeInMilliseconds()).thenCallRealMethod();

        doNothing().when(SCRIPT_LOGIN).cleanUpScriptLoginData(any(), eq(CLIENT_API_SUPPORT));
    }

    @Test
    void setup_standard_configuration_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(CLIENT_API_SUPPORT.createNewSession(SCAN_CONTEXT.getContextName(), "true")).thenReturn(null);
        when(CLIENT_API_SUPPORT.configureMaximumAlertsForEachRule("0")).thenReturn(null);
        when(CLIENT_API_SUPPORT.enableAllPassiveScannerRules()).thenReturn(null);
        when(CLIENT_API_SUPPORT.enableAllActiveScannerRulesForPolicy(null)).thenReturn(null);
        when(CLIENT_API_SUPPORT.setAjaxSpiderBrowserId(BROWSER_ID))
                .thenReturn(null);

        /* execute */
        scannerToTest.setupStandardConfiguration();

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).createNewSession(SCAN_CONTEXT.getContextName(), "true");
        verify(CLIENT_API_SUPPORT, times(1)).configureMaximumAlertsForEachRule("0");
        verify(CLIENT_API_SUPPORT, times(1)).enableAllPassiveScannerRules();
        verify(CLIENT_API_SUPPORT, times(1)).enableAllActiveScannerRulesForPolicy(null);
        verify(CLIENT_API_SUPPORT, times(1)).setAjaxSpiderBrowserId(BROWSER_ID);
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
        verify(CLIENT_API_SUPPORT, never()).disablePassiveScannerRule(any());
        verify(CLIENT_API_SUPPORT, never()).disableActiveScannerRuleForPolicy(any(), any());
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

        String json = TestFileReader.readTextFromFile("src/test/resources/zap-available-rules/zap-full-ruleset.json");
        ZapFullRuleset ruleSet = new ZapFullRuleset().fromJSON(json);

        when(CLIENT_API_SUPPORT.disablePassiveScannerRule(any())).thenReturn(null);
        when(CLIENT_API_SUPPORT.disableActiveScannerRuleForPolicy(any(), any())).thenReturn(null);

        /* execute */
        scannerToTest.deactivateRules(ruleSet, deactivatedReferences);

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).disablePassiveScannerRule(any());
        verify(CLIENT_API_SUPPORT, times(2)).disableActiveScannerRuleForPolicy(any(), any());
    }

    @Test
    void setup_addtional_proxy_information_with_proxy_information_null_results_in_proxy_disabled()
            throws ClientApiException {
        /* prepare */
        when(CLIENT_API_SUPPORT.setHttpProxyEnabled("false")).thenReturn(null);

        /* execute */
        scannerToTest.setupAdditonalProxyConfiguration(null);

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).setHttpProxyEnabled("false");
    }

    @Test
    void setup_addtional_proxy_information_results_in_proxy_enabled() throws ClientApiException {
        /* prepare */
        String host = "127.0.0.1";
        int port = 8000;
        var portAsString = String.valueOf(port);
        ProxyInformation proxyInformation = new ProxyInformation(host, port);

        when(CLIENT_API_SUPPORT.configureHttpProxy(host, portAsString, null, null, null)).thenReturn(null);
        when(CLIENT_API_SUPPORT.setHttpProxyEnabled("true")).thenReturn(null);
        when(CLIENT_API_SUPPORT.setHttpProxyAuthEnabled("false")).thenReturn(null);

        /* execute */
        scannerToTest.setupAdditonalProxyConfiguration(proxyInformation);

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).configureHttpProxy(host, portAsString, null, null, null);
        verify(CLIENT_API_SUPPORT, times(1)).setHttpProxyEnabled("true");
        verify(CLIENT_API_SUPPORT, times(1)).setHttpProxyAuthEnabled("false");
    }

    @Test
    void create_context_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String expectedContextId = "random-id";
        when(CLIENT_API_SUPPORT.createNewContext(CONTEXT_NAME)).thenReturn(expectedContextId);

        /* execute */
        String contextId = scannerToTest.createContext();

        /* test */
        assertEquals(expectedContextId, contextId);
        verify(SCAN_CONTEXT, times(2)).getContextName();
        verify(CLIENT_API_SUPPORT, times(1)).createNewContext(CONTEXT_NAME);
    }

    @Test
    void add_replacer_rules_for_headers_with_no_headers_results_add_replacer_rule_is_never_called() throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubwebScanConfig = new SecHubWebScanConfiguration();
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubwebScanConfig);

        when(CLIENT_API_SUPPORT.addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        verify(CLIENT_API_SUPPORT, never()).addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("headerPartWithoutOnlyForUrlsTestNamedArguments")
    void add_replacer_rules_for_headers_with_no_onlyForUrls_results_add_replacer_rule_is_called_once_for_each_header(String sechubScanConfigJSON)
            throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        int times = sechubWebScanConfig.getHeaders().get().size();
        verify(CLIENT_API_SUPPORT, times(times)).addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("headerPartWithOnlyForUrlsTestNamedArguments")
    void add_replacer_rules_for_headers_with_onlyForUrls_results_add_replacer_rule_is_called_once_for_each_onylForUrl(String sechubScanConfigJSON)
            throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(RESPONSE);

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
        verify(CLIENT_API_SUPPORT, times(times)).addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void add_replacer_rules_for_headers_with_data_section_results_add_replacer_rule_is_called_once_for_each_header() throws ClientApiException {
        /* prepare */
        String sechubConfigWithfilesystemPartHasMoreThanOneFile = """
                {
                  "apiVersion" : "1.0",
                  "data" : {
                    "sources" : [ {
                      "name" : "header-file-reference",
                      "fileSystem" : {
                        "files" : [ "header-token.txt", "second-header-token.txt" ]
                      }
                    },
                     {
                      "name" : "another-header-file-reference",
                      "fileSystem" : {
                        "files" : [ "token.txt", "second-header-token.txt" ]
                      }
                    }]
                  },
                  "webScan" : {
                    "url" : "https://localhost:8443",
                    "headers" : [{
                      "name" : "Key",
                      "use" : [ "header-file-reference" ]
                    },
                    {
                      "name" : "Other",
                      "use" : [ "another-header-file-reference" ]
                    }]
                  }
                }
                """;
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubConfigWithfilesystemPartHasMoreThanOneFile).getWebScan()
                .get();
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        Map<String, File> headerFiles = new HashMap<>();
        headerFiles.put("Key", new File("src/test/resources/header-value-files/header-token.txt"));
        headerFiles.put("Other", new File("src/test/resources/header-value-files/token.txt"));
        when(SCAN_CONTEXT.getHeaderValueFiles()).thenReturn(headerFiles);

        when(CLIENT_API_SUPPORT.addReplacerRule(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).addReplacerRule("Key", "true", "REQ_HEADER", "false", "Key", "header-token", null, null);
        verify(CLIENT_API_SUPPORT, times(1)).addReplacerRule("Other", "true", "REQ_HEADER", "false", "Other", "token", null, null);
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-include-exclude.json" })
    void set_includes_and_excludes_api_support_is_called_once_for_each_include_and_once_for_exclude(String sechubConfigFile)
            throws ClientApiException, MalformedURLException {
        /* prepare */
        String json = TestFileReader.readTextFromFile(sechubConfigFile);

        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();
        IncludeExcludeToZapURLHelper helper = new IncludeExcludeToZapURLHelper();

        URL targetUrl = sechubWebScanConfig.getUrl().toURL();
        List<String> includesList = sechubWebScanConfig.getIncludes().get();
        Set<String> includes = new HashSet<>(helper.createListOfUrls(targetUrl, includesList));
        when(SCAN_CONTEXT.getZapURLsIncludeSet()).thenReturn(includes);

        List<String> excludesList = sechubWebScanConfig.getExcludes().get();
        Set<String> excludes = new HashSet<>(helper.createListOfUrls(targetUrl, excludesList));
        when(SCAN_CONTEXT.getZapURLsExcludeSet()).thenReturn(excludes);

        when(CLIENT_API_SUPPORT.addIncludeUrlPatternToContext(any(), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.accessUrlViaZap(any(), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.addExcludeUrlPatternToContext(any(), any())).thenReturn(RESPONSE);

        /* @formatter:off */
        int includesWithoutWildcards = (int) includes.stream()
                                                     .filter(s -> !s.contains(".*"))
                                                     .count();
        /* @formatter:on */

        /* execute */
        scannerToTest.addIncludedAndExcludedUrlsToContext();

        /* test */
        verify(CLIENT_API_SUPPORT, times(includes.size())).addIncludeUrlPatternToContext(any(), any());
        // make sure this method is only called for includes without wildcards
        verify(CLIENT_API_SUPPORT, times(includesWithoutWildcards)).accessUrlViaZap(any(), any());
        verify(CLIENT_API_SUPPORT, times(excludes.size())).addExcludeUrlPatternToContext(any(), any());
    }

    @Test
    void import_openapi_file_but_api_file_is_null_api_support_is_never_called() throws ClientApiException {
        /* prepare */
        String contextId = "context-id";

        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(new SecHubWebScanConfiguration());
        when(CLIENT_API_SUPPORT.importOpenApiFile(any(), any(), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.importOpenApiDefintionFromUrl(any(), any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(CLIENT_API_SUPPORT, never()).importOpenApiFile(any(), any(), any());
        verify(CLIENT_API_SUPPORT, never()).importOpenApiDefintionFromUrl(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json" })
    void import_openapi_file_api_support_is_called_once(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        String contextId = "context-id";
        String json = TestFileReader.readTextFromFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();

        List<File> apiFiles = new ArrayList<>();
        apiFiles.add(new File("openapi3.json"));

        when(SCAN_CONTEXT.getApiDefinitionFiles()).thenReturn(apiFiles);
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.importOpenApiFile(any(), any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).importOpenApiFile(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-from-url.json" })
    void import_openapi_defintion_from_url_api_support_is_called_once(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        String contextId = "context-id";
        String json = TestFileReader.readTextFromFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.importOpenApiFile(any(), any(), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.importOpenApiDefintionFromUrl(any(), any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(CLIENT_API_SUPPORT, never()).importOpenApiFile(any(), any(), any());
        verify(CLIENT_API_SUPPORT, times(1)).importOpenApiDefintionFromUrl(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-from-file-and-url.json" })
    void import_openapi_from_file_and_from_url_api_support_is_called_once(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        String contextId = "context-id";
        String json = TestFileReader.readTextFromFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();

        List<File> apiFiles = new ArrayList<>();
        apiFiles.add(new File("openapi3.json"));

        when(SCAN_CONTEXT.getApiDefinitionFiles()).thenReturn(apiFiles);
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.importOpenApiFile(any(), any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).importOpenApiFile(any(), any(), any());
        verify(CLIENT_API_SUPPORT, times(1)).importOpenApiDefintionFromUrl(any(), any(), any());
    }

    @Test
    void import_client_certificate_file_but_client_certificate_file_is_null_api_support_is_never_called() throws ClientApiException {
        /* prepare */
        when(CLIENT_API_SUPPORT.importPkcs12ClientCertificate(any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.importClientCertificate();

        /* test */
        verify(CLIENT_API_SUPPORT, never()).importOpenApiFile(any(), any(), any());
    }

    @Test
    void try_import_without_client_certificate_file_api_support_is_never_called() throws ClientApiException {
        /* prepare */
        String jsonWithClientCertConfig = """
                {
                  "apiVersion" : "1.0",
                  "project" : "example_project",
                  "webScan" : {
                    "url" : "https://my-app.com"
                  }
                }
                """;
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(jsonWithClientCertConfig).getWebScan().get();

        File clientCertificateFile = new File("backend-cert.p12");

        when(SCAN_CONTEXT.getClientCertificateFile()).thenReturn(clientCertificateFile);
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.importPkcs12ClientCertificate(any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.importClientCertificate();

        /* test */
        verify(CLIENT_API_SUPPORT, never()).importPkcs12ClientCertificate(any(), any());
    }

    @Test
    void import_client_certificate_file_api_support_is_called_once() throws ClientApiException {
        /* prepare */
        String jsonWithCertPassword = """
                {
                  "apiVersion" : "1.0",
                  "project" : "example_project",
                  "webScan" : {
                    "url" : "https://my-app.com",
                    "clientCertificate" : {
                      "password" : "secret-password",
                      "use" : [ "client-certificate-file-reference" ]
                    }
                  }
                }
                """;

        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(jsonWithCertPassword).getWebScan().get();

        File clientCertificateFile = mock(File.class);

        when(SCAN_CONTEXT.getClientCertificateFile()).thenReturn(clientCertificateFile);
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientCertificateFile.exists()).thenReturn(true);

        when(CLIENT_API_SUPPORT.importPkcs12ClientCertificate(any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.importClientCertificate();

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).importPkcs12ClientCertificate(any(), any());
    }

    @Test
    void import_client_certificate_file_but_without_password_api_support_is_called_once() throws ClientApiException {
        /* prepare */
        String jsonWithoutCertPassword = """
                {
                  "apiVersion" : "1.0",
                  "project" : "example_project",
                  "webScan" : {
                    "url" : "https://my-app.com",
                    "clientCertificate" : {
                      "use" : [ "client-certificate-file-reference" ]
                    }
                  }
                }
                """;

        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(jsonWithoutCertPassword).getWebScan().get();

        File clientCertificateFile = mock(File.class);

        when(SCAN_CONTEXT.getClientCertificateFile()).thenReturn(clientCertificateFile);
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);
        when(clientCertificateFile.exists()).thenReturn(true);

        when(CLIENT_API_SUPPORT.importPkcs12ClientCertificate(any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.importClientCertificate();

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).importPkcs12ClientCertificate(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json",
            "src/test/resources/sechub-config-examples/form-based-auth.json" })
    void configure_login_inside_zap_using_no_auth_and_unsupported_auth_return_null(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        String contextId = "context-id";
        String json = TestFileReader.readTextFromFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();

        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        /* execute */
        UserInformation userInformation = scannerToTest.setupLoginInsideZapContext(contextId);

        /* test */
        assertEquals(null, userInformation);
    }

    @Test
    void configure_login_inside_zap_using_basic_auth_results_in_expected_calls() throws ClientApiException, MalformedURLException {
        /* prepare */
        String contextId = "context-id";
        String userId = "user-id";
        URL targetUrl = URI.create("https://127.0.0.1:8000").toURL();
        String json = TestFileReader.readTextFromFile("src/test/resources/sechub-config-examples/basic-auth.json");
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();
        BasicLoginConfiguration basicLoginConfiguration = sechubWebScanConfig.getLogin().get().getBasic().get();
        String userName = new String(basicLoginConfiguration.getUser());

        String zapAuthenticationMethod = AuthenticationType.HTTP_BASIC_AUTHENTICATION.getZapAuthenticationMethod();
        String zapSessionManagementMethod = SessionManagementType.HTTP_AUTH_SESSION_MANAGEMENT.getZapSessionManagementMethod();

        when(SCAN_CONTEXT.getTargetUrl()).thenReturn(targetUrl);
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.configureAuthenticationMethod(eq(contextId), eq(zapAuthenticationMethod), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setSessionManagementMethod(eq(contextId), eq(zapSessionManagementMethod), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.createNewUser(contextId, userName)).thenReturn(userId);
        when(CLIENT_API_SUPPORT.configureAuthenticationCredentials(eq(contextId), eq(userId), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setForcedUser(contextId, userId)).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setForcedUserModeEnabled(true)).thenReturn(RESPONSE);

        /* execute */
        UserInformation userInformation = scannerToTest.setupLoginInsideZapContext(contextId);

        /* test */
        assertEquals(userName, userInformation.userName());
        assertEquals(userId, userInformation.zapuserId());

        verify(SCAN_CONTEXT, times(2)).getTargetUrl();

        verify(CLIENT_API_SUPPORT, times(1)).configureAuthenticationMethod(eq(contextId), eq(zapAuthenticationMethod), any());
        verify(CLIENT_API_SUPPORT, times(1)).setSessionManagementMethod(eq(contextId), eq(zapSessionManagementMethod), any());
        verify(CLIENT_API_SUPPORT, times(1)).createNewUser(contextId, userName);
        verify(CLIENT_API_SUPPORT, times(1)).configureAuthenticationCredentials(eq(contextId), eq(userId), any());
        verify(CLIENT_API_SUPPORT, times(1)).setForcedUser(contextId, userId);
        verify(CLIENT_API_SUPPORT, times(1)).setForcedUserModeEnabled(true);
    }

    @Test
    void configure_login_inside_zap_using_script_auth_without_script_file_results_in_script_login_not_being_called() throws Exception {
        /* prepare */
        String contextId = "context-id";
        String userId = "user-id";
        String userName = "user";
        URL targetUrl = URI.create("https://127.0.0.1:8000").toURL();
        SecHubWebScanConfiguration sechubWebScanConfig = new SecHubWebScanConfiguration();
        sechubWebScanConfig.setUrl(targetUrl.toURI());
        WebLoginConfiguration login = new WebLoginConfiguration();
        sechubWebScanConfig.setLogin(Optional.of(login));

        String zapAuthenticationMethod = AuthenticationType.MANUAL_AUTHENTICATION.getZapAuthenticationMethod();
        String zapSessionManagementMethod = SessionManagementType.COOKIE_BASED_SESSION_MANAGEMENT.getZapSessionManagementMethod();

        when(SCRIPT_LOGIN.login(SCAN_CONTEXT, CLIENT_API_SUPPORT)).thenReturn("zap-auth-session");

        when(SCAN_CONTEXT.getTargetUrl()).thenReturn(targetUrl);
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);
        when(SCAN_CONTEXT.getGroovyScriptLoginFile()).thenReturn(null);

        when(CLIENT_API_SUPPORT.configureAuthenticationMethod(eq(contextId), eq(zapAuthenticationMethod), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setSessionManagementMethod(eq(contextId), eq(zapSessionManagementMethod), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.createNewUser(contextId, userName)).thenReturn(userId);
        when(CLIENT_API_SUPPORT.configureAuthenticationCredentials(eq(contextId), eq(userId), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setForcedUser(contextId, userId)).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setForcedUserModeEnabled(true)).thenReturn(RESPONSE);

        /* execute */
        UserInformation userInformation = scannerToTest.setupLoginInsideZapContext(contextId);

        /* test */
        assertNull(userInformation);
        verify(SCRIPT_LOGIN, never()).login(SCAN_CONTEXT, CLIENT_API_SUPPORT);
        verify(SCAN_CONTEXT, times(1)).getGroovyScriptLoginFile();
    }

    @Test
    void configure_login_inside_zap_using_script_auth_with_existing_script_file_results_in_script_login_being_called() throws Exception {
        /* prepare */
        String contextId = "context-id";
        String userId = "user-id";
        String userName = "DUMMY";
        URL targetUrl = URI.create("https://127.0.0.1:8000").toURL();
        SecHubWebScanConfiguration sechubWebScanConfig = new SecHubWebScanConfiguration();
        sechubWebScanConfig.setUrl(targetUrl.toURI());
        WebLoginConfiguration login = new WebLoginConfiguration();
        sechubWebScanConfig.setLogin(Optional.of(login));

        String zapAuthenticationMethod = AuthenticationType.MANUAL_AUTHENTICATION.getZapAuthenticationMethod();
        String zapSessionManagementMethod = SessionManagementType.COOKIE_BASED_SESSION_MANAGEMENT.getZapSessionManagementMethod();
        File scriptFile = new File("src/test/resources/login-script-examples/test-script.groovy");

        when(SCRIPT_LOGIN.login(SCAN_CONTEXT, CLIENT_API_SUPPORT)).thenReturn("zap-auth-session");

        when(SCAN_CONTEXT.getTargetUrl()).thenReturn(targetUrl);
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);
        when(SCAN_CONTEXT.getGroovyScriptLoginFile()).thenReturn(scriptFile);

        when(CLIENT_API_SUPPORT.configureAuthenticationMethod(eq(contextId), eq(zapAuthenticationMethod), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setSessionManagementMethod(eq(contextId), eq(zapSessionManagementMethod), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.createNewUser(contextId, userName)).thenReturn(userId);
        when(CLIENT_API_SUPPORT.configureAuthenticationCredentials(eq(contextId), eq(userId), any())).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setForcedUser(contextId, userId)).thenReturn(RESPONSE);
        when(CLIENT_API_SUPPORT.setForcedUserModeEnabled(true)).thenReturn(RESPONSE);

        /* execute */
        UserInformation userInformation = scannerToTest.setupLoginInsideZapContext(contextId);

        /* test */
        assertEquals(userName, userInformation.userName());
        assertEquals(userId, userInformation.zapuserId());

        verify(SCRIPT_LOGIN, times(1)).login(SCAN_CONTEXT, CLIENT_API_SUPPORT);
        verify(SCAN_CONTEXT, times(2)).getGroovyScriptLoginFile();

        verify(CLIENT_API_SUPPORT, times(1)).configureAuthenticationMethod(eq(contextId), eq(zapAuthenticationMethod), any());
        verify(CLIENT_API_SUPPORT, times(1)).setSessionManagementMethod(eq(contextId), eq(zapSessionManagementMethod), any());
        verify(CLIENT_API_SUPPORT, times(1)).createNewUser(contextId, userName);
        verify(CLIENT_API_SUPPORT, times(1)).configureAuthenticationCredentials(eq(contextId), eq(userId), any());
        verify(CLIENT_API_SUPPORT, times(1)).setForcedUser(contextId, userId);
        verify(CLIENT_API_SUPPORT, times(1)).setForcedUserModeEnabled(true);
    }

    @Test
    void generate_report_calls_api_support_once() throws ClientApiException {
        /* prepare */
        when(SCAN_CONTEXT.getReportFile())
                .thenReturn(Paths.get("src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json"));

        when(CLIENT_API_SUPPORT.generateReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.generateZapReport();

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).generateReport(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void cleanup_after_scan() throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubwebScanConfig = new SecHubWebScanConfiguration();
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubwebScanConfig);
        when(CLIENT_API_SUPPORT.removeReplacerRule(any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.cleanUp();

        /* test */
        verify(CLIENT_API_SUPPORT, times(1)).removeReplacerRule(ZapScanner.X_SECHUB_DAST_HEADER_NAME);
        verify(SCRIPT_LOGIN, times(1)).cleanUpScriptLoginData(SCAN_CONTEXT.getTargetUrlAsString(), CLIENT_API_SUPPORT);
    }

    @ParameterizedTest
    @MethodSource("headerPartWithoutOnlyForUrlsTestNamedArguments")
    void cleanup_after_scan_without_onylForUrls_headers_set_cleans_up_all_replacer_rules(String sechubScanConfigJSON) throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.removeReplacerRule(any())).thenReturn(RESPONSE);

        /* execute */
        scannerToTest.cleanUp();

        /* test */
        int times = sechubWebScanConfig.getHeaders().get().size();
        verify(CLIENT_API_SUPPORT, times(1)).removeReplacerRule(ZapScanner.X_SECHUB_DAST_HEADER_NAME);
        verify(CLIENT_API_SUPPORT, times(times + 1)).removeReplacerRule(any());
        verify(SCRIPT_LOGIN, times(1)).cleanUpScriptLoginData(SCAN_CONTEXT.getTargetUrlAsString(), CLIENT_API_SUPPORT);
    }

    @ParameterizedTest
    @MethodSource("headerPartWithOnlyForUrlsTestNamedArguments")
    void cleanup_after_scan_with_onylForUrls_headers_set_cleans_up_all_replacer_rules(String sechubScanConfigJSON) throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(SCAN_CONTEXT.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(CLIENT_API_SUPPORT.removeReplacerRule(any())).thenReturn(RESPONSE);

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
        verify(CLIENT_API_SUPPORT, times(1)).removeReplacerRule(ZapScanner.X_SECHUB_DAST_HEADER_NAME);
        verify(CLIENT_API_SUPPORT, times(times + 1)).removeReplacerRule(any());
        verify(SCRIPT_LOGIN, times(1)).cleanUpScriptLoginData(SCAN_CONTEXT.getTargetUrlAsString(), CLIENT_API_SUPPORT);
    }

    @Test
    void wait_for_ajaxSpider_scan_is_cancelled_results_in_exception_with_dedicated_exit_code()
            throws ClientApiException {
        /* prepare */
        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(ZAP_PDS_EVENT_HANDLER).cancelScan(CONTEXT_NAME);

        long scanDuration = 20000L;
        when(SCAN_CONTEXT.isActiveScanEnabled()).thenReturn(true);

        when(CLIENT_API_SUPPORT.stopAjaxSpider()).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForAjaxSpiderResults(scanDuration);
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(ZAP_PDS_EVENT_HANDLER, times(2)).isScanCancelled();
        verify(SCAN_CONTEXT, times(1)).isActiveScanEnabled();
        verify(CLIENT_API_SUPPORT, times(1)).stopAjaxSpider();
    }

    @Test
    void wait_for_ajaxSpider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(false);

        long scanDuration = 1000L;
        when(SCAN_CONTEXT.isActiveScanEnabled()).thenReturn(true);

        when(CLIENT_API_SUPPORT.stopAjaxSpider()).thenReturn(null);
        when(CLIENT_API_SUPPORT.getAjaxSpiderStatus()).thenReturn("stopped");

        /* execute */
        scannerToTest.waitForAjaxSpiderResults(scanDuration);

        /* test */
        verify(SCAN_CONTEXT, times(1)).isActiveScanEnabled();
        verify(CLIENT_API_SUPPORT, atLeast(1)).getAjaxSpiderStatus();
        verify(CLIENT_API_SUPPORT, times(1)).stopAjaxSpider();
    }

    @Test
    void wait_for_spider_scan_is_cancelled_results_in_exception_with_dedicated_exit_code() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(ZAP_PDS_EVENT_HANDLER).cancelScan(CONTEXT_NAME);

        long scanDuration = 20000L;
        when(SCAN_CONTEXT.isActiveScanEnabled()).thenReturn(true);

        when(CLIENT_API_SUPPORT.stopSpiderScan(scanId)).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForSpiderResults(scanId, scanDuration);
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(ZAP_PDS_EVENT_HANDLER, times(2)).isScanCancelled();
        verify(SCAN_CONTEXT, times(1)).isActiveScanEnabled();
        verify(CLIENT_API_SUPPORT, times(1)).stopSpiderScan(scanId);
    }

    @Test
    void wait_for_spider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(false);
        long scanDuration = 1000L;
        when(SCAN_CONTEXT.isActiveScanEnabled()).thenReturn(true);
        ZapProductMessageHelper messageHelper = mock(ZapProductMessageHelper.class);
        when(SCAN_CONTEXT.getZapProductMessageHelper()).thenReturn(messageHelper);

        when(CLIENT_API_SUPPORT.stopSpiderScan(scanId)).thenReturn(null);
        when(CLIENT_API_SUPPORT.getSpiderStatusForScan(scanId)).thenReturn(42);
        when(CLIENT_API_SUPPORT.logFullSpiderResults(scanId)).thenReturn(0L);

        /* execute */
        scannerToTest.waitForSpiderResults(scanId, scanDuration);

        /* test */
        verify(SCAN_CONTEXT, times(1)).isActiveScanEnabled();
        verify(SCAN_CONTEXT, times(1)).getZapProductMessageHelper();
        verify(CLIENT_API_SUPPORT, atLeast(1)).getSpiderStatusForScan(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).stopSpiderScan(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).logFullSpiderResults(scanId);
    }

    @Test
    void wait_for_passiveScan_scan_is_cancelled_results_in_exception_with_dedicated_exit_code()
            throws ClientApiException {
        /* prepare */
        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(ZAP_PDS_EVENT_HANDLER).cancelScan(CONTEXT_NAME);

        long scanDuration = 20000L;
        when(SCAN_CONTEXT.isActiveScanEnabled()).thenReturn(false);
        when(SCAN_CONTEXT.isAjaxSpiderEnabled()).thenReturn(false);

        when(CLIENT_API_SUPPORT.getNumberOfPassiveScannerRecordsToScan()).thenReturn(12);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.passiveScan(scanDuration);
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(ZAP_PDS_EVENT_HANDLER, times(2)).isScanCancelled();
        verify(SCAN_CONTEXT, times(1)).isActiveScanEnabled();
        verify(SCAN_CONTEXT, times(1)).isAjaxSpiderEnabled();
        verify(CLIENT_API_SUPPORT, atLeast(1)).getNumberOfPassiveScannerRecordsToScan();
    }

    @Test
    void wait_for_passiveScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(false);

        long scanDuration = 20000L;
        when(SCAN_CONTEXT.isActiveScanEnabled()).thenReturn(false);
        when(SCAN_CONTEXT.isAjaxSpiderEnabled()).thenReturn(false);

        when(CLIENT_API_SUPPORT.getNumberOfPassiveScannerRecordsToScan()).thenReturn(0);

        /* execute */
        scannerToTest.passiveScan(scanDuration);

        /* test */
        verify(SCAN_CONTEXT, times(1)).isActiveScanEnabled();
        verify(SCAN_CONTEXT, times(1)).isAjaxSpiderEnabled();
        verify(CLIENT_API_SUPPORT, times(1)).getNumberOfPassiveScannerRecordsToScan();
    }

    @Test
    void wait_for_activeScan_scan_is_cancelled_results_in_exception_with_dedicated_exit_code() throws ClientApiException {
        /* prepare */
        String scanId = "12345";
        long scanDuration = 20000L;

        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(ZAP_PDS_EVENT_HANDLER).cancelScan(CONTEXT_NAME);

        when(CLIENT_API_SUPPORT.getActiveScannerStatusForScan(scanId)).thenReturn(42);
        when(CLIENT_API_SUPPORT.stopActiveScan(scanId)).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForActiveScanResults(scanId, scanDuration);
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(ZAP_PDS_EVENT_HANDLER, times(2)).isScanCancelled();
        verify(CLIENT_API_SUPPORT, never()).getActiveScannerStatusForScan(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).stopActiveScan(scanId);
    }

    @Test
    void wait_for_activeScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String scanId = "12345";
        long scanDuration = 20000L;

        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(false);

        when(CLIENT_API_SUPPORT.getActiveScannerStatusForScan(scanId)).thenReturn(100);
        when(CLIENT_API_SUPPORT.stopActiveScan(scanId)).thenReturn(null);

        /* execute */
        scannerToTest.waitForActiveScanResults(scanId, scanDuration);

        /* test */
        verify(CLIENT_API_SUPPORT, atLeast(1)).getActiveScannerStatusForScan(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).stopActiveScan(scanId);
    }

    @Test
    void run_ajaxSpider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(false);

        long scanDuration = 1000L;
        when(SCAN_CONTEXT.isActiveScanEnabled()).thenReturn(true);

        when(CLIENT_API_SUPPORT.stopAjaxSpider()).thenReturn(null);
        when(CLIENT_API_SUPPORT.getAjaxSpiderStatus()).thenReturn("stopped");

        /* execute */
        scannerToTest.runAjaxSpider(scanDuration);

        /* test */
        verify(SCAN_CONTEXT, times(1)).isActiveScanEnabled();
        verify(CLIENT_API_SUPPORT, atLeast(1)).getAjaxSpiderStatus();
        verify(CLIENT_API_SUPPORT, times(1)).stopAjaxSpider();
    }

    @Test
    void run_spider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String scanId = "12345";
        long scanDuration = 1000L;

        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(false);

        when(SCAN_CONTEXT.isActiveScanEnabled()).thenReturn(true);
        ZapProductMessageHelper messageHelper = mock(ZapProductMessageHelper.class);
        when(SCAN_CONTEXT.getZapProductMessageHelper()).thenReturn(messageHelper);

        when(CLIENT_API_SUPPORT.stopSpiderScan(scanId)).thenReturn(null);
        when(CLIENT_API_SUPPORT.getSpiderStatusForScan(scanId)).thenReturn(42);
        when(CLIENT_API_SUPPORT.logFullSpiderResults(scanId)).thenReturn(0L);
        when(CLIENT_API_SUPPORT.startSpiderScan(any(), any(), any(), any(), any())).thenReturn(scanId);

        /* execute */
        scannerToTest.runSpider(scanDuration);

        /* test */
        verify(SCAN_CONTEXT, times(1)).isActiveScanEnabled();
        verify(SCAN_CONTEXT, times(1)).getZapProductMessageHelper();
        verify(CLIENT_API_SUPPORT, atLeast(1)).getSpiderStatusForScan(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).stopSpiderScan(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).logFullSpiderResults(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).startSpiderScan(any(), any(), any(), any(), any());
    }

    @Test
    void run_activeScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        String scanId = "12345";

        long scanDuration = 20000L;

        when(ZAP_PDS_EVENT_HANDLER.isScanCancelled()).thenReturn(false);

        when(CLIENT_API_SUPPORT.getActiveScannerStatusForScan(scanId)).thenReturn(100);
        when(CLIENT_API_SUPPORT.stopActiveScan(scanId)).thenReturn(null);
        when(CLIENT_API_SUPPORT.startActiveScan(any(), any(), any(), any(), any(), any())).thenReturn(scanId);
        when(CLIENT_API_SUPPORT.atLeastOneURLDetected()).thenReturn(true);

        /* execute */
        scannerToTest.runActiveScan(scanDuration);

        /* test */
        verify(CLIENT_API_SUPPORT, atLeast(1)).getActiveScannerStatusForScan(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).stopActiveScan(scanId);
        verify(CLIENT_API_SUPPORT, times(1)).startActiveScan(any(), any(), any(), any(), any(), any());
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

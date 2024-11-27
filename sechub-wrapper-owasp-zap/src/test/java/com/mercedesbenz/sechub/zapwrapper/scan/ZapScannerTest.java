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
import com.mercedesbenz.sechub.zapwrapper.config.auth.ZapAuthenticationType;
import com.mercedesbenz.sechub.zapwrapper.config.auth.ZapSessionManagementType;
import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.RuleReference;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;
import com.mercedesbenz.sechub.zapwrapper.helper.IncludeExcludeToZapURLHelper;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapPDSEventHandler;
import com.mercedesbenz.sechub.zapwrapper.helper.ZapProductMessageHelper;
import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiWrapper;
import com.mercedesbenz.sechub.zapwrapper.scan.ZapScanner.UserInformation;
import com.mercedesbenz.sechub.zapwrapper.scan.login.ZapScriptLogin;
import com.mercedesbenz.sechub.zapwrapper.util.SystemUtil;
import com.mercedesbenz.sechub.zapwrapper.util.UrlUtil;

class ZapScannerTest {

    private ZapScanner scannerToTest;

    private ClientApiWrapper clientApiWrapper;
    private ZapScanContext scanContext;
    private ZapPDSEventHandler zapPDSEventHandler;
    private SystemUtil systemUtil;
    private ZapScriptLogin scriptLogin;
    private ZapProductMessageHelper messageHelper;

    private ApiResponse apiResponse;

    private static final String BROWSER_ID = ZAPAcceptedBrowserId.FIREFOX_HEADLESS.getBrowserId();
    private static final String CONTEXT_NAME = "context-name";

    @BeforeEach
    void beforeEach() throws ClientApiException {
        clientApiWrapper = mock();
        scanContext = mock();
        zapPDSEventHandler = mock();
        systemUtil = mock();
        scriptLogin = mock();
        messageHelper = mock();

        apiResponse = mock();

        // create scanner to test
        /* @formatter:off */
        scannerToTest = new ZapScanner(clientApiWrapper,
                                       scanContext,
                                       new UrlUtil(),
                                       systemUtil,
                                       scriptLogin);
        /* @formatter:on */

        // set global behavior
        when(scanContext.getContextName()).thenReturn(CONTEXT_NAME);
        when(scanContext.getZapProductMessageHelper()).thenReturn(messageHelper);
        when(scanContext.getZapPDSEventHandler()).thenReturn(zapPDSEventHandler);
        when(scanContext.getAjaxSpiderBrowserId()).thenReturn(BROWSER_ID);

        when(scriptLogin.login(scanContext, clientApiWrapper)).thenReturn("authSessionId");
        doNothing().when(scriptLogin).cleanUpScriptLoginData(anyString(), eq(clientApiWrapper));

        doNothing().when(messageHelper).writeProductError(any());
        doNothing().when(messageHelper).writeProductMessages(any());
        doNothing().when(messageHelper).writeSingleProductMessage(any());

        doNothing().when(systemUtil).waitForMilliseconds(anyInt());
        when(systemUtil.getCurrentTimeInMilliseconds()).thenCallRealMethod();

        doNothing().when(scriptLogin).cleanUpScriptLoginData(any(), eq(clientApiWrapper));
    }

    @Test
    void setup_standard_configuration_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(clientApiWrapper.createNewSession(scanContext.getContextName(), true)).thenReturn(null);
        when(clientApiWrapper.setMaximumAlertsForEachRuleToUnlimited()).thenReturn(null);
        when(clientApiWrapper.enableAllPassiveScannerRules()).thenReturn(null);
        when(clientApiWrapper.enableAllActiveScannerRulesForDefaultPolicy()).thenReturn(null);
        when(clientApiWrapper.setAjaxSpiderBrowserId(BROWSER_ID))
                .thenReturn(null);

        /* execute */
        scannerToTest.setupStandardConfiguration();

        /* test */
        verify(clientApiWrapper).createNewSession(scanContext.getContextName(), true);
        verify(clientApiWrapper).setMaximumAlertsForEachRuleToUnlimited();
        verify(clientApiWrapper).enableAllPassiveScannerRules();
        verify(clientApiWrapper).enableAllActiveScannerRulesForDefaultPolicy();
        verify(clientApiWrapper).setAjaxSpiderBrowserId(BROWSER_ID);
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
        verify(clientApiWrapper, never()).disablePassiveScannerRule(any());
        verify(clientApiWrapper, never()).disableActiveScannerRuleForDefaultPolicy(any());
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

        when(clientApiWrapper.disablePassiveScannerRule(any())).thenReturn(null);
        when(clientApiWrapper.disableActiveScannerRuleForDefaultPolicy(any())).thenReturn(null);

        /* execute */
        scannerToTest.deactivateRules(ruleSet, deactivatedReferences);

        /* test */
        verify(clientApiWrapper).disablePassiveScannerRule(any());
        verify(clientApiWrapper, times(2)).disableActiveScannerRuleForDefaultPolicy(any());
    }

    @Test
    void setup_addtional_proxy_information_with_proxy_information_null_results_in_proxy_disabled()
            throws ClientApiException {
        /* prepare */
        when(clientApiWrapper.setHttpProxyEnabled(false)).thenReturn(null);

        /* execute */
        scannerToTest.setupAdditonalProxyConfiguration(null);

        /* test */
        verify(clientApiWrapper).setHttpProxyEnabled(false);
    }

    @Test
    void setup_addtional_proxy_information_results_in_proxy_enabled() throws ClientApiException {
        /* prepare */
        String host = "127.0.0.1";
        int port = 8000;
        ProxyInformation proxyInformation = ProxyInformation.builder().setHost(host).setPort(port).build();

        when(clientApiWrapper.configureHttpProxy(proxyInformation)).thenReturn(null);
        when(clientApiWrapper.setHttpProxyEnabled(true)).thenReturn(null);
        when(clientApiWrapper.setHttpProxyAuthEnabled(false)).thenReturn(null);

        /* execute */
        scannerToTest.setupAdditonalProxyConfiguration(proxyInformation);

        /* test */
        verify(clientApiWrapper).configureHttpProxy(proxyInformation);
        verify(clientApiWrapper).setHttpProxyEnabled(true);
        verify(clientApiWrapper).setHttpProxyAuthEnabled(false);
    }

    @Test
    void create_context_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        int expectedContextId = 12345;
        when(clientApiWrapper.createNewContext(CONTEXT_NAME)).thenReturn(expectedContextId);

        /* execute */
        int contextId = scannerToTest.createContext();

        /* test */
        assertEquals(expectedContextId, contextId);
        verify(scanContext, times(2)).getContextName();
        verify(clientApiWrapper).createNewContext(CONTEXT_NAME);
    }

    @Test
    void add_replacer_rules_for_headers_with_no_headers_results_add_replacer_rule_is_never_called() throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubwebScanConfig = new SecHubWebScanConfiguration();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubwebScanConfig);

        when(clientApiWrapper.addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        verify(clientApiWrapper, never()).addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("headerPartWithoutOnlyForUrlsTestNamedArguments")
    void add_replacer_rules_for_headers_with_no_onlyForUrls_results_add_replacer_rule_is_called_once_for_each_header(String sechubScanConfigJSON)
            throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        int times = sechubWebScanConfig.getHeaders().get().size();
        verify(clientApiWrapper, times(times)).addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("headerPartWithOnlyForUrlsTestNamedArguments")
    void add_replacer_rules_for_headers_with_onlyForUrls_results_add_replacer_rule_is_called_once_for_each_onylForUrl(String sechubScanConfigJSON)
            throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any())).thenReturn(apiResponse);

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
        verify(clientApiWrapper, times(times)).addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any());
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
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        Map<String, File> headerFiles = new HashMap<>();
        headerFiles.put("Key", new File("src/test/resources/header-value-files/header-token.txt"));
        headerFiles.put("Other", new File("src/test/resources/header-value-files/token.txt"));
        when(scanContext.getHeaderValueFiles()).thenReturn(headerFiles);

        when(clientApiWrapper.addReplacerRule(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.addReplacerRulesForHeaders();

        /* test */
        verify(clientApiWrapper).addReplacerRule("Key", true, "REQ_HEADER", false, "Key", "header-token", null, null);
        verify(clientApiWrapper).addReplacerRule("Other", true, "REQ_HEADER", false, "Other", "token", null, null);
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
        when(scanContext.getZapURLsIncludeSet()).thenReturn(includes);

        List<String> excludesList = sechubWebScanConfig.getExcludes().get();
        Set<String> excludes = new HashSet<>(helper.createListOfUrls(targetUrl, excludesList));
        when(scanContext.getZapURLsExcludeSet()).thenReturn(excludes);

        when(clientApiWrapper.addIncludeUrlPatternToContext(any(), any())).thenReturn(apiResponse);
        when(clientApiWrapper.accessUrlViaZap(any(), anyBoolean())).thenReturn(apiResponse);
        when(clientApiWrapper.addExcludeUrlPatternToContext(any(), any())).thenReturn(apiResponse);

        /* @formatter:off */
        int includesWithoutWildcards = (int) includes.stream()
                                                     .filter(s -> !s.contains(".*"))
                                                     .count();
        /* @formatter:on */

        /* execute */
        scannerToTest.addIncludedAndExcludedUrlsToContext();

        /* test */
        verify(clientApiWrapper, times(includes.size())).addIncludeUrlPatternToContext(any(), any());
        // make sure this method is only called for includes without wildcards
        verify(clientApiWrapper, times(includesWithoutWildcards)).accessUrlViaZap(any(), anyBoolean());
        verify(clientApiWrapper, times(excludes.size())).addExcludeUrlPatternToContext(any(), any());
    }

    @Test
    void import_openapi_file_but_api_file_is_null_api_support_is_never_called() throws ClientApiException {
        /* prepare */
        int contextId = 12345;
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(new SecHubWebScanConfiguration());
        when(clientApiWrapper.importOpenApiFile(any(), any(), anyInt())).thenReturn(apiResponse);
        when(clientApiWrapper.importOpenApiDefintionFromUrl(any(), any(), anyInt())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(clientApiWrapper, never()).importOpenApiFile(any(), any(), anyInt());
        verify(clientApiWrapper, never()).importOpenApiDefintionFromUrl(any(), any(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json" })
    void import_openapi_file_api_support_is_called_once(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        int contextId = 12345;
        String json = TestFileReader.readTextFromFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();

        List<File> apiFiles = new ArrayList<>();
        apiFiles.add(new File("openapi3.json"));

        when(scanContext.getApiDefinitionFiles()).thenReturn(apiFiles);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.importOpenApiFile(any(), any(), anyInt())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(clientApiWrapper).importOpenApiFile(any(), any(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-from-url.json" })
    void import_openapi_defintion_from_url_api_support_is_called_once(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        int contextId = 12345;
        String json = TestFileReader.readTextFromFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.importOpenApiFile(any(), any(), anyInt())).thenReturn(apiResponse);
        when(clientApiWrapper.importOpenApiDefintionFromUrl(any(), any(), anyInt())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(clientApiWrapper, never()).importOpenApiFile(any(), any(), anyInt());
        verify(clientApiWrapper).importOpenApiDefintionFromUrl(any(), any(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-from-file-and-url.json" })
    void import_openapi_from_file_and_from_url_api_support_is_called_once(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        int contextId = 12345;
        String json = TestFileReader.readTextFromFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();

        List<File> apiFiles = new ArrayList<>();
        apiFiles.add(new File("openapi3.json"));

        when(scanContext.getApiDefinitionFiles()).thenReturn(apiFiles);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.importOpenApiFile(any(), any(), anyInt())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.loadApiDefinitions(contextId);

        /* test */
        verify(clientApiWrapper).importOpenApiFile(any(), any(), anyInt());
        verify(clientApiWrapper).importOpenApiDefintionFromUrl(any(), any(), anyInt());
    }

    @Test
    void import_client_certificate_file_but_client_certificate_file_is_null_api_support_is_never_called() throws ClientApiException {
        /* prepare */
        when(clientApiWrapper.importPkcs12ClientCertificate(any(), any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.importClientCertificate();

        /* test */
        verify(clientApiWrapper, never()).importOpenApiFile(any(), any(), anyInt());
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

        when(scanContext.getClientCertificateFile()).thenReturn(clientCertificateFile);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.importPkcs12ClientCertificate(any(), any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.importClientCertificate();

        /* test */
        verify(clientApiWrapper, never()).importPkcs12ClientCertificate(any(), any());
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

        when(scanContext.getClientCertificateFile()).thenReturn(clientCertificateFile);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientCertificateFile.exists()).thenReturn(true);

        when(clientApiWrapper.importPkcs12ClientCertificate(any(), any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.importClientCertificate();

        /* test */
        verify(clientApiWrapper).importPkcs12ClientCertificate(any(), any());
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

        when(scanContext.getClientCertificateFile()).thenReturn(clientCertificateFile);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);
        when(clientCertificateFile.exists()).thenReturn(true);

        when(clientApiWrapper.importPkcs12ClientCertificate(any(), any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.importClientCertificate();

        /* test */
        verify(clientApiWrapper).importPkcs12ClientCertificate(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json",
            "src/test/resources/sechub-config-examples/form-based-auth.json" })
    void configure_login_inside_zap_using_no_auth_and_unsupported_auth_return_null(String sechubConfigFile) throws ClientApiException {
        /* prepare */
        int contextId = 12345;
        String json = TestFileReader.readTextFromFile(sechubConfigFile);
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();

        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        /* execute */
        UserInformation userInformation = scannerToTest.setupLoginInsideZapContext(contextId);

        /* test */
        assertEquals(null, userInformation);
    }

    @Test
    void configure_login_inside_zap_using_basic_auth_results_in_expected_calls() throws ClientApiException, MalformedURLException {
        /* prepare */
        int userId = 123;
        int contextId = 12345;
        URL targetUrl = URI.create("https://127.0.0.1:8000").toURL();
        String json = TestFileReader.readTextFromFile("src/test/resources/sechub-config-examples/basic-auth.json");
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(json).getWebScan().get();
        BasicLoginConfiguration basicLoginConfiguration = sechubWebScanConfig.getLogin().get().getBasic().get();
        String userName = new String(basicLoginConfiguration.getUser());

        String zapAuthenticationMethod = ZapAuthenticationType.HTTP_BASIC_AUTHENTICATION.getZapAuthenticationMethod();
        String zapSessionManagementMethod = ZapSessionManagementType.HTTP_AUTH_SESSION_MANAGEMENT.getZapSessionManagementMethod();

        when(scanContext.getTargetUrl()).thenReturn(targetUrl);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.setAuthenticationMethod(eq(contextId), eq(zapAuthenticationMethod), any())).thenReturn(apiResponse);
        when(clientApiWrapper.setSessionManagementMethod(eq(contextId), eq(zapSessionManagementMethod), any())).thenReturn(apiResponse);
        when(clientApiWrapper.createNewUser(contextId, userName)).thenReturn(userId);
        when(clientApiWrapper.configureAuthenticationCredentials(eq(contextId), eq(userId), any())).thenReturn(apiResponse);
        when(clientApiWrapper.setForcedUser(contextId, userId)).thenReturn(apiResponse);
        when(clientApiWrapper.setForcedUserModeEnabled(true)).thenReturn(apiResponse);

        /* execute */
        UserInformation userInformation = scannerToTest.setupLoginInsideZapContext(contextId);

        /* test */
        assertEquals(userName, userInformation.userName());
        assertEquals(userId, userInformation.zapuserId());

        verify(scanContext, times(2)).getTargetUrl();

        verify(clientApiWrapper).setAuthenticationMethod(eq(contextId), eq(zapAuthenticationMethod), any());
        verify(clientApiWrapper).setSessionManagementMethod(eq(contextId), eq(zapSessionManagementMethod), any());
        verify(clientApiWrapper).createNewUser(contextId, userName);
        verify(clientApiWrapper).configureAuthenticationCredentials(eq(contextId), eq(userId), any());
        verify(clientApiWrapper).setForcedUser(contextId, userId);
        verify(clientApiWrapper).setForcedUserModeEnabled(true);
    }

    @Test
    void configure_login_inside_zap_using_script_auth_without_script_file_results_in_script_login_not_being_called() throws Exception {
        /* prepare */
        int userId = 123;
        int contextId = 12345;
        String userName = "user";
        URL targetUrl = URI.create("https://127.0.0.1:8000").toURL();
        SecHubWebScanConfiguration sechubWebScanConfig = new SecHubWebScanConfiguration();
        sechubWebScanConfig.setUrl(targetUrl.toURI());
        WebLoginConfiguration login = new WebLoginConfiguration();
        sechubWebScanConfig.setLogin(Optional.of(login));

        String zapAuthenticationMethod = ZapAuthenticationType.MANUAL_AUTHENTICATION.getZapAuthenticationMethod();
        String zapSessionManagementMethod = ZapSessionManagementType.COOKIE_BASED_SESSION_MANAGEMENT.getZapSessionManagementMethod();

        when(scriptLogin.login(scanContext, clientApiWrapper)).thenReturn("zap-auth-session");

        when(scanContext.getTargetUrl()).thenReturn(targetUrl);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);
        when(scanContext.getGroovyScriptLoginFile()).thenReturn(null);

        when(clientApiWrapper.setAuthenticationMethod(eq(contextId), eq(zapAuthenticationMethod), any())).thenReturn(apiResponse);
        when(clientApiWrapper.setSessionManagementMethod(eq(contextId), eq(zapSessionManagementMethod), any())).thenReturn(apiResponse);
        when(clientApiWrapper.createNewUser(contextId, userName)).thenReturn(userId);
        when(clientApiWrapper.configureAuthenticationCredentials(eq(contextId), eq(userId), any())).thenReturn(apiResponse);
        when(clientApiWrapper.setForcedUser(contextId, userId)).thenReturn(apiResponse);
        when(clientApiWrapper.setForcedUserModeEnabled(true)).thenReturn(apiResponse);

        /* execute */
        UserInformation userInformation = scannerToTest.setupLoginInsideZapContext(contextId);

        /* test */
        assertNull(userInformation);
        verify(scriptLogin, never()).login(scanContext, clientApiWrapper);
        verify(scanContext).getGroovyScriptLoginFile();
    }

    @Test
    void configure_login_inside_zap_using_script_auth_with_existing_script_file_results_in_script_login_being_called() throws Exception {
        /* prepare */
        int userId = 123;
        int contextId = 12345;
        String userName = "DUMMY";
        URL targetUrl = URI.create("https://127.0.0.1:8000").toURL();
        SecHubWebScanConfiguration sechubWebScanConfig = new SecHubWebScanConfiguration();
        sechubWebScanConfig.setUrl(targetUrl.toURI());
        WebLoginConfiguration login = new WebLoginConfiguration();
        sechubWebScanConfig.setLogin(Optional.of(login));

        File scriptFile = new File("src/test/resources/login-script-examples/test-script.groovy");

        when(scriptLogin.login(scanContext, clientApiWrapper)).thenReturn("zap-auth-session");

        when(scanContext.getTargetUrl()).thenReturn(targetUrl);
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);
        when(scanContext.getGroovyScriptLoginFile()).thenReturn(scriptFile);

        when(clientApiWrapper.setManualAuthenticationMethod(contextId)).thenReturn(apiResponse);
        when(clientApiWrapper.setCookieBasedSessionManagementMethod(contextId)).thenReturn(apiResponse);
        when(clientApiWrapper.createNewUser(contextId, userName)).thenReturn(userId);
        when(clientApiWrapper.configureAuthenticationCredentials(eq(contextId), eq(userId), any())).thenReturn(apiResponse);
        when(clientApiWrapper.setForcedUser(contextId, userId)).thenReturn(apiResponse);
        when(clientApiWrapper.setForcedUserModeEnabled(true)).thenReturn(apiResponse);

        /* execute */
        UserInformation userInformation = scannerToTest.setupLoginInsideZapContext(contextId);

        /* test */
        assertEquals(userName, userInformation.userName());
        assertEquals(userId, userInformation.zapuserId());

        verify(scriptLogin).login(scanContext, clientApiWrapper);
        verify(scanContext).getGroovyScriptLoginFile();

        verify(clientApiWrapper).setManualAuthenticationMethod(contextId);
        verify(clientApiWrapper).setCookieBasedSessionManagementMethod(contextId);
        verify(clientApiWrapper).createNewUser(contextId, userName);
        verify(clientApiWrapper).configureAuthenticationCredentials(eq(contextId), eq(userId), any());
        verify(clientApiWrapper).setForcedUser(contextId, userId);
        verify(clientApiWrapper).setForcedUserModeEnabled(true);
    }

    @Test
    void generate_report_calls_api_support_once() throws ClientApiException {
        /* prepare */
        when(scanContext.getReportFile())
                .thenReturn(Paths.get("src/test/resources/sechub-config-examples/no-auth-with-openapi-file.json"));

        when(clientApiWrapper.generateReport(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.generateZapReport();

        /* test */
        verify(clientApiWrapper).generateReport(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void cleanup_after_scan() throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubwebScanConfig = new SecHubWebScanConfiguration();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubwebScanConfig);
        when(clientApiWrapper.removeReplacerRule(any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.cleanUp();

        /* test */
        verify(clientApiWrapper).removeReplacerRule(ZapScanner.X_SECHUB_DAST_HEADER_NAME);
        verify(scriptLogin).cleanUpScriptLoginData(scanContext.getTargetUrlAsString(), clientApiWrapper);
    }

    @ParameterizedTest
    @MethodSource("headerPartWithoutOnlyForUrlsTestNamedArguments")
    void cleanup_after_scan_without_onylForUrls_headers_set_cleans_up_all_replacer_rules(String sechubScanConfigJSON) throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.removeReplacerRule(any())).thenReturn(apiResponse);

        /* execute */
        scannerToTest.cleanUp();

        /* test */
        int times = sechubWebScanConfig.getHeaders().get().size();
        verify(clientApiWrapper).removeReplacerRule(ZapScanner.X_SECHUB_DAST_HEADER_NAME);
        verify(clientApiWrapper, times(times + 1)).removeReplacerRule(any());
        verify(scriptLogin).cleanUpScriptLoginData(scanContext.getTargetUrlAsString(), clientApiWrapper);
    }

    @ParameterizedTest
    @MethodSource("headerPartWithOnlyForUrlsTestNamedArguments")
    void cleanup_after_scan_with_onylForUrls_headers_set_cleans_up_all_replacer_rules(String sechubScanConfigJSON) throws ClientApiException {
        /* prepare */
        SecHubWebScanConfiguration sechubWebScanConfig = SecHubScanConfiguration.createFromJSON(sechubScanConfigJSON).getWebScan().get();
        when(scanContext.getSecHubWebScanConfiguration()).thenReturn(sechubWebScanConfig);

        when(clientApiWrapper.removeReplacerRule(any())).thenReturn(apiResponse);

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
        verify(clientApiWrapper).removeReplacerRule(ZapScanner.X_SECHUB_DAST_HEADER_NAME);
        verify(clientApiWrapper, times(times + 1)).removeReplacerRule(any());
        verify(scriptLogin).cleanUpScriptLoginData(scanContext.getTargetUrlAsString(), clientApiWrapper);
    }

    @Test
    void wait_for_ajaxSpider_scan_is_cancelled_results_in_exception_with_dedicated_exit_code()
            throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(zapPDSEventHandler).cancelScan(CONTEXT_NAME);

        when(clientApiWrapper.stopAjaxSpider()).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForAjaxSpiderResults();
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(zapPDSEventHandler, times(2)).isScanCancelled();
        verify(clientApiWrapper).stopAjaxSpider();
    }

    @Test
    void wait_for_ajaxSpider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(clientApiWrapper.stopAjaxSpider()).thenReturn(null);
        when(clientApiWrapper.getAjaxSpiderStatus()).thenReturn("Running").thenReturn("stopped");

        /* execute */
        scannerToTest.waitForAjaxSpiderResults();

        /* test */
        verify(clientApiWrapper, times(2)).getAjaxSpiderStatus();
        verify(clientApiWrapper).stopAjaxSpider();
    }

    @Test
    void wait_for_spider_scan_is_cancelled_results_in_exception_with_dedicated_exit_code() throws ClientApiException {
        /* prepare */
        int scanId = 111111;
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(zapPDSEventHandler).cancelScan(CONTEXT_NAME);

        when(clientApiWrapper.stopSpiderScan(scanId)).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForSpiderResults(scanId);
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(zapPDSEventHandler, times(2)).isScanCancelled();
        verify(clientApiWrapper).stopSpiderScan(scanId);
    }

    @Test
    void wait_for_passiveScan_scan_is_cancelled_results_in_exception_with_dedicated_exit_code()
            throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(zapPDSEventHandler).cancelScan(CONTEXT_NAME);


        when(clientApiWrapper.getNumberOfPassiveScannerRecordsToScan()).thenReturn(12);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.runAndWaitForPassiveScan();
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(zapPDSEventHandler, times(2)).isScanCancelled();
        verify(clientApiWrapper, atLeast(1)).getNumberOfPassiveScannerRecordsToScan();
    }

    @Test
    void wait_for_passiveScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);


        when(clientApiWrapper.getNumberOfPassiveScannerRecordsToScan()).thenReturn(0);

        /* execute */
        scannerToTest.runAndWaitForPassiveScan();

        /* test */
        verify(clientApiWrapper).getNumberOfPassiveScannerRecordsToScan();
    }

    @Test
    void wait_for_activeScan_scan_is_cancelled_results_in_exception_with_dedicated_exit_code() throws ClientApiException {
        /* prepare */
        int scanId = 111111;
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(true);
        doCallRealMethod().when(zapPDSEventHandler).cancelScan(CONTEXT_NAME);

        when(clientApiWrapper.getActiveScannerStatusForScan(scanId)).thenReturn(42);
        when(clientApiWrapper.stopActiveScan(scanId)).thenReturn(null);

        /* execute */
        ZapWrapperRuntimeException exception = assertThrows(ZapWrapperRuntimeException.class, () -> {
            scannerToTest.waitForActiveScanResults(scanId);
        });

        /* test */
        assertEquals(ZapWrapperExitCode.SCAN_JOB_CANCELLED, exception.getExitCode());
        verify(zapPDSEventHandler, times(2)).isScanCancelled();
        verify(clientApiWrapper, never()).getActiveScannerStatusForScan(scanId);
        verify(clientApiWrapper).stopActiveScan(scanId);
    }

    @Test
    void wait_for_activeScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        int scanId = 111111;
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(clientApiWrapper.getActiveScannerStatusForScan(scanId)).thenReturn(100);
        when(clientApiWrapper.stopActiveScan(scanId)).thenReturn(null);

        /* execute */
        scannerToTest.waitForActiveScanResults(scanId);

        /* test */
        verify(clientApiWrapper, atLeast(1)).getActiveScannerStatusForScan(scanId);
        verify(clientApiWrapper).stopActiveScan(scanId);
    }

    @Test
    void run_ajaxSpider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(clientApiWrapper.stopAjaxSpider()).thenReturn(null);
        when(clientApiWrapper.getAjaxSpiderStatus()).thenReturn("Running").thenReturn("stopped");

        /* execute */
        scannerToTest.runAndWaitAjaxSpider();

        /* test */
        verify(clientApiWrapper, times(2)).getAjaxSpiderStatus();
        verify(clientApiWrapper).stopAjaxSpider();
    }

    @Test
    void run_spider_scan_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        int scanId = 111111;
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        ZapProductMessageHelper messageHelper = mock(ZapProductMessageHelper.class);
        when(scanContext.getZapProductMessageHelper()).thenReturn(messageHelper);

        when(clientApiWrapper.stopSpiderScan(scanId)).thenReturn(null);
        when(clientApiWrapper.getSpiderStatusForScan(scanId)).thenReturn(42).thenReturn(100);
        when(clientApiWrapper.logFullSpiderResults(scanId)).thenReturn(0L);
        when(clientApiWrapper.startSpiderScan(any(), any(), anyBoolean(), any(), anyBoolean())).thenReturn(scanId);

        /* execute */
        scannerToTest.runAndWaitForSpider();

        /* test */
        verify(scanContext).getZapProductMessageHelper();
        verify(clientApiWrapper, times(2)).getSpiderStatusForScan(scanId);
        verify(clientApiWrapper).stopSpiderScan(scanId);
        verify(clientApiWrapper).logFullSpiderResults(scanId);
        verify(clientApiWrapper).startSpiderScan(any(), any(), anyBoolean(), any(), anyBoolean());
    }

    @Test
    void run_activeScan_scan_is_ended_results_in_expected_calls() throws ClientApiException {
        /* prepare */
        int scanId = 111111;
        when(zapPDSEventHandler.isScanCancelled()).thenReturn(false);

        when(clientApiWrapper.getActiveScannerStatusForScan(scanId)).thenReturn(100);
        when(clientApiWrapper.stopActiveScan(scanId)).thenReturn(null);
        when(clientApiWrapper.startActiveScan(any(), anyBoolean(), anyBoolean(), any(), any(), any(), anyInt())).thenReturn(scanId);
        when(clientApiWrapper.atLeastOneURLDetected()).thenReturn(true);

        /* execute */
        scannerToTest.runAndWaitActiveScan(scanId);

        /* test */
        verify(clientApiWrapper).getActiveScannerStatusForScan(scanId);
        verify(clientApiWrapper).stopActiveScan(scanId);
        verify(clientApiWrapper).startActiveScan(any(), anyBoolean(), anyBoolean(), any(), any(), any(), anyInt());
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

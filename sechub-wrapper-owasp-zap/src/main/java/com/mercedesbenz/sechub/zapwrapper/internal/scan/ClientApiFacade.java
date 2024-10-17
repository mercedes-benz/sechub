// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.internal.scan;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ApiResponseSet;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

public class ClientApiFacade {

    private static final String URL_KEY = "url";
    private static final String STATUS_CODE_KEY = "statusCode";
    private static final String STATUS_REASON_KEY = "statusReason";
    private static final String METHOD_KEY = "method";

    private static final Logger LOG = LoggerFactory.getLogger(ClientApiFacade.class);

    private ClientApi clientApi;

    public ClientApiFacade(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    /**
     * Create new context inside the ZAP with the given name.
     *
     * @param contextName context name that will be created inside the current ZAP
     *                    session
     * @return api response of ZAP contextId returned by ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     *                            when anything goes wrong communicating with ZAP
     */
    public String createNewContext(String contextName) throws ClientApiException {
        ApiResponseElement createContextResponse = ((ApiResponseElement) clientApi.context.newContext(contextName));
        return getIdOfApiResponseElement(createContextResponse);
    }

    /**
     * Create a new session inside the ZAP. Overwriting files if the parameter is
     * set.
     *
     * @param contextName default context name inside the new ZAP session
     * @param overwrite   force the overwrite of the current session
     * @return api response of ZAP api response of ZAP
     *
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse createNewSession(String contextName, String overwrite) throws ClientApiException {
        return clientApi.core.newSession(contextName, overwrite);
    }

    /**
     * Set maximum alerts a rule can raise.
     *
     * @param maximum specifies the maximum number of alerts each rule can raise.
     *                Setting "0" means unlimited amount of alerts for each rule.
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse configureMaximumAlertsForEachRule(String maximum) throws ClientApiException {
        return clientApi.core.setOptionMaximumAlertInstances(maximum);
    }

    /**
     * Enables all passive rules.
     *
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse enableAllPassiveScannerRules() throws ClientApiException {
        return clientApi.pscan.enableAllScanners();
    }

    /**
     * Enable all active rules for the given policy.
     *
     * @param policy specifies the policy that will be configured. Configuring
     *               <code>null</code> configures the default policy.
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse enableAllActiveScannerRulesForPolicy(String policy) throws ClientApiException {
        return clientApi.ascan.enableAllScanners(null);
    }

    /**
     * Set the Browser used by the AjaxSpider.
     *
     * @param browserId Id of the browser that shall be used by ZAP
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse configureAjaxSpiderBrowserId(String browserId) throws ClientApiException {
        return clientApi.ajaxSpider.setOptionBrowserId(browserId);
    }

    /**
     * Disable passive rule by given ruleId.
     *
     * @param ruleId id of the rule that will be disabled
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse disablePassiveScannerRule(String ruleId) throws ClientApiException {
        return clientApi.pscan.disableScanners(ruleId);
    }

    /**
     * Disable the given rule by ID inside the given policy.
     *
     * @param ruleId id of the rule that will be disabled
     * @param policy specifies the policy that will be configured. Configuring
     *               <code>null</code> configures the default policy.
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse disableActiveScannerRuleForPolicy(String ruleId, String policy) throws ClientApiException {
        return clientApi.ascan.disableScanners(ruleId, null);
    }

    /**
     * Set HTTP proxy with the given parameters.
     *
     * @param host     hostname of the proxy
     * @param port     port of the proxy
     * @param realm    realm of the proxy
     * @param username username to access the proxy
     * @param password password to access the proxy
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse configureHttpProxy(String host, String port, String realm, String username, String password) throws ClientApiException {
        return clientApi.network.setHttpProxy(host, port, realm, username, password);
    }

    /**
     * Enable or disable HTTP proxy.
     *
     * @param enabled if "true" proxy will be used by ZAP, if "false" proxy will not
     *                be used by ZAP.
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setHttpProxyEnabled(String enabled) throws ClientApiException {
        return clientApi.network.setHttpProxyEnabled(enabled);
    }

    /**
     * Enable or disable HTTP proxy authentication.
     *
     * @param enabled if "true" proxy will be used by ZAP, if "false" proxy will not
     *                be used by ZAP.
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setHttpProxyAuthEnabled(String enabled) throws ClientApiException {
        return clientApi.network.setHttpProxyAuthEnabled(enabled);
    }

    /**
     * Add replacer rule. If a entry already exists from the last scan it is
     * replaced.
     *
     * @param description Id of the created or overwritten replacer rule
     * @param enabled     "true"/"false" to enable/disable the replacer rule
     * @param matchtype   is one of [REQ_HEADER, REQ_HEADER_STR, REQ_BODY_STR,
     *                    RESP_HEADER, RESP_HEADER_STR, RESP_BODY_STR]
     * @param matchregex  "true" if the matchString shall be treated as regex. When
     *                    "false" simple string comparison is used.
     * @param matchstring matchString is the string that will be matched against
     * @param replacement replacement is the replacement string
     * @param initiators  initiators may be blank (for all initiators) or a comma
     *                    separated list of integers as defined in <a href=
     *                    "https://github.com/zaproxy/zaproxy/blob/main/zap/src/main/java/org/parosproxy/paros/network/HttpSender.java">HttpSender</a>
     * @param url         pattern this replacer rule shall be used for. If
     *                    <code>null</code> it is applied for any URL.
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse addReplacerRule(String description, String enabled, String matchtype, String matchregex, String matchstring, String replacement,
            String initiators, String url) throws ClientApiException {
        try {
            return clientApi.replacer.addRule(description, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
        } catch (ClientApiException e) {
            String message = e.getMessage();
            if ("already exists".equalsIgnoreCase(message)) {
                clientApi.replacer.removeRule(description);
            }
            return clientApi.replacer.addRule(description, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
        }
    }

    /**
     * Include URL pattern to the given context.
     *
     * @param contextName name of the context the given URL pattern shall be added
     *                    to
     * @param urlPattern  regex URL pattern
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse addIncludeUrlPatternToContext(String contextName, String urlPattern) throws ClientApiException {
        return clientApi.context.includeInContext(contextName, urlPattern);
    }

    /**
     * Exclude URL pattern from the given context.
     *
     * @param contextName name of the context the given URL pattern shall be added
     *                    to
     * @param urlPattern  regex URL pattern
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse addExcludeUrlPatternToContext(String contextName, String urlPattern) throws ClientApiException {
        return clientApi.context.excludeFromContext(contextName, urlPattern);
    }

    /**
     * Access an URL through the ZAP. Successfully accessing the site will add it to
     * the site tree.
     *
     * @param url             must be a valid URL the ZAP can access.
     * @param followRedirects "true"/"false" depending if you want the ZAP to follow
     *                        redirects or not.
     * @return api response of ZAP ApiResponse of ZAP or <code>null</code> when URL
     *         was not accessible.
     */
    public ApiResponse accessUrlViaZap(String url, String followRedirects) {
        ApiResponse response = null;
        try {
            response = clientApi.core.accessUrl(url, followRedirects);
        } catch (ClientApiException e) {
            LOG.error("While trying to access URL {} got the error: {}", url, e.getMessage());
        }
        return response;
    }

    /**
     * Import the given openApi file in the context with the given ID. While
     * importing the file the ZAP tries to access all API endpoints via the given
     * URL and adds them to the sites tree if they could be accessed.
     *
     * @param openApiFile
     * @param url
     * @param contextId
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse importOpenApiFile(String openApiFile, String url, String contextId) throws ClientApiException {
        return clientApi.openapi.importFile(openApiFile, url, contextId);
    }

    /**
     * Import the given openApi in the context from the given apiDefinitionUrl.
     * While importing from the URL the ZAP tries to access all API endpoints via
     * the given targetUrl and adds them to the sites tree if they could be
     * accessed.
     *
     * @param apiDefinitionUrl URL with the openApi/swagger definition
     * @param targetUrl        targetUrl of the application, generally the base URL
     * @param contextId        Id of the context to which the API definitions shall
     *                         be added
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse importOpenApiDefintionFromUrl(URL apiDefinitionUrl, String targetUrl, String contextId) throws ClientApiException {
        return clientApi.openapi.importUrl(apiDefinitionUrl.toString(), targetUrl, contextId);
    }

    /**
     * Import the given PKCS12 client certificate using the optional client
     * certificates password if necessary.
     *
     * @param filepath path to the PKCS12 certificate
     * @param password password of the certificate file
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse importPkcs12ClientCertificate(String filepath, String password) throws ClientApiException {
        // add the client certificate to the list ZAP keeps inside the network add-on
        // the third parameter here always "0" is the index where to import inside the
        // ZAP internal list
        return clientApi.network.addPkcs12ClientCertificate(filepath, password, "0");
    }

    /**
     * Enable client certificate.
     *
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse enableClientCertificate() throws ClientApiException {
        return clientApi.network.setUseClientCertificate("true");
    }

    public ApiResponse disableClientCertificate() throws ClientApiException {
        return clientApi.network.setUseClientCertificate("false");
    }

    /**
     * This method checks if the site tree is empty. The ZAP creates the site tree
     * while crawling and detecting pages. The method is necessary since the active
     * scanner exits with an exception if the site tree is empty, when starting an
     * active scan.
     *
     * This can only happen in very few cases, but then we want to be able to inform
     * the user and write a report which is empty or contains at least the passively
     * detected results.
     *
     * @return true if at least one URL was detected by ZAP, false otherwise
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public boolean atLeastOneURLDetected() throws ClientApiException {
        ApiResponseList sitesList = (ApiResponseList) clientApi.core.sites();
        return sitesList.getItems().size() > 0;
    }

    /**
     * Removes a replacer rule by the given description. (Description is the ID for
     * the replacer rule inside ZAP)
     *
     * @param description Id of the replacer rule that shall be removed
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse removeReplacerRule(String description) throws ClientApiException {
        return clientApi.replacer.removeRule(description);
    }

    public ApiResponse generateReport(String title, String template, String theme, String description, String contexts, String sites, String sections,
            String includedconfidences, String includedrisks, String reportfilename, String reportfilenamepattern, String reportdir, String display)
            throws ClientApiException {
        return clientApi.reports.generate(title, template, theme, description, contexts, sites, sections, includedconfidences, includedrisks, reportfilename,
                reportfilenamepattern, reportdir, display);
    }

    /**
     * Check the status of the ajax spider scan.
     *
     * @return The status as string after the ajax spider scan is started it is
     *         either "running" or "stopped".
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public String getAjaxSpiderStatus() throws ClientApiException {
        return ((ApiResponseElement) clientApi.ajaxSpider.status()).getValue();
    }

    /**
     * Stop the ajax spider.
     *
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse stopAjaxSpider() throws ClientApiException {
        return clientApi.ajaxSpider.stop();
    }

    /**
     * Stop the spider for the given scan ID.
     *
     * @param scanId spider Id
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse stopSpiderScan(String scanId) throws ClientApiException {
        return clientApi.spider.stop(scanId);
    }

    /**
     * Logs all spider results with additional meta data and counts the amount of
     * spider results logged.
     *
     * @param scanId
     * @return the amount of spider results logged
     * @throws ClientApiException
     */
    public long logFullSpiderResults(String scanId) throws ClientApiException {
        int numberOfSpiderResults = 0;
        ApiResponseList results = (ApiResponseList) clientApi.spider.fullResults(scanId);
        for (ApiResponse resultItem : results.getItems()) {
            ApiResponseList elementList = (ApiResponseList) resultItem;

            for (ApiResponse elementListItem : elementList.getItems()) {
                // It seems like an ApiResponseSet is present if the URL was in scope.
                // Otherwise, e.g. in case of third party services links like cloudflare or
                // anything else that the crawler detects, elementListItem is of type
                // ApiResponseElement, which does not contain a values map.
                if (elementListItem instanceof ApiResponseSet) {
                    ApiResponseSet apiResponseSet = (ApiResponseSet) elementListItem;
                    Map<String, String> result = createSafeMap(apiResponseSet.getValuesMap());
                    String url = result.get(URL_KEY);
                    // robots.txt and sitemap.xml always appear inside the sites tree even if they
                    // are not available. Because of this it is skipped here.
                    if (url.contains("robots.txt") || url.contains("sitemap.xml")) {
                        continue;
                    }
                    String statusCode = result.get(STATUS_CODE_KEY);
                    String statusReason = result.get(STATUS_REASON_KEY);
                    String method = result.get(METHOD_KEY);

                    LOG.info("URL: '{}' returned status code: '{}/{}' on detection phase for request method: '{}'", url, statusCode, statusReason, method);
                    numberOfSpiderResults++;
                }
            }
        }
        return numberOfSpiderResults;
    }

    /**
     * Get the status of the spider scan with a specific scan ID.
     *
     * @param scanId spider Id
     * @return api response of ZAP The status as a number between 0 and 100.
     *         (percentage of scan completion)
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public int getSpiderStatusForScan(String scanId) throws ClientApiException {
        ApiResponseElement status = (ApiResponseElement) clientApi.spider.status(scanId);
        return Integer.parseInt(status.getValue());
    }

    /**
     * Get the number of records left to scan for the passive scan.
     *
     * @param scanId passive scan Id
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public int getNumberOfPassiveScannerRecordsToScan() throws ClientApiException {
        ApiResponseElement recordsToScan = (ApiResponseElement) clientApi.pscan.recordsToScan();
        return Integer.parseInt(recordsToScan.getValue());
    }

    /**
     * Stop the active scanner for the given scan ID.
     *
     * @param scanId active scan Id
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse stopActiveScan(String scanId) throws ClientApiException {
        return clientApi.ascan.stop(scanId);
    }

    /**
     * Get the status of the active scan with a specific scan ID.
     *
     * @param scanId active scan Id
     * @return api response of ZAP The status as a number between 0 and 100.
     *         (percentage of scan completion)
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public int getActiveScannerStatusForScan(String scanId) throws ClientApiException {
        ApiResponseElement status = (ApiResponseElement) clientApi.ascan.status(scanId);
        return Integer.parseInt(status.getValue());
    }

    /**
     * Start the spider with the given parameters.
     *
     * @param targetUrlAsString URL to scan
     * @param maxChildren       limit the number of children scanned
     * @param recurse           "true"/"false" to prevent the spider from seeding
     *                          recursively
     * @param contextName       the context that shall be used for this scan
     * @param subTreeOnly       restrict the spider under a site's subtree
     * @return api response of ZAP the ID of the started spider scan
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public String startSpiderScan(String targetUrlAsString, String maxChildren, String recurse, String contextName, String subTreeOnly)
            throws ClientApiException {
        ApiResponse response = clientApi.spider.scan(targetUrlAsString, maxChildren, recurse, contextName, subTreeOnly);
        return getIdOfApiResponseElement((ApiResponseElement) response);
    }

    /**
     * Start the ajax spider with the given parameters.
     *
     * @param targetUrlAsString URL to scan
     * @param inScope           "true"/"false" either you want to scan only in scope
     *                          or beyond
     * @param contextName       the context that shall be used for this scan
     * @param subTreeOnly       restrict the ajax spider under a site's subtree
     * @return api response of ZAP the response of the ZAP API call
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse startAjaxSpiderScan(String targetUrlAsString, String inScope, String contextName, String subTreeOnly) throws ClientApiException {
        return clientApi.ajaxSpider.scan(targetUrlAsString, inScope, contextName, subTreeOnly);
    }

    /**
     * Start the active scanner with the given parameters.
     *
     * @param targetUrlAsString URL to scan
     * @param recurse           "true"/"false" to prevent the active scan from
     *                          seeding recursively
     * @param inScopeOnly       "true"/"false" either you want to scan only in scope
     *                          or beyond
     * @param scanPolicyName    active scan rule policy to use for the scan
     *                          <code>null</code> means default
     * @param method            method to use
     * @param postData          explicit post data
     * @return api response of ZAP the ID of the started active scan
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public String startActiveScan(String targetUrlAsString, String recurse, String inScopeOnly, String scanPolicyName, String method, String postData)
            throws ClientApiException {
        ApiResponse response = clientApi.ascan.scan(targetUrlAsString, recurse, inScopeOnly, scanPolicyName, method, postData);
        return getIdOfApiResponseElement((ApiResponseElement) response);
    }

    /**
     * Start the spider with the given parameters as the given user.
     *
     * @param contextId   Id of the context to use
     * @param userId      Id of the user to use
     * @param url         target URL to scan
     * @param maxchildren limit the number of children scanned
     * @param recurse     "true"/"false" to prevent the spider from seeding
     *                    recursively
     * @param subtreeonly restrict the spider under a site's subtree
     * @return api response of ZAP the ID of the started spider scan
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public String startSpiderScanAsUser(String contextId, String userId, String url, String maxchildren, String recurse, String subtreeonly)
            throws ClientApiException {
        ApiResponse response = clientApi.spider.scanAsUser(contextId, userId, url, maxchildren, recurse, subtreeonly);
        return getIdOfApiResponseElement((ApiResponseElement) response);
    }

    /**
     * Start the ajax spider with the given parameters as the given user.
     *
     * @param contextname the context that shall be used for this scan
     * @param username    name of the user that shall be used for the scan
     * @param url         target URL to scan
     * @param subtreeonly restrict the ajax spider under a site's subtree
     * @return api response of ZAP the response of the ZAP API call
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse startAjaxSpiderScanAsUser(String contextname, String username, String url, String subtreeonly) throws ClientApiException {
        return clientApi.ajaxSpider.scanAsUser(contextname, username, url, subtreeonly);
    }

    /**
     * Start the active scanner with the given parameters as the given user.
     *
     * @param url            target URL to scan
     * @param contextId      Id of the context to use
     * @param userId         Id of the user to use
     * @param recurse        "true"/"false" to prevent the active scan from seeding
     *                       recursively
     * @param scanpolicyname active scan rule policy to use for the scan
     *                       <code>null</code> means default
     * @param method         method to use
     * @param postData       explicit post data
     * @return api response of ZAP the ID of the started active scan
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public String startActiveScanAsUser(String url, String contextId, String userId, String recurse, String scanpolicyname, String method, String postdata)
            throws ClientApiException {
        ApiResponse response = clientApi.ascan.scanAsUser(url, contextId, userId, recurse, scanpolicyname, method, postdata);
        return getIdOfApiResponseElement((ApiResponseElement) response);
    }

    /**
     * Configure the given authentication method for the given context.
     *
     * @param contextId              Id of the context to use
     * @param authMethodName         Id of the authentication method
     * @param authMethodConfigParams required parameters for the authentication
     *                               method
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse configureAuthenticationMethod(String contextId, String authMethodName, String authMethodConfigParams) throws ClientApiException {
        return clientApi.authentication.setAuthenticationMethod(contextId, authMethodName, authMethodConfigParams);
    }

    /**
     * Set session management method for the given context.
     *
     * @param contextId          Id of the context to use
     * @param methodName         Id of the session management method
     * @param methodconfigparams required parameters of the session management
     *                           method
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setSessionManagementMethod(String contextId, String methodName, String methodconfigparams) throws ClientApiException {
        return clientApi.sessionManagement.setSessionManagementMethod(contextId, methodName, methodconfigparams);
    }

    /**
     * Create a new user inside the given context.
     *
     * @param contextId Id of the context to use
     * @param username  Name of the user that shall be created
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public String createNewUser(String contextId, String username) throws ClientApiException {
        ApiResponseElement creatUserResponse = ((ApiResponseElement) clientApi.users.newUser(contextId, username));
        return getIdOfApiResponseElement(creatUserResponse);
    }

    /**
     * Set authentication credentials for the given user inside the given context.
     *
     * @param contextId                   Id of the context to use
     * @param userId                      Id of the user that shall be configured
     *                                    for the scan.
     * @param authCredentialsConfigParams credential configuration for the given
     *                                    context
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse configureAuthenticationCredentials(String contextId, String userId, String authCredentialsConfigParams) throws ClientApiException {
        return clientApi.users.setAuthenticationCredentials(contextId, userId, authCredentialsConfigParams);
    }

    /**
     * Sets whether or not the user, should be enabled inside the given context.
     *
     * @param contextId Id of the context to use
     * @param userId    Id of the user that shall enabled/disabled.
     * @param enabled   "true"/"false" to enable/disable the user
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setUserEnabled(String contextId, String userId, String enabled) throws ClientApiException {
        return clientApi.users.setUserEnabled(contextId, userId, enabled);
    }

    /**
     * Set the user that will be used in forced user mode for the given context.
     *
     * @param contextId Id of the context to use
     * @param userId    Id of the user to use
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setForcedUser(String contextId, String userId) throws ClientApiException {
        return clientApi.forcedUser.setForcedUser(contextId, userId);
    }

    /**
     * Set if the forced user mode should be enabled or not.
     *
     * @param enabled "true"/"false" to enable/disable the enforced user mode
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setForcedUserModeEnabled(boolean enabled) throws ClientApiException {
        return clientApi.forcedUser.setForcedUserModeEnabled(enabled);
    }

    private String getIdOfApiResponseElement(ApiResponseElement apiResponseElement) {
        return apiResponseElement.getValue();
    }

    private Map<String, String> createSafeMap(Map<String, ApiResponse> valuesMap) {
        ApiResponse url = valuesMap.get(URL_KEY);
        ApiResponse statusCode = valuesMap.get(STATUS_CODE_KEY);
        ApiResponse statusReason = valuesMap.get(STATUS_REASON_KEY);
        ApiResponse method = valuesMap.get(METHOD_KEY);

        Map<String, String> safeMap = new HashMap<>();
        String safeUrl = url != null ? url.toString() : "";
        safeMap.put(URL_KEY, safeUrl);

        String safeStatusCode = statusCode != null ? statusCode.toString() : "";
        safeMap.put(STATUS_CODE_KEY, safeStatusCode);

        String safeStatusReason = statusReason != null ? statusReason.toString() : "";
        safeMap.put(STATUS_REASON_KEY, safeStatusReason);

        String safeMethod = method != null ? method.toString() : "";
        safeMap.put(METHOD_KEY, safeMethod);

        return safeMap;
    }
}

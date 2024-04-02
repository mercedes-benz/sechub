// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.internal.scan;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

public class ClientApiFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ClientApiFacade.class);

    private ClientApi clientApi;

    public ClientApiFacade(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    /**
     * Create new context inside the ZAP with the given name.
     *
     * @param contextName
     * @return contextId returned by ZAP
     * @throws ClientApiException
     */
    public String createNewContext(String contextName) throws ClientApiException {
        ApiResponseElement createContextResponse = ((ApiResponseElement) clientApi.context.newContext(contextName));
        return getIdOfApiResponseElement(createContextResponse);
    }

    /**
     * Create a new session inside the ZAP. Overwriting files if the parameter is
     * set.
     *
     * @param contextName
     * @param overwrite
     * @return
     * @throws ClientApiException
     */
    public ApiResponse createNewSession(String contextName, String overwrite) throws ClientApiException {
        return clientApi.core.newSession(contextName, overwrite);
    }

    /**
     * Set maximum alerts for rule.
     *
     * @param maximum
     * @return
     * @throws ClientApiException
     */
    public ApiResponse configureMaximumAlertsForEachRule(String maximum) throws ClientApiException {
        return clientApi.core.setOptionMaximumAlertInstances(maximum);
    }

    /**
     * Enables all passive rules.
     *
     * @return
     * @throws ClientApiException
     */
    public ApiResponse enableAllPassiveScannerRules() throws ClientApiException {
        return clientApi.pscan.enableAllScanners();
    }

    /**
     * Enable all active rules for the given policy.
     *
     * @param policy
     * @return
     * @throws ClientApiException
     */
    public ApiResponse enableAllActiveScannerRulesForPolicy(String policy) throws ClientApiException {
        return clientApi.ascan.enableAllScanners(null);
    }

    /**
     * Set the Browser used by the AjaxSpider.
     *
     * @param browserId
     * @return
     * @throws ClientApiException
     */
    public ApiResponse configureAjaxSpiderBrowserId(String browserId) throws ClientApiException {
        return clientApi.ajaxSpider.setOptionBrowserId(browserId);
    }

    /**
     *
     * @param ruleId
     * @return
     * @throws ClientApiException
     */
    public ApiResponse disablePassiveScannerRule(String ruleId) throws ClientApiException {
        return clientApi.pscan.disableScanners(ruleId);
    }

    /**
     * Disable the given rule by ID inside the given policy.
     *
     * @param ruleId
     * @param policy
     * @return
     * @throws ClientApiException
     */
    public ApiResponse disableActiveScannerRuleForPolicy(String ruleId, String policy) throws ClientApiException {
        return clientApi.ascan.disableScanners(ruleId, null);
    }

    /**
     * Set HTTP proxy with the given parameters.
     *
     * @param host
     * @param port
     * @param realm
     * @param username
     * @param password
     * @return
     * @throws ClientApiException
     */
    public ApiResponse configureHttpProxy(String host, String port, String realm, String username, String password) throws ClientApiException {
        return clientApi.network.setHttpProxy(host, port, realm, username, password);
    }

    /**
     * Set usage of a HTTP proxy.
     *
     * @param enabled
     * @return
     * @throws ClientApiException
     */
    public ApiResponse setHttpProxyEnabled(String enabled) throws ClientApiException {
        return clientApi.network.setHttpProxyEnabled(enabled);
    }

    /**
     * Set usage of HTTP proxy authentication.
     *
     * @param enabled
     * @return
     * @throws ClientApiException
     */
    public ApiResponse setHttpProxyAuthEnabled(String enabled) throws ClientApiException {
        return clientApi.network.setHttpProxyAuthEnabled(enabled);
    }

    /**
     * Add replacer rule. If a entry already exists from the last scan it is
     * replaced.
     *
     * @param description
     * @param enabled
     * @param matchtype
     * @param matchregex
     * @param matchstring
     * @param replacement
     * @param initiators
     * @param url
     * @return
     * @throws ClientApiException
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
     * @param contextName
     * @param urlPattern
     * @return
     * @throws ClientApiException
     */
    public ApiResponse addIncludeUrlPatternToContext(String contextName, String urlPattern) throws ClientApiException {
        return clientApi.context.includeInContext(contextName, urlPattern);
    }

    /**
     * Exclude URL pattern from the given context.
     *
     * @param contextName
     * @param urlPattern
     * @return
     * @throws ClientApiException
     */
    public ApiResponse addExcludeUrlPatternToContext(String contextName, String urlPattern) throws ClientApiException {
        return clientApi.context.excludeFromContext(contextName, urlPattern);
    }

    /**
     * Access an URL through the ZAP. Successfully accessing the site will add it to
     * the site tree.
     *
     * @param url
     * @param followRedirects
     * @return ApiResponse of ZAP or <code>null</code> when URL was not accessible.
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
     * @return
     * @throws ClientApiException
     */
    public ApiResponse importOpenApiFile(String openApiFile, String url, String contextId) throws ClientApiException {
        return clientApi.openapi.importFile(openApiFile, url, contextId);
    }

    /**
     *
     * @param apiDefinitionUrl
     * @param targetUrl
     * @param contextId
     * @return
     * @throws ClientApiException
     */
    public ApiResponse importOpenApiDefintionFromUrl(URL apiDefinitionUrl, String targetUrl, String contextId) throws ClientApiException {
        return clientApi.openapi.importUrl(apiDefinitionUrl.toString(), targetUrl, contextId);
    }

    /**
     * Import the given PKCS12 client certificate using the optional client
     * certificates password if necessary.
     *
     * @param filepath
     * @param password
     * @return
     * @throws ClientApiException
     */
    public ApiResponse importPkcs12ClientCertificate(String filepath, String password) throws ClientApiException {
        // add the client certificate to the list ZAP keeps inside the network add-on
        // the third parameter here always "0" is the index where to import inside the
        // ZAP internal list
        return clientApi.network.addPkcs12ClientCertificate(filepath, password, "0");
    }

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
     * @return
     * @throws ClientApiException
     */
    public boolean atLeastOneURLDetected() throws ClientApiException {
        ApiResponseList sitesList = (ApiResponseList) clientApi.core.sites();
        return sitesList.getItems().size() > 0;
    }

    /**
     * Removes a replacer rule by the given description. (Description is the ID for
     * the replacer rule)
     *
     * @param description
     * @return
     * @throws ClientApiException
     */
    public ApiResponse removeReplacerRule(String description) throws ClientApiException {
        return clientApi.replacer.removeRule(description);
    }

    /**
     * Generate a report for the given parameters.
     *
     * @param title
     * @param template
     * @param theme
     * @param description
     * @param contexts
     * @param sites
     * @param sections
     * @param includedconfidences
     * @param includedrisks
     * @param reportfilename
     * @param reportfilenamepattern
     * @param reportdir
     * @param display
     * @return
     * @throws ClientApiException
     */
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
     * @throws ClientApiException
     */
    public String getAjaxSpiderStatus() throws ClientApiException {
        return ((ApiResponseElement) clientApi.ajaxSpider.status()).getValue();
    }

    /**
     * Stop the ajax spider.
     *
     * @return
     * @throws ClientApiException
     */
    public ApiResponse stopAjaxSpider() throws ClientApiException {
        return clientApi.ajaxSpider.stop();
    }

    /**
     * Stop the spider for the given scan ID.
     *
     * @param scanId
     * @return
     * @throws ClientApiException
     */
    public ApiResponse stopSpiderScan(String scanId) throws ClientApiException {
        return clientApi.spider.stop(scanId);
    }

    /**
     * Get a list of all URLs detected by the spider scan.
     *
     * @return
     * @throws ClientApiException
     */
    public List<String> getAllSpiderUrls() throws ClientApiException {
        List<ApiResponse> results = ((ApiResponseList) clientApi.spider.allUrls()).getItems();
        List<String> urls = new ArrayList<>();
        for (ApiResponse response : results) {
            urls.add(response.toString());
        }
        return urls;
    }

    /**
     * Get the status of the spider scan with a specific scan ID.
     *
     * @param scanId
     * @return The status as a number between 0 and 100. (percentage of scan
     *         completion)
     * @throws ClientApiException
     */
    public int getSpiderStatusForScan(String scanId) throws ClientApiException {
        ApiResponseElement status = (ApiResponseElement) clientApi.spider.status(scanId);
        return Integer.parseInt(status.getValue());
    }

    /**
     * Get the number of records left to scan for the passive scan.
     *
     * @param scanId
     * @return
     * @throws ClientApiException
     */
    public int getNumberOfPassiveScannerRecordsToScan() throws ClientApiException {
        ApiResponseElement recordsToScan = (ApiResponseElement) clientApi.pscan.recordsToScan();
        return Integer.parseInt(recordsToScan.getValue());
    }

    /**
     * Stop the active scanner for the given scan ID.
     *
     * @param scanId
     * @return
     * @throws ClientApiException
     */
    public ApiResponse stopActiveScan(String scanId) throws ClientApiException {
        return clientApi.ascan.stop(scanId);
    }

    /**
     * Get the status of the active scan with a specific scan ID.
     *
     * @param scanId
     * @return The status as a number between 0 and 100. (percentage of scan
     *         completion)
     * @throws ClientApiException
     */
    public int getActiveScannerStatusForScan(String scanId) throws ClientApiException {
        ApiResponseElement status = (ApiResponseElement) clientApi.ascan.status(scanId);
        return Integer.parseInt(status.getValue());
    }

    /**
     * Start the spider with the given parameters.
     *
     * @param targetUrlAsString
     * @param maxChildren
     * @param recurse
     * @param contextName
     * @param subTreeOnly
     * @return the ID of the started spider scan
     * @throws ClientApiException
     */
    public String startSpiderScan(String targetUrlAsString, String maxChildren, String recurse, String contextName, String subTreeOnly)
            throws ClientApiException {
        ApiResponse response = clientApi.spider.scan(targetUrlAsString, maxChildren, recurse, contextName, subTreeOnly);
        return getIdOfApiResponseElement((ApiResponseElement) response);
    }

    /**
     * Start the ajax spider with the given parameters.
     *
     * @param targetUrlAsString
     * @param inScope
     * @param contextName
     * @param subTreeOnly
     * @return the response of the ZAP API call
     * @throws ClientApiException
     */
    public ApiResponse startAjaxSpiderScan(String targetUrlAsString, String inScope, String contextName, String subTreeOnly) throws ClientApiException {
        return clientApi.ajaxSpider.scan(targetUrlAsString, inScope, contextName, subTreeOnly);
    }

    /**
     * Start the active scanner with the given parameters.
     *
     * @param targetUrlAsString
     * @param recurse
     * @param inScopeOnly
     * @param scanPolicyName
     * @param method
     * @param postData
     * @return the ID of the started active scan
     * @throws ClientApiException
     */
    public String startActiveScan(String targetUrlAsString, String recurse, String inScopeOnly, String scanPolicyName, String method, String postData)
            throws ClientApiException {
        ApiResponse response = clientApi.ascan.scan(targetUrlAsString, recurse, inScopeOnly, scanPolicyName, method, postData);
        return getIdOfApiResponseElement((ApiResponseElement) response);
    }

    /**
     * Start the spider with the given parameters as the given user.
     *
     * @param contextId
     * @param userId
     * @param url
     * @param maxchildren
     * @param recurse
     * @param subtreeonly
     * @return the ID of the started spider scan
     * @throws ClientApiException
     */
    public String startSpiderScanAsUser(String contextId, String userId, String url, String maxchildren, String recurse, String subtreeonly)
            throws ClientApiException {
        ApiResponse response = clientApi.spider.scanAsUser(contextId, userId, url, maxchildren, recurse, subtreeonly);
        return getIdOfApiResponseElement((ApiResponseElement) response);
    }

    /**
     * Start the ajax spider with the given parameters as the given user.
     *
     * @param contextname
     * @param username
     * @param url
     * @param subtreeonly
     * @return the response of the ZAP API call
     * @throws ClientApiException
     */
    public ApiResponse startAjaxSpiderScanAsUser(String contextname, String username, String url, String subtreeonly) throws ClientApiException {
        return clientApi.ajaxSpider.scanAsUser(contextname, username, url, subtreeonly);
    }

    /**
     * Start the active scanner with the given parameters as the given user.
     *
     * @param url
     * @param contextId
     * @param userId
     * @param recurse
     * @param scanpolicyname
     * @param method
     * @param postdata
     * @return the ID of the started active scan
     * @throws ClientApiException
     */
    public String startActiveScanAsUser(String url, String contextId, String userId, String recurse, String scanpolicyname, String method, String postdata)
            throws ClientApiException {
        ApiResponse response = clientApi.ascan.scanAsUser(url, contextId, userId, recurse, scanpolicyname, method, postdata);
        return getIdOfApiResponseElement((ApiResponseElement) response);
    }

    /**
     * Configure the given authentication method for the given context.
     *
     * @param contextId
     * @param authMethodName
     * @param authMethodConfigParams
     * @return
     * @throws ClientApiException
     */
    public ApiResponse configureAuthenticationMethod(String contextId, String authMethodName, String authMethodConfigParams) throws ClientApiException {
        return clientApi.authentication.setAuthenticationMethod(contextId, authMethodName, authMethodConfigParams);
    }

    /**
     * Set session management method for the given context.
     *
     * @param contextId
     * @param methodName
     * @param methodconfigparams
     * @return
     * @throws ClientApiException
     */
    public ApiResponse setSessionManagementMethod(String contextId, String methodName, String methodconfigparams) throws ClientApiException {
        return clientApi.sessionManagement.setSessionManagementMethod(contextId, methodName, methodconfigparams);
    }

    /**
     * Create a new user inside the given context.
     *
     * @param contextId
     * @param username
     * @return
     * @throws ClientApiException
     */
    public String createNewUser(String contextId, String username) throws ClientApiException {
        ApiResponseElement creatUserResponse = ((ApiResponseElement) clientApi.users.newUser(contextId, username));
        return getIdOfApiResponseElement(creatUserResponse);
    }

    /**
     * Set authentication credentials for the given user inside the given context.
     *
     * @param contextId
     * @param userId
     * @param authCredentialsConfigParams
     * @return
     * @throws ClientApiException
     */
    public ApiResponse configureAuthenticationCredentials(String contextId, String userId, String authCredentialsConfigParams) throws ClientApiException {
        return clientApi.users.setAuthenticationCredentials(contextId, userId, authCredentialsConfigParams);
    }

    /**
     * Sets whether or not the user, should be enabled inside the given context.
     *
     * @param contextId
     * @param userId
     * @param enabled
     * @return
     * @throws ClientApiException
     */
    public ApiResponse setUserEnabled(String contextId, String userId, String enabled) throws ClientApiException {
        return clientApi.users.setUserEnabled(contextId, userId, enabled);
    }

    /**
     * Set the user that will be used in forced user mode for the given context.
     *
     * @param contextId
     * @param userId
     * @return
     * @throws ClientApiException
     */
    public ApiResponse setForcedUser(String contextId, String userId) throws ClientApiException {
        return clientApi.forcedUser.setForcedUser(contextId, userId);
    }

    /**
     * Set if the forced user mode should be enabled or not.
     *
     * @param enabled
     * @return
     * @throws ClientApiException
     */
    public ApiResponse setForcedUserModeEnabled(boolean enabled) throws ClientApiException {
        return clientApi.forcedUser.setForcedUserModeEnabled(enabled);
    }

    private String getIdOfApiResponseElement(ApiResponseElement apiResponseElement) {
        return apiResponseElement.getValue();
    }
}

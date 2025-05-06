// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.internal.scan;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ApiResponseSet;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.commons.model.WebLogoutConfiguration;
import com.mercedesbenz.sechub.commons.model.login.WebLoginVerificationConfiguration;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;
import com.mercedesbenz.sechub.zapwrapper.config.ProxyInformation;
import com.mercedesbenz.sechub.zapwrapper.config.auth.ZapAuthenticationType;
import com.mercedesbenz.sechub.zapwrapper.config.auth.ZapSessionManagementType;

public class ClientApiWrapper {

    public static final String ZAP_CONNECTION_REFUSED = "Connection refused";

    private static final String URL_KEY = "url";
    private static final String STATUS_CODE_KEY = "statusCode";
    private static final String STATUS_REASON_KEY = "statusReason";
    private static final String METHOD_KEY = "method";

    private static final String URLS_IN_SCOPE = "urlsInScope";

    private static final Logger LOG = LoggerFactory.getLogger(ClientApiWrapper.class);

    private ClientApi clientApi;

    public ClientApiWrapper(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    /**
     * Create new context inside the ZAP with the given name.
     *
     * @param contextName context name that will be created inside the current ZAP
     *                    session
     * @return contextId returned by ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     *                            when anything goes wrong communicating with ZAP
     */
    public int createNewContext(String contextName) throws ClientApiException {
        ApiResponseElement createContextResponse = ((ApiResponseElement) clientApi.context.newContext(contextName));
        return Integer.parseInt(getIdOfApiResponseElement(createContextResponse));
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
    public ApiResponse createNewSession(String contextName, boolean overwrite) throws ClientApiException {
        LOG.info("Creating new session: {} inside the Zap", contextName);
        // to ensure parts from previous scan are deleted
        return clientApi.core.newSession(contextName, Boolean.toString(overwrite));
    }

    /**
     * Set maximum alerts a rule can raise to unlimited.
     *
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setMaximumAlertsForEachRuleToUnlimited() throws ClientApiException {
        LOG.info("Setting default maximum number of alerts for each rule.");
        // setting this value to zero means unlimited
        return clientApi.core.setOptionMaximumAlertInstances("0");
    }

    /**
     * Enables all passive rules.
     *
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse enableAllPassiveScannerRules() throws ClientApiException {
        LOG.info("Enable all passive scan rules before configuration begins.");
        // enable all passive scanner rules by default
        return clientApi.pscan.enableAllScanners();
    }

    /**
     * Enable all active rules for the default policy.
     *
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse enableAllActiveScannerRulesForDefaultPolicy() throws ClientApiException {
        LOG.info("Enable all active scan rules for default policy before configuration begins.");
        // enable all passive scanner rules by default
        // null specifies the default scan policy
        return clientApi.ascan.enableAllScanners(null);

    }

    /**
     * Set the Browser used by the AjaxSpider.
     *
     * @param browserId Id of the browser that shall be used by ZAP
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setAjaxSpiderBrowserId(String browserId) throws ClientApiException {
        LOG.info("Set browser for ajaxSpider: {}.", browserId);
        return clientApi.ajaxSpider.setOptionBrowserId(browserId);
    }

    /**
     * Define the max depth of directories after the ajax spider will stop the
     * recursion.
     *
     * @param maxDepth
     * @return
     * @throws ClientApiException
     */
    public ApiResponse setAjaxSpiderMaxDepth(int maxDepth) throws ClientApiException {
        LOG.info("Set max directory depth of ajaxSpider to: {}", maxDepth);
        return clientApi.spider.setOptionMaxDepth(maxDepth);
    }

    /**
     * Define the max depth of directories after the spider will stop the recursion.
     *
     * @param maxDepth
     * @return
     * @throws ClientApiException
     */
    public ApiResponse setSpiderMaxDepth(int maxDepth) throws ClientApiException {
        LOG.info("Set max directory depth of spider to: {}", maxDepth);
        return clientApi.spider.setOptionMaxDepth(maxDepth);
    }

    /**
     * Disable passive rule by given ruleId.
     *
     * @param ruleId id of the rule that will be disabled
     * @return <code>true</code> if the rule was a passive rule and was deactivated,
     *         <code>false</code> if the rule was not a passive rule and was not
     *         deactivated
     * @throws ClientApiException when communication with ZAP is not possible
     */
    public boolean disablePassiveScannerRule(String ruleId) throws ClientApiException {
        try {
            clientApi.pscan.disableScanners(ruleId);
            LOG.info("Passive scanner rule: {}, was deactivated", ruleId);
            return true;
        } catch (ClientApiException e) {
            if (e.getMessage().equalsIgnoreCase(ZAP_CONNECTION_REFUSED)) {
                throw e;
            }
            LOG.warn("Rule with id: {} was not a passive scanner rule.", ruleId);
            return false;
        }
    }

    /**
     * Disable the given rule by ID inside the default policy.
     *
     * @param ruleId id of the rule that will be disabled
     * @return <code>true</code> if the rule was a passive rule and was deactivated,
     *         <code>false</code> if the rule was not a passive rule and was not
     *         deactivated
     * @throws ClientApiException when communication with ZAP is not possible
     */
    public boolean disableActiveScannerRuleForDefaultPolicy(String ruleId) throws ClientApiException {
        try {
            // null specifies the default scan policy
            clientApi.ascan.disableScanners(ruleId, null);
            LOG.info("Active scanner rule: {}, was deactivated", ruleId);
            return true;
        } catch (ClientApiException e) {
            if (e.getMessage().equalsIgnoreCase(ZAP_CONNECTION_REFUSED)) {
                throw e;
            }
            LOG.warn("Rule with id: {} was not an active scanner rule.", ruleId);
            return false;
        }
    }

    /**
     * Configure http proxy inside ZAP.
     *
     * @param proxyInformation
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse configureHttpProxy(ProxyInformation proxyInformation) throws ClientApiException {
        return clientApi.network.setHttpProxy(proxyInformation.getHost(), Integer.toString(proxyInformation.getPort()), proxyInformation.getRealm(),
                proxyInformation.getUsername(), proxyInformation.getPassword());
    }

    /**
     * Enable or disable HTTP proxy.
     *
     * @param enabled if "true" proxy will be used by ZAP, if "false" proxy will not
     *                be used by ZAP.
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setHttpProxyEnabled(boolean enabled) throws ClientApiException {
        return clientApi.network.setHttpProxyEnabled(Boolean.toString(enabled));
    }

    /**
     * Enable or disable HTTP proxy authentication.
     *
     * @param enabled if "true" proxy will be used by ZAP, if "false" proxy will not
     *                be used by ZAP.
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setHttpProxyAuthEnabled(boolean enabled) throws ClientApiException {
        return clientApi.network.setHttpProxyAuthEnabled(Boolean.toString(enabled));
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
    public ApiResponse addReplacerRule(String description, boolean enabled, String matchtype, boolean matchregex, String matchstring, String replacement,
            String initiators, String url) throws ClientApiException {
        try {
            return clientApi.replacer.addRule(description, Boolean.toString(enabled), matchtype, Boolean.toString(matchregex), matchstring, replacement,
                    initiators, url);
        } catch (ClientApiException e) {
            String message = e.getMessage();
            if ("already exists".equalsIgnoreCase(message)) {
                clientApi.replacer.removeRule(description);
            }
            return clientApi.replacer.addRule(description, Boolean.toString(enabled), matchtype, Boolean.toString(matchregex), matchstring, replacement,
                    initiators, url);
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
    public ApiResponse accessUrlViaZap(String url, boolean followRedirects) {
        ApiResponse response = null;
        try {
            LOG.info("Trying to access URL: {} via ZAP to make sure it is added to the sites tree with following redirects set to: {}.", url, followRedirects);
            response = clientApi.core.accessUrl(url, Boolean.toString(followRedirects));
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
    public ApiResponse importOpenApiFile(String openApiFile, String url, int contextId) throws ClientApiException {
        LOG.info("Loading openAPI file: {}", openApiFile.toString());
        return clientApi.openapi.importFile(openApiFile, url, Integer.toString(contextId));
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
    public ApiResponse importOpenApiDefintionFromUrl(URL apiDefinitionUrl, String targetUrl, int contextId) throws ClientApiException {
        LOG.info("Loading openAPI definition from : {}", apiDefinitionUrl.toString());
        return clientApi.openapi.importUrl(apiDefinitionUrl.toString(), targetUrl, Integer.toString(contextId));
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
        LOG.info("Loading client certificate file: {}", filepath);
        return clientApi.network.addPkcs12ClientCertificate(filepath, password, "0");
    }

    /**
     * Enable client certificate.
     *
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse enableClientCertificate() throws ClientApiException {
        LOG.info("Enable client certificate.");
        return clientApi.network.setUseClientCertificate("true");
    }

    public ApiResponse disableClientCertificate() throws ClientApiException {
        // disable client certificate here, the imported client certificate will be
        // removed on ZAP shutdown automatically anyway
        LOG.info("Disable client certificate if one was used for the scan.");
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
        LOG.info("Remove replacer rule: {} if it exists.", description);
        return clientApi.replacer.removeRule(description);
    }

    public ApiResponse generateReport(String title, String template, String theme, String description, String contexts, String sites, String sections,
            String includedconfidences, String includedrisks, String reportfilename, String reportfilenamepattern, String reportdir, String display)
            throws ClientApiException {
        File reportFile = new File(reportdir, reportfilename);
        LOG.info("Writing scan report to: {}", reportFile.getAbsolutePath());
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
    public ApiResponse stopSpiderScan(int scanId) throws ClientApiException {
        return clientApi.spider.stop(Integer.toString(scanId));
    }

    /**
     * Get the number of spider results.
     *
     * @param scanId
     * @return number of spider results
     * @throws ClientApiException
     */
    public long getNumberOfSpiderResults(int scanId) throws ClientApiException {
        int numberOfSpiderResults = 0;
        ApiResponseList results = (ApiResponseList) clientApi.spider.fullResults(Integer.toString(scanId));
        for (ApiResponse resultItem : results.getItems()) {
            ApiResponseList elementList = (ApiResponseList) resultItem;

            for (ApiResponse elementListItem : elementList.getItems()) {
                if (URLS_IN_SCOPE.equals(elementListItem.getName())) {
                    ApiResponseSet apiResponseSet = (ApiResponseSet) elementListItem;
                    Map<String, String> result = createSafeMap(apiResponseSet.getValuesMap());
                    String url = result.get(URL_KEY);
                    // robots.txt and sitemap.xml always appear inside the sites tree even if they
                    // are not available. Because of this it is skipped here.
                    if (url.contains("robots.txt") || url.contains("sitemap.xml")) {
                        continue;
                    }
                    numberOfSpiderResults++;
                }
            }
        }
        return numberOfSpiderResults;
    }

    /**
     * Get the number of ajaxSpider results.
     *
     * @return number of ajaxSpider results
     * @throws ClientApiException
     */
    public long getNumberOfAjaxSpiderResults() throws ClientApiException {
        return Long.parseLong(clientApi.ajaxSpider.numberOfResults().toString());
    }

    /**
     * Get the status of the spider scan with a specific scan ID.
     *
     * @param scanId spider Id
     * @return api response of ZAP The status as a number between 0 and 100.
     *         (percentage of scan completion)
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public int getSpiderStatusForScan(int scanId) throws ClientApiException {
        ApiResponseElement status = (ApiResponseElement) clientApi.spider.status(Integer.toString(scanId));
        return Integer.parseInt(status.getValue());
    }

    /**
     * Get the number of records left to scan for the passive scan.
     *
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
    public ApiResponse stopActiveScan(int scanId) throws ClientApiException {
        return clientApi.ascan.stop(Integer.toString(scanId));
    }

    /**
     * Get the status of the active scan with a specific scan ID.
     *
     * @param scanId active scan Id
     * @return api response of ZAP The status as a number between 0 and 100.
     *         (percentage of scan completion)
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public int getActiveScannerStatusForScan(int scanId) throws ClientApiException {
        ApiResponseElement status = (ApiResponseElement) clientApi.ascan.status(Integer.toString(scanId));
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
    public int startSpiderScan(String targetUrlAsString, String maxChildren, boolean recurse, String contextName, boolean subTreeOnly)
            throws ClientApiException {
        ApiResponse response = clientApi.spider.scan(targetUrlAsString, maxChildren, Boolean.toString(recurse), contextName, Boolean.toString(subTreeOnly));
        return Integer.parseInt(getIdOfApiResponseElement((ApiResponseElement) response));
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
    public ApiResponse startAjaxSpiderScan(String targetUrlAsString, boolean inScope, String contextName, boolean subTreeOnly) throws ClientApiException {
        return clientApi.ajaxSpider.scan(targetUrlAsString, Boolean.toString(inScope), contextName, Boolean.toString(subTreeOnly));
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
     * @param contextId         the contextId of the context to use for the scan
     * @return api response of ZAP the ID of the started active scan
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public int startActiveScan(String targetUrlAsString, boolean recurse, boolean inScopeOnly, String scanPolicyName, String method, String postData,
            int contextId) throws ClientApiException {
        ApiResponse response = clientApi.ascan.scan(targetUrlAsString, Boolean.toString(recurse), Boolean.toString(inScopeOnly), scanPolicyName, method,
                postData, contextId);
        return Integer.parseInt(getIdOfApiResponseElement((ApiResponseElement) response));
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
     * @param subtreeOnly restrict the spider under a site's subtree
     * @return api response of ZAP the ID of the started spider scan
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public int startSpiderScanAsUser(int contextId, int userId, String url, String maxchildren, boolean recurse, boolean subtreeOnly)
            throws ClientApiException {
        ApiResponse response = clientApi.spider.scanAsUser(Integer.toString(contextId), Integer.toString(userId), url, maxchildren, Boolean.toString(recurse),
                Boolean.toString(subtreeOnly));
        return Integer.parseInt(getIdOfApiResponseElement((ApiResponseElement) response));
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
    public ApiResponse startAjaxSpiderScanAsUser(String contextname, String username, String url, boolean subtreeonly) throws ClientApiException {
        return clientApi.ajaxSpider.scanAsUser(contextname, username, url, Boolean.toString(subtreeonly));
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
    public int startActiveScanAsUser(String url, int contextId, int userId, boolean recurse, String scanpolicyname, String method, String postData)
            throws ClientApiException {
        ApiResponse response = clientApi.ascan.scanAsUser(url, Integer.toString(contextId), Integer.toString(userId), Boolean.toString(recurse), scanpolicyname,
                method, postData);
        return Integer.parseInt(getIdOfApiResponseElement((ApiResponseElement) response));
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
    public ApiResponse setAuthenticationMethod(int contextId, String authMethodName, String authMethodConfigParams) throws ClientApiException {
        return clientApi.authentication.setAuthenticationMethod(Integer.toString(contextId), authMethodName, authMethodConfigParams);
    }

    /**
     * Configure manual authentication method for the given context.
     *
     * @param contextId Id of the context to use
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse setManualAuthenticationMethod(int contextId) throws ClientApiException {
        String authMethodConfigParams = null;
        String authMethodName = ZapAuthenticationType.MANUAL_AUTHENTICATION.getZapAuthenticationMethod();
        return clientApi.authentication.setAuthenticationMethod(Integer.toString(contextId), authMethodName, authMethodConfigParams);
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
    public ApiResponse setSessionManagementMethod(int contextId, String methodName, String methodconfigparams) throws ClientApiException {
        return clientApi.sessionManagement.setSessionManagementMethod(Integer.toString(contextId), methodName, methodconfigparams);
    }

    /**
     * Set cookie based session management method for the given context.
     *
     * @param contextId Id of the context to use
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse setCookieBasedSessionManagementMethod(int contextId) throws ClientApiException {
        String sessionMethodconfigparams = null;
        String sessionMethodName = ZapSessionManagementType.COOKIE_BASED_SESSION_MANAGEMENT.getZapSessionManagementMethod();
        return setSessionManagementMethod(contextId, sessionMethodName, sessionMethodconfigparams);
    }

    /**
     * Create a new user inside the given context.
     *
     * @param contextId Id of the context to use
     * @param username  Name of the user that shall be created
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public int createNewUser(int contextId, String username) throws ClientApiException {
        ApiResponseElement creatUserResponse = ((ApiResponseElement) clientApi.users.newUser(Integer.toString(contextId), username));
        return Integer.parseInt(getIdOfApiResponseElement(creatUserResponse));
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
    public ApiResponse configureAuthenticationCredentials(int contextId, int userId, String authCredentialsConfigParams) throws ClientApiException {
        return clientApi.users.setAuthenticationCredentials(Integer.toString(contextId), Integer.toString(userId), authCredentialsConfigParams);
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
    public ApiResponse setUserEnabled(int contextId, int userId, boolean enabled) throws ClientApiException {
        return clientApi.users.setUserEnabled(Integer.toString(contextId), Integer.toString(userId), Boolean.toString(enabled));
    }

    /**
     * Set the user that will be used in forced user mode for the given context.
     *
     * @param contextId Id of the context to use
     * @param userId    Id of the user to use
     * @return api response of ZAP
     * @throws ClientApiException when anything goes wrong communicating with ZAP
     */
    public ApiResponse setForcedUser(int contextId, int userId) throws ClientApiException {
        return clientApi.forcedUser.setForcedUser(Integer.toString(contextId), Integer.toString(userId));
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

    /**
     * Add a new HTTP session token to ZAP with the given token identifier for the
     * specified URL. The token identifier can be used to access the session later
     * on.
     *
     * @param targetUrl
     * @param sessionTokenIdentifier
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse addHTTPSessionToken(String targetUrl, String sessionTokenIdentifier) throws ClientApiException {
        LOG.info("Add new HTTP session token: {} to ZAP for URL: {}.", sessionTokenIdentifier, targetUrl);
        return clientApi.httpSessions.addSessionToken(targetUrl, sessionTokenIdentifier);
    }

    /**
     * Add a new HTTP session to ZAP with the given identifier for the specified
     * URL. The identifier can be used to access the session later on.
     *
     * @param targetUrl
     * @param sessionIdentifier
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse createEmptyHTTPSession(String targetUrl, String sessionIdentifier) throws ClientApiException {
        LOG.info("Create new empty HTTP session: {} in ZAP for URL: {}.", sessionIdentifier, targetUrl);
        return clientApi.httpSessions.createEmptySession(targetUrl, sessionIdentifier);

    }

    /**
     * Add a new HTTP session token value to ZAP with the given session identifier
     * using the given name and value for the specified URL.
     *
     * @param targetUrl
     * @param sessionIdentifier
     * @param name
     * @param value
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse setHTTPSessionTokenValue(String targetUrl, String sessionIdentifier, String name, String value) throws ClientApiException {
        LOG.info("Adding session token value to ZAP HTTP session: {} for URL: {}", sessionIdentifier, targetUrl);
        return clientApi.httpSessions.setSessionTokenValue(targetUrl, sessionIdentifier, name, value);

    }

    /**
     * Set the session with the given identifier and the given URL as the active
     * session the ZAP shall use.
     *
     * @param targetUrl
     * @param sessionIdentifier
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse setActiveHTTPSession(String targetUrl, String sessionIdentifier) throws ClientApiException {
        LOG.info("Set ZAP HTTP session: {} with URL: {} as active session to use.", sessionIdentifier, targetUrl);
        return clientApi.httpSessions.setActiveSession(targetUrl, sessionIdentifier);
    }

    /**
     * Remove the session with the given identifier and the given URL from ZAP.
     *
     * @param targetUrl
     * @param sessionIdentifier
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse removeHTTPSession(String targetUrl, String sessionIdentifier) throws ClientApiException {
        LOG.info("Remove session: {} for url: {} if it exists.", sessionIdentifier, targetUrl);
        return clientApi.httpSessions.removeSession(targetUrl, sessionIdentifier);
    }

    /**
     * Remove the session token with the given identifier and the given URL from
     * ZAP.
     *
     * @param targetUrl
     * @param sessionTokenIdentifier
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse removeHTTPSessionToken(String targetUrl, String sessionTokenIdentifier) throws ClientApiException {
        LOG.info("Remove session token: {} for url: {} if it exists.", sessionTokenIdentifier, targetUrl);
        return clientApi.httpSessions.removeSessionToken(targetUrl, sessionTokenIdentifier);
    }

    /**
     * Add an exclude for the ajax spider with the given description to the given
     * context to avoid logout during the scan. The html element identifier is
     * always lowercased for ZAP.
     *
     * @param contextName zap context name
     * @param description description of the ajax spider exclude
     * @param logout      logout section of the SecHub webscan config
     * @return
     * @throws ClientApiException
     */
    public ApiResponse addAjaxSpiderAvoidLogoutExclude(String contextName, String description, WebLogoutConfiguration logout) throws ClientApiException {
        String text = null;
        String attributename = null;
        String attributevalue = null;
        String enabled = Boolean.toString(true);
        return clientApi.ajaxSpider.addExcludedElement(contextName, description, logout.getHtmlElement().toLowerCase(), logout.getXpath(), text, attributename,
                attributevalue, enabled);
    }

    /**
     * Pause the spider scan with the given scan ID.
     *
     * @param scanId scan Id
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse pauseSpiderScan(int scanId) throws ClientApiException {
        return clientApi.spider.pause(Integer.toString(scanId));
    }

    /**
     * Resume the spider scan with the given scan ID.
     *
     * @param scanId scan Id
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse resumeSpiderScan(int scanId) throws ClientApiException {
        return clientApi.spider.resume(Integer.toString(scanId));
    }

    /**
     * Pause the active scan with the given scan ID.
     *
     * @param scanId scan Id
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse pauseActiveScan(int scanId) throws ClientApiException {
        return clientApi.ascan.pause(Integer.toString(scanId));
    }

    /**
     * Resume the active scan with the given scan ID.
     *
     * @param scanId scan Id
     * @return api response of ZAP
     * @throws ClientApiException
     */
    public ApiResponse resumeActiveScan(int scanId) throws ClientApiException {
        return clientApi.ascan.resume(Integer.toString(scanId));
    }

    /**
     * Check if the ZAP is still logged in and can access a specific URL
     *
     * @param verification the configuration for the login verification
     * @return <code>true</code> if the ZAP is still logged in and can access the
     *         URL, <code>false</code> otherwise
     */
    public boolean isZapLoggedIn(WebLoginVerificationConfiguration verification) {
        ApiResponseList apiResponse = (ApiResponseList) accessUrlViaZap(verification.getUrl().toString(), false);
        if (apiResponse == null) {
            return false;
        }
        return verifyStatus(apiResponse, verification.getResponseCode());
    }

    private boolean verifyStatus(ApiResponseList apiResponse, int expectedStatusCode) {
        /* Workaround because of ZAP API */
        List<ApiResponse> itemList = apiResponse.getItems();
        if (itemList.isEmpty()) {
            LOG.error("No items in the accessUrlViaZap response list.");
            throw new ZapWrapperRuntimeException("No items in the accessUrlViaZap response list.", ZapWrapperExitCode.INVALID_ZAP_RESPONSE);
        }
        /* API returns a list of items, but there is only one item */
        ApiResponseSet firstItem = (ApiResponseSet) itemList.get(0);
        /*
         * The response header contains exactly one string with all headers, the HTTP is
         * always the first
         */
        String responseHeader = firstItem.getStringValue("responseHeader");
        if (responseHeader == null) {
            LOG.error("No response header in the accessUrlViaZap response.");
            throw new ZapWrapperRuntimeException("No response header in the accessUrlViaZap response.", ZapWrapperExitCode.INVALID_ZAP_RESPONSE);
        }
        // the substring httpStatus looks like this: "HTTP/1.1 200 Ok"
        String httpStatus = responseHeader.substring(0, responseHeader.indexOf("\r\n"));
        /* Check if the httpStatus contains is the expected one */
        return httpStatus.contains(Integer.toString(expectedStatusCode));
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

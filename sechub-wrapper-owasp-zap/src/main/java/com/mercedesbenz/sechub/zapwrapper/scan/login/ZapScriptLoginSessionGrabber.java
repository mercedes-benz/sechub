// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.util.Map;
import java.util.regex.Pattern;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiWrapper;

public class ZapScriptLoginSessionGrabber {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScriptLoginSessionGrabber.class);

    private static final String JWT_REPLACER_DESCRIPTION = "JWT";
    private static final String SESSION_TOKEN_IDENTIFIER = "session-token";
    private static final String SESSION_IDENTIFIER = "authenticated-session";

    private static final String LOCAL_STORAGE = "localStorage";
    private static final String SESSION_STORAGE = "sessionStorage";

    private static final Pattern JWT_PATTERN = Pattern.compile("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$");

    /**
     * The sessionGrabber will add all necessary session data to ZAP.
     *
     * @param firefox
     * @param targetUrl
     * @param clientApiWrapper
     * @return the name/identifier of the authenticated session inside ZAP
     * @throws ClientApiException
     */
    public String extractSessionAndPassToZAP(FirefoxDriver firefox, String targetUrl, ClientApiWrapper clientApiWrapper) throws ClientApiException {
        LOG.info("Removing old session data inside ZAP if necessary.");
        cleanUpOldSessionDataIfNecessary(targetUrl, clientApiWrapper);

        LOG.info("Add new HTTP session token: {} to ZAP.", SESSION_TOKEN_IDENTIFIER);
        clientApiWrapper.addHTTPSessionToken(targetUrl, SESSION_TOKEN_IDENTIFIER);
        LOG.info("Create new empty HTTP session: {} in ZAP.", SESSION_IDENTIFIER);
        clientApiWrapper.createEmptyHTTPSession(targetUrl, SESSION_IDENTIFIER);

        LOG.info("Adding all cookies to ZAP HTTP session: {}", SESSION_IDENTIFIER);
        for (Cookie cookie : firefox.manage().getCookies()) {
            clientApiWrapper.setHTTPSessionTokenValue(targetUrl, SESSION_IDENTIFIER, cookie.getName(), cookie.getValue());
        }
        LOG.info("Set ZAP HTTP session: {} as active session to use.", SESSION_IDENTIFIER);
        clientApiWrapper.setActiveHTTPSession(targetUrl, SESSION_IDENTIFIER);

        addJwtAsReplacerRuleToZap(firefox, clientApiWrapper);

        String followRedirects = "true";
        LOG.info("Accessing target URL: {} via ZAP to make sure it is added to the sites tree.", targetUrl);
        clientApiWrapper.accessUrlViaZap(targetUrl, followRedirects);

        return SESSION_IDENTIFIER;
    }

    /**
     * Looks for old session data and cleans them up, in case anything was left from
     * a previous run.
     *
     * @param targetUrl
     * @param clientApiWrapper
     */
    public void cleanUpOldSessionDataIfNecessary(String targetUrl, ClientApiWrapper clientApiWrapper) {
        try {
            clientApiWrapper.removeHTTPSession(targetUrl, SESSION_IDENTIFIER);
        } catch (ClientApiException e) {
            LOG.info("Could not find old HTTP session, nothing needs to be removed.");
        }
        try {
            clientApiWrapper.removeHTTPSessionToken(targetUrl, SESSION_TOKEN_IDENTIFIER);
        } catch (ClientApiException e) {
            LOG.info("Could not find old HTTP session token, nothing needs to be removed.");
        }
        try {
            clientApiWrapper.removeReplacerRule(JWT_REPLACER_DESCRIPTION);
        } catch (ClientApiException e) {
            LOG.info("Could not find old JWT repalcer rule, nothing needs to be removed.");
        }
    }

    private void addJwtAsReplacerRuleToZap(FirefoxDriver firefox, ClientApiWrapper clientApiWrapper) throws ClientApiException {
        String enabled = "true";
        // "REQ_HEADER" means the header entry will be added to the requests if not
        // existing or replaced if already existing
        String matchtype = "REQ_HEADER";
        String matchregex = "false";

        // matchstring and replacement will be set to the header name and header value
        String matchstring = "Authorization";
        String replacement = null;

        // setting initiators to null means all initiators (ZAP components),
        // this means spider, active scan, etc will send this rule for their requests.
        String initiators = null;
        // default URL is null which means the header would be send on any request to
        // any URL
        String url = null;

        LOG.info("Searching: {} for JWT and add JWT as replacer rule.", LOCAL_STORAGE);
        Map<String, String> localStorage = retrieveStorage(firefox, LOCAL_STORAGE);

        for (String key : localStorage.keySet()) {
            String value = localStorage.get(key);
            if (isJWT(value)) {
                replacement = "Bearer %s".formatted(value);
                clientApiWrapper.addReplacerRule(JWT_REPLACER_DESCRIPTION, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
                return;
            }
        }
        LOG.info("Searching: {} for JWT and add JWT as replacer rule.", SESSION_STORAGE);
        Map<String, String> sessionStorage = retrieveStorage(firefox, SESSION_STORAGE);
        for (String key : sessionStorage.keySet()) {
            String value = sessionStorage.get(key);
            if (isJWT(value)) {
                replacement = "Bearer %s".formatted(value);
                clientApiWrapper.addReplacerRule(JWT_REPLACER_DESCRIPTION, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
                return;
            }
        }
    }

    private Map<String, String> retrieveStorage(JavascriptExecutor jsExecutor, String storageType) {
        String script = """
                let items = {};
                for (let i = 0; i < %s.length; i++) {
                  let key = %s.key(i);
                  items[key] = %s.getItem(key);
                }
                return items;
                """.formatted(storageType, storageType, storageType);

        @SuppressWarnings("unchecked")
        Map<String, String> storage = (Map<String, String>) jsExecutor.executeScript(script);
        return storage;
    }

    private boolean isJWT(String value) {
        if (value == null) {
            return false;
        }
        if (!JWT_PATTERN.matcher(value).matches()) {
            return false;
        }
        String[] split = value.split("\\.");
        return split[0].startsWith("eyJ") && split[1].startsWith("eyJ");
    }

}

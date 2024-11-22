// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.util.Map;

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

    private JWTSupport jwtSupport;

    public ZapScriptLoginSessionGrabber() {
        this(new JWTSupport());
    }

    ZapScriptLoginSessionGrabber(JWTSupport jwtSupport) {
        this.jwtSupport = jwtSupport;
    }

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
        cleanUpOldSessionDataIfNecessary(targetUrl, clientApiWrapper);

        clientApiWrapper.addHTTPSessionToken(targetUrl, SESSION_TOKEN_IDENTIFIER);
        clientApiWrapper.createEmptyHTTPSession(targetUrl, SESSION_IDENTIFIER);

        for (Cookie cookie : firefox.manage().getCookies()) {
            clientApiWrapper.setHTTPSessionTokenValue(targetUrl, SESSION_IDENTIFIER, cookie.getName(), cookie.getValue());
        }
        clientApiWrapper.setActiveHTTPSession(targetUrl, SESSION_IDENTIFIER);

        if (!addJwtAsReplacerRuleToZap(firefox, clientApiWrapper, LOCAL_STORAGE)) {
            addJwtAsReplacerRuleToZap(firefox, clientApiWrapper, SESSION_STORAGE);
        }

        boolean followRedirects = true;
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
    public void cleanUpOldSessionDataIfNecessary(String targetUrl, ClientApiWrapper clientApiWrapper) throws ClientApiException {
        try {
            clientApiWrapper.removeHTTPSession(targetUrl, SESSION_IDENTIFIER);
        } catch (ClientApiException e) {
            if (e.getMessage().equalsIgnoreCase("Connection refused")) {
                throw e;
            }
        }
        try {
            clientApiWrapper.removeHTTPSessionToken(targetUrl, SESSION_TOKEN_IDENTIFIER);
        } catch (ClientApiException e) {
            if (e.getMessage().equalsIgnoreCase("Connection refused")) {
                throw e;
            }
        }
        try {
            clientApiWrapper.removeReplacerRule(JWT_REPLACER_DESCRIPTION);
        } catch (ClientApiException e) {
            if (e.getMessage().equalsIgnoreCase("Connection refused")) {
                throw e;
            }
        }
    }

    private boolean addJwtAsReplacerRuleToZap(FirefoxDriver firefox, ClientApiWrapper clientApiWrapper, String storageType) throws ClientApiException {
        boolean enabled = true;
        // "REQ_HEADER" means the header entry will be added to the requests if not
        // existing or replaced if already existing
        String matchtype = "REQ_HEADER";
        boolean matchregex = false;

        // matchstring and replacement will be set to the header name and header value
        String matchstring = "Authorization";
        String replacement = null;

        // setting initiators to null means all initiators (ZAP components),
        // this means spider, active scan, etc will send this rule for their requests.
        String initiators = null;
        // default URL is null which means the header would be send on any request to
        // any URL
        String url = null;

        LOG.info("Searching: {} for JWT and add JWT as replacer rule.", storageType);
        Map<String, String> localStorage = retrieveStorage(firefox, storageType);

        for (String key : localStorage.keySet()) {
            String value = localStorage.get(key);
            if (jwtSupport.isJWT(value)) {
                replacement = "Bearer %s".formatted(value);
                clientApiWrapper.addReplacerRule(JWT_REPLACER_DESCRIPTION, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
                return true;
            }
        }
        return false;
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
}

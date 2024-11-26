// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.util.Map;

import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApiException;

import com.mercedesbenz.sechub.zapwrapper.internal.scan.ClientApiWrapper;

public class ZapScriptLoginSessionConfigurator {
    private static final Logger LOG = LoggerFactory.getLogger(ZapScriptLoginSessionConfigurator.class);

    private static final String JWT_REPLACER_DESCRIPTION = "JWT";
    private static final String SESSION_TOKEN_IDENTIFIER = "session-token";
    private static final String SESSION_IDENTIFIER = "authenticated-session";

    private JWTSupport jwtSupport;

    public ZapScriptLoginSessionConfigurator() {
        this(new JWTSupport());
    }

    ZapScriptLoginSessionConfigurator(JWTSupport jwtSupport) {
        this.jwtSupport = jwtSupport;
    }

    /**
     * This method will add all necessary session data to ZAP. ZAP can use this
     * authenticated session for scanning.
     *
     * @param loginResult
     * @param targetUrl
     * @param clientApiWrapper
     * @return the name/identifier of the authenticated session inside ZAP
     * @throws ClientApiException
     */
    public String passSessionDataToZAP(ScriptLoginResult loginResult, String targetUrl, ClientApiWrapper clientApiWrapper) throws ClientApiException {
        cleanUpOldSessionDataIfNecessary(targetUrl, clientApiWrapper);

        clientApiWrapper.addHTTPSessionToken(targetUrl, SESSION_TOKEN_IDENTIFIER);
        clientApiWrapper.createEmptyHTTPSession(targetUrl, SESSION_IDENTIFIER);

        for (Cookie cookie : loginResult.getSessionCookies()) {
            clientApiWrapper.setHTTPSessionTokenValue(targetUrl, SESSION_IDENTIFIER, cookie.getName(), cookie.getValue());
        }
        clientApiWrapper.setActiveHTTPSession(targetUrl, SESSION_IDENTIFIER);

        if (!addJwtAsReplacerRuleToZap(loginResult.getSessionStorage(), clientApiWrapper)) {
            addJwtAsReplacerRuleToZap(loginResult.getLocalStorage(), clientApiWrapper);
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

    private boolean addJwtAsReplacerRuleToZap(Map<String, String> storage, ClientApiWrapper clientApiWrapper) throws ClientApiException {
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

        LOG.info("Searching browser storage for JWT and add JWT as replacer rule.");

        for (String key : storage.keySet()) {
            String value = storage.get(key);
            if (jwtSupport.isJWT(value)) {
                replacement = "Bearer %s".formatted(value);
                clientApiWrapper.addReplacerRule(JWT_REPLACER_DESCRIPTION, enabled, matchtype, matchregex, matchstring, replacement, initiators, url);
                return true;
            }
        }
        return false;
    }

}

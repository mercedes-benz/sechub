// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.scan.login;

import java.util.*;

import org.openqa.selenium.Cookie;

public class ScriptLoginResult {

    private Set<Cookie> sessionCookies;
    private Map<String, String> localStorage;
    private Map<String, String> sessionStorage;

    private boolean loginFailed;

    public ScriptLoginResult() {
        this.sessionCookies = new HashSet<>();
        this.localStorage = new HashMap<>();
        this.sessionStorage = new HashMap<>();
    }

    public Set<Cookie> getSessionCookies() {
        return Collections.unmodifiableSet(sessionCookies);
    }

    public void setSessionCookies(Set<Cookie> sessionCookies) {
        if (sessionCookies != null) {
            this.sessionCookies = sessionCookies;
        }
    }

    public Map<String, String> getLocalStorage() {
        return Collections.unmodifiableMap(localStorage);
    }

    public void setLocalStorage(Map<String, String> localStorage) {
        if (localStorage != null) {
            this.localStorage = localStorage;
        }
    }

    public Map<String, String> getSessionStorage() {
        return Collections.unmodifiableMap(sessionStorage);
    }

    public void setSessionStorage(Map<String, String> sessionStorage) {
        if (sessionStorage != null) {
            this.sessionStorage = sessionStorage;
        }
    }

    public boolean isLoginFailed() {
        return loginFailed;
    }

    public void setLoginFailed(boolean loginFailed) {
        this.loginFailed = loginFailed;
    }

}

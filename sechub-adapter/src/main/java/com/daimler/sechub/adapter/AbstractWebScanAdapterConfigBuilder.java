// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.net.URI;
import java.net.URL;
import java.util.Set;

import com.daimler.sechub.commons.model.SecHubTimeUnit;
import com.daimler.sechub.commons.model.login.ActionType;


/**
 * Web scan adapter configuration builder
 *
 * @param <B> Builder
 * @param <C> Configuration
 */
public abstract class AbstractWebScanAdapterConfigBuilder<B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends WebScanAdapterConfig>
    extends AbstractAdapterConfigBuilder<B, C>
{

    private LoginBuilder currentLoginBuilder;
    private SecHubTimeUnitData maxScanDuration;
    private URI targetURI;
    private URI rootTargetURI;
    private Set<String> includes;
    private Set<String> excludes;
   
    protected AbstractWebScanAdapterConfigBuilder() {
        super();
    }
    
    /**
     * A set of includes.
     * 
     * Includes are necessary if a crawler of the web scan cannot find an page.
     * 
     * Each include is a sub-path or path to a page. It needs to start with a slash "/".
     * The includes are combined with the target (base URI) to create a full URI.
     * 
     * <h4>For example<h4>
     * 
     * Target: https://my.example.org:8943<br/>
     * Includes:
     * 
     * <ul>
     *  <li>/admin</li>
     *  <li>/api/hidden</li>
     *  <li>/hidden-login.html</li>
     * </ul>
     * 
     * Combined:
     * 
     * <ul>
     *  <li>https://my.example.org:8943/admin</li>
     *  <li>https://my.example.org:8943/api/hidden</li>
     *  <li>https://my.example.org:8943/hidden-login.html</li>
     * </ul>
     * 
     * @param includes
     * @return
     */
    @SuppressWarnings("unchecked")
    public B setIncludes(Set<String> includes) {
        this.includes = includes;
        return (B) this;
    }
    
    /**
     * A set of excludes.
     * 
     * All excludes will not be scanned.
     * Excludes are necessary if one wants to exclude a page or a part of a web application.
     * 
     * Each exclude is a part of a URI. It needs to start with a slash "/".
     * The excludes are combined with the target (base URI) to create a full URI.
     * 
     * <h4>For example<h4>
     * 
     * Target: https://my.example.org:8943<br/>
     * Excludes:
     * 
     * <ul>
     *  <li>/admin</li>
     *  <li>/api/sensitive</li>
     *  <li>/contaxt.html</li>
     * </ul>
     * 
     * Combined:
     * 
     * <ul>
     *  <li>https://my.example.org:8943/admin</li>
     *  <li>https://my.example.org:8943/api/hidden</li>
     *  <li>https://my.example.org:8943/hidden-login.html</li>
     * </ul>
     * 
     * @param excludes
     * @return
     */
    @SuppressWarnings("unchecked")
    public B setExcludes(Set<String> excludes) {
        this.excludes = excludes;
        return (B) this;
    }
    
    @SuppressWarnings("unchecked")
    public B setMaxScanDuration(SecHubTimeUnitData maxScanDuration) {
        this.maxScanDuration = maxScanDuration;
        return (B) this;
    }
    
    @SuppressWarnings("unchecked")
    public B setTargetURI(URI targetURI) {
        if (targetURI == null) {
            return (B) this;
        }
        this.targetURI = targetURI;
        this.rootTargetURI = uriShrinkSupport.shrinkToRootURI(targetURI);
        return (B) this;
    }

    public class LoginBuilder {

        private AbstractLoginConfig createdLoginConfig;
        private URL loginUrl;

        /**
         * Setup login url
         * 
         * @param url
         * @return builder
         */
        public LoginBuilder url(URL url) {
            this.loginUrl = url;
            return this;

        }

        public BasicLoginBuilder basic() {
            return new BasicLoginBuilder();
        }

        public FormLoginBuilder form() {
            return new FormLoginBuilder();
        }

        public class FormLoginBuilder {
            public FormAutoDetectLoginBuilder autoDetect() {
                return new FormAutoDetectLoginBuilder();
            }

            public FormScriptLoginBuilder script() {
                return new FormScriptLoginBuilder();
            }
        }

        public class FormScriptLoginBuilder {

            private FormScriptLoginConfig formScriptLoginConfig = new FormScriptLoginConfig();

            public FormScriptLoginPageBuilder addPage() {
                return new FormScriptLoginPageBuilder();
            }

            @SuppressWarnings("unchecked")
            public final B endLogin() {
                doEndLogin(formScriptLoginConfig);
                return (B) AbstractWebScanAdapterConfigBuilder.this;
            }

            public class FormScriptLoginPageBuilder {
                private LoginScriptPage page = new LoginScriptPage();

                public FormScriptLoginActionBuilder addAction(ActionType action) {
                    return new FormScriptLoginActionBuilder(action);
                }

                public FormScriptLoginBuilder doEndPage() {
                    formScriptLoginConfig.getPages().add(page);
                    return FormScriptLoginBuilder.this;
                }

                public class FormScriptLoginActionBuilder {

                    private LoginScriptAction action = new LoginScriptAction();

                    public FormScriptLoginActionBuilder(ActionType action) {
                        this.action.actionType = action;
                    }

                    public FormScriptLoginActionBuilder select(String css) {
                        action.selector = css;
                        return this;
                    }

                    public FormScriptLoginActionBuilder enterValue(String value) {
                        action.value = encrypt(value);
                        return this;
                    }

                    public FormScriptLoginActionBuilder description(String description) {
                        action.description = description;
                        return this;
                    }

                    public FormScriptLoginActionBuilder unit(SecHubTimeUnit unit) {
                        action.unit = unit;
                        return this;
                    }

                    public FormScriptLoginPageBuilder endStep() {
                        page.getActions().add(action);
                        return FormScriptLoginPageBuilder.this;
                    }

                }
            }
        }

        private void doEndLogin(AbstractLoginConfig loginConfig) {
            createdLoginConfig = loginConfig;
        }

        public class FormAutoDetectLoginBuilder {

            private FormAutoDetectLoginConfig formAutomatedLoginConfig = new FormAutoDetectLoginConfig();

            public FormAutoDetectLoginBuilder username(String user) {
                formAutomatedLoginConfig.user = encrypt(user);
                return this;
            }

            public FormAutoDetectLoginBuilder password(String password) {
                formAutomatedLoginConfig.password = encrypt(password);
                return this;
            }

            @SuppressWarnings("unchecked")
            public final B endLogin() {
                doEndLogin(formAutomatedLoginConfig);
                return (B) AbstractWebScanAdapterConfigBuilder.this;
            }

        }

        public class BasicLoginBuilder {

            private BasicLoginConfig basicLoginConfig = new BasicLoginConfig();

            public BasicLoginBuilder username(String user) {
                basicLoginConfig.user = encrypt(user);
                return this;
            }

            public BasicLoginBuilder password(String password) {
                basicLoginConfig.password = encrypt(password);
                return this;
            }

            public BasicLoginBuilder realm(String realm) {
                basicLoginConfig.realm = encrypt(realm);
                return this;
            }

            @SuppressWarnings("unchecked")
            public final B endLogin() {
                doEndLogin(basicLoginConfig);
                return (B) AbstractWebScanAdapterConfigBuilder.this;
            }
        }

    }

    /**
     * @return a new login builder when not already started or former login was
     *         ended. Otherwise current login builder is returned
     */
    public LoginBuilder login() {
        currentLoginBuilder = new LoginBuilder();
        return currentLoginBuilder;
    }

    @Override
    void packageInternalCustomBuild(C config) {
        if (! (config instanceof AbstractWebScanAdapterConfig)) {
            throw new IllegalArgumentException("Wrong config type class hierarchy. Your config is of type " + config.getClass().getName() + " is not a descendant of " + AbstractCodeScanAdapterConfig.class.getSimpleName());
        }
        
        AbstractWebScanAdapterConfig abstractWebScanConfig = (AbstractWebScanAdapterConfig) config;
        
        abstractWebScanConfig.maxScanDuration = maxScanDuration;
        abstractWebScanConfig.targetURI = targetURI;
        abstractWebScanConfig.rootTargetURI = rootTargetURI;
        abstractWebScanConfig.includes = includes;
        abstractWebScanConfig.excludes = excludes;

        if (currentLoginBuilder == null) {
            return;
        }
        
        if (currentLoginBuilder.createdLoginConfig == null) {
            return;
        }
        
        abstractWebScanConfig.loginConfig = currentLoginBuilder.createdLoginConfig;
        abstractWebScanConfig.loginConfig.loginUrl = currentLoginBuilder.loginUrl;
    }

}
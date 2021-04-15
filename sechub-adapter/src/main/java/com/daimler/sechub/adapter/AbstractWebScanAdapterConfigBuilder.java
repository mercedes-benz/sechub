// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import java.net.URL;

public abstract class AbstractWebScanAdapterConfigBuilder<B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig>
        extends AbstractAdapterConfigBuilder<B, C> {

    private LoginBuilder currentLoginBuilder;
    private SecHubTimeUnitData maxScanDuration;

    @SuppressWarnings("unchecked")
    public B setMaxScanDuration(SecHubTimeUnitData maxScanDuration) {
        this.maxScanDuration = maxScanDuration;
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
        config.maxScanDuration = maxScanDuration;

        if (currentLoginBuilder == null) {
            return;
        }
        
        if (currentLoginBuilder.createdLoginConfig == null) {
            return;
        }
        
        config.loginConfig = currentLoginBuilder.createdLoginConfig;
        config.loginConfig.loginUrl = currentLoginBuilder.loginUrl;
    }

}
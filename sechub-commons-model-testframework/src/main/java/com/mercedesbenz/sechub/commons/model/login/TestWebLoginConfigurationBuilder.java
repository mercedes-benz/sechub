// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.TestSecHubConfigurationBuilder.TestWebConfigurationBuilder;

public class TestWebLoginConfigurationBuilder {

    private WebLoginConfiguration loginConfig;
    private TestWebConfigurationBuilder mainBuilder;

    public TestWebLoginConfigurationBuilder(String url, TestWebConfigurationBuilder testWebConfigurationBuilder) {
        loginConfig = new WebLoginConfiguration();
        try {
            loginConfig.setUrl(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Testcase corrupt, not a valid url:" + url);
        }
        this.mainBuilder = testWebConfigurationBuilder;
    }

    public TestWebConfigurationBuilder basic(String user, String login) {

        BasicLoginConfiguration basicLogin = new BasicLoginConfiguration();
        basicLogin.setUser(user.toCharArray());
        basicLogin.setPassword(login.toCharArray());
        loginConfig.basic = Optional.of(basicLogin);
        return mainBuilder.login(loginConfig);
    }

    public ScriptPageEntryBuilder formScripted(String user, String login) {
        FormLoginConfiguration formLogin = new FormLoginConfiguration();
        loginConfig.form = Optional.of(formLogin);
        ScriptPageEntryBuilder builder = new ScriptPageEntryBuilder();
        return builder;
    }

    public TestWebLoginConfigurationBuilder totp(String seed, int validityInSeconds, TOTPHashAlgorithm hashAlgorithm, int tokenLength) {
        WebLoginTOTPConfiguration totp = new WebLoginTOTPConfiguration();
        totp.setSeed(seed);
        totp.setValidityInSeconds(validityInSeconds);
        totp.setHashAlgorithm(hashAlgorithm);
        totp.setTokenLength(tokenLength);
        loginConfig.setTotp(totp);

        return this;
    }

    public class ScriptPageEntryBuilder {
        private List<Page> pages = new ArrayList<>();

        private ScriptPageEntryBuilder() {

        }

        public ScriptPageBuilder createPage() {
            return new ScriptPageBuilder();
        }

        public class ScriptPageBuilder {
            private Page page;
            private List<Action> actions = new ArrayList<>();

            private ScriptPageBuilder() {
                page = new Page();
            }

            public ScriptStepBuilder createAction() {
                return new ScriptStepBuilder();
            }

            public class ScriptStepBuilder {
                private Action action;

                private ScriptStepBuilder() {
                    action = new Action();
                }

                public ScriptStepBuilder type(ActionType actionType) {
                    action.type = actionType;
                    return this;
                }

                public ScriptStepBuilder selector(String selector) {
                    action.selector = Optional.ofNullable(selector);
                    return this;
                }

                public ScriptStepBuilder value(String value) {
                    action.value = Optional.ofNullable(value);
                    return this;
                }

                public ScriptStepBuilder description(String description) {
                    action.description = Optional.ofNullable(description);
                    return this;
                }

                /**
                 * The time unit
                 *
                 * E. g. "hour", "millisecond" etc.
                 *
                 * @param unit
                 * @return
                 */
                public ScriptStepBuilder unit(SecHubTimeUnit unit) {
                    action.unit = Optional.ofNullable(unit);
                    return this;
                }

                public ScriptPageBuilder add() {
                    actions.add(action);
                    return ScriptPageBuilder.this;
                }
            }

            public ScriptPageEntryBuilder add() {
                page.actions = Optional.of(actions);
                pages.add(page);
                return ScriptPageEntryBuilder.this;
            }

        }

        public TestWebConfigurationBuilder done() {
            FormLoginConfiguration formLogin = new FormLoginConfiguration();

            Script script = new Script();
            script.pages = Optional.of(pages);
            Optional<Script> optionalScript = Optional.of(script);

            formLogin.script = optionalScript;
            WebLoginConfiguration config = TestWebLoginConfigurationBuilder.this.loginConfig;

            config.form = Optional.of(formLogin);
            return mainBuilder.login(loginConfig);
        }
    }
}

// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

public abstract class AbstractWebScanAdapterConfigBuilder<B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig> extends AbstractAdapterConfigBuilder<B,C> {

	private LoginBuilder loginBuilder;

	public class LoginBuilder{

		private LoginConfig createdLoginConfig;

		public BasicLoginBuilder basic() {
			return new BasicLoginBuilder();
		}

		public FormLoginBuilder form() {
			return new FormLoginBuilder();
		}

		public class FormLoginBuilder{
			public FormAutomatedLoginBuilder automated() {
				return new FormAutomatedLoginBuilder();
			}

			public FormScriptLoginBuilder script() {
				return new FormScriptLoginBuilder();
			}
		}

		public class FormScriptLoginBuilder{

			private FormScriptLoginConfig formScriptLoginConfig = new FormScriptLoginConfig();

			public FormScriptLoginStepBuilder addStep(String type) {
				return new FormScriptLoginStepBuilder(type);
			}

			@SuppressWarnings("unchecked")
			public final B endLogin() {
				createdLoginConfig=formScriptLoginConfig;
				return (B) AbstractWebScanAdapterConfigBuilder.this;
			}

			public class FormScriptLoginStepBuilder{

				private LoginScriptStep step = new LoginScriptStep();

				public FormScriptLoginStepBuilder(String type) {
					step.type=type;
				}

				public FormScriptLoginStepBuilder select(String css) {
					step.selector=css;
					return this;
				}

				public FormScriptLoginStepBuilder enterValue(String value) {
					step.value=encrypt(value);
					return this;
				}

				public FormScriptLoginBuilder endStep() {
					formScriptLoginConfig.getSteps().add(step);
					return FormScriptLoginBuilder.this;
				}

			}
		}

		public class FormAutomatedLoginBuilder{

			private FormAutomatedLoginConfig formAutomatedLoginConfig = new FormAutomatedLoginConfig();

			public FormAutomatedLoginBuilder username(String user) {
				formAutomatedLoginConfig.user=encrypt(user);
				return this;
			}

			public FormAutomatedLoginBuilder password(String password) {
				formAutomatedLoginConfig.password=encrypt(password);
				return this;
			}

			@SuppressWarnings("unchecked")
			public final B endLogin() {
				createdLoginConfig=formAutomatedLoginConfig;
				return (B) AbstractWebScanAdapterConfigBuilder.this;
			}
		}

		public class BasicLoginBuilder{

			private BasicLoginConfig basicLoginConfig = new BasicLoginConfig();

			public BasicLoginBuilder username(String user) {
				basicLoginConfig.user=encrypt(user);
				return this;
			}

			public BasicLoginBuilder password(String password) {
				basicLoginConfig.password=encrypt(password);
				return this;
			}

			public BasicLoginBuilder realm(String realm) {
				basicLoginConfig.realm=encrypt(realm);
				return this;
			}

			@SuppressWarnings("unchecked")
			public final B endLogin() {
				createdLoginConfig=basicLoginConfig;
				return (B) AbstractWebScanAdapterConfigBuilder.this;
			}
		}
	}

	public LoginBuilder login() {
		loginBuilder=new LoginBuilder();
		return loginBuilder;
	}

	@Override
	void packageInternalCustomBuild(C config) {
		if (loginBuilder==null) {
			return;
		}
		if (loginBuilder.createdLoginConfig==null) {
			return;
		}
		config.loginConfig=loginBuilder.createdLoginConfig;
	}

}
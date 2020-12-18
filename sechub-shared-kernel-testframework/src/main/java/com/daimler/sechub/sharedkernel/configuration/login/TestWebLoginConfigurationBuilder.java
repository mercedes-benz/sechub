// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration.login;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.sharedkernel.configuration.TestSecHubConfigurationBuilder.TestWebConfigurationBuilder;

public class TestWebLoginConfigurationBuilder {

	private WebLoginConfiguration loginConfig;
	private TestWebConfigurationBuilder mainBuilder;

	public TestWebLoginConfigurationBuilder(String url, TestWebConfigurationBuilder testWebConfigurationBuilder) {
		loginConfig = new WebLoginConfiguration();
		try {
			loginConfig.setUrl(new URL(url));
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Testcase corrupt, not a valid url:"+url);
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

	public TestWebConfigurationBuilder formAuto(String user, String login) {
		FormLoginConfiguration formLogin = new FormLoginConfiguration();
		loginConfig.form = Optional.of(formLogin);
		AutoDetectUserLoginConfiguration autoDetect = new AutoDetectUserLoginConfiguration();
		autoDetect.setUser(user.toCharArray());
		autoDetect.setPassword(login.toCharArray());
		Optional<AutoDetectUserLoginConfiguration> autoDetectOptoin = Optional.of(autoDetect);

		formLogin.autodetect = autoDetectOptoin;
		return mainBuilder.login(loginConfig);
	}

	public ScriptEntryBuilder formScripted(String user, String login) {
		FormLoginConfiguration formLogin = new FormLoginConfiguration();
		loginConfig.form = Optional.of(formLogin);
		ScriptEntryBuilder builder = new ScriptEntryBuilder();
		return builder;
	}

	public class ScriptEntryBuilder {

		private List<ScriptEntry> entries = new ArrayList<>();

		private ScriptEntryBuilder() {

		}

		public ScriptEntryBuilder step(String type, String selector, String value, String description) {
			ScriptEntry entry = new ScriptEntry();
			entry.action = type;
			entry.selector = Optional.ofNullable(selector);
			entry.value = Optional.ofNullable(value);
			entry.description = Optional.ofNullable(description);

			entries.add(entry);
			return this;
		}

		public TestWebConfigurationBuilder done() {
			FormLoginConfiguration formLogin = new FormLoginConfiguration();
			Optional<List<ScriptEntry>> scriptOption = Optional.of(entries);
			formLogin.script = scriptOption;
			WebLoginConfiguration config = TestWebLoginConfigurationBuilder.this.loginConfig;

			config.form = Optional.of(formLogin);
			return mainBuilder.login(loginConfig);
		}
	}

}

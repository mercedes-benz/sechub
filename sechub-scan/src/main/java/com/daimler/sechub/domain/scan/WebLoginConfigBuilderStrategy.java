package com.daimler.sechub.domain.scan;

import java.util.List;
import java.util.Optional;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;
import com.daimler.sechub.adapter.AdapterConfig;
import com.daimler.sechub.adapter.AdapterConfigurationStrategy;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubWebScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.AutoDetectUserLoginConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.BasicLoginConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.FormLoginConfiguration;
import com.daimler.sechub.sharedkernel.configuration.login.ScriptEntry;
import com.daimler.sechub.sharedkernel.configuration.login.WebLoginConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

/**
 * A common strategy for webscan configuration - usable by every web scan
 * product executor to configure corresponding adapter easily by sechub
 * configuration. <br>
 * <br>
 * Using this strategy will reduce boilerplate code in web scan executors.
 *
 * @author Albert Tregnaghi
 *
 * @param <B> builder
 * @param <C> configuration
 */
public class WebLoginConfigBuilderStrategy
		implements AdapterConfigurationStrategy/*<B, C>*/ {

	private SecHubExecutionContext context;

	public WebLoginConfigBuilderStrategy(SecHubExecutionContext context) {
		this.context=context;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <B extends AbstractAdapterConfigBuilder<B, C>, C extends AdapterConfig> void configure(B configBuilder) {
		if (configBuilder instanceof AbstractWebScanAdapterConfigBuilder) {
			AbstractWebScanAdapterConfigBuilder webConfigBuilder = (AbstractWebScanAdapterConfigBuilder) configBuilder;
			configureX(webConfigBuilder);
		}else {
			throw new IllegalArgumentException("Wrong usage in code: Only accetable for web scan adapters!");
		}
	}

	private <B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig> void configureX(B configBuilder) {
		SecHubConfiguration configuration = context.getConfiguration();
		if (configuration==null) {
			return;
		}
		Optional<SecHubWebScanConfiguration> webScan = configuration.getWebScan();
		if (! webScan.isPresent()) {
			return;
		}

		configureLogin(webScan.get(), configBuilder);

	}

	private <B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig> void configureLogin(SecHubWebScanConfiguration secHubWebScanConfiguration, B configBuilder) {
		if (!secHubWebScanConfiguration.getLogin().isPresent()) {
			return;
		}
		WebLoginConfiguration webLoginConfiguration = secHubWebScanConfiguration.getLogin().get();
		configureWebLoginBasic(webLoginConfiguration, configBuilder);
		configureWebLoginForm(webLoginConfiguration, configBuilder);

	}

	private <B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig>  void configureWebLoginBasic(WebLoginConfiguration webLoginConfiguration, B configBuilder) {
		Optional<BasicLoginConfiguration> basic = webLoginConfiguration.getBasic();
		if (!basic.isPresent()) {
			return;
		}
		BasicLoginConfiguration config = basic.get();
		/* @formatter:off */
		configBuilder.
			login().
				basic().
					username(new String(config.getUser())).
					password(new String(config.getPassword())).
					realm(config.getRealm().orElse(null)).
			endLogin();
		/* @formatter:on */

	}

	private <B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig>  void configureWebLoginForm(WebLoginConfiguration webLoginConfiguration, B configBuilder) {
		Optional<FormLoginConfiguration> formLogin = webLoginConfiguration.getForm();
		if (!formLogin.isPresent()) {
			return;
		}
		FormLoginConfiguration config = formLogin.get();

		configureWebLoginFormAutoDetect(config, configBuilder);
		configureWebLoginFormScript(config, configBuilder);

	}

	private <B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig>  void configureWebLoginFormScript(FormLoginConfiguration config, B configBuilder) {
		if (!config.getScript().isPresent()) {
			return;
		}
		List<ScriptEntry> scriptList = config.getScript().get();
		AbstractWebScanAdapterConfigBuilder<B, C>.LoginBuilder.FormScriptLoginBuilder scriptBuilder = configBuilder.login().form().script();

		for (ScriptEntry entry : scriptList) {
			/* @formatter:off */
			scriptBuilder.
				addStep(entry.getStep()).
					select(entry.getSelector().orElse(null)).
					enterValue(entry.getValue().orElse(null)).
				endStep();
			/* @formatter:on */

		}
		scriptBuilder.endLogin();
	}

	private  <B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig> void configureWebLoginFormAutoDetect(FormLoginConfiguration config, B configBuilder) {
		if (!config.getAutodetect().isPresent()) {
			return;
		}
		AutoDetectUserLoginConfiguration autoDetect = config.getAutodetect().get();
		/* @formatter:off */
		configBuilder.
			login().
				form().autoDetect().
					username(new String(autoDetect.getUser())).
					password(new String(autoDetect.getPassword())).endLogin();
		/* @formatter:on */
	}





}

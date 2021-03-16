// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfig;
import com.daimler.sechub.adapter.AbstractWebScanAdapterConfigBuilder;
import com.daimler.sechub.adapter.AdapterConfig;
import com.daimler.sechub.adapter.AdapterConfigurationStrategy;
import com.daimler.sechub.adapter.SecHubTimeUnit;
import com.daimler.sechub.adapter.SecHubTimeUnitData;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubWebScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.WebScanDurationConfiguration;
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
public class WebConfigBuilderStrategy implements AdapterConfigurationStrategy/* <B, C> */ {

    private SecHubExecutionContext context;

    public WebConfigBuilderStrategy(SecHubExecutionContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <B extends AbstractAdapterConfigBuilder<B, C>, C extends AdapterConfig> void configure(B configBuilder) {
        if (configBuilder instanceof AbstractWebScanAdapterConfigBuilder) {
            AbstractWebScanAdapterConfigBuilder webConfigBuilder = (AbstractWebScanAdapterConfigBuilder) configBuilder;
            coinfigureImpl(webConfigBuilder);
        } else {
            throw new IllegalArgumentException("Wrong usage in code: Only accetable for web scan adapters!");
        }
    }

    private <B extends AbstractWebScanAdapterConfigBuilder<B, C>, C extends AbstractWebScanAdapterConfig> void coinfigureImpl(B configBuilder) {
        /* check precondition : login configured */
        SecHubConfiguration configuration = context.getConfiguration();
        if (configuration == null) {
            return;
        }
        Optional<SecHubWebScanConfiguration> webScan = configuration.getWebScan();
        if (!webScan.isPresent()) {
            return;
        }
        SecHubWebScanConfiguration webscanConfig = webScan.get();

        handleMaxScanDuration(configBuilder, webscanConfig);

        /* ----------------------- LOGIN ----------------------- */

        Optional<WebLoginConfiguration> loginOpt = webscanConfig.getLogin();
        if (!loginOpt.isPresent()) {
            return;
        }

        /* handle different web login configurations: */
        WebLoginConfiguration loginConfiguration = loginOpt.get();
        URL loginUrl = loginConfiguration.getUrl();

        /* ------ BASIC --------- */
        Optional<BasicLoginConfiguration> basic = loginConfiguration.getBasic();
        if (basic.isPresent()) {
            configureBasicAuth(configBuilder, loginUrl, basic.get());
            return;
        }

        /* ------ FORM --------- */
        Optional<FormLoginConfiguration> formLogin = loginConfiguration.getForm();
        if (!formLogin.isPresent()) {
            return;
        }
        FormLoginConfiguration formLoginConfig = formLogin.get();

        /* ------ FORM:AUTODETECT --------- */
        if (formLoginConfig.getAutodetect().isPresent()) {
            configureAutodetectAuth(configBuilder, loginUrl, formLoginConfig.getAutodetect().get());
            return;
        }
        /* ------ FORM:SCRIPT--------- */
        if (formLoginConfig.getScript().isPresent()) {
            configureScriptAuth(configBuilder, loginUrl, formLoginConfig.getScript().get());
        }

    }

    private <B extends AbstractWebScanAdapterConfigBuilder<B, ?>> void handleMaxScanDuration(B configBuilder, SecHubWebScanConfiguration webscanConfig) {
        Optional<WebScanDurationConfiguration> optMaxScanDuration = webscanConfig.getMaxScanDuration();
        if (!optMaxScanDuration.isPresent()) {
            return;
        }
        
        int duration = optMaxScanDuration.get().getDuration();
        SecHubTimeUnit unit = optMaxScanDuration.get().getUnit();
        SecHubTimeUnitData maxScanDuration = SecHubTimeUnitData.of(duration, unit);

        configBuilder.setMaxScanDuration(maxScanDuration);
    }

    /* ------------------------ */
    /* +---- BASIC -----------+ */
    /* ------------------------ */
    private <B extends AbstractWebScanAdapterConfigBuilder<B, ?>> void configureBasicAuth(B configBuilder, URL loginUrl, BasicLoginConfiguration config) {
        /* @formatter:off */
		configBuilder.
		login().
			url(loginUrl).
				basic().
					username(new String(config.getUser())).
					password(new String(config.getPassword())).
					realm(config.getRealm().orElse(null)).
		endLogin();
		/* @formatter:on */
    }

    /* ------------------------ */
    /* +---- FORM:AUTODETECT -+ */
    /* ------------------------ */
    private <B extends AbstractWebScanAdapterConfigBuilder<B, ?>> void configureAutodetectAuth(B configBuilder, URL loginUrl,
            AutoDetectUserLoginConfiguration autoDetect) {
        /* @formatter:off */
		configBuilder.
			login().
				url(loginUrl).
				form().
					autoDetect().
						username(new String(autoDetect.getUser())).
						password(new String(autoDetect.getPassword())).
			endLogin();
		/* @formatter:on*/
    }

    /* ------------------------ */
    /* +---- FORM:SCRIPT -----+ */
    /* ------------------------ */
    private <C extends AbstractWebScanAdapterConfig, B extends AbstractWebScanAdapterConfigBuilder<B, C>> void configureScriptAuth(B configBuilder,
            URL loginUrl, List<ScriptEntry> scriptList) {
        AbstractWebScanAdapterConfigBuilder<B, C>.LoginBuilder.FormScriptLoginBuilder scriptBuilder = configBuilder.login().url(loginUrl).form().script();

        for (ScriptEntry entry : scriptList) {
            /* @formatter:off */
			scriptBuilder.
				addStep(entry.getAction()).
					select(entry.getSelector().orElse(null)).
					enterValue(entry.getValue().orElse(null)).
					description(entry.getDescription().orElse(null)).
					unit(entry.getUnit().orElse(null)).
				endStep();
			/* @formatter:on */

        }
        scriptBuilder.endLogin();
    }

}

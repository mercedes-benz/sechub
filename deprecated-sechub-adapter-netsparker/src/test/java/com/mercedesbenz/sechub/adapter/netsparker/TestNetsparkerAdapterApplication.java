// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.netsparker;

import static com.mercedesbenz.sechub.test.TestUtil.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.SecHubTimeUnitData;
import com.mercedesbenz.sechub.adapter.netsparker.NetsparkerConfig.NetsparkerConfigBuilder;
import com.mercedesbenz.sechub.commons.model.SecHubTimeUnit;
import com.mercedesbenz.sechub.commons.model.login.ActionType;

/**
 * A simple java application to test netsparker adapter
 *
 * @author Albert Tregnaghi
 *
 */
public class TestNetsparkerAdapterApplication {

    public static void main(String[] args) throws Exception {
        SecHubTimeUnitData maxScanDuration = null;

        String maxScanDurationDurationProperty = getSystemProperty("sechub.adapter.netsparker.maxscanduration.duration");
        String maxScanDurationUnitProperty = getSystemProperty("sechub.adapter.netsparker.maxscanduration.unit");

        if (maxScanDurationDurationProperty != null && maxScanDurationUnitProperty != null) {
            int duration = Integer.valueOf(maxScanDurationDurationProperty);
            SecHubTimeUnit unit = SecHubTimeUnit.valueOf(maxScanDurationUnitProperty);

            maxScanDuration = SecHubTimeUnitData.of(duration, unit);
        }

        /* @formatter:off */
		NetsparkerConfigBuilder builder = NetsparkerConfig.builder().
				setUser(getSystemProperty("sechub.adapter.netsparker.user")).
				setTrustAllCertificates(getSystemPropertyBooleanOrFalse("sechub.adapter.netsparker.trustall")).
				setAgentGroupName(getSystemProperty("sechub.adapter.netsparker.user.agent.groupname")).
				setPasswordOrAPIToken(getSystemProperty("sechub.adapter.netsparker.apitoken")).
				setPolicyID(getSystemProperty("sechub.adapter.netsparker.policyid")).
				setProductBaseUrl(getSystemProperty("sechub.adapter.netsparker.baseurl")).
				setLicenseID(getSystemProperty("sechub.adapter.netsparker.licenseid","none")).
				setTargetURI(new URI(getSystemProperty("sechub.adapter.netsparker.targeturi"))).
				setMaxScanDuration(maxScanDuration);

		/* @formatter:on */
        String loginType = getSystemProperty("sechub.adapter.netsparker.login.type", "<none>");
        if ("basic".equalsIgnoreCase(loginType)) {
            handleBasicLogin(builder);
        } else if ("formAutodetect".equalsIgnoreCase(loginType)) {
            handleFormAutodetect(builder);
        } else if ("formScript".equalsIgnoreCase(loginType)) {
            handleFormScript(builder);
        } else if ("<none>".equalsIgnoreCase(loginType)) {
            /* ignore */
        } else {
            throw new IllegalArgumentException("login type:" + loginType + " not supported!");
        }
        NetsparkerAdapterConfig config = builder.build();
        NetsparkerAdapter netsparker = new NetsparkerAdapterV1();
        AdapterExecutionResult adapterResult = netsparker.start(config, new AdapterMetaDataCallback() {

            AdapterMetaData metaData;

            @Override
            public void persist(AdapterMetaData metaData) {
                System.out.println("persist:" + metaData);
                this.metaData = metaData;
            }

            @Override
            public AdapterMetaData getMetaDataOrNull() {
                return metaData;
            }
        });

        System.out.println("result:");
        System.out.println(adapterResult.getProductResult());

    }

    private static void handleFormAutodetect(NetsparkerConfigBuilder builder) throws MalformedURLException {
        builder.login().url(new URL(getSystemProperty("sechub.adapter.netsparker.login.url"))).form().autoDetect()
                .username(getSystemProperty("sechub.adapter.netsparker.login.user")).password(getSystemProperty("sechub.adapter.netsparker.login.password"))
                .endLogin();
    }

    private static void handleFormScript(NetsparkerConfigBuilder builder) throws MalformedURLException {
        /* @formatter:off */
		builder.login().
				url(new URL(getSystemProperty("sechub.adapter.netsparker.login.url"))).
				form().
					script().
					    addPage().
    						addAction(ActionType.USERNAME).
    							select(getSystemProperty("sechub.adapter.netsparker.login.script.page1.action1.input.selector","#username")).
    							enterValue(getSystemProperty("sechub.adapter.netsparker.login.user")).
    					    endStep().
    					    addAction(ActionType.PASSWORD).
    							select(getSystemProperty("sechub.adapter.netsparker.login.script.page1.action2.input.selector","#password")).
    							enterValue(getSystemProperty("sechub.adapter.netsparker.login.password")).
    						endStep().
    						addAction(ActionType.CLICK).
    							select(getSystemProperty("sechub.adapter.netsparker.login.script.page1.action3.click.selector","#doLogin")).
    						endStep().
    					doEndPage().
				endLogin();
		/* @formatter:on */
    }

    private static void handleBasicLogin(NetsparkerConfigBuilder builder) throws MalformedURLException {
        builder.login().url(new URL(getSystemProperty("sechub.adapter.netsparker.login.url"))).basic()
                .username(getSystemProperty("sechub.adapter.netsparker.login.user")).password(getSystemProperty("sechub.adapter.netsparker.login.password"))
                .endLogin();

    }

}

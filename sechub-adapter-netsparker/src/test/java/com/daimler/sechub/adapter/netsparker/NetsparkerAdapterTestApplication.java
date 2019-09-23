package com.daimler.sechub.adapter.netsparker;

import static com.daimler.sechub.test.TestUtil.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.daimler.sechub.adapter.netsparker.NetsparkerConfig.NetsparkerConfigBuilder;

/**
 * A simple java application to test netsparker adapter
 *
 * @author Albert Tregnaghi
 *
 */
public class NetsparkerAdapterTestApplication {

	public static void main(String[] args) throws Exception {
		/* @formatter:off */
		NetsparkerConfigBuilder builder = NetsparkerConfig.builder().
				setUser(getSystemProperty("sechub.adapter.netsparker.user")).
				setTrustAllCertificates(getSystemPropertyBooleanOrFalse("sechub.adapter.netsparker.trustall")).
				setAgentGroupName(getSystemProperty("sechub.adapter.netsparker.user.agent.groupname")).
				setPasswordOrAPIToken(getSystemProperty("sechub.adapter.netsparker.apitoken")).
				setPolicyID(getSystemProperty("sechub.adapter.netsparker.policyid")).
				setProductBaseUrl(getSystemProperty("sechub.adapter.netsparker.baseurl")).
				setLicenseID(getSystemProperty("sechub.adapter.netsparker.licenseid","none")).
				setTargetURI(new URI(getSystemProperty("sechub.adapter.netsparker.targeturi")));

		/* @formatter:on */
		String loginType = getSystemProperty("sechub.adapter.netsparker.login.type","<none>");
		if ("basic".equalsIgnoreCase(loginType)){
			handleBasicLogin(builder);
		}else if ("formAutodetect".equalsIgnoreCase(loginType))	{
			handleFormAutodetect(builder);
		}
		NetsparkerAdapterConfig config = builder.build();
		NetsparkerAdapter netsparker = new NetsparkerAdapterV1();
		String result = netsparker.start(config);

		System.out.println("result:");
		System.out.println(result);

	}

	private static void handleFormAutodetect(NetsparkerConfigBuilder builder) throws MalformedURLException {
		builder.login().url(new URL(getSystemProperty("sechub.adapter.netsparker.login.url"))).form().autoDetect().username(getSystemProperty("sechub.adapter.netsparker.login.user"))
				.password(getSystemProperty("sechub.adapter.netsparker.login.password")).endLogin();
	}

	private static void handleBasicLogin(NetsparkerConfigBuilder builder) throws MalformedURLException{
		builder.login().url(new URL(getSystemProperty("sechub.adapter.netsparker.login.url"))).basic().username(getSystemProperty("sechub.adapter.netsparker.login.user"))
				.password(getSystemProperty("sechub.adapter.netsparker.login.password")).endLogin();

	}

}

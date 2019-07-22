// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxOAuthSupport.CheckmarxOAuthData;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;




public class CheckmarxOAuthSupportTest {
	private CheckmarxOAuthSupport supportToTest;

	@Before
	public void before() throws Exception {
		supportToTest = new CheckmarxOAuthSupport();
	}

	@Test
	public void test_data_can_be_extracted() throws AdapterException {
		/* prepare */
		String data = "{\"access_token\":\"12345MeUdnk6O_-EEp93I1e8rsdlHvBg\",\"expires_in\":86400,\"token_type\":\"Bearer\"}{Cache-Control=[no-store, no-cache, max-age=0, private], Pragma=[no-cache], Content-Length=[1786], Content-Type=[application/json; charset=utf-8], Server=[Microsoft-IIS/8.5], X-AspNet-Version=[4.0.30319], X-Powered-By=[ASP.NET], Date=[Tue, 25 Sep 2018 13:29:26 GMT]}";
				
		/* execute */
		CheckmarxOAuthData result = supportToTest.extractFromJson(JSONAdapterSupport.FOR_UNKNOWN_ADAPTER,data);
		
		/* test */
		assertNotNull(result);
		assertEquals("Bearer",result.getTokenType());
		assertEquals("12345MeUdnk6O_-EEp93I1e8rsdlHvBg",result.getAccessToken());
		assertEquals(86400,result.getExpiresIn());
	}

}

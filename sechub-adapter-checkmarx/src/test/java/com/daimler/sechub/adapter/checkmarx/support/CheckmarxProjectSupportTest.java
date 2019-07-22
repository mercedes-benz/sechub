// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx.support;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.checkmarx.CheckmarxSessionData;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;




public class CheckmarxProjectSupportTest {
	private CheckmarxProjectSupport supportToTest;

	@Before
	public void before() throws Exception {
		supportToTest = new CheckmarxProjectSupport();
	}

	@Test
	public void test_data_can_be_extracted() throws AdapterException {
		/* prepare */
		String data = "[{\r\n" + 
				"    \"id\": 1234,\r\n" + 
				"    \"teamId\": \"xxxx-aax7-45asdf-b194-c736b605700d\",\r\n" + 
				"    \"name\": \"TheName\",\r\n" + 
				"    \"isPublic\": true,\r\n" + 
				"    \"customFields\": [],\r\n" + 
				"    \"links\": [\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"self\",\r\n" + 
				"        \"uri\": \"/projects/113\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"teams\",\r\n" + 
				"        \"uri\": \"/auth/teams/\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"latestscan\",\r\n" + 
				"        \"uri\": \"/sast/scans?projectId=12213&last=1\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"allscans\",\r\n" + 
				"        \"uri\": \"/sast/scans?projectId=12213\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"rel\": \"scansettings\",\r\n" + 
				"        \"uri\": \"/sast/scanSettings/1234\"\r\n" + 
				"      },\r\n" + 
				"      {\r\n" + 
				"        \"type\": \"local\",\r\n" + 
				"        \"rel\": \"source\",\r\n" + 
				"        \"uri\": null\r\n" + 
				"      }\r\n" + 
				"    ]\r\n" + 
				"  }\r\n" + 
				"]{Cache-Control=[no-cache], Pragma=[no-cache], Content-Type=[application/json; charset=utf-8], Expires=[-1], Server=[Microsoft-IIS/8.5], api-version=[2.0], X-AspNet-Version=[4.0.30319], X-Powered-By=[ASP.NET], Date=[Tue, 25 Sep 2018 14:06:42 GMT], Content-Length=[725]}";
		
		/* execute */
		CheckmarxSessionData result = supportToTest.extractFirstProjectFromJsonWithProjectArray(JSONAdapterSupport.FOR_UNKNOWN_ADAPTER,data);
		
		/* test */
		assertNotNull(result);
		assertEquals(1234L,result.getProjectId());
		assertEquals("TheName",result.getProjectName());
	}

}

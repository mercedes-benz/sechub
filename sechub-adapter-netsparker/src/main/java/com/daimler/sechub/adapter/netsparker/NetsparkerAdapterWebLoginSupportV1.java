// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.netsparker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.daimler.sechub.adapter.FormAutoDetectLoginConfig;
import com.daimler.sechub.adapter.FormScriptLoginConfig;
import com.daimler.sechub.adapter.LoginConfig;
import com.daimler.sechub.adapter.WebScanAdapterConfig;

public class NetsparkerAdapterWebLoginSupportV1 {

	public void addAuthorizationInfo(WebScanAdapterConfig config, Map<String,Object> rootMap) {
		Objects.nonNull(config);

		LoginConfig loginConfig = config.getLoginConfig();
		if (loginConfig==null) {
			return;
		}
		if (loginConfig.isBasic()) {
			addBasicAuthorization(loginConfig,rootMap);
			return;
		}
		if (loginConfig.isFormAutoDetect()) {
			addFormAutodetectAuthorization(loginConfig,rootMap);
			return;
		}
		if (loginConfig.isFormScript()) {
			addFormScriptAuthorization(loginConfig,rootMap);
			return;
		}
		throw new IllegalArgumentException("Is currently not supported:"+config.getClass().getSimpleName());
	}

	// see https://your-netsparker-server/docs/index#!/Scans/Scans_New
	private void addFormScriptAuthorization(LoginConfig config, Map<String, Object> rootMap) {
//		"FormAuthenticationSettingModel": {
//	    "CustomScripts": [
//		    "Value" : "....your script ...."
//		],
//	    "DefaultPersonaValidation": true,
//	    "DetectBearerToken": true,
//	    "DisableLogoutDetection": false,
//	    "IsEnabled": true,
//	    "LoginFormUrl": "http://example.com/login.php",
//	    "LoginRequiredUrl": "http://example.com/admin.php",
//	    "LogoutKeywordPatterns": [
//	      {
//	        "Pattern": "Signin required",
//	        "Regex": true
//	      }
//	    ],
//	    "LogoutKeywordPatternsValue": "[{\"Pattern\":\"Signin required\",\"Regex\":true}]",
//	    "LogoutRedirectPattern": "http://example.com/Default.php?ref=*",
//	    "OverrideTargetUrl": false,
//	    "Personas": [
//	      {
//	        "IsActive": true,
//	        "Password": "pass",
//	        "UserName": "user"
//	      }
//	    ],
//	    "PersonasValidation": true
//	  },

		NetsparkerLoginScriptGenerator scriptGenerator = new NetsparkerLoginScriptGenerator();
		FormScriptLoginConfig asFormScript = config.asFormScript();
		String script = scriptGenerator.generate(asFormScript.getSteps());

		Map<String, Object> formAuthenticationSettingModel = new TreeMap<>();
		rootMap.put("FormAuthenticationSettingModel", formAuthenticationSettingModel);

		formAuthenticationSettingModel.put("LoginFormUrl", asFormScript.getLoginURL());
		List<Map<String,Object>> customScripts = new ArrayList<Map<String,Object>>();
		Map<String, Object> customScript = new TreeMap<>();
		customScript.put("Value", script);
		customScripts.add(customScript);
		formAuthenticationSettingModel.put("DefaultPersonaValidation", true);
		formAuthenticationSettingModel.put("CustomScripts", customScripts);
		formAuthenticationSettingModel.put("IsEnabled", true);
		formAuthenticationSettingModel.put("PersonasValidation", true);



		List<Map<String,Object>> personas = new ArrayList<Map<String,Object>>();
		Map<String, Object> entry = new TreeMap<>();
		entry.put("UserName", asFormScript.getUserName());
		entry.put("Password", asFormScript.getPassword());
		entry.put("IsActive", true);
		personas.add(entry);
		formAuthenticationSettingModel.put("Personas", personas);



	}

	// see https://your-netsparker-server/docs/index#!/Scans/Scans_New
	private void addFormAutodetectAuthorization(LoginConfig config, Map<String, Object> rootMap) {
//		"FormAuthenticationSettingModel": {
//	    "CustomScripts": [],
//	    "DefaultPersonaValidation": true,
//	    "DetectBearerToken": true,
//	    "DisableLogoutDetection": false,
//	    "IsEnabled": true,
//	    "LoginFormUrl": "http://example.com/login.php",
//	    "LoginRequiredUrl": "http://example.com/admin.php",
//	    "LogoutKeywordPatterns": [
//	      {
//	        "Pattern": "Signin required",
//	        "Regex": true
//	      }
//	    ],
//	    "LogoutKeywordPatternsValue": "[{\"Pattern\":\"Signin required\",\"Regex\":true}]",
//	    "LogoutRedirectPattern": "http://example.com/Default.php?ref=*",
//	    "OverrideTargetUrl": false,
//	    "Personas": [
//	      {
//	        "IsActive": true,
//	        "Password": "pass",
//	        "UserName": "user"
//	      }
//	    ],
//	    "PersonasValidation": true
//	  },
		Map<String, Object> formAuthenticationSettingModel = new TreeMap<>();
		FormAutoDetectLoginConfig asFormAutoDetect = config.asFormAutoDetect();

		rootMap.put("FormAuthenticationSettingModel", formAuthenticationSettingModel);


		List<Map<String,Object>> personas = new ArrayList<Map<String,Object>>();
		Map<String, Object> entry = new TreeMap<>();
		entry.put("UserName", asFormAutoDetect.getUser());
		entry.put("Password", asFormAutoDetect.getPassword());
		entry.put("IsActive", true);
		personas.add(entry);

		formAuthenticationSettingModel.put("DisableLogoutDetection", true);
		formAuthenticationSettingModel.put("LoginFormUrl", asFormAutoDetect.getLoginURL());
		formAuthenticationSettingModel.put("Personas", personas);
		formAuthenticationSettingModel.put("PersonasValidation", true);
		formAuthenticationSettingModel.put("IsEnabled", true);
	}

	// see https://your-netsparker-server/docs/index#!/Scans/Scans_New
	private void addBasicAuthorization(LoginConfig config, Map<String,Object> rootMap) {
//
//		 "BasicAuthenticationApiModel": {
//	    "Credentials": [
//	      {
//	        "AuthenticationType": "Basic",
//	        "Domain": "example.com",
//	        "Password": "pass",
//	        "UriPrefix": "http://example.com/",
//	        "UserName": "user"
//	      }
//	    ],
//	    "IsEnabled": true,
//	    "NoChallenge": false
//	  },

		Map<String, Object> basicAuthenticationApiModel = new TreeMap<>();
		rootMap.put("BasicAuthenticationApiModel", basicAuthenticationApiModel);

		List<Map<String, Object>>credentialList = new ArrayList<>();
		basicAuthenticationApiModel.put("Credentials", credentialList);

		Map<String, Object> credentialEntry1 = new TreeMap<>();
		credentialEntry1.put("AuthenticationType", "Basic");
		String realm = config.asBasic().getRealm();
		if (realm!=null) {
			credentialEntry1.put("Domain", realm);
		}
		credentialEntry1.put("UserName", config.asBasic().getUser());
		credentialEntry1.put("Password", config.asBasic().getPassword());
		credentialEntry1.put("UriPrefix", config.asBasic().getLoginURL());

		credentialList.add(credentialEntry1);

		basicAuthenticationApiModel.put("IsEnabled", true);
		basicAuthenticationApiModel.put("NoChallenge", false);

	}
}

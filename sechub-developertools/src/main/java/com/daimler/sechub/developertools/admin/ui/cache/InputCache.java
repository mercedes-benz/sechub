// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.cache;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class InputCache {
    
    public static InputCache DEFAULT = new InputCache();
	
	private Map<InputCacheIdentifier,String> cache = new EnumMap<>(InputCacheIdentifier.class);
	
	private InputCache() {
        createDefaults();
    }

    private void createDefaults() {
        /* @formatter:off */
	    set(InputCacheIdentifier.PDS_PORT, "8444");
	    set(InputCacheIdentifier.PDS_SERVER, "localhost");
        
	    set(InputCacheIdentifier.PDS_USER, "pds-inttest-admin");
	    set(InputCacheIdentifier.PDS_APITOKEN, "pds-inttest-apitoken");
	    set(InputCacheIdentifier.PDS_JOB_PARAMS, "product1.qualititycheck.enabled=false;product1.level=1");
	    set(InputCacheIdentifier.PDS_SECHUB_JOBUUID, UUID.randomUUID().toString());
	    set(InputCacheIdentifier.PDS_PRODUCT_ID, "PDS_INTTEST_PRODUCT_CODESCAN");
        
	    set(InputCacheIdentifier.EMAILADRESS, "sechub@example.org");
	    set(InputCacheIdentifier.PROJECT_MOCK_CONFIG_JSON,
                "{ \n" + "  \"apiVersion\" : \"1.0\",\n" + "\n" + "   \"codeScan\" : {\n" + "         \"result\" : \"yellow\"   \n" + "   },\n"
                        + "   \"webScan\" : {\n" + "         \"result\" : \"green\"   \n" + "   },\n" + "   \"infraScan\" : {\n"
                        + "         \"result\" : \"red\"   \n" + "   }\n" + " \n" + "}");
	    set(InputCacheIdentifier.MARK_PROJECT_FALSE_POSITIVE, "{\n" + 
                "    \"apiVersion\": \"1.0\", \n" + 
                "    \"type\" : \"falsePositiveJobDataList\", \n" + 
                "    \n" + 
                "    \"jobData\": [\n" + 
                "            {\n" + 
                "                \"jobUUID\": \"$JobUUID\",\n" + 
                "                \"findingId\": 42, \n" + 
                "                \"comment\" : \"Can be ignored, because:\" \n" + 
                "            },\n" + 
                "            {\n" + 
                "                \"jobUUID\": \"$JobUUID\",\n" + 
                "                \"findingId\": 4711\n" + 
                "            }\n" + 
                "      ]\n" + 
                "                \n" + 
                "}");
        /* @formatter:on */
    }

	public String get(InputCacheIdentifier identifier) {
		if (identifier==null) {
			return "";
		}
		return cache.get(identifier);
	}

	public void set(InputCacheIdentifier identifier, String value) {
		cache.put(identifier,value);
	}
}

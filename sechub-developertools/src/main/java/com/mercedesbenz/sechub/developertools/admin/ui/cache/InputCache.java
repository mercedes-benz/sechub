// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.cache;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.ui.ConfigurationSetup;

public class InputCache {

    private static final Logger LOG = LoggerFactory.getLogger(InputCache.class);

    public static InputCache DEFAULT = new InputCache();

    private Map<InputCacheIdentifier, String> cache = new EnumMap<>(InputCacheIdentifier.class);

    private InputCache() {
        createDefaults();
    }

    private void createDefaults() {
        /* @formatter:off */
        String targetFolder = ConfigurationSetup.getParentFolderPathForSecHubClientScanOrNull();
        if (targetFolder!=null) {
            set(InputCacheIdentifier.CLIENT_SCAN_TARGETFOLDER, targetFolder);
        }else {
            try {
                set(InputCacheIdentifier.CLIENT_SCAN_TARGETFOLDER, Paths.get("./..").toRealPath().toString());
            } catch (IOException e) {
                LOG.error("Was not able to set default scan target folder",e);
            }
        }
        set(InputCacheIdentifier.PAGE,"0");
        set(InputCacheIdentifier.PAGE_SIZE,"1");
        set(InputCacheIdentifier.PDS_PORT, "8444");
	    set(InputCacheIdentifier.PDS_HOSTNAME, "localhost");

	    set(InputCacheIdentifier.PDS_USER, "pds-inttest-admin");
	    set(InputCacheIdentifier.PDS_APITOKEN, "pds-inttest-apitoken");
	    set(InputCacheIdentifier.PDS_JOB_PARAMS, "product1.qualititycheck.enabled=false;product1.level=1");
	    set(InputCacheIdentifier.PDS_SECHUB_JOBUUID, UUID.randomUUID().toString());
	    set(InputCacheIdentifier.PDS_PRODUCT_ID, "PDS_INTTEST_PRODUCT_CODESCAN");

	    set(InputCacheIdentifier.EMAILADDRESS, "sechub@example.org");
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
        if (identifier == null) {
            return "";
        }
        return cache.get(identifier);
    }

    public void set(InputCacheIdentifier identifier, String value) {
        cache.put(identifier, value);
    }
}

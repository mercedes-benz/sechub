// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupResult.AutoCleanupResultKey;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;

/**
 * This default implementation does only log the result
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile("!" + PDSProfiles.INTEGRATIONTEST)
public class DefaultPDSAutoCleanupResultInspector implements PDSAutoCleanupResultInspector {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPDSAutoCleanupResultInspector.class);

    @Override
    public void inspect(PDSAutoCleanupResult data) {
        /* @formatter:off */
        AutoCleanupResultKey key = data.getKey();
        LOG.info("Auto cleanup, variant '{}' deleted {} entries older than {} days. Used timestamp '{}' inside:{}",
                key.getVariant(),
                data.getDeletedEntries(),
                data.getCleanupTimeInDays(),
                data.getUsedCleanupTimeStamp(),
                key.getInspectedClass().getSimpleName());
        /* @formatter:on */
    }

}

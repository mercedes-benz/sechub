// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.autocleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult.AutoCleanupResultKey;

/**
 * This default implementation does only log the result
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile("!" + Profiles.INTEGRATIONTEST)
public class DefaultAutoCleanupResultInspector implements AutoCleanupResultInspector {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAutoCleanupResultInspector.class);

    @Override
    public void inspect(AutoCleanupResult data) {
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

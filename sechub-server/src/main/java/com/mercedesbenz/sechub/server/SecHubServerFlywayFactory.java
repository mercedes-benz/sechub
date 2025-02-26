// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.RepairOutput;
import org.flywaydb.core.api.output.RepairResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Configuration
@Profile("!" + Profiles.TEST) // spring profile "test" has no Flyway enabled
public class SecHubServerFlywayFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubServerFlywayFactory.class);

    @Value("${sechub.migration.flyway.autorepair:true}")
    @MustBeDocumented(value = "When enabled, flyway migration problems will be automatically repaired", scope = DocumentationScopeConstants.SCOPE_MIGRATION)
    boolean repairAutomatically;

    @Bean
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        if (repairAutomatically) {
            LOG.info("Start flyway repair");

            RepairResult result = flyway.repair();

            logRepairs("align migration", result.migrationsAligned);
            logRepairs("delete migration", result.migrationsDeleted);
            logRepairs("remove migration", result.migrationsRemoved);

            LOG.info("Flyway repair done for flyway version:{}", result.flywayVersion);

        }
        return new FlywayMigrationInitializer(flyway);
    }

    private void logRepairs(String message, List<RepairOutput> outputList) {
        for (RepairOutput output : outputList) {
            logRepair(message, output);
        }

    }

    private void logRepair(String repairAction, RepairOutput output) {
        LOG.info("Flyway repair did {}: version={},description={},", repairAction, output.version, output.description);
    }

}
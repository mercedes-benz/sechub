// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

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

import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;

@Configuration
@Profile("!" + PDSProfiles.TEST) // spring profile "test" has no Flyway enabled
public class PDSFlywayFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PDSFlywayFactory.class);

    @Value("${pds.migration.flyway.autorepair:true}")
    @PDSMustBeDocumented(value = "When enabled, flyway migration problems will be automatically repaired", scope = "migration")
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
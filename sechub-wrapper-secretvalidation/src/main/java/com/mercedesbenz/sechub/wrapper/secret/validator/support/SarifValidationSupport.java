// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.jcup.sarif_2_1_0.model.Location;
import de.jcup.sarif_2_1_0.model.PhysicalLocation;
import de.jcup.sarif_2_1_0.model.Region;
import de.jcup.sarif_2_1_0.model.Result;

@Component
public class SarifValidationSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SarifValidationSupport.class);

    public boolean findingCanBeValidated(Result finding) {
        if (finding == null) {
            LOG.info("Finding entry in SARIF report is null. Finding validation will be skipped.");
            return false;
        }
        List<Location> locations = finding.getLocations();
        if (locations == null || locations.isEmpty()) {
            LOG.info("Finding locations list in sarif report are null or empty. Finding validation will be skipped.");
            return false;
        }
        return true;
    }

    public boolean findingLocationCanBeValidated(Location location) {
        if (location == null) {
            LOG.info("Finding location entry in SARIF report is null. Finding validation will be skipped.");
            return false;
        }
        PhysicalLocation physicalLocation = location.getPhysicalLocation();
        if (physicalLocation == null) {
            LOG.info("Finding physical location entry in SARIF report is null. Finding validation will be skipped.");
            return false;
        }
        Region findingRegion = physicalLocation.getRegion();
        if (findingRegion == null) {
            LOG.info("Finding physical location region entry in SARIF report is null. Finding validation will be skipped.");
            return false;
        }
        return true;
    }
}

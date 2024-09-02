// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.notNull;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoClassification;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

@Component
public class SerecoProjectDataWebScanFalsePositiveSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SerecoProjectDataWebScanFalsePositiveSupport.class);

    public boolean areBothHavingSameCweIdOrBothNoCweId(WebscanFalsePositiveProjectData webScanData, SerecoVulnerability vulnerability) {
        notNull(vulnerability, " vulnerability may not be null");
        notNull(webScanData, " webscanProjectData may not be null");

        Integer cweIdOrNull = webScanData.getCweId();

        SerecoClassification serecoClassification = vulnerability.getClassification();
        String serecoCWE = serecoClassification.getCwe();
        if (serecoCWE == null || serecoCWE.isEmpty()) {
            if (cweIdOrNull == null) {
                /*
                 * when not set in meta data and also not in vulnerability, than we assume it is
                 * the same
                 */
                return true;
            }
            return false;
        }
        if (cweIdOrNull == null) {
            return false;
        }
        try {
            int serecoCWEint = Integer.parseInt(serecoCWE);
            if (cweIdOrNull.intValue() != serecoCWEint) {
                /* not same type of common vulnerability enumeration - so skip */
                return false;
            }

        } catch (NumberFormatException e) {
            LOG.error("Sereco vulnerability type:{} found CWE:{} but not expected integer format!", vulnerability.getType(), serecoCWE);
            return false;

        }
        return true;
    }

    /**
     * Iterates the given list of hostPatterns and uses each string value as key to
     * get the corresponding compiled pattern from the given map. Then tries to
     * match the given host against each pattern of the projectDataPatternMap.
     *
     * @param targetUrl
     * @param projectDataPatternMap
     * @return
     */
    public boolean isMatchingUrlPattern(String targetUrl, Map<String, Pattern> projectDataPatternMap) {
        notNull(targetUrl, " host may not be null");
        notNull(projectDataPatternMap, " projectDataPatternMap may not be null");

        for (String id : projectDataPatternMap.keySet()) {
            Pattern pattern = projectDataPatternMap.get(id);
            if (pattern == null) {
                // At this point this should never happen because the map is meant to be created
                // by the associated projectData
                throw new IllegalStateException("Project data wildcard pattern for id: %s was not part of the pattern map.".formatted(id));
            }
            if (pattern.matcher(targetUrl).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given method is inside the list of methods.
     *
     * @param method
     * @param methods
     * @return <code>true</code> if the given method is inside methods or methods is
     *         empty or <code>null</code>
     */
    public boolean isMatchingMethodOrIgnoreIfNotSet(String method, List<String> methods) {
        notNull(method, " method may not be null");
        return listContainsTrimmedStringIgnoreCase(method.trim(), methods);
    }

    private boolean listContainsTrimmedStringIgnoreCase(String trimmedString, List<String> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        for (String projectDataPort : list) {
            String projectDataPortTrimmed = projectDataPort.trim();
            if (projectDataPortTrimmed.equalsIgnoreCase(trimmedString)) {
                return true;
            }
        }
        return false;
    }
}

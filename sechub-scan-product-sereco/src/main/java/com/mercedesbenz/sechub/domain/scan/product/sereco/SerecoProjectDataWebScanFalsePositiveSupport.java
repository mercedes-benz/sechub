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
     * @param host
     * @param hostPatterns
     * @param projectDataPatternMap
     * @return <code>true</code> if the given host matches any of the given patterns
     */
    public boolean isMatchingHostPattern(String host, List<String> hostPatterns, Map<String, Pattern> projectDataPatternMap) {
        notNull(host, " host may not be null");
        notNull(hostPatterns, " hostPatterns may not be null");
        notNull(projectDataPatternMap, " projectDataPatternMap may not be null");

        return isMatchingAnyPattern(host, hostPatterns, projectDataPatternMap);
    }

    /**
     * Iterates the given list of urlPathPatterns and uses each string value as key
     * to get the corresponding compiled pattern from the given map. Then tries to
     * match the given host against each pattern of the projectDataPatternMap.
     *
     * @param urlFilePart
     * @param urlPathPatterns
     * @param projectDataPatternMap
     * @return <code>true</code> if the given urlFilePart matches any of the given
     *         patterns
     */
    public boolean isMatchingUrlPathPattern(String urlFilePart, List<String> urlPathPatterns, Map<String, Pattern> projectDataPatternMap) {
        notNull(urlFilePart, " urlFilePart may not be null");
        notNull(urlPathPatterns, " urlPathPatterns may not be null");
        notNull(projectDataPatternMap, " projectDataPatternMap may not be null");

        return isMatchingAnyPattern(urlFilePart, urlPathPatterns, projectDataPatternMap);
    }

    /**
     * Checks if the given port is inside the list of ports.
     *
     * @param port
     * @param ports
     * @return <code>true</code> if the given port is inside ports or ports is empty
     *         or <code>null</code>
     */
    public boolean isMatchingPortOrIngoreIfNotSet(String port, List<String> ports) {
        notNull(port, " port may not be null");
        return listContainsTrimmedStringIgnoreCase(port.trim(), ports);
    }

    /**
     * Checks if the given protocol is inside the list of protocols.
     *
     * @param protocol
     * @param protocols
     * @return <code>true</code> if the given protocol is inside protocols or
     *         protocols is empty or <code>null</code>
     */
    public boolean isMatchingProtocolOrIngoreIfNotSet(String protocol, List<String> protocols) {
        notNull(protocol, " protocol may not be null");
        return listContainsTrimmedStringIgnoreCase(protocol.trim(), protocols);
    }

    /**
     * Checks if the given method is inside the list of methods.
     *
     * @param method
     * @param methods
     * @return <code>true</code> if the given method is inside methods or methods is
     *         empty or <code>null</code>
     */
    public boolean isMatchingMethodOrIngoreIfNotSet(String method, List<String> methods) {
        notNull(method, " method may not be null");
        return listContainsTrimmedStringIgnoreCase(method.trim(), methods);
    }

    private boolean isMatchingAnyPattern(String stringToMatch, List<String> wildcardPatternList, Map<String, Pattern> projectDataPatternMap) {
        for (String patternEntryKey : wildcardPatternList) {
            Pattern pattern = projectDataPatternMap.get(patternEntryKey);
            if (pattern == null) {
                // At this point this should never happen because the map is meant to be created
                // by the associated projectData
                throw new IllegalStateException("Project data wildcard pattern: " + patternEntryKey + " was not part of the pattern map.");
            }
            if (pattern.matcher(stringToMatch).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean listContainsTrimmedStringIgnoreCase(String trimmedString, List<String> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        for (String projectDataPort : list) {
            String projectDataPortTrimmmed = projectDataPort.trim();
            if (projectDataPortTrimmmed.equalsIgnoreCase(trimmedString)) {
                return true;
            }
        }
        return false;
    }
}

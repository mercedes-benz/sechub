// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;

@Component
public class TemplateTypeScanConfigIdResolver {

    private static Map<TemplateType, ScanProjectConfigID> map = new LinkedHashMap<>();
    private static final Set<String> unmodifiableSet;

    static {
        register();

        Set<String> set = new LinkedHashSet<>();
        for (ScanProjectConfigID configId : map.values()) {
            set.add(configId.getId());
        }

        unmodifiableSet = Collections.unmodifiableSet(set);
    }

    private static void register() {
        map.put(TemplateType.WEBSCAN_LOGIN, ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN);
    }

    /**
     * Resolves configuration identifier for given template type
     *
     * @param type
     * @return configuration identifier, never <code>null</code>
     * @throws IllegalStateException if a template type would exist which could not
     *                               be mapped to a configuration identifier
     */
    public ScanProjectConfigID resolve(TemplateType type) {

        ScanProjectConfigID result = map.get(type);
        if (result == null) {
            throw new IllegalStateException("Type : " + type + " is not supported!");
        }
        return result;

    }

    /**
     * Resolve all possible configuration ids for templates
     *
     * @return set containing all possible configuration ids (for template types),
     *         never <code>null</code>
     */
    public Set<String> resolveAllPossibleConfigIds() {
        return unmodifiableSet;
    }
}

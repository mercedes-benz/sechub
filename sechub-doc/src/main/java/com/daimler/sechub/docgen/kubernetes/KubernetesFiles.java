// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.kubernetes;

import static com.daimler.sechub.docgen.util.DocGeneratorUtil.*;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.daimler.sechub.docgen.DocAnnotationData;

public class KubernetesFiles {
    String serverDeploymentYaml = "";
    Map<String, StringBuilder> deploymentMap = new TreeMap<>();
    Map<String, Set<String>> secretInfoMap = new TreeMap<>();
    Map<String, String> secretFileNameMapping = new TreeMap<>();

    StringBuilder getDeployment(DocAnnotationData data) {
        StringBuilder sb = deploymentMap.get(data.scope);
        if (sb == null) {
            sb = new StringBuilder();
            newLine(sb, "# ----------------------------------------------------------------------------------------------#");
            newLine(sb, "# - Scope: " + data.scope);
            newLine(sb, "# --------------------------------------------------------------------------------------------- #");
            deploymentMap.put(data.scope, sb);
        }
        return sb;
    }

    public Set<String> getSecretNames() {
        return secretInfoMap.keySet();
    }

    public Set<String> getSecretKeys(String secretName) {
        Set<String> set = secretInfoMap.get(secretName);
        if (set != null) {
            return set;
        }
        set = new TreeSet<>();
        secretInfoMap.put(secretName, set);
        return set;
    }

    /**
     * Resolves filename for secret key content
     *
     * @param key
     * @return filename for given key. If non special defined , it will always be
     *         "${key}.txt"
     */
    public String getFileNameForSecretKey(String key) {
        String secretFileName = secretFileNameMapping.get(key);
        if (secretFileName != null) {
            return secretFileName;
        }
        return key + ".txt";
    }
}
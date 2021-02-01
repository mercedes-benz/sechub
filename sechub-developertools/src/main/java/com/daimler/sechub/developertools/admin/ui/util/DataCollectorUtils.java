// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.util;

import com.daimler.sechub.developertools.admin.DeveloperAdministration;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileList;
import com.daimler.sechub.test.executionprofile.TestExecutionProfileListEntry;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class DataCollectorUtils {
    
    public static String fetchProfileInformationAboutProject(String profileId,UIContext uiContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Profiles:\n");
        DeveloperAdministration administration = uiContext.getAdministration();
        TestExecutionProfileList list = administration.fetchExecutionProfileList();
        for (TestExecutionProfileListEntry entry: list.executionProfiles) {
            TestExecutionProfile profile = administration.fetchExecutionProfile(entry.id);
            if (profile.projectIds.contains(profileId)) {
                sb.append("- ");
                sb.append(profile.id);
                if (profile.enabled) {
                    sb.append("(enabled)");
                }else {
                    sb.append("(disabled)");
                }
                sb.append("\n  with executor configurations:");
                /* @formatter:off */
                for (TestExecutorConfig config: profile.configurations) {
                    sb.append("\n   *").
                        append(config.name).
                        
                        append(", executor:").append(config.productIdentifier).append(" V").append(config.executorVersion).
                        append(", enabled:").append(config.enabled).
                        append(", uuid=").append(config.uuid);
                }
                /* @formatter:on */
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

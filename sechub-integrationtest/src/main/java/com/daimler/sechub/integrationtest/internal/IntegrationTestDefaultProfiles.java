package com.daimler.sechub.integrationtest.internal;

public class IntegrationTestDefaultProfiles {
    public static final DoNotChangeTestExecutionProfile PROFILE_1 = defineProfile1();

    private static final DoNotChangeTestExecutionProfile[] ALL_PROFILES = new DoNotChangeTestExecutionProfile[] { PROFILE_1 };

    public static DoNotChangeTestExecutionProfile[] getAllDefaultProfiles() {
        return ALL_PROFILES;
    }

    private static DoNotChangeTestExecutionProfile defineProfile1() {

        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.CHECKMARX_V1);
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.NETSPARKER_V1);
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.NESSUS_V1);
        profile.id = "inttest-default-profile1";
        profile.enabled = true;
        return profile;
    }

}

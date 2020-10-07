package com.daimler.sechub.integrationtest.internal;

public class IntegrationTestDefaultProfiles {
    public static final DoNotChangeTestExecutionProfile PROFILE_1 = defineProfile1();

    private static DoNotChangeTestExecutionProfile defineProfile1() {
        
        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.configurations.add(IntegrationTestDefaultExecutorConfigurations.CHECKMARX_V1);
        profile.configurations.add(IntegrationTestDefaultExecutorConfigurations.NETSPARKER_V1);
        profile.configurations.add(IntegrationTestDefaultExecutorConfigurations.NESSUS_V1);
        profile.id="inttest-default-profile1";
        return profile;
    }
    
}

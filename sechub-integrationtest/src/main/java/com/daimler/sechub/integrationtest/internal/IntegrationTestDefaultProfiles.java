// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

public class IntegrationTestDefaultProfiles {
    public static final DoNotChangeTestExecutionProfile PROFILE_1 = defineProfile1();
    public static final DoNotChangeTestExecutionProfile PROFILE_2_PDS_CODESCAN = defineProfile2();
    public static final DoNotChangeTestExecutionProfile PROFILE_3_PDS_CODESCAN_SARIF = defineProfile3();
    
    public static final DoNotChangeTestExecutionProfile PROFILE_4_PDS_CODESCAN_SARIF_NO_SECHUB_STORAGE_USED = defineProfile4();

    private static final DoNotChangeTestExecutionProfile[] ALL_PROFILES = new DoNotChangeTestExecutionProfile[] {

            PROFILE_1,

            PROFILE_2_PDS_CODESCAN,
            
            PROFILE_3_PDS_CODESCAN_SARIF,
            
            PROFILE_4_PDS_CODESCAN_SARIF_NO_SECHUB_STORAGE_USED
            
    };

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

    private static DoNotChangeTestExecutionProfile defineProfile2() {

        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_A);
        profile.id = "inttest-default-profile2-pds";
        profile.enabled = true;
        return profile;
    }
    
    private static DoNotChangeTestExecutionProfile defineProfile3() {
        
        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_D);
        profile.id = "inttest-default-profile3-sarif"; // not more than 30 chars per profile id, so we use this
        profile.enabled = true;
        return profile;
    }
    
    
    private static DoNotChangeTestExecutionProfile defineProfile4() {
        
        DoNotChangeTestExecutionProfile profile = new DoNotChangeTestExecutionProfile();
        profile.initialConfigurationsWithoutUUID.add(IntegrationTestDefaultExecutorConfigurations.PDS_V1_CODE_SCAN_E);
        profile.id = "inttest-default-profile4-sarif"; // not more than 30 chars per profile id, so we use this
        profile.description="Same as profile 3, but executor config does not reuse sechub storage!";
        profile.enabled = true;
        return profile;
    }
    

}

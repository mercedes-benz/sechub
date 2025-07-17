// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.amazonaws.regions.Regions;
import com.amazonaws.util.EC2MetadataUtils;

class AwsRegionResolverTest {

    @ParameterizedTest
    @ValueSource(strings = { "us-west-1", "'eu-central-1'", "mars-north-99" })
    void any_name_is_resolved_as_is(String input) {
        assertThat(AwsRegionResolver.resolve(input)).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(strings = { "default", "DEFAULT" })
    void default_is_resolved_to_regions_default_region(String value) {
        assertThat(AwsRegionResolver.resolve(value)).isEqualTo(Regions.DEFAULT_REGION.getName());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void not_existing_values_result_in_null(String value) {
        assertThat(AwsRegionResolver.resolve(value)).isNull();
    }

    @Test
    void current_is_resolved_by_ec2_metadata_utils_getec2instanceregion() {
        /* prepare */
        String regionFromAWSMetaDataUtils = "e2-instance-region-example";

        String result = null;

        try (MockedStatic<EC2MetadataUtils> theMock = Mockito.mockStatic(EC2MetadataUtils.class)) {
            theMock.when(EC2MetadataUtils::getEC2InstanceRegion).thenReturn(regionFromAWSMetaDataUtils);
            /* execute */
            result = AwsRegionResolver.resolve("current");
        }

        /* test */
        assertThat(result).isEqualTo(regionFromAWSMetaDataUtils);
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;

class TemplateTypeScanConfigIdResolverTest {

    private TemplateTypeScanConfigIdResolver resolverToTest;

    @BeforeEach
    void beforeEach() {
        resolverToTest = new TemplateTypeScanConfigIdResolver();
    }

    @ParameterizedTest
    @EnumSource(value = TemplateType.class)
    void every_template_type_can_be_resolved(TemplateType type) {

        /* execute */
        ScanProjectConfigID result = resolverToTest.resolve(type);

        /* test */
        assertThat(result).isNotNull();
    }

    void resolve_all_possible_works() {
        /* execute */
        Set<String> configIdsResolved = resolverToTest.resolveAllPossibleConfigIds();

        /* test */
        assertThat(configIdsResolved).contains(ScanProjectConfigID.TEMPLATE_WEBSCAN_LOGIN.getId());
        assertThat(configIdsResolved).doesNotContain(ScanProjectConfigID.FALSE_POSITIVE_CONFIGURATION.getId());
        assertThat(configIdsResolved).doesNotContain(ScanProjectConfigID.MOCK_CONFIGURATION.getId());
        assertThat(configIdsResolved).doesNotContain(ScanProjectConfigID.PROJECT_ACCESS_LEVEL.getId());

        assertThat(configIdsResolved).describedAs("check that all template types are resolved").hasSize(TemplateType.values().length);
    }

}

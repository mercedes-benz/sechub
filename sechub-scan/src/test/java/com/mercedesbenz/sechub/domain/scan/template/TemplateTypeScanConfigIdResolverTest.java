package com.mercedesbenz.sechub.domain.scan.template;

import static org.assertj.core.api.Assertions.*;

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

}

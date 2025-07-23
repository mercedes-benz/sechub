package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;

class SerecoSeverityTest {

    @ParameterizedTest
    @EnumSource(Severity.class)
    void sereco_severity_can_import_all_commons_sechub_severities(Severity severity) {
        /* execute */
        SerecoSeverity result = SerecoSeverity.fromString(severity.name());
        
        /* test */
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(severity.name());
        
    }
    

}

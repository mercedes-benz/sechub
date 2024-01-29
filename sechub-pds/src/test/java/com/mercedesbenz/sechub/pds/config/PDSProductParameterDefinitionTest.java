// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;

class PDSProductParameterDefinitionTest {

    @Test
    void noDefaultSet_has_no_default() {
        /* prepare */
        PDSProductParameterDefinition def = new PDSProductParameterDefinition();
        def.setDefault(null);

        /* test */
        assertFalse(def.hasDefault());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "test", "4711" })
    void default_value_defined__has_default_returns_true(String value) {
        /* prepare */
        PDSProductParameterDefinition def = new PDSProductParameterDefinition();
        def.setDefault(value);

        /* test */
        assertTrue(def.hasDefault());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "test", "4711" })
    @NullSource
    void serialize_deserialize_default_set_as_expected(String value) {
        /* prepare */
        PDSProductParameterDefinition def = new PDSProductParameterDefinition();
        def.setDefault(value);

        String json = JSONConverter.get().toJSON(def);
        PDSProductParameterDefinition def2 = JSONConverter.get().fromJSON(PDSProductParameterDefinition.class, json);

        /* test */
        assertEquals(value, def2.getDefault(), "Defaut value not as expected");
    }

}

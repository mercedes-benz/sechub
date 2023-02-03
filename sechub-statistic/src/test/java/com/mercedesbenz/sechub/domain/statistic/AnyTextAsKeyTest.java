package com.mercedesbenz.sechub.domain.statistic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AnyTextAsKeyTest {

    @Test
    void reference_text_is_null() {
        assertNull(AnyTextAsKey.ANY_TEXT.getKeyValue());
    }

    @Test
    void null_is_not_accepted_as_constructor_parameter() {
        assertThrows(IllegalArgumentException.class, () -> new AnyTextAsKey(null));
    }

    @EmptySource
    @ParameterizedTest
    @ValueSource(strings = { "a", " ", "-", "abcdef", "key1.key2", " " })
    void construction_allowed_with_text_and_has_it_as_keyvalue(String text) {
        /* execute */
        AnyTextAsKey key = new AnyTextAsKey(text);

        /* test */
        assertEquals(text, key.getKeyValue());
    }

}

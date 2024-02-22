package com.mercedesbenz.sechub.commons.core.prepare;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PrepareResultTest {

    @ParameterizedTest
    @ValueSource(strings = { "SECHUB_PREPARE_RESULT;status=ok", "SECHUB_PREPARE_RESULT;  status = ok", "SECHUB_PREPARE_RESULT; status= ok" })
    void fromString_correct_formats_preparation_done(String text) {
        /* execute */
        PrepareResult result = PrepareResult.fromString(text);

        /* test */
        assertNotNull(result);
        assertEquals(PrepareStatus.OK, result.getStatus());
        assertTrue(result.isPreparationDone());
    }

    @ParameterizedTest
    @ValueSource(strings = { "SECHUB_PREPARE_RESULT;status=failed", "SECHUB_PREPARE_RESULT; status= failed" })
    void fromString_correct_formats_preparation_not_done(String text) {
        /* execute */
        PrepareResult result = PrepareResult.fromString(text);

        /* test */
        assertNotNull(result);
        assertEquals(PrepareStatus.FAILED, result.getStatus());
        assertFalse(result.isPreparationDone());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { ".", "SECHUB_PREPARE_OTHER", "      ", "SECHUB_PREPARE_RESULT,status=ok", "SECHUB_PREPARE_RESULT;status:ok" })
    void fromString_not_acceptable_format_has_result_where_preparation_was_not_succesful(String text) {
        /* execute */
        PrepareResult result = PrepareResult.fromString(text);

        /* test */
        assertNotNull(result);
        assertNull(result.getStatus());
        assertFalse(result.isPreparationDone());
    }

    @Test
    void toString_can_be_parsed_back() {
        assertEquals(PrepareStatus.OK, toStringAndBackAsStatus(PrepareStatus.OK));
        assertEquals(PrepareStatus.FAILED, toStringAndBackAsStatus(PrepareStatus.FAILED));
        assertEquals(null, toStringAndBackAsStatus(null));
    }

    private PrepareStatus toStringAndBackAsStatus(PrepareStatus status) {
        return PrepareResult.fromString(new PrepareResult(status).toString()).getStatus();
    }

}

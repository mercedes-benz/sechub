// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

class SerecoAnnotationTest {

    SerecoAnnotation annotationToTest;

    @BeforeEach
    void beforeEach() {
        annotationToTest = new SerecoAnnotation();
    }

    @ParameterizedTest
    @EnumSource(SecHubMessageType.class)
    void fromSecHubMessage(SecHubMessageType type) {

        /* prepare */
        String text = UUID.randomUUID().toString();

        /* execute */
        SerecoAnnotation result = SerecoAnnotation.fromSecHubMessage(new SecHubMessage(type, text));

        /* test */
        SerecoAnnotationType resultType = result.getType();
        if (resultType == null) {
            failBecauseWrongTypeResolved(resultType, type);
        }

        assertNotNull(resultType);

        switch (resultType) {
        case INTERNAL_ERROR_PRODUCT_FAILED:
            failBecauseWrongTypeResolved(resultType, type);
        case USER_ERROR:
            assertEquals(SecHubMessageType.ERROR, type);
            break;
        case USER_INFO:
            assertEquals(SecHubMessageType.INFO, type);
            break;
        case USER_WARNING:
            assertEquals(SecHubMessageType.WARNING, type);
            break;
        default:
            failBecauseWrongTypeResolved(resultType, type);
        }
        assertEquals(text, result.getValue());

    }

    @Test
    void fromSecHubMessage_sechub_message_type_and_message_null() {
        /* execute */
        SerecoAnnotation annotation = SerecoAnnotation.fromSecHubMessage(new SecHubMessage(null, null));

        /* test */
        assertEquals(SerecoAnnotationType.USER_INFO, annotation.getType());
        assertEquals(null, annotation.getValue());
    }

    private void failBecauseWrongTypeResolved(SerecoAnnotationType resultType, SecHubMessageType type) {
        fail("may not happen! result type is '" + resultType + "' for sechub message type '" + type + "'");
    }

}

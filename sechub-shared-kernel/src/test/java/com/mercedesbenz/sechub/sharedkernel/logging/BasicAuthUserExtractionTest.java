package com.mercedesbenz.sechub.sharedkernel.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.test.TestCanaryException;

class BasicAuthUserExtractionTest {

    private BasicAuthUserExtraction extractionToTest;

    @BeforeEach
    void beforeEach() {
        extractionToTest = new BasicAuthUserExtraction();
    }

    @Test
    void extractUserFromAuthHeader_null_extracted_to_info_that_not_defined() {
        assertEquals("info.no-basic-auth-defined", extractionToTest.extractUserFromAuthHeader(null));
    }

    @Test
    void extractUserFromAuthHeader_no_colon_set() {
        /* prepare */
        String data = Base64.getEncoder().encodeToString("somethingElse".getBytes());
        String authHeader = "Basic " + data;

        /* execute + test */
        assertEquals("error.colon-count-wrong:0", extractionToTest.extractUserFromAuthHeader(authHeader));
    }

    @Test
    void extractUserFromAuthHeader_three_colon_set() {
        /* prepare */
        String data = Base64.getEncoder().encodeToString("a:x:b:c".getBytes());
        String authHeader = "Basic " + data;

        /* execute + test */
        assertEquals("error.colon-count-wrong:3", extractionToTest.extractUserFromAuthHeader(authHeader));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Basic", "BASIC", "basic", "bASic" })
    void extractUserFromAuthHeader_prefix_handled_case_insensitive(String prefix) {
        /* prepare */
        String data = Base64.getEncoder().encodeToString("username1:password".getBytes());
        String authHeader = prefix + " " + data;

        /* execute + test */
        assertEquals("username1", extractionToTest.extractUserFromAuthHeader(authHeader));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "a", "adam42", "äpfel" })
    void extractUserFromAuthHeader_usernames_extracted(String username) {
        /* prepare */
        String data = Base64.getEncoder().encodeToString((username + ":password").getBytes());
        String authHeader = "Basic " + data;

        /* execute + test */
        assertEquals(username, extractionToTest.extractUserFromAuthHeader(authHeader));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Basic X", "Basic ", "Basic" })
    void extractUserFromAuthHeader_too_small_basic_auth(String authHeader) {

        /* execute + test */
        assertEquals("error.too-small", extractionToTest.extractUserFromAuthHeader(authHeader));
    }

    @ParameterizedTest
    @ValueSource(strings = { "X", "" })
    void extractUserFromAuthHeader_illegal_basic_auth_data_handled_as_error(String authHeader) {

        /* execute + test */
        assertEquals("error.unsupported-format", extractionToTest.extractUserFromAuthHeader(authHeader));
    }

    @Test
    void runtime_exception_at_base64_decoding_is_handled() {
        /* prepare */
        BasicAuthUserExtraction spiedExtractionToTest = spy(BasicAuthUserExtraction.class);
        when(spiedExtractionToTest.decodeUserFromBase64(anyString())).thenThrow(TestCanaryException.class);

        /* test */
        assertEquals("error.extraction-failed-with-exception", spiedExtractionToTest.extractUserFromAuthHeader("Basic aWx"));

    }

}

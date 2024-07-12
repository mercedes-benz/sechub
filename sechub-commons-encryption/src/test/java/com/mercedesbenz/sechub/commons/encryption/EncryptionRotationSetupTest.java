// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Rotation setup test will test also the builder because the setup cannot be
 * created without the builder and the builder has logic inside on build time
 * which must be tested as well.
 *
 * @author Albert Tregnaghi
 *
 */
class EncryptionRotationSetupTest {

    private PersistentCipher cipher1;
    private InitializationVector initialVector1;

    private PersistentCipher cipher2;
    private InitializationVector initialVector2;

    @BeforeEach
    void beforeEach() {
        cipher1 = mock(PersistentCipher.class);
        cipher2 = mock(PersistentCipher.class);

        initialVector1 = mock(InitializationVector.class);
        initialVector2 = mock(InitializationVector.class);
    }

    @Test
    void setup_build_without_arguments_throws_exception() {
        /* @formatter:off */
        assertThatThrownBy(
                ()->
                    EncryptionRotationSetup.builder().
                    build()).

                isInstanceOf(IllegalArgumentException.class).
                hasMessageStartingWith("old cipher must be defined");
        /* @formatter:on */
    }

    @Test
    void setup_build_without_old_cipher_throws_exception() {
        /* @formatter:off */
        assertThatThrownBy(
                ()->
                EncryptionRotationSetup.builder().
                    oldInitialVector(initialVector1).
                build()).

        isInstanceOf(IllegalArgumentException.class).
        hasMessageStartingWith("old cipher must be defined");
        /* @formatter:on */
    }

    @Test
    void setup_build_without_old_initialvector_throws_exception() {
        /* @formatter:off */
        assertThatThrownBy(
                ()->
                EncryptionRotationSetup.builder().
                    oldCipher(cipher1).
                build()).

        isInstanceOf(IllegalArgumentException.class).
        hasMessageStartingWith("old initial vector must be defined");
        /* @formatter:on */
    }

    @Test
    void setup_build_throws_exception_when_only_old_cipher_and_old_initial_vector() {
        /* @formatter:off */
        assertThatThrownBy(
                ()->
                    EncryptionRotationSetup.builder().
                        oldCipher(cipher1).
                        oldInitialVector(initialVector1).
                    build()).

                isInstanceOf(IllegalArgumentException.class).
                hasMessageStartingWith("no new cipher or a new initial vector given");
        /* @formatter:on */
    }

    @Test
    void setup_build_throws_exception_when_only_new_cipher_and_new_initial_vector() {
        /* @formatter:off */
        assertThatThrownBy(
                ()->
                    EncryptionRotationSetup.builder().
                        newCipher(cipher1).
                        newInitialVector(initialVector1).
                    build()).

                isInstanceOf(IllegalArgumentException.class).
                hasMessageStartingWith("old cipher must be defined");
        /* @formatter:on */
    }

    @Test
    void setup_build_throws_exception_when_old_initial_vector_is_missing() {
        /* @formatter:off */
        assertThatThrownBy(
                ()->
                    EncryptionRotationSetup.builder().
                        newCipher(cipher1).
                        newInitialVector(initialVector1).
                        oldCipher(cipher2).
                    build()).

                isInstanceOf(IllegalArgumentException.class).
                hasMessageStartingWith("old initial vector must be defined");
        /* @formatter:on */
    }

    @Test
    void setup_build_throws_exception_when_old_cipher_is_missing() {
        /* @formatter:off */
        assertThatThrownBy(
                ()->
                    EncryptionRotationSetup.builder().
                        newCipher(cipher1).
                        newInitialVector(initialVector1).
                        oldInitialVector(initialVector2).
                    build()).

                isInstanceOf(IllegalArgumentException.class).
                hasMessageStartingWith("old cipher must be defined");
        /* @formatter:on */
    }

    @Test
    void setup_build_works_for_password_rotation_when_no_new_initial_vector_is_set() {
        /* @formatter:off */

        /* execute */
        EncryptionRotationSetup setup = EncryptionRotationSetup.builder().
            oldCipher(cipher1).
            newCipher(cipher2).
            oldInitialVector(initialVector1).
        build();


        /* test */
        assertThat(setup).isNotNull();
        assertThat(setup.getOldCipher()).isEqualTo(cipher1);
        assertThat(setup.getNewCipher()).isEqualTo(cipher2);
        assertThat(setup.getOldInitialVector()).isEqualTo(initialVector1);
        // no new initial vector set, builder will setup to old one automatically
        assertThat(setup.getNewInitialVector()).isEqualTo(initialVector1);

        /* @formatter:on */
    }

    @Test
    void setup_build_works_for_initial_vector_rotation_when_no_new_cipher_is_set() {
        /* @formatter:off */

        /* execute */
        EncryptionRotationSetup setup = EncryptionRotationSetup.builder().
                oldCipher(cipher1).
                oldInitialVector(initialVector1).
                newInitialVector(initialVector2).
                build();


        /* test */
        assertThat(setup).isNotNull();
        assertThat(setup.getOldInitialVector()).isEqualTo(initialVector1);
        assertThat(setup.getNewInitialVector()).isEqualTo(initialVector2);
        assertThat(setup.getOldCipher()).isEqualTo(cipher1);

        // no new cipher set, builder will setup to old one automatically
        assertThat(setup.getNewCipher()).isEqualTo(cipher1);
        /* @formatter:on */
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.core;

import static com.mercedesbenz.sechub.server.core.SecHubTomcatServletWebserverFactory.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class SecHubTomcatServletWebserverFactoryTest {

    private static final int KB = 1024;
    private static final int MB = 1024 * 1024;

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 0, 233 })
    void mb_sizes_are_correctly_treated(int sizeMB) {
        assertEquals((sizeMB + 1) * MB, calculateBytesForOneMegabyteMoreThan(sizeMB + "MB"));
        assertEquals((sizeMB + 1) * MB, calculateBytesForOneMegabyteMoreThan(sizeMB + "mb"));
        assertEquals((sizeMB + 1) * MB, calculateBytesForOneMegabyteMoreThan(sizeMB + " MB"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 0, 31 })
    void kb_sizes_are_correctly_treated(int kiloByteSize) {
        assertEquals(kiloByteSize * KB + 1 * MB, calculateBytesForOneMegabyteMoreThan(kiloByteSize + "KB"));
        assertEquals(kiloByteSize * KB + 1 * MB, calculateBytesForOneMegabyteMoreThan(kiloByteSize + "kb"));
        assertEquals(kiloByteSize * KB + 1 * MB, calculateBytesForOneMegabyteMoreThan(kiloByteSize + " KB"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 0, 104857623 })
    void b_sizes_are_correctly_treated(int bytes) {
        assertEquals(bytes + 1 * MB, calculateBytesForOneMegabyteMoreThan(bytes + "B"));
        assertEquals(bytes + 1 * MB, calculateBytesForOneMegabyteMoreThan(bytes + "b"));
        assertEquals(bytes + 1 * MB, calculateBytesForOneMegabyteMoreThan(bytes + " B"));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "b", "B", "0x" })
    void not_acceptable_sizes_are_returning_negative_one(String givenSize) {
        assertEquals(-1, calculateBytesForOneMegabyteMoreThan(givenSize));
    }

}

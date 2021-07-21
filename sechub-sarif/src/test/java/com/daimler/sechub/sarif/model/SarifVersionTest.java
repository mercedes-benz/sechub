// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SarifVersionTest {

    @Test
    void all_enum_values_have_a_unique_version_text() {
        /* prepare */
        List<String> versionsAlreadyFound = new ArrayList<>();

        for (SarifVersion sarifVersionToTest : SarifVersion.values()) {
            String versionFound = sarifVersionToTest.getVersion();
            if (versionFound == null || versionFound.isEmpty()) {
                fail("version not set inside:" + sarifVersionToTest.name());
            }
            if (versionsAlreadyFound.contains(versionFound)) {
                fail("duplicated version found:" + versionFound);

            }
            versionsAlreadyFound.add(versionFound);
        }
    }

    @Test
    void all_enum_values_have_a_unique_schema_text() {
        /* prepare */
        List<String> schemasAlreadyFound = new ArrayList<>();

        for (SarifVersion sarifVersionToTest : SarifVersion.values()) {
            String schemaFound = sarifVersionToTest.getSchema();
            if (schemaFound == null || schemaFound.isEmpty()) {
                fail("schema not set inside:" + sarifVersionToTest.name());
            }
            if (schemasAlreadyFound.contains(schemaFound)) {
                fail("duplicated schema found:" + schemaFound);

            }
            schemasAlreadyFound.add(schemaFound);
        }
    }

}

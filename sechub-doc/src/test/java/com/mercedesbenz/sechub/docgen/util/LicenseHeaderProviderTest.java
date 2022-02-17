package com.mercedesbenz.sechub.docgen.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class LicenseHeaderProviderTest {

    private LicenseHeaderProvider toTest;

    @Before
    public void before() throws Exception {
        toTest = new LicenseHeaderProvider();
    }

    @Test
    public void check_license_headers_for_NULL_is_null() {
        assertLicenseHeader(null, null, null);
        assertLicenseHeader(null, "", null);
        assertLicenseHeader("adoc", null, null);
    }

    @Test
    public void check_license_headers_for_puml_as_expected() {
        assertLicenseHeader("puml", null, null);
        assertLicenseHeader("puml", "\n\n@startuml", "// SPDX-License-Identifier: MIT");
        assertLicenseHeader("puml", "", "' SPDX-License-Identifier: MIT");
    }

    @Test
    public void check_license_headers_for_plantuml_as_expected() {
        assertLicenseHeader("plantuml", null, null);
        assertLicenseHeader("plantuml", "\n\n@startuml", "// SPDX-License-Identifier: MIT");
        assertLicenseHeader("plantuml", "", "' SPDX-License-Identifier: MIT");
    }

    @Test
    public void check_license_headers_for_adoc_as_expected() {
        assertLicenseHeader("adoc", null, null);
        assertLicenseHeader("adoc", "", "// SPDX-License-Identifier: MIT");
        assertLicenseHeader("adoc", "\n\n@startuml", "// SPDX-License-Identifier: MIT");
    }

    @Test
    public void check_license_headers_for_asciidoc_as_expected() {
        assertLicenseHeader("asciidoc", null, null);
        assertLicenseHeader("asciidoc", "", "// SPDX-License-Identifier: MIT");
        assertLicenseHeader("asciidoc", "\n\n@startuml", "// SPDX-License-Identifier: MIT");
    }

    private void assertLicenseHeader(String fileEnding, String text, String expectedHeader) {
        File file;
        if (fileEnding == null) {
            file = null;
        } else {
            if (!fileEnding.startsWith(".")) {
                fileEnding = "." + fileEnding;
            }
            file = new File("xyz" + fileEnding);
        }
        String header = toTest.getLicenseHeader(text, file);
        assertEquals(expectedHeader, header);
    }

}

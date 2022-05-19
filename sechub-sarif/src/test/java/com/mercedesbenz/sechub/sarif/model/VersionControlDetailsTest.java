// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class VersionControlDetailsTest {

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (vcd) -> vcd.setRepositoryUri("https://github.com/other-corp/package")));
        testBothAreNOTEqual(createExample(), change(createExample(), (vcd) -> vcd.setRevisionId("d0dc2c0")));
        testBothAreNOTEqual(createExample(), change(createExample(), (vcd) -> vcd.setBranch("development")));
        testBothAreNOTEqual(createExample(), change(createExample(), (vcd) -> vcd.setRevisionTag("version 2.4.3")));
        testBothAreNOTEqual(createExample(), change(createExample(), (vcd) -> vcd.setAsOfTimeUtc("2019-03-17")));
        testBothAreNOTEqual(createExample(), change(createExample(), (vcd) -> vcd.setMappedTo(new ArtifactLocation("file:///home/user/directory/", "another/path/to/a/fileWithFinding.txt"))));
        testBothAreNOTEqual(createExample(), change(createExample(), (vcd) -> vcd.setProperties(null)));
        /* @formatter:on */

    }

    private VersionControlDetails createExample() {
        VersionControlDetails versionControlDetails = new VersionControlDetails();

        versionControlDetails.setRepositoryUri("https://github.com/example-corp/package");
        versionControlDetails.setRevisionId("b87c4e9");
        versionControlDetails.setBranch("main");
        versionControlDetails.setRevisionTag("version 1.2");
        versionControlDetails.setAsOfTimeUtc("2016-02-08T16:08:25.943Z");
        versionControlDetails.setMappedTo(new ArtifactLocation("file:///home/user/directory/", "path/to/fileWithFinding.txt"));
        versionControlDetails.setProperties(new PropertyBag());

        return versionControlDetails;
    }

}

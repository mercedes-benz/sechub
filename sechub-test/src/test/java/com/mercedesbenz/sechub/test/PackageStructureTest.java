// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.mercedesbenz.sechub.test.DomainAccessSourceVisitor.DomainProblem;

/**
 * This test case checks that domain communication is only done from packages
 * inside a domain but never between domains directly!
 *
 * @author Albert Tregnaghi
 *
 */
public class PackageStructureTest {

    @Test
    public void test_no_java_file_exists_where_package_with_com_mercedesbenz_secub_domain__imports_from_another_domain() {
        SimpleFileBasedPackageScanner scanner = new SimpleFileBasedPackageScanner();

        TestFileSupport support = new TestFileSupport(null);
        File root = support.getRootFolder();
        DomainAccessSourceVisitor visitor = new DomainAccessSourceVisitor();
//		scanner.setVerbose(true);
        scanner.visit(root, visitor);

        List<DomainProblem> problems = visitor.getProblems();
        if (problems.isEmpty()) {
            throw new IllegalStateException("testcase corrupt, did not found even expected test problem!");
        }
        StringBuilder sb = new StringBuilder();
        for (DomainProblem problem : problems) {
            if (problem.getFile().getName().endsWith("TestScanDomainUsesOtherDomain.java")) {
                /* this is test case file to check problem is identified - we ignore this */
                continue;
            }
            sb.append(problem.getFile() + "\n" + problem.getProblem());
            sb.append("\n\n");
        }
        if (sb.length() == 0) {
            /* okay, only test problem found */
            return;
        }
        fail("Found problems:\n" + sb.toString());
    }
}

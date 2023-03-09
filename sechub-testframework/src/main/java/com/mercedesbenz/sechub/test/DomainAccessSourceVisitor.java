// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.test.SimpleFileBasedPackageScanner.PackageSourceVisitor;

public class DomainAccessSourceVisitor implements PackageSourceVisitor {

    private static final String COM_MERCEDESBENZ_SECHUB_DOMAIN = "com.mercedesbenz.sechub.domain.";

    public class DomainProblem {
        private File file;
        private String problem;

        public DomainProblem(File sourceFile, String string) {
            this.file = sourceFile;
            this.problem = string;
        }

        public File getFile() {
            return file;
        }

        public String getProblem() {
            return problem;
        }

    }

    private List<DomainProblem> problems;

    public List<DomainProblem> getProblems() {
        return problems;
    }

    public DomainAccessSourceVisitor() {
        problems = new ArrayList<>();
    }

    @Override
    public void visit(File sourceFile, String packageOfFile, List<String> importedPackages) {
        if (sourceFile.getName().endsWith("Test.java") && sourceFile.getAbsolutePath().contains("sechub-test")) {
            // inside sechub-test project we allow this inside tests - e.g. when we test
            // that constants are same between two different domains (means defined twice).
            return;
        }
        String packageDomainName = fetchDomainName(packageOfFile);
        if (packageDomainName == null || packageDomainName.isEmpty()) {
            return;
        }
        for (String imported : importedPackages) {
            String importDomainName = fetchDomainName(imported);
            if (importDomainName != null && !importDomainName.equals(packageDomainName)) {
                problems.add(new DomainProblem(sourceFile, "import problem:" + imported + " imported but inside package:" + packageOfFile));
            }
        }
    }

    private String fetchDomainName(String packageOfFile) {
        int index = packageOfFile.indexOf(COM_MERCEDESBENZ_SECHUB_DOMAIN);
        if (index == -1) {
            return null;
        }
        String result = packageOfFile.substring(index + COM_MERCEDESBENZ_SECHUB_DOMAIN.length());
        int indexPoint = result.indexOf('.');
        if (indexPoint != -1) {
            result = result.substring(0, indexPoint);
        }
        return result;
    }

}
// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SimpleFileBasedPackageScanner {
    private static final String PACKAGE = "package";
    private static final String IMPORT = "import";
    private static final JavaFileFiter FILE_FILTER = new JavaFileFiter();

    private static class JavaFileFiter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String name = file.getName();
            return name.endsWith(".java");
        }
    }

    public static interface PackageSourceVisitor {

        public void visit(File sourceFile, String packageOfFile, List<String> importedPackages);
    }

    private boolean verbose;

    public void visit(File file, PackageSourceVisitor visitor) {
        if (file.isDirectory()) {
            if (verbose) {
                System.out.println("visiting directory:" + file);
            }
            for (File child : file.listFiles(FILE_FILTER)) {
                visit(child, visitor);
            }
        } else {
            if (verbose) {
                System.out.println("visiting file:" + file);
            }
            loadAndVisitFile(file, visitor);
        }
    }

    private void loadAndVisitFile(File file, PackageSourceVisitor visitor) {
        List<String> imports = new ArrayList<>();

        String packageOfFile = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith(IMPORT)) {
                    String importLine = reduceLineWithoutPrefixAndTrailingSemicolon(trimmedLine, IMPORT);
                    imports.add(importLine);
                } else if (trimmedLine.startsWith(PACKAGE)) {
                    packageOfFile = reduceLineWithoutPrefixAndTrailingSemicolon(trimmedLine, PACKAGE);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Something corrupt: Cannot read file " + file.getAbsolutePath(), e);
        }
        visitor.visit(file, packageOfFile, imports);
    }

    private String reduceLineWithoutPrefixAndTrailingSemicolon(String trimmedLine, String prefix) {
        String reduced = trimmedLine.substring(prefix.length()).trim();
        /* remove ; */
        if (reduced.endsWith(";")) {
            reduced = reduced.substring(0, reduced.length() - 1);
        }
        return reduced.trim();
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    };
}
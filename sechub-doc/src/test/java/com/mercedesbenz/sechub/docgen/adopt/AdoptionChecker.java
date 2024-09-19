// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.adopt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mercedesbenz.sechub.commons.TextFileReader;

public class AdoptionChecker {

    TextFileReader reader = new TextFileReader();
    private Class<?> adoptedClass;

    public AdoptionChecker(Class<?> adoptedClass) {
        this.adoptedClass = adoptedClass;
    }

    public void assertAdoptedClassEqualsFileLocatedAt(String packageName, String className) throws IOException {

        /*
         * Why is this done by text file compare? because system tools may not be
         * available on sechub-test because sechub-systemtest does need the java api
         * (for details look into technical documentation about build)
         */

        File adoptedFile = new File("./src/main/java/" + javaClassNameToPath(adoptedClass));
        String adopted = reader.readTextFromFile(adoptedFile);
        String adoptedChanged = adopted.replaceAll(Pattern.quote(adoptedClass.getSimpleName()), Matcher.quoteReplacement(className));
        adoptedChanged = adoptedChanged.replaceAll(Pattern.quote(adoptedClass.getPackageName()), Matcher.quoteReplacement(packageName));

        String pathname = "./../sechub-systemtest/src/main/java/" + javaClassToPath(packageName, className);
        File originFile = new File(pathname);
        String origin = reader.readTextFromFile(originFile);

        String originReduced = withoutCommentsOrEmptyLines(origin);
        String adoptedReduced = withoutCommentsOrEmptyLines(adoptedChanged);

        assertEquals(originReduced, adoptedReduced);
    }

    private String javaClassNameToPath(Class<?> javaClass) {
        return javaClassToPath(javaClass.getPackageName(), javaClass.getSimpleName());
    }

    private String javaClassToPath(String _package, String _class) {
        return (_package + "." + _class).replace('.', '/') + ".java";
    }

    private String withoutCommentsOrEmptyLines(String code) {
        String[] lines = code.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (!line.isBlank()) {
                String trimmed = line.trim();
                boolean isComment = trimmed.startsWith("*");
                isComment = isComment || trimmed.startsWith("/*");

                if (isComment) {
                    continue;
                }
                sb.append(line);
                sb.append("\n");
            }
        }

        return sb.toString();
    }

}

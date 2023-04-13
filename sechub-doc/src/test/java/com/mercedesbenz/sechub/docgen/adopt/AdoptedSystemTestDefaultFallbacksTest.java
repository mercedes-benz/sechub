package com.mercedesbenz.sechub.docgen.adopt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.TextFileReader;

class AdoptedSystemTestDefaultFallbacksTest {

    @Test
    public void origin_and_adopted_are_content_equal() throws Exception {
        /*
         * Why is this done by text file compare? because system tools may not be
         * available on sechub-test because sechub-systemtest does need the java api
         * (for details look into technical documentation about build)
         */

        String _packageName = "com.mercedesbenz.sechub.systemtest.config";
        String _className = "DefaultFallback";

        /* prepare */
        TextFileReader reader = new TextFileReader();

        Class<AdoptedSystemTestDefaultFallbacks> _adoptedClass = AdoptedSystemTestDefaultFallbacks.class;
        File adoptedFile = new File("./src/main/java/" + javaClassNameToPath(_adoptedClass));
        String adopted = reader.loadTextFile(adoptedFile);
        String adoptedChanged = adopted.replaceAll("AdoptedSystemTestDefaultFallbacks", "DefaultFallback");
        adoptedChanged = adoptedChanged.replaceAll(_adoptedClass.getPackageName(), _packageName);

        File originFile = new File("./../sechub-systemtest/src/main/java/" + javaClassToPath(_packageName, _className));
        String origin = reader.loadTextFile(originFile);

        assertEquals(withoutCommentsOrEmptyLines(origin), withoutCommentsOrEmptyLines(adoptedChanged));

    }

    private String javaClassNameToPath(Class<?> javaClass) {
        return javaClassToPath(javaClass.getPackageName(), javaClass.getSimpleName());
    }

    private String javaClassToPath(String _package, String _class) {
        return (_package + "." + _class).replace('.', '/') + ".java";
    }

    private String withoutCommentsOrEmptyLines(String code) {
        String regexpNoComments = "\\/\\*(.|\\n)*\\*\\/";
        String noComments = code.replaceAll(regexpNoComments, "");

        String[] lines = noComments.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (!line.isBlank()) {
                sb.append(line);
                sb.append("\n");
            }
        }

        return sb.toString();
    }

}

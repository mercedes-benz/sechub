package com.mercedesbenz.sechub.docgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AsciidocUtilTest {

    @Test // no parameterized test with CSV values, because CSV values shall not contain
          // \n inside
    void isEmptyAsciidocContent_returns_true_when_only_not_visible_parts() {

        /* prepare @formatter:off */
        String[] emptyDataToTest = new String[] {
                null,
                "|===",
                "//something\n//line2\n//line\n[option\n----\n----",
                "//something\n//line2\n//line\n[source\n----\n----" };
        // @formatter:on
        for (String emptyData : emptyDataToTest) {
            /* execute */
            boolean isEmpty = AsciidocUtil.isEmptyAsciidocContent(emptyData);

            /* test */
            assertTrue(isEmpty, "This should be empty:" + emptyData);

        }
    }

    @Test // no parameterized test with CSV values, because CSV values shall not contain
          // \n inside
    void isEmptyAsciidocContent_returns_false_when_visible_parts_inside() {
        /* prepare @formatter:off */
        String[] dataToTest = new String[] {
                "line1",
                "//comment\nline2",
                "//something\n//line2\n//line\n[other\n----\n----",
                "//something\n//line2\n//line\n[xyz\n|---\nsomething\n----" };

        // @formatter:on
        for (String data : dataToTest) {
            /* execute */
            boolean isEmpty = AsciidocUtil.isEmptyAsciidocContent(data);

            /* test */
            assertFalse(isEmpty, "This should be not empty:" + data);

        }
    }

}

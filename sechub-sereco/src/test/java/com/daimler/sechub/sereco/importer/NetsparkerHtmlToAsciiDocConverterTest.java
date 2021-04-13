// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NetsparkerHtmlToAsciiDocConverterTest {

    @Test
    public void single_paragraph() {
        /* prepare */
        String html = "<p>This is text.</p>\n";
        String expected = "This is text.";

        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }

    @Test
    public void many_paragraphs() {
        /* prepare */
        String html = "<p>First paragraph.</p>\n<p>Second paragraph.</p>\n<p>Third paragraph.</p>\n";
        String expected = "First paragraph.\n\nSecond paragraph.\n\nThird paragraph.";

        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }

    @Test
    public void unordered_list() {
        /* prepare */
        String html = "<ul><li class='abc'>First</li><li>Second</li><li>Third</li><ul>\n";
        String expected = "* First\n * Second\n * Third";

        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }

    @Test
    public void ordered_list() {
        /* prepare */
        String html = "<ol><li class='abc'>First</li><li>Second</li><li>Third</li></ol>\n";
        String expected = ". First\n . Second\n . Third";

        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }

    @Test
    public void link() {
        /* prepare */
        String html = "<a href='https://example.org'>Example.org</a>\n";
        String expected = "https://example.org[Example.org]";

        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }

    @Test
    public void table() {
        /* prepare */
        
        /* @formatter:off */
        String html = "<table class=\"kb-table\" width=\"30%\">\n" + 
                        "<tr>\n" + 
                            "<td><b>Error</b></td>\n" + 
                            "<td><b>Resolution</b></td>\n" +
                        "</tr>\n" +
                        "<tr>\n" + 
                            "<td nowrap>preload directive not present</td>\n" +
                            "<td nowrap>Submit domain for inclusion in browsers&#39; HTTP Strict Transport Security (HSTS) preload list.</td>\n" + 
                        "</tr>\n" + 
                      "</table>";

        String expected = ".Table\n" + 
                          "|=========================\n" + 
                          "| Error | Resolution\n" +
                          "\n" + 
                          "| preload directive not present\n" +
                          "| Submit domain for inclusion in browsers' HTTP Strict Transport Security (HSTS) preload list.\n" + 
                          "|=========================";
        /* @formatter:on */
        
        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }

    @Test
    public void table_and_paragraph() {
        /* prepare */
        
        /* @formatter:off */
        String html = "<p>Netsparker Enterprise detected errors during parsing of Strict-Transport-Security header.</p>" +
                "<table class=\"kb-table\" width=\"30%\">\n" +
                    "<tr>\n" + 
                        "<td><b>Error</b></td>\n" +
                        "<td><b>Resolution</b></td>\n" +
                    "</tr>\n" +
                    "<tr>\n" +
                        "<td nowrap>preload directive not present</td>\n" +
                        "<td nowrap>Submit domain for inclusion in browsers&#39; HTTP Strict Transport Security (HSTS) preload list.</td>\n" +
                    "</tr>\n" +
                "</table>";

        String expected = "Netsparker Enterprise detected errors during parsing of Strict-Transport-Security header.\n" + 
                "\n.Table\n" +
                "|=========================\n" +
                "| Error | Resolution\n" +
                "\n" +
                "| preload directive not present\n" +
                "| Submit domain for inclusion in browsers' HTTP Strict Transport Security (HSTS) preload list.\n" +
                "|=========================";
        /* @formatter:on */

        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }

    @Test
    public void javascript() {
        /* prepare */
        String html = "<script>alert('hello');</script>";
        String expected = "";

        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }

    @Test
    public void paragraph_with_strange_characters() {
        /* prepare */
        String html = "<p>Netsparker Enterprise detected that a deprecated, insecureÂ transportation security protocol (TLS 1.1) is supported by your web server.</p>\n"
                + "<p>TLS 1.1 will be considered as deprecated by major web browsers (i.e. Chrome, Firefox, Safari, Edge, Internet Explorer)Â starting in 2020.</p>";

        String expected = "Netsparker Enterprise detected that a deprecated, insecure transportation security protocol (TLS 1.1) is supported by your web server.\n\n"
                + "TLS 1.1 will be considered as deprecated by major web browsers (i.e. Chrome, Firefox, Safari, Edge, Internet Explorer) starting in 2020.";

        /* execute */
        String actual = NetsparkerHtmlToAsciiDocConverter.convert(html);

        /* test */
        assertEquals(expected, actual);
    }
}

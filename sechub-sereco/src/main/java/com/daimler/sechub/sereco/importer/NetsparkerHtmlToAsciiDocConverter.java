// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

/**
 * Converts the Netsparker HTML to the AsciiDoc plain text format
 */
public class NetsparkerHtmlToAsciiDocConverter {

    private final static Pattern NO_BREAK_SPACE_PATTERN = Pattern.compile("Â&nbsp;");
    private final static Pattern SPACES_BEFORE_NEWLINE = Pattern.compile("\\s+\n");

    /**
     * Convert unsanitized HTML into the AsciiDoc plain text format.
     * 
     * @param unsanitizedHtml
     * @return The content in the AsciiDoc plain text format.
     */
    public static String convert(String unsanitizedHtml) {
        // Sanitize HTML to prevent Cross-Site-Scripting (XSS)
        String html = Jsoup.clean(unsanitizedHtml, Whitelist.relaxed());

        html = removeStrangeSymbols(html);

        // Parse sanitized HTML
        Document document = Jsoup.parse(html);

        convertParagraphs(document);

        convertLinks(document);

        convertLists(document);

        convertTables(document);
        
        String text = document.wholeText().trim();
        
        text = removeSpacesBeforeNewline(text);

        return text;
    }
    
    private static String removeSpacesBeforeNewline(String text) {
        text = SPACES_BEFORE_NEWLINE.matcher(text).replaceAll("\n\n");
        return text;
    }

    private static String removeStrangeSymbols(String html) {
        // Delete all "Â&nbsp;" from the HTML
        // Unclear why Netsparker sometimes includes it
        html = NO_BREAK_SPACE_PATTERN.matcher(html).replaceAll(" ");
        return html;
    }

    private static void convertParagraphs(Document document) {
        Elements paragraphs = document.select("p");

        for (Element paragraph : paragraphs) {
            paragraph = paragraph.text(paragraph.text() + "\n");
        }
    }

    private static void convertLinks(Document document) {
        Elements links = document.select("a");

        for (Element link : links) {
            String url = link.attr("href");

            if (!url.isEmpty()) {
                String linkText = link.text();

                link.text(url + "[" + linkText + "]");
            }
        }
    }

    private static void convertLists(Document document) {
        // Convert unordered list items
        convertListItems(document.select("ul > li"), "*");

        // Convert ordered list items
        convertListItems(document.select("ol > li"), ".");
    }

    private static void convertListItems(Elements listItems, String bulletSymbol) {
        for (Element listItem : listItems) {
            String listItemText = listItem.text();
            listItem.text(bulletSymbol + " " + listItemText);
        }
    }

    private static void convertTables(Document document) {
        Elements tables = document.select("table");
        for (Element table : tables) {
            table = convertTableToAsciiDocTable(table);
        }
    }

    private static Element convertTableToAsciiDocTable(Element table) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(".Table\n");
        sb.append("|=========================\n");

        Element firstRow = table.selectFirst("tr");

        Elements firstRowFields = firstRow.select("td");

        for (Element firstRowField : firstRowFields) {
            sb.append("| " + firstRowField.text() + " ");
        }

        Elements allButFirstRows = table.select("tr:not(:first-child)");

        for (Element row : allButFirstRows) {
            sb.append("\n\n");

            Elements fields = row.select("td");

            for (Element field : fields) {
                sb.append("| " + field.text() + "\n");
            }
        }

        sb.append("|=========================\n");

        return table.text(sb.toString());
    }
}
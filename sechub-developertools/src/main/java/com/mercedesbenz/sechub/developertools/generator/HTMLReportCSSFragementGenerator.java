// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.generator;

import java.io.File;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.test.TextFileWriter;

/**
 * How to use ? Why this generator?
 *
 * 1. As a web designer I want to change my CSS files and style the report
 * without always restarting the server + fetching new reports etc.
 *
 * Remark: To avoid starting a server at all, it is also possible to start only
 * the `ThymeLeafHTMLReportingTest` and inspect the results! This generator is
 * only necessary when we have to change the CSS styling.
 *
 * 2. Change the CSS styles - how to ? Enable the web developer mode - see
 * HTMLScanResultReportModelBuilder.java and start the server. Generate some
 * testdata and download a HTML report.
 *
 * 3. The report does not look well, because the css file cannot be loaded (same
 * origin policy...)
 *
 * 4. Store the report as local html file and load again css does now apply and
 * you can design...
 *
 * 5. After css file is as wanted... --> Start this generator. --> will adopt
 * css data into fragement file.
 *
 * 6. Commit push done...
 *
 * @author Albert Tregnaghi
 *
 */
public class HTMLReportCSSFragementGenerator {

    private static String CSS_FRAGMENT_START = "<style type=\"text/css\" th:fragment=\"styles\">";

    private static final TextFileReader reader = new TextFileReader();
    private static final TextFileWriter writer = new TextFileWriter();

    public static void main(String[] args) throws Exception {
        File scanHTMLFolder = new File("./../sechub-scan/src/main/resources/templates/report/html");

        File cssFile = new File(scanHTMLFolder, "scanresult.css");
        File fragmentsFile = new File(scanHTMLFolder, "fragments.html");

        String cssContent = reader.loadTextFile(cssFile);
        String fragmentsContent = reader.loadTextFile(fragmentsFile);

        int fragmentIndex = fragmentsContent.indexOf(CSS_FRAGMENT_START);

        if (fragmentIndex == -1) {
            throw new IllegalStateException("Fragment start not found!");
        }
        fragmentIndex = fragmentIndex + CSS_FRAGMENT_START.length();
        int closeStyleIndex = fragmentsContent.indexOf("</style", fragmentIndex);
        if (closeStyleIndex == -1) {
            throw new IllegalStateException("Fragment end not found!");
        }

        String newFragmentContent = fragmentsContent.substring(0, fragmentIndex) + "\n" + cssContent + "\n" + fragmentsContent.substring(closeStyleIndex);
        writer.save(fragmentsFile, newFragmentContent, true);

    }

}

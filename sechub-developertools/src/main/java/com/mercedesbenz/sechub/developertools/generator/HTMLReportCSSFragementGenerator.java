// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.generator;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.test.CSSFileToFragementMerger;

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

    public static void main(String[] args) throws Exception {
        new HTMLReportCSSFragementGenerator().generate();
    }

    public void generate() throws IOException {
        File scanHTMLFolder = new File("./../sechub-scan/src/main/resources/templates/report/html");

        File cssFile = new File(scanHTMLFolder, "scanresult.css");
        File fragmentsFile = new File(scanHTMLFolder, "fragments.html");

        CSSFileToFragementMerger merger = new CSSFileToFragementMerger();
        merger.merge(cssFile, fragmentsFile);

    }

}

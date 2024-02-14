// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.generator;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.test.CSSFileToFragementMerger;

/**
 * This fragment generator keeps styling.html and scanresult.css in sync. Please
 * change always the CSS file and call afterwards this generator to have the CSS
 * data in both worlds.
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

        CSSFileToFragementMerger merger = new CSSFileToFragementMerger();
        merger.merge(cssFile, new File(scanHTMLFolder, "styling.html")); // used by scanreport.html template
    }

}

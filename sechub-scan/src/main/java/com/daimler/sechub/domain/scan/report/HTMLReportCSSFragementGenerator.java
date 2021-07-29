package com.daimler.sechub.domain.scan.report;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * How to use ? Why this generator?
 * 
 * 1. As a web designer I want to change my CSS files and style the report
 * without always restarting the server + fetching new reports etc.
 * 
 * 2. Change the CSS styles - how to ? Enable the web developer mode - see
 * HTMLScanResultReportModelBuilder.java and start the server. Generate some
 * testdata and download a HTML report.
 * 
 * 3. The report does not look well, because the css file cannot be loaded (same
 * origin policy...)
 * 
 * 4. store the report as local html file and load again css does now apply and
 * you can design...
 * 
 * 5. After css file is as wanted... --> Start this generator. --> will adopt
 * css data into fragement file.
 * 
 * 6. commit push done...
 * 
 * @author Albert Tregnaghi
 *
 */
public class HTMLReportCSSFragementGenerator {

    private static String CSS_FRAGMENT_START = "<style type=\"text/css\" th:fragment=\"styles\">";

    public static void main(String[] args) throws Exception {
        File cssFile = new File("./src/main/resources/templates/report/html/scanresult.css");
        File fragmentsFile = new File("./src/main/resources/templates/report/html/fragments.html");
        Charset utf8 = Charset.forName("UTF-8");

        String cssContent = Files.readString(cssFile.toPath(), utf8);
        String fragmentsContent = Files.readString(fragmentsFile.toPath(), utf8);

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
        Files.writeString(fragmentsFile.toPath(), newFragmentContent, utf8);

    }
}

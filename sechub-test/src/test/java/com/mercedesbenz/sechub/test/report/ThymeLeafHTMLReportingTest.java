// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test.report;

import static com.mercedesbenz.sechub.test.report.ReportTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.TrafficLightSupport;
import com.mercedesbenz.sechub.docgen.util.DocGenTextFileWriter;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.report.HTMLScanResultReportModelBuilder;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.test.TestUtil;

/**
 * A special reporting test: Will create "real life" HTML reports very fast (no
 * server or spring boot container start necessary) and test output rudimentary.
 *
 * After the tests are executed, you can open the created files in your browser
 * and check the results. This is very convenient for report development.
 *
 * @author Albert Tregnaghi
 *
 */
public class ThymeLeafHTMLReportingTest {
    private static TemplateEngine thymeleafTemplateEngine;

    private static final Logger LOG = LoggerFactory.getLogger(ThymeLeafHTMLReportingTest.class);

    @BeforeAll
    private static void beforAll() throws IOException {
        thymeleafTemplateEngine = new TemplateEngine();
        thymeleafTemplateEngine.setDialect(new SpringStandardDialect());

        FileTemplateResolver templateResolver = new FileTemplateResolver();
        String templatesDirectory = "./../sechub-scan/src/main/resources/templates/";

        templateResolver.setPrefix(templatesDirectory);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        templateResolver.setCheckExistence(true);

        thymeleafTemplateEngine.setTemplateResolver(templateResolver);

    }

    @Test
    void example1_owasp_zap_sarif_report_is_transformed_to_expected_sechub_report_HTML_with_web_data_and_user_labels() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(1, ProductIdentifier.PDS_WEBSCAN, ReportInputFormat.SARIF, "owasp_zap_report");
        context.sechubJobUUID = "f5fdccc6-45d1-4b41-972c-08ff9ee0dddb";
        context.getMetaData().getLabels().put("Typ", "OWASP Zap report example");
        context.getMetaData().getLabels().put("Info statement", "The labels are alphabetical sorted!");
        context.getMetaData().getLabels().put("Timestamp", LocalDateTime.now().toString());

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        storeHTMLOutputAsFile(htmlResult, "example1");

        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("XSS"), "The report must at least contain a cross site scripting vulnerability!");
        assertTrue(htmlResult.contains("CWE-614"), "The report must at least contain the CWE-614 vulnerability!");
        assertTrue(htmlResult.contains("Cross Site Scripting (Reflected)"), "The report must at least contain a cross site scripting reflected vulnerability!");

        assertTrue(htmlResult.contains("Red findings"));
        assertTrue(htmlResult.contains("Yellow findings"));
        assertTrue(htmlResult.contains("Green findings"));

        assertTrue(htmlResult.contains("OWASP Zap report example"));

    }

    @Test
    void example2_artifical_data_is_transformed_to_expected_sechub_report_HTML_with_web_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(2, ProductIdentifier.PDS_WEBSCAN, ReportInputFormat.SARIF, "artificial_data");
        context.sechubJobUUID = "f5fdccc6-45d2-4b42-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        storeHTMLOutputAsFile(htmlResult, "example2");

        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("testdata.rule1.shortdescription.text"));

        assertFalse(htmlResult.contains("Red findings"));
        assertTrue(htmlResult.contains("Yellow findings"));
        assertFalse(htmlResult.contains("Green findings"));

    }

    @Test
    void example3_covertiy_sarif_is_transformed_to_expected_sechub_report_HTML_with_code_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(3, ProductIdentifier.PDS_CODESCAN, ReportInputFormat.SARIF, "coverity");
        context.sechubJobUUID = "f5fdccc6-45d3-4b43-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        storeHTMLOutputAsFile(htmlResult, "example3");

        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("Aliasing3.java"));
        assertTrue(htmlResult.contains("Filesystem path, filename, or URI manipulation"));
        assertTrue(htmlResult.contains("securibench-micro/src/securibench/micro/basic/Basic40.java"));

        assertTrue(htmlResult.contains("Red findings"));
        assertTrue(htmlResult.contains("Yellow findings"));
        assertTrue(htmlResult.contains("Green findings"));

    }

    @Test
    void example4_checkmarx_xml_is_transformed_to_expected_sechub_report_HTML_with_code_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(4, ProductIdentifier.PDS_CODESCAN, ReportInputFormat.CHECKMARX, "checkmarx");
        context.sechubJobUUID = "f5fdccc6-45d3-4b44-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        storeHTMLOutputAsFile(htmlResult, "example4");

        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("java/com/mercedesbenz/sechub/docgen/util/DocGenTextFileWriter.java"));

        assertFalse(htmlResult.contains("Red findings"));
        assertTrue(htmlResult.contains("Yellow findings"));
        assertTrue(htmlResult.contains("Green findings"));

    }

    @Test
    void example5_gosec_sarif_is_transformed_to_expected_sechub_report_HTML_with_code_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(5, ProductIdentifier.PDS_CODESCAN, ReportInputFormat.SARIF, "gosec");
        context.sechubJobUUID = "f5fdccc6-45d3-4b45-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        storeHTMLOutputAsFile(htmlResult, "example5");
        assertTrue(htmlResult.contains(context.sechubJobUUID));

        assertTrue(htmlResult.contains("Red findings"));
        assertTrue(htmlResult.contains("Yellow findings"));
        assertFalse(htmlResult.contains("Green findings"));

    }

    @Test
    void example6_sechub_report_json_file_would_be_shown_as_expected_report_HTML_with_messages() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(6, ProductIdentifier.PDS_CODESCAN, ReportInputFormat.SECHUB_REPORT,
                "report_without_findings_but_messages");
        context.sechubJobUUID = "f5fdccc6-45d3-4b45-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        storeHTMLOutputAsFile(htmlResult, "example6");

        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("Job execution failed because of an internal problem!"));
        assertTrue(htmlResult.contains("No results from a security product available for this job!"));
        assertTrue(htmlResult.contains("Messages"));

        assertFalse(htmlResult.contains("Red findings"));
        assertFalse(htmlResult.contains("Yellow findings"));
        assertFalse(htmlResult.contains("Green findings"));

    }

    @Test
    void example7_gitleaks_sarif_is_transformed_to_expected_sechub_report_HTML_with_secret_data_and_finding_revision_id() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(7, ProductIdentifier.PDS_SECRETSCAN, ReportInputFormat.SARIF, "gitleaks");
        context.sechubJobUUID = "b6fdccc6-45d3-4b45-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        /* test */
        assertNotNull(htmlResult);
        storeHTMLOutputAsFile(htmlResult, "example7");
        assertTrue(htmlResult.contains(context.sechubJobUUID));

        assertTrue(htmlResult.contains("Yellow findings"));
        assertFalse(htmlResult.contains("Red findings"));
        assertFalse(htmlResult.contains("Green findings"));
        assertTrue(htmlResult.contains("0000000000012345")); // commit sha as revision
    }

    @Test
    void example8_pseudo_gitleaks_sarif_with_version_control_transformed_to_sechub_report_HTML_with_version_control_and_secret_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(8, ProductIdentifier.PDS_SECRETSCAN, ReportInputFormat.SARIF, "pseudo_gitleaks_with_version_control");
        context.sechubJobUUID = "a5feccc6-45d3-4b45-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        storeHTMLOutputAsFile(htmlResult, "example8");
        assertTrue(htmlResult.contains(context.sechubJobUUID));

        assertTrue(htmlResult.contains("Yellow findings"));
        assertFalse(htmlResult.contains("Red findings"));
        assertFalse(htmlResult.contains("Green findings"));
        assertTrue(htmlResult.contains("b87c4e9")); // first one
        assertFalse(htmlResult.contains("cafdac7")); // seconds is not listed by sechub
    }

    private void storeHTMLOutputAsFile(String htmlResult, String name) throws IOException {
        TestFileWriter writer = new TestFileWriter();
        writer.writeTextToFile(new File("./build/test-data/thymeleaf-test/" + name + ".html"), htmlResult, true);
    }

    private String processThymeLeafTemplates(TestReportContext context) throws IOException, SecHubExecutionException {
        String htmlResult = thymeleafTemplateEngine.process("report/html/report", context.convertToThymeLeafContext());

        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, context);

        return htmlResult;
    }

    private void storeAsJSONFileForDebuggingWhenTempFilesAreKept(String sechubJsonReport, TestReportContext context) throws IOException {
        storeAsFileForDevelopmentWhenTempFilesAreKept(sechubJsonReport, context, "json");
    }

    private void storeAsHTMLFileForReportDesignWhenTempFilesAreKept(String htmlResult, TestReportContext context) throws IOException {
        storeAsFileForDevelopmentWhenTempFilesAreKept(htmlResult, context, "html");
    }

    private void storeAsFileForDevelopmentWhenTempFilesAreKept(String content, TestReportContext context, String fileEnding) throws IOException {
        if (!TestUtil.isKeepingTempfiles()) {
            return;
        }
        Path testFile = TestUtil.createTempFileInBuildFolder("thymeleaf-html-reporttest-" + context.exampleName + "." + fileEnding);
        DocGenTextFileWriter writer = new DocGenTextFileWriter();
        writer.writeTextToFile(testFile.toFile(), content);

        LOG.info("Wrote test file to:{}", testFile);
    }

    private enum ReportInputFormat {
        SARIF, CHECKMARX, SECHUB_REPORT
    }

    /**
     * Contains information about report meta data
     */
    private class ReportMetaDataInfo {
        private Map<String, String> labels = new TreeMap<>();

        public boolean isMetaDataNecessaryForReport() {
            return !labels.isEmpty();
        }

        public Map<String, String> getLabels() {
            return labels;
        }

    }

    private class TestReportContext {

        private String variant;
        private String exampleName;
        private String sechubJobUUID;
        private String sourceReportAsString;
        private ReportInputFormat inputFormat;
        private ProductIdentifier productIdentifier;
        private ReportMetaDataInfo metaData = new ReportMetaDataInfo();

        private TestReportContext(int exampleNumber, ProductIdentifier productIdentifier, ReportInputFormat inputFormat, String variant) {
            this.variant = variant;
            this.exampleName = "example" + exampleNumber;
            this.productIdentifier = productIdentifier;
            this.inputFormat = inputFormat;
            initReport();
        }

        private void initReport() {
            switch (inputFormat) {
            case SECHUB_REPORT:
                sourceReportAsString = ReportTestHelper.loadSecHubReportFileTemplate(exampleName + "_" + variant);
                break;
            case CHECKMARX:
                sourceReportAsString = ReportTestHelper.load3rdPartyReportAsString(exampleName + "_" + variant + ".xml");
                break;
            case SARIF:
                sourceReportAsString = ReportTestHelper.loadSarifReport(exampleName + "_" + variant);
                break;
            default:
                throw new IllegalStateException("input format not supported:" + inputFormat);

            }
        }

        public ReportMetaDataInfo getMetaData() {
            return metaData;
        }

        public IContext convertToThymeLeafContext() throws IOException, SecHubExecutionException {
            Map<String, Object> tyhmeleafMap = createThymeLeafReportData();
            IContext context = new Context(Locale.ENGLISH, tyhmeleafMap);
            return context;
        }

        private Map<String, Object> createThymeLeafReportData() throws IOException, SecHubExecutionException {
            ScanReport report;
            switch (inputFormat) {
            case SECHUB_REPORT:
                report = transformSecHubReportTemplateToResult(sourceReportAsString, sechubJobUUID);
                break;
            case CHECKMARX:
                report = transformCheckmarxToSecHubReportResult(sourceReportAsString, sechubJobUUID);
                break;
            case SARIF:
                // for sarif transformation we must give the product identifier as information
                report = transformToScanReport(sourceReportAsString, productIdentifier, sechubJobUUID);
                break;
            default:
                throw new IllegalStateException("input format not supported:" + inputFormat);

            }
            TrafficLightSupport trafficLightSupport = new TrafficLightSupport();
            HTMLScanResultReportModelBuilder reportModelBuilder = new HTMLScanResultReportModelBuilder();

            String sechubReportAsJson = report.getResult();
            SecHubReportModel reportModel = SecHubReportModel.fromJSONString(sechubReportAsJson);

            report.setTrafficLight(trafficLightSupport.calculateTrafficLight(reportModel.getResult()));

            ScanSecHubReport scanReport = new ScanSecHubReport(report);
            if (getMetaData().isMetaDataNecessaryForReport()) {

                Optional<SecHubReportMetaData> reportMetaData = scanReport.getMetaData();
                reportMetaData.get().getLabels().putAll(getMetaData().labels);
                scanReport.setMetaData(reportMetaData.get());
            }
            storeAsJSONFileForDebuggingWhenTempFilesAreKept(JSONConverter.get().toJSON(scanReport, true), this);
            Map<String, Object> tyhmeleafMap = reportModelBuilder.build(scanReport);
            return tyhmeleafMap;
        }

    }
}

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
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring5.dialect.SpringStandardDialect;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.TrafficLightSupport;
import com.mercedesbenz.sechub.docgen.util.TextFileWriter;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.TestHTMLScanResultReportModelBuilder;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.test.CSSFileToFragementMerger;
import com.mercedesbenz.sechub.test.TestUtil;

/**
 * A special reporting test: Will create "real life" HTML reports very fast (no
 * server or spring boot container start necessary) and test output rudimentary.
 * <br>
 * <br>
 * Does automatically load sarif test data from
 * "src/test/resources/report/input". Also able to store temporary JSON and HTML
 * output files to build when {@link TestUtil#isDeletingTempFiles()} returns
 * <code>true</code> (this is interesting when designing or debugging
 * reporting).
 *
 * When {@link TestUtil#isDeletingTempFiles()} returns <code>true</code> the
 * fragements file will be automatically updated by data from "scanresult.css"
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

        if (TestUtil.isAutoCSSFragementGenerationEnabled()) {

            File scanHTMLFolder = new File("./../sechub-scan/src/main/resources/templates/report/html");

            File cssFile = new File(scanHTMLFolder, "scanresult.css");
            File fragmentsFile = new File(scanHTMLFolder, "fragments.html");

            CSSFileToFragementMerger merger = new CSSFileToFragementMerger();
            merger.merge(cssFile, fragmentsFile);
        } else {
            LOG.info("Skipping CSS auto generation/merging");
        }
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

        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("XSS"), "The report must at least contain a cross site scripting vulnerability!");
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

        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("java/com/mercedesbenz/sechub/docgen/util/TextFileWriter.java"));

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

        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("Job execution failed because of an internal problem!"));
        assertTrue(htmlResult.contains("No results from a security product available for this job!"));
        assertTrue(htmlResult.contains("Messages"));

        assertFalse(htmlResult.contains("Red findings"));
        assertFalse(htmlResult.contains("Yellow findings"));
        assertFalse(htmlResult.contains("Green findings"));

    }

    private String processThymeLeafTemplates(TestReportContext context) throws IOException, SecHubExecutionException {
        String htmlResult = thymeleafTemplateEngine.process("report/html/scanresult", context.convertToThymeLeafContext());

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
        TextFileWriter writer = new TextFileWriter();
        writer.save(testFile.toFile(), content);

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
            TestHTMLScanResultReportModelBuilder reportModelBuilder = new TestHTMLScanResultReportModelBuilder(trafficLightSupport);

            String sechubReportAsJson = report.getResult();
            SecHubReportModel reportModel = SecHubReportModel.fromJSONString(sechubReportAsJson);

            report.setTrafficLight(trafficLightSupport.calculateTrafficLight(reportModel.getResult()));

            ScanSecHubReport scanReport = new ScanSecHubReport(report);
            if (getMetaData().isMetaDataNecessaryForReport()) {

                SecHubReportMetaData reportMetaData = new SecHubReportMetaData();
                reportMetaData.getLabels().putAll(getMetaData().labels);
                scanReport.setMetaData(reportMetaData);
            }
            storeAsJSONFileForDebuggingWhenTempFilesAreKept(JSONConverter.get().toJSON(scanReport, true), this);
            Map<String, Object> tyhmeleafMap = reportModelBuilder.build(scanReport);
            return tyhmeleafMap;
        }

    }
}

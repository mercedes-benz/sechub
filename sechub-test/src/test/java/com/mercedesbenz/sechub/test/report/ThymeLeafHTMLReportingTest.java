// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test.report;

import static com.mercedesbenz.sechub.test.report.ReportTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring5.dialect.SpringStandardDialect;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.TrafficLightSupport;
import com.mercedesbenz.sechub.docgen.util.TextFileWriter;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.TestHTMLScanResultReportModelBuilder;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportResultType;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.test.TestUtil;

/**
 * A special reporting test: Will create "real life" HTML reports very fast (no
 * server or spring boot container start necessary). <br>
 * <br>
 * Does automatically load sarif test data from
 * "src/test/resources/report/input". Also able to store temp JSON and HTML
 * output files to build when {@link TestUtil#isDeletingTempFiles()} returns
 * <code>true</code> (this is interesting when designing or debugging
 * reporting). For information about CSS changed please read more in
 * `HTMLReportCSSFragementGenerator`
 *
 * @author Albert Tregnaghi
 *
 */
public class ThymeLeafHTMLReportingTest {
    private static TemplateEngine thymeleafTemplateEngine;

    @Test
    void example1_owasp_zap_sarif_report_is_transformed_to_expected_sechub_report_HTML_with_web_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(1, ReportInputFormat.SARIF, "owasp_zap_report");
        context.sechubJobUUID = "f5fdccc6-45d1-4b41-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(context.sechubJobUUID));

        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, context);
    }

    private String processThymeLeafTemplates(TestReportContext context) throws IOException, SecHubExecutionException {
        return thymeleafTemplateEngine.process("report/html/scanresult", context.convertToThymeLeafContext());
    }

    @Test
    void example2_artifical_data_is_transformed_to_expected_sechub_report_HTML_with_web_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(2, ReportInputFormat.SARIF, "artificial_data");
        context.sechubJobUUID = "f5fdccc6-45d2-4b42-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(context.sechubJobUUID));

        storeAsFileForDevelopmentWhenTempFilesAreKept(htmlResult, context, "html");
    }

    @Test
    void example3_covertiy_sarif_is_transformed_to_expected_sechub_report_HTML_with_code_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(3, ReportInputFormat.SARIF, "coverity");
        context.sechubJobUUID = "f5fdccc6-45d3-4b43-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(context.sechubJobUUID));

        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, context);
    }

    @Test
    void example4_checkmarx_xml_is_transformed_to_expected_sechub_report_HTML_with_code_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(4, ReportInputFormat.CHECKMARX, "checkmarx");
        context.sechubJobUUID = "f5fdccc6-45d3-4b44-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(context.sechubJobUUID));

        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, context);
    }

    @Test
    void example5_gosec_sarif_is_transformed_to_expected_sechub_report_HTML_with_code_data() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(5, ReportInputFormat.SARIF, "gosec");
        context.sechubJobUUID = "f5fdccc6-45d3-4b45-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(context.sechubJobUUID));

        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, context);
    }

    @Test
    void example6_sechub_report_json_file_would_be_shown_as_expected_report_HTML_with_messages() throws Exception {
        /* prepare */
        TestReportContext context = new TestReportContext(6, ReportInputFormat.SECHUB_REPORT, "report_without_findings_but_messages");
        context.sechubJobUUID = "f5fdccc6-45d3-4b45-972c-08ff9ee0dddb";

        /* execute */
        String htmlResult = processThymeLeafTemplates(context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(context.sechubJobUUID));
        assertTrue(htmlResult.contains("Job execution failed because of an internal problem!"));
        assertTrue(htmlResult.contains("No results from a security product available for this job!"));
        assertTrue(htmlResult.contains("Messages"));

        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, context);

    }

    @BeforeAll
    private static void beforAll() {
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

        System.out.println("Wrote test file to:" + testFile);
    }

    private enum ReportInputFormat {
        SARIF, CHECKMARX, SECHUB_REPORT
    }

    private class TestReportContext {

        private String variant;
        private String exampleName;
        private String sechubJobUUID;
        private String sourceReportAsString;
        private ReportInputFormat inputFormat;

        private TestReportContext(int exampleNumber, ReportInputFormat inputFormat, String variant) {
            this.variant = variant;
            this.inputFormat = inputFormat;
            this.exampleName = "example" + exampleNumber;

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

        public IContext convertToThymeLeafContext() throws IOException, SecHubExecutionException {
            Map<String, Object> tyhmeleafMap = createThymeLeafReportData();
            IContext context = new Context(Locale.ENGLISH, tyhmeleafMap);
            return context;
        }

        private Map<String, Object> createThymeLeafReportData() throws IOException, SecHubExecutionException {
            ScanReport report;
            switch (inputFormat) {
            case SECHUB_REPORT:

                report = new ScanReport(null, null);
                report.setResultType(ScanReportResultType.MODEL);
                String sechubReport = sourceReportAsString.replace("__SECHUB_JOB_UUID__", sechubJobUUID);
                report.setResult(sechubReport);
                break;
            case CHECKMARX:
                report = transformCheckmarxToSecHubReportResult(sourceReportAsString, sechubJobUUID);
                break;
            case SARIF:
                report = transformSarifToScanReport(sourceReportAsString, ProductIdentifier.PDS_WEBSCAN, sechubJobUUID);
                break;
            default:
                throw new IllegalStateException("input format not supported:" + inputFormat);

            }
            TrafficLightSupport trafficLightSupport = new TrafficLightSupport();
            TestHTMLScanResultReportModelBuilder reportModelBuilder = new TestHTMLScanResultReportModelBuilder(trafficLightSupport);

            String sechubReportAsJson = report.getResult();
            SecHubResult sechubResult = SecHubResult.fromJSONString(sechubReportAsJson);

            report.setTrafficLight(trafficLightSupport.calculateTrafficLight(sechubResult));

            ScanSecHubReport scanReport = new ScanSecHubReport(report);
            storeAsJSONFileForDebuggingWhenTempFilesAreKept(JSONConverter.get().toJSON(scanReport, true), this);
            Map<String, Object> tyhmeleafMap = reportModelBuilder.build(scanReport);
            return tyhmeleafMap;
        }

    }
}

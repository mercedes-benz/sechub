package com.daimler.sechub.test.report;

import static com.daimler.sechub.test.report.ReportTestHelper.*;
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

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.docgen.util.TextFileWriter;
import com.daimler.sechub.domain.scan.ReportTransformationResult;
import com.daimler.sechub.domain.scan.TestHTMLScanResultReportModelBuilder;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.domain.scan.report.ScanReportTrafficLightCalculator;
import com.daimler.sechub.domain.scan.report.ScanSecHubReport;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.test.TestUtil;

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
        SARIF, CHECKMARX,
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
            ReportTransformationResult sechubReportResult;
            switch (inputFormat) {
            case CHECKMARX:
                sechubReportResult = transformCheckmarxToSecHubReportResult(sourceReportAsString, sechubJobUUID);
                break;
            case SARIF:
                sechubReportResult = transformSarifToSecHubReportResult(sourceReportAsString, ProductIdentifier.PDS_WEBSCAN, sechubJobUUID);
                break;
            default:
                throw new IllegalStateException("input format not supported:" + inputFormat);

            }
            ScanReportTrafficLightCalculator trafficLightCalculator = new ScanReportTrafficLightCalculator();
            TestHTMLScanResultReportModelBuilder builder = new TestHTMLScanResultReportModelBuilder(trafficLightCalculator);
            ScanReport report = new ScanReport(sechubReportResult.getJobUUID(), "project1");
            report.setResult(sechubJobUUID);

            String sechubReportResultJSON = sechubReportResult.getResult().toJSON();

            report.setResult(sechubReportResultJSON);
            report.setTrafficLight(trafficLightCalculator.calculateTrafficLight(sechubReportResult));

            ScanSecHubReport scanReport = new ScanSecHubReport(report);
            storeAsJSONFileForDebuggingWhenTempFilesAreKept(JSONConverter.get().toJSON(scanReport, true), this);
            Map<String, Object> tyhmeleafMap = builder.build(scanReport);
            return tyhmeleafMap;
        }

    }
}

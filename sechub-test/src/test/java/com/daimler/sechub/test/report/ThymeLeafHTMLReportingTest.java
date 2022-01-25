package com.daimler.sechub.test.report;

import static com.daimler.sechub.test.report.ReportTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
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
 * reporting). For information about CSS changed please read more in `HTMLReportCSSFragementGenerator`
 * 
 * @author Albert Tregnaghi
 *
 */
public class ThymeLeafHTMLReportingTest {
    private static TemplateEngine thymeleafTemplateEngine;
    private TestReportContext testReportContext;

    @Test
    void example1_owasp_zap_sarif_report_is_transformed_to_expected_sechub_report_HTML_with_web_data() throws Exception {
        /* prepare */
        testReportContext = new TestReportContext(1, "owasp_zap_report");
        testReportContext.loadSarifReport();
        testReportContext.sechubJobUUID = "f5fdccc6-45d1-4b42-972c-08ff9ee0dddb";

        Map<String, Object> tyhmeleafMap = createThymeLeafReportData(testReportContext);
        IContext context = new Context(Locale.ENGLISH, tyhmeleafMap);

        /* execute */
        String htmlResult = thymeleafTemplateEngine.process("report/html/scanresult", context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(testReportContext.sechubJobUUID));

        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, testReportContext);
    }

    @Test
    void example2_artifical_data_is_transformed_to_expected_sechub_report_HTML_with_web_data() throws Exception {
        /* prepare */
        testReportContext = new TestReportContext(2, "artificial_data");
        testReportContext.loadSarifReport();
        testReportContext.sechubJobUUID = "f5fdccc6-45d2-4b42-972c-08ff9ee0dddb";

        Map<String, Object> tyhmeleafMap = createThymeLeafReportData(testReportContext);
        IContext context = new Context(Locale.ENGLISH, tyhmeleafMap);

        /* execute */
        String htmlResult = thymeleafTemplateEngine.process("report/html/scanresult", context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(testReportContext.sechubJobUUID));

        storeAsFileForDevelopmentWhenTempFilesAreKept(htmlResult, testReportContext, "html");
    }

    @Test
    void example3_covertiy_sarif_is_transformed_to_expected_sechub_report_HTML_with_code_data() throws Exception {
        /* prepare */
        testReportContext = new TestReportContext(3, "coverity");
        testReportContext.loadSarifReport();
        testReportContext.sechubJobUUID = "f5fdccc6-45d3-4b42-972c-08ff9ee0dddb";

        Map<String, Object> tyhmeleafMap = createThymeLeafReportData(testReportContext);
        IContext context = new Context(Locale.ENGLISH, tyhmeleafMap);

        /* execute */
        String htmlResult = thymeleafTemplateEngine.process("report/html/scanresult", context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(testReportContext.sechubJobUUID));

        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, testReportContext);
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

    private Map<String, Object> createThymeLeafReportData(TestReportContext testReportContext) throws IOException, SecHubExecutionException {
        ReportTransformationResult sechubReportResult = transformSarifToSecHubReportResult(testReportContext.sarifJson, ProductIdentifier.PDS_WEBSCAN,
                testReportContext.sechubJobUUID);
        ScanReportTrafficLightCalculator trafficLightCalculator = new ScanReportTrafficLightCalculator();
        TestHTMLScanResultReportModelBuilder builder = new TestHTMLScanResultReportModelBuilder(trafficLightCalculator);
        ScanReport report = new ScanReport(sechubReportResult.getJobUUID(), "project1");
        report.setResult(testReportContext.sechubJobUUID);

        String sechubReportResultJSON = sechubReportResult.getResult().toJSON();

        report.setResult(sechubReportResultJSON);
        report.setTrafficLight(trafficLightCalculator.calculateTrafficLight(sechubReportResult));

        ScanSecHubReport scanReport = new ScanSecHubReport(report);
        storeAsJSONFileForDebuggingWhenTempFilesAreKept(JSONConverter.get().toJSON(scanReport, true), testReportContext);
        Map<String, Object> tyhmeleafMap = builder.build(scanReport);
        return tyhmeleafMap;
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
        File testFile = TestUtil.createTempFileInBuildFolder("thymeleaf-html-reporttest-" + context.exampleName + "." + fileEnding);
        TextFileWriter writer = new TextFileWriter();
        writer.save(testFile, content);

        System.out.println("Wrote test file to:" + testFile.getAbsolutePath());
    }

    private class TestReportContext {

        private String type;
        private String exampleName;
        private String sechubJobUUID;
        private String sarifJson;

        private TestReportContext(int exampleNumber, String type) {
            this.type = type;
            this.exampleName = "example" + exampleNumber;
        }

        private void loadSarifReport() {
            sarifJson = ReportTestHelper.loadSarifReport(exampleName + "_" + type);
        }
    }
}

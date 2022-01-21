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
 * A special reporting test: Will create "real life" HTML reports very fast (no server or spring boot container start necessary).
 * 
 * @author Albert Tregnaghi
 *
 */
public class ThymeLeafHTMLReportingTest {
    private static TemplateEngine thymeleafTemplateEngine; 
    
    
    @Test
    void example1_owasp_zap_sarif_report_is_transformed_to_expected_sechub_report_HTML_with_web_data() throws Exception {
        /* prepare */
        String reportName = "example1_owasp_zap_report";

        String sarifJson = loadSarifReport(reportName);

        String sechubJobUUID = "f5fdccc6-45d1-4b42-972c-08ff9ee0dddb";

        Map<String, Object> tyhmeleafMap = createThymeLeafReportData(sarifJson, sechubJobUUID);
        IContext context = new Context(Locale.ENGLISH, tyhmeleafMap);

        /* execute */
        String htmlResult = thymeleafTemplateEngine.process("report/html/scanresult", context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(sechubJobUUID));
        
        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, "htmlReportTest-example1.html");
    }
    
    @Test
    void example2_artifical_data_is_transformed_to_expected_sechub_report_HTML_with_web_data() throws Exception {
        /* prepare */
        String reportName = "example2_artificial_data";

        String sarifJson = loadSarifReport(reportName);

        String sechubJobUUID = "f5fdccc6-45d2-4b42-972c-08ff9ee0dddb";

        Map<String, Object> tyhmeleafMap = createThymeLeafReportData(sarifJson, sechubJobUUID);
        IContext context = new Context(Locale.ENGLISH, tyhmeleafMap);

        /* execute */
        String htmlResult = thymeleafTemplateEngine.process("report/html/scanresult", context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(sechubJobUUID));
        
        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, "htmlReportTest-example2.html");
    }
    
    @Test
    void example3_covertiy_sarif_is_transformed_to_expected_sechub_report_HTML_with_code_data() throws Exception {
        /* prepare */
        String reportName = "example3_coverity";

        String sarifJson = loadSarifReport(reportName);

        String sechubJobUUID = "f5fdccc6-45d3-4b42-972c-08ff9ee0dddb";

        Map<String, Object> tyhmeleafMap = createThymeLeafReportData(sarifJson, sechubJobUUID);
        IContext context = new Context(Locale.ENGLISH, tyhmeleafMap);

        /* execute */
        String htmlResult = thymeleafTemplateEngine.process("report/html/scanresult", context);

        /* test */
        assertNotNull(htmlResult);
        assertTrue(htmlResult.contains(sechubJobUUID));
        
        storeAsHTMLFileForReportDesignWhenTempFilesAreKept(htmlResult, "htmlReportTest-example3.html");
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

    private void storeAsHTMLFileForReportDesignWhenTempFilesAreKept(String htmlResult, String outputFileName) throws IOException {
        if (! TestUtil.isKeepingTempfiles()) {
        }
        File testFile = TestUtil.createTempFileInBuildFolder(outputFileName);
        TextFileWriter writer = new TextFileWriter();
        writer.save(testFile, htmlResult);
        
            System.out.println("Wrote test file to:"+testFile.getAbsolutePath());
    }

    private Map<String, Object> createThymeLeafReportData(String sarifJson, String sechubJobUUID) throws IOException, SecHubExecutionException {
        ReportTransformationResult sechubReportResult = transformSarifToSecHubReportResult(sarifJson, ProductIdentifier.PDS_WEBSCAN, sechubJobUUID);
        ScanReportTrafficLightCalculator trafficLightCalculator = new ScanReportTrafficLightCalculator();
        TestHTMLScanResultReportModelBuilder builder = new TestHTMLScanResultReportModelBuilder(trafficLightCalculator);
        ScanReport report = new ScanReport(sechubReportResult.getJobUUID(), "project1");
        report.setResult(sechubJobUUID);
        report.setResult(sechubReportResult.getResult().toJSON());
        report.setTrafficLight(trafficLightCalculator.calculateTrafficLight(sechubReportResult));

        ScanSecHubReport scanReport = new ScanSecHubReport(report);
        Map<String, Object> tyhmeleafMap = builder.build(scanReport);
        return tyhmeleafMap;
    }

}

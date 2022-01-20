package com.daimler.sechub.test.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.domain.scan.ReportTransformationResult;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.daimler.sechub.domain.scan.product.sereco.SerecoProductResultTransformer;
import com.daimler.sechub.domain.scan.product.sereco.TestSerecoProductResultTransformer;
import com.daimler.sechub.integrationtest.TextFileReader;
import com.daimler.sechub.sereco.importer.SarifV1JSONImporter;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

/**
 * A special reporting test: Will test if existing SARIF reports are transformed
 * to wanted SecHub report format correctly (internally SARIF will be
 * transformed to SERECO and after this SERECO data will be transformed to final
 * SecHub report). <br>
 * <br>
 * This is exactly the same call mechanism as SecHub does internally, but without false
 * positive marking and using dedicated SARIF importer version V1 (but we have
 * currently only this, so SecHub will use also this version only). <br>
 * <br>
 * Our normal integrations tests do slow down our builds. Having SARIF already
 * tested by integration tests for SARIF code scans, we test here SARIF
 * details e.g. for web scanning - but much faster.
 * 
 * @author Albert Tregnaghi
 *
 */
public class Sarif2Sereco2SecHubReportTest {

    private static final String REPORT_PATH = "./src/test/resources/report/";

    private static final TextFileReader reader = new TextFileReader();

    SerecoProductResultTransformer transfomer = new TestSerecoProductResultTransformer();
    SarifV1JSONImporter importer = new SarifV1JSONImporter();

    @Test
    void example1_owasp_zap_report_is_transformed_to_expected_sechub_report() throws Exception {
        /* prepare */
        String reportName = "example1_owasp_zap_report";

        String sarifJson = loadSarifReport(reportName);
        String expectedSecHubJson = loadSecHubReport(reportName);

        String sechubJobUUID = "f5fdccc6-45d1-4b41-972c-08ff9ee0dddb";

        /* execute */
        String sechubJson = transformSarifToSecHubReportJSON(sarifJson, ProductIdentifier.PDS_WEBSCAN, sechubJobUUID);

        /* test */
        assertEquals(expectedSecHubJson, sechubJson);
    }

    @Test
    void example2_artifical_data_is_transformed_to_expected_sechub_report() throws Exception {
        /* prepare */
        String reportName = "example2_artificial_data";

        String sarifJson = loadSarifReport(reportName);
        String expectedSecHubJson = loadSecHubReport(reportName);

        String sechubJobUUID = "f5fdccc6-45d1-4b42-972c-08ff9ee0dddb";

        /* execute */
        String sechubJson = transformSarifToSecHubReportJSON(sarifJson, ProductIdentifier.PDS_WEBSCAN, sechubJobUUID);

        /* test */
        assertEquals(expectedSecHubJson, sechubJson);
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private String loadSarifReport(String name) {
        return reader.loadTextFile(new File(REPORT_PATH + "input/" + name + ".sarif.json"));
    }

    private String loadSecHubReport(String name) {
        return reader.loadTextFile(new File(REPORT_PATH + "output/" + name + ".sechub.json"));
    }

    private String transformSarifToSecHubReportJSON(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        ReportTransformationResult result = transformSarifToSecHubReportResult(sarifJson, productIdentifier, sechubJobUUID);

        return JSONConverter.get().toJSON(result, true);
    }

    private ReportTransformationResult transformSarifToSecHubReportResult(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        ProductExecutorConfigInfo info = mock(ProductExecutorConfigInfo.class);
        when(info.getProductIdentifier()).thenReturn(productIdentifier);

        // import from SARIF to SERECO format
        SerecoMetaData serecoMetaData = importer.importResult(sarifJson);
        String serecoJSon = JSONConverter.get().toJSON(serecoMetaData);

        // transform SERECO JSON to SecHub report transformation result
        ProductResult productResult = new ProductResult(UUID.fromString(sechubJobUUID), "project-1", info, serecoJSon);
        ReportTransformationResult result = transfomer.transform(productResult);
        return result;
    }

}

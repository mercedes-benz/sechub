// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test.report;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.domain.scan.ReportTransformationResult;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.product.sereco.SerecoProductResultTransformer;
import com.mercedesbenz.sechub.domain.scan.product.sereco.TestSerecoProductResultTransformer;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.sereco.importer.CheckmarxV1XMLImporter;
import com.mercedesbenz.sechub.sereco.importer.ProductResultImporter;
import com.mercedesbenz.sechub.sereco.importer.SarifV1JSONImporter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.test.TestFileReader;

public class ReportTestHelper {

    private static final String REPORT_PATH = "./src/test/resources/report/";

    private static final SarifV1JSONImporter sarifImporter = new SarifV1JSONImporter();
    private static final CheckmarxV1XMLImporter checkmarxImporter = new CheckmarxV1XMLImporter();
    private static final SerecoProductResultTransformer serecoProductResultTransformer = new TestSerecoProductResultTransformer();

    public static String load3rdPartyReportAsString(String fullName) {
        return TestFileReader.loadTextFile(new File(REPORT_PATH + "input/" + fullName));
    }

    public static String loadSarifReport(String name) {
        return TestFileReader.loadTextFile(new File(REPORT_PATH + "input/" + name + ".sarif.json"));
    }

    public static String loadSecHubReportFileTemplate(String name) {
        return TestFileReader.loadTextFile(new File(REPORT_PATH + "input/" + name + ".sechub-template.json"));
    }

    public static String loadExpectedSecHubReportOutputFile(String name) {
        return TestFileReader.loadTextFile(new File(REPORT_PATH + "output/" + name + ".sechub.json"));
    }

    public static String transformSarifToSecHubReportJSON(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        ScanReport scanReport = transformSarifToScanReport(sarifJson, productIdentifier, sechubJobUUID);

        return scanReport.getResult();
    }

    public static String transformCheckmarxToSecHubReportJSON(String checkmarxXML, String sechubJobUUID) throws IOException, SecHubExecutionException {
        ScanReport scanReport = transformSarifToScanReport(checkmarxXML, ProductIdentifier.CHECKMARX, sechubJobUUID);

        return scanReport.getResult();
    }

    public static ScanReport transformCheckmarxToSecHubReportResult(String xml, String sechubJobUUID) throws IOException, SecHubExecutionException {
        return simulateCreateScanReportService(xml, ProductIdentifier.CHECKMARX, sechubJobUUID, checkmarxImporter, true);
    }

    public static ScanReport transformSarifToScanReport(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        return simulateCreateScanReportService(sarifJson, productIdentifier, sechubJobUUID, sarifImporter, true);
    }

    private static ScanReport simulateCreateScanReportService(String xml, ProductIdentifier productIdentifier, String sechubJobUUID,
            ProductResultImporter productResultImporter, boolean hasProductResults) throws IOException, SecHubExecutionException {

        ProductExecutorConfigInfo info = mock(ProductExecutorConfigInfo.class);
        when(info.getProductIdentifier()).thenReturn(productIdentifier);

        // import from SARIF to SERECO format
        SerecoMetaData serecoMetaData = productResultImporter.importResult(xml);
        String serecoJSon = JSONConverter.get().toJSON(serecoMetaData);

        // transform SERECO JSON to SecHub report transformation result
        ProductResult productResult = new ProductResult(UUID.fromString(sechubJobUUID), "project-1", info, serecoJSon);
        ReportTransformationResult result = serecoProductResultTransformer.transform(productResult);
        result.setAtLeastOneRealProductResultContained(hasProductResults);

        String transformationResultAsJson = result.toJSON();
        ScanReport scanReport = new ScanReport(UUID.fromString(sechubJobUUID), "project1");
        scanReport.setResult(transformationResultAsJson);

        return scanReport;
    }

}

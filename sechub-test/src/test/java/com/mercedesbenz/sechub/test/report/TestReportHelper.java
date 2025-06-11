// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test.report;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.ReportTransformationResult;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.product.sereco.SerecoProductResultTransformer;
import com.mercedesbenz.sechub.domain.scan.product.sereco.TestSerecoProductResultTransformer;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportResultType;
import com.mercedesbenz.sechub.sereco.importer.CheckmarxV1XMLImporter;
import com.mercedesbenz.sechub.sereco.importer.ProductResultImporter;
import com.mercedesbenz.sechub.sereco.importer.SarifV1JSONImporter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestUtil;

public class TestReportHelper {

    private static final String REPORT_PATH = "./src/test/resources/report/";

    private static final SarifV1JSONImporter sarifImporter = new TestSarifV1JSONImporter();
    private static final CheckmarxV1XMLImporter checkmarxImporter = new CheckmarxV1XMLImporter();
    private static final SerecoProductResultTransformer serecoProductResultTransformer = new TestSerecoProductResultTransformer();

    private static final Logger LOG = LoggerFactory.getLogger(TestReportHelper.class);

    public static String load3rdPartyReportAsString(String fullName) {
        return TestFileReader.readTextFromFile(new File(REPORT_PATH + "input/" + fullName));
    }

    public static String loadSarifReport(String name) {
        return TestFileReader.readTextFromFile(new File(REPORT_PATH + "input/" + name + ".sarif.json"));
    }

    public static String loadSecHubReportFileTemplate(String name) {
        return TestFileReader.readTextFromFile(new File(REPORT_PATH + "input/" + name + ".sechub-template.json"));
    }

    public static String loadExpectedSecHubReportOutputFile(String name) {
        return TestFileReader.readTextFromFile(new File(REPORT_PATH + "output/" + name + ".sechub.json"));
    }

    public static String transformSarifToSecHubReportJSON(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        ScanReport scanReport = transformToScanReport(sarifJson, productIdentifier, sechubJobUUID);

        return scanReport.getResult();
    }

    public static String transformCheckmarxToSecHubReportJSON(String checkmarxXML, String sechubJobUUID) throws IOException, SecHubExecutionException {
        ScanReport scanReport = transformToScanReport(checkmarxXML, ProductIdentifier.CHECKMARX, sechubJobUUID);

        return scanReport.getResult();
    }

    public static ScanReport transformSecHubReportTemplateToResult(String sechubReportJsonTemplate, String sechubJobUUID) {
        ScanReport report = new ScanReport(null, null);
        report.setResultType(ScanReportResultType.MODEL);
        String sechubReport = sechubReportJsonTemplate.replace("__SECHUB_JOB_UUID__", sechubJobUUID);
        report.setResult(sechubReport);

        return report;
    }

    public static ScanReport transformCheckmarxToSecHubReportResult(String xml, String sechubJobUUID) throws IOException, SecHubExecutionException {
        return simulateCreateScanReportService(xml, ProductIdentifier.CHECKMARX, sechubJobUUID, checkmarxImporter, ScanType.CODE_SCAN, true);
    }

    public static ScanReport transformToScanReport(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {

        ScanType scanType = productIdentifier == null ? ScanType.WEB_SCAN : productIdentifier.getType();

        return simulateCreateScanReportService(sarifJson, productIdentifier, sechubJobUUID, sarifImporter, scanType, true);
    }

    private static ScanReport simulateCreateScanReportService(String report, ProductIdentifier productIdentifier, String sechubJobUUID,
            ProductResultImporter productResultImporter, ScanType scanType, boolean hasProductResults) throws IOException, SecHubExecutionException {

        ProductExecutorConfigInfo info = mock(ProductExecutorConfigInfo.class);
        when(info.getProductIdentifier()).thenReturn(productIdentifier);

        // import from SARIF to SERECO format
        SerecoMetaData serecoMetaData = productResultImporter.importResult(report, scanType);
        String serecoJSon = JSONConverter.get().toJSON(serecoMetaData);

        // transform SERECO JSON to SecHub report transformation result
        ProductResult productResult = new ProductResult(UUID.fromString(sechubJobUUID), "project-1", info, serecoJSon);
        ReportTransformationResult result = serecoProductResultTransformer.transform(productResult);
        result.setAtLeastOneRealProductResultContained(hasProductResults);

        String transformationResultAsJson = result.getModel().toJSON();
        if (TestUtil.isTraceEnabled()) {
            LOG.info("Transformed sechub report is:\n{}", result.getModel().toFormattedJSON());
        }
        ScanReport scanReport = new ScanReport(UUID.fromString(sechubJobUUID), "project1");
        scanReport.setResultType(ScanReportResultType.MODEL);
        scanReport.setResult(transformationResultAsJson);

        return scanReport;
    }

}

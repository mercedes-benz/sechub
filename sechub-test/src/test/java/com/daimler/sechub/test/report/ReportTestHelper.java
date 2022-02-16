package com.daimler.sechub.test.report;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.domain.scan.ReportTransformationResult;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.daimler.sechub.domain.scan.product.sereco.SerecoProductResultTransformer;
import com.daimler.sechub.domain.scan.product.sereco.TestSerecoProductResultTransformer;
import com.daimler.sechub.integrationtest.TextFileReader;
import com.daimler.sechub.sereco.importer.CheckmarxV1XMLImporter;
import com.daimler.sechub.sereco.importer.ProductResultImporter;
import com.daimler.sechub.sereco.importer.SarifV1JSONImporter;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

public class ReportTestHelper {

    private static final String REPORT_PATH = "./src/test/resources/report/";
    private static final TextFileReader reader = new TextFileReader();

    private static final SarifV1JSONImporter sarifImporter = new SarifV1JSONImporter();
    private static final CheckmarxV1XMLImporter checkmarxImporter = new CheckmarxV1XMLImporter();
    private static final SerecoProductResultTransformer transfomer = new TestSerecoProductResultTransformer();

    public static String load3rdPartyReportAsString(String fullName) {
        return reader.loadTextFile(new File(REPORT_PATH + "input/" + fullName));
    }

    public static String loadSarifReport(String name) {
        return reader.loadTextFile(new File(REPORT_PATH + "input/" + name + ".sarif.json"));
    }

    public static String loadSecHubReport(String name) {
        return reader.loadTextFile(new File(REPORT_PATH + "output/" + name + ".sechub.json"));
    }

    public static String transformCheckmarxToSecHubReportJSON(String checkmarxXML, String sechubJobUUID) throws IOException, SecHubExecutionException {
        ReportTransformationResult result = transformSarifToSecHubReportResult(checkmarxXML, ProductIdentifier.CHECKMARX, sechubJobUUID);

        return JSONConverter.get().toJSON(result, true);
    }

    public static ReportTransformationResult transformCheckmarxToSecHubReportResult(String xml, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        return transform(xml, ProductIdentifier.CHECKMARX, sechubJobUUID, checkmarxImporter);
    }

    public static ReportTransformationResult transformSarifToSecHubReportResult(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        return transform(sarifJson, productIdentifier, sechubJobUUID, sarifImporter);
    }

    private static ReportTransformationResult transform(String xml, ProductIdentifier productIdentifier, String sechubJobUUID, ProductResultImporter importer)
            throws IOException, SecHubExecutionException {
        ProductExecutorConfigInfo info = mock(ProductExecutorConfigInfo.class);
        when(info.getProductIdentifier()).thenReturn(productIdentifier);

        // import from SARIF to SERECO format
        SerecoMetaData serecoMetaData = importer.importResult(xml);
        String serecoJSon = JSONConverter.get().toJSON(serecoMetaData);

        // transform SERECO JSON to SecHub report transformation result
        ProductResult productResult = new ProductResult(UUID.fromString(sechubJobUUID), "project-1", info, serecoJSon);
        ReportTransformationResult result = transfomer.transform(productResult);
        return result;
    }

    public static String transformSarifToSecHubReportJSON(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        ReportTransformationResult result = transformSarifToSecHubReportResult(sarifJson, productIdentifier, sechubJobUUID);

        return JSONConverter.get().toJSON(result, true);
    }

}

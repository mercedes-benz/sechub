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
import com.daimler.sechub.sereco.importer.SarifV1JSONImporter;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

public class ReportTestHelper {

    private static final String REPORT_PATH = "./src/test/resources/report/";
    private static final TextFileReader reader = new TextFileReader();

    private static final SarifV1JSONImporter importer = new SarifV1JSONImporter();
    private static final SerecoProductResultTransformer transfomer = new TestSerecoProductResultTransformer();

    public static String loadSarifReport(String name) {
        return reader.loadTextFile(new File(REPORT_PATH + "input/" + name + ".sarif.json"));
    }

    public static String loadSecHubReport(String name) {
        return reader.loadTextFile(new File(REPORT_PATH + "output/" + name + ".sechub.json"));
    }

    public static String transformSarifToSecHubReportJSON(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
            throws IOException, SecHubExecutionException {
        ReportTransformationResult result = transformSarifToSecHubReportResult(sarifJson, productIdentifier, sechubJobUUID);

        return JSONConverter.get().toJSON(result, true);
    }

    public static ReportTransformationResult transformSarifToSecHubReportResult(String sarifJson, ProductIdentifier productIdentifier, String sechubJobUUID)
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

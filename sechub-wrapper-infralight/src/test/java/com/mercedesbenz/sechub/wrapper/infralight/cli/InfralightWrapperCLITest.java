package com.mercedesbenz.sechub.wrapper.infralight.cli;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanProductData;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanResult;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralighProductImportService;

class InfralightWrapperCLITest {

    private InfralightWrapperCLI cli;
    private InfralightWrapperEnvironment environment;
    private InfralighProductImportService scanService;
    private TextFileWriter textFileWriter;

    @BeforeEach
    void beforeEach() throws Exception {
        cli = new InfralightWrapperCLI();

        environment = mock();
        scanService = mock();
        textFileWriter = mock();

        cli.environment = environment;
        cli.scanService = scanService;
        cli.textFileWriter = textFileWriter;

    }

    @Test
    void cli_call_textwriter_at_the_end_and_writes_scan_result_as_json_to_result_file() throws Exception {
        /* prepare */
        GenericInfrascanResult result = new GenericInfrascanResult();
        GenericInfrascanProductData productData = new GenericInfrascanProductData();
        productData.setProduct("test-product");
        
        GenericInfrascanFinding testFinding = new GenericInfrascanFinding();
        testFinding.setCweId(3105);
        testFinding.setName("Test weakness");
        testFinding.setDescription("Just for testing");

        productData.getFindings().add(testFinding);
        result.getProducts().add(productData );
        when(scanService.importGenericInfrascanResult(any())).thenReturn(result);

        when(environment.getInfrascanProductsOutputFolder()).thenReturn("/workspace-folder/output");
        when(environment.getPdsResultFile()).thenReturn("/workspace/result.txt");

        /* execute */
        cli.run();

        /* test */
        ArgumentCaptor<File> resultFileCaptor = ArgumentCaptor.forClass(File.class);
        ArgumentCaptor<String> resultFileCcontentStringCaptor = ArgumentCaptor.forClass(String.class);

        verify(textFileWriter).writeTextToFile(resultFileCaptor.capture(), resultFileCcontentStringCaptor.capture(), eq(true));

        File file = resultFileCaptor.getValue();
        assertThat(file).isNotNull();
        assertThat(file.getPath()).isEqualTo("/workspace/result.txt");

        String content = resultFileCcontentStringCaptor.getValue();
        
        String expected ="""
                {"products":[{"product":"test-product","findings":[{"cweId":3105,"name":"Test weakness","description":"Just for testing"}]}],"type":"generic-infrascan-result"}
                """.trim();
        
        assertThat(content).isNotNull().isNotBlank().contains(expected);

    }

}

package com.mercedesbenz.sechub.wrapper.infralight.cli;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralighProductImportService;

import de.jcup.sarif_2_1_0.model.Run;
import de.jcup.sarif_2_1_0.model.SarifSchema210;

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
        SarifSchema210 schema = new SarifSchema210();
        Run run1 = new Run();
        run1.setLanguage("test");
        schema.getRuns().add(run1);
        when(scanService.importProductResultsAsSarif(any())).thenReturn(schema);

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
        assertThat(content).isNotNull().isNotBlank().contains("{\"runs\":[{\"language\":\"test\"}]}"); // test schema data contained

    }

}

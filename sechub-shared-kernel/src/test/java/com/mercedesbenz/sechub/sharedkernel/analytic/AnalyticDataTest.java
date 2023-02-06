package com.mercedesbenz.sechub.sharedkernel.analytic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;

class AnalyticDataTest {

    @Test
    void empty_analytic_data_can_be_transformed_to_json() {
        AnalyticData analyticData = new AnalyticData();

        /* execute */
        String json = analyticData.toJSON();

        /* test */
        assertNotNull(json);
    }

    @Test
    void analytic_data_with_code_parts_can_be_transformed_to_json_and_back() {
        /* prepare */
        CodeAnalyticData codeAnalyticData = new CodeAnalyticData(); 

        codeAnalyticData.setFilesForLanguage("something1", 12L);
        codeAnalyticData.setFilesForLanguage("something2", 34L);
        codeAnalyticData.setLinesOfCodeForLanguage("something1", 100L);
        codeAnalyticData.setLinesOfCodeForLanguage("something2", 200L);

        codeAnalyticData.getProductInfo().setName("test-product");
        codeAnalyticData.getProductInfo().setVersion("0.2.3");
        
        AnalyticData analyticData = new AnalyticData();
        analyticData.setCodeAnalyticData(codeAnalyticData);
        
        /* execute */
        String json = analyticData.toFormattedJSON();
        AnalyticData result = JSONConverter.get().fromJSON(AnalyticData.class, json);

        /* test */
        Optional<CodeAnalyticData> opt = result.getCodeAnalyticData();
        assertTrue(opt.isPresent());
        
        CodeAnalyticData codeAnalyticData2 = opt.get();
        assertEquals(12L, codeAnalyticData2.getFilesForLanguage("something1"));
        assertEquals(34L, codeAnalyticData2.getFilesForLanguage("something2"));
        assertEquals(100L, codeAnalyticData2.getLinesOfCodeForLanguage("something1"));
        assertEquals(200L, codeAnalyticData2.getLinesOfCodeForLanguage("something2"));
        assertEquals("test-product", codeAnalyticData2.getProductInfo().getName());
        assertEquals("0.2.3", codeAnalyticData2.getProductInfo().getVersion());
    }

}

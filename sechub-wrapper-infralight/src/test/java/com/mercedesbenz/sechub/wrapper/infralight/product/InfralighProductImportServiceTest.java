package com.mercedesbenz.sechub.wrapper.infralight.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanProductData;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanResult;

class InfralighProductImportServiceTest {

    private static final Path TEST_PATH = Path.of("./");
    private InfralighProductImportService importServiceToTest;
    private InfralightProductImportStringDataProvider dataProvider;

    @BeforeEach
    void beforeEach() {
        importServiceToTest = new InfralighProductImportService();
        
        dataProvider=mock();
        importServiceToTest.dataProvider=dataProvider;
    }
    
    @Test
    void one_importer_set_is_used_and_results_from_importer_are_inside_result() throws Exception{
        
        InfralightProductImporter importer1 = mock();
        importServiceToTest.productImporters.add(importer1);
        
        when(importer1.getProductName()).thenReturn("importer1");
        when(importer1.getImportFileName()).thenReturn("test-import1.txt");
        
        String data = "testdata";
        when(dataProvider.getStringDataForImporter(importer1, TEST_PATH)).thenReturn(data);
        
        GenericInfrascanFinding finding1 = new GenericInfrascanFinding();
        finding1.setName("finding1");
        GenericInfrascanFinding finding2 = new GenericInfrascanFinding();
        finding2.setName("finding2");
        
        when(importer1.startImport(data)).thenReturn(List.of(finding1,finding2));
        
        /* execute */
        GenericInfrascanResult result = importServiceToTest.importGenericInfrascanResult(TEST_PATH);
        
        verify(dataProvider).getStringDataForImporter(importer1, TEST_PATH);
        
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(1);
        
        GenericInfrascanProductData product1 = result.getProducts().iterator().next();
        assertThat(product1.getProduct()).isEqualTo("importer1");
        assertThat(product1.getFindings()).hasSize(2).contains(finding1,finding2);
    }

}

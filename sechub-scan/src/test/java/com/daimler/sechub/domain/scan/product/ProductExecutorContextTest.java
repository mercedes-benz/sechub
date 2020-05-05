package com.daimler.sechub.domain.scan.product;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterMetaData;

public class ProductExecutorContextTest {
    ProductExecutorContext contextToTest;
    List<ProductResult> formerResults;
    ProductExecutorCallback callback;
    AdapterMetaDataConverter converter = new AdapterMetaDataConverter();
    @Before
    public void before() {
        formerResults = new ArrayList<ProductResult>();
        callback = mock(ProductExecutorCallback.class);
        when(callback.getMetaDataConverter()).thenReturn(converter);
    }

    @Test
    public void inital_ProductExecutorContext_does_call_setCurrentProductResult_with_null_when_no_former_results_available() {
        /* execute */
        contextToTest = new ProductExecutorContext(formerResults, callback);
        
        /* test */
        verify(callback).setCurrentProductResult(null);
    }

    @Test
    public void inital_ProductExecutorContext_set_first_result_at_callback_setCurrentProductResult() {
        /* prepare */
        ProductResult result1 = new ProductResult();
        ProductResult result2 = new ProductResult();
        formerResults.add(result1);
        formerResults.add(result2);
        /* execute */
        contextToTest = new ProductExecutorContext(formerResults, callback);

        /* test */
        verify(callback).setCurrentProductResult(result1);
    }
    
    @Test
    public void formerResults_1_result_with_metadata__results_in_metadata_from_callback_getMetaDataOrNull() {
        /* prepare */
        AdapterMetaData metaData = new AdapterMetaData();
        
        contextToTest = new ProductExecutorContext(formerResults, callback);
        when(callback.getMetaDataOrNull()).thenReturn(metaData);
        
        /* execute */
        AdapterMetaData result = contextToTest.getCurrentMetaDataOrNull();

        /* test */
        verify(callback).getMetaDataOrNull();
        assertEquals(result,metaData);
    }
    
    @Test
    public void useFirstFormerResultHavingMetaData_finds_product_by_metadata() {
        /* prepare */
        ProductResult result1 = new ProductResult();
        ProductResult result2 = new ProductResult();
        ProductResult result3 = new ProductResult();
        formerResults.add(result1);
        formerResults.add(result2);
        formerResults.add(null);
        formerResults.add(result3);
        
        AdapterMetaData metaData1 = new AdapterMetaData();
        metaData1.setValue("test.key", "xyz");
        AdapterMetaData metaData2 = new AdapterMetaData();
        metaData2.setValue("test.key", "abc");
        result1.setMetaData(converter.convertToJSONOrNull(metaData1));
        result2.setMetaData(null);
        result3.setMetaData(converter.convertToJSONOrNull(metaData2));
        
        contextToTest = new ProductExecutorContext(formerResults, callback);
        
        /* execute */
        contextToTest.useFirstFormerResultHavingMetaData("test.key", "abc");

        /* test */
        verify(callback).setCurrentProductResult(result3);
    }
    

}

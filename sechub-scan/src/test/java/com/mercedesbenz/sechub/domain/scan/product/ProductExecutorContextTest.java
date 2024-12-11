// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;

public class ProductExecutorContextTest {
    ProductExecutorContext contextToTest;
    List<ProductResult> formerResults;
    ProductExecutorCallback callback;
    AdapterMetaDataConverter converter = new AdapterMetaDataConverter();
    private ProductExecutorConfig config;

    @Before
    public void before() {
        formerResults = new ArrayList<>();

        callback = mock(ProductExecutorCallback.class);
        config = mock(ProductExecutorConfig.class);
        when(callback.getMetaDataConverter()).thenReturn(converter);

        /* execute */
        contextToTest = new ProductExecutorContext(config, formerResults);
        contextToTest.callback = callback;

    }

    @Test
    public void useFirstFormerResult_set_current_product_result_with_null_when_no_former_result_available() {

        contextToTest.useFirstFormerResult();

        /* test */
        verify(callback).setCurrentProductResult(null);
    }

    @Test
    public void useFirstFormerResult_set_current_product_result_with_former_result() {
        /* prepare */
        ProductResult result1 = new ProductResult();
        ProductResult result2 = new ProductResult();
        formerResults.add(result1);
        formerResults.add(result2);

        /* execute */
        contextToTest.useFirstFormerResult();

        /* test */
        verify(callback).setCurrentProductResult(result1);
    }

    @Test
    public void formerResults_1_result_with_metadata__results_in_metadata_from_callback_getMetaDataOrNull() {
        /* prepare */
        AdapterMetaData metaData = new AdapterMetaData();

        when(callback.getMetaDataOrNull()).thenReturn(metaData);

        /* execute */
        AdapterMetaData result = contextToTest.getCurrentMetaDataOrNull();

        /* test */
        verify(callback).getMetaDataOrNull();
        assertThat(result).isEqualTo(metaData);
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

        contextToTest.useFirstFormerResult();
        verify(callback, times(1)).setCurrentProductResult(result3); // by constructor...

        /* execute */
        contextToTest.useFirstFormerResultHavingMetaData("test.key", "abc");

        /* test */
        verify(callback, times(2)).setCurrentProductResult(result3); // by last call
    }

}

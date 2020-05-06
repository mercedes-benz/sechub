package com.daimler.sechub.domain.scan.product;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterMetaData;

public class AdapterMetaDataConverterTest {

    
    

    private AdapterMetaDataConverter converterToTest;


    @Before
    public void before() throws Exception {
        converterToTest = new AdapterMetaDataConverter();
    }

    @Test
    public void metadata_null_transformed_to_string_is_null() {
        assertNull(converterToTest.convertToJSONOrNull(null));
    }
    
    @Test
    public void metadataString_null_transformed_to_metadata_is_null() {
        assertNull(converterToTest.convertToMetaDataOrNull(null));
    }
    
    @Test
    public void values_transformed_to_string_are_transformed_back_correctly() {
        /* prepare*/
        AdapterMetaData metaData = new AdapterMetaData();
        metaData.setValue("key1", "value1");
        metaData.setValue("sub1.key2", "value1.2");
        
        /* execute */
        String text = converterToTest.convertToJSONOrNull(metaData);
        AdapterMetaData metaDataback = converterToTest.convertToMetaDataOrNull(text);

        /* test */
        assertEquals("value1",metaDataback.getValue("key1"));
        assertEquals("value1.2",metaDataback.getValue("sub1.key2"));
    }
    

}

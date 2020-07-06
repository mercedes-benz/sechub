package com.daimler.sechub.pds.execution;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PDSKeyToEnvConverterTest {

    private PDSKeyToEnvConverter converterToTest;

    @Before
    public void before() throws Exception {
        converterToTest = new PDSKeyToEnvConverter();
    }

    @Test
    public void abc_DOT_def_DOT_ghi_is_converted_to_ABC_DEF_GHI() {
        assertEquals("ABC_DEF_GHI",  converterToTest.convertKeyToEnv("abc.def.ghi"));
    }
    
    @Test
    public void empty_keeps_empty() {
        assertEquals("",  converterToTest.convertKeyToEnv(""));
    }
    
    @Test
    public void null_keeps_null() {
        assertEquals(null,  converterToTest.convertKeyToEnv(null));
    }
    
    @Test
    public void abc_becomes_ABC() {
        assertEquals("ABC",  converterToTest.convertKeyToEnv("abc"));
    }

}

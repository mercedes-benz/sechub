// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FilenameVariantConverterTest {

    private FilenameVariantConverter converterToTest;

    @Before
    public void before() {
        converterToTest = new FilenameVariantConverter();
    }

    @Test
    public void getFilenameWithoutVariant_when_variants_defined() {
        assertEquals("filename1.txt", converterToTest.getFilenameWithoutVariant("filename1-variant_variant1.txt"));
        assertEquals("filename21.pdf", converterToTest.getFilenameWithoutVariant("filename21-variant_variantXY.pdf"));
        assertEquals("filename22.xyz.pdf", converterToTest.getFilenameWithoutVariant("filename22-variant_variant21.xyz.pdf"));
    }

    @Test
    public void getFilenameWithoutVariant_when_no_variant_defined() {
        assertEquals("filename1.txt", converterToTest.getFilenameWithoutVariant("filename1.txt"));
    }

    @Test
    public void filename_test_to_variantfilename_variant_is_variant1() {
        assertEquals("filename1-variant_variant1.txt", converterToTest.toVariantFileName("filename1.txt", "variant1"));
    }

    @Test
    public void filename_test_to_variantfilename_variant_is_null() {
        assertEquals("filename1.txt", converterToTest.toVariantFileName("filename1.txt", null));
    }

    @Test
    public void filename_test_to_variantfilename_variant_is_empty() {
        assertEquals("filename1.txt", converterToTest.toVariantFileName("filename1.txt", ""));
    }

    @Test
    public void other_identifier() {
        converterToTest = new FilenameVariantConverter("_other-identifier_");
        assertEquals("filename1_other-identifier_variantX.txt", converterToTest.toVariantFileName("filename1.txt", "variantX"));
        assertEquals("variantY", converterToTest.getVariantFromFilename("filename1_other-identifier_variantY.txt"));
    }

    @Test
    public void filename_without_variant_has_variant_empty() {
        assertEquals("", converterToTest.getVariantFromFilename("filename1.txt"));
    }

    @Test
    public void filename_with_variant1_has_variant1() {
        assertEquals("variant1", converterToTest.getVariantFromFilename("filename1-variant_variant1.txt"));
        assertEquals("variant1", converterToTest.getVariantFromFilename("filename1-variant_variant1.pdf"));
    }

    @Test
    public void filename_with_incorrect_variant1_has_variant_empty() {
        assertEquals("", converterToTest.getVariantFromFilename("filename1-variant_txt"));
        assertEquals("", converterToTest.getVariantFromFilename("filename1-variant_.txt"));
        assertEquals("", converterToTest.getVariantFromFilename("filename1-variant_variant1"));
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ScanType;

class ScanTypeBasedProductExecutorFilterTest {

    @Test
    void when_given_type_is_null_an_illegal_arg_exception_is_thrown() {
        assertThrows(IllegalArgumentException.class, () -> new ScanTypeBasedProductExecutorFilter(null));
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void when_2_product_executors_have_wanted_scantype_only_these_two_are_returned_by_filter_others_are_ignored(ScanType typeToTest) {
        /* prepare */
        ScanTypeBasedProductExecutorFilter filterToTest = new ScanTypeBasedProductExecutorFilter(typeToTest);

        List<ProductExecutor> list = new ArrayList<>();

        /* add 2 expected */
        ProductExecutor expected1 = mock(ProductExecutor.class);
        list.add(expected1);
        when(expected1.getScanType()).thenReturn(typeToTest);

        ProductExecutor expected2 = mock(ProductExecutor.class);
        list.add(expected2);
        when(expected2.getScanType()).thenReturn(typeToTest);

        addUnwantedProductExecutors(typeToTest, list);

        /* execute */
        List<ProductExecutor> result = filterToTest.filter(list);

        /* test */
        assertEquals(2, result.size());
        assertTrue(result.contains(expected1));
        assertTrue(result.contains(expected2));

    }

    private void addUnwantedProductExecutors(ScanType wantedType, List<ProductExecutor> list) {
        for (ScanType type : ScanType.values()) {
            if (type.equals(wantedType)) {
                continue;
            }
            ProductExecutor unwanted = mock(ProductExecutor.class);
            list.add(unwanted);
        }
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void when_no_product_executor_has_wanted_scantype_only_an_empty_list_is_returned(ScanType typeToTest) {
        /* prepare */
        ScanTypeBasedProductExecutorFilter filterToTest = new ScanTypeBasedProductExecutorFilter(typeToTest);

        List<ProductExecutor> list = new ArrayList<>();

        addUnwantedProductExecutors(typeToTest, list);

        /* execute */
        List<ProductExecutor> result = filterToTest.filter(list);

        /* test */
        assertTrue(result.isEmpty());

    }

}

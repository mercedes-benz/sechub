// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.docgen.reflections.Reflections;

public class MustBeDocumentedDataCollectorTest {

    private MustBeDocumentedDataCollector collectorToTest;
    private Reflections reflections;

    @Before
    public void before() throws Exception {
        reflections = mock(Reflections.class);
        collectorToTest = new MustBeDocumentedDataCollector(reflections);
    }

    @Test
    public void collect_does_fetch_mustbedocumented__on_method_field_and_type_by_reflections() throws Exception {

        /* execute */
        collectorToTest.collect();

        /* test */
        verify(reflections).getMethodsAnnotatedWith(MustBeDocumented.class);
        verify(reflections).getFieldsAnnotatedWith(MustBeDocumented.class);
        verify(reflections).getTypesAnnotatedWith(MustBeDocumented.class);
    }

}

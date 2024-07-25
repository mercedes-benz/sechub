// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry;
import com.mercedesbenz.sechub.docgen.DocAnnotationData;
import com.mercedesbenz.sechub.docgen.util.ClasspathDataCollector;

public class SystemPropertiesDescriptionGeneratorTest {

    private SystemPropertiesDescriptionGenerator generatorToTest;
    private ClasspathDataCollector collector;
    private SecureEnvironmentVariableKeyValueRegistry registry;

    @Before
    public void before() throws Exception {

        registry = mock(SecureEnvironmentVariableKeyValueRegistry.class);
        generatorToTest = new SystemPropertiesDescriptionGenerator();

        collector = mock(ClasspathDataCollector.class);
    }

    @Test
    public void even_when_list_is_null_result_is_not_null_but_empty() throws Exception {

        /* prepare */
        when(collector.fetchMustBeDocumentParts()).thenReturn(null);

        /* execute */
        String generated = generatorToTest.generate(null,registry);

        /* test */
        assertNotNull(generated);
        assertTrue(generated.isEmpty());
    }

    @Test
    public void when_param_is_empty_list_result_is_not_null_but_empty() throws Exception {

        /* execute */
        String generated = generatorToTest.generate(Collections.emptyList(), registry);

        /* test */
        assertNotNull(generated);
        assertTrue(generated.isEmpty());
    }

    @Test
    public void when_list_contains_one_entry_but_not_with_spring_value_1_result_is_empty() throws Exception {

        /* prepare */
        DocAnnotationData a = new DocAnnotationData();
        List<DocAnnotationData> list = new ArrayList<>();
        list.add(a);

        /* execute */
        String generated = generatorToTest.generate(list, registry);

        /* test */
        assertNotNull(generated);
        assertTrue(generated.isEmpty());
    }

    @Test
    public void when_collector_returns_one_entry_with_spring_value_1_a_table_is_build() throws Exception {

        /* prepare */
        DocAnnotationData a = new DocAnnotationData();
        a.springValue = "${something}";
        a.description = "the description";
        List<DocAnnotationData> list = new ArrayList<>();
        list.add(a);

        /* execute */
        String generated = generatorToTest.generate(list, registry);

        /* test */
        assertNotNull(generated);
        assertFalse(generated.isEmpty());

        assertTrue(generated.indexOf("|===") != -1);
    }

}

// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.annotation.Scheduled;

import com.mercedesbenz.sechub.docgen.DocAnnotationData;
import com.mercedesbenz.sechub.docgen.util.ClasspathDataCollector;

public class ScheduleDescriptionGeneratorTest {

    private ScheduleDescriptionGenerator generatorToTest;
    private ClasspathDataCollector collector;

    @Before
    public void before() throws Exception {
        generatorToTest = new ScheduleDescriptionGenerator();

        collector = mock(ClasspathDataCollector.class);
    }

    @Test
    public void even_when_collector_returns_null_result_is_not_null_but_empty() throws Exception {

        /* prepare */
        when(collector.fetchMustBeDocumentParts()).thenReturn(null);

        /* execute */
        String generated = generatorToTest.generate(collector);

        /* test */
        assertNotNull(generated);
        assertTrue(generated.isEmpty());
    }

    @Test
    public void when_collector_returns_empty_list_result_is_not_null_but_empty() throws Exception {

        /* prepare */
        when(collector.fetchMustBeDocumentParts()).thenReturn(Collections.emptyList());

        /* execute */
        String generated = generatorToTest.generate(collector);

        /* test */
        assertNotNull(generated);
        assertTrue(generated.isEmpty());
    }

    @Test
    public void when_collector_returns_one_entry_but_not_with_spring_value_1_result_is_empty() throws Exception {

        /* prepare */
        DocAnnotationData a = new DocAnnotationData();
        List<DocAnnotationData> list = new ArrayList<>();
        list.add(a);

        when(collector.fetchMustBeDocumentParts()).thenReturn(list);

        /* execute */
        String generated = generatorToTest.generate(collector);

        /* test */
        assertNotNull(generated);
        assertTrue(generated.isEmpty());
    }

    @Test
    public void when_collector_returns_one_entry_with_spring_value_1_result_is_empty() throws Exception {

        /* prepare */
        DocAnnotationData a = new DocAnnotationData();
        a.springValue = "${something}";
        a.description = "the description";
        List<DocAnnotationData> list = new ArrayList<>();
        list.add(a);

        when(collector.fetchMustBeDocumentParts()).thenReturn(list);

        /* execute */
        String generated = generatorToTest.generate(collector);

        /* test */
        assertNotNull(generated);
        assertTrue(generated.isEmpty());
    }

    @Test
    public void when_collector_returns_one_entry_with_spring_scheduled_a_table_is_build() throws Exception {

        /* prepare */
        DocAnnotationData a = new DocAnnotationData();
        a.springScheduled = mock(Scheduled.class);
        when(a.springScheduled.cron()).thenReturn("cronjob");
        List<DocAnnotationData> list = new ArrayList<>();
        list.add(a);

        when(collector.fetchMustBeDocumentParts()).thenReturn(list);

        /* execute */
        String generated = generatorToTest.generate(collector);

        /* test */
        assertNotNull(generated);
        assertFalse(generated.isEmpty());

        assertTrue(generated.indexOf("|===") != -1);
    }

    @Test
    public void when_collector_returns_one_entry_with_spring_scheduled_fixedDelayString_a_table_is_build() throws Exception {

        /* prepare */
        DocAnnotationData a = new DocAnnotationData();
        a.springScheduled = mock(Scheduled.class);
        when(a.springScheduled.fixedDelayString()).thenReturn("fixedDelayString");
        List<DocAnnotationData> list = new ArrayList<>();
        list.add(a);

        when(collector.fetchMustBeDocumentParts()).thenReturn(list);

        /* execute */
        String generated = generatorToTest.generate(collector);

        /* test */
        assertNotNull(generated);
        assertFalse(generated.isEmpty());

        assertTrue(generated.indexOf("|===") != -1);
    }

    @Test
    public void when_collector_returns_one_entry_with_spring_scheduled_initialDelayString_a_table_is_build() throws Exception {

        /* prepare */
        DocAnnotationData a = new DocAnnotationData();
        a.springScheduled = mock(Scheduled.class);
        when(a.springScheduled.initialDelayString()).thenReturn("initialDelayString");
        List<DocAnnotationData> list = new ArrayList<>();
        list.add(a);

        when(collector.fetchMustBeDocumentParts()).thenReturn(list);

        /* execute */
        String generated = generatorToTest.generate(collector);

        /* test */
        assertNotNull(generated);
        assertFalse(generated.isEmpty());

        assertTrue(generated.indexOf("|===") != -1);
    }

}

// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.daimler.sechub.docgen.DocAnnotationData;

public class ClasspathDataCollectorTest {
    @Test
    public void mockconfiguration_annotation_data_can_be_fetched_and_at_least_one_spring_value_documentation_found() {
        ClasspathDataCollector collector = new ClasspathDataCollector();

        List<DocAnnotationData> data = collector.fetchMockAdapterSpringValueDocumentationParts();
        assertNotNull(data);
        assertFalse(data.isEmpty());

        Iterator<DocAnnotationData> iterator = data.iterator();

        boolean atLeastOneDescribedSpringValueFound = false;
        while (iterator.hasNext()) {
            DocAnnotationData d = iterator.next();
            assertNotNull(d);
            if (!atLeastOneDescribedSpringValueFound) {
                if (d.springValue != null) {
                    if (d.description != null && !d.description.isEmpty()) {
                        atLeastOneDescribedSpringValueFound = true;
                    }
                }
            }
        }
        assertTrue(atLeastOneDescribedSpringValueFound);
    }

    @Test
    public void mustbedocumented_annotation_data_can_be_fetched_and_at_least_one_spring_value_documentation_found() {
        ClasspathDataCollector collector = new ClasspathDataCollector();

        List<DocAnnotationData> data = collector.fetchMustBeDocumentParts();
        assertNotNull(data);
        assertFalse(data.isEmpty());

        Iterator<DocAnnotationData> iterator = data.iterator();

        boolean atLeastOneDescribedSpringValueFound = false;
        while (iterator.hasNext()) {
            DocAnnotationData d = iterator.next();
            assertNotNull(d);
            if (!atLeastOneDescribedSpringValueFound) {
                if (d.springValue != null) {
                    if (d.description != null && !d.description.isEmpty()) {
                        atLeastOneDescribedSpringValueFound = true;
                    }
                }
            }
        }
        assertTrue(atLeastOneDescribedSpringValueFound);
    }

    @Test
    public void mustbedocumented_annotation_data_can_be_fetched_and_at_least_one_spring_schedule_documentation_found() {
        ClasspathDataCollector collector = new ClasspathDataCollector();

        List<DocAnnotationData> data = collector.fetchMustBeDocumentParts();
        assertNotNull(data);
        assertFalse(data.isEmpty());

        Iterator<DocAnnotationData> iterator = data.iterator();

        boolean atLeastOneDescribedSpringScheduleFound = false;
        while (iterator.hasNext()) {
            DocAnnotationData d = iterator.next();
            assertNotNull(d);
            if (!atLeastOneDescribedSpringScheduleFound) {
                if (d.springScheduled != null) {
                    if (d.description != null && !d.description.isEmpty()) {
                        atLeastOneDescribedSpringScheduleFound = true;
                    }
                }
            }
        }
        assertTrue(atLeastOneDescribedSpringScheduleFound);
    }

}

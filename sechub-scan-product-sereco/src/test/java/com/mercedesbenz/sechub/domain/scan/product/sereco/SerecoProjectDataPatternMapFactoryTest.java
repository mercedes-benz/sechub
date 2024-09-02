// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveJobData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;

class SerecoProjectDataPatternMapFactoryTest {

    private SerecoProjectDataPatternMapFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new SerecoProjectDataPatternMapFactory();
    }

    /*-------------------------------------TEST PATTERN CREATION-------------------------------------------*/

    @Test
    void factory_throws_illegal_argument_exception_if_list_of_false_positives_is_null() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> factoryToTest.create(null));
    }

    @Test
    void factory_returns_empty_map_if_list_of_false_positives_is_null() {
        /* execute */
        Map<String, Pattern> map = factoryToTest.create(new ArrayList<>());

        /* test */
        assertTrue(map.isEmpty());
    }

    @Test
    void factory_returns_empty_map_if_list_of_false_positives_only_contains_jobData_entries() {
        /* prepare */
        List<FalsePositiveEntry> falsePositives = new ArrayList<>();

        FalsePositiveEntry entry1 = new FalsePositiveEntry();
        entry1.setJobData(new FalsePositiveJobData());
        falsePositives.add(entry1);

        FalsePositiveEntry entry2 = new FalsePositiveEntry();
        entry2.setJobData(new FalsePositiveJobData());
        falsePositives.add(entry2);

        /* execute */
        Map<String, Pattern> map = factoryToTest.create(falsePositives);

        /* test */
        assertTrue(map.isEmpty());
    }

    @Test
    void for_projectData_webscan_factory_returns_expected_map_with_patterns_for_each_mandatory_that_parameter() {
        /* prepare */
        String projectDataId = "unique";
        String urlPattern = "https://myapp-*.example.com:80*/rest/*/search?*";

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(projectDataId, urlPattern);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        assertEquals("^\\Qhttps://myapp-\\E.*\\Q.example.com:80\\E.*\\Q/rest/\\E.*\\Q/search?\\E.*$", patternMap.get(projectDataId).toString());

    }

    /*---------------------------TEST THE CREATED URL PATH PATTERNS FOR EXPECTED BEHAVIOUR---------------------------------*/

    @Test
    void url_path_patterns_without_wildcards_match_expected_strings() {
        /* prepare */
        String projectDataId = "unique";
        String urlPattern = "https://myapp-*.example.com:80*/rest/*/search?*";

        List<FalsePositiveEntry> falsePositives = createFalsePositiveEntriesForProjectDataWebscan(projectDataId, urlPattern);

        /* execute */
        Map<String, Pattern> patternMap = factoryToTest.create(falsePositives);

        /* test */
        Pattern pattern = patternMap.get(projectDataId);

        assertTrue(pattern.matcher("https://myapp-dev.example.com:80*/rest/profile/search?q=test").matches());

        assertFalse(pattern.matcher("https://myapp.example.com:80/rest/search?").matches());
    }

    /*----------------------------------------------HELPER------------------------------------------------------*/

    private List<FalsePositiveEntry> createFalsePositiveEntriesForProjectDataWebscan(String id, String urlPattern) {
        List<FalsePositiveEntry> falsePositives = new ArrayList<>();

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();
        projectData.setId(id);
        WebscanFalsePositiveProjectData webScan = new WebscanFalsePositiveProjectData();
        webScan.setUrlPattern(urlPattern);
        projectData.setWebScan(webScan);

        FalsePositiveEntry entry = new FalsePositiveEntry();
        entry.setProjectData(projectData);
        falsePositives.add(entry);
        return falsePositives;
    }

}

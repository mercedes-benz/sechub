package com.mercedesbenz.sechub.sharedkernel.analytic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CodeAnalyticDataTest {

    private CodeAnalyticData dataToTest;

    @BeforeEach
    void beforeEach() {
        dataToTest = new CodeAnalyticData();
    }

    @Test
    void initial_product_data_not_null() {
        assertNotNull(dataToTest.getProductInfo());
    }

    @CsvSource({ "java,10", "go,5" })
    @ParameterizedTest
    void calculateFilesForAllLanguages_setting_files_for_a_language_is_inside_result(String language, long files) {
        /* execute */
        dataToTest.setFilesForLanguage(language, files);

        /* test */
        assertEquals(files, dataToTest.calculateFilesForAllLanguages());

    }

    @CsvSource({ "java,100", "go,50" })
    @ParameterizedTest
    void calculateLinesOfCodeForAllLanguages_setting_lines_for_a_language_is_inside_result(String language, long linesOfCode) {
        /* execute */
        dataToTest.setLinesOfCodeForLanguage(language, linesOfCode);

        /* test */
        assertEquals(linesOfCode, dataToTest.calculateLinesOfCodeForAllLanguages());

    }

    @CsvSource({ "java,10,go,5" })
    @ParameterizedTest
    void calculateFilesForAllLanguages_setting_files_for_two_languages_inside_result(String language1, long files1, String language2, long files2) {
        /* execute */
        dataToTest.setFilesForLanguage(language1, files1);
        dataToTest.setFilesForLanguage(language2, files2);

        /* test */
        assertEquals(files1 + files2, dataToTest.calculateFilesForAllLanguages());

    }

    @CsvSource({ "java,100,go,50" })
    @ParameterizedTest
    void calculateLinesOfCodeForAllLanguages_setting_lines_for_two_languages_is_inside_result(String language1, long linesOfCode1, String language2,
            long linesOfCode2) {
        /* execute */
        dataToTest.setLinesOfCodeForLanguage(language1, linesOfCode1);
        dataToTest.setLinesOfCodeForLanguage(language2, linesOfCode2);

        /* test */
        assertEquals(linesOfCode1 + linesOfCode2, dataToTest.calculateLinesOfCodeForAllLanguages());

    }

    @Test
    void calculateLinesOfCodeForAllLanguages_setting_lines_for_a_language_null_is_inside_result_but_language_is_emptxy() {
        /* execute */
        dataToTest.setLinesOfCodeForLanguage(null, 4711L);

        /* test */
        assertEquals(4711L, dataToTest.calculateLinesOfCodeForAllLanguages());
        assertEquals(1, dataToTest.getLanguages().size());
        assertTrue(dataToTest.getLanguages().contains(""));
    }

    @Test
    void calculateFilesForAllLanguages_setting_lines_for_a_language_null_is_inside_result_but_language_is_emptxy() {
        /* execute */
        dataToTest.setFilesForLanguage(null, 5711L);

        /* test */
        assertEquals(5711L, dataToTest.calculateFilesForAllLanguages());
        assertEquals(1, dataToTest.getLanguages().size());
        assertTrue(dataToTest.getLanguages().contains(""));
    }

    @Test
    void mixed_settings_calculation_works() {
        /* execute */
        dataToTest.setFilesForLanguage("java", 711L);
        dataToTest.setFilesForLanguage("go", 705L);
        dataToTest.setLinesOfCodeForLanguage("java", 1705L);
        dataToTest.setLinesOfCodeForLanguage("go", 2705L);

        /* test */
        assertEquals(1416L, dataToTest.calculateFilesForAllLanguages());
        assertEquals(4410L, dataToTest.calculateLinesOfCodeForAllLanguages());
        assertEquals(2, dataToTest.getLanguages().size());
        assertTrue(dataToTest.getLanguages().contains("java"));
        assertTrue(dataToTest.getLanguages().contains("go"));
    }

    @Test
    void no_settings_calculation_works() {
        assertEquals(0L, dataToTest.calculateFilesForAllLanguages());
        assertEquals(0L, dataToTest.calculateLinesOfCodeForAllLanguages());
        assertEquals(0, dataToTest.getLanguages().size());
    }

    @CsvSource({ "java,100", "go,50" })
    @ParameterizedTest
    void setting_files_for_a_language_rembers_language_and_files(String language, long files) {
        /* execute */
        dataToTest.setFilesForLanguage(language, files);

        /* test */
        Set<String> languagesFound = dataToTest.getLanguages();

        assertTrue(languagesFound.contains(language));
        assertEquals(1, languagesFound.size());

        assertEquals(files, dataToTest.getFilesForLanguage(language));

    }

    @CsvSource({ "java,1000", "go,335" })
    @ParameterizedTest
    void setting_lines_for_a_language_rembers_language_and_lines(String language, long lines) {
        /* execute */
        dataToTest.setLinesOfCodeForLanguage(language, lines);

        /* test */
        Set<String> languagesFound = dataToTest.getLanguages();

        assertTrue(languagesFound.contains(language));
        assertEquals(1, languagesFound.size());

        assertEquals(lines, dataToTest.getLinesOfCodeForLanguage(language));

    }

}

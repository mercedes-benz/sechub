package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;

class SecHubFileStructureDataProviderIncludeExcludeFilterTest {

    private SecHubFileStructureDataProviderIncludeExcludeFilter filterToTest;

    @BeforeEach
    void beforeEach() {
        filterToTest = new SecHubFileStructureDataProviderIncludeExcludeFilter();
    }

    @Test
    void path_null_is_always_filtered_even_when_provider_is_null() {
        assertTrue(filterToTest.isFiltered(null, null));
    }

    @ParameterizedTest
    @CsvSource({ "abc", "bla.go", "/somewhere/test.txt" })
    @EmptySource
    void path_is_never_filtered_when_provider_is_null(String path) {
        assertFalse(filterToTest.isFiltered(path, null));
    }

    @ParameterizedTest(name = "path \"{0}\" with exclude pattern \"{1}\" - isFiltered() must return {2}")
    /* @formatter:off */
    @CsvSource({
        "abc,abc,true",
        "test.txt,test.txt,true",
        "test.txt,test.*,true",
        "test2.txt,test.*,false",
        "abc,a*,true",
        "bla.go,*.go,true",
        "bla.GO,*.go,true",
        "/somewhere/test.txt,*.txt,true"})
    /* @formatter:on */
    void path_is_filtered_excludes_set(String path, String exclude, boolean expectedToBeFiltered) {
        /* prepare */
        SecHubFileStructureDataProvider provider = createMockProviderWithExcludes(exclude);

        /* execute */
        boolean filtered = filterToTest.isFiltered(path, provider);

        /* test */
        assertEquals(expectedToBeFiltered, filtered);
    }

    @ParameterizedTest(name = "path \"{0}\" with include pattern \"{1}\" - isFiltered() must return {2}")
    /* @formatter:off */
    @CsvSource({
        "abc,abc,false",
        "abc,abd,true",
        "test1.txt,test1.txt,false",
        "test2.txt,test1.txt,true",
        "abc,a*,false",
        "bla.go,*.go,false",
        "bla.GO,*.go,false",
        "bla.java,*.go,true"})
    /* @formatter:on */
    void path_is_filtered_includes_set(String path, String included, boolean expectedToBeFiltered) {
        /* prepare */
        SecHubFileStructureDataProvider provider = createMockProviderWithIncludes(included);

        /* execute */
        boolean filtered = filterToTest.isFiltered(path, provider);

        /* test */
        assertEquals(expectedToBeFiltered, filtered);
    }

    @Test
    void similar_to_integrationtest_combined_filtering() {
        /* prepare */
        Set<String> includes = new LinkedHashSet<>();
        includes.add("*included-folder/*");

        Set<String> excludes = new LinkedHashSet<>();
        excludes.add("*excluded-folder/**");
        excludes.add("*excluded*.txt");

        SecHubFileStructureDataProvider provider = createMockProvider(excludes, includes);

        /* execute + test */
        assertFiltered("excluded-folder/file-x-1.txt", provider);
        assertFiltered("files-b/included-folder/excluded-1.txt", provider);

        assertNotFiltered("included-folder/file-b-1.txt", provider);
        assertNotFiltered("files-b/included-folder/file-b-1.txt", provider);
    }

    private void assertFiltered(String path, SecHubFileStructureDataProvider provider) {
        assertFiltered(true, path, provider);
    }

    private void assertNotFiltered(String path, SecHubFileStructureDataProvider provider) {
        assertFiltered(false, path, provider);
    }

    private void assertFiltered(boolean expectedToBeFiltered, String path, SecHubFileStructureDataProvider provider) {
        boolean filtered = filterToTest.isFiltered(path, provider);

        if (filtered != expectedToBeFiltered) {
            fail("Path: '" + path + "' must be filtered: " + expectedToBeFiltered + ", but was: " + filtered);
        }
    }

    @ParameterizedTest(name = "path \"{0}\" with include pattern \"{1}\" and exclude pattern \"{2}\" - isFiltered() must return {3}")
    /* @formatter:off */
    @CsvSource({
        "abc,abc,abd,false",
        "abc,abc,abc,true",
        "abc,abd,abd,true",
        "abc,abd,abc,true",
        "abd,abd,abc,false",
        "abc,abc,abc,true",
        "/home/mypath/something.txt,*.txt,**some*.txt,true",
        "/home/mypath/something.txt,*.txt,**no*.txt,false",
        "/home/mypath/something.txt,*.txt,/home/mypath/some*.txt,true",
        "/home/otherpath/something.txt,*.txt,/home/mypath/some*.txt,false",
        "/home/otherpath/something.txt,*.txt,/home/**/some*.txt,true",
        "/home/otherpath/something.txt,/home/**/some*.txt,,false",
        "/home/otherpath/something.txt,,/home/**/some*.txt,true",
        "/home/otherpath/something.txt,,,false",
        "/home/otherpath/subpath/something.txt,*.txt,/home/**/some*.txt,true",
        "test1.txt,test1.txt,test.*,false",
        "test1.txt,test1.txt,test1.*,true",
        "abc,a*,b*,false",
        "abc,a*,a*,true",
        "abc,b*,b*,true",
        "bla.go,*.go,blub.go,false",
        "bla.go,*.go,bla.go,true",
        "bla.java,*.go,bla.go,true",
        "bla.java,*.go,bla.go,true",
        "bla.java,*.go,bla.java,true",
        "bla.java,*.java,bla.go,false"})
    /* @formatter:on */
    void path_is_filtered_includes_and_excludes_set(String path, String included, String excluded, boolean expectedToBeFiltered) {
        /* prepare */

        Set<String> excludes = new LinkedHashSet<>(Arrays.asList(excluded));
        Set<String> includes = new LinkedHashSet<>(Arrays.asList(included));

        SecHubFileStructureDataProvider provider = createMockProvider(excludes, includes);

        /* execute */
        boolean filtered = filterToTest.isFiltered(path, provider);

        /* test */
        assertEquals(expectedToBeFiltered, filtered);
    }

    private SecHubFileStructureDataProvider createMockProviderWithIncludes(String... included) {
        Set<String> excludes = new LinkedHashSet<>();
        Set<String> includes = new LinkedHashSet<>(Arrays.asList(included));

        return createMockProvider(excludes, includes);
    }

    private SecHubFileStructureDataProvider createMockProviderWithExcludes(String... excluded) {
        Set<String> excludes = new LinkedHashSet<>(Arrays.asList(excluded));
        Set<String> includes = new LinkedHashSet<>();

        return createMockProvider(excludes, includes);
    }

    private SecHubFileStructureDataProvider createMockProvider(Set<String> excludes, Set<String> includes) {
        SecHubFileStructureDataProvider provider = mock(SecHubFileStructureDataProvider.class);
        when(provider.getUnmodifiableExcludeFilePatterns()).thenReturn(excludes);
        when(provider.getUnmodifiableIncludeFilePatterns()).thenReturn(includes);
        return provider;
    }

}

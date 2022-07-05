// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ArchiveTransformationDataFactoryTest {

    private MutableSecHubFileStructureDataProvider dataProvider;
    private ArchiveTransformationDataFactory factoryToTest;
    private SecHubFileStructureDataProviderIncludeExcludeFilter includeExcludeFilter;

    @BeforeEach
    void beforeEach() {
        dataProvider = new MutableSecHubFileStructureDataProvider();
        includeExcludeFilter = mock(SecHubFileStructureDataProviderIncludeExcludeFilter.class, "includeExcludeFilterMock");

        factoryToTest = new ArchiveTransformationDataFactory();
        factoryToTest.includeExcludefilter = includeExcludeFilter;
    }

    @Test
    void new_factory_has_include_exclude_filter_set() {
        assertNotNull(new ArchiveTransformationDataFactory().includeExcludefilter);
    }

    @Test
    void factory_create_method_calls_include_exclude_filter_when_creating_data_result_when_rootfolder_accepted() {
        /* prepare */
        String path = "/some/path/data.txt";
        dataProvider.setRootFolderAccepted(true);

        /* execute */
        factoryToTest.create(dataProvider, path);

        /* test */
        verify(includeExcludeFilter).isFiltered(path, dataProvider);
    }

    @ParameterizedTest(name = "When include/exclude filter returns:{0}, the root path will be accepted:{1}")
    /* @formatter:off */
    @CsvSource({
        "true,false",
        "false,true"
    })
    /* @formatter:on */
    void factory_sets_accepted_like_include_exclude_filter_result_when_root_folder_accepted_with_rootpath(boolean filtered, boolean accepted) {
        /* prepare */
        String rootPath = "/some/path/data.txt";
        dataProvider.setRootFolderAccepted(true);
        when(includeExcludeFilter.isFiltered(rootPath, dataProvider)).thenReturn(filtered);

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, rootPath);

        /* test */
        assertEquals(accepted, result.isAccepted());
    }

    @ParameterizedTest(name = "When include/exclude filter returns:{0}, the wellknown data section path will be accepted:{1}")
    /* @formatter:off */
    @CsvSource({
        "true,false",
        "false,true"
    })
    /* @formatter:on */
    void factory_sets_accepted_like_include_exclude_filter_result_when_accepted_data_section(boolean filtered, boolean accepted) {
        /* prepare */
        String dataSectionPath = ArchiveConstants.DATA_SECTION_FOLDER + "name-1/some/path/data.txt";
        dataProvider.setRootFolderAccepted(false);
        dataProvider.addAcceptedReferenceNames(Collections.singleton("name-1"));
        when(includeExcludeFilter.isFiltered("some/path/data.txt", dataProvider)).thenReturn(filtered);

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, dataSectionPath);

        /* test */
        assertEquals(accepted, result.isAccepted());
    }

    @ParameterizedTest(name = "When include/exclude filter returns:{0}, the unknown data section path will be accepted:{1}")
    /* @formatter:off */
    @CsvSource({
        "true,false",
        "false,false"
    })
    /* @formatter:on */
    void factory_sets_not_accepted_when_data_section_not_accepted_no_matter_what_filter_wants(boolean filtered, boolean accepted) {
        /* prepare */
        String dataSectionPath = ArchiveConstants.DATA_SECTION_FOLDER + "unknown-name/some/path/data.txt";
        dataProvider.setRootFolderAccepted(false);
        dataProvider.addAcceptedReferenceNames(Collections.singleton("other-name"));
        when(includeExcludeFilter.isFiltered(dataSectionPath, dataProvider)).thenReturn(filtered);

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, dataSectionPath);

        /* test */
        assertEquals(accepted, result.isAccepted());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "pathElement", "/path/other/file.txt", "__data__/unique-name1/file1.txt" })
    void nothing_accepted_or_wanted_when_dataProvider_has_no_accepted_pathes_and_no_root_path_set(String path) {
        /* prepare */
        dataProvider.setRootFolderAccepted(false);

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, path);

        /* test */
        assertFalse(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "pathElement", "/path/other/file.txt", "other/__data__", "other/__data__/file1.txt", "__data__2",
            "__DATA__/the_parent_is_uppercased.txt" })
    void rootfolder_accepted__path_does_not_start_with_data_section(String path) {
        /* prepare */
        dataProvider.setRootFolderAccepted(true);

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, path);

        /* test */
        assertTrue(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "__data__", "__data__/unique-name1/file1.txt", "__data__/file1.txt", "__data__/" })
    void rootfolder_accepted__path_start_with_data_section__no_accepted_reference_name_defined(String path) {
        /* prepare */
        dataProvider.setRootFolderAccepted(true);

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, path);

        /* test */
        assertFalse(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "__data__/name-2/file1.txt", "__data__/name-2/subfolder2/file1.txt" })
    void rootfolder_not_accepted__path_start_with_data_section__accepted_name1_defined_but_name2_used_in_pathes(String path) {
        /* prepare */
        dataProvider.setRootFolderAccepted(false);
        dataProvider.addAcceptedReferenceNames(Collections.singleton("name-1"));

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, path);

        /* test */
        assertFalse(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "__data__/name-12/file1.txt", "__data__/name-12/subfolder2/file1.txt" })
    void rootfolder_not_accepted__path_start_with_data_section__accepted_name1_defined_but_name12_used_in_pathes(String path) {
        /* prepare */
        dataProvider.setRootFolderAccepted(false);
        dataProvider.addAcceptedReferenceNames(Collections.singleton("name-1"));

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, path);

        /* test */
        assertFalse(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "__data__/name-1/file1.txt", "__data__/name-1/subfolder2/file1.txt" })
    void rootfolder_not_accepted__path_start_with_data_section__accepted_name1_defined_and_used_in_pathes(String path) {
        /* prepare */
        dataProvider.setRootFolderAccepted(false);
        dataProvider.addAcceptedReferenceNames(Collections.singleton("name-1"));

        /* execute */
        ArchiveTransformationData result = factoryToTest.create(dataProvider, path);

        /* test */
        assertTrue(result.isAccepted());
        assertTrue(result.isPathChangeWanted());

        // Paths are reduced/transformed as expected:
        assertEquals(path.substring("__data__/name-1/".length()), result.getChangedPath());
    }

}

package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ArchiveTransformationDataFactoryTest {

    private SecHubFileStructureConfiguration configuration;

    @BeforeEach
    void beforeEach() {
        configuration = new SecHubFileStructureConfiguration();

    }

    @ParameterizedTest
    @ValueSource(strings = { "", "pathElement", "/path/other/file.txt", "__data__/unique-name1/file1.txt" })
    void nothing_accepted_or_wanted_when_configuration_has_no_accepted_pathes_and_no_root_path_set(String path) {
        /* prepare */
        configuration.setRootFolderAccepted(false);

        /* execute */
        ArchiveTransformationData result = ArchiveTransformationDataFactory.create(configuration, path);

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
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveTransformationData result = ArchiveTransformationDataFactory.create(configuration, path);

        /* test */
        assertTrue(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "__data__", "__data__/unique-name1/file1.txt", "__data__/file1.txt", "__data__/" })
    void rootfolder_accepted__path_start_with_data_section__no_accepted_reference_name_defined(String path) {
        /* prepare */
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveTransformationData result = ArchiveTransformationDataFactory.create(configuration, path);

        /* test */
        assertFalse(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "__data__/name-2/file1.txt", "__data__/name-2/subfolder2/file1.txt" })
    void rootfolder_not_accepted__path_start_with_data_section__accepted_name1_defined_but_name2_used_in_pathes(String path) {
        /* prepare */
        configuration.setRootFolderAccepted(false);
        configuration.addAcceptedReferenceNames(Collections.singleton("name-1"));

        /* execute */
        ArchiveTransformationData result = ArchiveTransformationDataFactory.create(configuration, path);

        /* test */
        assertFalse(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "__data__/name-12/file1.txt", "__data__/name-12/subfolder2/file1.txt" })
    void rootfolder_not_accepted__path_start_with_data_section__accepted_name1_defined_but_name12_used_in_pathes(String path) {
        /* prepare */
        configuration.setRootFolderAccepted(false);
        configuration.addAcceptedReferenceNames(Collections.singleton("name-1"));

        /* execute */
        ArchiveTransformationData result = ArchiveTransformationDataFactory.create(configuration, path);

        /* test */
        assertFalse(result.isAccepted());
        assertFalse(result.isPathChangeWanted());

        assertNull(result.getChangedPath());
    }

    @ParameterizedTest
    @ValueSource(strings = { "__data__/name-1/file1.txt", "__data__/name-1/subfolder2/file1.txt" })
    void rootfolder_not_accepted__path_start_with_data_section__accepted_name1_defined_and_used_in_pathes(String path) {
        /* prepare */
        configuration.setRootFolderAccepted(false);
        configuration.addAcceptedReferenceNames(Collections.singleton("name-1"));

        /* execute */
        ArchiveTransformationData result = ArchiveTransformationDataFactory.create(configuration, path);

        /* test */
        assertTrue(result.isAccepted());
        assertTrue(result.isPathChangeWanted());

        // Paths are reduced/transformed as expected:
        assertEquals(path.substring("__data__/name-1/".length()), result.getChangedPath());
    }

}

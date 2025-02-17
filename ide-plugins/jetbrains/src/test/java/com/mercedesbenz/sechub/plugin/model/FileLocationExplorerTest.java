// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.model;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static com.mercedesbenz.sechub.plugin.model.TestResourceHelper.getEnsuredTestPath;
import static org.junit.Assert.*;

public class FileLocationExplorerTest {

    private FileLocationExplorer explorerToTest;

    @Before
    public void before() {
        explorerToTest = new FileLocationExplorer();
    }

    /* ----------------------------------------------- */
    /* ------------- REGEXP creation tests ----------- */
    /* ----------------------------------------------- */

    @Test
    public void path_contains_plusplus_is_correctly_handled() throws Exception{

        /* prepare */
        String location = "src/main/C++/os_injection.cpp";

        /* execute */
        String osSpecificLocation = explorerToTest.convertLocationOSSpecificRegExp(location, "\\");

        /* test */
        // remark: \Q start Quoting inside java regexp, \E ends it
        assertEquals("\\Qsrc\\main\\C++\\os_injection.cpp\\E",osSpecificLocation);
    }

    @Test
    public void location_converted_from_unix_to_windows_style_when_separator_is_backslash() throws Exception{

        /* prepare */
        String location = "src/main/java/com/example/Test1.java";

        /* execute */
        String osSpecificLocation = explorerToTest.convertLocationOSSpecificRegExp(location, "\\");

        /* test */
        assertEquals("\\Qsrc\\main\\java\\com\\example\\Test1.java\\E",osSpecificLocation);
    }

    @Test
    public void location_unconverted_from_unix_when_separator_is_slash() throws Exception{

        /* prepare */
        String location = "src/main/java/com/example/Test1.java";

        /* execute */
        String osSpecificLocation = explorerToTest.convertLocationOSSpecificRegExp(location, "/");

        /* test */
        assertEquals("\\Qsrc/main/java/com/example/Test1.java\\E",osSpecificLocation);
    }

    /* ----------------------------------------------- */
    /* ------------- SearchFor implementation tests -- */
    /* ----------------------------------------------- */
    @Test
    public void scenario1_projects_TestMe_java_found() throws Exception {
        /* prepare */
        Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
        Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
        Path expectedFile = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/TestMe.java");

        explorerToTest.getSearchFolders().add(project1);
        explorerToTest.getSearchFolders().add(project2);

        /* execute */
        String locationString = "TestMe.java";
        List<Path> found = explorerToTest.searchFor(locationString);

        /* test */
        assertEquals(1,found.size());
        assertEquals(expectedFile, found.get(0));
    }

    @Test
    public void scenario1_projects_example_TestMe_java_found() throws Exception {
        /* prepare */
        Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
        Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
        Path expectedFile = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/TestMe.java");

        explorerToTest.getSearchFolders().add(project1);
        explorerToTest.getSearchFolders().add(project2);

        /* execute */
        String locationString = "example/TestMe.java";
        List<Path> found = explorerToTest.searchFor(locationString);

        /* test */
        assertEquals(1,found.size());
        assertEquals(expectedFile, found.get(0));
    }

    @Test
    public void scenario1_projects_source_TestMe_c_found() throws Exception {
        /* prepare */
        Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
        Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
        Path expectedFile = getEnsuredTestPath("explorer/scenario1/project2/source/TestMe.c");

        explorerToTest.getSearchFolders().add(project1);
        explorerToTest.getSearchFolders().add(project2);

        /* execute */
        String locationString = "source/TestMe.c";
        List<Path> found = explorerToTest.searchFor(locationString);

        /* test */
        assertEquals(1,found.size());
        assertEquals(expectedFile, found.get(0));
    }

    @Test
    public void scenario1_projects_TestMe_c_found() throws Exception {
        /* prepare */
        Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
        Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
        Path expectedFile = getEnsuredTestPath("explorer/scenario1/project2/source/TestMe.c");

        explorerToTest.getSearchFolders().add(project1);
        explorerToTest.getSearchFolders().add(project2);

        /* execute */
        String locationString = "TestMe.c";
        List<Path> found = explorerToTest.searchFor(locationString);

        /* test */
        assertEquals(1,found.size());
        assertEquals(expectedFile, found.get(0));
    }

    @Test
    public void scenario1_projects_com_example_TestMe_java_found() throws Exception {
        /* prepare */
        Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
        Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
        Path expectedFile = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/TestMe.java");

        explorerToTest.getSearchFolders().add(project1);
        explorerToTest.getSearchFolders().add(project2);

        /* execute */
        String locationString = "com/example/TestMe.java";
        List<Path> found = explorerToTest.searchFor(locationString);

        /* test */
        assertEquals(1,found.size());
        assertEquals(expectedFile, found.get(0));
    }

    @Test
    public void scenario1_projects_SameName_java_found() throws Exception {
        /* prepare */
        Path project1 = getEnsuredTestPath("explorer/scenario1/project1");
        Path project2 = getEnsuredTestPath("explorer/scenario1/project2");
        Path expectedFile1 = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/SameName.java");
        Path expectedFile2 = getEnsuredTestPath("explorer/scenario1/project1/src/test/java/com/example/subpackage/SameName.java");
        Path unExpectedFile3 = getEnsuredTestPath("SameName.java");

        explorerToTest.getSearchFolders().add(project1);
        explorerToTest.getSearchFolders().add(project2);

        /* execute */
        String locationString = "SameName.java";
        List<Path> found = explorerToTest.searchFor(locationString);

        /* test */
        assertEquals(2,found.size());
        assertTrue(found.contains(expectedFile1));
        assertTrue(found.contains(expectedFile2));
        assertFalse(found.contains(unExpectedFile3));
    }

    @Test
    public void scenario2_projects_cpp_os_injection_cpp_found() throws Exception {
        /* prepare */
        Path project1 = getEnsuredTestPath("explorer/scenario2/project1");
        Path expectedFile = getEnsuredTestPath("explorer/scenario2/project1/Code/c++/os_injection.cpp");

        explorerToTest.getSearchFolders().add(project1);

        /* execute */
        String locationString = "os_injection.cpp";
        List<Path> found = explorerToTest.searchFor(locationString);

        /* test */
        assertEquals(1,found.size());
        assertEquals(expectedFile, found.get(0));
    }


}

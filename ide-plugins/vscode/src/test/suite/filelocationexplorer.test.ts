// SPDX-License-Identifier: MIT
import * as assert from 'assert';
import * as path from 'path';
import * as fs from 'fs';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import { FileLocationExplorer } from '../../fileLocationExplorer';

suite('FileLocationExplorer Test Suite', () => {

	var explorerToTest: FileLocationExplorer;

	test('scenario1_projects_TestMe_java_found', () => {
		/* prepare */
		explorerToTest = new FileLocationExplorer();
		
		const project1 = getEnsuredTestPath("explorer/scenario1/project1");
		const project2 = getEnsuredTestPath("explorer/scenario1/project2");
		const expectedFilePath = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/TestMe.java");

		explorerToTest.searchFolders.add(project1);
		explorerToTest.searchFolders.add(project2);

		/* execute */
		const locationString = "example/TestMe.java";
		var found = explorerToTest.searchFor(locationString);

		/* test */
		assert.strictEqual(1, found.size,"Not expected size of locations returned");
		const firstValue = found.values().next().value;
		assert.deepStrictEqual(firstValue,expectedFilePath,"must be expected result path");
	});

	test('scenario1_projects_source_TestMe_c_found', () => {
		/* prepare */
		explorerToTest = new FileLocationExplorer();
		
		const project1 = getEnsuredTestPath("explorer/scenario1/project1");
		const project2 = getEnsuredTestPath("explorer/scenario1/project2");
		const expectedFilePath = getEnsuredTestPath("explorer/scenario1/project2/source/TestMe.c");

		explorerToTest.searchFolders.add(project1);
		explorerToTest.searchFolders.add(project2);

		/* execute */
		const locationString = "source/TestMe.c";
		var found = explorerToTest.searchFor(locationString);

		/* test */
		assert.strictEqual(1, found.size,"Not expected size of locations returned");
		const firstValue = found.values().next().value;
		assert.deepStrictEqual(firstValue,expectedFilePath,"must be expected result path");

	});

	test('scenario1_projects_TestMe_c_found', () => {
		/* prepare */
		explorerToTest = new FileLocationExplorer();
		
		const project1 = getEnsuredTestPath("explorer/scenario1/project1");
		const project2 = getEnsuredTestPath("explorer/scenario1/project2");
		const expectedFilePath = getEnsuredTestPath("explorer/scenario1/project2/source/TestMe.c");

		explorerToTest.searchFolders.add(project1);
		explorerToTest.searchFolders.add(project2);

		/* execute */
		const locationString = "TestMe.c";
		var found = explorerToTest.searchFor(locationString);

		/* test */
		assert.strictEqual(1, found.size,"Not expected size of locations returned");
		const firstValue = found.values().next().value;
		assert.deepStrictEqual(firstValue,expectedFilePath,"must be expected result path");

	});

	test('scenario1_projects_com_example_TestMe_java_found', () => {
		/* prepare */
		explorerToTest = new FileLocationExplorer();
		
		const project1 = getEnsuredTestPath("explorer/scenario1/project1");
		const project2 = getEnsuredTestPath("explorer/scenario1/project2");
		const expectedFilePath = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/TestMe.java");

		explorerToTest.searchFolders.add(project1);
		explorerToTest.searchFolders.add(project2);

		/* execute */
		const locationString = "com/example/TestMe.java";
		var found = explorerToTest.searchFor(locationString);

		/* test */
		assert.strictEqual(1, found.size,"Not expected size of locations returned");
		const firstValue = found.values().next().value;
		assert.deepStrictEqual(firstValue,expectedFilePath,"must be expected result path");

	});

	test('scenario1_projects_SameName_java_found', () => {
		/* prepare */
		explorerToTest = new FileLocationExplorer();
		
		const project1 = getEnsuredTestPath("explorer/scenario1/project1");
		const project2 = getEnsuredTestPath("explorer/scenario1/project2");
		const expectedFilePath1 = getEnsuredTestPath("explorer/scenario1/project1/src/main/java/com/example/SameName.java");
		const expectedFilePath2 = getEnsuredTestPath("explorer/scenario1/project1/src/test/java/com/example/subpackage/SameName.java");

		explorerToTest.searchFolders.add(project1);
		explorerToTest.searchFolders.add(project2);

		/* execute */
		const locationString = "SameName.java";
		var found = explorerToTest.searchFor(locationString);

		/* test */
		assert.strictEqual(2, found.size,"Not expected size of locations returned");
		const valuesIterator = found.values();
		const firstValue = valuesIterator.next().value;
		const secondValue = valuesIterator.next().value;
		assert.deepStrictEqual(firstValue,expectedFilePath1,"must be expected result path");
		assert.deepStrictEqual(secondValue,expectedFilePath2,"must be expected result path");

	});

	function getEnsuredTestPath(testfile: String): string {
		let testReportLocation = path.dirname(__filename) + "../../../../src/test/resources/" + testfile;
		var resolved = path.resolve(testReportLocation);
		if (!fs.existsSync(resolved)) {
			assert.fail("test report location does not exist:"+resolved);
		}
		return resolved;
	}

});

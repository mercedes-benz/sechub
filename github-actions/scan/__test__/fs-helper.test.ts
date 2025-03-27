// SPDX-License-Identifier: MIT

import * as fs_extra from 'fs-extra';
import { deleteDirectoryExceptGivenFile } from '../src/fs-helper';

describe('deleteDirectoryExceptGivenFile', () => {
    const tempTestDirectory = '__test__/tmp-test-dir';
    const testDataDirectory = '__test__/data/delete-resources-1/folder-to-cleanup';

    const validFileToKeep = tempTestDirectory+'/file-to-keep.txt';
    const fileToDelete1 = tempTestDirectory+'/file-to-delete.txt';
    const folderToDelete = tempTestDirectory+'/folder-to-delete';
    const fileToDelete2 = tempTestDirectory+'/folder-to-delete/file-to-delete.txt';

    beforeEach(() => {
        // copy test data to temporary directory
        fs_extra.copySync(testDataDirectory, tempTestDirectory);

        // check precoditions that all files where copied
        expect(fs_extra.existsSync(tempTestDirectory)).toBe(true);
        expect(fs_extra.existsSync(validFileToKeep)).toBe(true);
        expect(fs_extra.existsSync(fileToDelete1)).toBe(true);
        expect(fs_extra.existsSync(folderToDelete)).toBe(true);
        expect(fs_extra.existsSync(fileToDelete2)).toBe(true);
    });

    afterEach(() =>{
        // delete temporary directory after test
        fs_extra.removeSync(tempTestDirectory);
        // check after the test everything is deleted from the temporary test directory
        expect(fs_extra.existsSync(tempTestDirectory)).toBe(false);
        expect(fs_extra.existsSync(validFileToKeep)).toBe(false);
        expect(fs_extra.existsSync(fileToDelete1)).toBe(false);
        expect(fs_extra.existsSync(folderToDelete)).toBe(false);
        expect(fs_extra.existsSync(fileToDelete2)).toBe(false);
    });
   
    it('if the file to keep is not in the directory to clean nothing is done', async () => {
        /* prepare */
        const invalidFileToKeep = '__test__/data/delete-resources-1/folder-to-cleanup/file-to-keep.txt';
        expect(fs_extra.existsSync(invalidFileToKeep)).toBe(true);

        /* execute */
        deleteDirectoryExceptGivenFile(tempTestDirectory, invalidFileToKeep);

        /* test */
        expect(fs_extra.existsSync(invalidFileToKeep)).toBe(true);
        expect(fs_extra.existsSync(fileToDelete1)).toBe(true);
        expect(fs_extra.existsSync(folderToDelete)).toBe(true);
        expect(fs_extra.existsSync(fileToDelete2)).toBe(true);
    });

    it('deleting everything inside the directory to clean up except the given file', async () => {
        /* execute */
        deleteDirectoryExceptGivenFile(tempTestDirectory, validFileToKeep);

        /* test */
        expect(fs_extra.existsSync(validFileToKeep)).toBe(true);
        expect(fs_extra.existsSync(fileToDelete1)).toBe(false);
        expect(fs_extra.existsSync(folderToDelete)).toBe(false);
        expect(fs_extra.existsSync(fileToDelete2)).toBe(false);
    });
});
// SPDX-License-Identifier: MIT

import * as fs_extra from 'fs-extra';
import * as path from 'path';
import { deleteDirectoryExceptGivenFile } from '../src/fs-helper';

jest.mock('fs-extra');

describe('deleteDirectoryExceptGivenFile', () => {
    const directoryToCleanUp = '/path/to/directory';
    const fileToKeep = '/path/to/directory/fileToKeep.txt';
    const tempFile = `${path.dirname(path.resolve(directoryToCleanUp))}/${path.basename(path.resolve(fileToKeep))}`;

    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('should move the file to a temporary location, delete the directory, recreate it, and move the file back', async () => {
        /* prepare */
        (fs_extra.moveSync as jest.Mock).mockResolvedValue(undefined);
        (fs_extra.removeSync as jest.Mock).mockResolvedValue(undefined);
        (fs_extra.ensureDirSync as jest.Mock).mockResolvedValue(undefined);

        /* execute */
        deleteDirectoryExceptGivenFile(directoryToCleanUp, fileToKeep);

        /* test */
        expect(fs_extra.moveSync).toHaveBeenCalledWith(path.resolve(fileToKeep), tempFile);
        expect(fs_extra.removeSync).toHaveBeenCalledWith(directoryToCleanUp);
        expect(fs_extra.ensureDirSync).toHaveBeenCalledWith(path.dirname(path.resolve(fileToKeep)));
        expect(fs_extra.moveSync).toHaveBeenCalledWith(tempFile, path.resolve(fileToKeep));
    });

    it('should not perform any operations if the file is not inside the given directory', async () => {
        /* execute */
        deleteDirectoryExceptGivenFile('/does/not/contain/fileToKeep', fileToKeep);

        /* test */
        expect(fs_extra.moveSync).not.toHaveBeenCalled();
        expect(fs_extra.removeSync).not.toHaveBeenCalled();
        expect(fs_extra.ensureDirSync).not.toHaveBeenCalled();
    });
});
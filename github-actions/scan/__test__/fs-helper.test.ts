// SPDX-License-Identifier: MIT

import * as fs_extra from 'fs-extra';
import * as path from 'path';
import { deleteDirectoryExceptGivenFile } from '../src/fs-helper';

jest.mock('fs-extra');

describe('deleteDirectoryExceptGivenFile', () => {
    const directoryToCleanUp = '/path/to/directory';
    const fileToKeep = '/path/to/directory/fileToKeep.txt';
    const tempFile = `${path.dirname(path.resolve(directoryToCleanUp))}'/'${path.basename(path.resolve(fileToKeep))}`;

    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('should move the file to a temporary location, delete the directory, recreate it, and move the file back', async () => {
        /* prepare */
        (fs_extra.existsSync as jest.Mock).mockReturnValue(true);
        (fs_extra.moveSync as jest.Mock).mockResolvedValue(undefined);
        (fs_extra.removeSync as jest.Mock).mockResolvedValue(undefined);
        (fs_extra.ensureDirSync as jest.Mock).mockResolvedValue(undefined);

        /* execute */
        await deleteDirectoryExceptGivenFile(directoryToCleanUp, fileToKeep);

        /* test */
        expect(fs_extra.moveSync).toHaveBeenCalledWith(path.resolve(fileToKeep), tempFile);
        expect(fs_extra.removeSync).toHaveBeenCalledWith(directoryToCleanUp);
        expect(fs_extra.ensureDirSync).toHaveBeenCalledWith(path.dirname(path.resolve(fileToKeep)));
        expect(fs_extra.moveSync).toHaveBeenCalledWith(tempFile, path.resolve(fileToKeep));
    });

    it('should not perform any operations if the file to keep does not exist', async () => {
        /* prepare */
        (fs_extra.existsSync as jest.Mock).mockReturnValue(false);

        /* execute */
        await deleteDirectoryExceptGivenFile(directoryToCleanUp, fileToKeep);

        /* test */
        expect(fs_extra.moveSync).not.toHaveBeenCalled();
        expect(fs_extra.removeSync).not.toHaveBeenCalled();
        expect(fs_extra.ensureDirSync).not.toHaveBeenCalled();
    });
});
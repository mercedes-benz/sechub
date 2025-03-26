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
        (fs_extra.move as jest.Mock).mockResolvedValue(undefined);
        (fs_extra.remove as jest.Mock).mockResolvedValue(undefined);
        (fs_extra.ensureDir as jest.Mock).mockResolvedValue(undefined);

        /* execute */
        await deleteDirectoryExceptGivenFile(directoryToCleanUp, fileToKeep);

        /* test */
        expect(fs_extra.move).toHaveBeenCalledWith(path.resolve(fileToKeep), tempFile);
        expect(fs_extra.remove).toHaveBeenCalledWith(directoryToCleanUp);
        expect(fs_extra.ensureDir).toHaveBeenCalledWith(path.dirname(path.resolve(fileToKeep)));
        expect(fs_extra.move).toHaveBeenCalledWith(tempFile, path.resolve(fileToKeep));
    });

    it('should not perform any operations if the file to keep does not exist', async () => {
        /* prepare */
        (fs_extra.existsSync as jest.Mock).mockReturnValue(false);

        /* execute */
        await deleteDirectoryExceptGivenFile(directoryToCleanUp, fileToKeep);

        /* test */
        expect(fs_extra.move).not.toHaveBeenCalled();
        expect(fs_extra.remove).not.toHaveBeenCalled();
        expect(fs_extra.ensureDir).not.toHaveBeenCalled();
    });
});
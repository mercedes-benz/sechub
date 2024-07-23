import * as core from '@actions/core';
import * as toolCache from '@actions/tool-cache';
import * as os from 'os';
import * as path from 'path';
import { setupSecHubCli } from '../src/client-download';

jest.mock('@actions/core');
jest.mock('@actions/tool-cache');

describe('setupSecHubCli', () => {
    const version = '1.0.0';
    const platformDirectory = os.platform() === 'win32' ? 'win' : 'unix';
    const expectedPath = path.join(os.tmpdir(), `platform/${platformDirectory}`, 'sechub');

    beforeEach(() => {
        jest.resetAllMocks();
    });

    it('finds and returns the path to the cached SecHub CLI', async () => {
        (toolCache.find as jest.Mock).mockReturnValue(expectedPath);

        const result = await setupSecHubCli(version);

        expect(result).toContain('sechub');
        expect(core.addPath).toHaveBeenCalledWith(expectedPath);
        expect(toolCache.downloadTool).not.toHaveBeenCalled();
    });

    it('downloads, caches, and returns the path to the SecHub CLI if not cached', async () => {
        (toolCache.find as jest.Mock).mockReturnValue('');
        (toolCache.downloadTool as jest.Mock).mockResolvedValue('path/to/downloaded.zip');
        (toolCache.extractZip as jest.Mock).mockResolvedValue('path/to/extracted');
        (toolCache.cacheDir as jest.Mock).mockResolvedValue(expectedPath);

        const result = await setupSecHubCli(version);

        expect(result).toContain('sechub');
        expect(core.addPath).toHaveBeenCalledWith(expectedPath);
        expect(toolCache.downloadTool).toHaveBeenCalled();
        expect(toolCache.extractZip).toHaveBeenCalled();
        expect(toolCache.cacheDir).toHaveBeenCalled();
    });

    it('handles errors during download or caching', async () => {
        (toolCache.find as jest.Mock).mockReturnValue('');
        (toolCache.downloadTool as jest.Mock).mockRejectedValue(new Error('Download failed'));

        await expect(setupSecHubCli(version)).rejects.toThrow('Download failed');

        expect(core.addPath).not.toHaveBeenCalled();
    });
});

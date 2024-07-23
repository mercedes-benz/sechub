// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as toolCache from '@actions/tool-cache';
import * as path from 'path';
import * as os from 'os';
import { getPlatform, getPlatformDirectory } from './platform-helper';

const SECHUB_TOOL = 'sechub';
const SECHUB_EXECUTABLE = getPlatform() === 'win32' ? 'sechub.exe' : 'sechub';

/**
 * Sets up the SecHub CLI by downloading and caching it.
 *
 * @param version the version of the client to download
 */
export async function setupSecHubCli(version: string): Promise<string> {
    const dir = await findOrDownload(version);
    core.addPath(dir);
    core.info(`${SECHUB_TOOL} ${version} is now set up at ${dir}`);
    return path.join(dir, SECHUB_EXECUTABLE);
}

/**
 * Finds the SecHub CLI in the cache or downloads it if it is not found.
 *
 * @param version the version of the client to find or download
 */
async function findOrDownload(version: string): Promise<string> {
    const existingDir = toolCache.find(SECHUB_TOOL, version);

    if (existingDir) {
        core.debug(`Found cached ${SECHUB_TOOL} ${version} at ${existingDir}`);
        return existingDir;
    } else {
        core.debug(`${SECHUB_TOOL} ${version} not cached, so attempting to download`);
        return await downloadClientRelease(version);
    }
}

/**
 * Downloads release for the SecHub CLI.
 *
 * @param clientVersion the version of the client to download
 */
async function downloadClientRelease(clientVersion: string): Promise<string> {
    const zipDownloadUrl = `https://github.com/mercedes-benz/sechub/releases/download/v${clientVersion}-client/sechub-cli-${clientVersion}.zip`;

    core.debug(`SecHub-Client download URL: ${zipDownloadUrl}`);

    const archivePath = await toolCache.downloadTool(zipDownloadUrl);

    const extracted = await toolCache.extractZip(archivePath, os.tmpdir());
    const releaseFolder = path.join(extracted, `platform/${getPlatformDirectory()}`);
    return await toolCache.cacheDir(releaseFolder, SECHUB_TOOL, clientVersion);
}

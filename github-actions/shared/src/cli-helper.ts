// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as shell from 'shelljs';
import * as settingsFile from './settings.json';
import { SecHubJson } from './types';
import * as configFile from './sechub.json';
import * as toolCache from '@actions/tool-cache';
import * as path from 'path';
import * as os from 'os';

const SECHUB_TOOL = 'sechub';
const ARCHIVE_SUBDIR_PATH = 'platform/linux-386';

export async function setupSecHubCli(version: string): Promise<string> {
    const dir = await findOrDownload(version);
    core.addPath(dir);
    core.info(`${SECHUB_TOOL} ${version} is now set up at ${dir}`);
    return dir;
}

async function findOrDownload(version: string): Promise<string> {
    const existingDir = toolCache.find(SECHUB_TOOL, version);

    if (existingDir) {
        core.debug(`Found cached ${SECHUB_TOOL} at ${existingDir}`);
        return existingDir;
    } else {
        core.debug(`${SECHUB_TOOL} not cached, so attempting to download`);
        return await downloadRelease(version);
    }
}

/**
 * Downloads a release for the SecHub CLI.
 * @param version The version that should be downloaded
 */
async function downloadRelease(version: string): Promise<string> {
    const zipUrl = `https://github.com/mercedes-benz/sechub/releases/download/v${version}-client/sechub-cli-${version}.zip`;
    core.debug('SecHub-Url: ' + zipUrl);

    const archivePath = await toolCache.downloadTool(zipUrl);

    const archiveDest = path.join(os.homedir(), 'tmp');
    const extracted = await toolCache.extractZip(archivePath, archiveDest);
    const releaseFolder = path.join(extracted, ARCHIVE_SUBDIR_PATH);
    return await toolCache.cacheDir(releaseFolder, SECHUB_TOOL, version);
}

/**
 * Creates the sechub.json with the given user input values.
 * @param includeFolders Which folders should be included
 * @param excludeFolders Which folders should be excluded
 */
export function createSecHubJsonFile(includeFolders: string[] | null, excludeFolders: string[] | null) {
    core.info('Config-Path was not found. Config will be manually created...');
    const secHubJson = createSecHubJson(includeFolders, excludeFolders);
    const stringifiedSecHubJson = JSON.stringify(secHubJson);
    core.debug('SecHub-Config: ' + stringifiedSecHubJson);

    shell.ShellString(stringifiedSecHubJson).to(settingsFile.secHubJsonFileName);
}

/**
 * Creates the object for the sechub.json with the given user input values.
 * @param includeFolders Which folders should be included
 * @param excludeFolders Which folders should be excluded
 */
function createSecHubJson(includeFolders: string[] | null, excludeFolders: string[] | null): SecHubJson {
    const sechubJson: SecHubJson = configFile;
    if (includeFolders) {
        sechubJson.codeScan.fileSystem.folders = includeFolders;
    }

    if (excludeFolders) {
        sechubJson.codeScan.excludes = excludeFolders;
    }
    return sechubJson;
}

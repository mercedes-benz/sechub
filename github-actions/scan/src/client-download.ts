// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as fs from 'fs';
import { ensureDirectorySync, downloadFile, unzipFile, chmodSync } from './fs-helper';
import { LaunchContext } from './launcher';

/**
 * Downloads release for the SecHub CLI if not already loaded.
 *
 * @param context launch context
 */
export async function downloadClientRelease(context: LaunchContext): Promise<void> {
    const clientVersion = context.clientVersion;

    if (fs.existsSync(context.clientExecutablePath)) {
        core.debug(`Client already downloaded - skip download. Path:${context.clientExecutablePath}`);
        return;
    }

    const secHubZipFilePath = `${context.clientDownloadFolder}/sechub.zip`;
    const zipDownloadUrl = `https://github.com/mercedes-benz/sechub/releases/download/v${clientVersion}-client/sechub-cli-${clientVersion}.zip`;

    core.debug(`SecHub-Client download URL: ${zipDownloadUrl}`);
    core.debug(`SecHub-Client download folder: ${context.clientDownloadFolder}`);

    ensureDirectorySync(context.clientDownloadFolder);
    await downloadFile(zipDownloadUrl, secHubZipFilePath);
    await unzipFile(secHubZipFilePath, context.clientDownloadFolder);
    chmodSync(context.clientExecutablePath);
}


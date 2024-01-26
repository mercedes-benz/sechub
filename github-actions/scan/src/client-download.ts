// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as fs from 'fs';
import { shellExecSynchOrFail } from './fs-helper';
import { LaunchContext } from './launcher';

/**
 * Downloads release for the SecHub CLI if not already loaded.
 * 
 * @param version The version that should be downloaded
 */
export function downloadClientRelease(context: LaunchContext): void {
    const clientVersion = context.inputData.sechubCLIVersion;

    if (fs.existsSync(context.clientExecutablePath)) {
        core.debug(`Client already downloaded - skip download. Path:${context.clientExecutablePath}`);
        return;
    }   

    const secHubZipFilePath = `${context.clientDownloadFolder}/sechub.zip`;
    const zipDownloadUrl = `https://github.com/mercedes-benz/sechub/releases/download/v${clientVersion}-client/sechub-cli-${clientVersion}.zip`;
    
    core.debug(`SecHub-Client download URL: ${zipDownloadUrl}`);
    core.debug(`SecHub-Client download folder: ${context.clientDownloadFolder}`);
    
    shellExecSynchOrFail(`mkdir ${context.clientDownloadFolder} -p`);

    shellExecSynchOrFail(`curl -L ${zipDownloadUrl} -o ${secHubZipFilePath}`);
    shellExecSynchOrFail(`unzip -o ${secHubZipFilePath} -d ${context.clientDownloadFolder}`);
    shellExecSynchOrFail(`chmod +x ${secHubZipFilePath}`);
}

